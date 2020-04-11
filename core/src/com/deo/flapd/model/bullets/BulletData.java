package com.deo.flapd.model.bullets;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.JsonValue;

public class BulletData {

    String texture;

    float width;
    float height;
    float x;
    float y;

    int damage;

    int speed;

    float angle;

    String trail;
    ParticleEffect trailParticleEffect;

    float trailScale;

    BulletData(JsonValue enemyInfo, String type){

    }

}
