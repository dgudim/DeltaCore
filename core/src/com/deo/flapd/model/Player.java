package com.deo.flapd.model;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.control.GameVariables.bulletsShot;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.LogLevel.WARNING;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.lerpWithConstantSpeed;
import static com.deo.flapd.utils.DUtils.log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.control.GameVariables;
import com.deo.flapd.model.bullets.PlayerBullet;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.SoundManager;

public class Player extends Entity {
    
    private final Sprite shield;
    public Sprite magnetField;
    public Sprite repellentField;
    public Sprite aimRadius;
    
    private final ParticleEffect damage_fire;
    public ParticleEffect explosionEffect;
    
    private final Color shieldColor;
    private final Color shipColor;
    
    private final SoundManager soundManager;
    
    public float shieldCharge, charge;
    
    private float shieldRegenerationSpeed;
    private float shieldPowerConsumption;
    public float shieldCapacity;
    public float shieldStrengthMultiplier = 1;
    
    private float powerGeneration;
    public float chargeCapacity;
    public float chargeCapacityMultiplier = 1;
    
    public float healthCapacity;
    public float healthMultiplier = 1;
    
    public float bonusPowerConsumption;
    
    private float speed;
    private final float acceleration;
    
    private float speedX, speedY;
    
    private final float[] fireOffsetsX;
    private final float[] fireOffsetsY;
    
    private final Array<ParticleEffect> fires;
    
    CompositeManager compositeManager;
    Enemies enemies;
    
    public Array<PlayerBullet> bullets;
    private float bulletReloadTimer;
    
    private float bulletSpread;
    private float shootingSpeedMultiplier;
    private float powerConsumption;
    
    private int bulletsPerShot;
    
    private int gunCount;
    private int currentActiveGun;
    private float[] gunOffsetsX;
    private float[] gunOffsetsY;
    
    public Player(CompositeManager compositeManager, float x, float y, boolean newGame, Enemies enemies) {
        this.compositeManager = compositeManager;
        this.enemies = enemies;
        AssetManager assetManager = compositeManager.getAssetManager();
        soundManager = compositeManager.getSoundManager();
        
        shieldColor = new Color(1, 1, 1, 1);
        shipColor = new Color(1, 1, 1, 1);
        
        TextureAtlas fields = assetManager.get("player/shields.atlas", TextureAtlas.class);
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        JsonEntry shipConfig = new JsonEntry(new JsonReader().parse(Gdx.files.internal("player/shipConfigs.json")).get(getString(Keys.currentHull)));
        
        fires = new Array<>();
        
        int fireCount = shipConfig.getInt(1, "fireCount");
        
        fireOffsetsX = new float[fireCount];
        fireOffsetsY = new float[fireCount];
        
        float[] colors = new JsonReader().parse("{\"colors\":" + getString(treeJson.getString("fire_engine_left_red", getString(Keys.currentEngine), "usesEffect") + "_color") + "}").get("colors").asFloatArray();
        
        for (int i = 0; i < fireCount; i++) {
            fireOffsetsX[i] = shipConfig.getFloat(0, "fires", "fire" + i + "OffsetX");
            fireOffsetsY[i] = shipConfig.getFloat(0, "fires", "fire" + i + "OffsetY");
            
            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal("particles/" + treeJson.getString("fire_engine_left_red", getString(Keys.currentEngine), "usesEffect") + ".p"), Gdx.files.internal("particles"));
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
            entitySprite = new Sprite(assetManager.get("items/items.atlas", TextureAtlas.class).findRegion(getString(Keys.currentHull)));
        }
        
        float weight = 0;
        speed = treeJson.getFloat(false, 1, getString(Keys.currentEngine), "parameters", "parameter.speed");
        
