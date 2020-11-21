package com.deo.flapd.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.model.enemies.Meteorite;

import static com.deo.flapd.utils.DUtils.getInteger;

public class Meteorites{

    public static Array<Meteorite> meteorites;
    public static int meteoritesDestroyed;
    private AssetManager assetManager;
    private boolean easterEgg;

    public Meteorites(AssetManager assetManager, boolean newGame, boolean easterEgg) {

        this.assetManager = assetManager;
        this.easterEgg = easterEgg;

        meteorites = new Array<>();

        if (!newGame) {
            meteoritesDestroyed = getInteger("meteoritesDestroyed");
        } else {
            meteoritesDestroyed = 0;
        }

    }

    public void Spawn(float x, float flyingDirection, float radius) {
        Meteorite meteorite = new Meteorite(assetManager, easterEgg, x, flyingDirection, radius);
        meteorites.add(meteorite);
    }

    public void update(float delta){
        for (int i = 0; i < meteorites.size; i++) {
            meteorites.get(i).update(delta);
            if(meteorites.get(i).remove){
                meteorites.get(i).dispose();
                meteorites.removeIndex(i);
            }
        }
    }

    public void drawEffects(SpriteBatch batch) {
        for (int i = 0; i < meteorites.size; i++) {
            meteorites.get(i).drawEffect(batch);
        }
    }

    public void drawBase(SpriteBatch batch) {
        for (int i = 0; i < meteorites.size; i++) {
           meteorites.get(i).draw(batch);
        }
    }

    public void dispose() {
        for(int i = 0; i< meteorites.size; i++){
            meteorites.get(i).dispose();
        }
    }

}


