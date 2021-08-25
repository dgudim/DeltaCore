package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.utils.JsonEntry;

import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getString;

public class Bullet {
    
    // TODO: 7/22/2021 rework this crap
    private final Player player;
    private final Enemies enemies;
    private final Rectangle playerBounds;
    public final Array<Rectangle> bullets;
    private final Array<Float> damages;
    private final Array<ParticleEffect> trails;
    private final Array<ParticleEffect> disposedTrails;
    private final Array<Float> trailCountDownTimers;
    private final Array<Float> degrees;
    private final Array<Float> explosionTimers;
    private final Array<ParticleEffect> explosions;
    private final Array<Boolean> types;
    private final Sprite bullet;
    
    private Music laserSaw;
    private Sound shot;
    
    private final float soundVolume;
    
    private float spread;
    
    private final Array<Boolean> explosionQueue;
    public final Array<Boolean> remove_Bullet;
    
    private float shootingSpeedMultiplier;
    private float powerConsumption;
    
    public Sprite laser;
    
    private final String bulletExplosionEffect;
    private final String bulletTrailEffect;
    private final boolean hasTrail;
    
    private float width, height;
    
    public float damage;
    private int baseDamage;
    private int bulletSpeed;
    private int bulletsPerShot;
    private final float bulletTrailTimer;
    
    private float millis;
    
    private float laserHeight;
    private float laserDuration, currentDuration;
    private String laserColor;
    private boolean isLaserActive;
    public boolean isLaser;
    private final boolean isHoming;
    private float homingSpeed;
    private float explosionTimer;
    
    private final int gunCount;
    private int currentActiveGun;
    private final float[] gunOffsetsX;
    private final float[] gunOffsetsY;
    