        JsonEntry params_core = treeJson.get(getString(Keys.currentCore), "parameters");
        for (int i = 0; i < params_core.size; i++) {
            switch (params_core.get(i).name) {
                case ("parameter.power_generation"):
                    powerGeneration = params_core.getFloat(1, i);
                    break;
                case ("parameter.health_multiplier"):
                    healthMultiplier = params_core.getFloat(1, i);
                    break;
                case ("parameter.shield_strength_multiplier"):
                    shieldStrengthMultiplier = params_core.getFloat(1, i);
                    break;
                case ("parameter.charge_capacity_multiplier"):
                    chargeCapacityMultiplier = params_core.getFloat(1, i);
                    break;
                case ("parameter.speed_multiplier"):
                    speed *= params_core.getFloat(1, i);
                    break;
                case ("parameter.weight"):
                    weight += params_core.getFloat(1, i);
                    break;
                default:
                    log("unknown parameter " + params_core.get(i).name + " for " + getString(Keys.currentCore), WARNING);
                    break;
            }
        }
        
        JsonEntry params_shield = treeJson.get(getString(Keys.currentShield), "parameters");
        for (int i = 0; i < params_shield.size; i++) {
            switch (params_shield.get(i).name) {
                case ("parameter.power_consumption"):
                    shieldPowerConsumption = params_shield.getFloat(1, i);
                    break;
                case ("parameter.regeneration_speed"):
                    shieldRegenerationSpeed = params_shield.getFloat(1, i);
                    break;
                case ("parameter.shield_capacity"):
                    shieldCapacity = params_shield.getFloat(1, i);
                    break;
                case ("parameter.weight"):
                    weight += params_shield.getFloat(1, i);
                    break;
                default:
                    log("unknown parameter " + params_shield.get(i).name + " for " + getString(Keys.currentShield), WARNING);
                    break;
            }
        }
        
        magnetField = new Sprite(fields.findRegion("field_attractor"));
        repellentField = new Sprite(fields.findRegion("field_repellent"));
        aimRadius = new Sprite(fields.findRegion("circle"));
        magnetField.setSize(0, 0);
        repellentField.setSize(0, 0);
        aimRadius.setSize(0, 0);
        
        String module = getString(Keys.currentModule);
        JsonEntry params_module = treeJson.get(false, module, "parameters");
        
        if (!module.equals("none")) {
            for (int i = 0; i < params_module.size; i++) {
                switch (params_module.get(i).name) {
                    case ("parameter.power_consumption"):
                        bonusPowerConsumption = params_module.getFloat(1, i);
                        break;
                    case ("parameter.attraction_radius"):
                        if (module.equals("part.magnet")) {
                            float radius = params_module.getFloat(1, i);
                            magnetField.setSize(radius, radius);
                        }
                        break;
                    case ("parameter.repellent_radius"):
                        if (module.equals("part.repellent_field")) {
                            float radius = params_module.getFloat(1, i);
                            repellentField.setSize(radius, radius);
                        }
                        break;
                    case ("parameter.aim_radius"):
                        if (module.equals("part.radar")) {
                            float radius = params_module.getFloat(1, i);
                            aimRadius.setSize(radius, radius);
                        }
                        break;
                    case ("parameter.weight"):
                        weight += params_module.getFloat(1, i);
                        break;
                    default:
                        log("Player: no need to load " + params_module.get(i).name + " from " + module + ", ignoring", INFO);
                        break;
                }
            }
        }
        
        chargeCapacity = treeJson.getFloat(1, getString(Keys.currentBattery), "parameters", "parameter.capacity");
        healthCapacity = treeJson.getFloat(1, getString(Keys.currentHull), "parameters", "parameter.health");
        
        float accelerationForce = treeJson.getFloat(false, 1, getString(Keys.currentEngine), "parameters", "parameter.acceleration_force");
        
        weight += treeJson.getFloat(false, 1, getString(Keys.currentEngine), "parameters", "parameter.weight");
        weight += treeJson.getFloat(false, 1, getString(Keys.currentWeapon), "parameters", "parameter.weight");
        weight += treeJson.getFloat(false, 1, getString(Keys.currentBattery), "parameters", "parameter.weight");
        weight += treeJson.getFloat(false, 1, getString(Keys.currentHull), "parameters", "parameter.weight");
        
        acceleration = accelerationForce / weight;
        
        shield = new Sprite(fields.findRegion(treeJson.getString("noValue", getString(Keys.currentShield), "usesEffect")));
        
