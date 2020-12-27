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
import com.deo.flapd.model.SpaceShip;
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

    private Texture bg1;
    private Texture bg2;
    private Texture bg3;
    private Texture FillTexture;
    private int horizontalFillingThreshold;
    private int verticalFillingThreshold;

    private Meteorites meteorites;
    private UraniumCell uraniumCell;
    private Checkpoint checkpoint;

    private SpriteBatch batch;

    private SpaceShip ship;
    private GameUi gameUi;
    private GameLogic gameLogic;

    private OrthographicCamera camera;
    private Viewport viewport;

    static boolean is_paused;

    private float movement;

    private Game game;

    private MusicManager musicManager;

    private Bonus bonus;

    private Drops drops;

    private PostProcessor postProcessor;

    private boolean enableShader;

    private Enemies enemies;
    private Bosses bosses;

    private boolean drawScreenExtenders = true;

    GameScreen(final Game game, SpriteBatch batch, AssetManager assetManager, PostProcessor blurProcessor, boolean newGame) {

        this.game = game;

        this.batch = batch;

        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);

        bg1 = assetManager.get("bg_layer1.png");
        bg2 = assetManager.get("bg_layer2.png");
        bg3 = assetManager.get("bg_layer3.png");
        FillTexture = assetManager.get("menuFill.png");

        bg1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bg2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bg3.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        enemies = new Enemies(assetManager);
        enemies.loadEnemies();

        bosses = new Bosses(assetManager);
        bosses.loadBosses();

        ship = new SpaceShip(assetManager, 0, 204, newGame, enemies);

        enemies.setTargetPlayer(ship);
        bosses.setTargetPlayer(ship);

        uraniumCell = new UraniumCell(assetManager);

        bonus = new Bonus(assetManager, 50, 50, ship, bosses);

        drops = new Drops(assetManager, 48, getFloat("ui"));

        gameUi = new GameUi(game, batch, assetManager, blurProcessor, ship);

        meteorites = new Meteorites(assetManager, newGame, getBoolean("easterEgg"));

        checkpoint = new Checkpoint(assetManager, ship);

        gameLogic = new GameLogic(ship, newGame, game, meteorites, checkpoint);

        musicManager = new MusicManager("music/main", 1, 5, 5);
        musicManager.setVolume(getFloat("musicVolume") / 100f);

        enableShader = getBoolean("bloom");
        postProcessor = blurProcessor;

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        if (is_paused) {
            delta = 0;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameLogic.handleInput(delta, gameUi.getDeltaX(), gameUi.getDeltaY(), gameUi.is_firing, gameUi.is_firing_secondary);

        if (enableShader) {
            postProcessor.capture();
        }
        batch.begin();

        batch.draw(bg1, 0, 0, (int) (movement / 4), -240, 800, 720);
        meteorites.update(delta);
        meteorites.drawEffects(batch);
        meteorites.drawBase(batch);
        batch.draw(bg2, 0, 0, (int) (movement / 2), -240, 800, 720);
        batch.draw(bg3, 0, 0, (int) movement, -240, 800, 720);

        bosses.draw(batch, delta);
        bosses.update(delta);
        ship.drawEffects(batch, delta);
        enemies.drawEffects(batch, delta);
        checkpoint.drawEffects(batch, delta);

        if (enableShader) {
            batch.end();
            postProcessor.render();
            batch.begin();
        }

        ship.drawBase(batch, delta);
        enemies.draw(batch);
        enemies.update(delta);
        checkpoint.drawBase(batch);

        ship.DrawShield(batch, delta);

        bonus.draw(batch, delta);
        drops.draw(batch, delta);
        uraniumCell.draw(batch, delta);

        gameUi.draw();

        movement += (200 * delta);
        if (movement > 2880) {
            movement = 0;
        }

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

        musicManager.dispose();

        gameUi.dispose();
        meteorites.dispose();

        ship.dispose();

        bonus.dispose();
        drops.dispose();
        uraniumCell.dispose();

        enemies.dispose();
        bosses.dispose();

        logVariables();
    }
}
