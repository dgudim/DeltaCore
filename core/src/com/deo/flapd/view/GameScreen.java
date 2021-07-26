package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.Meteorites;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.enemies.Bosses;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.utils.MusicManager;
import com.deo.flapd.utils.postprocessing.PostProcessor;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.logVariables;
import static com.deo.flapd.utils.DUtils.updateCamera;

public class GameScreen implements Screen {
    
    private final Texture bg1;
    private final Texture bg2;
    private final Texture FillTexture;
    private int horizontalFillingThreshold;
    private int verticalFillingThreshold;
    
    private final Meteorites meteorites;
    private final UraniumCell uraniumCell;
    private final Checkpoint checkpoint;
    
    private final SpriteBatch batch;
    
    private final Player player;
    private final GameUi gameUi;
    private final GameLogic gameLogic;
    
    private final OrthographicCamera camera;
    private final Viewport viewport;
    
    static boolean is_paused;
    
    private float movement;
    
    private final Game game;
    
    private final MusicManager musicManager;
    
    private final Bonus bonus;
    
    private final Drops drops;
    
    private final PostProcessor postProcessor;
    
    private final boolean enableShader;
    
    private final Enemies enemies;
    private final Bosses bosses;
    
    private boolean drawScreenExtenders = true;
    
    GameScreen(final Game game, SpriteBatch batch, AssetManager assetManager, PostProcessor blurProcessor, MusicManager musicManager, boolean newGame) {
        
        this.game = game;
        this.musicManager = musicManager;
        
        this.batch = batch;
        
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        
        bg1 = assetManager.get("backgrounds/bg_layer1.png");
        bg2 = assetManager.get("backgrounds/bg_layer2.png");
        FillTexture = assetManager.get("screenFill.png");
        
        bg1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        bg2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        
        enemies = new Enemies(assetManager);
        enemies.loadEnemies();
        
        bosses = new Bosses(assetManager);
        bosses.loadBosses();
        
        player = new Player(assetManager, 0, 204, newGame, enemies);
        
        enemies.setTargetPlayer(player);
        bosses.setTargetPlayer(player);
        
        uraniumCell = new UraniumCell(assetManager);
        
        bonus = new Bonus(assetManager, 50, 50, player, bosses);
        
        drops = new Drops(assetManager, 48, getFloat("ui"));
        
        gameUi = new GameUi(game, batch, assetManager, blurProcessor, player, musicManager);
        
        meteorites = new Meteorites(assetManager, getBoolean("easterEgg"));
        
        checkpoint = new Checkpoint(assetManager, player);
        
        gameLogic = new GameLogic(player, newGame, game, meteorites, checkpoint);
        
        this.musicManager.setNewMusicSource("music/main", 1, 5, 5);
        this.musicManager.setVolume(getFloat("musicVolume") / 100f);
        
        enableShader = getBoolean("bloom");
        postProcessor = blurProcessor;
    }
    
    @Override
    public void show() {
    
    }
    
    @Override
    public void render(float delta) {
        
        delta = is_paused ? 0 : delta;
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameLogic.handleInput(delta, gameUi.getDeltaX(), gameUi.getDeltaY(), gameUi.is_firing, gameUi.is_firing_secondary);
        
        if (enableShader) {
            postProcessor.capture();
        }
        batch.begin();
        
        batch.draw(bg1, 0, 0, (int) (movement * 50), -240, 800, 720);
        batch.draw(bg2, 0, 0, (int) (movement * 53), -240, 800, 720);
        meteorites.update(delta);
        meteorites.drawEffects(batch);
        meteorites.drawBase(batch);
        
        bosses.draw(batch, delta);
        bosses.update(delta);
        player.drawEffects(batch, delta);
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
        
        gameUi.draw();
        
        movement += delta;
        
        if (drawScreenExtenders) {
            for (int i = 0; i < verticalFillingThreshold; i++) {
                batch.draw(FillTexture, 0, -72 * (i + 1), 456, 72);
                batch.draw(FillTexture, 456, -72 * (i + 1), 456, 72);
                batch.draw(FillTexture, 0, 408 + 72 * (i + 1), 456, 72);
                batch.draw(FillTexture, 456, 408 + 72 * (i + 1), 456, 72);
            }
            
            for (int i = 0; i < horizontalFillingThreshold; i++) {
                for (int i2 = 0; i2 < 7; i2++) {
                    batch.draw(FillTexture, -456 - 456 * i, 408 - i2 * 72, 456, 72);
                    batch.draw(FillTexture, 800 + 456 * i, 408 - i2 * 72, 456, 72);
                }
            }
        }
        
        batch.end();
        
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
        gameUi.resize(width, height);
        
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
        
        verticalFillingThreshold = (int) Math.ceil((targetHeight - actualHeight) / 144);
        horizontalFillingThreshold = (int) Math.ceil((targetWidth - actualWidth) / 912);
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
        meteorites.dispose();
        
        player.dispose();
        
        bonus.dispose();
        drops.dispose();
        uraniumCell.dispose();
        
        enemies.dispose();
        bosses.dispose();
        
        logVariables();
    }
}