        if (!newGame) {
            shieldCharge = clamp(getFloat(Keys.playerShieldValue), 0, shieldCapacity * shieldStrengthMultiplier);
            health = clamp(getFloat(Keys.playerHealthValue), 0, healthCapacity * healthMultiplier);
            charge = clamp(getFloat(Keys.playerChargeValue), 0, chargeCapacity * chargeCapacityMultiplier);
        } else {
            shieldCharge = shieldCapacity * shieldStrengthMultiplier;
            health = healthCapacity * healthMultiplier;
            charge = chargeCapacity * chargeCapacityMultiplier;
        }
        
        this.x = x;
        this.y = y;
        setSize(shipConfig.getFloat(1, "width"), shipConfig.getFloat(1, "height"));
        init();
        
        shield.setSize(width + 30, height + 30);
        
        damage_fire = new ParticleEffect();
        damage_fire.load(Gdx.files.internal("particles/fire.p"), Gdx.files.internal("particles"));
        
        explosionEffect = new ParticleEffect();
        explosionEffect.load(Gdx.files.internal("particles/" + new JsonReader().parse(Gdx.files.internal("shop/tree.json")).get(getString(Keys.currentCore)).getString("usesEffect") + ".p"), Gdx.files.internal("particles"));
        
        damage_fire.start();
        
        if (!newGame) {
            bulletsShot = getInteger(Keys.bulletsShot);
        } else {
            bulletsShot = 0;
        }
        
        gunCount = shipConfig.getInt(1, "gunCount");
        currentActiveGun = 0;
        
        gunOffsetsX = new float[gunCount];
        gunOffsetsY = new float[gunCount];
        
        for (int i = 0; i < gunCount; i++) {
            gunOffsetsX[i] = shipConfig.getFloat(0, "guns", "gun" + i + "OffsetX");
            gunOffsetsY[i] = shipConfig.getFloat(0, "guns", "gun" + i + "OffsetY");
        }
        
        JsonEntry params_weapon = treeJson.get(getString(Keys.currentWeapon), "parameters");
        
        shootingSpeedMultiplier = params_weapon.getFloat(1, "parameter.shooting_speed");
        powerConsumption = params_weapon.getFloat(1, "parameter.power_consumption");
        bulletsPerShot = params_weapon.getInt(1, "parameter.bullets_per_shot");
        bulletSpread = params_weapon.getFloat(1, "parameter.spread");
        
