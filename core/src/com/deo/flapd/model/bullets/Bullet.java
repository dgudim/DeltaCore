package com.deo.flapd.model.bullets;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.drawParticleEffectBounds;
import static com.deo.flapd.utils.DUtils.lineRectIntersect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.model.Entity;
import com.deo.flapd.model.EntityWithAim;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.DUtils;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.particles.ParticleEffectPoolLoader;

public class Bullet extends EntityWithAim {
    
    protected float newX;
    protected float newY;
    protected float newRot;
    
    protected float angleOffset;
    
    protected BulletData data;
    
    AssetManager assetManager;
    
    public boolean queuedForDeletion = false;
    boolean explosionFinished = false;
    boolean explosionStarted;
    
    Bullet(CompositeManager compositeManager, JsonEntry bulletData, float angleOffset) {
        data = new BulletData();
        assetManager = compositeManager.getAssetManager();
        loadBulletData(bulletData);
        canAim = data.isHoming;
        
        assetManager = compositeManager.getAssetManager();
        ParticleEffectPoolLoader particleEffectPool = compositeManager.getParticleEffectPool();
        
        if (assetManager.get("bullets/bullets.atlas", TextureAtlas.class).findRegion(data.texture) == null)
            throw new IllegalArgumentException("No bullet texture with name: " + data.texture);
        
        calculateSpawnPosition();
        float x = newX;
        float y = newY;
        float rotation = newRot + angleOffset;
        this.angleOffset = angleOffset;
        
        entitySprite = new Sprite(assetManager.get("bullets/bullets.atlas", TextureAtlas.class).findRegion(data.texture));
        
        if (!data.isLaser) {
            data.explosionParticleEffect = particleEffectPool.getParticleEffectByPath(data.explosion);
            data.explosionParticleEffect.scaleEffect(data.explosionScale);
            
            data.trailParticleEffect = particleEffectPool.getParticleEffectByPath(data.trail);
            data.trailParticleEffect.scaleEffect(data.trailScale);
            data.trailParticleEffect.setPosition(
                    x + width / 2f + MathUtils.cosDeg(
                            rotation + data.trailOffsetAngle) * data.trailOffsetDistance,
                    y + height / 2f + MathUtils.sinDeg(
                            rotation + data.trailOffsetAngle) * data.trailOffsetDistance);
        }
        
        if (data.isLaser) {
            width = 800;
            y += height / 2f;
        }
        
        setPositionAndRotation(x, y, rotation);
        setSize(width, height);
        init();
    }
    
    @Override
    protected void updateEntity(float delta) {
        if (data.isLaser) {
            if (data.isBeam) {
                calculateSpawnPosition();
                x = newX + width / 2f;
                y = newY + height / 2f;
                rotation = newRot + angleOffset;
                entitySprite.setRotation(rotation);
            }
            entitySprite.setColor(color);
            float scaledHeight = height * data.fadeOutTimer / data.maxFadeOutTimer;
            entitySprite.setOrigin(0, scaledHeight / 2f);
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
        if (!withBullet || data.hasCollisionWithEnemyBullets) {
            if (data.isLaser) {
                return lineRectIntersect(x, y, x + MathUtils.cosDeg(rotation) * width, y + MathUtils.sinDeg(rotation) * width, entity.entityHitBox);
            } else {
                return super.overlaps(entity);
            }
        } else {
            return false;
        }
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
            if(data.isHoming){
                drawAim(shapeRenderer, originX, originY, Color.LIGHT_GRAY);
            }
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
        if (homingTarget != null) {
            if (!homingTarget.isDead) {
                rotation = DUtils.lerpAngleWithConstantSpeed(rotation,
                        MathUtils.radiansToDegrees * MathUtils.atan2(
                                y + originY - (homingTarget.y + homingTarget.height / 2f),
                                x + originX - (homingTarget.x + homingTarget.width / 2f)) + 180,
                        data.homingSpeed, delta);
                data.explosionTimer -= delta;
            }
        }
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
                x += MathUtils.cosDeg(rotation) * speed * delta;
                y += MathUtils.sinDeg(rotation) * speed * delta;
                updateEntity(delta);
                
                data.trailParticleEffect.setPosition(
                        x + width / 2f + MathUtils.cosDeg(
                                rotation + data.trailOffsetAngle + angleOffset) * data.trailOffsetDistance,
                        y + height / 2f + MathUtils.sinDeg(
                                rotation + data.trailOffsetAngle + angleOffset) * data.trailOffsetDistance);
                
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
    
    @Override
    public void takeDamage(float damage) {
        if(!data.isLaser){
            super.takeDamage(damage);
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
    
    public float getDamage(float delta){
        return health * (data.isLaser ? data.fadeOutTimer / data.maxFadeOutTimer * delta : 1);
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


