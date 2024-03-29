package com.deo.flapd.view.overlays;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putInteger;
import static com.deo.flapd.view.screens.GameScreen.globalDeltaMultiplier;
import static com.deo.flapd.view.screens.GameScreen.is_paused;
import static com.deo.flapd.view.screens.GameScreen.playerDeltaMultiplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.deo.flapd.control.GameVariables;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.ScreenManager;
import com.deo.flapd.utils.SoundManager;
import com.deo.flapd.utils.ui.UIComposer;

import java.util.Locale;


public class GameUi {
    
    private final Stage stage;
    private final Stage pauseStage;
    
    private float deltaX;
    private float deltaY;
    
    private final ProgressBar healthProgressBar;
    private final ProgressBar shieldProgressBar;
    private final ProgressBar chargeProgressBar;
    
    public boolean is_firing;
    public boolean is_firing_secondary;
    
    private final BitmapFont font_numbers;
    private final BitmapFont font_main;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final TextureRegion PauseBg;
    
    private final float uiScale;
    private final boolean showFps;
    private final boolean transparency;
    private final Array<Float> fpsSmoothingArray;
    
    boolean chronosModuleEnabled;
    Image timeFreezeButton;
    Image timeFreezeButton_disabled;
    Image timeFreezeButton_active;
    private int maxTimeCharge;
    private float timeCharge;
    private float timeSinceLastActivation;
    private TimeFreezeButtonState timeFreezeButtonState;
    private float reloadTime;
    private float worldSpeedMultiplier;
    private float playerSpeedMultiplier;
    private boolean timeWarpActive = false;
    private float powerConsumption;
    
    private final ScreenManager screenManager;
    private final CompositeManager compositeManager;
    private final SoundManager soundManager;
    
    private final Player player;
    
