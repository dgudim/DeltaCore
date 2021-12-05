package com.deo.flapd;

import static com.deo.flapd.utils.DUtils.LogLevel.CRITICAL_ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.LogLevel.WARNING;
import static com.deo.flapd.utils.DUtils.clearLog;
import static com.deo.flapd.utils.DUtils.flushLogBuffer;
import static com.deo.flapd.utils.DUtils.getPrefs;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.MusicManager;
import com.deo.flapd.utils.SoundManager;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.utils.postprocessing.ShaderLoader;
import com.deo.flapd.utils.postprocessing.effects.Bloom;
import com.deo.flapd.utils.postprocessing.effects.MotionBlur;
import com.deo.flapd.view.screens.LoadingScreen;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class Main extends Game {
    
    private SpriteBatch batch;
    private PostProcessor blurProcessor;
    
    private CompositeManager compositeManager;
    
    public static String VERSION_NAME;
    
    @Override
    public void create() {
        ObjectMap<String, String> map = new ObjectMap<>();
        try {
            PropertiesUtils.load(map, Gdx.files.internal("version.properties").reader());
            VERSION_NAME = map.get("buildversion");
        } catch (IOException e) {
            VERSION_NAME = "unspecified";
            log("Build version is not specified", WARNING);
        }
        
        batch = new SpriteBatch();
        AssetManager assetManager = new AssetManager();
        
        clearLog();
        Date date = new Date();
        log("|-new session-|" + "  " + DateFormat.getDateTimeInstance().format(date) + "\n", INFO);
        
        ShaderLoader.BasePath = "shaders/";
        blurProcessor = new PostProcessor(false, false, Gdx.app.getType() == Application.ApplicationType.Desktop);
        Bloom bloom = new Bloom((int) (Gdx.graphics.getWidth() * 0.25f), (int) (Gdx.graphics.getHeight() * 0.25f));
        MotionBlur motionBlur = new MotionBlur();
        motionBlur.setBlurOpacity(0);
        blurProcessor.addEffect(bloom);
        blurProcessor.addEffect(motionBlur);
        
        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(BitmapFont.class, ".fnt", new BitmapFontLoader(resolver));
    
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
    
        String CHARSET = "\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890ёйцукенгшщзхъэждлорпавыфячсмитьбюЁЙЦУКЕНГШЩЗХЪЭЖДЛОРПАВЫФЯЧСМИТЬБЮ\"!`?'.,;:()[]{}<>|/@\\^$€-%+=#_&~*\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF";
    
        FreetypeFontLoader.FreeTypeFontLoaderParameter params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        params.fontFileName = "fonts/pixel.ttf";
        params.fontParameters.size = 40;
        params.fontParameters.characters = CHARSET;
        assetManager.load("fonts/pixel.ttf", BitmapFont.class, params);
    
        FreetypeFontLoader.FreeTypeFontLoaderParameter params2 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        params2.fontFileName = "fonts/bold_main.ttf";
        params2.fontParameters.size = 60;
        params2.fontParameters.characters = CHARSET;
        assetManager.load("fonts/bold_main.ttf", BitmapFont.class, params2);
        
        assetManager.load("fonts/font_numbers.fnt", BitmapFont.class);
        
        while (!assetManager.isFinished()) {
            assetManager.update();
        }
        
        compositeManager = new CompositeManager();
        compositeManager.setLocaleManager(new LocaleManager());
        compositeManager.setAssetManager(assetManager);
        compositeManager.setBlurProcessor(blurProcessor);
        compositeManager.setBloom(bloom);
        compositeManager.setMotionBlur(motionBlur);
        compositeManager.setMusicManager(new MusicManager(assetManager));
        compositeManager.setSoundManager(new SoundManager(assetManager));
        compositeManager.setGame(this);
        compositeManager.setBatch(batch);
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        compositeManager.setShapeRenderer(shapeRenderer);
        
        this.setScreen(new LoadingScreen(compositeManager));
    }
    
    @Override
    public void render() {
        try {
            super.render();
        } catch (Exception e) {
            logException(e);
            log("rendering error occurred, dump of preferences\n" + getPrefs() + "\n", CRITICAL_ERROR);
            log("force exiting", INFO);
            Gdx.app.exit();
        }
    }
    
    @Override
    public void dispose() {
        flushLogBuffer();
        batch.dispose();
        blurProcessor.dispose();
        compositeManager.dispose();
    }
}
