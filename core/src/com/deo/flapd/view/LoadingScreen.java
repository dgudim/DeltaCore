package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LoadingScreen implements Screen {

    private AssetManager assetManager;
    private SpriteBatch batch;
    private BitmapFont main;
    private Game game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private int screen_type;
    private boolean newGame;

    public LoadingScreen(Game game, SpriteBatch batch, AssetManager assetManager, int screen_type, boolean newGame){

        this.batch = batch;

        main = new BitmapFont(Gdx.files.internal("fonts/font2.fnt"), false);

        this.assetManager = assetManager;

        this.game = game;

        switch (screen_type){
           case(1):
               assetManager.load("bg_layer1.png", Texture.class);
               assetManager.load("bg_layer2.png", Texture.class);
               assetManager.load("bg_layer3.png", Texture.class);
               assetManager.load("ship.png", Texture.class);
               assetManager.load("ColdShield.png", Texture.class);
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

               assetManager.load("boss_ship/boss.png", Texture.class);
               assetManager.load("boss_ship/boss_dead.png", Texture.class);
               assetManager.load("boss_ship/bullet_blue.png", Texture.class);
               assetManager.load("boss_ship/bullet_red.png", Texture.class);
               assetManager.load("boss_ship/bullet_red_thick.png", Texture.class);
               assetManager.load("boss_ship/cannon1.png", Texture.class);
               assetManager.load("boss_ship/cannon2.png", Texture.class);
               assetManager.load("boss_ship/upperCannon_part1.png", Texture.class);
               assetManager.load("boss_ship/upperCannon_part2.png", Texture.class);
               assetManager.load("boss_ship/bigCannon.png", Texture.class);

               assetManager.load("uraniumCell.png", Texture.class);

               assetManager.load("firebutton.png", Texture.class);
               assetManager.load("weaponbutton.png", Texture.class);
               assetManager.load("pause.png", Texture.class);
               assetManager.load("level score indicator.png", Texture.class);
               assetManager.load("health indicator.png", Texture.class);
               assetManager.load("money_display.png", Texture.class);
               assetManager.load("exit.png", Texture.class);
               assetManager.load("resume.png", Texture.class);
               assetManager.load("restart.png", Texture.class);
               break;
           case(2):
               assetManager.load("greyishButton.png", Texture.class);
               assetManager.load("menuButtons/info_enabled.png", Texture.class);
               assetManager.load("menuButtons/info_disabled.png", Texture.class);
               assetManager.load("menuButtons/more_enabled.png", Texture.class);
               assetManager.load("menuButtons/more_disabled.png", Texture.class);
               assetManager.load("menuButtons/play_enabled.png", Texture.class);
               assetManager.load("menuButtons/play_disabled.png", Texture.class);
               assetManager.load("menuButtons/settings_enabled.png", Texture.class);
               assetManager.load("menuButtons/settings_disabled.png", Texture.class);
               assetManager.load("menuButtons/online_enabled.png", Texture.class);
               assetManager.load("menuButtons/online_disabled.png", Texture.class);

               assetManager.load("menuButtons/continue_e.png", Texture.class);
               assetManager.load("menuButtons/continue_d.png", Texture.class);
               assetManager.load("menuButtons/newGame_d.png", Texture.class);
               assetManager.load("menuButtons/shop_d.png", Texture.class);
               assetManager.load("menuButtons/newGame_e.png", Texture.class);
               assetManager.load("menuButtons/shop_e.png", Texture.class);

               assetManager.load("menuBg.png", Texture.class);
               assetManager.load("lamp.png", Texture.class);
               assetManager.load("infoBg.png", Texture.class);
               assetManager.load("bg_old.png", Texture.class);
               assetManager.load("ship2.png", Texture.class);

               assetManager.load("checkBox_disabled.png", Texture.class);
               assetManager.load("checkBox_enabled.png", Texture.class);
               assetManager.load("progressBarKnob.png", Texture.class);
               assetManager.load("progressBarBg.png", Texture.class);
               break;
       }
        this.screen_type = screen_type;

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);

        this.newGame = newGame;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta){
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        main.getData().setScale(1);
        main.setColor(Color.CYAN);
        main.draw(batch, "Loading Textures...", 155, 450,480, 1,false);
        main.setColor(new Color().fromHsv(Math.abs((int)(assetManager.getProgress()*100)/1.1f), 1.5f, 1).add(0,0,0,1));
        main.draw(batch, "Loaded: " + (int)(assetManager.getProgress()*100) + "%"  , 155, 390, 480, 1,false);
        main.getData().setScale(0.4f);
        main.setColor(Color.valueOf("00db00"));
        main.draw(batch, assetManager.getDiagnostics() + (int)(assetManager.getProgress()*100) + "%"  , 155, 350, 480, 1,false);
        assetManager.update();
        batch.end();

        if(assetManager.isFinished()){
            switch (screen_type){
                case(1):
                    game.setScreen(new GameScreen(game, batch, assetManager, newGame));
                    break;
                case(2):
                    game.setScreen(new MenuScreen(game, batch, assetManager));
                    break;
            }
        }
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

    public void dispose(){
        main.dispose();
    }

}
