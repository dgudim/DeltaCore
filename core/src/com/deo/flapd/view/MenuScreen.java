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
import com.badlogic.gdx.math.MathUtils;
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

    private Texture shop;
    private Texture shopButton_small_enabled;
    private Texture shopButton_small_disabled;
    private Texture shopButton_enabled;
    private Texture shopButton_disabled;
    private Texture menu_purchase, menu_purchase2;

    private Texture Engine1_t;
    private Texture Engine2_t;
    private Texture Engine3_t;

    private Texture Cannon1_t;
    private Texture Cannon2_t;
    private Texture Cannon3_t;

    private Texture cog;

    private Image Engine1;
    private Image Engine2;
    private Image Engine3;
    private Image yes, yesDisabled, no, noDisabled, upgrade, upgradeDisabled;

    private Image CategoryGun;
    private Image CategoryGun2;
    private Image CategoryEngine;
    private Image Cannon1;
    private Image Cannon2;
    private Image Cannon3;

    private Texture infoBg;

    private Image Lamp;

    private CheckBox fps;
    private CheckBox transparency;
    private CheckBox shaders;
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

    private BitmapFont font_main, font_numbers;

    private boolean info;
    private boolean settings;
    private boolean play;
    private boolean Music;
    private boolean Shop;
    public static boolean Sound;
    private float MusicVolume;
    public static float SoundVolume;

    private Stage Menu, ShopStage;

    private int movement;

    private InputMultiplexer multiplexer;

    private Music music;
    private float millis;
    private float millis2;

    private Game game;

    private AssetManager assetManager;

    private ParticleEffect fire, fire2;

    private int current_engine, current_cannon, current_category, money, cogs, ship_offset, menu_offset, menu_type, engine1upgradeLevel, engine2upgradeLevel, engine3upgradeLevel, cannon1upgradeLevel, cannon2upgradeLevel, cannon3upgradeLevel;

    private boolean menuAnimation;

    private boolean is2ndEngineUnlocked, is3rdEngineUnlocked, is2ndCannonUnlocked, is3rdCannonUnlocked;

    private boolean easterEgg;

       public MenuScreen(final Game game, final SpriteBatch batch, final AssetManager assetManager){

        this.game = game;

        this.assetManager = assetManager;

        menu_offset = 1000;

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

        current_engine = prefs.getInteger("current_engine");
        if(current_engine<1){
            prefs.putInteger("current_engine", 1);
            prefs.flush();
            current_engine = 1;
        }

        current_cannon = prefs.getInteger("current_cannon");
        if(current_cannon<1){
            prefs.putInteger("current_cannon", 1);
            prefs.flush();
            current_cannon = 1;
        }

        current_category = prefs.getInteger("current_category");
        if(current_category<1){
            prefs.putInteger("current_category", 1);
            prefs.flush();
            current_category = 1;
        }

        money = prefs.getInteger("money");
        cogs = prefs.getInteger("cogs");
        easterEgg = prefs.getBoolean("easterEgg");

        engine1upgradeLevel = prefs.getInteger("engine1upgradeLevel");
        engine2upgradeLevel = prefs.getInteger("engine2upgradeLevel");
        engine3upgradeLevel = prefs.getInteger("engine3upgradeLevel");
        cannon1upgradeLevel = prefs.getInteger("cannon1upgradeLevel");
        cannon2upgradeLevel = prefs.getInteger("cannon2upgradeLevel");
        cannon3upgradeLevel = prefs.getInteger("cannon3upgradeLevel");

        is2ndEngineUnlocked = prefs.getBoolean("is2ndEngineUnlocked");
        is3rdEngineUnlocked = prefs.getBoolean("is3rdEngineUnlocked");
        is2ndCannonUnlocked = prefs.getBoolean("is2ndCannonUnlocked");
        is3rdCannonUnlocked = prefs.getBoolean("is3rdCannonUnlocked");

        this.batch = batch;

        camera = new OrthographicCamera(800, 480);
        viewport = new FitViewport(800,480, camera);

        MenuBg = assetManager.get("menuBg.png");
        Lamp = new Image((Texture)(assetManager.get("lamp.png")));
        infoBg = assetManager.get("infoBg.png");

        font_main = assetManager.get("fonts/font2.fnt");
        font_numbers = assetManager.get("fonts/font.fnt");

        Bg = assetManager.get("bg_old.png");
        Bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Ship = assetManager.get("ship.png");

        menu_purchase = assetManager.get("shop/menuBuy.png");
        menu_purchase2 = assetManager.get("shop/menuBuy2.png");

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

        shop = assetManager.get("shop/main.png");
        shopButton_disabled = assetManager.get("shop/button_small.png");
        shopButton_enabled = assetManager.get("shop/button_small_enabled.png");
        shopButton_small_disabled = assetManager.get("shop/button_tiny.png");
        shopButton_small_enabled = assetManager.get("shop/button_tiny_enabled.png");

        Engine1_t = assetManager.get("shop/engine1.png");
        Engine2_t = assetManager.get("shop/engine2.png");
        Engine3_t = assetManager.get("shop/engine3.png");

        Cannon1_t = assetManager.get("shop/Cannon1.png");
        Cannon2_t = assetManager.get("shop/Cannon2.png");
        Cannon3_t = assetManager.get("shop/Cannon3.png");

        cog = assetManager.get("bonus_part.png");

        Engine1 = new Image((Texture)assetManager.get("shop/engine1.png"));
        Engine2 = new Image((Texture)assetManager.get("shop/engine2.png"));
        Engine3 = new Image((Texture)assetManager.get("shop/engine3.png"));
        Cannon1 = new Image((Texture)assetManager.get("shop/Cannon1.png"));
        Cannon2 = new Image((Texture)assetManager.get("shop/Cannon2.png"));
        Cannon3 = new Image((Texture)assetManager.get("shop/Cannon3.png"));
        CategoryEngine = new Image((Texture)assetManager.get("shop/CategoryEngine.png"));
        CategoryGun = new Image((Texture)assetManager.get("shop/CategoryGun.png"));
        CategoryGun2 = new Image((Texture)assetManager.get("shop/CategoryGun2.png"));

        yes = new Image((Texture)assetManager.get("shop/yes.png"));
        yesDisabled = new Image((Texture)assetManager.get("shop/yesDisabled.png"));
        no = new Image((Texture) assetManager.get("shop/no.png"));
        noDisabled = new Image((Texture)assetManager.get("shop/noDisabled.png"));
        upgrade = new Image((Texture)assetManager.get("shop/upgrade.png"));
        upgradeDisabled = new Image((Texture)assetManager.get("shop/upgradeDisabled.png"));

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

        Engine3.setBounds(54.57f, 235.39801f, 114.66f, 45.864f);
        Engine2.setBounds(54.57f, 312.058f, 114.66f, 45.864f);
        Engine1.setBounds(54.57f, 388.71802f, 114.66f, 45.864f);

        Cannon1.setBounds(47, 388.71802f, 130.14354f, 45.933018f);
        Cannon2.setBounds(42, 312.058f, 142.07649f, 45.901638f);
        Cannon3.setBounds(43, 235.39801f, 140.01207f, 45.866024f);

        yes.setBounds(-100, -100, 83.2f, 57.2f);
        no.setBounds(-100, -100, 83.2f, 57.2f);
        yesDisabled.setBounds(-100, -100, 83.2f, 57.2f);
        noDisabled.setBounds(-100, -100, 83.2f, 57.2f);
        upgrade.setBounds(-100, -100, 280.8f, 57.2f);
        upgradeDisabled.setBounds(-100, -100, 280.8f, 57.2f);

        CategoryEngine.setBounds(245, 400, 60, 46);
        CategoryGun.setBounds(337, 405, 70, 38);
        CategoryGun2.setBounds(445, 400, 46, 46);

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

        fps = new CheckBox("",checkBoxStyle);
        transparency = new CheckBox("",checkBoxStyle);
        shaders = new CheckBox("",checkBoxStyle);

        uiScaling = new Slider(1, 2, 0.1f, false, sliderBarStyle);
        musicVolume = new Slider(0, 1, 0.1f, false, sliderBarStyle2);
        soundEffectsVolume = new Slider(0, 1, 0.1f, false, sliderBarStyle2);
        difficultyControl = new Slider(1, 5, 0.1f, false, sliderBarStyle2);

        uiScaling.setValue(prefs.getFloat("ui"));
        musicVolume.setValue(prefs.getFloat("musicVolume"));
        soundEffectsVolume.setValue(prefs.getFloat("soundEffectsVolume"));
        difficultyControl.setValue(prefs.getFloat("difficulty"));
        fps.setChecked(prefs.getBoolean("showFps"));
        transparency.setChecked(prefs.getBoolean("transparency"));
        shaders.setChecked(prefs.getBoolean("shaders"));

        fps.setBounds(13, 290, 50, 50);
        uiScaling.setBounds(10, 220, 250, 40);
        soundEffectsVolume.setBounds(310, 350, 170, 25);
        difficultyControl.setBounds(20, 340, 170, 25);
        musicVolume.setBounds(310, 400, 170, 25);
        transparency.setBounds(13,100,50,50);
        shaders.setBounds(13, 150, 50, 50);
        fps.getImage().setScaling(Scaling.fill);
        transparency.getImage().setScaling(Scaling.fill);
        shaders.getImage().setScaling(Scaling.fill);

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
        uiScaling.setVisible(false);
        musicVolume.setVisible(false);
        soundEffectsVolume.setVisible(false);
        difficultyControl.setVisible(false);
        transparency.setVisible(false);
        shaders.setVisible(false);

        Menu.addActor(fps);
        Menu.addActor(uiScaling);
        Menu.addActor(musicVolume);
        Menu.addActor(soundEffectsVolume);
        Menu.addActor(difficultyControl);
        Menu.addActor(transparency);
        Menu.addActor(shaders);

        Menu.addActor(newGame_disabled);
        Menu.addActor(continue_disabled);
        Menu.addActor(shop_disabled);

        Menu.addActor(continue_enabled);
        Menu.addActor(shop_enabled);
        Menu.addActor(newGame_enabled);

        Menu.addActor(Engine1);
        Menu.addActor(Engine2);
        Menu.addActor(Engine3);
        Menu.addActor(Cannon1);
        Menu.addActor(Cannon2);
        Menu.addActor(Cannon3);
        Menu.addActor(CategoryEngine);
        Menu.addActor(CategoryGun);
        Menu.addActor(CategoryGun2);

        ShopStage = new Stage(viewport, batch);

        ShopStage.addActor(yesDisabled);
        ShopStage.addActor(noDisabled);
        ShopStage.addActor(upgradeDisabled);
        ShopStage.addActor(yes);
        ShopStage.addActor(no);
        ShopStage.addActor(upgrade);

        newGame_enabled.setVisible(false);
        continue_enabled.setVisible(false);
        shop_enabled.setVisible(false);
        newGame_disabled.setVisible(false);
        continue_disabled.setVisible(false);
        shop_disabled.setVisible(false);
        Engine1.setVisible(false);
        Engine2.setVisible(false);
        Engine3.setVisible(false);

        upgrade.setVisible(false);
        yes.setVisible(false);
        no.setVisible(false);

        CategoryGun.setVisible(false);
        CategoryGun2.setVisible(false);
        CategoryEngine.setVisible(false);
        Cannon1.setVisible(false);
        Cannon2.setVisible(false);
        Cannon3.setVisible(false);

        fire = new ParticleEffect();
        fire2 = new ParticleEffect();

           switch (current_engine){
               case(1):
                   fire.load(Gdx.files.internal("particles/fire_engileleft_red_green.p"), Gdx.files.internal("particles"));
                   fire2.load(Gdx.files.internal("particles/fire_engileleft_red_green.p"), Gdx.files.internal("particles"));
                   break;
               case(2):
                   fire.load(Gdx.files.internal("particles/fire_engileleft_red_purple.p"), Gdx.files.internal("particles"));
                   fire2.load(Gdx.files.internal("particles/fire_engileleft_red_purple.p"), Gdx.files.internal("particles"));
                   break;
               case(3):
                   fire.load(Gdx.files.internal("particles/fire_engileleft_blue_purple.p"), Gdx.files.internal("particles"));
                   fire2.load(Gdx.files.internal("particles/fire_engileleft_blue_purple.p"), Gdx.files.internal("particles"));
                   break;
           }

           fire.setPosition(330, 268);
           fire.start();
           fire2.setPosition(324, 290);
           fire2.start();

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(Menu);
        multiplexer.addProcessor(ShopStage);

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
                    Hide(0);
                }else {
                    play = true;
                    info = false;
                    settings = false;
                    Shop = false;
                    Hide(2);
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
                    Hide(0);
                }else {
                    settings = true;
                    info = false;
                    play = false;
                    Shop = false;
                    Hide(1);
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
                    Shop = false;
                    Hide(0);
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

                Music = musicVolume.getValue() > 0;

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

        musicVolume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(music.isPlaying()) {
                    music.setVolume(musicVolume.getValue());
                }
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
                game.setScreen(new LoadingScreen(game, batch, assetManager, 1, true, false));
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
                if(prefs.getFloat("Health")>0) {
                    game.setScreen(new LoadingScreen(game, batch, assetManager, 1, false, false));
                }
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
                if(!Shop) {
                    Shop = true;
                    info = false;
                    settings = false;
                    play = false;
                    switch(current_category){
                        case(1):
                            Hide(3);
                            break;
                        case(2):
                            Hide(4);
                            break;
                        case(3):
                            Hide(5);
                            break;
                    }
                }
            }
        });

        transparency.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putBoolean("transparency", transparency.isChecked());
                prefs.flush();
            }
        });

        shaders.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putBoolean("shaders", shaders.isChecked());
                prefs.flush();
            }
        });

        Engine1.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(current_engine == 1){
                    menu_offset = 350;
                    menu_type = 2;
                    menuAnimation = true;
                }else{
                    menuAnimation = false;
                    menu_offset = 350;
                }
                current_engine = 1;
                prefs.putInteger("current_engine", 1);
                prefs.flush();
                UpdateFire();
            }
        });

        Engine2.addListener(new InputListener(){

               @Override
               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                   return true;
               }

               @Override
               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                   if(current_engine == 2){
                       menu_offset = 350;
                       if(is2ndEngineUnlocked) {
                           menu_type = 2;
                           menuAnimation = true;
                       }else{
                           menu_type = 1;
                           menuAnimation = true;
                       }
                   }else{
                       menuAnimation = false;
                       menu_offset = 350;
                   }
                   current_engine = 2;
                   if(is2ndEngineUnlocked) {
                       prefs.putInteger("current_engine", 2);
                       prefs.flush();
                   }
                   UpdateFire();
               }
           });

        Engine3.addListener(new InputListener(){

               @Override
               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                   return true;
               }

               @Override
               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                   if(current_engine == 3){
                       menu_offset = 350;
                       if(is3rdEngineUnlocked) {
                           menu_type = 2;
                           menuAnimation = true;
                       }else{
                           menu_type = 1;
                           menuAnimation = true;
                       }
                   }else{
                       menuAnimation = false;
                       menu_offset = 350;
                   }
                   current_engine = 3;
                   if(is3rdEngineUnlocked) {
                       prefs.putInteger("current_engine", 3);
                       prefs.flush();
                   }
                   UpdateFire();
               }
           });

        yesDisabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                yes.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                yes.setVisible(false);
                switch (current_category){
                    case(1):
                        switch (current_engine){
                        case(2):
                            if(money>=1500){
                                is2ndEngineUnlocked = true;
                                money = money-1500;
                                prefs.putInteger("money",money);
                                prefs.putBoolean("is2ndEngineUnlocked", true);
                                prefs.putInteger("current_engine", 2);
                                prefs.flush();
                                menuAnimation = false;
                            }
                            break;
                        case(3):
                            if(money>=3500){
                                is3rdEngineUnlocked = true;
                                money = money-3500;
                                prefs.putInteger("money",money);
                                prefs.putBoolean("is3rdEngineUnlocked", true);
                                prefs.putInteger("current_engine", 3);
                                prefs.flush();
                                menuAnimation = false;
                            }
                            break;
                    }
                        break;
                    case(2):
                        switch (current_cannon){
                            case(2):
                                if(money>=2500){
                                    is2ndCannonUnlocked = true;
                                    money = money-2500;
                                    prefs.putInteger("money",money);
                                    prefs.putBoolean("is2ndCannonUnlocked", true);
                                    prefs.putInteger("current_cannon", 2);
                                    prefs.flush();
                                    menuAnimation = false;
                                }
                                break;
                            case(3):
                                if(money>=4500){
                                    is3rdCannonUnlocked = true;
                                    money = money-4500;
                                    prefs.putInteger("money",money);
                                    prefs.putBoolean("is3rdCannonUnlocked", true);
                                    prefs.putInteger("current_cannon", 3);
                                    prefs.flush();
                                    menuAnimation = false;
                                }
                                break;
                        }
                        break;
                }
            }
        });

        noDisabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                no.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                no.setVisible(false);
                menuAnimation = false;
            }
        });

        upgradeDisabled.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                upgrade.setVisible(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                upgrade.setVisible(false);
                switch (current_category){
                    case(1):switch (current_engine){
                        case(1):
                            if(cogs>=2+(engine1upgradeLevel/3)){
                                cogs-=2+(engine1upgradeLevel/3);
                                prefs.putInteger("cogs", cogs);
                                engine1upgradeLevel++;
                                prefs.putInteger("engine1upgradeLevel",engine1upgradeLevel);
                                prefs.flush();
                                menuAnimation = false;
                            }
                            break;
                        case(2):
                            if(cogs>=2+(2*engine2upgradeLevel/3)){
                                cogs-=2+(2*engine2upgradeLevel/3);
                                prefs.putInteger("cogs", cogs);
                                engine2upgradeLevel++;
                                prefs.putInteger("engine2upgradeLevel",engine2upgradeLevel);
                                prefs.flush();
                                menuAnimation = false;
                            }
                            break;
                        case(3):
                            if(cogs>=3+(2*engine3upgradeLevel/3)){
                                cogs-=3+(2*engine3upgradeLevel/3);
                                prefs.putInteger("cogs", cogs);
                                engine3upgradeLevel++;
                                prefs.putInteger("engine3upgradeLevel",engine3upgradeLevel);
                                prefs.flush();
                                menuAnimation = false;
                            }
                            break;
                        }
                        break;
                    case(2):
                        switch (current_cannon){
                            case(1):
                                if(cogs>=2+(2*cannon1upgradeLevel/3)){
                                    cogs-=2+(2*cannon1upgradeLevel/3);
                                    prefs.putInteger("cogs", cogs);
                                    cannon1upgradeLevel++;
                                    prefs.putInteger("cannon1upgradeLevel",cannon1upgradeLevel);
                                    prefs.flush();
                                    menuAnimation = false;
                                }
                                break;
                            case(2):
                                if(cogs>=3+(2*cannon2upgradeLevel/3)){
                                    cogs-=3+(2*cannon2upgradeLevel/3);
                                    prefs.putInteger("cogs", cogs);
                                    cannon2upgradeLevel++;
                                    prefs.putInteger("cannon2upgradeLevel",cannon2upgradeLevel);
                                    prefs.flush();
                                    menuAnimation = false;
                                }
                                break;
                            case(3):
                                if(cogs>=4+(2*cannon3upgradeLevel/3)){
                                    cogs-=4+(2*cannon3upgradeLevel/3);
                                    prefs.putInteger("cogs", cogs);
                                    cannon3upgradeLevel++;
                                    prefs.putInteger("cannon3upgradeLevel",cannon3upgradeLevel);
                                    prefs.flush();
                                    menuAnimation = false;
                                }
                                break;
                        }
                        break;
                }
            }
        });

        CategoryEngine.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                current_category = 1;
                menu_offset = 350;
                menuAnimation = false;
                prefs.putInteger("current_category", current_category);
                prefs.flush();
                Hide(3);
            }
        });

        CategoryGun.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                current_category = 2;
                menu_offset = 350;
                menuAnimation = false;
                prefs.putInteger("current_category", current_category);
                prefs.flush();
                Hide(4);
            }
        });

        CategoryGun2.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                current_category = 3;
                menu_offset = 350;
                menuAnimation = false;
                prefs.putInteger("current_category", current_category);
                prefs.flush();
                Hide(5);
            }
        });

        Cannon1.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(current_cannon == 1){
                    menu_offset = 350;
                    menu_type = 2;
                    menuAnimation = true;
                }else{
                    menuAnimation = false;
                    menu_offset = 350;
                }
                current_cannon = 1;
                prefs.putInteger("current_cannon", 1);
                prefs.flush();
            }
        });

        Cannon2.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(current_cannon == 2){
                    menu_offset = 350;
                    if(is2ndCannonUnlocked) {
                        menu_type = 2;
                        menuAnimation = true;
                    }else{
                        menu_type = 1;
                        menuAnimation = true;
                    }
                }else{
                    menuAnimation = false;
                    menu_offset = 350;
                }
                current_cannon = 2;
                if(is2ndCannonUnlocked) {
                    prefs.putInteger("current_cannon", 2);
                    prefs.flush();
                }
            }
        });

        Cannon3.addListener(new InputListener(){
               @Override
               public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                   return true;
               }

               @Override
               public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                   if(current_cannon == 3){
                       menu_offset = 350;
                       if(is3rdCannonUnlocked) {
                           menu_type = 2;
                           menuAnimation = true;
                       }else{
                           menu_type = 1;
                           menuAnimation = true;
                       }
                   }else{
                       menuAnimation = false;
                       menu_offset = 350;
                   }
                   current_cannon = 3;
                   if(is3rdCannonUnlocked) {
                       prefs.putInteger("current_cannon", 3);
                       prefs.flush();
                   }
               }
           });

        buildNumber.addListener(new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                easterEgg = !easterEgg;
                prefs.putBoolean("easterEgg", easterEgg);
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
        if(movement> 2880){
            movement = 0;
        }

        batch.draw(Bg, 0, 0, movement, -240, 800, 720);

        fire.setPosition(230+ship_offset, 268);
        fire2.setPosition(224+ship_offset, 290);
        fire.draw(batch);
        fire.update(delta);
        fire2.draw(batch);
        fire2.update(delta);

        batch.draw(Ship, 220+ship_offset, 250, 76.8f, 57.6f);

        batch.end();
        ShopStage.draw();
        ShopStage.act(delta);
        batch.begin();

        if(Shop){
            batch.draw(shop, 5,70, 530, 400);

            switch(current_category){
                case(1):
                    switch (current_engine){
                        case(1):
                            batch.draw(shopButton_disabled, 35.5f, 224.7f, 153.5f, 70.3f);
                            batch.draw(shopButton_disabled, 35.5f, 299.96f, 153.5f, 70.3f);
                            batch.draw(shopButton_enabled, 35.5f, 375.02f, 153.5f, 70.3f);
                            if(money>=0) {
                                font_numbers.setColor(Color.GREEN);
                            }else{
                                font_numbers.setColor(Color.RED);
                            }
                            font_numbers.getData().setScale(0.3f);

                            font_numbers.draw(batch, ""+(2+(engine1upgradeLevel/3)), 51, 204, 100,1, false);
                            batch.draw(cog, 155, 177, 30, 30);

                            font_main.getData().setScale(0.27f);
                            font_main.setColor(Color.YELLOW);
                            font_main.draw(batch, "RD-170 engine (stock)", 62, 154, 100,1, false);
                            font_main.draw(batch, "Speed multiplier: "+String.format ("%.1f",(1+engine1upgradeLevel/10f))+"X", 62, 134, 100,1, false);
                            font_main.draw(batch, "Thrust: "+(7887*(1+engine1upgradeLevel/10f))+"kN", 62, 114, 100,1, false);
                            break;
                        case(2):
                            batch.draw(shopButton_disabled, 35.5f, 224.7f, 153.5f, 70.3f);
                            batch.draw(shopButton_enabled, 35.5f, 299.96f, 153.5f, 70.3f);
                            batch.draw(shopButton_disabled, 35.5f, 375.02f, 153.5f, 70.3f);
                            if(money>=1500) {
                                font_numbers.setColor(Color.GREEN);
                            }else{
                                font_numbers.setColor(Color.RED);
                            }
                            font_numbers.getData().setScale(0.3f);

                            if(!is2ndEngineUnlocked) {
                                font_numbers.draw(batch, "1500", 62, 204, 100, 1, false);
                            }else{
                                font_numbers.draw(batch, ""+(2+(int)(2*engine2upgradeLevel/2.6)), 51, 204, 100,1, false);
                                batch.draw(cog, 155, 177, 30, 30);
                            }

                            font_main.getData().setScale(0.27f);
                            font_main.setColor(Color.ORANGE);
                            font_main.draw(batch, "NK-33 nuclear engine", 62, 154, 100,1, false);
                            font_main.draw(batch, "Speed multiplier: "+String.format ("%.1f",(1.4f+engine2upgradeLevel/10f))+"X", 62, 134, 100,1, false);
                            font_main.draw(batch, "Thrust: "+(7887*(1.4f+engine2upgradeLevel/10f))+"kN", 62, 114, 100,1, false);
                            break;
                        case(3):
                            batch.draw(shopButton_enabled, 35.5f, 224.7f, 153.5f, 70.3f);
                            batch.draw(shopButton_disabled, 35.5f, 299.96f, 153.5f, 70.3f);
                            batch.draw(shopButton_disabled, 35.5f, 375.02f, 153.5f, 70.3f);
                            if(money>=3500) {
                                font_numbers.setColor(Color.GREEN);
                            }else{
                                font_numbers.setColor(Color.RED);
                            }
                            font_numbers.getData().setScale(0.3f);

                            if(!is3rdEngineUnlocked) {
                                font_numbers.draw(batch, "3500", 62, 204, 100, 1, false);
                            }else{
                                font_numbers.draw(batch, ""+(3+(int)(2*engine3upgradeLevel/2.3)), 51, 204, 100,1, false);
                                batch.draw(cog, 155, 177, 30, 30);
                            }

                            font_main.getData().setScale(0.27f);
                            font_main.setColor(Color.valueOf("#b37dfa"));
                            font_main.draw(batch, "F119 plasma engine", 62, 154, 100,1, false);
                            font_main.draw(batch, "Speed multiplier: "+String.format ("%.1f",(1.7f+engine3upgradeLevel/10f))+"X", 62, 134, 100,1, false);
                            font_main.draw(batch, "Thrust: "+(7887*(1.7f+engine3upgradeLevel/10f))+"kN", 62, 114, 100,1, false);
                            break;
                    }

                    switch (menu_type){
                        case(1):
                            batch.draw(menu_purchase2, 5+menu_offset, 70, 530, 400);
                            yes.setPosition(427+menu_offset, 175);
                            yesDisabled.setPosition(427+menu_offset, 175);
                            no.setPosition(234+menu_offset, 175);
                            noDisabled.setPosition(234+menu_offset, 175);
                            font_main.setColor(Color.RED);
                            font_main.getData().setScale(0.7f);
                            font_main.draw(batch,"NO", 225+menu_offset, 210, 100,1, false );
                            font_main.setColor(Color.GREEN);
                            font_main.draw(batch,"YES", 418+menu_offset, 210, 100,1, false );
                            if (current_engine == 2){
                                font_main.setColor(Color.GREEN);
                                font_main.getData().setScale(0.6f);
                                font_main.draw(batch, "Buy for 1500?", 320+menu_offset, 340, 100,1, false );
                                batch.draw(Engine2_t, 280+menu_offset, 250, 171.99f, 68.796f);
                            }else if (current_engine == 3){
                                font_main.setColor(Color.GREEN);
                                font_main.getData().setScale(0.6f);
                                font_main.draw(batch, "Buy for 3500?", 320+menu_offset, 340, 100,1, false );
                                batch.draw(Engine3_t, 280+menu_offset, 250, 171.99f, 68.796f);
                            }
                            break;
                        case(2):
                            upgrade.setPosition(232+menu_offset, 175);
                            upgradeDisabled.setPosition(232+menu_offset, 175);
                            batch.draw(menu_purchase, 5+menu_offset, 70, 530, 400);
                            font_main.setColor(Color.GREEN);
                            font_main.getData().setScale(0.7f);
                            font_main.draw(batch,"UPGRADE!", 325+menu_offset, 210, 100,1, false );
                            font_main.setColor(Color.GREEN);
                            font_main.getData().setScale(0.5f);
                            batch.draw(cog, 447+menu_offset, 320, 30, 30);
                            font_main.draw(batch, "?", 445+menu_offset, 340, 100,1, false );
                            switch (current_engine){
                                case(1):
                                    batch.draw(Engine1_t, 361+menu_offset, 270, 114.66f, 45.864f);
                                    font_main.draw(batch, "Upgrade for "+(2+(engine1upgradeLevel/3)), 292+menu_offset, 340, 100,1, false );
                                    font_main.draw(batch, "Level: "+(engine1upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    font_main.getData().setScale(0.28f);
                                    font_main.draw(batch, "Speed Multiplier: ", 254+menu_offset, 305, 100,1, false);
                                    font_main.draw(batch, ""+String.format ("%.1f",(1+engine1upgradeLevel/10f))+"-->"+String.format ("%.1f",(1.1f+engine1upgradeLevel/10f)), 254+menu_offset, 290, 100,1, false);
                                    break;
                                case(2):
                                    batch.draw(Engine2_t, 361+menu_offset, 270, 114.66f, 45.864f);
                                    font_main.draw(batch, "Upgrade for "+(2+(int)(2*engine2upgradeLevel/2.6)), 292+menu_offset, 340, 100,1, false );
                                    font_main.draw(batch, "Level: "+(engine2upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    font_main.getData().setScale(0.28f);
                                    font_main.draw(batch, "Speed Multiplier: ", 254+menu_offset, 305, 100,1, false);
                                    font_main.draw(batch, ""+String.format ("%.1f",(1.4f+engine2upgradeLevel/10f))+"-->"+String.format ("%.1f",(1.5f+engine2upgradeLevel/10f)), 254+menu_offset, 290, 100,1, false);
                                    break;
                                case(3):
                                    batch.draw(Engine3_t, 361+menu_offset, 270, 114.66f, 45.864f);
                                    font_main.draw(batch, "Upgrade for "+(3+(int)(2*engine3upgradeLevel/2.3)), 292+menu_offset, 340, 100,1, false );
                                    font_main.draw(batch, "Level: "+(engine3upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    font_main.getData().setScale(0.28f);
                                    font_main.draw(batch, "Speed Multiplier: ", 254+menu_offset, 305, 100,1, false);
                                    font_main.draw(batch, ""+String.format ("%.1f",(1.7f+engine3upgradeLevel/10f))+"-->"+String.format ("%.1f",(1.8f+engine3upgradeLevel/10f)), 254+menu_offset, 290, 100,1, false);
                                    break;
                            }
                            break;
                    }

                    batch.draw(shopButton_small_enabled, 238.5f, 399.5f, 73, 46);
                    batch.draw(shopButton_small_disabled, 335.5f, 399.5f, 73, 46);
                    batch.draw(shopButton_small_disabled, 432.5f, 399.5f, 73, 46);
                    break;
                case(2):
                    switch (current_cannon){
                        case(1):
                            batch.draw(shopButton_disabled, 35.5f, 224.7f, 153.5f, 70.3f);
                            batch.draw(shopButton_disabled, 35.5f, 299.96f, 153.5f, 70.3f);
                            batch.draw(shopButton_enabled, 35.5f, 375.02f, 153.5f, 70.3f);
                            if(money>=0) {
                                font_numbers.setColor(Color.GREEN);
                            }else{
                                font_numbers.setColor(Color.RED);
                            }
                            font_numbers.getData().setScale(0.3f);

                            font_numbers.draw(batch, ""+(2+(2*cannon1upgradeLevel/3)), 51, 204, 100,1, false);
                            batch.draw(cog, 155, 177, 30, 30);

                            font_main.getData().setScale(0.2f);
                            font_main.setColor(Color.YELLOW);
                            font_main.draw(batch, "Light Machine Gun (stock)", 62, 154, 100,1, false);
                            font_main.draw(batch, "Shooting rate multiplier: 1X", 62, 139, 100,1, false);
                            font_main.draw(batch, "Spread : "+MathUtils.clamp((1.5f-cannon1upgradeLevel*0.1f), 0.7f, 1.5f), 62, 124, 100,1, false);
                            font_main.draw(batch, "Damage : "+(40+cannon1upgradeLevel), 62, 109, 100,1, false);
                            break;
                        case(2):
                            batch.draw(shopButton_disabled, 35.5f, 224.7f, 153.5f, 70.3f);
                            batch.draw(shopButton_enabled, 35.5f, 299.96f, 153.5f, 70.3f);
                            batch.draw(shopButton_disabled, 35.5f, 375.02f, 153.5f, 70.3f);
                            if(money>=1500) {
                                font_numbers.setColor(Color.GREEN);
                            }else{
                                font_numbers.setColor(Color.RED);
                            }
                            font_numbers.getData().setScale(0.3f);

                            if(!is2ndCannonUnlocked) {
                                font_numbers.draw(batch, "2500", 62, 204, 100, 1, false);
                            }else{
                                font_numbers.draw(batch, ""+(3+(int)(2*cannon2upgradeLevel/2.6)), 51, 204, 100,1, false);
                                batch.draw(cog, 155, 177, 30, 30);
                            }

                            font_main.getData().setScale(0.2f);
                            font_main.setColor(Color.ORANGE);
                            font_main.draw(batch, "Heavy Machine Gun", 62, 154, 100,1, false);
                            font_main.draw(batch, "Shooting rate multiplier: 0.8X", 62, 139, 100,1, false);
                            font_main.draw(batch, "Spread : "+MathUtils.clamp((1.2f-cannon2upgradeLevel*0.1f), 0.7f, 1.5f), 62, 124, 100,1, false);
                            font_main.draw(batch, "Damage : "+(60+cannon2upgradeLevel), 62, 109, 100,1, false);
                            break;
                        case(3):
                            batch.draw(shopButton_enabled, 35.5f, 224.7f, 153.5f, 70.3f);
                            batch.draw(shopButton_disabled, 35.5f, 299.96f, 153.5f, 70.3f);
                            batch.draw(shopButton_disabled, 35.5f, 375.02f, 153.5f, 70.3f);
                            if(money>=3500) {
                                font_numbers.setColor(Color.GREEN);
                            }else{
                                font_numbers.setColor(Color.RED);
                            }
                            font_numbers.getData().setScale(0.3f);

                            if(!is3rdCannonUnlocked) {
                                font_numbers.draw(batch, "4500", 62, 204, 100, 1, false);
                            }else{
                                font_numbers.draw(batch, ""+(4+(int)(2*cannon3upgradeLevel/2.3)), 51, 204, 100,1, false);
                                batch.draw(cog, 155, 177, 30, 30);
                            }

                            font_main.getData().setScale(0.2f);
                            font_main.setColor(Color.SCARLET);
                            font_main.draw(batch, "Laser Gun", 62, 154, 100,1, false);
                            font_main.draw(batch, "Shooting rate multiplier: 1.3X", 62, 139, 100,1, false);
                            font_main.draw(batch, "Spread : "+MathUtils.clamp((0.8f-cannon3upgradeLevel*0.1f), 0.7f, 1.5f), 62, 124, 100,1, false);
                            font_main.draw(batch, "Damage : "+(70+cannon3upgradeLevel), 62, 109, 100,1, false);
                            break;
                    }

                    switch (menu_type){
                        case(1):
                            batch.draw(menu_purchase2, 5+menu_offset, 70, 530, 400);
                            yes.setPosition(427+menu_offset, 175);
                            yesDisabled.setPosition(427+menu_offset, 175);
                            no.setPosition(234+menu_offset, 175);
                            noDisabled.setPosition(234+menu_offset, 175);
                            font_main.setColor(Color.RED);
                            font_main.getData().setScale(0.7f);
                            font_main.draw(batch,"NO", 225+menu_offset, 210, 100,1, false );
                            font_main.setColor(Color.GREEN);
                            font_main.draw(batch,"YES", 418+menu_offset, 210, 100,1, false );
                            if (current_cannon == 2){
                                font_main.setColor(Color.GREEN);
                                font_main.getData().setScale(0.6f);
                                font_main.draw(batch, "Buy for 2500?", 320+menu_offset, 340, 100,1, false );
                                batch.draw(Cannon2_t, 310+menu_offset, 260, 142.07649f, 45.901638f);
                            }else if (current_cannon == 3){
                                font_main.setColor(Color.GREEN);
                                font_main.getData().setScale(0.6f);
                                font_main.draw(batch, "Buy for 4500?", 320+menu_offset, 340, 100,1, false );
                                batch.draw(Cannon3_t, 311+menu_offset, 260, 140.01207f, 45.866024f);
                            }
                            break;
                        case(2):
                            upgrade.setPosition(232+menu_offset, 175);
                            upgradeDisabled.setPosition(232+menu_offset, 175);
                            batch.draw(menu_purchase, 5+menu_offset, 70, 530, 400);
                            font_main.setColor(Color.GREEN);
                            font_main.getData().setScale(0.7f);
                            font_main.draw(batch,"UPGRADE!", 325+menu_offset, 210, 100,1, false );
                            font_main.setColor(Color.GREEN);
                            font_main.getData().setScale(0.4f);
                            batch.draw(cog, 445+menu_offset, 320, 30, 30);
                            font_main.draw(batch, "?", 445+menu_offset, 340, 100,1, false );
                            switch (current_cannon){
                                case(1):
                                batch.draw(Cannon1_t, 361+menu_offset, 270, 130.14354f, 45.933018f);
                                font_main.draw(batch, "Upgrade for "+(2+(2*cannon1upgradeLevel/3)), 290+menu_offset, 340, 100,1, false );
                                font_main.getData().setScale(0.26f);
                                font_main.draw(batch, "Damage: "+(40+cannon1upgradeLevel)+"-->"+(40+cannon1upgradeLevel+1), 252+menu_offset, 310, 100,1, false);
                                font_main.draw(batch, "Spread: "+MathUtils.clamp((1.5f-cannon1upgradeLevel*0.1f), 0.7f, 1.5f)+"-->"+MathUtils.clamp((1.5f-(cannon1upgradeLevel+1)*0.1f), 0.7f, 1.5f), 252+menu_offset, 290, 100,1, false);
                                font_main.getData().setScale(0.4f);
                                font_main.draw(batch, "Level: "+(cannon1upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    break;
                                case(2):
                                batch.draw(Cannon2_t, 361+menu_offset, 270, 142.07649f, 45.901638f);
                                font_main.draw(batch, "Upgrade for "+(3+(int)(2*cannon2upgradeLevel/2.6)), 290+menu_offset, 340, 100,1, false );
                                font_main.getData().setScale(0.26f);
                                font_main.draw(batch, "Damage: "+(60+cannon2upgradeLevel)+"-->"+(60+cannon2upgradeLevel+1), 252+menu_offset, 310, 100,1, false);
                                font_main.draw(batch, "Spread: "+MathUtils.clamp((1.2f-cannon2upgradeLevel*0.1f), 0.7f, 1.5f)+"-->"+MathUtils.clamp((1.2f-(cannon2upgradeLevel+1)*0.1f), 0.7f, 1.5f), 252+menu_offset, 290, 100,1, false);
                                font_main.getData().setScale(0.4f);
                                font_main.draw(batch, "Level: "+(cannon2upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    break;
                                case(3):
                                batch.draw(Cannon3_t, 361+menu_offset, 270, 140.01207f, 45.866024f);
                                font_main.draw(batch, "Upgrade for "+(4+(int)(2*cannon3upgradeLevel/2.3)), 290+menu_offset, 340, 100,1, false);
                                font_main.getData().setScale(0.26f);
                                font_main.draw(batch, "Damage: "+(70+cannon3upgradeLevel)+"-->"+(70+cannon3upgradeLevel+1), 252+menu_offset, 310, 100,1, false);
                                font_main.draw(batch, "Spread: "+MathUtils.clamp((0.8f-cannon3upgradeLevel*0.1f), 0.7f, 1.5f)+"-->"+MathUtils.clamp((0.8f-(cannon3upgradeLevel+1)*0.1f), 0.7f, 1.5f), 252+menu_offset, 290, 100,1, false);
                                font_main.getData().setScale(0.4f);
                                font_main.draw(batch, "Level: "+(cannon3upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    break;
                            }
                            break;
                    }

                    batch.draw(shopButton_small_disabled, 238.5f, 399.5f, 73, 46);
                    batch.draw(shopButton_small_enabled, 335.5f, 399.5f, 73, 46);
                    batch.draw(shopButton_small_disabled, 432.5f, 399.5f, 73, 46);
                    break;
                case(3):
                    batch.draw(shopButton_small_disabled, 238.5f, 399.5f, 73, 46);
                    batch.draw(shopButton_small_disabled, 335.5f, 399.5f, 73, 46);
                    batch.draw(shopButton_small_enabled, 432.5f, 399.5f, 73, 46);
                    break;
            }

            font_numbers.setColor(Color.CYAN);
            font_numbers.getData().setScale(0.4f);
            if(menu_type == 2) {
                font_numbers.draw(batch, "" + cogs, 245, 133, 100, 1, false);
                batch.draw(cog, 350, 103, 30, 30);
            }else{
                font_numbers.draw(batch, "" + money, 260, 133, 100, 1, false);
            }

            for(int i = 0; i<7; i++) {
                if (menuAnimation && menu_offset > 0) {
                    menu_offset -= 1;
                } else if (!menuAnimation && menu_offset < 350) {
                    menu_offset += 1;
                }
            }

            if(ship_offset<100){
                ship_offset++;
            }

        }else if(ship_offset>0){
            ship_offset--;
        }

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
            font_main.draw(batch, "LumixLab, Watermelon0guy, PYTHØN", 5, 275, 531, 1, false);
            font_main.draw(batch, "Ha4upelmeney, Lukmanov", 5, 245, 531, 1, false);
            font_main.setColor(Color.valueOf("#0FE500"));
            font_main.draw(batch, "Contributors: Volkov, DefenseX", 5, 210, 531, 1, false);
            font_main.draw(batch, "Zsingularityz", 5, 176, 531, 1, false);
            font_main.setColor(Color.valueOf("#CAE500"));
            font_main.draw(batch, "Deltacore", 5, 135, 531, 1, false);
            font_main.draw(batch,"® All right reserved", 5, 95, 531, 1, false);
        }

        if(settings) {
            batch.draw(infoBg, 5, 65, 531, 410);
            font_main.getData().setScale(0.5f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "ø¤º°°º¤ø,¸¸,ø¤[ Settings ]¤ø,¸¸,ø¤º°°º¤ø", 5, 460, 531, 1, false);
            font_main.setColor(Color.valueOf("#0FE500"));
            MusicVolume = (int) (musicVolume.getValue() * 100);
            SoundVolume = (int) (soundEffectsVolume.getValue() * 100);
            font_main.draw(batch, "Music: " + MusicVolume + "%", 95, 420, 132, 1, false);
            font_main.draw(batch, "Sound effects: " + SoundVolume + "%", 95, 370, 132, 1, false);
            font_main.draw(batch, "Show Fps", 65, 320, 132, 1, false);
            uiScale = (int) (uiScaling.getValue() * 100);
            font_main.draw(batch, "Ui Scaling: " + uiScale + " %", 325, 263, 132, 1, false);
            font_main.draw(batch, "(In game)", 325, 233, 132, 1, false);
            font_main.draw(batch, "Semi-transparent UI", 140, 128, 132, 1, false);
            font_main.draw(batch, "Enable shaders", 107, 178, 132, 1, false);
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
        font_main.draw(batch, "V 0.0.2 Build 5", 5, 35, 150, 1, false);
        if(easterEgg){
            font_main.getData().setScale(0.2f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "cat edition", 5, 20, 150, 1, false);
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
        assetManager.unload("ship.png");

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

        assetManager.unload("shop/main.png");
        assetManager.unload("shop/button_small.png");
        assetManager.unload("shop/button_small_enabled.png");
        assetManager.unload("shop/button_tiny.png");
        assetManager.unload("shop/button_tiny_enabled.png");

        assetManager.unload("shop/engine1.png");
        assetManager.unload("shop/engine2.png");
        assetManager.unload("shop/engine3.png");

        assetManager.unload("shop/menuBuy.png");
        assetManager.unload("shop/menuBuy2.png");

        assetManager.unload("shop/yes.png");
        assetManager.unload("shop/yesDisabled.png");
        assetManager.unload("shop/no.png");
        assetManager.unload("shop/noDisabled.png");
        assetManager.unload("shop/upgrade.png");
        assetManager.unload("shop/upgradeDisabled.png");

        assetManager.unload("shop/CategoryGun.png");
        assetManager.unload("shop/CategoryGun2.png");
        assetManager.unload("shop/CategoryEngine.png");
        assetManager.unload("shop/Cannon1.png");
        assetManager.unload("shop/Cannon2.png");
        assetManager.unload("shop/Cannon3.png");

        Menu.dispose();
        ShopStage.dispose();
        music.dispose();
        checkBoxSkin.dispose();
        sliderBarSkin.dispose();
        font_main.dispose();
        font_numbers.dispose();
        fire.dispose();
        fire2.dispose();
        MenuBg.dispose();
        Bg.dispose();
        shop.dispose();
        shopButton_small_enabled.dispose();
        shopButton_small_disabled.dispose();
        shopButton_enabled.dispose();
        shopButton_disabled.dispose();
        infoBg.dispose();
        menu_purchase.dispose();
        menu_purchase2.dispose();
        Engine1_t.dispose();
        Engine2_t.dispose();
        Engine3_t.dispose();
        Cannon1_t.dispose();
        Cannon2_t.dispose();
        Cannon3_t.dispose();
    }

   private void Hide(int type){
           switch (type){
               case(0):
                   fps.setVisible(false);
                   shaders.setVisible(false);
                   uiScaling.setVisible(false);
                   musicVolume.setVisible(false);
                   soundEffectsVolume.setVisible(false);
                   difficultyControl.setVisible(false);
                   newGame_disabled.setVisible(false);
                   continue_disabled.setVisible(false);
                   shop_disabled.setVisible(false);
                   transparency.setVisible(false);
                   Engine1.setVisible(false);
                   Engine2.setVisible(false);
                   Engine3.setVisible(false);
                   yesDisabled.setVisible(false);
                   noDisabled.setVisible(false);
                   upgradeDisabled.setVisible(false);
                   CategoryGun.setVisible(false);
                   CategoryGun2.setVisible(false);
                   CategoryEngine.setVisible(false);
                   Cannon1.setVisible(false);
                   Cannon2.setVisible(false);
                   Cannon3.setVisible(false);
                   break;
               case(1):
                   Hide(0);
                   fps.setVisible(true);
                   shaders.setVisible(true);
                   uiScaling.setVisible(true);
                   musicVolume.setVisible(true);
                   soundEffectsVolume.setVisible(true);
                   transparency.setVisible(true);
                   break;
               case(2):
                   Hide(0);
                   difficultyControl.setVisible(true);
                   newGame_disabled.setVisible(true);
                   continue_disabled.setVisible(true);
                   shop_disabled.setVisible(true);
                   break;
               case(3):
                   Hide(0);
                   CategoryGun.setVisible(true);
                   CategoryGun2.setVisible(true);
                   CategoryEngine.setVisible(true);
                   Engine1.setVisible(true);
                   Engine2.setVisible(true);
                   Engine3.setVisible(true);
                   yesDisabled.setVisible(true);
                   noDisabled.setVisible(true);
                   upgradeDisabled.setVisible(true);
                   break;
               case(4):
                   Hide(0);
                   CategoryGun.setVisible(true);
                   CategoryGun2.setVisible(true);
                   CategoryEngine.setVisible(true);
                   Cannon1.setVisible(true);
                   Cannon2.setVisible(true);
                   Cannon3.setVisible(true);
                   yesDisabled.setVisible(true);
                   noDisabled.setVisible(true);
                   upgradeDisabled.setVisible(true);
                   break;
               case(5):
                   Hide(0);
                   CategoryGun.setVisible(true);
                   CategoryGun2.setVisible(true);
                   CategoryEngine.setVisible(true);
                   yesDisabled.setVisible(true);
                   noDisabled.setVisible(true);
                   upgradeDisabled.setVisible(true);
                   break;
           }
    }

    private void UpdateFire(){

        fire.dispose();
        fire2.dispose();
        fire = new ParticleEffect();
        fire2 = new ParticleEffect();

        switch (current_engine){
            case(1):
                fire.load(Gdx.files.internal("particles/fire_engileleft_red_green.p"), Gdx.files.internal("particles"));
                fire2.load(Gdx.files.internal("particles/fire_engileleft_red_green.p"), Gdx.files.internal("particles"));
                break;
            case(2):
                fire.load(Gdx.files.internal("particles/fire_engileleft_red_purple.p"), Gdx.files.internal("particles"));
                fire2.load(Gdx.files.internal("particles/fire_engileleft_red_purple.p"), Gdx.files.internal("particles"));
                break;
            case(3):
                fire.load(Gdx.files.internal("particles/fire_engileleft_blue_purple.p"), Gdx.files.internal("particles"));
                fire2.load(Gdx.files.internal("particles/fire_engileleft_blue_purple.p"), Gdx.files.internal("particles"));
                break;
        }

        fire.setPosition(330, 268);
        fire.start();
        fire2.setPosition(324, 290);
        fire2.start();
    }
}