package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Bonus;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.UraniumCell;
import com.deo.flapd.model.enemies.BasicEnemy;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.model.enemies.Boss_battleShip;
import com.deo.flapd.model.enemies.Kamikadze;
import com.deo.flapd.model.enemies.ShotgunEnemy;
import com.deo.flapd.model.enemies.SniperEnemy;
import com.deo.flapd.utils.DUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GameScreen implements Screen{

    private Texture bg1;
    private Texture bg2;
    private Texture bg3;
    private Texture ship_texture;
    private Texture shield_texture;

    private Bullet bullet;
    private BasicEnemy enemy;
    private SniperEnemy enemy_sniper;
    private ShotgunEnemy enemy_shotgun;
    private Meteorite meteorite;
    private Kamikadze kamikadze;
    private Boss_battleShip boss_battleShip;
    private UraniumCell uraniumCell;
    private Checkpoint checkpoint;

    private SpriteBatch batch;

    private SpaceShip ship;
    private GameUi gameUi;
    private GameLogic gameLogic;

    private OrthographicCamera camera;
    private Viewport viewport;

    public static boolean is_paused;

    private int movement;

    private Game game;

    private AssetManager assetManager;

    private Preferences prefs;

    private Music music;
    private boolean Music;
    private float millis, millis2, millis3, musicVolume;

    private Bonus bonus;

    private Executor executor;

    private ShaderProgram shaderProgram;

    private boolean shaders;

    GameScreen(final Game game, SpriteBatch batch, AssetManager assetManager, boolean newGame){

        prefs = Gdx.app.getPreferences("Preferences");

        this.game = game;

        this.batch = batch;

        this.assetManager = assetManager;

        shaders = prefs.getBoolean("shaders");

        executor = Executors.newSingleThreadExecutor();

        camera = new OrthographicCamera(800, 480);
        viewport = new FitViewport(800,480, camera);

        bg1 = assetManager.get("bg_layer1.png");
        bg2 = assetManager.get("bg_layer2.png");
        bg3 = assetManager.get("bg_layer3.png");

        bg1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bg2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bg3.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        ship_texture = assetManager.get("ship.png");
        shield_texture = assetManager.get("ColdShield.png");

        ship = new SpaceShip(ship_texture, shield_texture, 0, 204, 76.8f, 57.6f, newGame);

        uraniumCell = new UraniumCell(assetManager, 96, 96, prefs.getFloat("ui"));

        boss_battleShip =  new Boss_battleShip(assetManager, 1100, 150, ship.getBounds(), uraniumCell);

        bonus = new Bonus(assetManager, 50, 50, ship.getBounds(), boss_battleShip);

        boss_battleShip.postConstruct(bonus);

        gameUi = new GameUi(game, batch, assetManager, ship, newGame);

        switch (prefs.getInteger("current_cannon")){
            case(1):bullet = new Bullet((Texture)assetManager.get("bu1.png"),0.4f*MathUtils.clamp((1.5f-prefs.getInteger("cannon1upgradeLevel")*0.1f), 0.7f, 1.5f), 1, ship.getBounds(), newGame);
                break;
            case(2):bullet = new Bullet((Texture)assetManager.get("bu2.png"),0.4f*MathUtils.clamp((1.2f-prefs.getInteger("cannon2upgradeLevel")*0.1f), 0.7f, 1.5f), 0.8f, ship.getBounds(), newGame);
                break;
            case(3):bullet = new Bullet((Texture)assetManager.get("bu3.png"),0.4f*MathUtils.clamp((0.8f-prefs.getInteger("cannon3upgradeLevel")*0.1f), 0.7f, 1.5f), 1.3f, ship.getBounds(), newGame);
                break;
        }

        enemy = new BasicEnemy(uraniumCell, assetManager,104, 74, 32, 32, 0, 0, 0.4f, 100, 10);
        enemy_sniper = new SniperEnemy(uraniumCell, assetManager,336, 188, 100, 12, 20, 14, 0, 270, 94, bonus);
        enemy_shotgun = new ShotgunEnemy(uraniumCell, assetManager,388, 144, 16, 16, 3, 17, 2.4f, 371, 80, bonus, prefs.getBoolean("easterEgg"));
        kamikadze = new Kamikadze(uraniumCell, assetManager,348, 192, ship.getBounds(), bonus);

        meteorite = new Meteorite(uraniumCell, assetManager, bonus, newGame);

        checkpoint = new Checkpoint(assetManager, ship.getBounds());

        gameLogic = new GameLogic(ship.getBounds(), newGame, game, bullet, enemy, enemy_shotgun, enemy_sniper, meteorite, kamikadze, boss_battleShip, checkpoint);

        musicVolume = prefs.getFloat("musicVolume");

        if(musicVolume > 0) {
            Music = true;
        }

        music = Gdx.audio.newMusic(Gdx.files.internal("music/main2.ogg"));

        ShaderProgram.pedantic = false;
        shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/glow.vertex"),Gdx.files.internal("shaders/glow.fragment"));
        DUtils.log("\n shader compiler log: " + shaderProgram.getLog() + " ==end of log== " + "\n");
    }

    @Override
    public void show() {
        millis2 = 101;
        music.setVolume(0);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.begin();
            batch.draw(bg1, 0, 0, movement / 4, -240, 800, 720);
            batch.draw(bg2, 0, 0, movement / 2, -240, 800, 720);
            batch.draw(bg3, 0, 0, movement, -240, 800, 720);

            ship.draw(batch, is_paused);

            if(!is_paused && !ship.isExploded()) {
                gameLogic.handleInput(gameUi.getDeltaX(), gameUi.getDeltaY(), gameUi.is_firing, gameUi.is_firing_secondary);
                executor.execute(new Runnable(){
                    @Override
                    public void run(){
                        try {
                        gameLogic.detectCollisions(is_paused);
                        }catch (Exception e){
                                StringWriter sw = new StringWriter();
                                e.printStackTrace(new PrintWriter(sw));
                                String fullStackTrace = sw.toString();
                                DUtils.log("\n"+fullStackTrace + "\n");
                        }
                    }
                });
            }

            enemy.draw(batch, is_paused);
            enemy_sniper.draw(batch, is_paused);
            enemy_shotgun.draw(batch, is_paused);
            kamikadze.draw(batch, is_paused);
            boss_battleShip.draw(batch, is_paused);

            bullet.draw(batch, is_paused);
            meteorite.draw(batch, is_paused);
            checkpoint.draw(batch, is_paused);

            bonus.draw(batch, is_paused);

            if(shaders) {
                shaderProgram.begin();
                shaderProgram.setUniformf("td_alpha", GameUi.Shield / 100);
                shaderProgram.setUniformf("a_alpha", millis3);
                millis3 += delta * (1 - GameUi.Shield / 100 + 1);
                if (millis3 > 1) {
                    millis3 = 0;
                }
                shaderProgram.end();
                batch.setShader(shaderProgram);
                ship.DrawShield(is_paused);
                batch.setShader(null);
            }else{
                ship.DrawShield(is_paused);
            }

            uraniumCell.draw(batch, is_paused);

            gameUi.draw(is_paused);

            if(!is_paused){
                movement = (int)(movement + (200 * delta));
                if(movement> 2880){
                    movement = 0;
                }
            }

            batch.end();

        if(Music) {

            if (millis > 10) {
                if (music.getPosition() > 65 && music.getPosition() < 69 && music.getVolume() > 0) {
                    music.setVolume(music.getVolume() - 0.05f);
                }
                if (music.getPosition() > 0 && music.getPosition() < 4 && music.getVolume() < musicVolume) {
                    music.setVolume(music.getVolume() + 0.05f);
                }
                millis = 0;
            }

            millis = millis + 50 * Gdx.graphics.getDeltaTime();
            millis2 = millis2 + 0.5f * Gdx.graphics.getDeltaTime();

            if(millis2 > 100){
                music.play();
                millis2 = 0;
            }

        }
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height);
        gameUi.resize(width, height);
    }

    @Override
    public void pause() {
        is_paused = true;
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        music.stop();
        game.getScreen().dispose();
    }

    @Override
    public void dispose() {

    gameUi.dispose();
    enemy.dispose();
    enemy_shotgun.dispose();
    enemy_sniper.dispose();
    meteorite.dispose();
    bullet.dispose();

    ship.dispose();

    assetManager.unload("bg_layer1.png");
    assetManager.unload("bg_layer2.png");
    assetManager.unload("bg_layer3.png");
    assetManager.unload("ship.png");
    assetManager.unload("ColdShield.png");
    assetManager.unload("pew3.png");
    assetManager.unload("pew.png");
    assetManager.unload("trainingbot.png");
    assetManager.unload("enemy_shotgun.png");
    assetManager.unload("enemy_sniper.png");
    assetManager.unload("pew2.png");
    assetManager.unload("Meteo.png");
    assetManager.unload("atomic_bomb.png");

    assetManager.unload("bonus_bullets.png");
    assetManager.unload("bonus_health.png");
    assetManager.unload("bonus_part.png");
    assetManager.unload("bonus_shield.png");
    assetManager.unload("bonus_boss.png");

    assetManager.unload("boss_ship/boss.png");
    assetManager.unload("boss_ship/boss_dead.png");
    assetManager.unload("boss_ship/bullet_blue.png");
    assetManager.unload("boss_ship/bullet_red.png");
    assetManager.unload("boss_ship/bullet_red_thick.png");
    assetManager.unload("boss_ship/cannon1.png");
    assetManager.unload("boss_ship/cannon2.png");
    assetManager.unload("boss_ship/upperCannon_part1.png");
    assetManager.unload("boss_ship/upperCannon_part2.png");
    assetManager.unload("boss_ship/bigCannon.png");

    assetManager.unload("uraniumCell.png");

    assetManager.unload("checkpoint.png");
    assetManager.unload("checkpoint_green.png");

    assetManager.unload("bu1.png");
    assetManager.unload("bu2.png");
    assetManager.unload("bu3.png");

    assetManager.unload("cat.png");

    boss_battleShip.dispose();

    music.dispose();

    bonus.dispose();
    uraniumCell.dispose();
    }
}
