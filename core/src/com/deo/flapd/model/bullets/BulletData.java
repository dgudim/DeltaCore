package com.deo.flapd.model.bullets;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.utils.JsonEntry;

import static com.deo.flapd.utils.DUtils.getDistanceBetweenTwoPoints;

public class BulletData {
    
    public String texture;
    
    boolean isLaser;
    public boolean hasCollisionWithPlayerBullets;
    
    public float width;
    public float height;
    
    float damage;
    
    float speed;
    
    public String trail;
    public float trailOffsetDistance;
    public float trailOffsetAngle;
    public boolean drawTrailOnTop;
    public ParticleEffect trailParticleEffect;
    
    public String explosion;
    public ParticleEffect explosionParticleEffect;
    
    float trailScale;
    public float explosionScale;
    
    float fadeOutTimer;
    float maxFadeOutTimer;
    
    boolean isHoming;
    float explosionTimer;
    float homingSpeed;
    
    float screenShakeIntensity;
    float screenShakeDuration;
    
    String color;
    
    public BulletData(JsonEntry bulletData) {
        loadFromBulletData(bulletData);
    }
    
    void loadFromBulletData(JsonEntry bulletData) {
        texture = bulletData.getString("noTexture", "texture");
        
        isLaser = bulletData.getBoolean(false, false, "isLaser");
        if (isLaser) {
            fadeOutTimer = bulletData.getFloat(3, "fadeOutTimer");
            maxFadeOutTimer = bulletData.getFloat(3, "fadeOutTimer");
            color = bulletData.getString("ffffffff", "color");
        } else {
            width = bulletData.getFloat(1, "width");
            
            speed = bulletData.getFloat(130, "speed");
            
            trail = bulletData.getString("particles/bullet_trail_left.p", "trail");
            trailScale = bulletData.getFloat(1, "trailScale");
            drawTrailOnTop = bulletData.getBoolean(false, false, "drawTrailOnTop");
            float[] trailOffset = bulletData.getFloatArray(new float[]{0, 0}, "trailOffset");
            trailOffsetAngle = MathUtils.atan2(trailOffset[1], trailOffset[0]) * MathUtils.radiansToDegrees;
            trailOffsetDistance = getDistanceBetweenTwoPoints(0, 0, trailOffset[0], trailOffset[1]);
            
            explosion = bulletData.getString("particles/explosion2.p", "explosionEffect");
            explosionScale = bulletData.getFloat(1, "explosionScale");
            
            isHoming = bulletData.getBoolean(false, false, "homing");
            if (isHoming) {
                explosionTimer = bulletData.getFloat(3, "explosionTimer");
                homingSpeed = bulletData.getFloat(5, "homingSpeed");
            }
        }
        
        height = bulletData.getFloat(1, "height");
        
        hasCollisionWithPlayerBullets = bulletData.getBoolean(false, false, "hasCollisionWithPlayerBullets");
        
        damage = bulletData.getFloat(1, "damage");
        
        screenShakeIntensity = bulletData.getFloat(false, 0, "screenShakeOnHit");
        screenShakeDuration = bulletData.getFloat(false, 0, "screenShakeDuration");
    }
}
