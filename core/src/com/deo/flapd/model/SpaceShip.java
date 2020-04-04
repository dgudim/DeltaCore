package com.deo.flapd.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.view.GameUi;

public class SpaceShip extends ShipObject {

    public SpaceShip(AssetManager assetManager, float x, float y, float width, float height, boolean newGame) {
        super((Texture)assetManager.get("ship.png"),(Texture)assetManager.get("ColdShield.png"), x, y, width, height, newGame);
    }

    public void drawBase(SpriteBatch batch, boolean is_paused){
        super.draw(batch, is_paused);
    }

    public void DrawShield(SpriteBatch batch, boolean is_paused){
        super.drawShield(batch ,is_paused, MathUtils.clamp(GameUi.Shield/100, 0, 1));
    }

    public void drawEffects(SpriteBatch batch, float delta, boolean is_paused){
        super.drawEffects(batch, delta, is_paused);
    }
}
