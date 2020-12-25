package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.ShipObject;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.bullets.BulletData;
import com.deo.flapd.model.bullets.EnemyBullet;
import com.deo.flapd.utils.DUtils;
import com.deo.flapd.utils.JsonEntry;

import static com.deo.flapd.utils.DUtils.enemyDisposes;
import static com.deo.flapd.utils.DUtils.enemyFireDisposes;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.lerpToColor;

public class Enemy extends Entity {

    public EnemyData data;

    private BulletData bulletData;
    private Array<EnemyBullet> bullets;

    private AssetManager assetManager;

    boolean queuedForDeletion = false;
    private boolean explosionFinished = false;

    private Sound explosionSound;
    private Sound shootingSound;
    private float volume;

    private float difficulty;

    private Animation<TextureRegion> enemyAnimation;
    private float animationPosition;

    private Enemies enemies;

    private ShipObject player;
    private Polygon playerBounds;
    private Bullet playerBullet;

    Enemy(AssetManager assetManager, EnemyData data, Enemies enemies, ShipObject ship) {
        this.assetManager = assetManager;
        this.data = data;
        this.enemies = enemies;
        player = ship;
        playerBounds = player.bounds;
        playerBullet = player.bullet;
        difficulty = getFloat("difficulty");

        if (data.spawnsBullets) {
            bulletData = new BulletData(data.enemyInfo, data.type);
            shootingSound = Gdx.audio.newSound(Gdx.files.internal(bulletData.shootSound));
        }

        if (data.spawnsDrones) {
            shootingSound = Gdx.audio.newSound(Gdx.files.internal(data.droneSpawnSound));
        }

        explosionSound = Gdx.audio.newSound(Gdx.files.internal(data.explosionSound));
        volume = getFloat("soundVolume");

        bullets = new Array<>();
        if (data.hasAnimation) {
            entitySprite = new Sprite();
            enemyAnimation = new Animation<TextureRegion>(data.frameDuration, assetManager.get(data.texture, TextureAtlas.class).findRegions(data.name), Animation.PlayMode.LOOP);
        } else {
            entitySprite = new Sprite(assetManager.get("enemies/enemies.atlas", TextureAtlas.class).findRegion(data.texture));
        }

        health = data.health;

        width = data.width;
        height = data.height;
        x = data.x;
        y = data.y;
        speed = data.speed;
        rotation = data.rotation;
        originX = width / 2f;
        originY = height / 2f;

        super.init();
        for (int i = 0; i < data.fireEffects.length; i++) {
            ParticleEffect fire = new ParticleEffect();
            fire.load(Gdx.files.internal(data.fireEffects[i]), Gdx.files.internal("particles"));
            fire.scaleEffect(data.fireScales[i]);
            data.fireParticleEffects.set(i, fire);
            fire.start();
        }
        data.explosionParticleEffect = new ParticleEffect();
        data.explosionParticleEffect.load(Gdx.files.internal(data.explosion), Gdx.files.internal("particles"));
        data.explosionParticleEffect.scaleEffect(data.explosionScale);
    }

