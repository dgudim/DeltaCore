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
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.ShipObject;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.bullets.BulletData;
import com.deo.flapd.model.bullets.EnemyBullet;
import com.deo.flapd.utils.DUtils;

import static com.deo.flapd.utils.DUtils.enemyDisposes;
import static com.deo.flapd.utils.DUtils.enemyFireDisposes;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.lerpToColor;

public class Enemy extends Entity{

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
        originX = data.width / 2f;
        originY = data.health / 2f;
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
        if (!isDead) {
            if (!data.isHoming) {
                data.x = MathUtils.clamp(data.x, -data.width - 500, 800);
                data.y = MathUtils.clamp(data.y, 0, 480 - data.height);
            }
            super.update();

            if (!data.isHoming) {
                data.x -= data.speed * delta;
            } else {

                data.rotation = DUtils.lerpAngleWithConstantSpeed(data.rotation,
                        MathUtils.radiansToDegrees * MathUtils.atan2(
                                data.y - (player.bounds.getY() + player.bounds.getBoundingRectangle().getHeight() / 2f),
                                data.x - (player.bounds.getX() + player.bounds.getBoundingRectangle().getWidth() / 2f)),
                        data.homingSpeed, delta);
                data.x -= MathUtils.cosDeg(data.rotation) * data.speed * 2 * delta;
                data.y -= MathUtils.sinDeg(data.rotation) * data.speed * 2 * delta;
                data.explosionTimer -= delta;
                entitySprite.setColor(1, MathUtils.clamp(data.explosionTimer / data.idealExplosionTimer, 0, 1), MathUtils.clamp(data.explosionTimer / data.idealExplosionTimer, 0, 1), 1);
            }

            for (int i = 0; i < data.fireParticleEffects.size; i++) {
                data.fireParticleEffects.get(i).setPosition(
                        data.x + data.width / 2f + MathUtils.cosDeg(
                                data.rotation + data.fireParticleEffectAngles.get(i)) * data.fireParticleEffectDistances.get(i),
                        data.y + data.height / 2f + MathUtils.sinDeg(
                                data.rotation + data.fireParticleEffectAngles.get(i)) * data.fireParticleEffectDistances.get(i));
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

            if (data.x < -data.width - data.fireParticleEffects.get(0).getBoundingBox().getWidth() - 20) {
                isDead = true;
                explosionFinished = true;
            }

            if (data.isHoming && data.explosionTimer <= 0) {
                kill();
            }

            animationPosition += delta;

        }

        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).update(delta);
            if (bullets.get(i).queuedForDeletion) {
                bullets.get(i).dispose();
                bullets.removeIndex(i);
            }
        }

        for (int i = 0; i < playerBullet.bullets.size; i++) {
            if (playerBullet.bullets.get(i).overlaps(entityHitBox)) {
                color = Color.valueOf(data.hitColor);
                data.health -= playerBullet.damages.get(i);
                GameLogic.Score += 30 + 10 * (playerBullet.damages.get(i) / 50 - 1);
                playerBullet.removeBullet(i, true);
            }
        }

        if (playerBullet.laser.getBoundingRectangle().overlaps(entityHitBox)) {
            color = Color.valueOf(data.hitColor);
            if (data.health > 0) {
                GameLogic.Score += 10;
            }
            data.health -= playerBullet.damage / 10;
        }

        for (int i = 0; i < Meteorite.meteorites.size; i++) {
            if (Meteorite.meteorites.get(i).overlaps(entityHitBox)) {
                color = Color.valueOf(data.hitColor);
                if (data.health > Meteorite.healths.get(i)) {
                    data.health -= Meteorite.healths.get(i);
                    Meteorite.removeMeteorite(i, true);
                } else if (data.health == Meteorite.healths.get(i)) {
                    data.health = 0;
                    Meteorite.removeMeteorite(i, true);
                } else if (data.health < Meteorite.healths.get(i)) {
                    Meteorite.healths.set(i, Meteorite.healths.get(i) - data.health);
                    data.health = 0;
                }
            }
        }

        if (playerBounds.getBoundingRectangle().overlaps(entityHitBox)) {
            kill();
            player.takeDamage(data.health / 3f);
        }

        if (player.repellentField.getBoundingRectangle().overlaps(entityHitBox) && player.Charge >= player.bonusPowerConsumption * delta) {
            float shipWidth = playerBounds.getBoundingRectangle().getWidth();
            float shipHeight = playerBounds.getBoundingRectangle().getWidth();
            float shipX = playerBounds.getBoundingRectangle().getX();
            float shipY = playerBounds.getBoundingRectangle().getY();
            if (data.x > shipX + shipWidth / 2f && data.y > shipY + shipHeight / 2f) {
                data.x += 50 * delta;
                data.y += 50 * delta;
            } else if (data.x > shipX + shipWidth / 2f && data.y + data.height < shipY + shipHeight / 2f) {
                data.x += 50 * delta;
                data.y -= 50 * delta;
            } else if (data.x + data.width < shipX + shipWidth / 2f && data.y + data.height < shipY + shipHeight / 2f) {
                data.x -= 50 * delta;
                data.y -= 50 * delta;
            } else if (data.x + data.width < shipX + shipWidth / 2f && data.y > shipY + shipHeight / 2f) {
                data.x -= 50 * delta;
                data.y += 50 * delta;
            } else if (data.x > shipX + shipWidth) {
                data.x += 50 * delta;
            } else if (data.x + data.width < shipX) {
                data.x -= 50 * delta;
            } else if (data.y > shipY + shipHeight) {
                data.y += 50 * delta;
            } else if (data.y + data.height < shipY) {
                data.y -= 50 * delta;
            }
            player.Charge -= player.bonusPowerConsumption * delta;
        }

        if (data.health <= 0 && !isDead) {
            kill();
        }

        queuedForDeletion = isDead && bullets.size == 0 && (data.explosionParticleEffect.isComplete() || explosionFinished);
    }

    private void shoot() {
        for (int i = 0; i < bulletData.bulletsPerShot; i++) {
            BulletData newBulletData = new BulletData(data.enemyInfo, data.type);

            newBulletData.x = data.x + data.width / 2f + MathUtils.cosDeg(data.rotation + newBulletData.bulletAngle) * newBulletData.bulletDistance;
            newBulletData.y = data.y + data.height / 2f + MathUtils.sinDeg(data.rotation + newBulletData.bulletAngle) * newBulletData.bulletDistance;

            newBulletData.angle = getRandomInRange(-10, 10) * newBulletData.spread + data.rotation;
            if (data.canAim) {
                newBulletData.angle += MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(newBulletData.y - playerBounds.getY(), newBulletData.x - playerBounds.getX()), data.aimMinAngle, data.aimMaxAngle);
            }
            bullets.add(new EnemyBullet(assetManager, newBulletData, player));
        }
        shootingSound.play(volume);
        data.millis = 0;
    }

    private void spawnDrone() {
        float x = data.x + data.width / 2f + MathUtils.cosDeg(data.rotation + data.droneAngle) * data.droneDistance;
        float y = data.y + data.height / 2f + MathUtils.sinDeg(data.rotation + data.droneAngle) * data.droneDistance;
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
        data.explosionParticleEffect.setPosition(data.x + data.width / 2f, data.y + data.height / 2f);
        data.explosionParticleEffect.start();
        isDead = true;

        GameLogic.enemiesKilled++;

        UraniumCell.Spawn(entityHitBox, (int) (getRandomInRange(data.moneyCount[0], data.moneyCount[1]) * difficulty), 1, data.moneyTimer);
        if (getRandomInRange(0, 100) <= data.bonusChance) {
            Bonus.Spawn(getRandomInRange(data.bonusType[0], data.bonusType[1]), entityHitBox);
        }

        Drops.drop(entityHitBox, (int) (getRandomInRange(data.dropCount[0], data.dropCount[1]) * difficulty), data.dropTimer, getRandomInRange(data.dropRarity[0], data.dropRarity[1]));

        entitySprite.setPosition(-data.width - 100, -data.height - 100);
        data.x = -data.width - 100;
        data.y = -data.height - 100;

        explosionSound.play(volume);
    }

}
