package com.deo.flapd.view;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.model.SpaceShip;


public class GameUi{

    private Viewport viewport;
    private Stage stage, PauseStage;
    InputMultiplexer multiplexer;
    private OrthographicCamera cam;

    private Touchpad touchpad;
    private Skin touchpad_skin;
    private Touchpad.TouchpadStyle touchpadStyle;
    private float deltaX;
    private float deltaY;

    private ProgressBar health;
    private ProgressBar shield;
    private ProgressBar.ProgressBarStyle healthBarStyle;
    private ProgressBar.ProgressBarStyle shieldBarStyle;

    private Table table;

    private Image fireButton;
    private Image weaponChangeButton;
    private Image pause;
    private Image pause2;
    private Image levelScore;
    private boolean is_firing;

    private Image exit_button;
    private Image continue_button;
    private Image restart_button;
    private Table Pause;
    private Skin pause_skin;

    private BitmapFont font_numbers, font_white, font_main;

    private SpriteBatch batch;

    private Texture PauseBg, PauseBg2;

    public static float Shield;
    public static float Health;
    public static int Score;
    public static int enemiesKilled;
    public static int enemiesSpawned;
    private float uiScale;
    private float difficulty;

    private boolean showFps;

    private Preferences prefs;

    private Game game;

    private AssetManager assetManager;

    private Texture knob;
    private Texture touch_bg;

    private LoadingScreen loadingScreen;

    private Polygon bounds;

    private ParticleEffect explosion;

    private boolean exploded, transparency;

    private SpaceShip ship;

