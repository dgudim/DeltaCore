package com.deo.flapd.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.enemies.Bosses;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.model.environment.EnvironmentalEffects;
import com.deo.flapd.model.loot.Bonus;
import com.deo.flapd.model.loot.Drops;
import com.deo.flapd.model.loot.UraniumCell;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.MusicManager;
import com.deo.flapd.utils.SoundManager;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.view.overlays.GameUi;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.deo.flapd.utils.DUtils.drawBg;
import static com.deo.flapd.utils.DUtils.drawScreenExtenders;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.updateCamera;

public class GameScreen implements Screen {
    
    private final Texture bg1;
    private final Texture bg2;
    private final Texture fillTexture;
    private int horizontalFillingThreshold;
    private int verticalFillingThreshold;
    
    private final EnvironmentalEffects environmentalEffects;
    private final UraniumCell uraniumCell;
    private final Checkpoint checkpoint;
    
    private final SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    
    private final Player player;
    private final GameUi gameUi;
    private final GameLogic gameLogic;
    
    private final OrthographicCamera camera;
    private final ScreenViewport viewport;
    
    public static volatile boolean is_paused;
    public static volatile float playerDeltaMultiplier = 1;
    public static volatile float globalDeltaMultiplier = 1;
    
    private float movement;
    
    private final Game game;
    
    private final MusicManager musicManager;
    private final SoundManager soundManager;
    
    private final Bonus bonus;
    
    private final Drops drops;
    
    private final PostProcessor postProcessor;
    
    private final boolean enableShader;
    
    private final Enemies enemies;
    private final Bosses bosses;
    
    private boolean drawScreenExtenders = true;
    private final boolean drawDebug;
    
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
        
        AssetManager assetManager = compositeManager.getAssetManager();
        game = compositeManager.getGame();
        musicManager = compositeManager.getMusicManager();
        soundManager = compositeManager.getSoundManager();
        batch = compositeManager.getBatch();
        postProcessor = compositeManager.getBlurProcessor();
        
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        
        drawDebug = getBoolean("drawDebug");
        shapeRenderer = compositeManager.getShapeRenderer();
        
        bg1 = assetManager.get("backgrounds/bg_layer1.png");
        bg2 = assetManager.get("backgrounds/bg_layer2.png");
        fillTexture = assetManager.get("screenFill.png");
        
        bg1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        bg2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        
        enemies = new Enemies(assetManager);
        enemies.loadEnemies();
        
        bosses = new Bosses();
        bosses.loadBosses(assetManager, musicManager);
        
        player = new Player(assetManager, 0, 204, newGame, enemies);
        
        enemies.setTargetPlayer(player);
        bosses.setTargetPlayer(player);
        
        uraniumCell = new UraniumCell(assetManager);
        
        bonus = new Bonus(assetManager, 50, 50, player, bosses);
        
        drops = new Drops(assetManager, 48, getFloat("ui"));
        
        gameUi = new GameUi(viewport, compositeManager, player);
        
        environmentalEffects = new EnvironmentalEffects(assetManager);
        
        checkpoint = new Checkpoint(assetManager, player);
        
        gameLogic = new GameLogic(player, newGame, game, environmentalEffects, checkpoint);
        
        this.musicManager.setNewMusicSource("music/main", 1, 5, 5);
        this.musicManager.setVolume(getFloat("musicVolume") / 100f);
        
        enableShader = getBoolean("bloom");
        
        soundManager.playSound("ftl_flight");
    }
    
    @Override
    public void show() {
    
    }
    
    @Override
    public void render(float delta) {
    
        delta = is_paused ? 0 : delta;
        float originalDelta = delta;
        float playerDelta = delta * playerDeltaMultiplier;
        delta *= globalDeltaMultiplier;
        
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
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        gameLogic.handleInput(playerDelta, gameUi.getDeltaX(), gameUi.getDeltaY(), gameUi.is_firing, gameUi.is_firing_secondary);
        
        if (enableShader) {
            postProcessor.capture();
        }
        batch.begin();
        batch.enableBlending();
        
        environmentalEffects.update(delta);
        environmentalEffects.drawEffects(batch);
        environmentalEffects.drawBase(batch);
        
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
        
        drawBg(batch, bg1, bg2, warpSpeed, movement);
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
        checkpoint.drawBase(batch);
        
        player.drawShield(batch, delta);
        
        bonus.draw(batch, delta);
        drops.draw(batch, delta);
        uraniumCell.draw(batch, delta);
        
        if (warpSpeed == 0) {
            gameUi.draw(originalDelta);
        }
        
        if (drawScreenExtenders) {
            drawScreenExtenders(batch, fillTexture, verticalFillingThreshold, horizontalFillingThreshold);
        }
        
        batch.end();
        
        if (drawDebug) {
            shapeRenderer.begin();
            bosses.drawDebug(shapeRenderer);
            player.drawDebug(shapeRenderer);
            enemies.drawDebug(shapeRenderer);
            shapeRenderer.end();
        }
        
        musicManager.update(delta);
        
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            camera.zoom *= 1.01;
            camera.update();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            camera.zoom *= 0.99;
            camera.update();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            drawScreenExtenders = !drawScreenExtenders;
        }
    }
    
    @Override
    public void resize(int width, int height) {
        updateCamera(camera, viewport, width, height);
        
        float targetHeight = viewport.getScreenHeight();
        float targetWidth = viewport.getScreenWidth();
        
        float sourceHeight = 480.0f;
        float sourceWidth = 800.0f;
        
        float targetRatio = targetHeight / targetWidth;
        float sourceRatio = sourceHeight / sourceWidth;
        float scale;
        if (targetRatio > sourceRatio) {
            scale = targetWidth / sourceWidth;
        } else {
            scale = targetHeight / sourceHeight;
        }
        
        int actualWidth = (int) (sourceWidth * scale);
        int actualHeight = (int) (sourceHeight * scale);
        
        Gdx.gl.glLineWidth(1 / camera.zoom);
        
        verticalFillingThreshold = (int) Math.ceil((targetHeight - actualHeight) / 144);
        horizontalFillingThreshold = (int) Math.ceil((targetWidth - actualWidth) / 912);
    }
    
    public static void screenShake(float intensity, float duration) {
        screenShakeIntensity = intensity;
        screenShakeIntensityDuration = duration;
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
        game.getScreen().dispose();
    }
    
    @Override
    public void dispose() {
        
        gameUi.dispose();
        environmentalEffects.dispose();
        
        player.dispose();
        
        bonus.dispose();
        drops.dispose();
        uraniumCell.dispose();
        
        enemies.dispose();
        bosses.dispose();
    }
}
