package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Drops;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.enemies.Boss_battleShip;
import com.deo.flapd.model.enemies.Boss_evilEye;
import com.deo.flapd.model.enemies.Enemies;
import com.deo.flapd.utils.MusicWave;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.utils.postprocessing.effects.Bloom;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.logException;
import static com.deo.flapd.utils.DUtils.updateCamera;

public class GameScreen implements Screen {

    private Texture bg1;
    private Texture bg2;
    private Texture bg3;
    private Texture FillTexture;
    private final int fillingThreshold = 7;

    private Meteorite meteorite;
    private Boss_battleShip boss_battleShip;
    private Boss_evilEye boss_evilEye;
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

    private Music music;
    private boolean Music;
    private float millis, millis2, musicVolume;

    private Bonus bonus;

    private Drops drops;

    private PostProcessor postProcessor;

    private boolean enableShader;

    private Enemies enemies;

    private float[] samples;
    private Music music2;
    private float maxValue;
    private Array<Float> displayData;
    private Image bar;

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

        ship = new SpaceShip(assetManager, 0, 204, newGame, enemies);

        enemies.setTargetPlayer(ship);

        uraniumCell = new UraniumCell(assetManager);

        boss_battleShip = new Boss_battleShip(assetManager, 1100, 150, ship);

        boss_evilEye = new Boss_evilEye(assetManager, ship);

        bonus = new Bonus(assetManager, 50, 50, ship, boss_battleShip, boss_evilEye);

        drops = new Drops(assetManager, 48, getFloat("ui"));

        gameUi = new GameUi(game, batch, assetManager, blurProcessor, ship);

        meteorite = new Meteorite(assetManager, newGame, getBoolean("easterEgg"));

        checkpoint = new Checkpoint(assetManager, ship);

        gameLogic = new GameLogic(ship, newGame, game, meteorite, boss_battleShip, checkpoint, boss_evilEye);

        musicVolume = getFloat("musicVolume");

        if (musicVolume > 0) {
            Music = true;
        }

        music = Gdx.audio.newMusic(Gdx.files.internal("music/main" + getRandomInRange(1, 5) + ".ogg"));

        enableShader = getBoolean("bloom");
        postProcessor = blurProcessor;

        MusicWave musicWave = new MusicWave();

        displayData = new Array<>();

        Pixmap pixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        TextureRegionDrawable BarBackgroundBlank = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        bar = new Image(BarBackgroundBlank);

        samples = musicWave.getSamples();
        music2 = musicWave.getMusic();

        for(int i = 0; i<samples.length; i++){
            if(samples[i]>maxValue){
                maxValue = samples[i];
            }
        }

        music2.play();
    }

    @Override
    public void show() {
        millis2 = 101;
        music.setVolume(0);
    }

    @Override
    public void render(float delta) {

        if (is_paused) {
            delta = 0;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameLogic.handleInput(delta, gameUi.getDeltaX(), gameUi.getDeltaY(), gameUi.is_firing, gameUi.is_firing_secondary);

        try {
            gameLogic.detectCollisions(is_paused);
        } catch (Exception e) {
            logException(e);
        }

        if (enableShader) {
            Bloom effect = (Bloom)postProcessor.effectsManager.get(0);
            float value = Math.abs(samples[(int)(music2.getPosition()*44100)]/maxValue);
            effect.setBloomSaturation(value*2.3f);
            //displayData.add(value*150);
            if(displayData.size>401){
                displayData.removeIndex(0);
            }
            postProcessor.capture();
        }
        batch.begin();

        batch.draw(bg1, 0, 0, (int) (movement / 4), -240, 800, 720);
        batch.draw(bg2, 0, 0, (int) (movement / 2), -240, 800, 720);
        batch.draw(bg3, 0, 0, (int) movement, -240, 800, 720);

        bonus.draw(batch, delta);
        drops.draw(batch, delta);
        uraniumCell.draw(batch, delta);

        ship.drawEffects(batch, delta);
        enemies.drawEffects(batch, delta);
        boss_battleShip.draw(batch, delta);
        boss_evilEye.draw(batch, delta);
        meteorite.drawEffects(batch, delta);
        checkpoint.drawEffects(batch, delta);

        if (enableShader) {
            batch.end();
            postProcessor.render();
            batch.begin();
        }

        for(int i = 0; i<displayData.size; i++){
            bar.setBounds(400+i, 0, 2, displayData.get(displayData.size-1-i)+10);
            bar.setColor(new Color().fromHsv(i/3.0f+110, 1.5f, 1).add(0, 0, 0, 1));
            bar.draw(batch, 1);
            bar.setBounds(400-i, 0, 2, displayData.get(displayData.size-1-i)+10);
            bar.setColor(new Color().fromHsv(i/3.0f+110, 1.5f, 1).add(0, 0, 0, 1));
            bar.draw(batch, 1);
        }

        ship.drawBase(batch, delta);
        enemies.draw(batch);
        enemies.update(delta);
        meteorite.drawBase(batch, delta);
        checkpoint.drawBase(batch);

        ship.DrawShield(batch, delta);

        gameUi.draw();

        movement += (200 * delta);
        if (movement > 2880) {
            movement = 0;
        }

        for (int i = 0; i < fillingThreshold; i++) {
            batch.draw(FillTexture, 0, -72 * (i + 1), 456, 72);
            batch.draw(FillTexture, 456, -72 * (i + 1), 456, 72);
            batch.draw(FillTexture, 0, 408 + 72 * (i + 1), 456, 72);
            batch.draw(FillTexture, 456, 408 + 72 * (i + 1), 456, 72);
        }

        for (int i = 0; i < (fillingThreshold / 6) + 1; i++) {
            for (int i2 = 0; i2 < 7; i2++) {
                batch.draw(FillTexture, -456 - 456 * i, 408 - i2 * 72, 456, 72);
                batch.draw(FillTexture, 800 + 456 * i, 408 - i2 * 72, 456, 72);
            }
        }

        batch.end();

        if (Music) {

            if (millis > 10) {
                if (music.getPosition() > 65 && music.getPosition() < 69 && music.getVolume() > 0) {
                    music.setVolume(music.getVolume() - 0.05f);
                }
                if (music.getPosition() > 0 && music.getPosition() < 4 && music.getVolume() < musicVolume) {
                    music.setVolume(music.getVolume() + 0.05f);
                }
                millis = 0;
            }

            millis = millis + 50 * delta;
            millis2 = millis2 + 0.5f * delta;

            if (millis2 > 100) {
                music.dispose();
                music = Gdx.audio.newMusic(Gdx.files.internal("music/main" + getRandomInRange(1, 5) + ".ogg"));
                music.setPosition(1);
                music.setVolume(0);
                music.play();
                millis2 = 0;
            }

        }
    }

    @Override
    public void resize(int width, int height) {
        updateCamera(camera, viewport, width, height);
        gameUi.resize(width, height);
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
        music.stop();
        game.getScreen().dispose();
    }

    @Override
    public void dispose() {

        gameUi.dispose();
        meteorite.dispose();

        ship.dispose();

        boss_battleShip.dispose();
        boss_evilEye.dispose();

        music.dispose();

        bonus.dispose();
        drops.dispose();
        uraniumCell.dispose();

        enemies.dispose();

        music2.dispose();
    }
}
