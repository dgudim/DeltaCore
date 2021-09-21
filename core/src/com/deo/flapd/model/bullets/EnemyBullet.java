package com.deo.flapd.model.bullets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.deo.flapd.model.Entity;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.DUtils;
import com.deo.flapd.view.screens.GameScreen;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.drawParticleEffectBounds;
import static com.deo.flapd.view.screens.LoadingScreen.particleEffectPoolLoader;
import static java.lang.StrictMath.min;

public class EnemyBullet extends Entity {
    
    private final BulletData data;
    
    public boolean queuedForDeletion = false;
    private boolean explosionFinished = false;
    private boolean explosionStarted;
    
    private final boolean hasCollisionWithPlayerBullets;
    
    private final Player player;
    private final PlayerBullet playerBullet;
    
    public EnemyBullet(AssetManager assetManager, BulletData bulletData, Player player, float x, float y, float rotation, boolean hasCollisionWithPlayerBullets) {
        if (assetManager.get("bullets/bullets.atlas", TextureAtlas.class).findRegion(bulletData.texture) == null)
            throw new IllegalArgumentException("no bullet texture with name: " + bulletData.texture);
        
        entitySprite = new Sprite(assetManager.get("bullets/bullets.atlas", TextureAtlas.class).findRegion(bulletData.texture));
        
        this.hasCollisionWithPlayerBullets = hasCollisionWithPlayerBullets;
        
        data = bulletData;
        
        this.player = player;
        playerBullet = this.player.bullet;
        
        if (!data.isLaser) {
            bulletData.explosionParticleEffect = particleEffectPoolLoader.getParticleEffectByPath(bulletData.explosion);
            bulletData.explosionParticleEffect.scaleEffect(bulletData.explosionScale);
            
            bulletData.trailParticleEffect = particleEffectPoolLoader.getParticleEffectByPath(bulletData.trail);
            bulletData.trailParticleEffect.scaleEffect(bulletData.trailScale);
            bulletData.trailParticleEffect.setPosition(
                    x + width / 2f + MathUtils.cosDeg(
                            rotation + data.trailOffsetAngle * data.trailOffsetDistance),
                    y + height / 2f + MathUtils.sinDeg(
                            rotation + data.trailOffsetAngle * data.trailOffsetDistance));
        }
        
        if (bulletData.isLaser) {
            bulletData.width = 600;
            y += bulletData.height / 2f;
            color = Color.valueOf(bulletData.color);
        }
        
        setPositionAndRotation(x, y, rotation + (bulletData.isLaser ? 180 : 0));
        setSize(bulletData.width, bulletData.height);
        health = bulletData.damage;
        init();
    }
    
