package com.deo.flapd.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.particles.ParticleEffectPoolLoader;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.utils.ui.UIComposer;

import static com.deo.flapd.Main.VERSION_NAME;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.LogLevel.WARNING;
import static com.deo.flapd.utils.DUtils.clearPrefs;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.updateCamera;

public class LoadingScreen implements Screen {
    
    enum LoadingState {LOADING_TEXTURES, LOADING_SOUNDS, LOADING_PARTICLES, LOADING_STYLES}
    
    private final CompositeManager compositeManager;
    private final AssetManager assetManager;
    private final LocaleManager localeManager;
    public static ParticleEffectPoolLoader particleEffectPoolLoader;
    private final SpriteBatch batch;
    private final BitmapFont font_main;
    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ProgressBar loadingBar;
    private final ShapeRenderer shapeRenderer;
    private float rotation, halfRotation, progress, millis;
    private final PostProcessor blurProcessor;
    private final boolean enableShader;
    private final long loadingTime;
    
    private LoadingState loadingState;
    private String loadingStateName;
    
    public LoadingScreen(CompositeManager compositeManager) {
        
        this.compositeManager = compositeManager;
        batch = compositeManager.getBatch();
        blurProcessor = compositeManager.getBlurProcessor();
        assetManager = compositeManager.getAssetManager();
        localeManager = compositeManager.getLocaleManager();
        game = compositeManager.getGame();
        
        if (getFloat(Keys.uiScale) <= 0) {
            putFloat(Keys.uiScale, 1);
            putFloat(Keys.soundVolume, 100);
            putFloat(Keys.musicVolume, 100);
            putFloat(Keys.difficulty, 1);
            putBoolean(Keys.transparentUi, true);
            putBoolean(Keys.enableBloom, true);
            JsonEntry tree = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
            for (int i = 0; i < tree.size; i++) {
                if (tree.getString("part", i, "type").equals("category")) {
                    putBoolean("unlocked_" + tree.getString("noDefaultPartSpecified", i, "default"), true);
                    if (tree.getString("noSaveToLocation", i, "saveTo").equals("noSaveToLocation")) {
                        log("No save to location specified for " + tree.get(i) + " (item at index " + i + ")", WARNING);
                    }
                    putString(tree.getString("noSaveToLocation", i, "saveTo"), tree.get(i).getString("noDefaultPartSpecified", "default"));
                }
            }
            log("------------first launch------------\n", INFO);
        }
        
        log("---------------------------------------\n", INFO);
        log("------------started loading------------", INFO);
        loadingTime = TimeUtils.millis();
        
        font_main = assetManager.get("fonts/pixel.ttf");
        
        shapeRenderer = compositeManager.getShapeRenderer();
        
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        
        ProgressBar.ProgressBarStyle loadingBarStyle = new ProgressBar.ProgressBarStyle();
        loadingBarStyle.knob = constructFilledImageWithColor(0, 24, Color.valueOf("1979b5"));
        loadingBarStyle.knobBefore = constructFilledImageWithColor(100, 24, Color.valueOf("1979b5"));
        loadingBarStyle.background = constructFilledImageWithColor(800, 40, Color.BLACK);
        
        loadingBar = new ProgressBar(0, 100, 0.01f, false, loadingBarStyle);
        loadingBar.setSize(800, 24);
        loadingBar.setPosition(0, 30);
        loadingBar.setAnimateDuration(0.01f);
        
        enableShader = getBoolean(Keys.enableBloom);
        
        setLoadingState(LoadingState.LOADING_TEXTURES);
        
        load();
    }
    
    @Override
    public void show() {
    
    }
    
