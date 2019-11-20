package com.deo.flapd;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deo.flapd.view.LoadingScreen;
import com.deo.flapd.view.MenuScreen;

public class Main extends Game {

    private SpriteBatch batch;
    private Preferences prefs;

    private AssetManager assetManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        prefs = Gdx.app.getPreferences("Preferences");
        assetManager = new AssetManager();

        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(BitmapFont.class,".fnt", new BitmapFontLoader(resolver));

        assetManager.load("fonts/font.fnt", BitmapFont.class);
        assetManager.load("fonts/font_white.fnt", BitmapFont.class);
        assetManager.load("fonts/font2.fnt", BitmapFont.class);

        while (!assetManager.isFinished()) {
            assetManager.update();
        }

        if (prefs.getFloat("ui")<=0) {
            prefs.putFloat("ui", 1.25f);
            System.out.println(prefs.getFloat("ui"));
            prefs.putFloat("soundEffectsVolume", 1);
            prefs.putFloat("musicVolume", 1 );
            prefs.putFloat("difficulty", 1);
            prefs.putBoolean("transparency", true);
            prefs.flush();
        }

        this.setScreen(new LoadingScreen(this, batch, assetManager, 2, true, true));
    }

    @Override
    public void render(){
        super.render();
    }

    @Override
    public void dispose(){
        batch.dispose();
        assetManager.dispose();
    }

}
