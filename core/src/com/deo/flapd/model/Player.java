package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.model.bullets.PlayerBullet;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.utils.JsonEntry;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getItemTextureNameByName;
import static com.deo.flapd.utils.DUtils.getString;

public class Player extends Entity {
    
    private final Sprite shield;
    public Sprite magnetField;
    public Sprite repellentField;
    public Sprite aimRadius;
    
    private final ParticleEffect damage_fire;
    public ParticleEffect explosionEffect;
    
    private final Color shieldColor;
    private final Color shipColor;
    
    private final Sound explosion;
    
    private final float soundVolume;
    
    public float shieldCharge, charge;
    
    private float shieldRegenerationSpeed;
    private float shieldPowerConsumption;
    public float shieldStrength;
    public float shieldStrengthMultiplier = 1;
    
    private float powerGeneration;
    public float chargeCapacity;
    public float chargeCapacityMultiplier = 1;
    
    public float healthCapacity;
    public float healthMultiplier = 1;
    
    public float bonusPowerConsumption;
    
    private final float[] fireOffsetsX;
    private final float[] fireOffsetsY;
    
    private final Array<ParticleEffect> fires;
    
    public PlayerBullet bullet;
    public int bulletsShot;
    
    public Player(AssetManager assetManager, float x, float y, boolean newGame, Enemies enemies) {
        
        shieldColor = new Color(1, 1, 1, 1);
        shipColor = new Color(1, 1, 1, 1);
        
        TextureAtlas fields = assetManager.get("player/shields.atlas", TextureAtlas.class);
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        JsonEntry shipConfig = new JsonEntry(new JsonReader().parse(Gdx.files.internal("player/shipConfigs.json")).get(getString("currentArmour")));
        
        fires = new Array<>();
        
        int fireCount = shipConfig.getInt(1, "fireCount");
        
        fireOffsetsX = new float[fireCount];
        fireOffsetsY = new float[fireCount];
        
        float[] colors = new JsonReader().parse("{\"colors\":" + getString(treeJson.getString("fire_engine_left_red", getString("currentEngine"), "usesEffect") + "_color") + "}").get("colors").asFloatArray();
        
        for (int i = 0; i < fireCount; i++) {
            fireOffsetsX[i] = shipConfig.getFloat(0, "fires", "fire" + i + "OffsetX");
            fireOffsetsY[i] = shipConfig.getFloat(0, "fires", "fire" + i + "OffsetY");
            
            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal("particles/" + treeJson.getString("fire_engine_left_red", getString("currentEngine"), "usesEffect") + ".p"), Gdx.files.internal("particles"));
            fire.start();
            fire.getEmitters().get(0).getTint().setColors(colors);
            fires.add(fire);
        }
        
        hasAnimation = shipConfig.getBoolean(false, "hasAnimation");
        
        if (hasAnimation) {
            entitySprite = new Sprite();
            entityAnimation = new Animation<>(
                    shipConfig.getFloat(1, "frameDuration"),
                    assetManager.get("player/animations/" + shipConfig.getString("", "animation") + ".atlas", TextureAtlas.class)
                            .findRegions(shipConfig.getString("", "animation")),
                    Animation.PlayMode.LOOP);
        } else {
            entitySprite = new Sprite(assetManager.get("items/items.atlas", TextureAtlas.class).findRegion(getItemTextureNameByName(getString("currentArmour"))));
        }
        
