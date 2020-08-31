package com.deo.flapd.model.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.model.ShipObject;

public class Bosses {

    private AssetManager assetManager;
    private Array<Boss> bosses;
    private final String[] bossNames;
    private ShipObject player;

    public Bosses(AssetManager assetManager){
        this.assetManager = assetManager;
        bosses = new Array<>();
        bossNames = new String[]{"boss_ship", "boss_evil"};
    }

    public void loadBosses(){
        for(int i = 0;i < bossNames.length; i++){
            bosses.add(new Boss(bossNames[i], assetManager));
        }
    }

    public void setTargetPlayer(ShipObject player) {
        this.player = player;
    }

    public void update(float delta){
        for(int i = 0; i<bosses.size; i++){
            bosses.get(i).update(delta);
        }
    }

    public void draw(SpriteBatch batch){
        for(int i = 0; i<bosses.size; i++){
            bosses.get(i).draw(batch);
        }
    }

    public void drawEffects(SpriteBatch batch, float delta){

    }

    public void dispose(){

    }
}