    void draw(SpriteBatch batch) {
        if (!isDead && !data.hasAnimation) {
            entitySprite.draw(batch);
        } else if (data.hasAnimation && !isDead) {
            entitySprite.setRegion(enemyAnimation.getKeyFrame(animationPosition));
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

    void update(float delta) {

        super.update();

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
                                y - (player.bounds.getY() + player.bounds.getBoundingRectangle().getHeight() / 2f),
                                x - (player.bounds.getX() + player.bounds.getBoundingRectangle().getWidth() / 2f)),
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

            for (int i = 0; i < playerBullet.bullets.size; i++) {
                if (playerBullet.bullets.get(i).overlaps(entityHitBox)) {
                    color = Color.valueOf(data.hitColor);
                    health -= playerBullet.damages.get(i);
                    GameLogic.Score += 30 + 10 * (playerBullet.damages.get(i) / 50 - 1);
                    playerBullet.removeBullet(i, true);
                }
            }

            if (playerBullet.laser.getBoundingRectangle().overlaps(entityHitBox)) {
                color = Color.valueOf(data.hitColor);
                if (health > 0) {
                    GameLogic.Score += 10;
                }
                health -= playerBullet.damage / 10f;
            }

            if (playerBounds.getBoundingRectangle().overlaps(entityHitBox)) {
                kill();
                player.takeDamage(health / 3f);
            }

            if (health <= 0) {
                kill();
            }

            if (player.repellentField.getBoundingRectangle().overlaps(entityHitBox) && player.Charge >= player.bonusPowerConsumption * delta) {
                float shipWidth = playerBounds.getBoundingRectangle().getWidth();
                float shipHeight = playerBounds.getBoundingRectangle().getWidth();
                float shipX = playerBounds.getBoundingRectangle().getX();
                float shipY = playerBounds.getBoundingRectangle().getY();
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
                player.Charge -= player.bonusPowerConsumption * delta;
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
        for (int i = 0; i < bulletData.bulletsPerShot; i++) {
            BulletData newBulletData = new BulletData(data.enemyInfo, data.type);

            newBulletData.x = x + width / 2f + MathUtils.cosDeg(rotation + newBulletData.bulletAngle) * newBulletData.bulletDistance;
            newBulletData.y = y + height / 2f + MathUtils.sinDeg(rotation + newBulletData.bulletAngle) * newBulletData.bulletDistance;

            newBulletData.angle = getRandomInRange(-10, 10) * newBulletData.spread + rotation;
            if (data.canAim) {
                newBulletData.angle += MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(newBulletData.y - playerBounds.getY(), newBulletData.x - playerBounds.getX()), data.aimMinAngle, data.aimMaxAngle);
            }
            bullets.add(new EnemyBullet(assetManager, newBulletData, player));
        }
        shootingSound.play(volume);
        data.millis = 0;
    }

    private void spawnDrone() {
        float x = this.x + width / 2f + MathUtils.cosDeg(rotation + data.droneAngle) * data.droneDistance;
        float y = this.y + height / 2f + MathUtils.sinDeg(rotation + data.droneAngle) * data.droneDistance;
        for (int i = 0; i < data.dronesPerSpawn; i++) {
            EnemyData droneData = new EnemyData(enemies.enemiesJson.get(data.droneType), data.type).setNewPosition(x, y);
            droneData.health *= difficulty;
            enemies.enemyEntities.add(new Enemy(assetManager, droneData, enemies, player));
        }
        shootingSound.play(volume);
        data.millis = 0;
    }

    void dispose() {
        for (int i = 0; i < data.fireParticleEffects.size; i++) {
            data.fireParticleEffects.get(i).dispose();
            enemyFireDisposes++;
        }
        data.fireParticleEffects.clear();
        data.explosionParticleEffect.dispose();
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).dispose();
        }
        explosionSound.dispose();
        if (data.spawnsBullets) {
            shootingSound.dispose();
        }
        enemyDisposes++;
    }

    private void kill() {
        for (int i = 0; i < data.fireParticleEffects.size; i++) {
            data.fireParticleEffects.get(i).dispose();
            enemyFireDisposes++;
        }
        data.fireParticleEffects.clear();
        data.explosionParticleEffect.setPosition(x + originX, y + originY);
        data.explosionParticleEffect.start();
        isDead = true;

        GameLogic.enemiesKilled++;

        UraniumCell.Spawn(entityHitBox, (int) (getRandomInRange(data.moneyCount[0], data.moneyCount[1]) * difficulty), 1, data.moneyTimer);
        if (getRandomInRange(0, 100) <= data.bonusChance) {
            Bonus.Spawn(getRandomInRange(data.bonusType[0], data.bonusType[1]), entityHitBox);
        }

        Drops.drop(entityHitBox, (int) (getRandomInRange(data.dropCount[0], data.dropCount[1]) * difficulty), data.dropTimer, getRandomInRange(data.dropRarity[0], data.dropRarity[1]));

        explosionSound.play(volume);
    }
}

class EnemyData {

    String name;
    String texture;
    String explosionSound;
    String explosion;
    ParticleEffect explosionParticleEffect;
    float explosionScale;

    float x;
    float y;
    float rotation;

    float width;
    float height;

    int fireEffectCount;
    int[] fireOffsetsX;
    int[] fireOffsetsY;
    String[] fireEffects;
    float[] fireScales;
    Array<ParticleEffect> fireParticleEffects;
    Array<Float> fireParticleEffectAngles;
    Array<Float> fireParticleEffectDistances;

    int health;

    int speed;

    float shootingDelay;

    float millis;

    String hitColor;

    String type;

    int[] spawnHeight;

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

    int[] droneSpawnOffset;
    float droneAngle;
    float droneDistance;

    boolean spawnsBullets;

    boolean hasAnimation;

    float frameDuration;

    public boolean canAim;
    public float aimMinAngle;
    public float aimMaxAngle;

    JsonEntry enemyInfo;