    public GameUi(ScreenViewport viewport, CompositeManager compositeManager, Player player) {
        
        this.compositeManager = compositeManager;
        screenManager = compositeManager.getScreenManager();
        soundManager = compositeManager.getSoundManager();
        AssetManager assetManager = compositeManager.getAssetManager();
        batch = compositeManager.getBatch();
        shapeRenderer = compositeManager.getShapeRenderer();
        LocaleManager localeManager = compositeManager.getLocaleManager();
        UIComposer uiComposer = compositeManager.getUiComposer();
        this.player = player;
        
        fpsSmoothingArray = new Array<>();
        
        uiScale = getFloat(Keys.uiScale);
        showFps = getBoolean(Keys.showFps);
        transparency = getBoolean(Keys.transparentUi);
        chronosModuleEnabled = getString(Keys.currentModule).equals("part.chronos_module");
        
        final JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        if (chronosModuleEnabled) {
            worldSpeedMultiplier = treeJson.getFloat(1, "part.chronos_module", "parameters", "parameter.world_speed_multiplier");
            playerSpeedMultiplier = treeJson.getFloat(1, "part.chronos_module", "parameters", "parameter.player_speed_multiplier");
            maxTimeCharge = treeJson.getInt(1, "part.chronos_module", "parameters", "parameter.active_time") * 1000;
            timeCharge = maxTimeCharge;
            reloadTime = treeJson.getFloat(1, "part.chronos_module", "parameters", "parameter.reload_time") * 1000;
            powerConsumption = treeJson.getFloat(1, "part.chronos_module", "parameters", "parameter.power_consumption");
            timeFreezeButtonState = TimeFreezeButtonState.AVAILABLE;
        }
        
        TextureAtlas gameUiAtlas = assetManager.get("ui/gameUi.atlas");
        
        Image fireButton = new Image(gameUiAtlas.findRegion("firebutton"));
        Image weaponChangeButton = new Image(gameUiAtlas.findRegion("weaponbutton"));
        timeFreezeButton = new Image(gameUiAtlas.findRegion("timeButton"));
        timeFreezeButton_disabled = new Image(gameUiAtlas.findRegion("timeButton_reload"));
        timeFreezeButton_active = new Image(gameUiAtlas.findRegion("timeButton_active"));
        
        Image pause = new Image(gameUiAtlas.findRegion("pause"));
        Image levelScore = new Image(gameUiAtlas.findRegion("level_score_indicator"));
        Image money_display = new Image(gameUiAtlas.findRegion("money_display"));
        Image stats_indicator_panel = new Image(gameUiAtlas.findRegion("health_indicator"));
        
        Image heartIcon = new Image(gameUiAtlas.findRegion("heart_icon"));
        Image shieldIcon = new Image(gameUiAtlas.findRegion("shield_icon"));
        
        stats_indicator_panel.setBounds(658 - 142 * (uiScale - 1), 398 - 82 * (uiScale - 1), 142 * uiScale, 82 * uiScale);
        pause.setBounds(770 - 32 * (uiScale - 1), 450 - 32 * (uiScale - 1), 29 * uiScale, 29 * uiScale);
        levelScore.setBounds(516 - 284 * (uiScale - 1), 398 - 82 * (uiScale - 1), 142 * uiScale, 82 * uiScale);
        money_display.setBounds(374 - 426 * (uiScale - 1), 428 - 52 * (uiScale - 1), 142 * uiScale, 52 * uiScale);
        
        timeFreezeButton.setBounds(0, 480 - uiScale * 52.5f, 66 * uiScale, 52.5f * uiScale);
        timeFreezeButton_disabled.setBounds(0, 480 - uiScale * 52.5f, 66 * uiScale, 52.5f * uiScale);
        timeFreezeButton_active.setBounds(0, 480 - uiScale * 52.5f, 66 * uiScale, 52.5f * uiScale);
        setTimeFreezeButtonState(TimeFreezeButtonState.AVAILABLE);
        
        heartIcon.setBounds(658 - 142 * (uiScale - 1), 410 - 70 * (uiScale - 1), 18 * uiScale, 18 * uiScale);
        shieldIcon.setBounds(658 - 142 * (uiScale - 1), 432 - 48 * (uiScale - 1), 18 * uiScale, 18 * uiScale);
        
        font_numbers = assetManager.get("fonts/font_numbers.fnt");
        font_main = assetManager.get("fonts/pixel.ttf");
        
        stage = new Stage(viewport, batch);
        pauseStage = new Stage(viewport, batch);
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(pauseStage);
        Gdx.input.setInputProcessor(multiplexer);
        
        Table weaponControls = new Table();
        weaponControls.bottom();
        weaponControls.add(weaponChangeButton).padRight(5);
        weaponControls.add(fireButton);
        weaponControls.row();
        weaponControls.setBounds(511 - 283 * (uiScale - 1.1f), 5, 283.5f * (uiScale - 0.1f), 69.75f * (uiScale - 0.1f));
        
        TextButton exit_button = uiComposer.addTextButton("pauseButton", localeManager.get("pause.exit"), 0.5f);
        TextButton continue_button = uiComposer.addTextButton("pauseButton", localeManager.get("pause.continue"), 0.5f);
        TextButton restart_button = uiComposer.addTextButton("pauseButton", localeManager.get("pause.restart"), 0.5f);
        continue_button.setScale(uiScale);
        restart_button.setScale(uiScale);
        exit_button.setScale(uiScale);
        exit_button.setTransform(true);
        restart_button.setTransform(true);
        continue_button.setTransform(true);
        
        Table pause1 = new Table();
        pause1.setBounds(400 - 600 * uiScale / 2, 240 - 360 * uiScale / 2, 600 * uiScale, 360 * uiScale);
        pause1.add(continue_button).padRight(340.5f * (uiScale - 1)).padTop(50 * (uiScale - 1));
        pause1.row();
        pause1.add(restart_button).padTop(15 * uiScale + 43 * (uiScale - 1)).padBottom(15 * uiScale + 43 * (uiScale - 1)).padRight(340.5f * (uiScale - 1));
        pause1.row();
        pause1.add(exit_button).padRight(341 * (uiScale - 1));
        
        pause1.setBackground(new TextureRegionDrawable(gameUiAtlas.findRegion("pauseBg")));
        PauseBg = gameUiAtlas.findRegion("grey");
        
        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = new TextureRegionDrawable(gameUiAtlas.findRegion("bg_stick"));
        touchpadStyle.knob = new TextureRegionDrawable(gameUiAtlas.findRegion("knob"));
        touchpadStyle.knob.setMinWidth(22 * uiScale);
        touchpadStyle.knob.setMinHeight(22 * uiScale);
        
        Touchpad touchpad = new Touchpad(0, touchpadStyle);
        touchpad.setResetOnTouchUp(true);
        
        if (transparency) {
            touchpad.setColor(1, 1, 1, 0.7f);
        }
        
        touchpad.setBounds(10 + getFloat(Keys.joystickOffsetX), 10 + getFloat(Keys.joystickOffsetY), 110 * uiScale, 110 * uiScale);
        
        ProgressBar.ProgressBarStyle healthBarStyle = new ProgressBar.ProgressBarStyle();
        ProgressBar.ProgressBarStyle shieldBarStyle = new ProgressBar.ProgressBarStyle();
        ProgressBar.ProgressBarStyle chargeBarStyle = new ProgressBar.ProgressBarStyle();
        
        TextureRegionDrawable BarBackgroundBlank = constructFilledImageWithColor(0, (int) (12 * uiScale), Color.BLACK);
        
        healthBarStyle.knob = BarBackgroundBlank;
        healthBarStyle.knobBefore = constructFilledImageWithColor(100, (int) (12 * uiScale), Color.WHITE);
        
        shieldBarStyle.knob = BarBackgroundBlank;
        shieldBarStyle.knobBefore = constructFilledImageWithColor(100, (int) (12 * uiScale), Color.WHITE);
        
        chargeBarStyle.knob = BarBackgroundBlank;
        chargeBarStyle.knobBefore = constructFilledImageWithColor(100, (int) (12 * uiScale), Color.YELLOW);
        
        healthProgressBar = new ProgressBar(0, player.healthCapacity * player.healthMultiplier, 0.01f, false, healthBarStyle);
        shieldProgressBar = new ProgressBar(0, player.shieldCapacity * player.shieldStrengthMultiplier, 0.01f, false, shieldBarStyle);
        chargeProgressBar = new ProgressBar(0, player.chargeCapacity * player.chargeCapacityMultiplier, 0.01f, false, chargeBarStyle);
        
        healthProgressBar.setBounds(666 - 134 * (uiScale - 1), 413 - 62 * (uiScale - 1), 124 * uiScale, 10);
        shieldProgressBar.setBounds(666 - 134 * (uiScale - 1), 435 - 40 * (uiScale - 1), 72 * uiScale, 10);
        chargeProgressBar.setBounds(666 - 134 * (uiScale - 1), 457 - 18 * (uiScale - 1), 56 * uiScale, 10);
        
        healthProgressBar.setAnimateDuration(0.25f);
        shieldProgressBar.setAnimateDuration(0.25f);
        chargeProgressBar.setAnimateDuration(0.25f);
        
        if (transparency) {
            chargeProgressBar.setColor(1, 1, 1, 0.5f);
            pause.setColor(1, 1, 1, 0.5f);
            stats_indicator_panel.setColor(1, 1, 1, 0.5f);
            levelScore.setColor(1, 1, 1, 0.5f);
            money_display.setColor(1, 1, 1, 0.5f);
            font_numbers.setColor(0, 1, 1, 0.5f);
            heartIcon.setColor(1, 1, 1, 0.7f);
            shieldIcon.setColor(1, 1, 1, 0.7f);
            fireButton.setColor(1, 1, 1, 0.7f);
            weaponChangeButton.setColor(1, 1, 1, 0.7f);
        } else {
            font_numbers.setColor(0, 1, 1, 1);
        }
        
        stage.addActor(stats_indicator_panel);
        stage.addActor(pause);
        stage.addActor(levelScore);
        stage.addActor(money_display);
        stage.addActor(touchpad);
        stage.addActor(shieldProgressBar);
        stage.addActor(healthProgressBar);
        stage.addActor(chargeProgressBar);
        if (chronosModuleEnabled) {
            stage.addActor(timeFreezeButton);
            stage.addActor(timeFreezeButton_disabled);
            stage.addActor(timeFreezeButton_active);
        }
        stage.addActor(weaponControls);
        stage.addActor(heartIcon);
        stage.addActor(shieldIcon);
        
        pauseStage.addActor(pause1);
        
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
        
        timeFreezeButton_active.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setTimeWarpState(1, 1, 0, false);
                setTimeFreezeButtonState(TimeFreezeButtonState.DISABLED);
                timeSinceLastActivation = 0;
            }
        });
        
        timeFreezeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setTimeWarpState(playerSpeedMultiplier, worldSpeedMultiplier, 0.97f, true);
                setTimeFreezeButtonState(TimeFreezeButtonState.ACTIVE);
            }
        });
        
        pause.addListener(new InputListener() {
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                is_paused = true;
                soundManager.playSound_noLink("click");
                return true;
            }
        });
        
        continue_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                soundManager.playSound_noLink("click");
                if (is_paused) {
                    is_paused = false;
                }
            }
        });
        
        exit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                soundManager.playSound_noLink("click");
                if (is_paused) {
                    screenManager.setCurrentScreenMenuScreen();
                    is_paused = false;
                }
            }
        });
        
        restart_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                soundManager.playSound_noLink("click");
                if (is_paused) {
                    screenManager.setCurrentScreenGameScreen(true);
                    is_paused = false;
                }
            }
        });
        
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        
    }
    
    public void draw(float delta) {
        
        batch.end();
        
        if (chronosModuleEnabled) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(timeFreezeButtonState == TimeFreezeButtonState.AVAILABLE ? Color.valueOf("#800000") : Color.LIME);
            shapeRenderer.rect(5, 480 - uiScale * 52.5f + 5, 66 * uiScale - 10, (52.5f * uiScale - 10) * (timeCharge / (float) maxTimeCharge));
            shapeRenderer.end();
            if (timeWarpActive) {
                if (player.charge >= powerConsumption * delta && timeCharge > 0) {
                    timeCharge = clamp(timeCharge - delta * 1000, 0, maxTimeCharge);
                    player.charge -= powerConsumption * delta;
                } else {
                    setTimeFreezeButtonState(TimeFreezeButtonState.DISABLED);
                    setTimeWarpState(1, 1, 0, false);
                    timeWarpActive = false;
                    timeSinceLastActivation = -1;
                }
            } else {
                timeCharge = clamp(timeCharge + delta * 1000 * maxTimeCharge / reloadTime, 0, maxTimeCharge);
            }
            
            if(timeSinceLastActivation > 1 && timeCharge > 10 && timeFreezeButtonState == TimeFreezeButtonState.DISABLED){
                setTimeFreezeButtonState(TimeFreezeButtonState.AVAILABLE);
            }else {
                timeSinceLastActivation += delta;
            }
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
            screenManager.pauseGame();
        
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            soundManager.playSound_noLink("click");
            screenManager.pauseGame();
        }
        
        stage.draw();
        stage.act(delta);
        batch.begin();
        font_numbers.getData().setScale(0.3f * uiScale);
        font_numbers.draw(batch, "" + (int)GameVariables.score, 537 - 263 * (uiScale - 1), 467 - 12 * (uiScale - 1), 100 * uiScale, 1, false);
        font_numbers.draw(batch, "" + GameVariables.money, (537 - 122) - (263 + 122) * (uiScale - 1), 467 - 12 * (uiScale - 1), 100 * uiScale, 1, false);
        font_main.getData().setScale(0.27f * uiScale);
        
        if (showFps) {
            font_main.setColor(Color.WHITE);
            font_main.getData().setScale(0.45f + 0.225f * (uiScale - 1));
            font_main.draw(batch, "Fps: " + getFps(delta), 3, 475);
        }
        
        if (is_paused) {
            batch.draw(PauseBg, 0, 0, 800, 480);
        }
        
        batch.end();
        
        if (is_paused) {
            pauseStage.draw();
            pauseStage.act(delta);
        }
        
        batch.begin();
        Color healthColor = new Color().fromHsv(player.health / (player.healthCapacity * player.healthMultiplier) * 120, 1, 1);
        Color shieldColor = new Color().fromHsv(220 - player.shieldCharge / (player.shieldCapacity * player.shieldStrengthMultiplier) * 40, 1, 1);
        healthProgressBar.setColor(healthColor.r, healthColor.g, healthColor.b, transparency ? 0.5f : 1);
        shieldProgressBar.setColor(shieldColor.r, shieldColor.g, shieldColor.b, transparency ? 0.5f : 1);
        healthProgressBar.setValue(player.health);
        shieldProgressBar.setValue(player.shieldCharge);
        chargeProgressBar.setValue(player.charge);
        
        if (transparency) {
            font_main.setColor(0, 0, 0, 0.7f);
        } else {
            font_main.setColor(0, 0, 0, 1);
        }
        
        if (player.isDead && player.explosionEffect.isComplete()) {
            screenManager.setCurrentScreenGameOverScreen();
        }
    }
    
    private enum TimeFreezeButtonState {AVAILABLE, ACTIVE, DISABLED}
    
    private void setTimeFreezeButtonState(TimeFreezeButtonState timeFreezeButtonState) {
        this.timeFreezeButtonState = timeFreezeButtonState;
        timeFreezeButton.setVisible(timeFreezeButtonState == TimeFreezeButtonState.AVAILABLE);
        timeFreezeButton_disabled.setVisible(timeFreezeButtonState == TimeFreezeButtonState.DISABLED);
        timeFreezeButton_active.setVisible(timeFreezeButtonState == TimeFreezeButtonState.ACTIVE);
    }
    
    private void setTimeWarpState(float playerDelta, float globalDelta, float motionBlurOpacity, boolean active) {
        compositeManager.getMotionBlur().setBlurOpacity(motionBlurOpacity);
        globalDeltaMultiplier = globalDelta;
        playerDeltaMultiplier = playerDelta;
        timeWarpActive = active;
    }
    
    public void dispose() {
        
        stage.dispose();
        pauseStage.dispose();
        
        putInteger(Keys.moneyAmount, GameVariables.money);
        
        setTimeWarpState(1, 1, 0, false);
    }
    
    public float getDeltaX() {
        return deltaX;
    }
    
    public float getDeltaY() {
        return deltaY;
    }
    
    private int getFps(float delta) {
        if (fpsSmoothingArray.size < 120) {
            fpsSmoothingArray.add(delta);
        } else {
            for (int i = 0; i < fpsSmoothingArray.size - 1; i++) {
                fpsSmoothingArray.set(i, fpsSmoothingArray.get(i + 1));
            }
            fpsSmoothingArray.set(fpsSmoothingArray.size - 1, delta);
        }
        float sum = 0;
        for (int i = 0; i < fpsSmoothingArray.size; i++) {
            sum += fpsSmoothingArray.get(i);
        }
        return (int)(fpsSmoothingArray.size / sum);
    }
}
