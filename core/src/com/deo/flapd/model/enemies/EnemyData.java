package com.deo.flapd.model.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.getRandomInRange;

class EnemyData {

    String name;
    String texture;
    String explosionSound;
    String explosionEffect;
    float explosionScale;

    float x;
    float y;

    float width;
    float height;

    int fireEffectCount;
    int[] fireOffsetsX;
    int[] fireOffsetsY;
    String[] fireEffects;
    float[] fireScales;
    Array<ParticleEffect> fireParticleEffects;

    int health;

    int speed;

    float shootingFrequency;

    float millis;

    String hitColor;

    Color currentColor = Color.WHITE;

    String type;

    int[] spawnHeight;

    int[] scoreSpawnConditions;

    float spawnFrequency;

    int[] enemyCountSpawnConditions;

    int dropTimer;

    int moneyTimer;

    int[] dropRarity;

    int[] dropCount;

    int[] moneyCount;

    int bonusChance;

    int[] bonusType;

    boolean onBossWave;

    JsonValue enemyInfo;

    EnemyData(JsonValue enemyInfo, String type){

        fireParticleEffects = new Array<>();
        this.type = type;
        this.enemyInfo = enemyInfo;

        name = enemyInfo.name;
        texture = enemyInfo.get(type).get("body").get("texture").asString();
        explosionSound = enemyInfo.get(type).get("body").get("explosionSound").asString();
        explosionEffect = enemyInfo.get(type).get("body").get("explosionEffect").asString();
        explosionScale = enemyInfo.get(type).get("body").get("explosionScale").asFloat();

        width = enemyInfo.get(type).get("body").get("width").asFloat();
        height = enemyInfo.get(type).get("body").get("height").asFloat();

        x = 805;
        y = 0;

        fireEffectCount = enemyInfo.get(type).get("body").get("fire").get("count").asInt();

        fireOffsetsX = new int[fireEffectCount];
        fireOffsetsY = new int[fireEffectCount];
        fireEffects = new String[fireEffectCount];
        fireScales = new float[fireEffectCount];
        fireParticleEffects.setSize(fireEffectCount);
        spawnHeight = enemyInfo.get("spawnHeight").asIntArray();
        spawnFrequency = enemyInfo.get("spawnFrequency").asFloat();
        enemyCountSpawnConditions = enemyInfo.get("spawnConditions").get("enemiesKilled").asIntArray();
        scoreSpawnConditions = enemyInfo.get("spawnConditions").get("score").asIntArray();
        onBossWave = enemyInfo.get("spawnConditions").get("bossWave").asBoolean();

        for(int i = 0; i<fireEffectCount; i++){
            fireOffsetsX[i] = enemyInfo.get(type).get("body").get("fire").get("offset"+i).asIntArray()[0];
            fireOffsetsY[i] = enemyInfo.get(type).get("body").get("fire").get("offset"+i).asIntArray()[1];
            fireEffects[i] = enemyInfo.get(type).get("body").get("fire").get("effect"+i).asString();
            fireScales[i] = enemyInfo.get(type).get("body").get("fire").get("scale"+i).asFloat();
        }

        health = enemyInfo.get(type).get("body").get("health").asInt();

        speed = enemyInfo.get(type).get("body").get("speed").asInt();

        shootingFrequency = enemyInfo.get(type).get("body").get("shootingFrequency").asFloat();

        hitColor = enemyInfo.get(type).get("body").get("hitColor").asString();

        dropTimer = enemyInfo.get("drops").get("timer").asInt();
        dropCount = enemyInfo.get("drops").get("count").asIntArray();
        dropRarity = enemyInfo.get("drops").get("rarity").asIntArray();

        moneyCount = enemyInfo.get("money").get("count").asIntArray();
        moneyTimer = enemyInfo.get("money").get("timer").asInt();

        bonusChance = enemyInfo.get("bonuses").get("chance").asInt();
        bonusType = enemyInfo.get("bonuses").get("type").asIntArray();

        millis = 0;
    }

    protected EnemyData clone(){
        EnemyData copy = new EnemyData(enemyInfo, type);
        copy.y = getRandomInRange(copy.spawnHeight[0], copy.spawnHeight[1]);
        return copy;
    }
}
