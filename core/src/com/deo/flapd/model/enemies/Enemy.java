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

import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Enemy {

    public EnemyData data;
    private Sprite enemy;

    private BulletData bulletData;
    private Array<EnemyBullet> bullets;

    private AssetManager assetManager;

    public boolean isDead = false;
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
            enemy = new Sprite();
            enemyAnimation = new Animation<TextureRegion>(data.frameDuration, assetManager.get(data.texture, TextureAtlas.class).findRegions(data.name), Animation.PlayMode.LOOP);
        } else {
            enemy = new Sprite(assetManager.get("enemies/enemies.atlas", TextureAtlas.class).findRegion(data.texture));
        }
        enemy.setSize(data.width, data.height);
        enemy.setPosition(data.x, data.y);
        enemy.setOrigin(data.width / 2, data.height / 2);
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
            enemy.draw(batch);
        } else if (data.hasAnimation && !isDead) {
            enemy.setRegion(enemyAnimation.getKeyFrame(animationPosition));
            enemy.draw(batch);
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
            data.x = MathUtils.clamp(data.x, -data.width - 500, 800);
            data.y = MathUtils.clamp(data.y, 0, 480 - data.height);
            enemy.setPosition(data.x, data.y);
            enemy.setColor(data.currentColor);
            enemy.setRotation(data.rotation);

            if (!data.isHoming) {
                data.x -= data.speed * delta;
            } else {
                data.x = MathUtils.lerp(data.x, playerBounds.getX(), delta * data.speed / 220.0f);
                data.y = MathUtils.lerp(data.y, playerBounds.getY() + playerBounds.getBoundingRectangle().getHeight() / 2, delta * data.speed / 220.0f);
                data.explosionTimer -= delta;
                enemy.setColor(1, MathUtils.clamp(data.explosionTimer / data.idealExplosionTimer, 0, 1), MathUtils.clamp(data.explosionTimer / data.idealExplosionTimer, 0, 1), 1);
                if (data.rotateTowardsShip) {
                    data.rotation = MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(data.y - playerBounds.getY(), data.x - playerBounds.getX()), data.minAngle, data.maxAngle);
                }
            }

            for (int i = 0; i < data.fireParticleEffects.size; i++) {
                data.fireParticleEffects.get(i).setPosition(data.x + data.fireOffsetsX[i], data.y + data.fireOffsetsY[i]);
            }

            if (data.currentColor.r < 1) {
                data.currentColor.r = MathUtils.clamp(data.currentColor.r + delta * 2.5f, 0, 1);
            }
            if (data.currentColor.g < 1) {
                data.currentColor.g = MathUtils.clamp(data.currentColor.g + delta * 2.5f, 0, 1);
            }
            if (data.currentColor.b < 1) {
                data.currentColor.b = MathUtils.clamp(data.currentColor.b + delta * 2.5f, 0, 1);
            }

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
            if (playerBullet.bullets.get(i).overlaps(enemy.getBoundingRectangle())) {
                data.currentColor = Color.valueOf(data.hitColor);
                data.health -= playerBullet.damages.get(i);
                GameLogic.Score += 30 + 10 * (playerBullet.damages.get(i) / 50 - 1);
                playerBullet.removeBullet(i, true);
            }
        }

        if (playerBullet.laser.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())) {
            data.currentColor = Color.valueOf(data.hitColor);
            if (data.health > 0) {
                GameLogic.Score += 10;
            }
            data.health -= playerBullet.damage / 10;
        }

        for (int i = 0; i < Meteorite.meteorites.size; i++) {
            if (Meteorite.meteorites.get(i).overlaps(enemy.getBoundingRectangle())) {
                data.currentColor = Color.valueOf(data.hitColor);
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

        if (playerBounds.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())) {
            kill();
            player.takeDamage(data.health / 3f);
        }

        if (player.repellentField.getBoundingRectangle().overlaps(enemy.getBoundingRectangle()) && player.Charge >= player.bonusPowerConsumption * delta) {
            float shipWidth = playerBounds.getBoundingRectangle().getWidth();
            float shipHeight = playerBounds.getBoundingRectangle().getWidth();
            float shipX = playerBounds.getBoundingRectangle().getX();
            float shipY = playerBounds.getBoundingRectangle().getY();
            if (data.x > shipX + shipWidth / 2 && data.y > shipY + shipHeight / 2) {
                data.x += 50 * delta;
                data.y += 50 * delta;
            } else if (data.x > shipX + shipWidth / 2 && data.y + data.height < shipY + shipHeight / 2) {
                data.x += 50 * delta;
                data.y -= 50 * delta;
            } else if (data.x + data.width < shipX + shipWidth / 2 && data.y + data.height < shipY + shipHeight / 2) {
                data.x -= 50 * delta;
                data.y -= 50 * delta;
            } else if (data.x + data.width < shipX + shipWidth / 2 && data.y > shipY + shipHeight / 2) {
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

        queuedForDeletion = isDead && !(bullets.size > 0) && (data.explosionParticleEffect.isComplete() || explosionFinished);
    }

    private void shoot() {
        for (int i = 0; i < bulletData.bulletsPerShot; i++) {
            BulletData newBulletData = new BulletData(data.enemyInfo, data.type);
            newBulletData.x = data.x + newBulletData.offset[0];
            newBulletData.y = data.y + newBulletData.offset[1];
            newBulletData.angle = getRandomInRange(-10, 10) * newBulletData.spread + data.rotation;
            if(data.canAim){
                newBulletData.angle += MathUtils.clamp(MathUtils.radiansToDegrees * MathUtils.atan2(newBulletData.y - playerBounds.getY(), newBulletData.x - playerBounds.getX()), data.aimMinAngle, data.aimMaxAngle);
            }
            bullets.add(new EnemyBullet(assetManager, newBulletData, player ));
        }
        shootingSound.play(volume);
        data.millis = 0;
    }

    private void spawnDrone() {
        for (int i = 0; i < data.dronesPerSpawn; i++) {
            EnemyData droneData = new EnemyData(enemies.enemiesJson.get(data.droneType), data.type).useAsDroneData(data.x + data.droneSpawnOffset[0], data.y + data.droneSpawnOffset[1]);
            droneData.health *= difficulty;
            enemies.enemyEntities.add(new Enemy(assetManager, droneData, enemies, player));
        }
        shootingSound.play(volume);
        data.millis = 0;
    }

    void dispose() {
        for (int i = 0; i < data.fireParticleEffects.size; i++) {
            data.fireParticleEffects.get(i).dispose();
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
    }

    private void kill() {
        for (int i = 0; i < data.fireParticleEffects.size; i++) {
            data.fireParticleEffects.get(i).dispose();
        }
        data.fireParticleEffects.clear();
        data.explosionParticleEffect.setPosition(data.x + data.width / 2, data.y + data.height / 2);
        data.explosionParticleEffect.start();
        isDead = true;

        GameLogic.enemiesKilled++;

        UraniumCell.Spawn(enemy.getBoundingRectangle(), (int) (getRandomInRange(data.moneyCount[0], data.moneyCount[1]) * difficulty), 1, data.moneyTimer);
        if (getRandomInRange(0, 100) <= data.bonusChance) {
            Bonus.Spawn(getRandomInRange(data.bonusType[0], data.bonusType[1]), enemy.getBoundingRectangle());
        }

        Drops.drop(enemy.getBoundingRectangle(), (int) (getRandomInRange(data.dropCount[0], data.dropCount[1]) * difficulty), data.dropTimer, getRandomInRange(data.dropRarity[0], data.dropRarity[1]));

        enemy.setPosition(-100, -100);

        explosionSound.play(volume);
    }

}
