package com.deo.flapd.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class SpaceShip extends ShipObject {

    public SpaceShip(AssetManager assetManager, float x, float y, boolean newGame) {
        super(assetManager, x, y, newGame);
    }

    public void drawBase(SpriteBatch batch, float delta){
        super.draw(batch, delta);
    }

    public void DrawShield(SpriteBatch batch, float delta){
        super.drawShield(batch, MathUtils.clamp(SpaceShip.Shield/100, 0, 1), delta);
    }

    public void drawEffects(SpriteBatch batch, float delta){
        super.drawEffects(batch, delta);
    }
}