    public void load() {
        
        assetManager.load("items/items.atlas", TextureAtlas.class);
        assetManager.load("menuButtons/menuButtons.atlas", TextureAtlas.class);
        assetManager.load("menuButtons/buttons.atlas", TextureAtlas.class);
        
        assetManager.load("enemies/bosses/boss_evil/bossEvil.atlas", TextureAtlas.class);
        assetManager.load("enemies/bosses/boss_ship/bossShip.atlas", TextureAtlas.class);
        assetManager.load("enemies/bosses/boss_star_destroyer/bossStarDestroyer.atlas", TextureAtlas.class);
        assetManager.load("enemies/bosses/boss_ultimate_destroyer/bossUltimateDestroyer.atlas", TextureAtlas.class);
        assetManager.load("enemies/bosses/boss_ufo/bossUFO.atlas", TextureAtlas.class);
        assetManager.load("enemies/bosses/boss_station/bossStation.atlas", TextureAtlas.class);
        
        assetManager.load("GameOverScreenButtons/GameOverButtons.atlas", TextureAtlas.class);
        assetManager.load("items/items.atlas", TextureAtlas.class);
        assetManager.load("shop/workshop.atlas", TextureAtlas.class);
        assetManager.load("shop/slots.atlas", TextureAtlas.class);
        assetManager.load("shop/shopButtons.atlas", TextureAtlas.class);
        assetManager.load("shop/ui.atlas", TextureAtlas.class);
        assetManager.load("player/shields.atlas", TextureAtlas.class);
        assetManager.load("player/animations/beastMode.atlas", TextureAtlas.class);
        assetManager.load("bonuses.atlas", TextureAtlas.class);
        assetManager.load("enemies/enemies.atlas", TextureAtlas.class);
        assetManager.load("enemies/ufo/ufo.atlas", TextureAtlas.class);
        assetManager.load("bullets/bullets.atlas", TextureAtlas.class);
        
        assetManager.load("Meteo.png", Texture.class);
        assetManager.load("fallingShip.png", Texture.class);
        
        assetManager.load("uraniumCell.png", Texture.class);
        
        assetManager.load("ui/menuUi.atlas", TextureAtlas.class);
        assetManager.load("ui/gameUi.atlas", TextureAtlas.class);
        
        assetManager.load("backgrounds/bg_layer1.png", Texture.class);
        assetManager.load("backgrounds/bg_layer2.png", Texture.class);
        
        assetManager.load("screenFill.png", Texture.class);
        
        assetManager.load("checkpoint.png", Texture.class);
        assetManager.load("checkpoint_green.png", Texture.class);
        
        assetManager.load("sfx/explosion.ogg", Sound.class);
        assetManager.load("sfx/gun1.ogg", Sound.class);
        assetManager.load("sfx/gun2.ogg", Sound.class);
        assetManager.load("sfx/gun3.ogg", Sound.class);
        assetManager.load("sfx/gun4.ogg", Sound.class);
        assetManager.load("sfx/laser.ogg", Sound.class);
        assetManager.load("sfx/click.ogg", Sound.class);
        assetManager.load("sfx/ftl.ogg", Sound.class);
        assetManager.load("sfx/ftl_flight.ogg", Sound.class);
        
        assetManager.load("music/main1.ogg", Music.class);
        assetManager.load("music/main2.ogg", Music.class);
        assetManager.load("music/main3.ogg", Music.class);
        assetManager.load("music/main4.ogg", Music.class);
        assetManager.load("music/main5.ogg", Music.class);
        assetManager.load("music/ambient1.ogg", Music.class);
        assetManager.load("music/ambient2.ogg", Music.class);
        assetManager.load("music/ambient3.ogg", Music.class);
        assetManager.load("music/ambient4.ogg", Music.class);
        assetManager.load("music/ambient5.ogg", Music.class);
        assetManager.load("music/vhs.ogg", Music.class);
        
        assetManager.load("music/killswitch.ogg", Music.class);
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.2f, 0.25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        checkState();
        
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        rotation -= 200 * delta;
        halfRotation += 70 * delta;
        
        millis += 200 * delta;
        
        String state = (millis > 0 ? "." : "") + (millis > 120 ? "." : "") + (millis > 240 ? "." : "");
        
        if (millis > 360) {
            millis = 0;
        }
        
        progress = assetManager.getProgress();
        
        Color color = new Color().add(0.5f / progress, progress + 0.1f, 0, 1);
        Color fillColor = new Color().add(0.0f, 0.1f, 0.15f, 1);
        
        if (enableShader) {
            blurProcessor.capture();
        }
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.triangle(calculateProgressOffsetX(0), calculateProgressOffsetY(0), calculateProgressOffsetX(60), calculateProgressOffsetY(60), calculateProgressOffsetX(120), calculateProgressOffsetY(120), fillColor, fillColor, fillColor);
        shapeRenderer.triangle(calculateProgressOffsetX(180), calculateProgressOffsetY(180), calculateProgressOffsetX(240), calculateProgressOffsetY(240), calculateProgressOffsetX(300), calculateProgressOffsetY(300), fillColor, fillColor, fillColor);
        shapeRenderer.triangle(calculateProgressOffsetX(0), calculateProgressOffsetY(0), calculateProgressOffsetX(120), calculateProgressOffsetY(120), calculateProgressOffsetX(300), calculateProgressOffsetY(300), fillColor, fillColor, fillColor);
        shapeRenderer.triangle(calculateProgressOffsetX(180), calculateProgressOffsetY(180), calculateProgressOffsetX(120), calculateProgressOffsetY(120), calculateProgressOffsetX(300), calculateProgressOffsetY(300), fillColor, fillColor, fillColor);
        
        for (int i = 0; i < 53; i++) {
            shapeRenderer.setColor(0.1f, 0.5f, Math.abs(30 * MathUtils.sin((i - rotation / 20) * 6.8f * MathUtils.degreesToRadians)) / 30, 1);
            shapeRenderer.rect(5 + 15 * i, 60, 10, Math.abs(30 * MathUtils.sin((i + rotation / 10) * 6.8f * MathUtils.degreesToRadians)));
        }
        
        shapeRenderer.end();
        shapeRenderer.begin();
        shapeRenderer.triangle(400 - MathUtils.cosDeg(rotation) * 150, 240 - MathUtils.sinDeg(rotation) * 150, 400 - MathUtils.cosDeg(rotation + 120) * 150, 240 - MathUtils.sinDeg(rotation + 120) * 150, 400 - MathUtils.cosDeg(rotation + 240) * 150, 240 - MathUtils.sinDeg(rotation + 240) * 150, color, color, color);
        shapeRenderer.triangle(400 - MathUtils.cosDeg(-rotation * progress) * 80 * progress, 240 - MathUtils.sinDeg(-rotation * progress) * 80 * progress, 400 - MathUtils.cosDeg(-rotation * progress + 120) * 80 * progress, 240 - MathUtils.sinDeg(-rotation * progress + 120) * 80 * progress, 400 - MathUtils.cosDeg(-rotation * progress + 240) * 80 * progress, 240 - MathUtils.sinDeg(-rotation * progress + 240) * 80 * progress, Color.GREEN, Color.GREEN, Color.GREEN);
        shapeRenderer.polygon(new float[]{calculateOffsetX(halfRotation), calculateOffsetY(halfRotation), calculateOffsetX(halfRotation + 60), calculateOffsetY(halfRotation + 60), calculateOffsetX(halfRotation + 120), calculateOffsetY(halfRotation + 120), calculateOffsetX(halfRotation + 180), calculateOffsetY(halfRotation + 180), calculateOffsetX(halfRotation + 240), calculateOffsetY(halfRotation + 240), calculateOffsetX(halfRotation + 300), calculateOffsetY(halfRotation + 300),});
        shapeRenderer.end();
        
        batch.begin();
        loadingBar.setValue(progress * 100);
        loadingBar.draw(batch, 1);
        loadingBar.act(delta);
        font_main.getData().setScale(0.8f);
        font_main.setColor(Color.CYAN);
        font_main.draw(batch, loadingStateName + state, 0, 440, 800, 1, false);
        font_main.getData().setScale(0.5f);
        font_main.setColor(Color.ORANGE);
        font_main.draw(batch, (int) (assetManager.getProgress() * 100) + "%", 0, 49, 800, 1, false);
        font_main.getData().setScale(0.3f);
        font_main.draw(batch, VERSION_NAME, 2, 478);
        assetManager.update();
        batch.end();
        
        if (enableShader) {
            blurProcessor.render();
        }
    }
    