        bullets = new Array<>();
    }
    
    public void update(float deltaX, float deltaY, float delta, boolean shooting, boolean shootingSecondary) {
        updateWeapon(shooting, shootingSecondary, delta);
        updateSpeed(deltaX, deltaY, delta);
    }
    
    public void updateWeapon(boolean shooting, boolean shootingSecondary, float delta) {
        
        bulletReloadTimer = bulletReloadTimer + 50 * (GameVariables.bonuses_collected / 100.0f + 1) * delta * gunCount;
        
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            shooting = true;
        if (Gdx.input.isKeyPressed(Input.Keys.M))
            shootingSecondary = true;
        
        if (shooting && charge >= powerConsumption && bulletReloadTimer > 10 / shootingSpeedMultiplier) {
            charge -= powerConsumption;
            for (int i = 0; i < bulletsPerShot; i++) {
                bullets.add(new PlayerBullet(compositeManager, enemies) {
                    @Override
                    public void calculateSpawnPosition() {
                        newX = Player.this.entityHitBox.getX() + gunOffsetsX[currentActiveGun];
                        newY = Player.this.entityHitBox.getY() + gunOffsetsY[currentActiveGun];
                        currentActiveGun++;
                        if (currentActiveGun >= gunCount) {
                            currentActiveGun = 0;
                        }
                        float degree = Player.this.rotation;
                        // TODO: 16/12/2021 implement module's logic (aim etc)
                        
                        degree += getRandomInRange(-10, 10) * bulletSpread;
                        
                        newRot = degree;
                    }
                });
            }
            bulletReloadTimer = 0;
            bulletsShot += bulletsPerShot;
            soundManager.playSound_noLink("gun4");
        }
    }
    
    public void updateSpeed(float deltaX, float deltaY, float delta) {
        
        if (Gdx.input.isKeyPressed(Input.Keys.W))
            deltaY = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            deltaX = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            deltaY = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            deltaX = 1;
        
        speedX = lerpWithConstantSpeed(speedX, speed * deltaX, acceleration, delta);
        speedY = lerpWithConstantSpeed(speedY, speed * deltaY, acceleration, delta);
        x = clamp(x + speedX * delta, 0, 800 - width);
        y = clamp(y + speedY * delta, 0, 480 - height);
        rotation = clamp((speedY / speed - speedX / speed) * 5, -9, 9);
    }
    
    public void scaleFireMotion(float motionScale) {
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
    
    public void drawBullets(SpriteBatch batch, float delta) {
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).update(delta);
            bullets.get(i).draw(batch, delta);
            if (bullets.get(i).queuedForDeletion) {
                bullets.get(i).dispose();
                bullets.removeIndex(i);
            }
        }
    }
    
    @Override
    protected void updateEntity(float delta) {
        entitySprite.setPosition(x, y);
        entitySprite.setRotation(rotation);
        entitySprite.setColor(shipColor);
        updateHealth(delta);
    }
    
    public float getCollisionDamageWithBullet(Entity entity, float delta){
        if(!entity.isDead){
            for(int i = 0; i < bullets.size; i++){
                if(bullets.get(i).overlaps(entity)){
                    float damage = bullets.get(i).getDamage(delta);
                    bullets.get(i).explode();
                    return damage;
                }
            }
        }
        return 0;
    }
    
    public void setHomingTarget(Entity homingTarget){
        for(int i = 0; i < bullets.size; i++){
            bullets.get(i).setHomingTarget(homingTarget);
        }
    }
    
    public void collideWithBullet(Entity entity, float delta){
        collideWithBullet(entity, false, delta);
    }
    
    public void collideWithBullet(Entity entity, boolean withBullet, float delta){
        if(!entity.isDead){
            for(int i = 0; i < bullets.size; i++){
                if(bullets.get(i).overlaps(entity, withBullet)){
                    float damage = bullets.get(i).getDamage(delta);
                    bullets.get(i).takeDamage(entity.health);
                    entity.takeDamage(damage);
                    break;
                }
            }
        }
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
            if (shieldCharge < shieldCapacity * shieldStrengthMultiplier && charge >= shieldPowerConsumption * delta) {
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
            shield.setPosition(x - 15, y - 15);
            
            shield.setRotation(rotation);
            shield.setColor(shieldColor.r, shieldColor.g, shieldColor.b, clamp(shieldCharge / 100, 0, 1));
            shield.draw(batch);
            
            shieldColor.r = clamp(shieldColor.r + 3.5f * delta, 0, 1);
            shieldColor.g = clamp(shieldColor.g + 3.5f * delta, 0, 1);
            shieldColor.b = clamp(shieldColor.b + 3.5f * delta, 0, 1);
        }
    }
    
    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        super.drawDebug(shapeRenderer);
        for(int i = 0; i < bullets.size; i++){
            bullets.get(i).drawDebug(shapeRenderer);
        }
    }
    
    public void dispose() {
        
        for (int i = 0; i < fires.size; i++) {
            fires.get(i).dispose();
        }
        fires.clear();
        
        explosionEffect.dispose();
        damage_fire.dispose();
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).dispose();
        }
    }
    
    private void set_tintRed(boolean shield) {
        if (shield) {
            shieldColor.set(Color.RED);
        } else {
            shipColor.set(Color.RED);
        }
    }
    
    @Override
    public void takeDamage(float damage) {
        if (shieldCharge >= damage) {
            shieldCharge -= damage;
            set_tintRed(true);
        } else {
            health -= (damage - shieldCharge) / 5;
            if(health <= 0){
                explode();
            }
            shieldCharge = 0;
            set_tintRed(false);
        }
    }
    
    @Override
    public void explode() {
        explosionEffect.setPosition(x + 25.6f, y + 35.2f);
        explosionEffect.start();
        isDead = true;
        health = -1000;
        shieldCharge = -1000;
        charge = -1000;
        soundManager.playSound_noLink("explosion");
    }
}
