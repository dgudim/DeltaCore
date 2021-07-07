package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.utils.JsonEntry;

import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getString;

public abstract class ShipObject {
    
    public Rectangle bounds;
    public float rotation;
    private final Sprite ship;
    private final Sprite shield;
    Sprite magnetField;
    public Sprite repellentField;
    Sprite aimRadius;
    
    private final ParticleEffect damage_fire;
    public ParticleEffect explosionEffect;
    
    private float red;
    private float green;
    private float blue;
    private float red2;
    private float green2;
    private float blue2;
    
    public boolean exploded;
    
    private final Sound explosion;
    
    private final float soundVolume;
    
    public float Health, Shield, Charge;
    
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
    
    private Animation<TextureRegion> enemyAnimation;
    private float animationPosition;
    private final boolean hasAnimation;
    
    private final float[] fireOffsetsX;
    private final float[] fireOffsetsY;
    
    private final Array<ParticleEffect> fires;
    
    public Bullet bullet;
    public int bulletsShot;
    
    ShipObject(AssetManager assetManager, float x, float y, boolean newGame, Enemies enemies) {
        
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
        
        float height = shipConfig.getFloat(1, "height");
        float width = shipConfig.getFloat(1, "width");
        
        hasAnimation = shipConfig.getBoolean(false, "hasAnimation");
        
        if (hasAnimation) {
            ship = new Sprite();
            enemyAnimation = new Animation<TextureRegion>(
                    shipConfig.getFloat(1, "frameDuration"),
                    assetManager.get("player/animations/" + shipConfig.getString("", "animation") + ".atlas", TextureAtlas.class)
                            .findRegions(shipConfig.getString("", "animation")),
                    Animation.PlayMode.LOOP);
        } else {
            ship = new Sprite(assetManager.get("items/items.atlas", TextureAtlas.class).findRegion(getItemCodeNameByName(getString("currentArmour"))));
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
        
        bounds = new Rectangle(0, 0, width, height);
        
        if (!newGame) {
            Shield = MathUtils.clamp(getFloat("Shield"), 0, shieldStrength * shieldStrengthMultiplier);
            Health = MathUtils.clamp(getFloat("Health"), 0, healthCapacity * healthMultiplier);
            Charge = MathUtils.clamp(getFloat("Charge"), 0, chargeCapacity * chargeCapacityMultiplier);
            bounds.setPosition(getFloat("ShipX"), getFloat("ShipY"));
        } else {
            Shield = shieldStrength * shieldStrengthMultiplier;
            Health = healthCapacity * healthMultiplier;
            Charge = chargeCapacity * chargeCapacityMultiplier;
            bounds.setPosition(x, y);
        }
        
        ship.setOrigin(width / 2f, height / 2f);
        shield.setOrigin((width + 30) / 2f, (height + 30) / 2f);
        
        ship.setSize(width, height);
        ship.setPosition(x, y);
        shield.setSize(width + 30, height + 30);
        shield.setPosition(x, y - 10);
        
        damage_fire = new ParticleEffect();
        damage_fire.load(Gdx.files.internal("particles/fire.p"), Gdx.files.internal("particles"));
        
        explosionEffect = new ParticleEffect();
        explosionEffect.load(Gdx.files.internal("particles/" + new JsonReader().parse(Gdx.files.internal("shop/tree.json")).get(getString("currentCore")).getString("usesEffect") + ".p"), Gdx.files.internal("particles"));
        
        damage_fire.start();
       
        red = 1;
        green = 1;
        blue = 1;
        red2 = 1;
        green2 = 1;
        blue2 = 1;
        exploded = false;
        soundVolume = getFloat("soundVolume");
        
        explosion = Gdx.audio.newSound(Gdx.files.internal("sfx/explosion.ogg"));
        
        bullet = new Bullet(assetManager, this, enemies, newGame);
    }
    
    public void drawEffects(SpriteBatch batch, float delta) {
        if (!exploded) {
            
            for (int i = 0; i < fires.size; i++) {
                fires.get(i).setPosition(bounds.getX() + fireOffsetsX[i], bounds.getY() + fireOffsetsY[i]);
                fires.get(i).draw(batch, delta);
            }
            
            damage_fire.setPosition(bounds.getX() + 10, bounds.getY() + 25);
            
            int fireLevel = Health < 70 ? 1 : 0 + Health < 50 ? 1 : 0 + Health < 30 ? 1 : 0;
            for(int i = 0; i<fireLevel; i++){
                damage_fire.draw(batch, delta);
            }
            
        } else {
            explosionEffect.draw(batch, delta);
        }
        bullet.draw(batch, delta);
    }
    
    public void draw(SpriteBatch batch, float delta) {
        if (!exploded) {
            ship.setPosition(bounds.getX(), bounds.getY());
            
            magnetField.setPosition(bounds.getX() + bounds.getWidth() / 2 - magnetField.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2 - magnetField.getHeight() / 2);
            repellentField.setPosition(bounds.getX() + bounds.getWidth() / 2 - repellentField.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2 - repellentField.getHeight() / 2);
            aimRadius.setPosition(bounds.getX() + bounds.getWidth() / 2 - aimRadius.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2 - aimRadius.getHeight() / 2);
            
            ship.setRotation(rotation);
            ship.setColor(red, green, blue, 1);
            
            if (red < 1) {
                red = red + 5 * delta;
            }
            if (green < 1) {
                green = green + 5 * delta;
            }
            if (blue < 1) {
                blue = blue + 5 * delta;
            }
            
            magnetField.draw(batch, Charge / (chargeCapacity * chargeCapacityMultiplier));
            repellentField.draw(batch, Charge / (chargeCapacity * chargeCapacityMultiplier));
            aimRadius.draw(batch, 1);
            
            if (hasAnimation) {
                ship.setRegion(enemyAnimation.getKeyFrame(animationPosition));
                animationPosition += delta;
            }
            
            ship.draw(batch);
            
            if (Charge + powerGeneration * delta < chargeCapacity * chargeCapacityMultiplier) {
                Charge += powerGeneration * delta;
            } else {
                Charge = chargeCapacity * chargeCapacityMultiplier;
            }
            if (Shield < shieldStrength * shieldStrengthMultiplier && Charge >= shieldPowerConsumption * delta) {
                Shield += shieldRegenerationSpeed * delta;
                Charge -= shieldPowerConsumption * delta;
            }
            
            if (Health <= 0 && !exploded) {
                explode();
            }
            
        } else {
            bounds.set(0, 0, 0, 0);
        }
    }
    
    void drawShield(SpriteBatch batch, float alpha, float delta) {
        if (!exploded) {
            shield.setPosition(bounds.getX() - 20, bounds.getY() - 15);
            shield.setRotation(rotation);
            shield.setColor(red2, green2, blue2, alpha);
            shield.draw(batch);
            
            if (red2 < 1) {
                red2 = red2 + 5 * delta;
            }
            if (green2 < 1) {
                green2 = green2 + 5 * delta;
            }
            if (blue2 < 1) {
                blue2 = blue2 + 5 * delta;
            }
            
        }
    }
    
    public void dispose() {
        
        for (int i = 0; i < fires.size; i++) {
            fires.get(i).dispose();
        }
        fires.clear();
        
        explosion.dispose();
        explosionEffect.dispose();
        damage_fire.dispose();
        bullet.dispose();
    }
    
    private void set_color(float red1, float green1, float blue1, boolean shield) {
        if (!shield) {
            red = red1;
            green = green1;
            blue = blue1;
        } else {
            red2 = red1;
            green2 = green1;
            blue2 = blue1;
        }
    }
    
    public void takeDamage(float damage) {
        if (Shield >= damage) {
            Shield -= damage;
            set_color(1, 0, 1, true);
        } else {
            Health = Health - (damage - Shield) / 5;
            Shield = 0;
            set_color(1, 0, 1, false);
        }
    }
    
    public void explode() {
        explosionEffect.setPosition(bounds.getX() + 25.6f, bounds.getY() + 35.2f);
        explosionEffect.start();
        exploded = true;
        Health = -1000;
        Shield = -1000;
        Charge = -1000;
        if (soundVolume > 0) {
            explosion.play(soundVolume / 100);
        }
    }
}
