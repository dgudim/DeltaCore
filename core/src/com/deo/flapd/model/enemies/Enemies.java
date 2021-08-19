package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.JsonEntry;

import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Enemies {
    
    private final AssetManager assetManager;
    
    JsonEntry enemiesJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("enemies/enemies.json")));
    private final Array<EnemyData> enemies;
    private final Array<String> enemyNames;
    public Array<Enemy> enemyEntities;
    
    private final float difficulty;
    
    private Player player;
    
    public Enemies(AssetManager assetManager) {
        
        this.assetManager = assetManager;
        
        enemies = new Array<>();
        enemyNames = new Array<>();
        enemyEntities = new Array<>();
        
        difficulty = getFloat("difficulty");
    }
    
    public void loadEnemies() {
        int enemyTypeCount = enemiesJson.size;
        
        for (int i = 0; i < enemyTypeCount; i++) {
            EnemyData enemyData = new EnemyData(enemiesJson.get(i));
            enemies.add(enemyData);
            enemyNames.add(enemyData.name);
        }
    }
    
    private void SpawnEnemy(EnemyData data) {
        data = data.clone();
        data.health *= difficulty;
        enemyEntities.add(new Enemy(assetManager, data, this, player));
    }
    
    public void draw(SpriteBatch batch) {
        for (int i = 0; i < enemyEntities.size; i++) {
            enemyEntities.get(i).draw(batch);
        }
    }
    
    public void drawEffects(SpriteBatch batch, float delta) {
        for (int i = 0; i < enemyEntities.size; i++) {
            enemyEntities.get(i).drawEffects(batch, delta);
        }
    }
    
    public void drawDebug(ShapeRenderer shapeRenderer){
        for (int i = 0; i < enemyEntities.size; i++) {
            enemyEntities.get(i).drawDebug(shapeRenderer);
        }
    }
    
    public void update(float delta) {
        for (int i = 0; i < enemies.size; i++) {
            EnemyData currentData = enemies.get(i);
            if (currentData.millis > currentData.spawnDelay * 100
                    && getRandomInRange(0, 45) >= 15
                    && currentData.onBossWave == GameLogic.bossWave
                    && GameLogic.score >= currentData.scoreSpawnConditions[0]
                    && GameLogic.score <= currentData.scoreSpawnConditions[1]
                    && GameLogic.enemiesKilled >= currentData.enemyCountSpawnConditions[0]
                    && GameLogic.enemiesKilled <= currentData.enemyCountSpawnConditions[1]) {
                SpawnEnemy(currentData);
                currentData.millis = 0;
            }
            currentData.millis += delta * 20;
        }
        for (int i = 0; i < enemyEntities.size; i++) {
            enemyEntities.get(i).update(delta);
            if (enemyEntities.get(i).queuedForDeletion) {
                enemyEntities.get(i).dispose();
                enemyEntities.removeIndex(i);
            }
        }
    }
    
    public void dispose() {
        for (int i = 0; i < enemyEntities.size; i++) {
            enemyEntities.get(i).dispose();
            enemyEntities.removeIndex(i);
        }
    }
    
    public void setTargetPlayer(Player targetPlayer) {
        player = targetPlayer;
    }
}
