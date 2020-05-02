package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.deo.flapd.control.GameLogic;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Enemies {

    private AssetManager assetManager;
    private JsonValue enemiesJson = new JsonReader().parse(Gdx.files.internal("enemies.json"));
    private Array<EnemyData> enemies;
    private Array<String> enemyNames;
    private Array<Enemy> enemyEntities;
    private String type;
    private float difficulty;

    public Enemies(AssetManager assetManager){
        this.assetManager = assetManager;
        enemies = new Array<>();
        enemyNames = new Array<>();
        enemyEntities = new Array<>();
        if(getBoolean("easterEgg")){
            type = "easterEgg";
        }else{
            type = "normal";
        }
        difficulty = getFloat("difficulty");
    }

    public void loadEnemies(){
        int enemyTypeCount = enemiesJson.size;

        for(int i = 0; i<enemyTypeCount; i++){
            EnemyData enemyData = new EnemyData(enemiesJson.get(i), type);
            enemies.add(enemyData);
            enemyNames.add(enemyData.name);
        }
    }

    private void SpawnEnemy(EnemyData data){
        data = data.clone();
        data.health *= difficulty;
        enemyEntities.add(new Enemy(assetManager, data));
    }

    public void draw(SpriteBatch batch){
        for(int i = 0; i<enemyEntities.size; i++){
            enemyEntities.get(i).draw(batch);
        }
    }

    public void drawEffects(SpriteBatch batch, float delta){
        for(int i = 0; i<enemyEntities.size; i++){
            enemyEntities.get(i).drawEffects(batch, delta);
        }
    }

    public void update(float delta){
        for(int i = 0; i<enemies.size; i++){
            EnemyData currentData = enemies.get(i);
            if(currentData.millis>currentData.spawnDelay*100 && getRandomInRange(0, 45)>=15 && currentData.onBossWave == GameLogic.bossWave && GameLogic.Score >= currentData.scoreSpawnConditions[0] && GameLogic.Score <= currentData.scoreSpawnConditions[1] && GameLogic.enemiesKilled >= currentData.enemyCountSpawnConditions[0] && GameLogic.enemiesKilled <= currentData.enemyCountSpawnConditions[1]){
                SpawnEnemy(currentData);
                currentData.millis = 0;
            }
            currentData.millis += delta*20;
        }
        for(int i = 0; i<enemyEntities.size; i++){
            enemyEntities.get(i).update(delta);
            if(enemyEntities.get(i).queuedForDeletion){
                enemyEntities.get(i).dispose();
                enemyEntities.removeIndex(i);
            }
        }
    }

    public void dispose(){
        for(int i = 0; i<enemyEntities.size; i++){
            enemyEntities.get(i).dispose();
            enemyEntities.removeIndex(i);
        }
    }

}
