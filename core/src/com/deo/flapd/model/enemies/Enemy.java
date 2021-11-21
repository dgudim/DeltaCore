package com.deo.flapd.model.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Entity;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.bullets.BulletData;
import com.deo.flapd.model.bullets.EnemyBullet;
import com.deo.flapd.model.bullets.PlayerBullet;
import com.deo.flapd.model.loot.Bonuses;
import com.deo.flapd.model.loot.Drops;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.DUtils;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.SoundManager;

import static com.deo.flapd.utils.DUtils.drawParticleEffectBounds;
import static com.deo.flapd.utils.DUtils.getDistanceBetweenTwoPoints;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.lerpToColor;
import static com.deo.flapd.view.screens.LoadingScreen.particleEffectPoolLoader;

public class Enemy extends Entity {
    
    public EnemyData data;
    
    private final Array<EnemyBullet> bullets;
    
    private final CompositeManager compositeManager;
    private final AssetManager assetManager;
    private final Drops drops;
    
    boolean queuedForDeletion = false;
    private boolean explosionFinished = false;
    
    private final SoundManager soundManager;
    
    private final float difficulty;
    
    private final Enemies enemies;
    
    private final Player player;
    private final Rectangle playerBounds;
    private final PlayerBullet playerBullet;
    
    Enemy(CompositeManager compositeManager, EnemyData data, Enemies enemies, Player player) {
        this.compositeManager = compositeManager;
        assetManager = compositeManager.getAssetManager();
        drops = compositeManager.getDrops();
        soundManager = compositeManager.getSoundManager();
        
        this.data = data;
        this.enemies = enemies;
        this.player = player;
        hasAnimation = data.hasAnimation;
        playerBounds = this.player.entityHitBox;
        playerBullet = this.player.bullet;
        difficulty = getFloat(Keys.difficulty);
        
        bullets = new Array<>();
        if (hasAnimation) {
            entitySprite = new Sprite();
            entityAnimation = new Animation<>(data.frameDuration, assetManager.get(data.texture, TextureAtlas.class).findRegions(data.name), Animation.PlayMode.LOOP);
        } else {
            entitySprite = new Sprite(assetManager.get("enemies/enemies.atlas", TextureAtlas.class).findRegion(data.texture));
        }
        
        health = data.health;
        regeneration = data.regeneration;
        
        setSize(data.width, data.height);
        x = data.x;
        y = data.y;
        speed = data.speed;
        rotation = data.rotation;
        
        super.init();
        for (int i = 0; i < data.fireEffects.length; i++) {
            ParticleEffectPool.PooledEffect fire = particleEffectPoolLoader.getParticleEffectByPath(data.fireEffects[i]);
            fire.scaleEffect(data.fireScales[i]);
            fire.setPosition(
                    x + width / 2f + MathUtils.cosDeg(
                            rotation + data.fireParticleEffectAngles.get(i)) * data.fireParticleEffectDistances.get(i),
                    y + height / 2f + MathUtils.sinDeg(
                            rotation + data.fireParticleEffectAngles.get(i)) * data.fireParticleEffectDistances.get(i));
            data.fireParticleEffects.set(i, fire);
        }
        data.explosionParticleEffect = particleEffectPoolLoader.getParticleEffectByPath(data.explosionEffect);
        data.explosionParticleEffect.scaleEffect(data.explosionScale);
    }
    
    void draw(SpriteBatch batch) {
        if (!isDead && !hasAnimation) {
            entitySprite.draw(batch);
        } else if (hasAnimation && !isDead) {
            entitySprite.setRegion(entityAnimation.getKeyFrame(animationPosition));
            entitySprite.draw(batch);
        }
    }
    
