package com.deo.flapd.model.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.view.GameUi;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Enemies {

    private AssetManager assetManager;
    private JsonValue enemiesJson = new JsonReader().parse(Gdx.files.internal("enemies.json"));
    private Array<EnemyData> enemies;
    private Array<String> enemyNames;
    private Array<Enemy> enemyEntities;
    private Array<ParticleEffect> explosions;
    private String type;

    public Enemies(AssetManager assetManager){
        this.assetManager = assetManager;
        enemies = new Array<>();
        enemyNames = new Array<>();
        enemyEntities = new Array<>();
        explosions = new Array<>();
        if(getBoolean("easterEgg")){
            type = "easterEgg";
        }else{
            type = "normal";
        }
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
        enemyEntities.add(new Enemy(assetManager, data));
    }

    public void draw(SpriteBatch batch){
        for(int i = 0; i<enemyEntities.size; i++){
            enemyEntities.get(i).draw(batch);
        }
    }

    public void drawEffects(SpriteBatch batch){
        for(int i = 0; i<enemyEntities.size; i++){
            enemyEntities.get(i).drawEffects(batch);
        }
        for(int i = 0; i<explosions.size; i++){
            explosions.get(i).draw(batch);
        }
    }

    public void update(float delta){
        for(int i = 0; i<enemies.size; i++){
            EnemyData currentData = enemies.get(i);
            if(currentData.millis>currentData.spawnFrequency*100 && getRandomInRange(0, 45)>=15 && currentData.onBossWave == GameLogic.bossWave && GameUi.Score >= currentData.scoreSpawnConditions[0] && GameUi.Score <= currentData.scoreSpawnConditions[1] && GameUi.enemiesKilled >= currentData.enemyCountSpawnConditions[0] && GameUi.enemiesKilled <= currentData.enemyCountSpawnConditions[1]){
                SpawnEnemy(currentData);
                currentData.millis = 0;
            }
            currentData.millis += currentData.spawnFrequency*delta*100;
        }
        for(int i = 0; i<enemyEntities.size; i++){
            Enemy currentEnemy = enemyEntities.get(i);
            currentEnemy.update(delta);
            if(currentEnemy.data.x<-currentEnemy.data.width-currentEnemy.data.fireParticleEffects.get(0).getBoundingBox().getWidth()-20){
                currentEnemy.dispose();
                enemyEntities.removeIndex(i);
            }
            if(currentEnemy.data.health<=0){
                ParticleEffect explosion = new ParticleEffect();
                explosion.load(Gdx.files.internal(currentEnemy.data.explosionEffect), Gdx.files.internal("particles"));
                explosion.setPosition(currentEnemy.data.x+currentEnemy.data.width/2, currentEnemy.data.y+currentEnemy.data.height/2);
                explosion.scaleEffect(currentEnemy.data.explosionScale);
                explosion.start();
                explosions.add(explosion);
                currentEnemy.dispose();
                enemyEntities.removeIndex(i);
            }
        }
        for(int i = 0; i<explosions.size; i++){
            ParticleEffect currentExplosion = explosions.get(i);
            currentExplosion.update(delta);
            if (currentExplosion.isComplete()){
                currentExplosion.dispose();
            }
        }
    }

}
