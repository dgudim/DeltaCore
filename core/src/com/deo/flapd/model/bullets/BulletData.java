package com.deo.flapd.model.bullets;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.utils.JsonEntry;

public class BulletData {
    
    String texture;
    
    public float width;
    public float height;
    
    public float[] offset;
    public float bulletAngle;
    public float bulletDistance;
    
    int damage;
    
    int speed;
    
    public float spread;
    
    String trail;
    public ParticleEffect trailParticleEffect;
    public String explosion;
    public ParticleEffect explosionParticleEffect;
    
    float trailScale;
    public float explosionScale;
    
    public String shootSound;
    
    public int bulletsPerShot;
    
    boolean isHoming;
    float explosionTimer;
    float homingSpeed;
    
    JsonEntry enemyInfo;
    
    String type;
    
    public BulletData(JsonEntry enemyInfo, String type) {
        loadFromBulletData(enemyInfo.get(type, "bullet"));
        this.enemyInfo = enemyInfo;
        this.type = type;
    }
    
    public BulletData(JsonEntry bulletData) {
        loadFromBulletData(bulletData);
    }
    
    void loadFromBulletData(JsonEntry bulletData) {
        texture = bulletData.getString("noTexture", "texture");
        width = bulletData.getFloat(1, "width");
        height = bulletData.getFloat(1, "height");
        offset = bulletData.getFloatArray(new float[]{0, 0}, "offset");
        bulletAngle = MathUtils.atan2(offset[1], offset[0]) * MathUtils.radiansToDegrees;
        bulletDistance = (float) Math.sqrt(offset[1] * offset[1] + offset[0] * offset[0]);
        damage = bulletData.getInt(1, "damage");
        spread = bulletData.getFloat(5, "spread");
        speed = bulletData.getInt(130, "speed");
        trail = bulletData.getString("particles/bullet_trail_left.p", "trail");
        trailScale = bulletData.getFloat(1, "trailScale");
        explosionScale = bulletData.getFloat(1, "explosionScale");
        shootSound = bulletData.getString("sfx/gun1.ogg", "shootSound");
        bulletsPerShot = bulletData.getInt(1, "bulletsPerShot");
        explosion = bulletData.getString("particles/explosion2.p", "explosionEffect");
        isHoming = bulletData.getBoolean(false, "homing");
        if (isHoming) {
            explosionTimer = bulletData.getFloat(3, "explosionTimer");
            homingSpeed = bulletData.getFloat(5, "homingSpeed");
        }
    }
    
}
