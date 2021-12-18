package com.deo.flapd.view.screens;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.deo.flapd.utils.DUtils.drawBg;
import static com.deo.flapd.utils.DUtils.drawScreenExtenders;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getVerticalAndHorizontalFillingThresholds;
import static com.deo.flapd.utils.DUtils.handleDebugInput;
import static com.deo.flapd.utils.DUtils.updateCamera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.deo.flapd.control.GameVariables;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.enemies.Bosses;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.model.environment.EnvironmentalEffects;
import com.deo.flapd.model.loot.Drops;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.MusicManager;
import com.deo.flapd.utils.SoundManager;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.view.overlays.GameUi;

public class GameScreen implements Screen {
    
    private final Texture bg1;
    private final Texture bg2;
    private final Texture fillTexture;
    private int horizontalFillingThreshold;
    private int verticalFillingThreshold;
    
    private EnvironmentalEffects environmentalEffects;
    private Checkpoint checkpoint;
    
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    
    private Player player;
    private GameUi gameUi;
    
    private final OrthographicCamera camera;
    private final ScreenViewport viewport;
    
    public static volatile boolean is_paused;
    public static volatile float playerDeltaMultiplier = 1;
    public static volatile float globalDeltaMultiplier = 1;
    
    private float movement;
    
    private final CompositeManager compositeManager;
    private final MusicManager musicManager;
    private final SoundManager soundManager;
    
    private Drops drops;
    
    private final PostProcessor postProcessor;
    
    private boolean enableShader;
    
    private Enemies enemies;
    private Bosses bosses;
    
    private boolean drawScreenExtenders = true;
    private boolean drawDebug;
    
    private static float screenShakeIntensity;
    private static float screenShakeIntensityDuration;
    private float previousShakeOffsetX;
    private float previousShakeOffsetY;
    private float cameraZoomOffset;
    
    private float warpTime = 0;
    private float warpSpeed = 70;
    private boolean warpSoundPlaying = true;
    private float previousFireMotionScale = 1;
    
    public GameScreen(CompositeManager compositeManager, boolean newGame) {
        
        this.compositeManager = compositeManager;
        AssetManager assetManager = compositeManager.getAssetManager();
        musicManager = compositeManager.getMusicManager();
        soundManager = compositeManager.getSoundManager();
        batch = compositeManager.getBatch();
        postProcessor = compositeManager.getBlurProcessor();
        shapeRenderer = compositeManager.getShapeRenderer();
        
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        
        bg1 = assetManager.get("backgrounds/bg_layer1.png");
        bg2 = assetManager.get("backgrounds/bg_layer2.png");
        fillTexture = assetManager.get("screenFill.png");
        
        bg1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        bg2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        
        reset(newGame);
    }
    
    public void reset(boolean newGame) {
        drawDebug = getBoolean(Keys.drawDebug);
        
        warpTime = 0;
        warpSpeed = 70;
        warpSoundPlaying = true;
        
        if(enemies != null){
            enemies.dispose();
        }
        enemies = new Enemies(compositeManager);
        enemies.loadEnemies();
    
        if(player != null){
            player.dispose();
        }
        player = new Player(compositeManager, 0, 204, newGame, enemies);
    
        if(drops != null){
            drops.dispose();
        }
        drops = new Drops(compositeManager, 50, player);
        compositeManager.setDrops(drops);
    
        if(bosses != null){
            bosses.dispose();
        }
        bosses = new Bosses(compositeManager, player);
    
        enemies.setTargetPlayer(player);
        
        if(gameUi != null){
            gameUi.dispose();
        }
        gameUi = new GameUi(viewport, compositeManager, player);
    
        if(environmentalEffects != null){
            environmentalEffects.dispose();
        }
        environmentalEffects = new EnvironmentalEffects(compositeManager);
        
        checkpoint = new Checkpoint(compositeManager, player, newGame);
    
        GameVariables.init(newGame);
    
        musicManager.setNewMusicSource("music/main", 1, 5, 5);
        musicManager.setVolume(getFloat(Keys.musicVolume) / 100f);
    
        enableShader = getBoolean(Keys.enableBloom);
    
        soundManager.playSound("ftl_flight");
        
    }
    
    @Override
    public void show() {
    
    }
    
