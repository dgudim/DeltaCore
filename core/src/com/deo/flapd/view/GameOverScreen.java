package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.ShipObject;
import com.deo.flapd.utils.postprocessing.PostProcessor;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.putInteger;
import static com.deo.flapd.utils.DUtils.updateCamera;

public class GameOverScreen implements Screen {

    private int Score, enemiesKilled, bulletsShot, highScore, moneyEarned;

    private float difficulty;

    private Stage stage;

    private Image GameOver;
    private Button Menu;
    private Button Restart;
    private Skin buttonSkin;
    private Button.ButtonStyle buttonStyle, buttonStyle2;

    private OrthographicCamera camera;
    private Viewport viewport;

    private SpriteBatch batch;

    private BitmapFont font_main;

    private Game game;

    private Boolean isNewHighScore;

    private PostProcessor blurProcessor;

    private boolean enableShader;

    private AssetManager assetManager;

    GameOverScreen(final Game game, final SpriteBatch batch, final AssetManager assetManager, final PostProcessor blurProcessor, ShipObject player) {

        this.batch = batch;

        this.game = game;

        this.blurProcessor = blurProcessor;

        this.assetManager = assetManager;

        enableShader = getBoolean("bloom");

        Score = GameLogic.Score;

        highScore = getInteger("highScore");

        difficulty = getFloat("difficulty");

        if (Score > highScore) {
            putInteger("highScore", Score);
            highScore = Score;
            isNewHighScore = true;
        } else {
            isNewHighScore = false;
        }

        enemiesKilled = GameLogic.enemiesKilled;

        bulletsShot = player.bulletsShot;

        moneyEarned = GameLogic.moneyEarned;

        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);

        while (!assetManager.isFinished()) {
            assetManager.update();
        }

        font_main = assetManager.get("fonts/font2(old).fnt");

        stage = new Stage(viewport, batch);

        buttonSkin = new Skin();
        buttonSkin.addRegions((TextureAtlas) assetManager.get("GameOverScreenButtons/GameOverButtons.atlas"));

        buttonStyle = new Button.ButtonStyle();
        buttonStyle.down = buttonSkin.getDrawable("restart_e");
        buttonStyle.up = buttonSkin.getDrawable("restart_d");
        buttonStyle.over = buttonSkin.getDrawable("restart_o");

        buttonStyle2 = new Button.ButtonStyle();
        buttonStyle2.down = buttonSkin.getDrawable("menu_e");
        buttonStyle2.up = buttonSkin.getDrawable("menu_d");
        buttonStyle2.over = buttonSkin.getDrawable("menu_o");

        GameOver = new Image(((TextureAtlas) assetManager.get("GameOverScreenButtons/GameOverButtons.atlas")).findRegion("game_over"));
        Menu = new Button(buttonStyle2);
        Restart = new Button(buttonStyle);

        GameOver.setBounds(78, 235, 640, 384);
        Menu.setBounds(296, 73, 208, 44);
        Restart.setBounds(296, 13, 208, 44);

        stage.addActor(GameOver);
        stage.addActor(Menu);
        stage.addActor(Restart);

        Menu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game, batch, assetManager, blurProcessor));
            }
        });

        Restart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getFloat("Health") > 0) {
                    game.setScreen(new GameScreen(game, batch, assetManager, blurProcessor, false));
                } else {
                    game.setScreen(new GameScreen(game, batch, assetManager, blurProcessor, true));
                }
                GameScreen.is_paused = false;
            }
        });

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            game.setScreen(new MenuScreen(game, batch, assetManager, blurProcessor));
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (enableShader) {
            blurProcessor.capture();
        }

        stage.draw();
        stage.act(delta);
        batch.begin();

        font_main.getData().setScale(0.7f);
        font_main.setColor(Color.valueOf("#ff1100"));
        font_main.draw(batch, "Score: " + Score, 305, 405, 200, 1, false);
        font_main.setColor(Color.valueOf("#ff2200"));
        font_main.draw(batch, "High Score: " + highScore, 305, 355, 200, 1, false);
        font_main.setColor(Color.valueOf("#ff3300"));
        font_main.draw(batch, "Bullets Shot: " + bulletsShot, 305, 305, 200, 1, false);
        font_main.setColor(Color.valueOf("#ff5500"));
        font_main.draw(batch, "Enemies Killed: " + enemiesKilled, 305, 210, 200, 1, false);
        font_main.setColor(Color.valueOf("#ff4400"));
        font_main.draw(batch, "Cells Collected: " + moneyEarned, 305, 255, 200, 1, false);
        font_main.setColor(Color.valueOf("#ff6600"));
        font_main.draw(batch, "", 305, 165, 200, 1, false);
        font_main.setColor(new Color().fromHsv(Math.abs(120 - difficulty * 20), 1.5f, 1).add(0, 0, 0, 1));
        font_main.getData().setScale(0.6f);
        font_main.draw(batch, "Difficulty: " + difficulty + "X", 51, 455, 200, 1, false);
        font_main.getData().setScale(0.7f);


        if (isNewHighScore) {
            font_main.getData().setScale(0.65f);
            font_main.setColor(Color.valueOf("#33ffaa"));
            font_main.draw(batch, "New High Score !", 555, 455, 200, 1, false);
        }

        batch.end();

        if (enableShader) {
            blurProcessor.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        updateCamera(camera, viewport, width, height);
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

    @Override
    public void dispose() {

    }
}
