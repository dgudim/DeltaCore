package com.deo.flapd.model.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.model.ShipObject;

import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Bosses {

    private AssetManager assetManager;
    public static Array<Boss> bosses;
    private final String[] bossNames = new String[]{"boss_ship", "boss_evil"};

    public Bosses(AssetManager assetManager){
        this.assetManager = assetManager;
        bosses = new Array<>();
    }

    public void loadBosses(){
        for(int i = 0;i < bossNames.length; i++){
            bosses.add(new Boss(bossNames[i], assetManager));
        }
    }

    public void setTargetPlayer(ShipObject player) {
        for(int i = 0; i<bosses.size; i++){
            bosses.get(i).setTargetPlayer(player);
        }
    }

    public void update(float delta){
        for(int i = 0; i<bosses.size; i++){
            bosses.get(i).update(delta);
        }
    }

    public void draw(SpriteBatch batch, float delta){
        for(int i = 0; i<bosses.size; i++){
            bosses.get(i).draw(batch, delta);
        }
    }

    public void spawnRandomBoss(){
        bosses.get(getRandomInRange(0, bosses.size-1)).spawn();
    }

    public void dispose(){
        for(int i = 0;i<bosses.size; i++){
            bosses.get(i).dispose();
        }
    }
}