    @Override
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        originX = data.isLaser ? 0 : width / 2f;
        originY = height / 2f;
    }
    
    @Override
    protected void updateEntity(float delta) {
        if (data.isLaser) {
            entitySprite.setColor(color);
            float scaledHeight = data.height * data.fadeOutTimer / data.maxFadeOutTimer;
            entitySprite.setOrigin(0, scaledHeight / 2f);
            entitySprite.setOriginBasedPosition(x, y);
            entitySprite.setSize(data.width, scaledHeight);
        } else {
            super.updateEntity(delta);
        }
    }
    
    @Override
    public boolean overlaps(Rectangle hitBox) {
        if (data.isLaser) {
            return checkLaserIntersection(hitBox);
        } else {
            return super.overlaps(hitBox);
        }
    }
    
    private boolean checkLaserIntersection(Rectangle hitBox) {
        if (hitBox.width <= 5 || hitBox.height <= 5) return false;
        float step = min(hitBox.width, hitBox.height) / 3f;
        float X1 = hitBox.x;
        float Y1 = hitBox.y;
        float X2 = hitBox.x + hitBox.width;
        float Y2 = hitBox.y + hitBox.height;
        for (int i = 0; i < width; i += step) {
            float pointX = x + MathUtils.cosDeg(rotation) * i;
            float pointY = y + MathUtils.sinDeg(rotation) * i;
            if (pointX > X1 && pointX < X2 && pointY > Y1 && pointY < Y2) {
                return true;
            }
        }
        return false;
    }
    
    public void draw(SpriteBatch batch, float delta) {
        if (!isDead) {
            if (!data.drawTrailOnTop && !data.isLaser) {
                data.trailParticleEffect.draw(batch, delta);
            }
            entitySprite.draw(batch);
            if (data.drawTrailOnTop && !data.isLaser) {
                data.trailParticleEffect.draw(batch, delta);
            }
        } else {
            if (!data.isLaser) {
                data.explosionParticleEffect.draw(batch, delta);
            }
        }
    }
    
    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        if (!data.isLaser) {
            super.drawDebug(shapeRenderer);
            shapeRenderer.setColor(Color.YELLOW);
            drawParticleEffectBounds(shapeRenderer, data.trailParticleEffect);
        } else {
            shapeRenderer.rectLine(x, y, x + MathUtils.cosDeg(rotation) * width, y + MathUtils.sinDeg(rotation) * width, height);
        }
    }
    
    public void update(float delta) {
        if (hasCollisionWithPlayerBullets) {
            for (int i = 0; i < playerBullet.bullets.size; i++) {
                if (overlaps(playerBullet.bullets.get(i)) && !playerBullet.remove_Bullet.get(i)) {
                    explode();
                    playerBullet.removeBullet(i, true);
                }
            }
        }
        
        if (overlaps(player.entityHitBox)) {
            player.takeDamage(health * (data.isLaser ? data.fadeOutTimer / data.maxFadeOutTimer : 1) / (data.isLaser ? delta : 1));
            GameScreen.screenShake(data.screenShakeIntensity * (data.isLaser ? data.fadeOutTimer / data.maxFadeOutTimer : 1), data.screenShakeDuration);
            explode();
        }
        
        if (!isDead) {
            if (data.isLaser) {
                data.fadeOutTimer = clamp(data.fadeOutTimer - delta, 0f, data.maxFadeOutTimer);
                updateEntity(delta);
                if (data.fadeOutTimer <= 0) {
                    entityHitBox.setPosition(-1000, -1000);
                    isDead = true;
                    health = 0;
                }
            } else {
                if (data.isHoming) {
                    rotation = DUtils.lerpAngleWithConstantSpeed(rotation,
                            MathUtils.radiansToDegrees * MathUtils.atan2(
                                    y - (player.y + player.height / 2f),
                                    x - (player.x + player.width / 2f)),
                            data.homingSpeed, delta);
                    data.explosionTimer -= delta;
                }
                x -= MathUtils.cosDeg(rotation) * data.speed * delta * (data.isHoming ? 2 : 1);
                y -= MathUtils.sinDeg(rotation) * data.speed * delta * (data.isHoming ? 2 : 1);
                updateEntity(delta);
                
                data.trailParticleEffect.setPosition(
                        x + width / 2f + MathUtils.cosDeg(
                                rotation + data.trailOffsetAngle) * data.trailOffsetDistance,
                        y + height / 2f + MathUtils.sinDeg(
                                rotation + data.trailOffsetAngle) * data.trailOffsetDistance);
                
                if (x < -width - 30 || x > 800 + width + 30 || y > 480 + 30 || y < -width - 30) {
                    isDead = true;
                    explosionFinished = true;
                }
                
                if (data.isHoming && data.explosionTimer <= 0) {
                    explode();
                }
            }
        }
        if (data.isLaser) {
            queuedForDeletion = isDead;
        } else {
            queuedForDeletion = (data.explosionParticleEffect.isComplete() || explosionFinished) && isDead;
        }
    }
    
    public void dispose() {
        if (!data.isLaser) {
            data.explosionParticleEffect.free();
            if (!explosionStarted) {
                data.trailParticleEffect.free();
            }
        }
    }
    
    private void explode() {
        if (!data.isLaser) {
            data.trailParticleEffect.free();
            data.explosionParticleEffect.setPosition(x + originX, y + originY);
            explosionStarted = true;
            entitySprite.setPosition(-100, -100);
            isDead = true;
            health = 0;
        }
    }
    
}

