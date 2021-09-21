package com.deo.flapd.model.environment;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class EnvironmentalEffects {

    private final Array<EnvironmentalEffect> environmentalEffects;
    private final AssetManager assetManager;
    
    public EnvironmentalEffects(AssetManager assetManager) {
        this.assetManager = assetManager;
        environmentalEffects = new Array<>();
    }

    public void spawnMeteorite(float x, float flyingDirection, float radius) {
        Meteorite meteorite = new Meteorite(assetManager, x, flyingDirection, radius);
        environmentalEffects.add(meteorite);
    }
    
    public void spawnFallingShip(float x) {
        FallingShip fallingShip = new FallingShip(assetManager, x);
        environmentalEffects.add(fallingShip);
    }
    
    public void update(float delta){
        for (int i = 0; i < environmentalEffects.size; i++) {
            environmentalEffects.get(i).update(delta);
            if(environmentalEffects.get(i).remove){
                environmentalEffects.get(i).dispose();
                environmentalEffects.removeIndex(i);
            }
        }
    }

    public void drawEffects(SpriteBatch batch) {
        for (int i = 0; i < environmentalEffects.size; i++) {
            environmentalEffects.get(i).drawEffect(batch);
        }
    }

    public void drawBase(SpriteBatch batch) {
        for (int i = 0; i < environmentalEffects.size; i++) {
           environmentalEffects.get(i).draw(batch);
        }
    }

    public void dispose() {
        for(int i = 0; i< environmentalEffects.size; i++){
            environmentalEffects.get(i).dispose();
        }
    }

}