    EnemyData(JsonEntry enemyInfo, String type) {

        JsonEntry enemyBodyInfo = enemyInfo.get(type, "body");
        fireParticleEffects = new Array<>();
        fireParticleEffectAngles = new Array<>();
        fireParticleEffectDistances = new Array<>();
        this.type = type;
        this.enemyInfo = enemyInfo;

        name = enemyInfo.name;
        texture = enemyBodyInfo.getString("texture");
        explosionSound = enemyBodyInfo.getString("explosionSound");
        explosion = enemyBodyInfo.getString("explosionEffect");
        explosionScale = enemyBodyInfo.getFloat("explosionScale");

        width = enemyBodyInfo.getFloat("width");
        height = enemyBodyInfo.getFloat("height");

        x = 805;
        y = 0;

        fireEffectCount = enemyInfo.getInt(type, "body", "fire", "count");

        fireOffsetsX = new int[fireEffectCount];
        fireOffsetsY = new int[fireEffectCount];
        fireEffects = new String[fireEffectCount];
        fireScales = new float[fireEffectCount];
        fireParticleEffects.setSize(fireEffectCount);
        fireParticleEffectDistances.setSize(fireEffectCount);
        fireParticleEffectAngles.setSize(fireEffectCount);

        spawnHeight = enemyInfo.getIntArray("spawnHeight");
        spawnDelay = enemyInfo.getFloat("spawnDelay");
        enemyCountSpawnConditions = enemyInfo.getIntArray("spawnConditions", "enemiesKilled");
        scoreSpawnConditions = enemyInfo.getIntArray("spawnConditions", "score");
        onBossWave = enemyInfo.getBoolean("spawnConditions", "bossWave");

        for (int i = 0; i < fireEffectCount; i++) {
            fireOffsetsX[i] = enemyBodyInfo.getIntArray("fire", "offset" + i)[0];
            fireOffsetsY[i] = enemyBodyInfo.getIntArray("fire", "offset" + i)[1];
            fireEffects[i] = enemyBodyInfo.getString("fire", "effect" + i);
            fireScales[i] = enemyBodyInfo.getFloat("fire", "scale" + i);
            fireParticleEffectAngles.set(i, MathUtils.atan2(fireOffsetsY[i], fireOffsetsX[i]) * MathUtils.radiansToDegrees);
            fireParticleEffectDistances.set(i, (float) Math.sqrt(fireOffsetsY[i] * fireOffsetsY[i] + fireOffsetsX[i] * fireOffsetsX[i]));
        }

        hasAnimation = enemyBodyInfo.getBoolean("hasAnimation");
        if (hasAnimation) {
            frameDuration = enemyBodyInfo.getFloat("frameDuration");
        }

        isHoming = enemyBodyInfo.getBoolean("homing");

        if (isHoming) {
            explosionTimer = enemyBodyInfo.getFloat("explosionTimer");
            idealExplosionTimer = enemyBodyInfo.getFloat("explosionTimer");
            homingSpeed = enemyBodyInfo.getFloat("homingSpeed");
        }

        spawnsDrones = enemyBodyInfo.getBoolean("spawnsDrones");

        spawnsBullets = enemyBodyInfo.getBoolean("spawnsBullets");

        health = enemyBodyInfo.getInt("health");

        speed = enemyBodyInfo.getInt("speed");

        if (spawnsBullets) {
            shootingDelay = enemyBodyInfo.getFloat("shootingDelay");
            canAim = enemyBodyInfo.getBoolean("canAim");
            if (canAim) {
                aimMinAngle = enemyBodyInfo.getFloatArray("aimAngleLimit")[0];
                aimMaxAngle = enemyBodyInfo.getFloatArray("aimAngleLimit")[1];
            }
        }

        if (spawnsDrones) {
            droneSpawnDelay = enemyBodyInfo.getFloat("droneSpawnDelay");
            dronesPerSpawn = enemyBodyInfo.getInt("dronesPerSpawn");
            droneType = enemyBodyInfo.getString("droneType");
            droneSpawnSound = enemyBodyInfo.getString("droneSpawnSound");
            droneSpawnOffset = enemyBodyInfo.getIntArray("droneSpawnOffset");
            droneAngle = MathUtils.atan2(droneSpawnOffset[1], droneSpawnOffset[0]) * MathUtils.radiansToDegrees;
            droneDistance = (float) Math.sqrt(droneSpawnOffset[1] * droneSpawnOffset[1] + droneSpawnOffset[0] * droneSpawnOffset[0]);
        }

        hitColor = enemyBodyInfo.getString("hitColor");

        dropTimer = enemyInfo.getInt("drops", "timer");
        dropCount = enemyInfo.getIntArray("drops", "count");
        dropRarity = enemyInfo.getIntArray("drops", "rarity");

        moneyCount = enemyInfo.getIntArray("money", "count");
        moneyTimer = enemyInfo.getInt("money", "timer");

        bonusChance = enemyInfo.getInt("bonuses", "chance");
        bonusType = enemyInfo.getIntArray("bonuses", "type");

        millis = 0;
    }

    protected EnemyData clone() {
        EnemyData copy = new EnemyData(enemyInfo, type);
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
