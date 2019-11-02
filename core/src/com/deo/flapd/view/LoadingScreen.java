package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LoadingScreen {

    private AssetManager assetManager;
    private SpriteBatch batch;
    private BitmapFont main;

    public LoadingScreen(SpriteBatch batch){

        this.batch = batch;

        main = new BitmapFont(Gdx.files.internal("fonts/font2.fnt"), false);

    }

    public void render(float percentage){
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        main.setColor(Color.CYAN);
        main.draw(batch, "Loaded: " + percentage + "%"  , 200, 250);
        batch.end();
    }

    public void dispose(){
        main.dispose();
    }

}
