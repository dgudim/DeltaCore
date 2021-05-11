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
        JsonEntry bulletInfo = enemyInfo.get(type, "bullet");
        texture = bulletInfo.getString("texture");
        width = bulletInfo.getFloat("width");
        height = bulletInfo.getFloat("height");
        this.enemyInfo = enemyInfo;
        this.type = type;
        offset = bulletInfo.getFloatArray("offset");
        bulletAngle = MathUtils.atan2(offset[1], offset[0]) * MathUtils.radiansToDegrees;
        bulletDistance = (float) Math.sqrt(offset[1] * offset[1] + offset[0] * offset[0]);
        damage = bulletInfo.getInt("damage");
        spread = bulletInfo.getFloat("spread");
        speed = bulletInfo.getInt("speed");
        trail = bulletInfo.getString("trail");
        trailScale = bulletInfo.getFloat("trailScale");
        explosionScale = bulletInfo.getFloat("explosionScale");
        shootSound = bulletInfo.getString("shootSound");
        bulletsPerShot = bulletInfo.getInt("bulletsPerShot");
        explosion = bulletInfo.getString("explosionEffect");
        isHoming = bulletInfo.getBoolean("homing");
        if(isHoming){
            explosionTimer = bulletInfo.getFloat("explosionTimer");
            homingSpeed = bulletInfo.getFloat("homingSpeed");
        }
    }

    public BulletData(JsonEntry bulletData) {
        texture = bulletData.getString("texture");
        width = bulletData.getFloat("width");
        height = bulletData.getFloat("height");
        offset = bulletData.getFloatArray("offset");
        bulletAngle = MathUtils.atan2(offset[1], offset[0]) * MathUtils.radiansToDegrees;
        bulletDistance = (float) Math.sqrt(offset[1] * offset[1] + offset[0] * offset[0]);
        damage = bulletData.getInt("damage");
        spread = bulletData.getFloat("spread");
        speed = bulletData.getInt("speed");
        trail = bulletData.getString("trail");
        trailScale = bulletData.getFloat("trailScale");
        explosionScale = bulletData.getFloat("explosionScale");
        shootSound = bulletData.getString("shootSound");
        bulletsPerShot = bulletData.getInt("bulletsPerShot");
        explosion = bulletData.getString("explosionEffect");
        isHoming = bulletData.getBoolean("homing");
        if(isHoming){
            explosionTimer = bulletData.getFloat("explosionTimer");
            homingSpeed = bulletData.getFloat("homingSpeed");
        }
    }
}
