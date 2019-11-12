package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;



public class MenuScreen implements Screen{

    private float number;
    private boolean lamp_animation;

    private SpriteBatch batch;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture MenuBg;
    private Texture Bg;
    private Texture Ship;
    private Image buildNumber;
    private Image info_enabled;
    private Image info_disabled;
    private Image more_enabled;
    private Image more_disabled;
    private Image play_enabled;
    private Image play_disabled;
    private Image settings_enabled;
    private Image settings_disabled;
    private Image online_enabled;
    private Image online_disabled;

    private Image continue_disabled;
    private Image continue_enabled;
    private Image shop_disabled;
    private Image shop_enabled;
    private Image newGame_disabled;
    private Image newGame_enabled;

    private Texture infoBg;

    private Image Lamp;

    private CheckBox sound;
    private CheckBox fps;
    private CheckBox fx;
    private CheckBox showError;
    private CheckBox transparency;
    private Slider uiScaling;
    private Slider musicVolume;
    private Slider soundEffectsVolume;
    private Slider difficultyControl;
    private CheckBox.CheckBoxStyle checkBoxStyle;
    private Skin checkBoxSkin;
    private Slider.SliderStyle sliderBarStyle, sliderBarStyle2;
    private Skin sliderBarSkin;
    private float uiScale;

    private Preferences prefs;

    private BitmapFont font_main;

    private boolean info;
    private boolean settings;
    private boolean play;
    private boolean error;
    public static boolean Music;
    public static boolean Sound;
    public static float MusicVolume;
    public static float SoundVolume;

    private Stage Menu;

    private int movement;

    private InputMultiplexer multiplexer;

    private Music music;
    private float millis;
    private float millis2;

    private Game game;

    private AssetManager assetManager;

    private ParticleEffect fire;

