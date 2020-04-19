package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.SpaceShip;
import com.deo.flapd.utils.postprocessing.PostProcessor;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putInteger;
import static com.deo.flapd.utils.DUtils.updateCamera;
import static com.deo.flapd.view.GameScreen.is_paused;


public class GameUi {

    private Viewport viewport;
    private Stage stage, PauseStage;
    private InputMultiplexer multiplexer;
    private OrthographicCamera cam;

    private Touchpad touchpad;
    private Skin touchpad_skin;
    private Touchpad.TouchpadStyle touchpadStyle;
    private float deltaX;
    private float deltaY;

    private ProgressBar health;
    private ProgressBar shield;
    private ProgressBar charge;
    private ProgressBar.ProgressBarStyle healthBarStyle;
    private ProgressBar.ProgressBarStyle shieldBarStyle;
    private ProgressBar.ProgressBarStyle chargeBarStyle;

    private Table table;

    private Image fireButton;
    private Image weaponChangeButton;
    private Image pause;
    private Image pause2;
    private Image levelScore;
    private Image money_display;
    boolean is_firing;
    boolean is_firing_secondary;

    private TextButton exit_button;
    private TextButton continue_button;
    private TextButton restart_button;
    private Table Pause;
    private Skin pause_skin;
    private TextButton.TextButtonStyle pauseButtonStyle;

    private BitmapFont font_numbers, font_white, font_main, font_buttons;

    private SpriteBatch batch;

    private Texture PauseBg, PauseBg2, pauseButton_e, pauseButton_o, pauseButton_d;

    private float uiScale;
    private float difficulty;

    private boolean showFps;

    private Game game;

    private AssetManager assetManager;

    private Texture knob;
    private Texture touch_bg;

    private Polygon bounds;

    private ParticleEffect explosion;

    private boolean exploded, transparency;

    private SpaceShip ship;

    private PostProcessor blurProcessor;

