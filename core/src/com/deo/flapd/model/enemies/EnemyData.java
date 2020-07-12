package com.deo.flapd.model.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class EnemyData {

    String name;
    String texture;
    String explosionSound;
    String explosion;
    ParticleEffect explosionParticleEffect;
    float explosionScale;

    public float x;
    public float y;
    public float rotation;

    public float width;
    public float height;

    int fireEffectCount;
    int[] fireOffsetsX;
    int[] fireOffsetsY;
    String[] fireEffects;
    float[] fireScales;
    Array<ParticleEffect> fireParticleEffects;

    int health;

    int speed;

    float shootingDelay;

    float millis;

    String hitColor;

    Color currentColor = Color.WHITE;

    String type;

    int[] spawnHeight;

    int[] scoreSpawnConditions;

    float spawnDelay;

    int[] enemyCountSpawnConditions;

    int dropTimer;

    int moneyTimer;

    int[] dropRarity;

    int[] dropCount;

    int[] moneyCount;

    int bonusChance;

    int[] bonusType;

    boolean onBossWave;

    boolean isHoming;

    boolean rotateTowardsShip;

    float minAngle;

    float maxAngle;

    float explosionTimer;

    float idealExplosionTimer;

    boolean spawnsDrones;

    int dronesPerSpawn;

    float droneSpawnDelay;

    String droneType;

    String droneSpawnSound;

    int[] droneSpawnOffset;

    boolean spawnsBullets;

    boolean hasAnimation;

    float frameDuration;

    public boolean canAim;

    public float aimMinAngle;

    public float aimMaxAngle;

    JsonValue enemyInfo;

    EnemyData(JsonValue enemyInfo, String type) {

        JsonValue enemyBodyInfo = enemyInfo.get(type).get("body");
        fireParticleEffects = new Array<>();
        this.type = type;
        this.enemyInfo = enemyInfo;

        name = enemyInfo.name;
        texture = enemyBodyInfo.getString("texture");
        explosionSound = enemyBodyInfo.getString("explosionSound");
        explosion = enemyBodyInfo.getString("explosionEffect");
        explosionScale = enemyBodyInfo.getFloat("explosionScale");

        width = enemyBodyInfo.getFloat("width");
        height = enemyBodyInfo.getFloat("height");

        x = 805;
        y = 0;

        fireEffectCount = enemyInfo.get(type).get("body").get("fire").getInt("count");

        fireOffsetsX = new int[fireEffectCount];
        fireOffsetsY = new int[fireEffectCount];
        fireEffects = new String[fireEffectCount];
        fireScales = new float[fireEffectCount];
        fireParticleEffects.setSize(fireEffectCount);
        spawnHeight = enemyInfo.get("spawnHeight").asIntArray();
        spawnDelay = enemyInfo.getFloat("spawnDelay");
        enemyCountSpawnConditions = enemyInfo.get("spawnConditions").get("enemiesKilled").asIntArray();
        scoreSpawnConditions = enemyInfo.get("spawnConditions").get("score").asIntArray();
        onBossWave = enemyInfo.get("spawnConditions").getBoolean("bossWave");

        for (int i = 0; i < fireEffectCount; i++) {
            fireOffsetsX[i] = enemyBodyInfo.get("fire").get("offset" + i).asIntArray()[0];
            fireOffsetsY[i] = enemyBodyInfo.get("fire").get("offset" + i).asIntArray()[1];
            fireEffects[i] = enemyBodyInfo.get("fire").getString("effect" + i);
            fireScales[i] = enemyBodyInfo.get("fire").getFloat("scale" + i);
        }

        hasAnimation = enemyBodyInfo.getBoolean("hasAnimation");
        if(hasAnimation){
            frameDuration = enemyBodyInfo.getFloat("frameDuration");
        }

        isHoming = enemyBodyInfo.get("homing").asBoolean();

        if (isHoming) {
            explosionTimer = enemyBodyInfo.getFloat("explosionTimer");
            idealExplosionTimer = enemyBodyInfo.getFloat("explosionTimer");
            rotateTowardsShip = enemyBodyInfo.getBoolean("rotateTowardsShip");
            minAngle = enemyBodyInfo.get("rotationLimit").asFloatArray()[0];
            maxAngle = enemyBodyInfo.get("rotationLimit").asFloatArray()[1];
        }

        spawnsDrones = enemyBodyInfo.getBoolean("spawnsDrones");

        spawnsBullets = enemyBodyInfo.getBoolean("spawnsBullets");

        health = enemyBodyInfo.getInt("health");

        speed = enemyBodyInfo.getInt("speed");

        if(spawnsBullets) {
            shootingDelay = enemyBodyInfo.getFloat("shootingDelay");
            canAim = enemyBodyInfo.getBoolean("canAim");
            if(canAim){
                aimMinAngle = enemyBodyInfo.get("aimAngleLimit").asFloatArray()[0];
                aimMaxAngle = enemyBodyInfo.get("aimAngleLimit").asFloatArray()[1];
            }
        }

        if(spawnsDrones){
            droneSpawnDelay = enemyBodyInfo.getFloat("droneSpawnDelay");
            dronesPerSpawn = enemyBodyInfo.getInt("dronesPerSpawn");
            droneType = enemyBodyInfo.getString("droneType");
            droneSpawnSound = enemyBodyInfo.getString("droneSpawnSound");
            droneSpawnOffset = enemyBodyInfo.get("droneSpawnOffset").asIntArray();
        }

        hitColor = enemyBodyInfo.getString("hitColor");

        dropTimer = enemyInfo.get("drops").getInt("timer");
        dropCount = enemyInfo.get("drops").get("count").asIntArray();
        dropRarity = enemyInfo.get("drops").get("rarity").asIntArray();

        moneyCount = enemyInfo.get("money").get("count").asIntArray();
        moneyTimer = enemyInfo.get("money").getInt("timer");

        bonusChance = enemyInfo.get("bonuses").getInt("chance");
        bonusType = enemyInfo.get("bonuses").get("type").asIntArray();

        millis = 0;
    }

    protected EnemyData clone() {
        EnemyData copy = new EnemyData(enemyInfo, type);
        copy.y = getRandomInRange(copy.spawnHeight[0], copy.spawnHeight[1]);
        copy.shootingDelay += getRandomInRange(-7, 7) / 100f;
        return copy;
    }

    protected EnemyData useAsDroneData(float x, float y){
        this.x = x;
        this.y = y;
        shootingDelay += getRandomInRange(-10, 10) / 100f;
        return this;
    }
}
