package com.deo.flapd.view.screens;

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
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.MusicManager;
import com.deo.flapd.utils.postprocessing.PostProcessor;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.putInteger;
import static com.deo.flapd.utils.DUtils.updateCamera;

public class GameOverScreen implements Screen {
    
    private final int score;
    private final int enemiesKilled;
    private final int bulletsShot;
    private int highScore;
    private final int moneyEarned;
    
    private final float difficulty;
    
    private final Stage stage;
    
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final PostProcessor blurProcessor;
    
    private final BitmapFont font_main;
    
    private final Game game;
    
    private final boolean isNewHighScore;
    private final boolean enableShader;
    
    private final AssetManager assetManager;
    
    private final MusicManager musicManager;
    
    public GameOverScreen(final Game game, final SpriteBatch batch, final AssetManager assetManager, final PostProcessor blurProcessor, Player player, final MusicManager musicManager) {
        
        this.game = game;
        this.musicManager = musicManager;
        
        this.batch = batch;
        this.blurProcessor = blurProcessor;
        this.assetManager = assetManager;
        
        enableShader = getBoolean("bloom");
        score = GameLogic.score;
        highScore = getInteger("highScore");
        difficulty = getFloat("difficulty");
        
        if (score > highScore) {
            putInteger("highScore", score);
            highScore = score;
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
        
        Skin buttonSkin = new Skin();
        buttonSkin.addRegions(assetManager.get("GameOverScreenButtons/GameOverButtons.atlas"));
        
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.down = buttonSkin.getDrawable("restart_e");
        buttonStyle.up = buttonSkin.getDrawable("restart_d");
        buttonStyle.over = buttonSkin.getDrawable("restart_o");
        
        Button.ButtonStyle buttonStyle2 = new Button.ButtonStyle();
        buttonStyle2.down = buttonSkin.getDrawable("menu_e");
        buttonStyle2.up = buttonSkin.getDrawable("menu_d");
        buttonStyle2.over = buttonSkin.getDrawable("menu_o");
        
        Image gameOver = new Image(((TextureAtlas) assetManager.get("GameOverScreenButtons/GameOverButtons.atlas")).findRegion("game_over"));
        Button menu = new Button(buttonStyle2);
        Button restart = new Button(buttonStyle);
        
        gameOver.setBounds(78, 235, 640, 384);
        menu.setBounds(296, 73, 208, 44);
        restart.setBounds(296, 13, 208, 44);
        
        stage.addActor(gameOver);
        stage.addActor(menu);
        stage.addActor(restart);
        
        menu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game, batch, assetManager, blurProcessor, musicManager));
            }
        });
        
        restart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, batch, assetManager, blurProcessor, musicManager, true));
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
            game.setScreen(new MenuScreen(game, batch, assetManager, blurProcessor, musicManager));
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
        font_main.draw(batch, "Score: " + score, 305, 405, 200, 1, false);
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