    void drawEffects(SpriteBatch batch, float delta) {
        if (!isDead) {
            for (int i = 0; i < data.fireParticleEffects.size; i++) {
                data.fireParticleEffects.get(i).draw(batch, delta);
            }
        } else {
            data.explosionParticleEffect.draw(batch, delta);
        }
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).draw(batch, delta);
        }
    }
    
    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        super.drawDebug(shapeRenderer);
        shapeRenderer.setColor(Color.YELLOW);
        for (int i = 0; i < data.fireParticleEffects.size; i++) {
            drawParticleEffectBounds(shapeRenderer, data.fireParticleEffects.get(i));
        }
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).drawDebug(shapeRenderer);
        }
    }
    
    void update(float delta) {
        updateEntity(delta);
        
        if (!isDead) {
            
            if (!data.isHoming) {
                x = MathUtils.clamp(x, -width - 500, 800);
                y = MathUtils.clamp(y, 0, 480 - height);
            }
            
            if (!data.isHoming) {
                x -= speed * delta;
            } else {
                
                rotation = DUtils.lerpAngleWithConstantSpeed(rotation,
                        MathUtils.radiansToDegrees * MathUtils.atan2(
                                y - (player.y + player.height / 2f),
                                x - (player.x + player.width / 2f)),
                        data.homingSpeed, delta);
                x -= MathUtils.cosDeg(rotation) * speed * 2 * delta;
                y -= MathUtils.sinDeg(rotation) * speed * 2 * delta;
                data.explosionTimer -= delta;
                entitySprite.setColor(1, MathUtils.clamp(data.explosionTimer / data.idealExplosionTimer, 0, 1), MathUtils.clamp(data.explosionTimer / data.idealExplosionTimer, 0, 1), 1);
            }
            
            for (int i = 0; i < data.fireParticleEffects.size; i++) {
                data.fireParticleEffects.get(i).setPosition(
                        x + width / 2f + MathUtils.cosDeg(
                                rotation + data.fireParticleEffectAngles.get(i)) * data.fireParticleEffectDistances.get(i),
                        y + height / 2f + MathUtils.sinDeg(
                                rotation + data.fireParticleEffectAngles.get(i)) * data.fireParticleEffectDistances.get(i));
            }
            
            lerpToColor(color, Color.WHITE, 2.5f, delta);
            
            if (data.spawnsBullets) {
                if (data.millis > data.shootingDelay * 100) {
                    shoot();
                }
            }
            
            if (data.spawnsDrones) {
                if (data.millis > data.droneSpawnDelay * 100) {
                    spawnDrone();
                }
            }
            
            data.millis += delta * 20;
            
            if (x < -width - data.fireParticleEffects.get(0).getBoundingBox().getWidth() - 20) {
                isDead = true;
                explosionFinished = true;
            }
            
            if (data.isHoming && data.explosionTimer <= 0) {
                kill();
            }
            
            animationPosition += delta;
            
            float damage = playerBullet.overlaps(entityHitBox, true);
            if (damage > 0) {
                color = Color.valueOf(data.hitColor);
                health -= damage;
                GameLogic.score += 30 + 10 * (damage / 50 - 1);
            }
            
            if (overlaps(playerBullet.laser.getBoundingRectangle())) {
                color = Color.valueOf(data.hitColor);
                if (health > 0) {
                    GameLogic.score += 10;
                }
                health -= playerBullet.damage / 10f;
            }
            
            if (overlaps(playerBounds)) {
                kill();
                player.takeDamage(health / 3f);
            }
            
            if (health <= 0) {
                kill();
            }
            
            if (overlaps(player.repellentField.getBoundingRectangle()) && player.charge >= player.bonusPowerConsumption * delta) {
                float shipWidth = playerBounds.getWidth();
                float shipHeight = playerBounds.getWidth();
                float shipX = playerBounds.getX();
                float shipY = playerBounds.getY();
                if (x > shipX + shipWidth / 2f && y > shipY + shipHeight / 2f) {
                    x += 50 * delta;
                    y += 50 * delta;
                } else if (x > shipX + shipWidth / 2f && y + height < shipY + shipHeight / 2f) {
                    x += 50 * delta;
                    y -= 50 * delta;
                } else if (x + width < shipX + shipWidth / 2f && y + height < shipY + shipHeight / 2f) {
                    x -= 50 * delta;
                    y -= 50 * delta;
                } else if (x + width < shipX + shipWidth / 2f && y > shipY + shipHeight / 2f) {
                    x -= 50 * delta;
                    y += 50 * delta;
                } else if (x > shipX + shipWidth) {
                    x += 50 * delta;
                } else if (x + width < shipX) {
                    x -= 50 * delta;
                } else if (y > shipY + shipHeight) {
                    y += 50 * delta;
                } else if (y + height < shipY) {
                    y -= 50 * delta;
                }
                player.charge -= player.bonusPowerConsumption * delta;
            }
            
        }
        
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).update(delta);
            if (bullets.get(i).queuedForDeletion) {
                bullets.get(i).dispose();
                bullets.removeIndex(i);
            }
        }
        
        queuedForDeletion = isDead && bullets.size == 0 && (data.explosionParticleEffect.isComplete() || explosionFinished);
    }
    
    private void shoot() {
        for (int i = 0; i < data.bulletsPerShot; i++) {
            BulletData newBulletData = new BulletData(data.enemyInfo.get("bullet"));
            
            float newX = x + width / 2f + MathUtils.cosDeg(rotation + data.bulletOffsetAngle) * data.bulletOffsetDistance;
            float newY = y + height / 2f + MathUtils.sinDeg(rotation + data.bulletOffsetAngle) * data.bulletOffsetDistance;
            
            float newAngle = getRandomInRange(-10, 10) * data.bulletSpread + rotation;
            if (data.canAim) {
                newAngle += MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(newY - playerBounds.getY(), newX - playerBounds.getX()), data.aimMinAngle, data.aimMaxAngle);
            }
            
            bullets.add(new EnemyBullet(assetManager, newBulletData, player, newX, newY, newAngle, newBulletData.hasCollisionWithPlayerBullets));
        }
        soundManager.playSound_noLink(data.shootingSound);
        data.millis = 0;
    }
    
    private void spawnDrone() {
        float x = this.x + width / 2f + MathUtils.cosDeg(rotation + data.droneAngle) * data.droneDistance;
        float y = this.y + height / 2f + MathUtils.sinDeg(rotation + data.droneAngle) * data.droneDistance;
        for (int i = 0; i < data.dronesPerSpawn; i++) {
            EnemyData droneData = new EnemyData(enemies.enemiesJson.get(data.droneType)).setNewPosition(x, y);
            droneData.health *= difficulty;
            enemies.enemyEntities.add(new Enemy(compositeManager, droneData, enemies, player));
        }
        soundManager.playSound_noLink(data.droneSpawnSound);
        data.millis = 0;
    }
    
    void dispose() {
        for (int i = 0; i < data.fireParticleEffects.size; i++) {
            data.fireParticleEffects.get(i).free();
        }
        data.fireParticleEffects.clear();
        data.explosionParticleEffect.free();
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).dispose();
        }
    }
    
    private void kill() {
        for (int i = 0; i < data.fireParticleEffects.size; i++) {
            data.fireParticleEffects.get(i).free();
        }
        data.fireParticleEffects.clear();
        data.explosionParticleEffect.setPosition(x + originX, y + originY);
        isDead = true;
        
        GameLogic.enemiesKilled++;
        
        drops.dropMoney(entityHitBox, (int) (getRandomInRange(data.moneyCount[0], data.moneyCount[1]) * difficulty), data.moneyTimer);
        if (getRandomInRange(0, 100) <= data.bonusChance) {
            Bonuses.Spawn(getRandomInRange(data.bonusType[0], data.bonusType[1]), entityHitBox);
        }
        
        drops.drop(entityHitBox, (int) (getRandomInRange(data.dropCount[0], data.dropCount[1]) * difficulty), data.dropTimer, getRandomInRange(data.dropRarity[0], data.dropRarity[1]));
        
       soundManager.playSound_noLink(data.explosionSound);
    }
}

