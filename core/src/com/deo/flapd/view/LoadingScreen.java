package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.utils.postprocessing.PostProcessor;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.deo.flapd.utils.DUtils.clearPrefs;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getPrefs;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putInteger;

public class LoadingScreen implements Screen {

    private AssetManager assetManager;
    private SpriteBatch batch;
    private BitmapFont main;
    private Game game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ProgressBar loadingBar;
    private ProgressBar.ProgressBarStyle loadingBarStyle;
    private ShapeRenderer shapeRenderer;
    private float rotation, halfRotation, progress, millis;
    private Color color, fillColor;
    private String state;
    private PostProcessor blurProcessor;
    private boolean enableShader;
    private long loadingTime;

    public LoadingScreen(Game game, SpriteBatch batch, AssetManager assetManager, PostProcessor blurProcessor){

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

        loadingBarStyle = new ProgressBar.ProgressBarStyle();

        Pixmap pixmap4 = new Pixmap(0, 24, Pixmap.Format.RGBA8888);
        pixmap4.setColor(Color.valueOf("1979b5"));
        pixmap4.fill();
        TextureRegionDrawable BarForeground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap4)));
        pixmap4.dispose();

        Pixmap pixmap5 = new Pixmap(100, 24, Pixmap.Format.RGBA8888);
        pixmap5.setColor(Color.valueOf("1979b5"));
        pixmap5.fill();
        TextureRegionDrawable BarForeground2 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap5)));
        pixmap5.dispose();

        Pixmap pixmap6 = new Pixmap(800, 40, Pixmap.Format.RGBA8888);
        pixmap6.setColor(Color.BLACK);
        pixmap6.fill();
        TextureRegionDrawable BarBackground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap6)));
        pixmap6.dispose();

        loadingBarStyle.knob = BarForeground;
        loadingBarStyle.knobBefore = BarForeground2;
        loadingBarStyle.background = BarBackground;

        loadingBar = new ProgressBar(0, 100, 0.01f, false, loadingBarStyle);
        loadingBar.setSize(800, 24);
        loadingBar.setPosition(0, 30);
        loadingBar.setAnimateDuration(0.01f);

        Gdx.gl20.glLineWidth(10);

        enableShader = getBoolean("bloom");

        load();
    }

    @Override
    public void show() {

    }

    public void load() {
        assetManager.load("items/items.atlas", TextureAtlas.class);
        assetManager.load("items/parts.atlas", TextureAtlas.class);
        assetManager.load("menuButtons/menuButtons.atlas", TextureAtlas.class);
        assetManager.load("menuButtons/buttons.atlas", TextureAtlas.class);
        assetManager.load("boss_evil/bossEvil.atlas", TextureAtlas.class);
        assetManager.load("boss_ship/bossShip.atlas", TextureAtlas.class);
        assetManager.load("GameOverScreenButtons/GameOverButtons.atlas", TextureAtlas.class);
        assetManager.load("items/items.atlas", TextureAtlas.class);
        assetManager.load("shop/workshop.atlas", TextureAtlas.class);
        assetManager.load("shop/shopButtons.atlas", TextureAtlas.class);

        assetManager.load("bg_layer1.png", Texture.class);
        assetManager.load("bg_layer2.png", Texture.class);
        assetManager.load("bg_layer3.png", Texture.class);
        assetManager.load("ship.png", Texture.class);
        assetManager.load("ColdShield.png", Texture.class);
        assetManager.load("HotShield.png", Texture.class);
        assetManager.load("pew3.png", Texture.class);
        assetManager.load("pew.png", Texture.class);
        assetManager.load("trainingbot.png", Texture.class);
        assetManager.load("enemy_shotgun.png", Texture.class);
        assetManager.load("enemy_sniper.png", Texture.class);
        assetManager.load("pew2.png", Texture.class);
        assetManager.load("Meteo.png", Texture.class);
        assetManager.load("atomic_bomb.png", Texture.class);

        assetManager.load("bonus_bullets.png", Texture.class);
        assetManager.load("bonus_health.png", Texture.class);
        assetManager.load("bonus_part.png", Texture.class);
        assetManager.load("bonus_shield.png", Texture.class);
        assetManager.load("bonus_boss.png", Texture.class);

        assetManager.load("uraniumCell.png", Texture.class);

        assetManager.load("firebutton.png", Texture.class);
        assetManager.load("weaponbutton.png", Texture.class);
        assetManager.load("pause.png", Texture.class);
        assetManager.load("level score indicator.png", Texture.class);
        assetManager.load("health indicator.png", Texture.class);
        assetManager.load("money_display.png", Texture.class);

        assetManager.load("checkpoint.png", Texture.class);
        assetManager.load("checkpoint_green.png", Texture.class);

        assetManager.load("bu1.png", Texture.class);
        assetManager.load("bu2.png", Texture.class);
        assetManager.load("bu3.png", Texture.class);

        assetManager.load("cat.png", Texture.class);
        assetManager.load("cat2.png", Texture.class);
        assetManager.load("cat_meteorite.png", Texture.class);
        assetManager.load("cat_bomb.png", Texture.class);
        assetManager.load("whiskas.png", Texture.class);
        assetManager.load("laser.png", Texture.class);

        assetManager.load("greyishButton.png", Texture.class);

        assetManager.load("menuBg.png", Texture.class);
        assetManager.load("menuFill.png", Texture.class);
        assetManager.load("lamp.png", Texture.class);
        assetManager.load("infoBg.png", Texture.class);
        assetManager.load("bg_old.png", Texture.class);
        assetManager.load("ship.png", Texture.class);

        assetManager.load("checkBox_disabled.png", Texture.class);
        assetManager.load("checkBox_enabled.png", Texture.class);
        assetManager.load("checkBox_disabled_over.png", Texture.class);
        assetManager.load("checkBox_enabled_over.png", Texture.class);
        assetManager.load("progressBarKnob.png", Texture.class);
        assetManager.load("progressBarBg.png", Texture.class);
        assetManager.load("progressBarKnob_over.png", Texture.class);
        assetManager.load("progressBarKnob_enabled.png", Texture.class);
    }

    @Override
    public void render(float delta){
        Gdx.gl.glClearColor(0,0.2f,0.25f,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        rotation-=200*delta;
        halfRotation+=70*delta;

        millis+=200*delta;

        if(millis>0){
            state = ".";
        }

        if(millis > 120){
            state = "..";
        }

        if(millis > 240){
            state = "...";
        }

        if(millis > 360){
            millis = 0;
        }

        progress = assetManager.getProgress();

        color = new Color().add(0.5f/progress, progress+0.1f,0,1);
        fillColor = new Color().add(0.0f, 0.1f,0.15f,1);

        if(enableShader){
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
        shapeRenderer.polygon(new float[]{calculateOffsetX(halfRotation),calculateOffsetY(halfRotation),calculateOffsetX(halfRotation+60),calculateOffsetY(halfRotation+60),calculateOffsetX(halfRotation+120),calculateOffsetY(halfRotation+120),calculateOffsetX(halfRotation+180),calculateOffsetY(halfRotation+180),calculateOffsetX(halfRotation+240),calculateOffsetY(halfRotation+240),calculateOffsetX(halfRotation+300),calculateOffsetY(halfRotation+300),});
        for(int i = 0; i < 53; i++) {
            shapeRenderer.setColor(0.1f,0.5f, Math.abs(30*MathUtils.sin((i-rotation/20)*6.8f*MathUtils.degreesToRadians))/30, 1);
            shapeRenderer.rect(5+15*i, 60, 10, Math.abs(30*MathUtils.sin((i+rotation/10)*6.8f*MathUtils.degreesToRadians)));
        }
        shapeRenderer.end();

        batch.begin();
        loadingBar.setValue(progress*100);
        loadingBar.draw(batch, 1);
        loadingBar.act(delta);
        main.getData().setScale(0.8f);
        main.setColor(Color.CYAN);
        main.draw(batch, "Loading"+state, 0, 440, 800, 1, false);
        main.getData().setScale(0.5f);
        main.setColor(Color.ORANGE);
        main.draw(batch, (int)(assetManager.getProgress()*100)+"%", 0, 47, 800, 1, false);
        assetManager.update();
        batch.end();

        if(enableShader){
            blurProcessor.render();
        }

        try {
            if (assetManager.isFinished()) {
                float elapsedTime = TimeUtils.timeSinceMillis(loadingTime)/1000.0f;
                float relativePercentage = (100 - elapsedTime * 100f / 3);
                if(relativePercentage>=0){
                    log("\n loaded, elapsed time " + elapsedTime + "s(" + relativePercentage + "% better than average)");
                }else{
                    log("\n loaded, elapsed time " + elapsedTime + "s(" + -relativePercentage + "% worse than average)");
                }
                game.setScreen(new MenuScreen(game, batch, assetManager, blurProcessor));
            }
        }catch (ClassCastException | NumberFormatException e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String fullStackTrace = sw.toString();
            log("\n"+fullStackTrace + "\n");
            log("\n wiping data :) \n");
            clearPrefs();
            putInteger("money", 7000);
            putFloat("ui", 1.25f);
            putFloat("soundEffectsVolume", 1);
            putFloat("musicVolume", 1 );
            putFloat("difficulty", 1);
            putBoolean("transparency", true);
            putBoolean("shaders", false);
            log("dump pf preferences "+getPrefs()+"\n");
            log("...done...restarting");
        } catch (Exception e2) {
            StringWriter sw = new StringWriter();
            e2.printStackTrace(new PrintWriter(sw));
            String fullStackTrace = sw.toString();
            log("\n" + fullStackTrace + "\n");
            log("dump pf preferences "+getPrefs()+"\n");
            log("force exiting");
            System.exit(1);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(400, 240, 0);
        float tempScaleH = height/480.0f;
        float tempScaleW = width/800.0f;
        float zoom = Math.min(tempScaleH, tempScaleW);
        camera.zoom = 1/zoom;
        Gdx.gl20.glLineWidth(10.0f/camera.zoom);
        camera.update();
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

    public void dispose(){
        main.dispose();
    }

    private float calculateOffsetX(float rotation){
        rotation = 400 - MathUtils.cosDeg(rotation) * 140;
        return rotation;
    }

    private float calculateOffsetY(float rotation){
        rotation = 240 - MathUtils.sinDeg(rotation) * 140;
        return rotation;
    }

    private float calculateProgressOffsetX(float rotation){
        rotation = 400 - MathUtils.cosDeg(rotation) * 550 * (progress+0.1f);
        return rotation;
    }

    private float calculateProgressOffsetY(float rotation){
        rotation = 240 - MathUtils.sinDeg(rotation) * 550 * (progress+0.1f);
        return rotation;
    }

}
