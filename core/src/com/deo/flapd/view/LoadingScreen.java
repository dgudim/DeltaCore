package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
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
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.postprocessing.PostProcessor;

import static com.deo.flapd.utils.DUtils.clearPrefs;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.updateCamera;

public class LoadingScreen implements Screen {
    
    private final AssetManager assetManager;
    private final SpriteBatch batch;
    private final BitmapFont main;
    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ProgressBar loadingBar;
    private final ShapeRenderer shapeRenderer;
    private float rotation, halfRotation, progress, millis;
    private String state;
    private final PostProcessor blurProcessor;
    private final boolean enableShader;
    private final long loadingTime;
    public static Tree craftingTree;
    private String stateName;
    
    public LoadingScreen(Game game, SpriteBatch batch, final AssetManager assetManager, PostProcessor blurProcessor) {
        
        if (getFloat("ui") <= 0) {
            putFloat("ui", 1);
            putFloat("soundVolume", 100);
            putFloat("musicVolume", 100);
            putFloat("difficulty", 1);
            putBoolean("transparency", true);
            putBoolean("bloom", true);
            JsonEntry tree = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
            for (int i = 0; i < tree.size; i++) {
                if (tree.getString("part", i, "type").equals("basePart")) {
                    putBoolean("unlocked_" + getItemCodeNameByName(tree.get(i).name), true);
                    if (tree.getString("noSaveToLocation", i, "saveTo").equals("noSaveToLocation")) {
                        log("\nNo save to location specified for " + tree.get(i) + " (item at index " + i + ")");
                    }
                    putString(tree.getString("noSaveToLocation", i, "saveTo"), tree.get(i).name);
                }
            }
            log("\n------------first launch------------" + "\n");
        }
        
        log("\n started loading");
        loadingTime = TimeUtils.millis();
        
        this.batch = batch;
        
        this.blurProcessor = blurProcessor;
        
        main = new BitmapFont(Gdx.files.internal("fonts/font2(old).fnt"), false);
        
        this.assetManager = assetManager;
        
        this.game = game;
        
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        
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
        
        Gdx.gl20.glLineWidth(10);
        
        enableShader = getBoolean("bloom");
        
        stateName = "Loading";
        
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
        assetManager.load("GameOverScreenButtons/GameOverButtons.atlas", TextureAtlas.class);
        assetManager.load("items/items.atlas", TextureAtlas.class);
        assetManager.load("shop/workshop.atlas", TextureAtlas.class);
        assetManager.load("shop/slots.atlas", TextureAtlas.class);
        assetManager.load("shop/shopButtons.atlas", TextureAtlas.class);
        assetManager.load("shop/ui.atlas", TextureAtlas.class);
        assetManager.load("player/bullets.atlas", TextureAtlas.class);
        assetManager.load("player/shields.atlas", TextureAtlas.class);
        assetManager.load("player/animations/beastMode.atlas", TextureAtlas.class);
        assetManager.load("bonuses.atlas", TextureAtlas.class);
        assetManager.load("enemies/enemies.atlas", TextureAtlas.class);
        assetManager.load("enemies/ufo/ufo.atlas", TextureAtlas.class);
        assetManager.load("9bg.png", Texture.class);
        
        assetManager.load("bg_layer1.png", Texture.class);
        assetManager.load("bg_layer2.png", Texture.class);
        assetManager.load("bg_layer3.png", Texture.class);
        assetManager.load("HotShield.png", Texture.class);
        assetManager.load("pew3.png", Texture.class);
        assetManager.load("pew.png", Texture.class);
        assetManager.load("pew2.png", Texture.class);
        assetManager.load("homingPew.png", Texture.class);
        assetManager.load("Meteo.png", Texture.class);
        
        assetManager.load("uraniumCell.png", Texture.class);
        
        assetManager.load("firebutton.png", Texture.class);
        assetManager.load("weaponbutton.png", Texture.class);
        assetManager.load("pause.png", Texture.class);
        assetManager.load("level score indicator.png", Texture.class);
        assetManager.load("health indicator.png", Texture.class);
        assetManager.load("money_display.png", Texture.class);
        
        assetManager.load("checkpoint.png", Texture.class);
        assetManager.load("checkpoint_green.png", Texture.class);
        
        assetManager.load("cat_meteorite.png", Texture.class);
        assetManager.load("whiskas.png", Texture.class);
        assetManager.load("laser.png", Texture.class);
        
        assetManager.load("greyishButton.png", Texture.class);
        
        assetManager.load("menuBg.png", Texture.class);
        assetManager.load("menuFill.png", Texture.class);
        assetManager.load("lamp.png", Texture.class);
        assetManager.load("infoBg.png", Texture.class);
        assetManager.load("infoBg2.png", Texture.class);
        assetManager.load("treeBg.png", Texture.class);
        assetManager.load("bg_old.png", Texture.class);
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
        
        state = (millis > 0 ? "." : "") + (millis > 120 ? "." : "") + (millis > 240 ? "." : "");
        
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
        shapeRenderer.end();
        shapeRenderer.begin();
        shapeRenderer.triangle(400 - MathUtils.cosDeg(rotation) * 150, 240 - MathUtils.sinDeg(rotation) * 150, 400 - MathUtils.cosDeg(rotation + 120) * 150, 240 - MathUtils.sinDeg(rotation + 120) * 150, 400 - MathUtils.cosDeg(rotation + 240) * 150, 240 - MathUtils.sinDeg(rotation + 240) * 150, color, color, color);
        shapeRenderer.triangle(400 - MathUtils.cosDeg(-rotation * progress) * 80 * progress, 240 - MathUtils.sinDeg(-rotation * progress) * 80 * progress, 400 - MathUtils.cosDeg(-rotation * progress + 120) * 80 * progress, 240 - MathUtils.sinDeg(-rotation * progress + 120) * 80 * progress, 400 - MathUtils.cosDeg(-rotation * progress + 240) * 80 * progress, 240 - MathUtils.sinDeg(-rotation * progress + 240) * 80 * progress, Color.GREEN, Color.GREEN, Color.GREEN);
        shapeRenderer.polygon(new float[]{calculateOffsetX(halfRotation), calculateOffsetY(halfRotation), calculateOffsetX(halfRotation + 60), calculateOffsetY(halfRotation + 60), calculateOffsetX(halfRotation + 120), calculateOffsetY(halfRotation + 120), calculateOffsetX(halfRotation + 180), calculateOffsetY(halfRotation + 180), calculateOffsetX(halfRotation + 240), calculateOffsetY(halfRotation + 240), calculateOffsetX(halfRotation + 300), calculateOffsetY(halfRotation + 300),});
        for (int i = 0; i < 53; i++) {
            shapeRenderer.setColor(0.1f, 0.5f, Math.abs(30 * MathUtils.sin((i - rotation / 20) * 6.8f * MathUtils.degreesToRadians)) / 30, 1);
            shapeRenderer.rect(5 + 15 * i, 60, 10, Math.abs(30 * MathUtils.sin((i + rotation / 10) * 6.8f * MathUtils.degreesToRadians)));
        }
        shapeRenderer.end();
        
        batch.begin();
        loadingBar.setValue(progress * 100);
        loadingBar.draw(batch, 1);
        loadingBar.act(delta);
        main.getData().setScale(0.8f);
        main.setColor(Color.CYAN);
        main.draw(batch, stateName + state, 0, 440, 800, 1, false);
        main.getData().setScale(0.5f);
        main.setColor(Color.ORANGE);
        main.draw(batch, (int) (assetManager.getProgress() * 100) + "%", 0, 47, 800, 1, false);
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
        main.dispose();
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
            if (assetManager.isFinished() && stateName.equals("Loading")) {
                float elapsedTime = TimeUtils.timeSinceMillis(loadingTime) / 1000.0f;
                float relativePercentage = (100 - elapsedTime * 100f / 3f);
                if (relativePercentage >= 0) {
                    log("\n loaded, elapsed time " + elapsedTime + "s(" + relativePercentage + "% better than average)");
                } else {
                    log("\n loaded, elapsed time " + elapsedTime + "s(" + -relativePercentage + "% worse than average)");
                }
            }
            if (stateName.equals("Building tree")) {
                craftingTree = new Tree(LoadingScreen.this.assetManager, 105, 65, 430, 410);
                game.setScreen(new MenuScreen(game, batch, assetManager, blurProcessor));
            }
            if (assetManager.isFinished()) {
                stateName = "Building tree";
            }
        } catch (ClassCastException | NumberFormatException e) {
            logException(e);
            log("\n wiping data :) \n");
            clearPrefs();
            log("...done...restarting");
        } catch (Exception e2) {
            logException(e2);
            log("force exiting");
            Gdx.app.exit();
        }
    }
    
}