    public GameUi(final Game game, final SpriteBatch batch, final AssetManager assetManager, SpaceShip Ship){

        this.game = game;

        this.assetManager = assetManager;

        bounds = Ship.getBounds();

        ship = Ship;

        assetManager.load("firebutton.png", Texture.class);
        assetManager.load("weaponbutton.png", Texture.class);
        assetManager.load("pause.png", Texture.class);
        assetManager.load("level score indicator.png", Texture.class);
        assetManager.load("health indicator.png", Texture.class);
        assetManager.load("exit.png", Texture.class);
        assetManager.load("resume.png", Texture.class);
        assetManager.load("restart.png", Texture.class);

        Shield = 100;
        Health = 100;
        Score = 0;

        enemiesKilled = 0;
        enemiesSpawned = 0;

        prefs = Gdx.app.getPreferences("Preferences");

        uiScale = prefs.getFloat("ui");

        showFps = prefs.getBoolean("showFps");

        difficulty = prefs.getFloat("difficulty");

        transparency = prefs.getBoolean("transparency");

        this.batch = batch;

        cam = new OrthographicCamera();
        viewport = new FitViewport(800, 480, cam);

        loadingScreen = new LoadingScreen(batch);

        while (!assetManager.isFinished()){
            loadingScreen.render(assetManager.getProgress());
            assetManager.update();
        }

        loadingScreen.dispose();

        knob = new Texture("knob.png");
        touch_bg = new Texture("bg_stick.png");

        PauseBg = new Texture("grey.png");

        PauseBg2 = new Texture("pauseBg.png");

        fireButton = new Image((Texture)assetManager.get("firebutton.png"));
        weaponChangeButton = new Image((Texture)assetManager.get("weaponbutton.png"));
        pause = new Image((Texture)assetManager.get("pause.png"));
        levelScore = new Image((Texture)assetManager.get("level score indicator.png"));
        pause2 = new Image((Texture)assetManager.get("health indicator.png"));
        exit_button = new Image((Texture)assetManager.get("exit.png"));
        continue_button = new Image((Texture)assetManager.get("resume.png"));
        restart_button = new Image((Texture)assetManager.get("restart.png"));

        pause2.setBounds(658-142*(uiScale-1), 398-82*(uiScale-1), 142*uiScale, 82*uiScale);
        pause.setBounds(770-32*(uiScale-1), 450-32*(uiScale-1),29*uiScale,29*uiScale);
        levelScore.setBounds(516-284*(uiScale-1), 398-82*(uiScale-1), 142*uiScale,82*uiScale);

        font_numbers = assetManager.get("fonts/font.fnt");
        font_white = assetManager.get("fonts/font_white.fnt");
        font_main = assetManager.get("fonts/font2.fnt");

        stage = new Stage(viewport, batch);
        PauseStage = new Stage(viewport, batch);
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(PauseStage);
        Gdx.input.setInputProcessor(multiplexer);

        continue_button.setScale(uiScale);
        restart_button.setScale(uiScale);
        exit_button.setScale(uiScale);

        Pause = new Table();
        Pause.setBounds(400-600*uiScale/2, 240-360*uiScale/2, 600*uiScale, 360*uiScale);
        Pause.add(continue_button).padRight(340.5f*(uiScale-1)).padTop(50*(uiScale-1));
        Pause.row();
        Pause.add(restart_button).padTop(15*uiScale+43*(uiScale-1)).padBottom(15*uiScale+43*(uiScale-1)).padRight(340.5f*(uiScale-1));
        Pause.row();
        Pause.add(exit_button).padRight(341*(uiScale-1));

        pause_skin = new Skin();
        pause_skin.add("pauseBg",PauseBg2);

        Pause.setBackground(pause_skin.getDrawable("pauseBg"));

        table = new Table();
        table.bottom();
        table.add(weaponChangeButton).padRight(5);
        table.add(fireButton);
        table.row();
        table.setBounds(511-283*(uiScale-1),5, 283.5f*uiScale, 69.75f*uiScale);

        touchpad_skin = new Skin();
        touchpad_skin.add("touchBg",touch_bg);
        touchpad_skin.add("touchKnob",knob);

        touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = touchpad_skin.getDrawable("touchBg");
        touchpadStyle.knob = touchpad_skin.getDrawable("touchKnob");
        touchpadStyle.knob.setMinWidth(32*uiScale);
        touchpadStyle.knob.setMinHeight(32*uiScale);

        touchpad = new Touchpad(0, touchpadStyle);
        touchpad.setResetOnTouchUp(true);

        if(transparency) {
            touchpad.setColor(1, 1, 1, 0.7f);
        }

        touchpad.setBounds(10, 10, 150*uiScale, 150*uiScale);

        healthBarStyle = new ProgressBar.ProgressBarStyle();
        shieldBarStyle = new ProgressBar.ProgressBarStyle();

        Pixmap pixmap = new Pixmap(100, (int)(12*uiScale), Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        TextureRegionDrawable BarBackground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        Pixmap pixmap2 = new Pixmap(0, (int)(12*uiScale), Pixmap.Format.RGBA8888);
        pixmap2.setColor(Color.GREEN);
        pixmap2.fill();
        TextureRegionDrawable BarForeground1 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap2)));
        pixmap2.dispose();

        Pixmap pixmap3 = new Pixmap(100, (int)(12*uiScale), Pixmap.Format.RGBA8888);
        pixmap3.setColor(Color.GREEN);
        pixmap3.fill();
        TextureRegionDrawable BarForeground2 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap3)));
        pixmap3.dispose();

        Pixmap pixmap4 = new Pixmap(0, (int)(12*uiScale), Pixmap.Format.RGBA8888);
        pixmap4.setColor(Color.CYAN);
        pixmap4.fill();
        TextureRegionDrawable BarForeground3 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap4)));
        pixmap4.dispose();

        Pixmap pixmap5 = new Pixmap(100, (int)(12*uiScale), Pixmap.Format.RGBA8888);
        pixmap5.setColor(Color.CYAN);
        pixmap5.fill();
        TextureRegionDrawable BarForeground4 = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap5)));
        pixmap5.dispose();

        healthBarStyle.knob = BarForeground1;
        healthBarStyle.knobBefore = BarForeground2;
        //healthBarStyle.background = BarBackground;

        shieldBarStyle.knob = BarForeground3;
        shieldBarStyle.knobBefore = BarForeground4;
        //shieldBarStyle.background = BarBackground;

        health = new ProgressBar(0, 100, 0.01f, false, healthBarStyle);
        shield = new ProgressBar(0, 100, 0.01f, false, shieldBarStyle);

        health.setBounds(666-134*(uiScale-1),413-62*(uiScale-1),124*uiScale,10);
        shield.setBounds(666-134*(uiScale-1),435-40*(uiScale-1),72*uiScale,10);

        health.setAnimateDuration(0.25f);
        shield.setAnimateDuration(0.25f);

        if(transparency){
            health.setColor(1,1,1, 0.5f);
            shield.setColor(1,1,1, 0.5f);
            pause.setColor(1,1,1, 0.5f);
            pause2.setColor(1,1,1, 0.5f);
            levelScore.setColor(1,1,1, 0.5f);
            font_numbers.setColor(0,1,1,0.5f);
        }
        else{
            font_numbers.setColor(0,1,1,1);
        }

        stage.addActor(pause2);
        stage.addActor(pause);
        stage.addActor(levelScore);
        stage.addActor(touchpad);
        stage.addActor(shield);
        stage.addActor(health);
        stage.addActor(table);

        PauseStage.addActor(Pause);

        explosion = new ParticleEffect();
        explosion.load(Gdx.files.internal("particles/explosion2.p"), Gdx.files.internal("particles"));

        touchpad.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                deltaX = ((Touchpad) actor).getKnobPercentX();
                deltaY = ((Touchpad) actor).getKnobPercentY();
            }
        });

        fireButton.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                is_firing = true;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                is_firing = false;
            }
        });

        pause.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                GameScreen.is_paused = true;
                return true;
            }
        });

        continue_button.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(GameScreen.is_paused) {
                    GameScreen.is_paused = false;
                }
            }
        });

        exit_button.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(GameScreen.is_paused) {
                    game.setScreen(new MenuScreen(game, batch, assetManager));
                    GameScreen.is_paused = false;
                }
            }
        });

        restart_button.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(GameScreen.is_paused) {
                    game.setScreen(new GameScreen(game, batch, assetManager, true));
                    GameScreen.is_paused = false;
                }
            }
        });

        }

    public void draw(boolean is_paused) {
        batch.end();
        stage.draw();
        stage.act(Gdx.graphics.getDeltaTime());
        batch.begin();
        font_numbers.getData().setScale(0.3f*uiScale);
        font_numbers.draw(batch, ""+Score, 537-263*(uiScale-1), 467-12*(uiScale-1), 100*uiScale,1, false);
        font_main.getData().setScale(0.27f*uiScale);
        font_main.draw(batch, "Difficulty: "+difficulty+"X", 544-263*(uiScale-1), 433-45*(uiScale-1), 100*uiScale,1, false);
        font_main.getData().setScale(0.45f*uiScale);

        if(showFps) {
            font_main.setColor(Color.WHITE);
            font_main.draw(batch, "Fps: " + Gdx.graphics.getFramesPerSecond(), 3, 475);
        }

        if(is_paused) {
            batch.draw(PauseBg, 0 ,0 , 800, 480);
        }

        batch.end();

        if (is_paused){
            PauseStage.draw();
            PauseStage.act(Gdx.graphics.getDeltaTime());
        }

        batch.begin();
        health.setValue(Health);
        shield.setValue(Shield);

        if(Health <= 0 && !exploded){
            explosion.setPosition(bounds.getX(), bounds.getY());
            explosion.start();
            exploded = true;
            Health = -1000;
            Shield = -1000;
            ship.explode();
        }

        if(exploded){
            explosion.draw(batch, Gdx.graphics.getDeltaTime());
            if(explosion.isComplete()){
              game.setScreen(new GameOverScreen(game, batch, assetManager));
            }
        }

        if(transparency){
            font_main.setColor(0,0,0,0.7f);
        }else{
            font_main.setColor(0,0,0,1);
        }

        if(Shield<100){
            Shield += 0.02;
        }
    }

    public void resize(int width, int height){
        viewport.update(width, height);
    }

    public void dispose(){

        stage.dispose();
        PauseBg.dispose();
        PauseStage.dispose();
        font_main.dispose();
        font_numbers.dispose();
        font_white.dispose();
        pause_skin.dispose();
        touchpad_skin.dispose();

        assetManager.unload("firebutton.png");
        assetManager.unload("weaponbutton.png");
        assetManager.unload("pause.png");
        assetManager.unload("level score indicator.png");
        assetManager.unload("health indicator.png");
        assetManager.unload("exit.png");
        assetManager.unload("resume.png");
        assetManager.unload("restart.png");

        knob.dispose();
        touch_bg.dispose();
        PauseBg.dispose();
        PauseBg2.dispose();

        explosion.dispose();
    }

    public float getDeltaX(){
        return deltaX;
    }

    public float getDeltaY(){
        return deltaY;
    }

    public boolean is_firing(){
        return is_firing;
    }
}