class EnemyData {
    
    String name;
    String texture;
    String explosionSound;
    String explosionEffect;
    ParticleEffectPool.PooledEffect explosionParticleEffect;
    float explosionScale;
    String shootingSound;
    
    float bulletOffsetAngle;
    float bulletOffsetDistance;
    int bulletsPerShot;
    float bulletSpread;
    
    float x;
    float y;
    float rotation;
    
    float width;
    float height;
    
    String[] fireEffects;
    float[] fireScales;
    Array<ParticleEffectPool.PooledEffect> fireParticleEffects;
    Array<Float> fireParticleEffectAngles;
    Array<Float> fireParticleEffectDistances;
    
    float health;
    float regeneration;
    int speed;
    float shootingDelay;
    
    float millis;
    
    String hitColor;
    
    private final int[] spawnHeight;
    int[] scoreSpawnConditions;
    float spawnDelay;
    int[] enemyCountSpawnConditions;
    
    int dropTimer;
    int moneyTimer;
    int[] dropRarity;
    int[] dropCount;
    int[] moneyCount;
    int bonusChance;
    int[] bonusType;
    
    boolean onBossWave;
    
    boolean isHoming;
    float homingSpeed;
    float explosionTimer;
    float idealExplosionTimer;
    
    boolean spawnsDrones;
    int dronesPerSpawn;
    float droneSpawnDelay;
    String droneType;
    String droneSpawnSound;
    float droneAngle;
    float droneDistance;
    