    public GameUi(final Game game, final SpriteBatch batch, final AssetManager assetManager, final PostProcessor blurProcessor, SpaceShip Ship) {

        this.game = game;

        this.assetManager = assetManager;

        this.blurProcessor = blurProcessor;

        bounds = Ship.getBounds();

        ship = Ship;

        uiScale = getFloat("ui");

        showFps = getBoolean("showFps");

        difficulty = getFloat("difficulty");

        transparency = getBoolean("transparency");

        this.batch = batch;

        cam = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(cam);

        knob = new Texture("knob.png");
        touch_bg = new Texture("bg_stick.png");

        PauseBg = new Texture("grey.png");
        PauseBg2 = new Texture("pauseBg.png");
        pauseButton_e = new Texture("buttonPauseBlank_enabled.png");
        pauseButton_o = new Texture("buttonPauseBlank_over.png");
        pauseButton_d = new Texture("buttonPauseBlank_disabled.png");

        fireButton = new Image((Texture) assetManager.get("firebutton.png"));
        weaponChangeButton = new Image((Texture) assetManager.get("weaponbutton.png"));
        pause = new Image((Texture) assetManager.get("pause.png"));
        levelScore = new Image((Texture) assetManager.get("level score indicator.png"));
        money_display = new Image((Texture) assetManager.get("money_display.png"));
        pause2 = new Image((Texture) assetManager.get("health indicator.png"));

        pause2.setBounds(658 - 142 * (uiScale - 1), 398 - 82 * (uiScale - 1), 142 * uiScale, 82 * uiScale);
        pause.setBounds(770 - 32 * (uiScale - 1), 450 - 32 * (uiScale - 1), 29 * uiScale, 29 * uiScale);
        levelScore.setBounds(516 - 284 * (uiScale - 1), 398 - 82 * (uiScale - 1), 142 * uiScale, 82 * uiScale);
        money_display.setBounds(374 - 426 * (uiScale - 1), 428 - 52 * (uiScale - 1), 142 * uiScale, 52 * uiScale);

        font_numbers = assetManager.get("fonts/font.fnt");
        font_white = assetManager.get("fonts/font_white.fnt");
        font_main = assetManager.get("fonts/font2(old).fnt");
        font_buttons = assetManager.get("fonts/font2.fnt");

        stage = new Stage(viewport, batch);
        PauseStage = new Stage(viewport, batch);
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(PauseStage);
        Gdx.input.setInputProcessor(multiplexer);

        pause_skin = new Skin();
        pause_skin.add("pauseBg", PauseBg2);
        pause_skin.add("button_e", pauseButton_e);
        pause_skin.add("button_o", pauseButton_o);
        pause_skin.add("button_d", pauseButton_d);

        table = new Table();
        table.bottom();
        table.add(weaponChangeButton).padRight(5);
        table.add(fireButton);
        table.row();
        table.setBounds(511 - 283 * (uiScale - 1.1f), 5, 283.5f * (uiScale - 0.1f), 69.75f * (uiScale - 0.1f));

        pauseButtonStyle = new TextButton.TextButtonStyle();
        pauseButtonStyle.font = font_buttons;
        pauseButtonStyle.up = pause_skin.getDrawable("button_d");
        pauseButtonStyle.fontColor = Color.valueOf("#FF8000");
        pauseButtonStyle.over = pause_skin.getDrawable("button_o");
        pauseButtonStyle.overFontColor = Color.valueOf("#FF9505");
        pauseButtonStyle.down = pause_skin.getDrawable("button_e");
        pauseButtonStyle.downFontColor = Color.valueOf("#FFAF05");

        exit_button = new TextButton("Exit", pauseButtonStyle);
        continue_button = new TextButton("Continue", pauseButtonStyle);
        restart_button = new TextButton("Restart", pauseButtonStyle);
        continue_button.setScale(uiScale);
        restart_button.setScale(uiScale);
        exit_button.setScale(uiScale);
        exit_button.setTransform(true);
        restart_button.setTransform(true);
        continue_button.setTransform(true);
        continue_button.getLabel().setFontScale(0.5f);
        restart_button.getLabel().setFontScale(0.5f);
        exit_button.getLabel().setFontScale(0.5f);

        Pause = new Table();
        Pause.setBounds(400 - 600 * uiScale / 2, 240 - 360 * uiScale / 2, 600 * uiScale, 360 * uiScale);
        Pause.add(continue_button).padRight(340.5f * (uiScale - 1)).padTop(50 * (uiScale - 1));
        Pause.row();
        Pause.add(restart_button).padTop(15 * uiScale + 43 * (uiScale - 1)).padBottom(15 * uiScale + 43 * (uiScale - 1)).padRight(340.5f * (uiScale - 1));
        Pause.row();
        Pause.add(exit_button).padRight(341 * (uiScale - 1));

        Pause.setBackground(pause_skin.getDrawable("pauseBg"));

        touchpad_skin = new Skin();
        touchpad_skin.add("touchBg", touch_bg);
        touchpad_skin.add("touchKnob", knob);

        touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = touchpad_skin.getDrawable("touchBg");
        touchpadStyle.knob = touchpad_skin.getDrawable("touchKnob");
        touchpadStyle.knob.setMinWidth(22 * uiScale);
        touchpadStyle.knob.setMinHeight(22 * uiScale);

        touchpad = new Touchpad(0, touchpadStyle);
        touchpad.setResetOnTouchUp(true);

        if (transparency) {
            touchpad.setColor(1, 1, 1, 0.7f);
        }

        touchpad.setBounds(10, 10, 110 * uiScale, 110 * uiScale);

        healthBarStyle = new ProgressBar.ProgressBarStyle();
        shieldBarStyle = new ProgressBar.ProgressBarStyle();
        chargeBarStyle = new ProgressBar.ProgressBarStyle();

        Pixmap pixmap = new Pixmap(0, (int) (12 * uiScale), Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        TextureRegionDrawable BarBackgroundBlank = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        Pixmap pixmap3 = new Pixmap(100, (int) (12 * uiScale), Pixmap.Format.RGBA8888);
        pixmap3.setColor(Color.GREEN);
        pixmap3.fill();
        TextureRegionDrawable BarForegroundGreen = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap3)));
        pixmap3.dispose();

        Pixmap pixmap5 = new Pixmap(100, (int) (12 * uiScale), Pixmap.Format.RGBA8888);
        pixmap5.setColor(Color.CYAN);
        pixmap5.fill();
        TextureRegionDrawable BarForegroundCyan = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap5)));
        pixmap5.dispose();

        Pixmap pixmap6 = new Pixmap(100, (int) (12 * uiScale), Pixmap.Format.RGBA8888);
        pixmap6.setColor(Color.YELLOW);
        pixmap6.fill();
        TextureRegionDrawable BarForegroundYellow = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap6)));
        pixmap6.dispose();

        healthBarStyle.knob = BarBackgroundBlank;
        healthBarStyle.knobBefore = BarForegroundGreen;

        shieldBarStyle.knob = BarBackgroundBlank;
        shieldBarStyle.knobBefore = BarForegroundCyan;

        chargeBarStyle.knob = BarBackgroundBlank;
        chargeBarStyle.knobBefore = BarForegroundYellow;

        health = new ProgressBar(0, 100*SpaceShip.healthMultiplier, 0.01f, false, healthBarStyle);
        shield = new ProgressBar(0, SpaceShip.shieldStrength, 0.01f, false, shieldBarStyle);
        charge = new ProgressBar(0, SpaceShip.chargeCapacity, 0.01f, false, chargeBarStyle);

        health.setBounds(666 - 134 * (uiScale - 1), 413 - 62 * (uiScale - 1), 124 * uiScale, 10);
        shield.setBounds(666 - 134 * (uiScale - 1), 435 - 40 * (uiScale - 1), 72 * uiScale, 10);
        charge.setBounds(666 - 134 * (uiScale - 1), 457 - 18 * (uiScale - 1), 56 * uiScale, 10);

        health.setAnimateDuration(0.25f);
        shield.setAnimateDuration(0.25f);
        charge.setAnimateDuration(0.25f);

        if (transparency) {
            health.setColor(1, 1, 1, 0.5f);
            shield.setColor(1, 1, 1, 0.5f);
            charge.setColor(1, 1, 1, 0.5f);
            pause.setColor(1, 1, 1, 0.5f);
            pause2.setColor(1, 1, 1, 0.5f);
            levelScore.setColor(1, 1, 1, 0.5f);
            money_display.setColor(1, 1, 1, 0.5f);
            font_numbers.setColor(0, 1, 1, 0.5f);
        } else {
            font_numbers.setColor(0, 1, 1, 1);
        }

        stage.addActor(pause2);
        stage.addActor(pause);
        stage.addActor(levelScore);
        stage.addActor(money_display);
        stage.addActor(touchpad);
        stage.addActor(shield);
        stage.addActor(health);
        stage.addActor(charge);
        stage.addActor(table);

        PauseStage.addActor(Pause);

        explosion = new ParticleEffect();
        explosion.load(Gdx.files.internal("particles/" + new JsonReader().parse(Gdx.files.internal("shop/tree.json")).get(getString("currentCore")).get("usesEffect").asString() + ".p"), Gdx.files.internal("particles"));

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

        weaponChangeButton.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                is_firing_secondary = true;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                is_firing_secondary = false;
            }
        });

        pause.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                is_paused = true;
                return true;
            }
        });

        continue_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (is_paused) {
                    is_paused = false;
                }
            }
        });

        exit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (is_paused) {
                    game.setScreen(new MenuScreen(game, batch, assetManager, blurProcessor));
                    is_paused = false;
                }
            }
        });

        restart_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (is_paused) {
                    game.setScreen(new GameScreen(game, batch, assetManager, blurProcessor, true));
                    is_paused = false;
                }
            }
        });

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

    }

    public void draw() {

        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            game.pause();
        }

        batch.end();
        stage.draw();
        stage.act(delta);
        batch.begin();
        font_numbers.getData().setScale(0.3f * uiScale);
        font_numbers.draw(batch, "" + GameLogic.Score, 537 - 263 * (uiScale - 1), 467 - 12 * (uiScale - 1), 100 * uiScale, 1, false);
        font_numbers.draw(batch, "" + GameLogic.money, (537 - 122) - (263 + 122) * (uiScale - 1), 467 - 12 * (uiScale - 1), 100 * uiScale, 1, false);
        font_main.getData().setScale(0.27f * uiScale);
        font_main.draw(batch, "Difficulty: " + difficulty + "X", 544 - 263 * (uiScale - 1), 433 - 45 * (uiScale - 1), 100 * uiScale, 1, false);

        if (showFps) {
            font_main.setColor(Color.WHITE);
            font_main.getData().setScale(0.45f + 0.225f * (uiScale - 1));
            font_main.draw(batch, "Fps: " + String.format("%.0f", 1 / delta), 3, 475);
        }

        if (is_paused) {
            batch.draw(PauseBg, 0, 0, 800, 480);
        }

        batch.end();

        if (is_paused) {
            PauseStage.draw();
            PauseStage.act(delta);
        }

        batch.begin();
        health.setValue(SpaceShip.Health);
        shield.setValue(SpaceShip.Shield);
        charge.setValue(SpaceShip.Charge);

        if (SpaceShip.Health <= 0 && !exploded) {
            explosion.setPosition(bounds.getX() + 25.6f, bounds.getY() + 35.2f);
            explosion.start();
            exploded = true;
            SpaceShip.Health = -1000;
            SpaceShip.Shield = -1000;
            SpaceShip.Charge = -1000;
            ship.explode();
        }

        if (transparency) {
            font_main.setColor(0, 0, 0, 0.7f);
        } else {
            font_main.setColor(0, 0, 0, 1);
        }
    }

    void drawExplosion(float delta) {
        if (exploded) {
            explosion.draw(batch, delta);
            if (explosion.isComplete()) {
                game.setScreen(new GameOverScreen(game, batch, assetManager, blurProcessor));
            }
        }
    }

    void resize(int width, int height) {
        updateCamera(cam, viewport, width, height);
    }

    public void dispose() {

        stage.dispose();
        PauseStage.dispose();
        font_main.dispose();
        font_numbers.dispose();
        font_white.dispose();
        pause_skin.dispose();
        touchpad_skin.dispose();

        knob.dispose();
        touch_bg.dispose();
        PauseBg.dispose();
        PauseBg2.dispose();
        pauseButton_e.dispose();
        pauseButton_d.dispose();
        pauseButton_o.dispose();

        explosion.dispose();

        putInteger("money", GameLogic.money);
    }

    float getDeltaX() {
        return deltaX;
    }

    float getDeltaY() {
        return deltaY;
    }
}
