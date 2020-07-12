package com.deo.flapd.model.bullets;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.JsonValue;

public class BulletData {

    String texture;

    public float width;
    public float height;
    public float x;
    public float y;

    public float[] offset;

    int damage;

    int speed;

    public float angle;

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

    JsonValue enemyInfo;

    String type;

    public BulletData(JsonValue enemyInfo, String type) {
        JsonValue bulletInfo = enemyInfo.get(type).get("bullet");
        texture = bulletInfo.getString("texture");
        width = bulletInfo.getFloat("width");
        height = bulletInfo.getFloat("height");
        x = 0;
        y = 0;
        angle = 0;
        this.enemyInfo = enemyInfo;
        this.type = type;
        offset = bulletInfo.get("offset").asFloatArray();
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
        }
    }
}