    boolean spawnsBullets;
    
    boolean hasAnimation;
    float frameDuration;
    
    public boolean canAim;
    public float aimMinAngle;
    public float aimMaxAngle;
    
    JsonEntry enemyInfo;
    
    EnemyData(JsonEntry enemyInfo) {
        
        this.enemyInfo = enemyInfo;
        
        name = enemyInfo.name;
        texture = enemyInfo.getString("noTexture", "texture");
        explosionSound = enemyInfo.getString("explosion", "explosionSound");
        explosionEffect = enemyInfo.getString("particles/explosion.p", "explosionEffect");
        explosionScale = enemyInfo.getFloat(1, "explosionScale");
        
        width = enemyInfo.getFloat(1, "width");
        height = enemyInfo.getFloat(1, "height");
        
        x = 805;
        y = 0;
        
        fireParticleEffects = new Array<>();
        fireParticleEffectAngles = new Array<>();
        fireParticleEffectDistances = new Array<>();
        
        int fireEffectCount = enemyInfo.getInt(1, "fire", "count");
        
        fireEffects = new String[fireEffectCount];
        fireScales = new float[fireEffectCount];
        fireParticleEffects.setSize(fireEffectCount);
        fireParticleEffectDistances.setSize(fireEffectCount);
        fireParticleEffectAngles.setSize(fireEffectCount);
        
        spawnHeight = enemyInfo.getIntArray(new int[]{20, 420}, "spawnHeight");
        spawnDelay = enemyInfo.getFloat(3, "spawnDelay");
        enemyCountSpawnConditions = enemyInfo.getIntArray(new int[]{-1, 10}, "spawnConditions", "enemiesKilled");
        scoreSpawnConditions = enemyInfo.getIntArray(new int[]{-1, 10000}, "spawnConditions", "score");
        onBossWave = enemyInfo.getBoolean(false, "spawnConditions", "bossWave");
        
        for (int i = 0; i < fireEffectCount; i++) {
            float[] fireOffset = enemyInfo.getFloatArray(new float[]{0, 0}, "fire", "offset" + i);
            fireEffects[i] = enemyInfo.getString("particles/fire_engine_left_blue.p", "fire", "effect" + i);
            fireScales[i] = enemyInfo.getFloat(1, "fire", "scale" + i);
            fireParticleEffectAngles.set(i, MathUtils.atan2(fireOffset[1], fireOffset[0]) * MathUtils.radiansToDegrees);
            fireParticleEffectDistances.set(i, getDistanceBetweenTwoPoints(0, 0, fireOffset[0], fireOffset[1]));
        }
        
        hasAnimation = texture.endsWith(".atlas");
        if (hasAnimation) {
            frameDuration = enemyInfo.getFloat(1, "frameDuration");
        }
        
        isHoming = enemyInfo.getBoolean(false, "homing");
        
        if (isHoming) {
            explosionTimer = enemyInfo.getFloat(3, "explosionTimer");
            idealExplosionTimer = enemyInfo.getFloat(3, "explosionTimer");
            homingSpeed = enemyInfo.getFloat(10, "homingSpeed");
        }
        
        spawnsDrones = enemyInfo.getBoolean(false, "spawnsDrones");
        
        spawnsBullets = enemyInfo.getBoolean(false, "spawnsBullets");
        
        health = enemyInfo.getFloat(100, "health");
        regeneration = enemyInfo.getFloat(0, "regeneration");
        
        speed = enemyInfo.getInt(100, "speed");
        
        if (spawnsBullets) {
            shootingSound = enemyInfo.getString("gun1", "shootSound");
            
            float[] bulletOffset = enemyInfo.getFloatArray(new float[]{0, 0}, "bulletOffset");
            bulletOffsetAngle = MathUtils.atan2(bulletOffset[1], bulletOffset[0]) * MathUtils.radiansToDegrees;
            bulletOffsetDistance = getDistanceBetweenTwoPoints(0, 0, bulletOffset[0], bulletOffset[1]);
            
            bulletsPerShot = enemyInfo.getInt(1, "bulletsPerShot");
            bulletSpread = enemyInfo.getFloat(0, "bulletSpread");
            
            shootingDelay = enemyInfo.getFloat(2, "shootingDelay");
            canAim = enemyInfo.getBoolean(false, "canAim");
            if (canAim) {
                aimMinAngle = enemyInfo.getFloatArray(new float[]{-30, 30}, "aimAngleLimit")[0];
                aimMaxAngle = enemyInfo.getFloatArray(new float[]{-30, 30}, "aimAngleLimit")[1];
            }
        }
        
        if (spawnsDrones) {
            droneSpawnDelay = enemyInfo.getFloat(3, "droneSpawnDelay");
            dronesPerSpawn = enemyInfo.getInt(1, "dronesPerSpawn");
            droneType = enemyInfo.getString(name, "droneType");
            droneSpawnSound = enemyInfo.getString("gun3", "droneSpawnSound");
            int[] droneSpawnOffset = enemyInfo.getIntArray(new int[]{0, 0}, "droneSpawnOffset");
            droneAngle = MathUtils.atan2(droneSpawnOffset[1], droneSpawnOffset[0]) * MathUtils.radiansToDegrees;
            droneDistance = getDistanceBetweenTwoPoints(0, 0, droneSpawnOffset[0], droneSpawnOffset[1]);
        }
        
        hitColor = enemyInfo.getString("#FF0000", "hitColor");
        
        dropTimer = enemyInfo.getInt(3, "drops", "timer");
        dropCount = enemyInfo.getIntArray(new int[]{1, 3}, "drops", "count");
        dropRarity = enemyInfo.getIntArray(new int[]{1, 3}, "drops", "rarity");
        
        moneyCount = enemyInfo.getIntArray(new int[]{1, 3}, "money", "count");
        moneyTimer = enemyInfo.getInt(3, "money", "timer");
        
        bonusChance = enemyInfo.getInt(50, "bonuses", "chance");
        bonusType = enemyInfo.getIntArray(new int[]{1, 3}, "bonuses", "type");
        
        millis = 0;
    }
    
    protected EnemyData clone() {
        EnemyData copy = new EnemyData(enemyInfo);
        copy.y = getRandomInRange(copy.spawnHeight[0], copy.spawnHeight[1]);
        copy.shootingDelay += getRandomInRange(-7, 7) / 100f;
        return copy;
    }
    
    protected EnemyData setNewPosition(float x, float y) {
        this.x = x;
        this.y = y;
        shootingDelay += getRandomInRange(-10, 10) / 100f;
        return this;
    }
}
