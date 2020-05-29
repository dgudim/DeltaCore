package com.deo.flapd.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.model.enemies.Enemies;

public class SpaceShip extends ShipObject {

    public SpaceShip(AssetManager assetManager, float x, float y, boolean newGame, Enemies enemies) {
        super(assetManager, x, y, newGame, enemies);
    }

    public void drawBase(SpriteBatch batch, float delta){
        super.draw(batch, delta);
    }

    public void DrawShield(SpriteBatch batch, float delta){
        super.drawShield(batch, MathUtils.clamp(Shield/100, 0, 1), delta);
    }

    public void drawEffects(SpriteBatch batch, float delta){
        super.drawEffects(batch, delta);
    }
}
