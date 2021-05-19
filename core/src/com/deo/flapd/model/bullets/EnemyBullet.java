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
import com.deo.flapd.model.enemies.Entity;
import com.deo.flapd.utils.DUtils;

import static com.deo.flapd.utils.DUtils.enemyBulletDisposes;
import static com.deo.flapd.utils.DUtils.enemyBulletTrailDisposes;

public class EnemyBullet extends Entity {
    
    private BulletData data;
    
    public boolean queuedForDeletion = false;
    private boolean explosionFinished = false;
    private boolean explosionStarted;

    private ShipObject player;
    private Bullet playerBullet;

    public EnemyBullet(AssetManager assetManager, BulletData bulletData, ShipObject ship) {
        entitySprite = new Sprite((Texture) assetManager.get(bulletData.texture));
        initBullet(bulletData, ship);
    }

    public EnemyBullet(TextureAtlas bossAtlas, BulletData bulletData, ShipObject ship) {
        entitySprite = new Sprite(bossAtlas.findRegion(bulletData.texture));
        initBullet(bulletData, ship);
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
    
        setSize(bulletData.width, bulletData.height);
        width = bulletData.width;
        height = bulletData.height;
        health = bulletData.damage;
        init();
    }

    public void draw(SpriteBatch batch, float delta) {
        if (!isDead) {
            entitySprite.draw(batch);
            data.trailParticleEffect.draw(batch, delta);
        } else {
            data.explosionParticleEffect.draw(batch, delta);
        }
    }

    public void update(float delta) {
        for (int i = 0; i < playerBullet.bullets.size; i++) {
            if (overlaps(playerBullet.bullets.get(i))) {
                explode();
                playerBullet.removeBullet(i, true);
            }
        }
        if (overlaps(player.bounds)) {
            player.takeDamage(health);
            explode();
        }
        for (int i = 0; i < Meteorites.meteorites.size; i++) {
            if (overlaps(Meteorites.meteorites.get(i))) {
                Meteorites.meteorites.get(i).health -= health;
                explode();
            }
        }
        if (!isDead) {
            if (!data.isHoming) {
                x -= MathUtils.cosDeg(rotation) * data.speed * delta;
                y -= MathUtils.sinDeg(rotation) * data.speed * delta;
            } else {
                rotation = DUtils.lerpAngleWithConstantSpeed(rotation,
                        MathUtils.radiansToDegrees * MathUtils.atan2(
                                y - (player.bounds.getY() + player.bounds.getHeight()/2f),
                                x - (player.bounds.getX() + player.bounds.getWidth()/2f)),
                        data.homingSpeed, delta);
                
                x -= MathUtils.cosDeg(rotation) * data.speed * 2 * delta;
                y -= MathUtils.sinDeg(rotation) * data.speed * 2 * delta;
                data.explosionTimer -= delta;
            }
            update();
            
            data.trailParticleEffect.setPosition(x + data.width / 2, y + data.height / 2);

            if (x < -width - 30 || x > 800 + width + 30 || y > 480 + 30 || y < -width - 30) {
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
        data.explosionParticleEffect.setPosition(x + originX, y + originY);
        data.explosionParticleEffect.start();
        explosionStarted = true;
        entitySprite.setPosition(-100, -100);
        isDead = true;
        health = 0;
    }

}