    public MenuScreen(final Game game, final SpriteBatch batch, final AssetManager assetManager){

        this.game = game;

        this.assetManager = assetManager;

        prefs = Gdx.app.getPreferences("Preferences");

        uiScale = prefs.getFloat("ui");
        MusicVolume = (int)(prefs.getFloat("musicVolume")*100);
        SoundVolume = (int)(prefs.getFloat("soundEffectsVolume")*100);

        if(MusicVolume > 0) {
            Music = true;
        }

        if(SoundVolume > 0) {
            Sound = true;
        }

        error = prefs.getBoolean("error");

        this.batch = batch;

        camera = new OrthographicCamera(800, 480);
        viewport = new FitViewport(800,480, camera);

        MenuBg = assetManager.get("menuBg.png");
        Lamp = new Image((Texture)(assetManager.get("lamp.png")));
        infoBg = assetManager.get("infoBg.png");

        font_main = assetManager.get("fonts/font2.fnt");

        Bg = assetManager.get("bg_old.png");
        Bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Ship = assetManager.get("ship2.png");

        buildNumber = new Image((Texture)assetManager.get("greyishButton.png"));

        info_enabled = new Image((Texture)assetManager.get("menuButtons/info_enabled.png"));
        info_disabled = new Image((Texture)assetManager.get("menuButtons/info_disabled.png"));

        more_enabled = new Image((Texture)assetManager.get("menuButtons/more_enabled.png"));
        more_disabled = new Image((Texture)assetManager.get("menuButtons/more_disabled.png"));

        play_enabled = new Image((Texture)assetManager.get("menuButtons/play_enabled.png"));
        play_disabled = new Image((Texture)assetManager.get("menuButtons/play_disabled.png"));

        settings_enabled = new Image((Texture)assetManager.get("menuButtons/settings_enabled.png"));
        settings_disabled = new Image((Texture)assetManager.get("menuButtons/settings_disabled.png"));

        online_enabled = new Image((Texture)assetManager.get("menuButtons/online_enabled.png"));
        online_disabled = new Image((Texture)assetManager.get("menuButtons/online_disabled.png"));

        continue_enabled  = new Image((Texture)assetManager.get("menuButtons/continue_e.png"));
        continue_disabled  = new Image((Texture)assetManager.get("menuButtons/continue_d.png"));

        newGame_enabled = new Image((Texture)assetManager.get("menuButtons/newGame_e.png"));
        newGame_disabled = new Image((Texture)assetManager.get("menuButtons/newGame_d.png"));

        shop_enabled = new Image((Texture)assetManager.get("menuButtons/shop_e.png"));
        shop_disabled = new Image((Texture)assetManager.get("menuButtons/shop_d.png"));

        play_disabled.setBounds(545, 325, 250, 75);
        online_disabled.setBounds(545, 245, 250, 75);
        settings_disabled.setBounds(545, 165, 250, 75);
        info_disabled.setBounds(545, 85, 250, 75);
        more_disabled.setBounds(545, 5, 250, 75);
        buildNumber.setBounds(5,5,150, 50);

        play_enabled.setBounds(545, 325, 250, 75);
        online_enabled.setBounds(545, 245, 250, 75);
        settings_enabled.setBounds(545, 165, 250, 75);
        info_enabled.setBounds(545, 85, 250, 75);
        more_enabled.setBounds(545, 5, 250, 75);

        continue_enabled.setBounds(180, 385, 160, 44);
        continue_disabled.setBounds(180, 385, 160, 44);

        newGame_enabled.setBounds(20, 385, 160, 44);
        newGame_disabled.setBounds(20, 385, 160, 44);

        shop_enabled.setBounds(340, 385, 160, 44);
        shop_disabled.setBounds(340, 385, 160, 44);

        Lamp.setBounds(730, 430, 15, 35);

        checkBoxSkin = new Skin();
        checkBoxSkin.add("off", assetManager.get("checkBox_disabled.png"));
        checkBoxSkin.add("on", assetManager.get("checkBox_enabled.png"));

        checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOff = checkBoxSkin.getDrawable("off");
        checkBoxStyle.checkboxOn = checkBoxSkin.getDrawable("on");
        checkBoxStyle.font = font_main;

        sliderBarSkin = new Skin();
        sliderBarSkin.add("knob", assetManager.get("progressBarKnob.png"));
        sliderBarSkin.add("knob2", assetManager.get("progressBarKnob.png"));
        sliderBarSkin.add("bg", assetManager.get("progressBarBg.png"));
        sliderBarSkin.add("bg2", assetManager.get("progressBarBg.png"));

        sliderBarStyle = new Slider.SliderStyle();
        sliderBarStyle.background = sliderBarSkin.getDrawable("bg");
        sliderBarStyle.knob = sliderBarSkin.getDrawable("knob");
        sliderBarStyle.knob.setMinHeight(62.5f);
        sliderBarStyle.knob.setMinWidth(37.5f);
        sliderBarStyle.background.setMinHeight(62.5f);
        sliderBarStyle.background.setMinWidth(250.0f);

        sliderBarStyle2 = new Slider.SliderStyle();
        sliderBarStyle2.background = sliderBarSkin.getDrawable("bg2");
        sliderBarStyle2.knob = sliderBarSkin.getDrawable("knob2");
        sliderBarStyle2.knob.setMinHeight(42.5f);
        sliderBarStyle2.knob.setMinWidth(27.5f);
        sliderBarStyle2.background.setMinHeight(42.5f);
        sliderBarStyle2.background.setMinWidth(230.0f);

        sound = new CheckBox("",checkBoxStyle);
        fps = new CheckBox("",checkBoxStyle);
        fx = new CheckBox("",checkBoxStyle);
        showError = new CheckBox("",checkBoxStyle);
        transparency = new CheckBox("",checkBoxStyle);

        uiScaling = new Slider(1, 2, 0.1f, false, sliderBarStyle);
        musicVolume = new Slider(0, 1, 0.1f, false, sliderBarStyle2);
        soundEffectsVolume = new Slider(0, 1, 0.1f, false, sliderBarStyle2);
        difficultyControl = new Slider(1, 5, 0.1f, false, sliderBarStyle2);

        sound.setDisabled(true);
        fx.setDisabled(true);

        uiScaling.setValue(prefs.getFloat("ui"));
        musicVolume.setValue(prefs.getFloat("musicVolume"));
        soundEffectsVolume.setValue(prefs.getFloat("soundEffectsVolume"));
        difficultyControl.setValue(prefs.getFloat("difficulty"));
        fps.setChecked(prefs.getBoolean("showFps"));
        sound.setChecked(Music);
        fx.setChecked(Sound);
        showError.setChecked(error);
        transparency.setChecked(prefs.getBoolean("transparency"));

        sound.setBounds(13, 390, 50, 50);
        fx.setBounds(13, 340, 50, 50);
        fps.setBounds(13, 290, 50, 50);
        showError.setBounds(13, 150, 50, 50);
        uiScaling.setBounds(10, 220, 250, 40);
        soundEffectsVolume.setBounds(360, 350, 170, 25);
        difficultyControl.setBounds(20, 340, 170, 25);
        musicVolume.setBounds(360, 400, 170, 25);
        transparency.setBounds(13,100,50,50);
        sound.getImage().setScaling(Scaling.fill);
        fps.getImage().setScaling(Scaling.fill);
        fx.getImage().setScaling(Scaling.fill);
        showError.getImage().setScaling(Scaling.fill);
        transparency.getImage().setScaling(Scaling.fill);

        Menu = new Stage(viewport, batch);

        Menu.addActor(play_disabled);
        Menu.addActor(online_disabled);
        Menu.addActor(settings_disabled);
        Menu.addActor(info_disabled);
        Menu.addActor(more_disabled);

        Menu.addActor(buildNumber);

        Menu.addActor(play_enabled);
        Menu.addActor(online_enabled);
        Menu.addActor(settings_enabled);
        Menu.addActor(info_enabled);
        Menu.addActor(more_enabled);

        play_enabled.setVisible(false);
        online_enabled.setVisible(false);
        settings_enabled.setVisible(false);
        info_enabled.setVisible(false);
        more_enabled.setVisible(false);

        fps.setVisible(false);
        sound.setVisible(false);
        fx.setVisible(false);
        showError.setVisible(false);
        uiScaling.setVisible(false);
        musicVolume.setVisible(false);
        soundEffectsVolume.setVisible(false);
        difficultyControl.setVisible(false);
        transparency.setVisible(false);

        Menu.addActor(fps);
        Menu.addActor(sound);
        Menu.addActor(fx);
        Menu.addActor(showError);
        Menu.addActor(uiScaling);
        Menu.addActor(musicVolume);
        Menu.addActor(soundEffectsVolume);
        Menu.addActor(difficultyControl);
        Menu.addActor(transparency);

        Menu.addActor(newGame_disabled);
        Menu.addActor(continue_disabled);
        Menu.addActor(shop_disabled);

        Menu.addActor(continue_enabled);
        Menu.addActor(shop_enabled);
        Menu.addActor(newGame_enabled);

        newGame_enabled.setVisible(false);
        continue_enabled.setVisible(false);
        shop_enabled.setVisible(false);
        newGame_disabled.setVisible(false);
        continue_disabled.setVisible(false);
        shop_disabled.setVisible(false);

        fire = new ParticleEffect();
        fire.load(Gdx.files.internal("particles/engine_warp.p"), Gdx.files.internal("particles"));
        fire.setPosition(205, 271);
        fire.start();

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(Menu);

        play_disabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                play_enabled.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                play_enabled.setVisible(false);

                if (play){
                    play = false;
                    fps.setVisible(false);
                    sound.setVisible(false);
                    fx.setVisible(false);
                    showError.setVisible(false);
                    uiScaling.setVisible(false);
                    musicVolume.setVisible(false);
                    soundEffectsVolume.setVisible(false);
                    difficultyControl.setVisible(false);
                    newGame_disabled.setVisible(false);
                    continue_disabled.setVisible(false);
                    shop_disabled.setVisible(false);
                    transparency.setVisible(false);
                }else {
                    play = true;
                    info = false;
                    settings = false;
                    fps.setVisible(false);
                    sound.setVisible(false);
                    fx.setVisible(false);
                    showError.setVisible(false);
                    uiScaling.setVisible(false);
                    musicVolume.setVisible(false);
                    soundEffectsVolume.setVisible(false);
                    difficultyControl.setVisible(true);
                    newGame_disabled.setVisible(true);
                    continue_disabled.setVisible(true);
                    shop_disabled.setVisible(true);
                    transparency.setVisible(false);
                }
            }
        });

        online_disabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                online_enabled.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                online_enabled.setVisible(false);
            }
        });

        settings_disabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                settings_enabled.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                settings_enabled.setVisible(false);

                if (settings){
                    settings = false;
                    fps.setVisible(false);
                    sound.setVisible(false);
                    fx.setVisible(false);
                    showError.setVisible(false);
                    uiScaling.setVisible(false);
                    musicVolume.setVisible(false);
                    soundEffectsVolume.setVisible(false);
                    difficultyControl.setVisible(false);
                    newGame_disabled.setVisible(false);
                    continue_disabled.setVisible(false);
                    shop_disabled.setVisible(false);
                    transparency.setVisible(false);
                }else {
                    settings = true;
                    info = false;
                    play = false;
                    fps.setVisible(true);
                    sound.setVisible(true);
                    fx.setVisible(true);
                    showError.setVisible(true);
                    uiScaling.setVisible(true);
                    musicVolume.setVisible(true);
                    soundEffectsVolume.setVisible(true);
                    difficultyControl.setVisible(false);
                    newGame_disabled.setVisible(false);
                    continue_disabled.setVisible(false);
                    shop_disabled.setVisible(false);
                    transparency.setVisible(true);
                }
            }
        });

        info_disabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                info_enabled.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                info_enabled.setVisible(false);
                if (info){
                    info = false;
                }else {
                    info = true;
                    settings = false;
                    play = false;
                    fps.setVisible(false);
                    sound.setVisible(false);
                    fx.setVisible(false);
                    showError.setVisible(false);
                    uiScaling.setVisible(false);
                    musicVolume.setVisible(false);
                    soundEffectsVolume.setVisible(false);
                    difficultyControl.setVisible(false);
                    newGame_disabled.setVisible(false);
                    continue_disabled.setVisible(false);
                    shop_disabled.setVisible(false);
                    transparency.setVisible(false);
                }
            }
        });

        more_disabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                more_enabled.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                more_enabled.setVisible(false);
            }
        });

        uiScaling.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                prefs.putFloat("ui", uiScaling.getValue());
                prefs.flush();
            }

        });

        fps.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                prefs.putBoolean("showFps", fps.isChecked());
                prefs.flush();
            }

        });

        soundEffectsVolume.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                prefs.putFloat("soundEffectsVolume", soundEffectsVolume.getValue());
                prefs.flush();

            }

        });

        musicVolume.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                prefs.putFloat("musicVolume", musicVolume.getValue());

                if(musicVolume.getValue() > 0) {
                    Music = true;
                }else{
                    Music = false;
                }

                if(Music && !music.isPlaying()){
                    music.play();
                    music.setVolume(0);
                    music.setPosition(1);
                }else{
                    if (!Music) {
                        music.stop();
                    }
                }

                prefs.flush();
            }
        });

        soundEffectsVolume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(soundEffectsVolume.getValue() > 0) {
                    Sound = true;
                    fx.setChecked(true);
                }else{
                    Sound = false;
                    fx.setChecked(false);
                }
            }
        });

        musicVolume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(music.isPlaying()) {
                    music.setVolume(musicVolume.getValue());
                }

                if(musicVolume.getValue() > 0) {
                    sound.setChecked(true);
                }else{
                    sound.setChecked(false);
                }
            }
        });

        showError.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                error = showError.isChecked();
                prefs.putBoolean("error", error);
                prefs.flush();
            }
        });

        difficultyControl.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                prefs.putFloat("difficulty", (float)((int)(difficultyControl.getValue()*100))/100);
                prefs.flush();
            }
        });

        newGame_disabled.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                newGame_enabled.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                newGame_enabled.setVisible(false);
                game.setScreen(new LoadingScreen(game, batch, assetManager, 1, true));
            }
        });

        continue_disabled.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                continue_enabled.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                continue_enabled.setVisible(false);
            }
        });

        shop_disabled.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                shop_enabled.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                shop_enabled.setVisible(false);
            }
        });

        transparency.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              prefs.putBoolean("transparency", transparency.isChecked());
              prefs.flush();
            }
        });

        music = Gdx.audio.newMusic(Gdx.files.internal("music/main.ogg"));

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
        millis2 = 101;
        music.setVolume(0);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        movement = (int)(movement + (200 * delta));

        batch.draw(Bg, 0, 0, movement, -240, 800, 720);

        fire.draw(batch);
        fire.update(delta);

        batch.draw(Ship, 200, 250, 133.84616f, 58.46154f);
        batch.draw(MenuBg, 0,0, 800, 480);

        if(info){
            batch.draw(infoBg, 5,65, 531, 410);
            font_main.getData().setScale(0.5f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "ø¤º°°º¤ø,¸¸,ø¤º°[ Info ]°º¤ø,¸¸,ø¤º°°º¤ø", 5, 460, 531, 1, false);
            font_main.setColor(Color.valueOf("#00ff55"));
            font_main.draw(batch, "Made by Deoxys", 5, 415, 531, 1, false);
            font_main.setColor(Color.CYAN);
            font_main.draw(batch, "Textures and Music by DefenseX", 5, 375, 531, 1, false);
            font_main.draw(batch, "Inspired by DefenseX, PetruCHIOrus", 5, 345, 531, 1, false);
            font_main.setColor(Color.valueOf("#cccc22"));
            font_main.draw(batch, "Testers: Misterowl, Kisliy_xleb", 5, 305, 531, 1, false);
            font_main.draw(batch, "Lumix_lab, Watermelon0guy, PYTHØN", 5, 275, 531, 1, false);
            font_main.draw(batch, "Ha4upelmeney, Lukmanov", 5, 245, 531, 1, false);
            font_main.setColor(Color.valueOf("#0FE500"));
            font_main.draw(batch, "Contributors: Volkov, DefenseX", 5, 210, 531, 1, false);
            font_main.draw(batch, "Zsingularityz", 5, 176, 531, 1, false);
            font_main.setColor(Color.valueOf("#CAE500"));
            font_main.draw(batch, "Deltacore", 5, 135, 531, 1, false);
            font_main.draw(batch,"® All right reserved", 5, 95, 531, 1, false);
        }

        if(settings){
            batch.draw(infoBg, 5,65, 531, 410);
            font_main.getData().setScale(0.5f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "ø¤º°°º¤ø,¸¸,ø¤[ Settings ]¤ø,¸¸,ø¤º°°º¤ø", 5, 460, 531, 1, false);
            font_main.setColor(Color.valueOf("#0FE500"));
            MusicVolume = (int)(musicVolume.getValue()*100);
            SoundVolume = (int)(soundEffectsVolume.getValue()*100);
            font_main.draw(batch, "Music: " + MusicVolume + "%", 145, 420, 132, 1, false);
            font_main.draw(batch, "Sound effects: " + SoundVolume + "%", 145, 370, 132, 1, false);
            font_main.draw(batch, "Show Fps", 65, 320, 132, 1, false);
            uiScale = (int)(uiScaling.getValue()*100);
            font_main.draw(batch, "Ui Scaling: "+uiScale+" %", 325, 263, 132, 1, false);
            font_main.draw(batch, "(In game)", 325, 233, 132, 1, false);
            font_main.draw(batch, "Semi-transparent UI", 140, 128, 132, 1, false);

            if(error){
                font_main.setColor(Color.RED);
                font_main.getData().setScale(0.4f);
                if(prefs.getString("lastError").length() > 1) {
                    font_main.draw(batch, prefs.getString("lastError"), 230, 178, 132, 1, false);
                }else{
                    font_main.draw(batch, "No errors", 230, 178, 132, 1, false);
                }
            }else{
                font_main.setColor(Color.valueOf("#0FE500"));
                font_main.getData().setScale(0.5f);
                font_main.draw(batch, "Show last error (developer)", 188, 178, 132, 1, false);
            }
        }

        if(Music) {

            if (millis > 10) {
                if (music.getPosition() > 65 && music.getPosition() < 69 && music.getVolume() > 0) {
                    music.setVolume(music.getVolume() - 0.05f);
                }
                if (music.getPosition() > 0 && music.getPosition() < 4 && music.getVolume() < musicVolume.getValue()) {
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

        if(play){
            batch.draw(infoBg, 5,65, 531, 410);
            font_main.getData().setScale(0.5f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "ø¤º°º¤ø,¸¸,ø¤[ Play Menu ]¤ø,¸¸,ø¤º°º¤ø", 5, 460, 531, 1, false);
            font_main.setColor(new Color().fromHsv(Math.abs(120-difficultyControl.getValue()*20), 1.5f, 1).add(0,0,0,1));
            font_main.draw(batch, "Difficulty: X"+(float)((int)(difficultyControl.getValue()*100))/100, 30, 355, 531, 1, false);
        }

        if(lamp_animation) {
            number = number + 0.01f;
        }else{
            number = number - 0.01f;
        }
        if(number>=1){
            lamp_animation = false;
        }
        if(number<=0){
            lamp_animation = true;
        }
        Lamp.setColor(1,1,1, number);
        Lamp.draw(batch, 1);

        batch.end();

        Menu.draw();
        Menu.act(delta);

        batch.begin();
        font_main.getData().setScale(0.35f);
        font_main.setColor(Color.GOLD);
        font_main.draw(batch, "V 0.0.1 Build 29", 5, 35, 150, 1, false);
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
        music.stop();
        game.getScreen().dispose();
    }

    @Override
    public void dispose() {

    assetManager.unload("greyishButton.png");
    assetManager.unload("menuButtons/info_enabled.png");
    assetManager.unload("menuButtons/info_disabled.png");
    assetManager.unload("menuButtons/more_enabled.png");
    assetManager.unload("menuButtons/more_disabled.png");
    assetManager.unload("menuButtons/play_enabled.png");
    assetManager.unload("menuButtons/play_disabled.png");
    assetManager.unload("menuButtons/settings_enabled.png");
    assetManager.unload("menuButtons/settings_disabled.png");
    assetManager.unload("menuButtons/online_enabled.png");
    assetManager.unload("menuButtons/online_disabled.png");

    assetManager.unload("menuBg.png");
    assetManager.unload("lamp.png");
    assetManager.unload("infoBg.png");
    assetManager.unload("bg_old.png");
    assetManager.unload("ship2.png");

    assetManager.unload("checkBox_disabled.png");
    assetManager.unload("checkBox_enabled.png");
    assetManager.unload("progressBarKnob.png");
    assetManager.unload("progressBarBg.png");

    assetManager.unload("menuButtons/continue_e.png");
    assetManager.unload("menuButtons/continue_d.png");
    assetManager.unload("menuButtons/newGame_d.png");
    assetManager.unload("menuButtons/shop_d.png");
    assetManager.unload("menuButtons/newGame_e.png");
    assetManager.unload("menuButtons/shop_e.png");

    Menu.dispose();
    music.dispose();
    checkBoxSkin.dispose();
    sliderBarSkin.dispose();
    font_main.dispose();
    fire.dispose();

    }
}
