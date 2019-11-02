package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.model.enemies.BasicEnemy;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorite;

public class GameOverScreen implements Screen {

    private int Score, enemiesKilled, bulletsShot, meteoritesDestroyed, highScore, enemiesSpawned;

    private float difficulty;

    private Stage stage;

    private Image GameOver;
    private Image Menu;
    private Image Restart;
    private Image Menu_disabled;
    private Image Restart_disabled;

    private OrthographicCamera camera;
    private Viewport viewport;

    private SpriteBatch batch;

    private BitmapFont font_main;

    private AssetManager assetManager;

    private Game game;

    private Preferences prefs;

    private Boolean isNewHighScore;

    public GameOverScreen(final Game game, final SpriteBatch batch, final AssetManager assetManager){

        this.batch = batch;

        this.assetManager = assetManager;

        this.game = game;

        prefs = Gdx.app.getPreferences("Preferences");

        Score = GameUi.Score;

        highScore = prefs.getInteger("highScore");

        difficulty = prefs.getFloat("difficulty");

        if(Score > highScore){
            prefs.putInteger("highScore", Score);
            prefs.flush();
            highScore = Score;
            isNewHighScore = true;
        }else{
            isNewHighScore = false;
        }

        enemiesKilled = GameUi.enemiesKilled;

        bulletsShot = Bullet.bulletsShot;

        meteoritesDestroyed = Meteorite.meteoritesDestroyed;

        enemiesSpawned = GameUi.enemiesSpawned;

        camera = new OrthographicCamera(800, 480);
        viewport = new FitViewport(800,480, camera);

        assetManager.load("GameOverScreenButtons/game_over.png", Texture.class);
        assetManager.load("GameOverScreenButtons/menu_e.png", Texture.class);
        assetManager.load("GameOverScreenButtons/menu_d.png", Texture.class);
        assetManager.load("GameOverScreenButtons/restart_e.png", Texture.class);
        assetManager.load("GameOverScreenButtons/restart_d.png", Texture.class);

        while (!assetManager.isFinished()) {
            assetManager.update();
        }

        font_main = assetManager.get("fonts/font2.fnt");

        stage = new Stage(viewport, batch);

        GameOver = new Image((Texture)assetManager.get("GameOverScreenButtons/game_over.png"));
        Menu = new Image((Texture)assetManager.get("GameOverScreenButtons/menu_e.png"));
        Menu_disabled = new Image((Texture)assetManager.get("GameOverScreenButtons/menu_d.png"));
        Restart = new Image((Texture)assetManager.get("GameOverScreenButtons/restart_e.png"));
        Restart_disabled = new Image((Texture)assetManager.get("GameOverScreenButtons/restart_d.png"));

        GameOver.setBounds(78, 235, 640, 384);
        Menu.setBounds(296, 98, 208, 44);
        Menu_disabled.setBounds(296, 98, 208, 44);
        Restart_disabled.setBounds(296, 38, 208, 44);
        Restart.setBounds(296, 38, 208, 44);

        stage.addActor(GameOver);
        stage.addActor(Menu);
        stage.addActor(Menu_disabled);
        stage.addActor(Restart);
        stage.addActor(Restart_disabled);

        Menu.setVisible(false);
        Restart.setVisible(false);

        Menu_disabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Menu.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Menu.setVisible(false);
                game.setScreen(new MenuScreen(game, batch, assetManager));
            }
        });

        Restart_disabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Restart.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Restart.setVisible(false);
                game.setScreen(new GameScreen(game, batch, assetManager, true));
                GameScreen.is_paused = false;
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
        font_main.getData().setScale(0.5f);
        font_main.setColor(Color.valueOf("#ff5500"));
        font_main.draw(batch, "Enemies Killed: " + enemiesKilled + " out of " + enemiesSpawned + " Enemies Spawned",  305, 195, 200, 1, false);
        font_main.getData().setScale(0.7f);
        font_main.setColor(Color.valueOf("#ff4400"));
        font_main.draw(batch, "Meteorites Destroyed: " + meteoritesDestroyed, 305, 255, 200, 1, false);
        font_main.setColor(new Color().fromHsv(Math.abs(120-difficulty*20), 1.5f, 1).add(0,0,0,1));
        font_main.getData().setScale(0.6f);
        font_main.draw(batch, "Difficulty: " + difficulty+"X", 51, 445, 200, 1, false);
        font_main.getData().setScale(0.7f);


        if(isNewHighScore) {
            font_main.getData().setScale(0.65f);
            font_main.setColor(Color.valueOf("#33ffaa"));
            font_main.draw(batch, "New High Score !", 555, 457, 200, 1, false);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        game.getScreen().dispose();
    }

    @Override
    public void dispose() {

        assetManager.unload("GameOverScreenButtons/game_over.png");
        assetManager.unload("GameOverScreenButtons/menu_e.png");
        assetManager.unload("GameOverScreenButtons/menu_d.png");
        assetManager.unload("GameOverScreenButtons/restart_e.png");
        assetManager.unload("GameOverScreenButtons/restart_d.png");

    }
}
