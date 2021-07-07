package com.deo.flapd.model.bullets;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.utils.JsonEntry;

import static com.deo.flapd.utils.DUtils.getDistanceBetweenTwoPoints;

public class BulletData {
    
    String texture;
    
    boolean isLaser;
    
    public float width;
    public float height;
    
    public float[] offset;
    public float bulletAngle;
    public float bulletDistance;
    
    float damage;
    
    float speed;
    
    public float spread;
    
    public String trail;
    public ParticleEffect trailParticleEffect;
    
    public String explosion;
    public ParticleEffect explosionParticleEffect;
    
    float trailScale;
    public float explosionScale;
    
    float fadeOutTimer;
    float maxFadeOutTimer;
    
    public String shootSound;
    
    public int bulletsPerShot;
    
    boolean isHoming;
    float explosionTimer;
    float homingSpeed;
    
    JsonEntry enemyInfo;
    String color;
    
    public BulletData(JsonEntry enemyInfo, String type) {
        loadFromBulletData(enemyInfo.get(type, "bullet"));
        this.enemyInfo = enemyInfo;
    }
    
    public BulletData(JsonEntry bulletData) {
        loadFromBulletData(bulletData);
    }
    
    void loadFromBulletData(JsonEntry bulletData) {
        texture = bulletData.getString("noTexture", "texture");
        
        isLaser = bulletData.getBoolean(false, "isLaser");
        if(isLaser){
            fadeOutTimer = bulletData.getFloat(3, "fadeOutTimer");
            maxFadeOutTimer = bulletData.getFloat(3, "fadeOutTimer");
            color = bulletData.getString("ffffffff", "color");
        }else{
            width = bulletData.getFloat(1, "width");
            
            speed = bulletData.getFloat(130, "speed");
    
            trail = bulletData.getString("particles/bullet_trail_left.p", "trail");
            trailScale = bulletData.getFloat(1, "trailScale");
    
            explosion = bulletData.getString("particles/explosion2.p", "explosionEffect");
            explosionScale = bulletData.getFloat(1, "explosionScale");
    
            isHoming = bulletData.getBoolean(false, "homing");
            if (isHoming) {
                explosionTimer = bulletData.getFloat(3, "explosionTimer");
                homingSpeed = bulletData.getFloat(5, "homingSpeed");
            }
        }
    
        height = bulletData.getFloat(1, "height");
        offset = bulletData.getFloatArray(new float[]{0, 0}, "offset");
        
        bulletAngle = MathUtils.atan2(offset[1], offset[0]) * MathUtils.radiansToDegrees;
        bulletDistance = getDistanceBetweenTwoPoints(0, 0, offset[0], offset[1]);
        
        damage = bulletData.getFloat(1, "damage");
        spread = bulletData.getFloat(5, "spread");
        
        shootSound = bulletData.getString("sfx/gun1.ogg", "shootSound");
        
        bulletsPerShot = bulletData.getInt(1, "bulletsPerShot");
    }
}
