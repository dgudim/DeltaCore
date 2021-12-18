package com.deo.flapd.view.screens;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.putInteger;
import static com.deo.flapd.utils.DUtils.updateCamera;

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
import com.deo.flapd.control.GameVariables;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.ScreenManager;
import com.deo.flapd.utils.postprocessing.PostProcessor;

import java.util.Locale;

public class GameOverScreen implements Screen {
    
    private int score;
    private int enemiesKilled;
    private int bulletsShot;
    private int highScore;
    private int moneyEarned;
    
    private float difficulty;
    
    private final Stage stage;
    
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final PostProcessor blurProcessor;
    
    private final BitmapFont font_main;
    
    private boolean isNewHighScore;
    private boolean enableShader;
    
    private final ScreenManager screenManager;
    
    public GameOverScreen(CompositeManager compositeManager) {
        
        screenManager = compositeManager.getScreenManager();
        batch = compositeManager.getBatch();
        blurProcessor = compositeManager.getBlurProcessor();
        AssetManager assetManager = compositeManager.getAssetManager();
        
        reset();
        
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        
        font_main = assetManager.get("fonts/pixel.ttf");
        
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
                screenManager.setCurrentScreenMenuScreen();
            }
        });
        
        restart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screenManager.setCurrentScreenGameScreen(true);
                GameScreen.is_paused = false;
            }
        });
    }
    
    public void reset() {
        enableShader = getBoolean(Keys.enableBloom);
        
        enemiesKilled = GameVariables.enemiesKilled;
        bulletsShot = GameVariables.bulletsShot;
        moneyEarned = GameVariables.moneyEarned;
        score = (int)GameVariables.score;
        
        highScore = getInteger(Keys.highScore);
        difficulty = getFloat(Keys.difficulty);
        
        if (score > highScore) {
            putInteger(Keys.highScore, score);
            highScore = score;
            isNewHighScore = true;
        } else {
            isNewHighScore = false;
        }
        
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override
    public void render(float delta) {
        
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            screenManager.setCurrentScreenMenuScreen();
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
        font_main.draw(batch, "Difficulty: " + String.format(Locale.ROOT, "%.1f", difficulty) + "X", 51, 455, 200, 1, false);
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
    
    }
    
    @Override
    public void dispose() {
    
    }
}
