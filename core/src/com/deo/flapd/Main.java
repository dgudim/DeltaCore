package com.deo.flapd;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deo.flapd.utils.MusicManager;
import com.deo.flapd.utils.ShaderLoader;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.utils.postprocessing.effects.Bloom;
import com.deo.flapd.view.LoadingScreen;

import java.text.DateFormat;
import java.util.Date;

import static com.deo.flapd.utils.DUtils.LogLevel.CRITICAL_ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.clearLog;
import static com.deo.flapd.utils.DUtils.getPrefs;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;
import static com.deo.flapd.view.LoadingScreen.particleEffectPoolLoader;

public class Main extends Game {
    
    private SpriteBatch batch;
    private PostProcessor blurProcessor;
    
    private AssetManager assetManager;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        
        clearLog();
        Date date = new Date();
        log("|-new session-|" + "  " + DateFormat.getDateTimeInstance().format(date) + "\n", INFO);
        
        ShaderLoader.BasePath = "shaders/";
        blurProcessor = new PostProcessor(false, false, Gdx.app.getType() == Application.ApplicationType.Desktop);
        Bloom bloom = new Bloom((int) (Gdx.graphics.getWidth() * 0.25f), (int) (Gdx.graphics.getHeight() * 0.25f));
        bloom.setBlurPasses(2);
        blurProcessor.addEffect(bloom);
        
        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(BitmapFont.class, ".fnt", new BitmapFontLoader(resolver));
        
        assetManager.load("fonts/font.fnt", BitmapFont.class);
        assetManager.load("fonts/font_white.fnt", BitmapFont.class);
        assetManager.load("fonts/font2.fnt", BitmapFont.class);
        assetManager.load("fonts/font2(old).fnt", BitmapFont.class);
        
        while (!assetManager.isFinished()) {
            assetManager.update();
        }
        
        this.setScreen(new LoadingScreen(this, batch, assetManager, blurProcessor, new MusicManager(assetManager)));
    }
    
    @Override
    public void render() {
        try {
            super.render();
        } catch (Exception e) {
            logException(e);
            log("global error occurred, dump of preferences\n" + getPrefs() + "\n", CRITICAL_ERROR);
            log("force exiting", INFO);
            Gdx.app.exit();
            Gdx.app.exit();
        }
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        assetManager.dispose();
        blurProcessor.dispose();
        if (particleEffectPoolLoader != null) {
            particleEffectPoolLoader.dispose();
        }
    }
}
