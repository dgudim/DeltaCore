package com.deo.flapd.model.bullets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.ShipObject;

public class EnemyBullet {

    public Sprite bullet;
    private BulletData data;

    private boolean isDead = false;
    public boolean queuedForDeletion = false;
    private boolean explosionFinished = false;

    private ShipObject player;
    private Bullet playerBullet;

    public EnemyBullet(AssetManager assetManager, BulletData bulletData, ShipObject ship) {

        data = bulletData;

        player = ship;
        playerBullet = player.bullet;

        bullet = new Sprite((Texture) assetManager.get(bulletData.texture));
        bullet.setPosition(bulletData.x, bulletData.y);
        bullet.setRotation(bulletData.angle);
        bullet.setSize(bulletData.width, bulletData.height);
        bullet.setOrigin(bulletData.width / 2, bulletData.height / 2);

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
        for (int i = 0; i < Meteorite.meteorites.size; i++) {
            if (Meteorite.meteorites.get(i).overlaps(bullet.getBoundingRectangle())) {
                Meteorite.healths.set(i, Meteorite.healths.get(i) - data.damage);
                explode();
            }
        }
        if (!isDead) {
            if(!data.isHoming) {
                data.x -= MathUtils.cosDeg(bullet.getRotation()) * data.speed * delta;
                data.y -= MathUtils.sinDeg(bullet.getRotation()) * data.speed * delta;
            }else{
                data.x = MathUtils.lerp(data.x, player.bounds.getX(), delta * data.speed / 220.0f);
                data.y = MathUtils.lerp(data.y, player.bounds.getY() + player.bounds.getBoundingRectangle().getHeight() / 2, delta * data.speed / 220.0f);
                bullet.setRotation(MathUtils.radiansToDegrees * MathUtils.atan2(data.y - player.bounds.getY(), data.x - player.bounds.getX()));
                data.explosionTimer -= delta;
            }
            bullet.setPosition(data.x, data.y);
            data.trailParticleEffect.setPosition(data.x + data.width / 2, data.y + data.height / 2);

            if (data.x < -data.width - 30 || data.x > 800 + data.width + 30 || data.y > 480 + 30 || data.y < -data.width - 30) {
                isDead = true;
                explosionFinished = true;
            }

            if(data.isHoming && data.explosionTimer<=0){
                explode();
            }
        }
        queuedForDeletion = (data.explosionParticleEffect.isComplete() || explosionFinished) && isDead;
    }

    public void dispose() {
        data.explosionParticleEffect.dispose();
        data.trailParticleEffect.dispose();
    }

    private void explode() {
        data.trailParticleEffect.dispose();
        data.explosionParticleEffect.setPosition(data.x + data.width / 2, data.y + data.height / 2);
        data.explosionParticleEffect.start();
        bullet.setPosition(-100, -100);
        isDead = true;
    }

}
