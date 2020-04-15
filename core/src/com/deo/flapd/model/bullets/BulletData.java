package com.deo.flapd.model.bullets;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.JsonValue;
import com.deo.flapd.model.enemies.EnemyData;

import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class BulletData {

    String texture;

    public float width;
    public float height;
    public float x;
    public float y;

    float[] offset;

    int damage;

    int speed;

    float angle;

    float spread;

    String trail;
    public ParticleEffect trailParticleEffect;
    public String explosion;
    public ParticleEffect explosionParticleEffect;

    float trailScale;
    public float explosionScale;

    public String shootSound;

    public int bulletsPerShot;

    boolean homing;

    JsonValue enemyInfo;

    String type;

    public BulletData(JsonValue enemyInfo, String type) {
        JsonValue bulletInfo = enemyInfo.get(type).get("bullet");
        texture = bulletInfo.get("texture").asString();
        width = bulletInfo.get("width").asFloat();
        height = bulletInfo.get("height").asFloat();
        x = 0;
        y = 0;
        angle = 0;
        this.enemyInfo = enemyInfo;
        this.type = type;
        offset = bulletInfo.get("offset").asFloatArray();
        damage = bulletInfo.get("damage").asInt();
        spread = bulletInfo.get("spread").asFloat();
        speed = bulletInfo.get("speed").asInt();
        trail = bulletInfo.get("trail").asString();
        trailScale = bulletInfo.get("trailScale").asFloat();
        explosionScale = bulletInfo.get("explosionScale").asFloat();
        shootSound = bulletInfo.get("shootSound").asString();
        bulletsPerShot = bulletInfo.get("bulletsPerShot").asInt();
        explosion = bulletInfo.get("explosionEffect").asString();
        homing = bulletInfo.get("homing").asBoolean();
    }

    public BulletData clone(EnemyData enemyData) {
        BulletData copy = new BulletData(enemyInfo, type);
        copy.x = enemyData.x + copy.offset[0];
        copy.y = enemyData.y + copy.offset[1];
        copy.angle = getRandomInRange(-10, 10) * copy.spread;
        return copy;
    }
}
