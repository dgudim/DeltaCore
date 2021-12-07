package com.deo.flapd.model.environment;

import static com.deo.flapd.utils.DUtils.getRandomBoolean;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.utils.CompositeManager;

public class EnvironmentalEffects {

    private final Array<EnvironmentalEffect> environmentalEffects;
    private final CompositeManager compositeManager;
    
    float meteoriteSpawnChance = 0.05f;
    float fallingShipSpawnChance = 0.005f;
    
    public EnvironmentalEffects(CompositeManager compositeManager) {
        this.compositeManager = compositeManager;
        environmentalEffects = new Array<>();
    }
    
    public void update(float delta){
        if (getRandomBoolean(meteoriteSpawnChance) && delta > 0) {
            environmentalEffects.add(new Meteorite(compositeManager, getRandomInRange(0, 480), (getRandomInRange(0, 60) - 30) / 10f, getRandomInRange(0, 10) + 5));
        }
        if (getRandomBoolean(fallingShipSpawnChance) && delta > 0) {
            environmentalEffects.add(new FallingShip(compositeManager, getRandomInRange(0, 480)));
        }
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