    @Override
    public void resize(int width, int height) {
        updateCamera(camera, viewport, width, height);
        Gdx.gl20.glLineWidth(10.0f / camera.zoom);
    }
    
    @Override
    public void pause() {
    
    }
    
    @Override
    public void resume() {
        blurProcessor.rebind();
    }
    
    @Override
    public void hide() {
        game.getScreen().dispose();
    }
    
    public void dispose() {
    }
    
    private float calculateOffsetX(float rotation) {
        rotation = 400 - MathUtils.cosDeg(rotation) * 140;
        return rotation;
    }
    
    private float calculateOffsetY(float rotation) {
        rotation = 240 - MathUtils.sinDeg(rotation) * 140;
        return rotation;
    }
    
    private float calculateProgressOffsetX(float rotation) {
        rotation = 400 - MathUtils.cosDeg(rotation) * 550 * (progress + 0.1f);
        return rotation;
    }
    
    private float calculateProgressOffsetY(float rotation) {
        rotation = 240 - MathUtils.sinDeg(rotation) * 550 * (progress + 0.1f);
        return rotation;
    }
    
    private void checkState() {
        try {
            if (assetManager.isFinished()) {
                if (loadingState.equals(LoadingState.LOADING_STYLES)) {
                    compositeManager.preloadSounds();
                    if(compositeManager.getUiComposer() == null){
                        UIComposer uiComposer = new UIComposer(compositeManager);
                        uiComposer.loadStyles(
                                "defaultLight", "defaultDark", "sliderDefaultNormal", "checkBoxDefault", "gitHub", "trello",
                                "workshopGreen", "workshopRed", "workshopCyan", "workshopPurple", "questionButton",
                                "arrowRightSmall", "arrowLeftSmall", "circle", "questionButton");
                        compositeManager.setUiComposer(uiComposer);
                    }
                    log("loaded, took " + TimeUtils.timeSinceMillis(loadingTime) / 1000.0f + "s", INFO);
                    game.setScreen(new MenuScreen(compositeManager));
                }
                if (particleEffectPoolLoader == null) {
                    if (loadingState.equals(LoadingState.LOADING_PARTICLES)) {
                        particleEffectPoolLoader = new ParticleEffectPoolLoader();
                    } else {
                        setLoadingState(LoadingState.LOADING_PARTICLES);
                    }
                } else {
                    setLoadingState(LoadingState.LOADING_STYLES);
                }
            } else if (assetManager.isLoaded("sfx/explosion.ogg", Sound.class) && loadingState != LoadingState.LOADING_SOUNDS) {
                setLoadingState(LoadingState.LOADING_SOUNDS);
            }
        } catch (ClassCastException | NumberFormatException e) {
            logException(e);
            log("wiping data :) \n", INFO);
            clearPrefs();
            log("...done...restarting", INFO);
        } catch (Exception e2) {
            logException(e2);
            log("force exiting", INFO);
            Gdx.app.exit();
        }
    }
    
    void setLoadingState(LoadingState loadingState) {
        this.loadingState = loadingState;
        loadingStateName = localeManager.get("loadingScreen.status."+loadingState);
    }
    
}