        String[] params = treeJson.getStringArray(new String[]{}, getString("currentCore"), "parameters");
        float[] paramValues = treeJson.getFloatArray(new float[]{}, getString("currentCore"), "parameterValues");
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("power generation")) {
                powerGeneration = paramValues[i];
            }
            if (params[i].endsWith("health multiplier")) {
                healthMultiplier = paramValues[i];
            }
            if (params[i].endsWith("shield strength multiplier")) {
                shieldStrengthMultiplier = paramValues[i];
            }
            if (params[i].endsWith("charge capacity multiplier")) {
                chargeCapacityMultiplier = paramValues[i];
            }
        }
        
        params = treeJson.getStringArray(new String[]{}, getString("currentShield"), "parameters");
        paramValues = treeJson.getFloatArray(new float[]{}, getString("currentShield"), "parameterValues");
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("power consumption")) {
                shieldPowerConsumption = paramValues[i];
            }
            if (params[i].endsWith("shield regeneration speed")) {
                shieldRegenerationSpeed = paramValues[i];
            }
            if (params[i].endsWith("shield strength")) {
                shieldStrength = paramValues[i];
            }
        }
        
        magnetField = new Sprite(fields.findRegion("field_attractor"));
        repellentField = new Sprite(fields.findRegion("field_repellent"));
        aimRadius = new Sprite(fields.findRegion("circle"));
        magnetField.setSize(0, 0);
        repellentField.setSize(0, 0);
        aimRadius.setSize(0, 0);
        
        String bonus = getString("currentBonus");
        
        if (bonus.contains("magnet")) {
            params = treeJson.getStringArray(new String[]{}, bonus, "parameters");
            paramValues = treeJson.getFloatArray(new float[]{}, bonus, "parameterValues");
            for (int i = 0; i < params.length; i++) {
                if (params[i].endsWith("power consumption")) {
                    bonusPowerConsumption = paramValues[i];
                }
                if (params[i].endsWith("attraction radius")) {
                    magnetField.setSize(paramValues[i], paramValues[i]);
                }
            }
        }
        
        if (bonus.contains("repellent")) {
            params = treeJson.getStringArray(new String[]{}, bonus, "parameters");
            paramValues = treeJson.getFloatArray(new float[]{}, bonus, "parameterValues");
            for (int i = 0; i < params.length; i++) {
                if (params[i].endsWith("power consumption")) {
                    bonusPowerConsumption = paramValues[i];
                }
                if (params[i].endsWith("repellent radius")) {
                    repellentField.setSize(paramValues[i], paramValues[i]);
                }
            }
        }
        
        if (bonus.contains("radar")) {
            params = treeJson.getStringArray(new String[]{}, bonus, "parameters");
            paramValues = treeJson.getFloatArray(new float[]{}, bonus, "parameterValues");
            for (int i = 0; i < params.length; i++) {
                if (params[i].endsWith("aim radius")) {
                    aimRadius.setSize(paramValues[i], paramValues[i]);
                }
                if (params[i].endsWith("power consumption")) {
                    bonusPowerConsumption = paramValues[i];
                }
            }
        }
        
        chargeCapacity = treeJson.getFloatArray(new float[]{}, getString("currentBattery"), "parameterValues")[0];
        healthCapacity = treeJson.getFloatArray(new float[]{}, getString("currentArmour"), "parameterValues")[0];
        
        shield = new Sprite(fields.findRegion(treeJson.getString("explosion2", getString("currentShield"), "usesEffect")));
        
        if (!newGame) {
            shieldCharge = clamp(getFloat("Shield"), 0, shieldStrength * shieldStrengthMultiplier);
            health = clamp(getFloat("Health"), 0, healthCapacity * healthMultiplier);
            charge = clamp(getFloat("Charge"), 0, chargeCapacity * chargeCapacityMultiplier);
        } else {
            shieldCharge = shieldStrength * shieldStrengthMultiplier;
            health = healthCapacity * healthMultiplier;
            charge = chargeCapacity * chargeCapacityMultiplier;
        }
        
        this.x = x;
        this.y = y;
        setSize(shipConfig.getFloat(1, "width"), shipConfig.getFloat(1, "height"));
        init();
        
        shield.setOrigin((width + 30) / 2f, (height + 30) / 2f);
        
        shield.setSize(width + 30, height + 30);
        shield.setPosition(x, y - 10);
        
        damage_fire = new ParticleEffect();
        damage_fire.load(Gdx.files.internal("particles/fire.p"), Gdx.files.internal("particles"));
        
        explosionEffect = new ParticleEffect();
        explosionEffect.load(Gdx.files.internal("particles/" + new JsonReader().parse(Gdx.files.internal("shop/tree.json")).get(getString("currentCore")).getString("usesEffect") + ".p"), Gdx.files.internal("particles"));
        
        damage_fire.start();
        
        soundVolume = getFloat("soundVolume");
        
        explosion = assetManager.get("sfx/explosion.ogg");
        
        bullet = new PlayerBullet(assetManager, this, enemies, newGame);
    }
    
    public void scaleFireMotion(float motionScale){
        for (int i = 0; i < fires.size; i++) {
            fires.get(i).scaleEffect(1, motionScale);
        }
    }
    
    public void drawEffects(SpriteBatch batch, float delta) {
        if (!isDead) {
            
            for (int i = 0; i < fires.size; i++) {
                fires.get(i).setPosition(x + fireOffsetsX[i], y + fireOffsetsY[i]);
                fires.get(i).draw(batch, delta);
            }
            
            damage_fire.setPosition(x + 10, y + 25);
            
            int fireLevel = (health < 70 ? 1 : 0) + (health < 50 ? 1 : 0) + (health < 30 ? 1 : 0);
            for (int i = 0; i < fireLevel; i++) {
                damage_fire.draw(batch, delta / (float) fireLevel);
            }
            
        } else {
            explosionEffect.draw(batch, delta);
        }
        
    }
    
    public void drawBullets(SpriteBatch batch, float delta){
        bullet.draw(batch, delta);
    }
    
    public void updateBulletReload(float delta){
        bullet.updateReload(delta);
    }
    
    @Override
    protected void updateEntity(float delta) {
        entitySprite.setPosition(x, y);
        entitySprite.setRotation(rotation);
        entitySprite.setColor(shipColor);
        updateHealth(delta);
    }
    
    public void drawSprites(SpriteBatch batch, float delta) {
        if (!isDead) {
            updateEntity(delta);
            
            magnetField.setPosition(x + width / 2 - magnetField.getWidth() / 2, y + height / 2 - magnetField.getHeight() / 2);
            repellentField.setPosition(x + width / 2 - repellentField.getWidth() / 2, y + height / 2 - repellentField.getHeight() / 2);
            aimRadius.setPosition(x + width / 2 - aimRadius.getWidth() / 2, y + height / 2 - aimRadius.getHeight() / 2);
            
            shipColor.r = clamp(shipColor.r + 3.5f * delta, 0, 1);
            shipColor.g = clamp(shipColor.g + 3.5f * delta, 0, 1);
            shipColor.b = clamp(shipColor.b + 3.5f * delta, 0, 1);
            
            magnetField.draw(batch, charge / (chargeCapacity * chargeCapacityMultiplier));
            repellentField.draw(batch, charge / (chargeCapacity * chargeCapacityMultiplier));
            aimRadius.draw(batch, 1);
            
            if (hasAnimation) {
                entitySprite.setRegion(entityAnimation.getKeyFrame(animationPosition));
                animationPosition += delta;
            }
            
            entitySprite.draw(batch);
            
            if (charge + powerGeneration * delta < chargeCapacity * chargeCapacityMultiplier) {
                charge += powerGeneration * delta;
            } else {
                charge = chargeCapacity * chargeCapacityMultiplier;
            }
            if (shieldCharge < shieldStrength * shieldStrengthMultiplier && charge >= shieldPowerConsumption * delta) {
                shieldCharge += shieldRegenerationSpeed * delta;
                charge -= shieldPowerConsumption * delta;
            }
            
            if (health <= 0 && !isDead) {
                explode();
            }
            
        } else {
            entityHitBox.set(0, 0, 0, 0);
        }
    }
    
    public void drawShield(SpriteBatch batch, float delta) {
        if (!isDead) {
            shield.setPosition(x - 20, y - 15);
            shield.setRotation(rotation);
            shield.setColor(shieldColor.r, shieldColor.g, shieldColor.b, clamp(shieldCharge / 100, 0, 1));
            shield.draw(batch);
            
            shieldColor.r = clamp(shieldColor.r + 3.5f * delta, 0, 1);
            shieldColor.g = clamp(shieldColor.g + 3.5f * delta, 0, 1);
            shieldColor.b = clamp(shieldColor.b + 3.5f * delta, 0, 1);
        }
    }
    
    public void dispose() {
        
        for (int i = 0; i < fires.size; i++) {
            fires.get(i).dispose();
        }
        fires.clear();
        
        explosionEffect.dispose();
        damage_fire.dispose();
        bullet.dispose();
    }
    
    private void set_tintRed(boolean shield) {
        if (shield) {
            shieldColor.set(Color.RED);
        } else {
            shipColor.set(Color.RED);
        }
    }
    
    public void takeDamage(float damage) {
        if (shieldCharge >= damage) {
            shieldCharge -= damage;
            set_tintRed(true);
        } else {
            health -= (damage - shieldCharge) / 5;
            shieldCharge = 0;
            set_tintRed(false);
        }
    }
    
    public void explode() {
        explosionEffect.setPosition(x + 25.6f, y + 35.2f);
        explosionEffect.start();
        isDead = true;
        health = -1000;
        shieldCharge = -1000;
        charge = -1000;
        if (soundVolume > 0) {
            explosion.play(soundVolume / 100f);
        }
    }
}
