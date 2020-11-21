package com.deo.flapd.model.bullets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorites;
import com.deo.flapd.model.ShipObject;
import com.deo.flapd.utils.DUtils;

import static com.deo.flapd.utils.DUtils.enemyBulletDisposes;
import static com.deo.flapd.utils.DUtils.enemyBulletTrailDisposes;

public class EnemyBullet {

    public Sprite bullet;
    private BulletData data;

    private boolean isDead = false;
    public boolean queuedForDeletion = false;
    private boolean explosionFinished = false;
    private boolean explosionStarted;

    private ShipObject player;
    private Bullet playerBullet;

    public EnemyBullet(AssetManager assetManager, BulletData bulletData, ShipObject ship) {

        initBullet(bulletData, ship);

        bullet = new Sprite((Texture) assetManager.get(bulletData.texture));
        bullet.setPosition(bulletData.x, bulletData.y);
        bullet.setRotation(bulletData.angle);
        bullet.setSize(bulletData.width, bulletData.height);
        bullet.setOrigin(bulletData.width / 2, bulletData.height / 2);
    }

    public EnemyBullet(TextureAtlas bossAtlas, BulletData bulletData, ShipObject ship) {

        initBullet(bulletData, ship);

        bullet = new Sprite(bossAtlas.findRegion(bulletData.texture));
        bullet.setPosition(bulletData.x, bulletData.y);
        bullet.setRotation(bulletData.angle);
        bullet.setSize(bulletData.width, bulletData.height);
        bullet.setOrigin(bulletData.width / 2, bulletData.height / 2);

    }

    private void initBullet(BulletData bulletData, ShipObject ship){
        data = bulletData;

        player = ship;
        playerBullet = player.bullet;

        bulletData.explosionParticleEffect = new ParticleEffect();
        bulletData.explosionParticleEffect.load(Gdx.files.internal(bulletData.explosion), Gdx.files.internal("particles"));
        bulletData.explosionParticleEffect.scaleEffect(bulletData.explosionScale);

        bulletData.trailParticleEffect = new ParticleEffect();
        bulletData.trailParticleEffect.load(Gdx.files.internal(bulletData.trail), Gdx.files.internal("particles"));
        bulletData.trailParticleEffect.scaleEffect(bulletData.trailScale);
        bulletData.trailParticleEffect.start();
    }

    public void draw(SpriteBatch batch, float delta) {
        if (!isDead) {
            bullet.draw(batch);
            data.trailParticleEffect.draw(batch, delta);
        } else {
            data.explosionParticleEffect.draw(batch, delta);
        }
    }

    public void update(float delta) {
        for (int i = 0; i < playerBullet.bullets.size; i++) {
            if (playerBullet.bullets.get(i).overlaps(bullet.getBoundingRectangle())) {
                explode();
                playerBullet.removeBullet(i, true);
            }
        }
        if (bullet.getBoundingRectangle().overlaps(player.bounds.getBoundingRectangle())) {
            explode();
            player.takeDamage(data.damage);
        }
        for (int i = 0; i < Meteorites.meteorites.size; i++) {
            if (Meteorites.meteorites.get(i).entityHitBox.overlaps(bullet.getBoundingRectangle())) {
                Meteorites.meteorites.get(i).health -= data.damage;
                explode();
            }
        }
        if (!isDead) {
            if (!data.isHoming) {
                data.x -= MathUtils.cosDeg(bullet.getRotation()) * data.speed * delta;
                data.y -= MathUtils.sinDeg(bullet.getRotation()) * data.speed * delta;
            } else {
                bullet.setRotation(DUtils.lerpAngleWithConstantSpeed(bullet.getRotation(),
                        MathUtils.radiansToDegrees * MathUtils.atan2(
                        data.y - (player.bounds.getY() + player.bounds.getBoundingRectangle().getHeight()/2f),
                        data.x - (player.bounds.getX() + player.bounds.getBoundingRectangle().getWidth()/2f)),
                        data.homingSpeed, delta));

                data.x -= MathUtils.cosDeg(bullet.getRotation()) * data.speed * 2 * delta;
                data.y -= MathUtils.sinDeg(bullet.getRotation()) * data.speed * 2 * delta;
                data.explosionTimer -= delta;
            }
            bullet.setPosition(data.x, data.y);
            data.trailParticleEffect.setPosition(data.x + data.width / 2, data.y + data.height / 2);

            if (data.x < -data.width - 30 || data.x > 800 + data.width + 30 || data.y > 480 + 30 || data.y < -data.width - 30) {
                isDead = true;
                explosionFinished = true;
            }

            if (data.isHoming && data.explosionTimer <= 0) {
                explode();
            }
        }
        queuedForDeletion = (data.explosionParticleEffect.isComplete() || explosionFinished) && isDead;
    }

    public void dispose() {
        data.explosionParticleEffect.dispose();
        data.trailParticleEffect.dispose();
        if (!explosionStarted) {
            enemyBulletTrailDisposes++;
        }
        enemyBulletDisposes++;
    }

    private void explode() {
        data.trailParticleEffect.dispose();
        enemyBulletTrailDisposes++;
        data.explosionParticleEffect.setPosition(data.x + data.width / 2, data.y + data.height / 2);
        data.explosionParticleEffect.start();
        explosionStarted = true;
        bullet.setPosition(-100, -100);
        isDead = true;
    }

}

