package com.deo.flapd.model.bullets;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.drawParticleEffectBounds;

import static java.lang.StrictMath.min;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.deo.flapd.model.Entity;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.particles.ParticleEffectPoolLoader;

public class Bullet extends Entity {
    
    protected float newX;
    protected float newY;
    protected float newRot;
    
    protected BulletData data;
    
    AssetManager assetManager;
    
    public boolean queuedForDeletion = false;
    boolean explosionFinished = false;
    boolean explosionStarted;
    
    boolean hasCollisionWithEnemyBullets = true;
    
    Bullet(CompositeManager compositeManager, JsonEntry bulletData) {
        data = new BulletData();
        assetManager = compositeManager.getAssetManager();
        loadBulletData(bulletData);
        
        assetManager = compositeManager.getAssetManager();
        ParticleEffectPoolLoader particleEffectPool = compositeManager.getParticleEffectPool();
        
        if (assetManager.get("bullets/bullets.atlas", TextureAtlas.class).findRegion(data.texture) == null)
            throw new IllegalArgumentException("No bullet texture with name: " + data.texture);
        
        calculateSpawnPosition();
        float x = newX;
        float y = newY;
        float rotation = newRot;
        
        entitySprite = new Sprite(assetManager.get("bullets/bullets.atlas", TextureAtlas.class).findRegion(data.texture));
        
        if (!data.isLaser) {
            data.explosionParticleEffect = particleEffectPool.getParticleEffectByPath(data.explosion);
            data.explosionParticleEffect.scaleEffect(data.explosionScale);
            
            data.trailParticleEffect = particleEffectPool.getParticleEffectByPath(data.trail);
            data.trailParticleEffect.scaleEffect(data.trailScale);
            data.trailParticleEffect.setPosition(
                    x + width / 2f + MathUtils.cosDeg(
                            rotation + data.trailOffsetAngle * data.trailOffsetDistance),
                    y + height / 2f + MathUtils.sinDeg(
                            rotation + data.trailOffsetAngle * data.trailOffsetDistance));
        }
        
        if (data.isLaser) {
            width = 600;
            y += height / 2f;
        }
        
        setPositionAndRotation(x, y, rotation + (data.isLaser ? 180 : 0));
        setSize(width, height);
        init();
    }
    
    @Override
    protected void updateEntity(float delta) {
        if (data.isLaser) {
            if (data.isBeam) {
                calculateSpawnPosition();
                x = newX;
                y = newY + height / 2f;
                rotation = newRot + 180;
                entitySprite.setRotation(rotation);
            }
            entitySprite.setColor(color);
            float scaledHeight = height * data.fadeOutTimer / data.maxFadeOutTimer;
            setOrigin(0, scaledHeight / 2f);
            entitySprite.setOriginBasedPosition(x, y);
            entitySprite.setSize(width, scaledHeight);
            updateHealth(delta);
        } else {
            super.updateEntity(delta);
        }
    }
    
    @Override
    public boolean overlaps(Entity entity) {
        return overlaps(entity, false);
    }
    
    public boolean overlaps(Entity entity, boolean withBullet) {
        if (!withBullet || hasCollisionWithEnemyBullets) {
            if (data.isLaser) {
                return checkLaserIntersection(entity.entityHitBox);
            } else {
                return super.overlaps(entity);
            }
        } else {
            return false;
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
            shapeRenderer.setColor(Color.ORANGE);
            drawParticleEffectBounds(shapeRenderer, data.explosionParticleEffect);
        } else {
            shapeRenderer.rectLine(x, y, x + MathUtils.cosDeg(rotation) * width, y + MathUtils.sinDeg(rotation) * width, height);
        }
    }
    
    @Override
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        originX = data.isLaser ? 0 : width / 2f;
        originY = height / 2f;
    }
    
    public void updateHomingLogic(float delta) {
    }
    
    public void update(float delta) {
        checkCollisions(delta);
        if (!isDead) {
            if (data.isLaser) {
                data.fadeOutTimer = clamp(data.fadeOutTimer - delta, 0f, data.maxFadeOutTimer);
                if (data.fadeOutTimer <= 0) {
                    die();
                }
                updateEntity(delta);
            } else {
                if (data.isHoming) {
                    updateHomingLogic(delta);
                }
                x += MathUtils.cosDeg(rotation) * speed * delta * (data.isHoming ? 2 : 1);
                y += MathUtils.sinDeg(rotation) * speed * delta * (data.isHoming ? 2 : 1);
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
    
    @Override
    public void explode() {
        if (!data.isLaser) {
            data.trailParticleEffect.free();
            data.explosionParticleEffect.setPosition(x + originX, y + originY);
            explosionStarted = true;
            entitySprite.setPosition(-100, -100);
            die();
        }
    }
    
    public void checkCollisions(float delta) {
    }
    
    public void calculateSpawnPosition() {
    }
    
    void loadBulletData(JsonEntry bulletData) {
    }
    
}

class BulletData {
    public String texture;
    
    boolean isLaser;
    public boolean isBeam;
    public boolean hasCollisionWithEnemyBullets;
    
    public String trail;
    public float trailOffsetDistance;
    public float trailOffsetAngle;
    public boolean drawTrailOnTop;
    public ParticleEffectPool.PooledEffect trailParticleEffect;
    
    public String explosion;
    public ParticleEffectPool.PooledEffect explosionParticleEffect;
    
    float trailScale;
    public float explosionScale;
    
    float fadeOutTimer;
    float maxFadeOutTimer;
    
    boolean isHoming;
    float explosionTimer;
    float homingSpeed;
    
    float screenShakeIntensity;
    float screenShakeDuration;
    
    BulletData() {
    }
}