    public Bullet(AssetManager assetManager, Player player, Enemies enemies, boolean newGame) {
        
        this.player = player;
        playerBounds = this.player.bounds;
        
        this.enemies = enemies;
        
        TextureAtlas bullets = assetManager.get("bullets/bullets.atlas");
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        JsonEntry shipConfig = new JsonEntry(new JsonReader().parse(Gdx.files.internal("player/shipConfigs.json")).get(getString("currentArmour")));
        
        JsonEntry currentCannon = treeJson.get(getString("currentCannon"));
        
        gunCount = shipConfig.getInt(1, "gunCount");
        currentActiveGun = 0;
        
        gunOffsetsX = new float[gunCount];
        gunOffsetsY = new float[gunCount];
        
        for (int i = 0; i < gunCount; i++) {
            gunOffsetsX[i] = shipConfig.getFloat(0, "guns", "gun" + i + "OffsetX");
            gunOffsetsY[i] = shipConfig.getFloat(0, "guns", "gun" + i + "OffsetY");
        }
        
        bulletExplosionEffect = currentCannon.getString("particles/explosion3.p", "usesEffect");
        
        if (currentCannon.getBoolean(false, "usesTrail")) {
            bulletTrailEffect = currentCannon.getString("particles/bullet_trail_left.p", "trailEffect");
            bulletTrailTimer = currentCannon.getFloat(5, "trailFadeOutTimer");
            hasTrail = true;
        } else {
            bulletTrailEffect = "";
            bulletTrailTimer = 0;
            hasTrail = false;
        }
        
        String[] params = currentCannon.getStringArray(new String[]{}, "parameters");
        float[] paramValues = currentCannon.getFloatArray(new float[]{}, "parameterValues");
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("damage")) {
                damage = (int) paramValues[i];
                baseDamage = (int) paramValues[i];
            }
            if (params[i].endsWith("shooting speed")) {
                shootingSpeedMultiplier = paramValues[i];
            }
            if (params[i].endsWith("spread")) {
                spread = paramValues[i];
            }
            if (params[i].endsWith("power consumption")) {
                powerConsumption = paramValues[i];
            }
            if (params[i].endsWith("bullet speed")) {
                bulletSpeed = (int) paramValues[i];
            }
            if (params[i].endsWith("bullets per shot")) {
                bulletsPerShot = (int) paramValues[i];
            }
            if (params[i].endsWith("laser beam thickness")) {
                laserHeight = paramValues[i];
                isLaser = true;
            }
            if (params[i].endsWith("laser pulse duration")) {
                laserDuration = paramValues[i];
                currentDuration = paramValues[i];
            }
        }
        
        if (isLaser) {
            bullet = new Sprite();
            laserColor = currentCannon.getString("#00FFFF", "laserBeamColor");
        } else {
            bullet = new Sprite(bullets.findRegion("bullet_" + getItemCodeNameByName(getString("currentCannon"))));
        }
        
        params = treeJson.getStringArray(new String[]{}, getString("currentCore"), "parameters");
        paramValues = treeJson.getFloatArray(new float[]{}, getString("currentCore"), "parameterValues");
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("damage multiplier")) {
                damage *= paramValues[i];
                baseDamage *= paramValues[i];
            }
        }
        
        isHoming = currentCannon.getBoolean(false, "homing");
        if (isHoming) {
            explosionTimer = currentCannon.getFloat(3, "explosionTimer");
            homingSpeed = currentCannon.getFloat(9, "homingSpeed");
        }
        
        this.bullets = new Array<>();
        damages = new Array<>();
        degrees = new Array<>();
        explosions = new Array<>();
        explosionQueue = new Array<>();
        remove_Bullet = new Array<>();
        types = new Array<>();
        trails = new Array<>();
        disposedTrails = new Array<>();
        trailCountDownTimers = new Array<>();
        explosionTimers = new Array<>();
        
        if (!newGame) {
            this.player.bulletsShot = getInteger("bulletsShot");
        } else {
            this.player.bulletsShot = 0;
        }
        
        laser = new Sprite(bullets.findRegion("bullet_laser"));
        laser.setSize(3, laserHeight);
        laser.setPosition(-100, -100);
        
        soundVolume = getFloat("soundVolume");
        if (isLaser) {
            laserSaw = assetManager.get("sfx/laser.ogg");
            laserSaw.play();
            laserSaw.setLooping(true);
            laserSaw.setVolume(0);
        } else {
            shot = assetManager.get("sfx/gun4.ogg");
        }
        
        width = bullet.getWidth();
        height = bullet.getHeight();
        
        float scale = 10 / height;
        width = width * scale;
        height = 10;
        
        bullet.setOrigin(0, 5);
    }
    
    public void Spawn(float damageMultiplier, boolean is_charged) {
        
        if (!isLaser) {
            if (player.Charge >= powerConsumption && millis > 11 / (shootingSpeedMultiplier + (GameLogic.bonuses_collected + 1) / 10.0f)) {
                for (int i = 0; i < bulletsPerShot; i++) {
                    Rectangle bullet = new Rectangle();
                    
                    bullet.setSize(width, height);
                    
                    bullet.x = playerBounds.getX() + gunOffsetsX[currentActiveGun];
                    bullet.y = playerBounds.getY() + gunOffsetsY[currentActiveGun];
                    
                    if (currentActiveGun + 1 < gunCount) {
                        currentActiveGun++;
                    } else {
                        currentActiveGun = 0;
                    }
                    
                    bullets.add(bullet);
                    explosionQueue.add(false);
                    remove_Bullet.add(false);
                    if (player.Charge >= powerConsumption * damageMultiplier / bulletsPerShot + 0.5f && is_charged) {
                        types.add(true);
                        damages.add(damage * damageMultiplier);
                        player.Charge -= powerConsumption * damageMultiplier / bulletsPerShot + 0.5f;
                    } else {
                        types.add(false);
                        damages.add(damage);
                        player.Charge -= powerConsumption / bulletsPerShot;
                    }
                    
                    float degree = player.rotation;
                    for (int i2 = 0; i2 < enemies.enemyEntities.size; i2++) {
                        if (enemies.enemyEntities.get(i2).overlaps(player.aimRadius.getBoundingRectangle())) {
                            Entity enemy = enemies.enemyEntities.get(i2);
                            degree = MathUtils.radiansToDegrees * MathUtils.atan2(enemy.y - bullet.y + enemy.height / 2, enemy.x - bullet.x + enemy.width / 2);
                            
                            if (degree < 45 && degree > -45) {
                                degree = 0;
                                break;
                            }
                        }
                    }
                    
                    degree += getRandomInRange(-10, 10) * spread;
                    
                    if (isHoming) {
                        explosionTimers.add(explosionTimer);
                        degree += 180;
                    }
                    
                    degrees.add(degree);
                    
                    bullet.x += MathUtils.cosDeg(player.rotation) * 6;
                    bullet.y += MathUtils.cosDeg(player.rotation);
                    
                    if (hasTrail) {
                        ParticleEffect trail = new ParticleEffect();
                        trail.load(Gdx.files.internal("particles/" + bulletTrailEffect + ".p"), Gdx.files.internal("particles"));
                        trail.start();
                        trails.add(trail);
                    }
                    player.bulletsShot++;
                }
                
                if (soundVolume > 0) {
                    shot.play(soundVolume / 100);
                }
                
                millis = 0;
            }
        } else {
            damage = baseDamage * (GameLogic.bonuses_collected + 1) / 10f;
        }
    }
    
    public float overlaps(Rectangle hitBox, boolean explode){
        for(int i = 0; i<bullets.size; i++){
            if(bullets.get(i).overlaps(hitBox) && !remove_Bullet.get(i)){
                removeBullet(i, explode);
                return damages.get(i);
            }
        }
        return 0;
    }
    
    public void updateLaser(boolean active) {
        isLaserActive = active;
    }
    
    public void draw(SpriteBatch batch, float delta) {
        
        if (!isLaser) {
            for (int i = 0; i < bullets.size; i++) {
                
                Rectangle bullet = bullets.get(i);
                float angle = degrees.get(i);
                
                if (hasTrail) {
                    trails.get(i).setPosition(bullet.x + bullet.width / 2, bullet.y + bullet.height / 2);
                }
                
                this.bullet.setPosition(bullet.x, bullet.y);
                this.bullet.setSize(bullet.width, bullet.height);
                if (types.get(i)) {
                    this.bullet.setColor(Color.GREEN);
                } else {
                    this.bullet.setColor(Color.WHITE);
                }
                
                if (!isHoming) {
                    
                    bullet.x += MathUtils.cosDeg(angle) * bulletSpeed * delta;
                    bullet.y += MathUtils.sinDeg(angle) * bulletSpeed * delta;
                    
                    this.bullet.setRotation(angle);
                } else {
                    
                    float posX = playerBounds.getX() + 1000;
                    float posY = playerBounds.getY();
                    for (int i2 = 0; i2 < enemies.enemyEntities.size; i2++) {
                        if (!enemies.enemyEntities.get(i2).isDead && enemies.enemyEntities.get(i2).x < posX && enemies.enemyEntities.get(i2).x > playerBounds.getX() + 150) {
                            posX = enemies.enemyEntities.get(i2).x + enemies.enemyEntities.get(i2).width / 2f;
                            posY = enemies.enemyEntities.get(i2).y + enemies.enemyEntities.get(i2).height / 2f;
                        }
                    }
                    degrees.set(i, MathUtils.lerpAngleDeg(angle, MathUtils.radiansToDegrees * MathUtils.atan2(bullet.y - posY, bullet.x - posX), homingSpeed / 700f));
                    this.bullet.setRotation(angle + 180);
                    bullet.x += MathUtils.cosDeg(angle + 180) * bulletSpeed * delta;
                    bullet.y += MathUtils.sinDeg(angle + 180) * bulletSpeed * delta;
                    explosionTimers.set(i, explosionTimers.get(i) - delta);
                    
                    if (explosionTimers.get(i) <= 0) {
                        removeBullet(i, true);
                    }
                }
                this.bullet.draw(batch);
                
                if (bullet.x > 800 || bullet.x < Math.max(bullet.height, bullet.width) || bullet.y > 480 || bullet.y < -Math.max(bullet.height, bullet.width)) {
                    removeBullet(i, false);
                }
            }
            for (int i3 = 0; i3 < explosions.size; i3++) {
                explosions.get(i3).draw(batch, delta);
                if (explosions.get(i3).isComplete()) {
                    explosions.get(i3).dispose();
                    explosions.removeIndex(i3);
                }
            }
            for (int i4 = 0; i4 < bullets.size; i4++) {
                if (explosionQueue.get(i4)) {
                    ParticleEffect explosionEffect = new ParticleEffect();
                    if (types.get(i4)) {
                        explosionEffect.load(Gdx.files.internal("particles/explosion3_4.p"), Gdx.files.internal("particles"));
                    } else {
                        explosionEffect.load(Gdx.files.internal("particles/" + bulletExplosionEffect + ".p"), Gdx.files.internal("particles"));
                    }
                    explosionEffect.setPosition(bullets.get(i4).x + bullets.get(i4).width / 2, bullets.get(i4).y + bullets.get(i4).height / 2);
                    explosionEffect.start();
                    explosions.add(explosionEffect);
                    explosionQueue.removeIndex(i4);
                    bullets.removeIndex(i4);
                    degrees.removeIndex(i4);
                    damages.removeIndex(i4);
                    remove_Bullet.removeIndex(i4);
                    types.removeIndex(i4);
                    if (hasTrail) {
                        disposedTrails.add(trails.get(i4));
                        trailCountDownTimers.add(bulletTrailTimer);
                        trails.removeIndex(i4);
                    }
                    if (isHoming) {
                        explosionTimers.removeIndex(i4);
                    }
                } else if (remove_Bullet.get(i4)) {
                    explosionQueue.removeIndex(i4);
                    bullets.removeIndex(i4);
                    degrees.removeIndex(i4);
                    damages.removeIndex(i4);
                    remove_Bullet.removeIndex(i4);
                    types.removeIndex(i4);
                    if (hasTrail) {
                        disposedTrails.add(trails.get(i4));
                        trailCountDownTimers.add(bulletTrailTimer);
                        trails.removeIndex(i4);
                    }
                    if (isHoming) {
                        explosionTimers.removeIndex(i4);
                    }
                }
            }
            for (int i = 0; i < disposedTrails.size; i++) {
                disposedTrails.get(i).draw(batch, delta);
                trailCountDownTimers.set(i, trailCountDownTimers.get(i) - delta);
                if (trailCountDownTimers.get(i) <= 0) {
                    disposedTrails.get(i).dispose();
                    disposedTrails.removeIndex(i);
                    trailCountDownTimers.removeIndex(i);
                }
            }
            for (int i = 0; i < trails.size; i++) {
                trails.get(i).draw(batch, delta);
            }
        } else if (isLaserActive && currentDuration >= 10) {
            laser.setRotation(player.rotation);
            laser.setPosition(playerBounds.getX() + MathUtils.cosDeg(laser.getRotation()) * 75, playerBounds.getY() + 16 / MathUtils.cosDeg(laser.getRotation()) + MathUtils.sinDeg(laser.getRotation()) * 80);
            laser.setSize(800, laserHeight);
            laser.setColor(Color.valueOf(laserColor));
            laser.draw(batch);
            laserSaw.setVolume(soundVolume / 100);
            currentDuration -= delta * 1000;
        } else {
            if (currentDuration < laserDuration) {
                if (player.Charge >= powerConsumption * delta * 10) {
                    player.Charge -= powerConsumption * delta * 10;
                    currentDuration += delta * 500;
                }
            }
            laser.setSize(0, 0);
            laser.setRotation(0);
            laser.setPosition(-100, -100);
            laserSaw.setVolume(0);
        }
        
        millis = millis + 50 * (GameLogic.bonuses_collected / 100.0f + 1) * delta * gunCount;
        
    }
    
    public void dispose() {
        bullets.clear();
        damages.clear();
        degrees.clear();
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        
        for (int i3 = 0; i3 < trails.size; i3++) {
            trails.get(i3).dispose();
            trails.removeIndex(i3);
        }
        
        for (int i3 = 0; i3 < disposedTrails.size; i3++) {
            disposedTrails.get(i3).dispose();
            disposedTrails.removeIndex(i3);
        }
        
        trailCountDownTimers.clear();
        explosionTimers.clear();
        explosionQueue.clear();
        remove_Bullet.clear();
        types.clear();
    }
    
    public void removeBullet(int i, boolean explode) {
        explosionQueue.set(i, explode);
        remove_Bullet.set(i, true);
    }
}