    @Override
    public void render(float delta) {
        
        delta = (is_paused && warpSpeed == 0) ? 0 : delta;
        float originalDelta = delta;
        float playerDelta = delta * playerDeltaMultiplier;
        delta *= globalDeltaMultiplier;
        
        updateScreenShake(delta);
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        player.update(gameUi.getDeltaX(), gameUi.getDeltaY(), playerDelta, gameUi.is_firing, gameUi.is_firing_secondary);
        
        if (enableShader) {
            postProcessor.capture();
        }
        batch.begin();
        batch.enableBlending();
        
        if (warpSpeed > 0) {
            warpTime += delta;
            player.scaleFireMotion((1 / previousFireMotionScale) * (warpSpeed / 17.5f + 1));
            previousFireMotionScale = warpSpeed / 17.5f + 1;
            player.setPositionAndRotation(player.x + delta * warpSpeed * 3, player.y, player.rotation);
            soundManager.setPitch("ftl_flight", (float) (0.5 + warpSpeed / 46.67));
        } else if (warpSoundPlaying) {
            soundManager.stopSound("ftl_flight");
            warpSoundPlaying = false;
        }
        
        drawBg(batch, bg1, bg2, warpSpeed, movement, environmentalEffects, delta);
        if (warpTime > 1 && warpSpeed > 0) {
            warpSpeed = clamp(warpSpeed - delta * 100, 0, warpSpeed);
        }
        
        movement += delta * (warpSpeed + 1);
        
        bosses.draw(batch, delta);
        bosses.update(delta);
        player.drawEffects(batch, playerDelta);
        player.drawBullets(batch, delta);
        enemies.drawEffects(batch, delta);
        checkpoint.drawEffects(batch, delta);
        
        if (enableShader) {
            batch.end();
            postProcessor.render();
            batch.begin();
        }
        
        player.drawSprites(batch, delta);
        enemies.draw(batch);
        enemies.update(delta);
        checkpoint.update(delta);
        checkpoint.drawBase(batch);
        
        player.drawShield(batch, delta);
        
        drops.draw(batch, delta);
        
        if (warpSpeed == 0) {
            gameUi.draw(originalDelta);
        }
        
        if (drawScreenExtenders = handleDebugInput(camera, drawScreenExtenders)) {
            drawScreenExtenders(batch, fillTexture, verticalFillingThreshold, horizontalFillingThreshold);
        }
        
        batch.end();
        
        if (drawDebug) {
            shapeRenderer.begin();
            bosses.drawDebug(shapeRenderer);
            drops.drawDebug(shapeRenderer);
            player.drawDebug(shapeRenderer);
            enemies.drawDebug(shapeRenderer);
            shapeRenderer.end();
        }
        
        musicManager.update(delta);
    }
    
    @Override
    public void resize(int width, int height) {
        updateCamera(camera, viewport, width, height);
        Gdx.gl.glLineWidth(1 / camera.zoom);
        
        int[] fillingThresholds = getVerticalAndHorizontalFillingThresholds(viewport);
        verticalFillingThreshold = fillingThresholds[0];
        horizontalFillingThreshold = fillingThresholds[1];
    }
    
    private void updateScreenShake(float delta){
        if (screenShakeIntensityDuration > 0) {
            float nextShakeX = (float) ((random() - 0.5) * 2 * screenShakeIntensity);
            float nextShakeY = (float) ((random() - 0.5) * 2 * screenShakeIntensity);
            if (cameraZoomOffset == 0) {
                cameraZoomOffset = screenShakeIntensity / 300f;
                camera.zoom -= screenShakeIntensity / 300f;
            }
            camera.translate(nextShakeX - previousShakeOffsetX, nextShakeY - previousShakeOffsetY);
            camera.update();
            previousShakeOffsetX = nextShakeX;
            previousShakeOffsetY = nextShakeY;
            screenShakeIntensityDuration -= delta;
        } else if (previousShakeOffsetX != 0 || previousShakeOffsetY != 0) {
            camera.zoom += cameraZoomOffset;
            cameraZoomOffset = 0;
            camera.translate(-previousShakeOffsetX, -previousShakeOffsetY);
            camera.update();
            previousShakeOffsetX = 0;
            previousShakeOffsetY = 0;
        }
    }
    
    public static void screenShake(float intensity, float duration) {
        if (!is_paused) {
            screenShakeIntensity = intensity;
            screenShakeIntensityDuration = duration;
        } else {
            screenShakeIntensity = 0;
            screenShakeIntensityDuration = 0;
        }
    }
    
    @Override
    public void pause() {
        is_paused = true;
    }
    
    @Override
    public void resume() {
        postProcessor.rebind();
    }
    
    @Override
    public void hide() {
    }
    
    @Override
    public void dispose() {
        gameUi.dispose();
        environmentalEffects.dispose();
        
        player.dispose();
        
        drops.dispose();
        
        enemies.dispose();
        bosses.dispose();
    }
}
