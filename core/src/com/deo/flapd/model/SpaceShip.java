package com.deo.flapd.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.view.GameUi;

public class SpaceShip extends ShipObject {

    private SpriteBatch batch;

    public SpaceShip(Texture ShipTexture, Texture ShieldTexture, float x, float y, float width, float height) {
        super(ShipTexture,ShieldTexture, x, y, width, height);
    }

    public void draw(SpriteBatch batch, boolean is_paused){
        this.batch = batch;
        super.draw(batch, is_paused);
    }

    public void DrawShield(boolean is_paused){
        super.drawShield(batch,is_paused, MathUtils.clamp(GameUi.Shield/100, 0, 1));
    }
}
