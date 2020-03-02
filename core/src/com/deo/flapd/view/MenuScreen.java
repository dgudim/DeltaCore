package com.deo.flapd.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.utils.DUtils;
import com.deo.flapd.utils.postprocessing.PostProcessor;

public class MenuScreen implements Screen{

    private float number;
    private boolean lamp_animation;

    private SpriteBatch batch;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Image MenuBg;
    private Texture Bg;
    private Texture Ship;
    private Image FillTexture;
    private Image buildNumber;
    private TextButton infoButton;
    private TextButton moreButton;
    private TextButton playButton;
    private TextButton settingsButton;
    private TextButton onlineButton;
    private TextButton workshopButton;

    private TextButton continueGameButton;
    private TextButton shopButton;
    private TextButton newGameButton;

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
    private TextButton yes, no, upgrade;

    private Image CategoryGun;
    private Image CategoryGun2;
    private Image CategoryEngine;
    private Image Cannon1;
    private Image Cannon2;
    private Image Cannon3;

    private Button trelloLink;
    private Button gitHubLink;
    private Button secretCode;
    private TextButton musicLink;

    private Image infoBg;

    private Image Lamp;

    private CheckBox fps;
    private CheckBox transparency;
    private CheckBox bloom;
    private CheckBox logging;
    private Slider uiScaling;
    private Slider musicVolume;
    private Slider soundEffectsVolume;
    private Slider difficultyControl;
    private float uiScale;

    private BitmapFont font_main, font_numbers, font_buttons;

    private boolean info;
    private boolean settings;
    private boolean play;
    private boolean Music;
    private boolean Shop;
    private boolean more;
    private boolean crafting;
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

    private ParticleEffect fire, fire2;

    private int current_engine, current_cannon, current_category, money, cogs, ship_offset, menu_offset, menu_type, engine1upgradeLevel, engine2upgradeLevel, engine3upgradeLevel, cannon1upgradeLevel, cannon2upgradeLevel, cannon3upgradeLevel;

    private boolean menuAnimation;

    private boolean is2ndEngineUnlocked, is3rdEngineUnlocked, is2ndCannonUnlocked, is3rdCannonUnlocked;

    private boolean easterEgg, easterEgg_unlocked;

    private int easterEggCounter;

    private final int fillingThreshold = 7;

    private PostProcessor blurProcessor;

    private boolean enableShader;

    private SlotManager slotManager;

       public MenuScreen(final Game game, final SpriteBatch batch, final AssetManager assetManager, final PostProcessor blurProcessor){

        this.game = game;

        this.blurProcessor = blurProcessor;

        menu_offset = 1000;

        uiScale = DUtils.getFloat("ui");
        MusicVolume = (int)(DUtils.getFloat("musicVolume")*100);
        SoundVolume = (int)(DUtils.getFloat("soundEffectsVolume")*100);

        if(MusicVolume > 0) {
            Music = true;
        }

        if(SoundVolume > 0) {
            Sound = true;
        }

        current_engine = DUtils.getInteger("current_engine");
        if(current_engine<1){
            DUtils.putInteger("current_engine", 1);
            current_engine = 1;
        }

        current_cannon = DUtils.getInteger("current_cannon");
        if(current_cannon<1){
            DUtils.putInteger("current_cannon", 1);
            current_cannon = 1;
        }

        current_category = DUtils.getInteger("current_category");
        if(current_category<1){
            DUtils.putInteger("current_category", 1);
            current_category = 1;
        }

        money = DUtils.getInteger("money");
        cogs = DUtils.getInteger("cogs");
        easterEgg = DUtils.getBoolean("easterEgg");
        easterEgg_unlocked = DUtils.getBoolean("easterEgg_unlocked");

        engine1upgradeLevel = DUtils.getInteger("engine1upgradeLevel");
        engine2upgradeLevel = DUtils.getInteger("engine2upgradeLevel");
        engine3upgradeLevel = DUtils.getInteger("engine3upgradeLevel");
        cannon1upgradeLevel = DUtils.getInteger("cannon1upgradeLevel");
        cannon2upgradeLevel = DUtils.getInteger("cannon2upgradeLevel");
        cannon3upgradeLevel = DUtils.getInteger("cannon3upgradeLevel");

        is2ndEngineUnlocked = DUtils.getBoolean("is2ndEngineUnlocked");
        is3rdEngineUnlocked = DUtils.getBoolean("is3rdEngineUnlocked");
        is2ndCannonUnlocked = DUtils.getBoolean("is2ndCannonUnlocked");
        is3rdCannonUnlocked = DUtils.getBoolean("is3rdCannonUnlocked");

        this.batch = batch;

        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);

        MenuBg = new Image((Texture)assetManager.get("menuBg.png"));
        Lamp = new Image((Texture)(assetManager.get("lamp.png")));
        infoBg = new Image((Texture)assetManager.get("infoBg.png"));
        infoBg.setBounds(5, 65, 531, 410);
        MenuBg.setBounds(0, 0, 800, 480);

        font_main = assetManager.get("fonts/font2(old).fnt");
        font_numbers = assetManager.get("fonts/font.fnt");
        font_buttons = assetManager.get("fonts/font2.fnt");

        Bg = assetManager.get("bg_old.png");
        Bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        FillTexture = new Image((Texture)assetManager.get("menuFill.png"));
        FillTexture.setSize(456, 72);

        Ship = assetManager.get("ship.png");

        menu_purchase = assetManager.get("shop/menuBuy.png");
        menu_purchase2 = assetManager.get("shop/menuBuy2.png");

        buildNumber = new Image((Texture)assetManager.get("greyishButton.png"));

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

        buildNumber.setBounds(5,5,150, 50);

        Engine3.setBounds(54.57f, 235.39801f, 114.66f, 45.864f);
        Engine2.setBounds(54.57f, 312.058f, 114.66f, 45.864f);
        Engine1.setBounds(54.57f, 388.71802f, 114.66f, 45.864f);

        Cannon1.setBounds(47, 388.71802f, 130.14354f, 45.933018f);
        Cannon2.setBounds(42, 312.058f, 142.07649f, 45.901638f);
        Cannon3.setBounds(43, 235.39801f, 140.01207f, 45.866024f);

        CategoryEngine.setBounds(245, 400, 60, 46);
        CategoryGun.setBounds(337, 384, 75, 75);
        CategoryGun2.setBounds(445, 400, 46, 46);

        Lamp.setBounds(730, 430, 15, 35);

        Skin buttonSkin = new Skin();
        buttonSkin.addRegions((TextureAtlas)assetManager.get("menuButtons/menuButtons.atlas"));
        buttonSkin.addRegions((TextureAtlas)assetManager.get("menuButtons/buttons.atlas"));
        buttonSkin.addRegions((TextureAtlas)assetManager.get("shop/shopButtons.atlas"));

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = font_buttons;
        textButtonStyle.downFontColor = Color.valueOf("#22370E");
        textButtonStyle.overFontColor = Color.valueOf("#3D51232");
        textButtonStyle.fontColor = Color.valueOf("#3D4931");
        textButtonStyle.over = buttonSkin.getDrawable("blank_over");
        textButtonStyle.down = buttonSkin.getDrawable("blank_enabled");
        textButtonStyle.up = buttonSkin.getDrawable("button_blank");

        TextButton.TextButtonStyle textButtonStyle2 = new TextButton.TextButtonStyle();
        textButtonStyle2.font = font_buttons;
        textButtonStyle2.downFontColor = Color.valueOf("#31FF25");
        textButtonStyle2.overFontColor = Color.valueOf("#00DC00");
        textButtonStyle2.fontColor = Color.valueOf("#46D33E");
        textButtonStyle2.over = buttonSkin.getDrawable("blank2_over");
        textButtonStyle2.down = buttonSkin.getDrawable("blank2_enabled");
        textButtonStyle2.up = buttonSkin.getDrawable("blank2_disabled");

        TextButton.TextButtonStyle textButtonStyle_music = new TextButton.TextButtonStyle();
        textButtonStyle_music.font = font_main;
        textButtonStyle_music.downFontColor = Color.valueOf("#00bdbd");
        textButtonStyle_music.overFontColor = Color.valueOf("#00dbdb");
        textButtonStyle_music.fontColor = Color.CYAN;

        Button.ButtonStyle buttonStyle_code = new TextButton.TextButtonStyle();
        buttonStyle_code.up = buttonSkin.getDrawable("secretCode_disabled");
        buttonStyle_code.over = buttonSkin.getDrawable("secretCode_over");
        buttonStyle_code.down = buttonSkin.getDrawable("secretCode_enabled");

        Button.ButtonStyle buttonStyle_git = new TextButton.TextButtonStyle();
        buttonStyle_git.up = buttonSkin.getDrawable("gitHub_disabled");
        buttonStyle_git.over = buttonSkin.getDrawable("gitHub_over");
        buttonStyle_git.down = buttonSkin.getDrawable("gitHub_enabled");

        Button.ButtonStyle buttonStyle_trello = new TextButton.TextButtonStyle();
        buttonStyle_trello.up = buttonSkin.getDrawable("trello_disabled");
        buttonStyle_trello.over = buttonSkin.getDrawable("trello_over");
        buttonStyle_trello.down = buttonSkin.getDrawable("trello_enabled");

        TextButton.TextButtonStyle textButtonStyle_shop_no = new TextButton.TextButtonStyle();
        textButtonStyle_shop_no.up = buttonSkin.getDrawable("noDisabled");
        textButtonStyle_shop_no.down = buttonSkin.getDrawable("no");
        textButtonStyle_shop_no.over = buttonSkin.getDrawable("noOver");
        textButtonStyle_shop_no.font = font_buttons;
        textButtonStyle_shop_no.downFontColor = Color.RED;
        textButtonStyle_shop_no.overFontColor = Color.valueOf("#DD0000");
        textButtonStyle_shop_no.fontColor = Color.SCARLET;

        TextButton.TextButtonStyle textButtonStyle_shop_yes = new TextButton.TextButtonStyle();
        textButtonStyle_shop_yes.up = buttonSkin.getDrawable("yesDisabled");
        textButtonStyle_shop_yes.down = buttonSkin.getDrawable("yes");
        textButtonStyle_shop_yes.over = buttonSkin.getDrawable("yesOver");
        textButtonStyle_shop_yes.font = font_buttons;
        textButtonStyle_shop_yes.downFontColor = Color.GREEN;
        textButtonStyle_shop_yes.overFontColor = Color.valueOf("#00DD00");
        textButtonStyle_shop_yes.fontColor = Color.LIME;

        yes = new TextButton("yes",textButtonStyle_shop_yes);
        yes.getLabel().setFontScale(0.45f);
        no = new TextButton("no",textButtonStyle_shop_no);
        no.getLabel().setFontScale(0.45f);
        upgrade = new TextButton("upgrade",textButtonStyle_shop_yes);
        upgrade.getLabel().setFontScale(0.45f);

        yes.setBounds(-100, -100, 83.2f, 57.2f);
        no.setBounds(-100, -100, 83.2f, 57.2f);
        upgrade.setBounds(-100, -100, 300.8f, 57.2f);

        gitHubLink = new Button(buttonStyle_git);
        trelloLink = new Button(buttonStyle_trello);
        secretCode = new Button(buttonStyle_code);
        trelloLink.setBounds(15, 350, 50, 50);
        gitHubLink.setBounds(15, 410, 50, 50);
        secretCode.setBounds(15, 290, 50, 50);

        workshopButton = new TextButton("Workshop" , textButtonStyle);
        workshopButton.setBounds(343, 330, 160, 50);
        workshopButton.getLabel().setFontScale(0.45f);

        continueGameButton = new TextButton("Continue" , textButtonStyle);
        continueGameButton.setBounds(180, 385, 160, 44);
        continueGameButton.getLabel().setFontScale(0.45f);

        newGameButton = new TextButton("New game" , textButtonStyle);
        newGameButton.setBounds(17, 385, 160, 44);
        newGameButton.getLabel().setFontScale(0.4f, 0.45f);

        shopButton = new TextButton("Shop" , textButtonStyle);
        shopButton.setBounds(343, 385, 160, 44);
        shopButton.getLabel().setFontScale(0.45f);

        infoButton = new TextButton("Info" , textButtonStyle2);
        infoButton.setBounds(545, 85, 250, 75);
        infoButton.getLabel().setFontScale(0.6f);

        moreButton = new TextButton("More" , textButtonStyle2);
        moreButton.setBounds(545, 5, 250, 75);
        moreButton.getLabel().setFontScale(0.6f);

        playButton = new TextButton("Play" , textButtonStyle2);
        playButton.setBounds(545, 325, 250, 75);
        playButton.getLabel().setFontScale(0.6f);

        settingsButton = new TextButton("Settings" , textButtonStyle2);
        settingsButton.setBounds(545, 165, 250, 75);
        settingsButton.getLabel().setFontScale(0.6f);

        onlineButton = new TextButton("Online" , textButtonStyle2);
        onlineButton.setBounds(545, 245, 250, 75);
        onlineButton.getLabel().setFontScale(0.6f);

        musicLink = new  TextButton("Evan King" , textButtonStyle_music);
        musicLink.setBounds(262, 359, 130, 30);
        musicLink.getLabel().setFontScale(0.48f);

        Skin checkBoxSkin = new Skin();
        checkBoxSkin.add("off", assetManager.get("checkBox_disabled.png"));
        checkBoxSkin.add("on", assetManager.get("checkBox_enabled.png"));
        checkBoxSkin.add("off_over", assetManager.get("checkBox_disabled_over.png"));
        checkBoxSkin.add("on_over", assetManager.get("checkBox_enabled_over.png"));

        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOff = checkBoxSkin.getDrawable("off");
        checkBoxStyle.checkboxOn = checkBoxSkin.getDrawable("on");
        checkBoxStyle.checkboxOnOver = checkBoxSkin.getDrawable("on_over");
        checkBoxStyle.checkboxOver = checkBoxSkin.getDrawable("off_over");
        checkBoxStyle.font = font_main;

        Skin sliderBarSkin = new Skin();
        sliderBarSkin.add("knob", assetManager.get("progressBarKnob.png"));
        sliderBarSkin.add("knob2", assetManager.get("progressBarKnob.png"));
        sliderBarSkin.add("bg", assetManager.get("progressBarBg.png"));
        sliderBarSkin.add("bg2", assetManager.get("progressBarBg.png"));
        sliderBarSkin.add("knob_over", assetManager.get("progressBarKnob_over.png"));
        sliderBarSkin.add("knob_over2", assetManager.get("progressBarKnob_over.png"));
        sliderBarSkin.add("knob_enabled", assetManager.get("progressBarKnob_enabled.png"));
        sliderBarSkin.add("knob_enabled2", assetManager.get("progressBarKnob_enabled.png"));

        Slider.SliderStyle sliderBarStyle = new Slider.SliderStyle();
        sliderBarStyle.background = sliderBarSkin.getDrawable("bg");
        sliderBarStyle.knob = sliderBarSkin.getDrawable("knob");
        sliderBarStyle.knobOver = sliderBarSkin.getDrawable("knob_over");
        sliderBarStyle.knobDown = sliderBarSkin.getDrawable("knob_enabled");
        sliderBarStyle.knob.setMinHeight(62.5f);
        sliderBarStyle.knob.setMinWidth(37.5f);
        sliderBarStyle.knobOver.setMinHeight(62.5f);
        sliderBarStyle.knobOver.setMinWidth(37.5f);
        sliderBarStyle.knobDown.setMinHeight(62.5f);
        sliderBarStyle.knobDown.setMinWidth(37.5f);
        sliderBarStyle.background.setMinHeight(62.5f);
        sliderBarStyle.background.setMinWidth(250.0f);

        Slider.SliderStyle sliderBarStyle2 = new Slider.SliderStyle();
        sliderBarStyle2.background = sliderBarSkin.getDrawable("bg2");
        sliderBarStyle2.knob = sliderBarSkin.getDrawable("knob2");
        sliderBarStyle2.knobOver = sliderBarSkin.getDrawable("knob_over2");
        sliderBarStyle2.knobDown = sliderBarSkin.getDrawable("knob_enabled2");
        sliderBarStyle2.knob.setMinHeight(42.5f);
        sliderBarStyle2.knob.setMinWidth(27.5f);
        sliderBarStyle2.knobOver.setMinHeight(42.5f);
        sliderBarStyle2.knobOver.setMinWidth(27.5f);
        sliderBarStyle2.knobDown.setMinHeight(42.5f);
        sliderBarStyle2.knobDown.setMinWidth(27.5f);
        sliderBarStyle2.background.setMinHeight(42.5f);
        sliderBarStyle2.background.setMinWidth(230.0f);

        fps = new CheckBox("", checkBoxStyle);
        transparency = new CheckBox("", checkBoxStyle);
        bloom = new CheckBox("", checkBoxStyle);
        logging = new CheckBox("", checkBoxStyle);

        uiScaling = new Slider(1, 2, 0.1f, false, sliderBarStyle);
        musicVolume = new Slider(0, 1, 0.1f, false, sliderBarStyle2);
        soundEffectsVolume = new Slider(0, 1, 0.1f, false, sliderBarStyle2);
        difficultyControl = new Slider(1, 5, 0.1f, false, sliderBarStyle2);

        uiScaling.setValue(DUtils.getFloat("ui"));
        musicVolume.setValue(DUtils.getFloat("musicVolume"));
        soundEffectsVolume.setValue(DUtils.getFloat("soundEffectsVolume"));
        difficultyControl.setValue(DUtils.getFloat("difficulty"));
        fps.setChecked(DUtils.getBoolean("showFps"));
        transparency.setChecked(DUtils.getBoolean("transparency"));
        bloom.setChecked(DUtils.getBoolean("bloom"));
        logging.setChecked(DUtils.getBoolean("logging"));

        fps.setBounds(13, 290, 50, 50);
        uiScaling.setBounds(10, 220, 250, 40);
        soundEffectsVolume.setBounds(310, 350, 170, 25);
        difficultyControl.setBounds(20, 300, 170, 25);
        musicVolume.setBounds(310, 400, 170, 25);
        transparency.setBounds(13,100,50,50);
        bloom.setBounds(200, 290, 50, 50);
        logging.setBounds(340, 290, 50, 50);
        fps.getImage().setScaling(Scaling.fill);
        transparency.getImage().setScaling(Scaling.fill);
        bloom.getImage().setScaling(Scaling.fill);
        logging.getImage().setScaling(Scaling.fill);

        Menu = new Stage(viewport, batch);

        Menu.addActor(playButton);
        Menu.addActor(onlineButton);
        Menu.addActor(settingsButton);
        Menu.addActor(infoButton);
        Menu.addActor(moreButton);
        Menu.addActor(buildNumber);

        fps.setVisible(false);
        uiScaling.setVisible(false);
        musicVolume.setVisible(false);
        soundEffectsVolume.setVisible(false);
        difficultyControl.setVisible(false);
        transparency.setVisible(false);
        bloom.setVisible(false);
        logging.setVisible(false);

        Menu.addActor(fps);
        Menu.addActor(uiScaling);
        Menu.addActor(musicVolume);
        Menu.addActor(soundEffectsVolume);
        Menu.addActor(difficultyControl);
        Menu.addActor(transparency);
        Menu.addActor(bloom);
        Menu.addActor(logging);
        Menu.addActor(trelloLink);
        Menu.addActor(gitHubLink);
        Menu.addActor(secretCode);
        Menu.addActor(musicLink);

        Menu.addActor(newGameButton);
        Menu.addActor(continueGameButton);
        Menu.addActor(shopButton);
        Menu.addActor(workshopButton);

        Menu.addActor(Engine1);
        Menu.addActor(Engine2);
        Menu.addActor(Engine3);
        Menu.addActor(Cannon1);
        Menu.addActor(Cannon2);
        Menu.addActor(Cannon3);
        Menu.addActor(CategoryEngine);
        Menu.addActor(CategoryGun);
        Menu.addActor(CategoryGun2);

        slotManager = new SlotManager(assetManager);
        slotManager.addSlots("items");
        slotManager.setBounds(90, 70, 440, 400);
        slotManager.attach(Menu);
        slotManager.setVisible(false);

        ShopStage = new Stage(viewport, batch);

        ShopStage.addActor(yes);
        ShopStage.addActor(no);
        ShopStage.addActor(upgrade);

        newGameButton.setVisible(false);
        continueGameButton.setVisible(false);
        shopButton.setVisible(false);
        workshopButton.setVisible(false);
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

        trelloLink.setVisible(false);
        gitHubLink.setVisible(false);
        secretCode.setVisible(false);
        musicLink.setVisible(false);

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

        playButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                    if (play) {
                        play = false;
                        Hide(0);
                    } else {
                        play = true;
                        info = false;
                        settings = false;
                        Shop = false;
                        more = false;
                        crafting = false;
                        Hide(2);
                    }
            }
        });

        onlineButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {

            }
        });

        settingsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                    if (settings) {
                        settings = false;
                        Hide(0);
                    } else {
                        settings = true;
                        info = false;
                        play = false;
                        Shop = false;
                        more = false;
                        crafting = false;
                        Hide(1);
                    }
            }
        });

        infoButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (info) {
                    info = false;
                    Hide(0);
                } else {
                    info = true;
                    settings = false;
                    play = false;
                    Shop = false;
                    more = false;
                    crafting = false;
                    Hide(8);
                }
            }
        });

        moreButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                    if (more) {
                        more = false;
                        Hide(0);
                    } else {
                        more = true;
                        info = false;
                        settings = false;
                        play = false;
                        Shop = false;
                        crafting = false;
                        Hide(6);
                    }
            }
        });

        uiScaling.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                DUtils.putFloat("ui", uiScaling.getValue());
            }
        });

        fps.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                DUtils.putBoolean("showFps", fps.isChecked());
            }

        });

        soundEffectsVolume.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                DUtils.putFloat("soundEffectsVolume", soundEffectsVolume.getValue());
            }
        });

        musicVolume.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                DUtils.putFloat("musicVolume", musicVolume.getValue());

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
                DUtils.putFloat("difficulty", (float)((int)(difficultyControl.getValue()*100))/100);
            }
        });

        newGameButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                /*
                DUtils.putInteger("enemiesKilled",0);
                DUtils.putInteger("moneyEarned",0);
                DUtils.putInteger("enemiesSpawned",0);
                DUtils.putInteger("Score",0);
                DUtils.putFloat("Health",100);
                DUtils.putFloat("Shield",100);
                DUtils.putBoolean("has1stBossSpawned", false);
                DUtils.putBoolean("has2ndBossSpawned", false);
                DUtils.putInteger("bonuses_collected", 0);
                DUtils.putInteger("lastCheckpoint", 0);
                DUtils.putInteger("bulletsShot", 0);
                DUtils.putInteger("meteoritesDestroyed", 0);
                DUtils.putFloat("ShipX", 0);
                DUtils.putFloat("ShipY", 220);
                 */
                game.setScreen(new GameScreen(game, batch, assetManager, blurProcessor, true));
            }
        });

        continueGameButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(DUtils.getFloat("Health")>0) {
                    game.setScreen(new GameScreen(game, batch, assetManager, blurProcessor, false));
                }
            }
        });

        shopButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!Shop) {
                    Shop = true;
                    info = false;
                    settings = false;
                    play = false;
                    more = false;
                    crafting = false;
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

        workshopButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                    if (crafting) {
                        crafting = false;
                        Hide(0);
                    } else {
                        crafting = true;
                        info = false;
                        settings = false;
                        play = false;
                        Shop = false;
                        more = false;
                        Hide(7);
                    }
            }
        });

        transparency.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DUtils.putBoolean("transparency", transparency.isChecked());
            }
        });

        bloom.addListener(new ChangeListener() {
               @Override
               public void changed(ChangeEvent event, Actor actor) {
               enableShader = bloom.isChecked();
               DUtils.putBoolean("bloom", bloom.isChecked());;
               }
        });

        logging.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DUtils.logging = logging.isChecked();
                DUtils.putBoolean("logging", logging.isChecked());
            }
        });

        Engine1.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(current_engine == 1){
                    menu_offset = 350;
                    menu_type = 2;
                    menuAnimation = true;
                }else{
                    menuAnimation = false;
                    menu_offset = 350;
                }
                current_engine = 1;
                DUtils.putInteger("current_engine", 1);
                UpdateFire();
            }
        });

        Engine2.addListener(new ClickListener(){
               @Override
               public void clicked(InputEvent event, float x, float y) {
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
                       DUtils.putInteger("current_engine", 2);
                   }
                   UpdateFire();
               }
           });

        Engine3.addListener(new ClickListener(){
               @Override
               public void clicked(InputEvent event, float x, float y) {
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
                       DUtils.putInteger("current_engine", 3);
                   }
                   UpdateFire();
               }
           });

        yes.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switch (current_category){
                    case(1):
                        switch (current_engine){
                        case(2):
                            if(money>=1500){
                                is2ndEngineUnlocked = true;
                                money = money-1500;
                                DUtils.putInteger("money",money);
                                DUtils.putBoolean("is2ndEngineUnlocked", true);
                                DUtils.putInteger("current_engine", 2);
                                menuAnimation = false;
                            }
                            break;
                        case(3):
                            if(money>=3500){
                                is3rdEngineUnlocked = true;
                                money = money-3500;
                                DUtils.putInteger("money",money);
                                DUtils.putBoolean("is3rdEngineUnlocked", true);
                                DUtils.putInteger("current_engine", 3);
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
                                    DUtils.putInteger("money",money);
                                    DUtils.putBoolean("is2ndCannonUnlocked", true);
                                    DUtils.putInteger("current_cannon", 2);
                                    menuAnimation = false;
                                }
                                break;
                            case(3):
                                if(money>=4500){
                                    is3rdCannonUnlocked = true;
                                    money = money-4500;
                                    DUtils.putInteger("money",money);
                                    DUtils.putBoolean("is3rdCannonUnlocked", true);
                                    DUtils.putInteger("current_cannon", 3);
                                    menuAnimation = false;
                                }
                                break;
                        }
                        break;
                }
            }
        });

        no.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                menuAnimation = false;
            }
        });

        upgrade.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switch (current_category){
                    case(1):switch (current_engine){
                        case(1):
                            if(cogs>=2+(2*engine1upgradeLevel/3)){
                                cogs-=2+(2*engine1upgradeLevel/3);
                                DUtils.putInteger("cogs", cogs);
                                engine1upgradeLevel++;
                                DUtils.putInteger("engine1upgradeLevel",engine1upgradeLevel);
                                menuAnimation = false;
                            }
                            break;
                        case(2):
                            if(cogs>=3+(2*engine2upgradeLevel/3)){
                                cogs-=3+(2*engine2upgradeLevel/3);
                                DUtils.putInteger("cogs", cogs);
                                engine2upgradeLevel++;
                                DUtils.putInteger("engine2upgradeLevel",engine2upgradeLevel);
                                menuAnimation = false;
                            }
                            break;
                        case(3):
                            if(cogs>=4+(2*engine3upgradeLevel/3)){
                                cogs-=4+(2*engine3upgradeLevel/3);
                                DUtils.putInteger("cogs", cogs);
                                engine3upgradeLevel++;
                                DUtils.putInteger("engine3upgradeLevel",engine3upgradeLevel);
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
                                    DUtils.putInteger("cogs", cogs);
                                    cannon1upgradeLevel++;
                                    DUtils.putInteger("cannon1upgradeLevel",cannon1upgradeLevel);
                                    menuAnimation = false;
                                }
                                break;
                            case(2):
                                if(cogs>=3+(2*cannon2upgradeLevel/3)){
                                    cogs-=3+(2*cannon2upgradeLevel/3);
                                    DUtils.putInteger("cogs", cogs);
                                    cannon2upgradeLevel++;
                                    DUtils.putInteger("cannon2upgradeLevel",cannon2upgradeLevel);
                                    menuAnimation = false;
                                }
                                break;
                            case(3):
                                if(cogs>=4+(2*cannon3upgradeLevel/3)){
                                    cogs-=4+(2*cannon3upgradeLevel/3);
                                    DUtils.putInteger("cogs", cogs);
                                    cannon3upgradeLevel++;
                                    DUtils.putInteger("cannon3upgradeLevel",cannon3upgradeLevel);
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
                DUtils.putInteger("current_category", current_category);
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
                DUtils.putInteger("current_category", current_category);
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
                DUtils.putInteger("current_category", current_category);
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
                DUtils.putInteger("current_cannon", 1);
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
                    DUtils.putInteger("current_cannon", 2);
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
                       DUtils.putInteger("current_cannon", 3);
                   }
               }
           });

        buildNumber.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                easterEggCounter++;
                if(easterEggCounter>10){
                    easterEgg = !easterEgg;
                    DUtils.putBoolean("easterEgg", easterEgg);
                    if(!easterEgg_unlocked) {
                        easterEgg_unlocked = true;
                        DUtils.putBoolean("easterEgg_unlocked", true);
                    }
                    easterEggCounter = 0;
                }
            }
        });

        trelloLink.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("https://trello.com/b/FowZ4XAO/delta-core");
            }
        });

        gitHubLink.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("https://github.com/dgudim/DeltaCore_");
            }
        });

        musicLink.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("https://evankingmusic.com/");
            }
        });

        music = Gdx.audio.newMusic(Gdx.files.internal("music/ambient"+DUtils.getRandomInRange(1, 5)+".ogg"));

        Gdx.input.setCatchKey(Input.Keys.BACK, false);

        enableShader = DUtils.getBoolean("bloom");
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
        millis2 = 101;
        music.setVolume(0);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        if (enableShader) {
            blurProcessor.capture();
        }

        batch.begin();

        movement = (int) (movement + (200 * delta));
        if (movement > 2880) {
            movement = 0;
        }

        batch.draw(Bg, 0, 0, movement, -240, 800, 720);

        fire.setPosition(230 + ship_offset, 268);
        fire2.setPosition(224 + ship_offset, 290);
        fire.draw(batch);
        fire.update(delta);
        fire2.draw(batch);
        fire2.update(delta);

        if (enableShader) {
            batch.end();
            blurProcessor.render();
        batch.begin();
        }

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
                            if(cogs>=2+(2*engine1upgradeLevel/3)) {
                                font_numbers.setColor(Color.GREEN);
                            }else{
                                font_numbers.setColor(Color.RED);
                            }
                            font_numbers.getData().setScale(0.3f);

                            font_numbers.draw(batch, ""+(2+(2*engine1upgradeLevel/3)), 51, 204, 100,1, false);
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

                            font_numbers.getData().setScale(0.3f);

                            if(!is2ndEngineUnlocked) {
                                if(money>=1500) {
                                    font_numbers.setColor(Color.GREEN);
                                }else{
                                    font_numbers.setColor(Color.RED);
                                }
                                font_numbers.draw(batch, "1500", 62, 204, 100, 1, false);
                            }else{
                                if(cogs>=3+(2*engine2upgradeLevel/3)) {
                                    font_numbers.setColor(Color.GREEN);
                                }else{
                                    font_numbers.setColor(Color.RED);
                                }
                                font_numbers.draw(batch, ""+(3+(2*engine2upgradeLevel/3)), 51, 204, 100,1, false);
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

                            font_numbers.getData().setScale(0.3f);

                            if(!is3rdEngineUnlocked) {
                                if(money>=3500) {
                                    font_numbers.setColor(Color.GREEN);
                                }else{
                                    font_numbers.setColor(Color.RED);
                                }
                                font_numbers.draw(batch, "3500", 62, 204, 100, 1, false);
                            }else{
                                if(cogs>=4+(2*engine3upgradeLevel/3)) {
                                    font_numbers.setColor(Color.GREEN);
                                }else{
                                    font_numbers.setColor(Color.RED);
                                }
                                font_numbers.draw(batch, ""+(4+(2*engine3upgradeLevel/3)), 51, 204, 100,1, false);
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
                            no.setPosition(234+menu_offset, 175);
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
                            upgrade.setPosition(222+menu_offset, 175);
                            batch.draw(menu_purchase, 5+menu_offset, 70, 530, 400);
                            font_main.setColor(Color.GREEN);
                            font_main.getData().setScale(0.5f);
                            batch.draw(cog, 447+menu_offset, 320, 30, 30);
                            font_main.draw(batch, "?", 445+menu_offset, 340, 100,1, false );
                            switch (current_engine){
                                case(1):
                                    batch.draw(Engine1_t, 361+menu_offset, 270, 114.66f, 45.864f);
                                    font_main.draw(batch, "Upgrade for "+(2+(2*engine1upgradeLevel/3)), 292+menu_offset, 340, 100,1, false );
                                    font_main.draw(batch, "Level: "+(engine1upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    font_main.getData().setScale(0.28f);
                                    font_main.draw(batch, "Speed Multiplier: ", 254+menu_offset, 305, 100,1, false);
                                    font_main.draw(batch, ""+String.format ("%.1f",(1+engine1upgradeLevel/10f))+"-->"+String.format ("%.1f",(1.1f+engine1upgradeLevel/10f)), 254+menu_offset, 290, 100,1, false);
                                    break;
                                case(2):
                                    batch.draw(Engine2_t, 361+menu_offset, 270, 114.66f, 45.864f);
                                    font_main.draw(batch, "Upgrade for "+(3+(2*engine2upgradeLevel/3)), 292+menu_offset, 340, 100,1, false );
                                    font_main.draw(batch, "Level: "+(engine2upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    font_main.getData().setScale(0.28f);
                                    font_main.draw(batch, "Speed Multiplier: ", 254+menu_offset, 305, 100,1, false);
                                    font_main.draw(batch, ""+String.format ("%.1f",(1.4f+engine2upgradeLevel/10f))+"-->"+String.format ("%.1f",(1.5f+engine2upgradeLevel/10f)), 254+menu_offset, 290, 100,1, false);
                                    break;
                                case(3):
                                    batch.draw(Engine3_t, 361+menu_offset, 270, 114.66f, 45.864f);
                                    font_main.draw(batch, "Upgrade for "+(4+(2*engine3upgradeLevel/3)), 292+menu_offset, 340, 100,1, false );
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
                            if(cogs>=2+(2*cannon1upgradeLevel/3)) {
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

                            font_numbers.getData().setScale(0.3f);

                            if(!is2ndCannonUnlocked) {
                                if(money>=1500) {
                                    font_numbers.setColor(Color.GREEN);
                                }else{
                                    font_numbers.setColor(Color.RED);
                                }
                                font_numbers.draw(batch, "2500", 62, 204, 100, 1, false);
                            }else{
                                if(cogs>=3+(2*cannon2upgradeLevel/3)) {
                                    font_numbers.setColor(Color.GREEN);
                                }else{
                                    font_numbers.setColor(Color.RED);
                                }
                                font_numbers.draw(batch, ""+(3+(2*cannon2upgradeLevel/3)), 51, 204, 100,1, false);
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

                            font_numbers.getData().setScale(0.3f);

                            if(!is3rdCannonUnlocked) {
                                if(money>=4500) {
                                    font_numbers.setColor(Color.GREEN);
                                }else{
                                    font_numbers.setColor(Color.RED);
                                }
                                font_numbers.draw(batch, "4500", 62, 204, 100, 1, false);
                            }else{
                                if(cogs>=4+(2*cannon3upgradeLevel/3)) {
                                    font_numbers.setColor(Color.GREEN);
                                }else{
                                    font_numbers.setColor(Color.RED);
                                }
                                font_numbers.draw(batch, ""+(4+(2*cannon3upgradeLevel/3)), 51, 204, 100,1, false);
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
                            no.setPosition(234+menu_offset, 175);
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
                            upgrade.setPosition(222+menu_offset, 175);
                            batch.draw(menu_purchase, 5+menu_offset, 70, 530, 400);
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
                                font_main.draw(batch, "Spread: "+String.format ("%.1f",MathUtils.clamp((1.5f-cannon1upgradeLevel*0.1f), 0.7f, 1.5f))+"-->"+String.format ("%.1f",MathUtils.clamp((1.5f-(cannon1upgradeLevel+1)*0.1f), 0.7f, 1.5f)), 252+menu_offset, 290, 100,1, false);
                                font_main.getData().setScale(0.4f);
                                font_main.draw(batch, "Level: "+(cannon1upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    break;
                                case(2):
                                batch.draw(Cannon2_t, 361+menu_offset, 270, 142.07649f, 45.901638f);
                                font_main.draw(batch, "Upgrade for "+(3+(2*cannon2upgradeLevel/3)), 290+menu_offset, 340, 100,1, false );
                                font_main.getData().setScale(0.26f);
                                font_main.draw(batch, "Damage: "+(60+cannon2upgradeLevel)+"-->"+(60+cannon2upgradeLevel+1), 252+menu_offset, 310, 100,1, false);
                                font_main.draw(batch, "Spread: "+String.format ("%.1f",MathUtils.clamp((1.2f-cannon2upgradeLevel*0.1f), 0.7f, 1.5f))+"-->"+String.format ("%.1f",MathUtils.clamp((1.2f-(cannon2upgradeLevel+1)*0.1f), 0.7f, 1.5f)), 252+menu_offset, 290, 100,1, false);
                                font_main.getData().setScale(0.4f);
                                font_main.draw(batch, "Level: "+(cannon2upgradeLevel+1), 320+menu_offset, 260, 100,1, false );
                                    break;
                                case(3):
                                batch.draw(Cannon3_t, 361+menu_offset, 270, 140.01207f, 45.866024f);
                                font_main.draw(batch, "Upgrade for "+(4+(2*cannon3upgradeLevel/3)), 290+menu_offset, 340, 100,1, false);
                                font_main.getData().setScale(0.26f);
                                font_main.draw(batch, "Damage: "+(70+cannon3upgradeLevel)+"-->"+(70+cannon3upgradeLevel+1), 252+menu_offset, 310, 100,1, false);
                                font_main.draw(batch, "Spread: "+String.format ("%.1f",MathUtils.clamp((0.8f-cannon3upgradeLevel*0.1f), 0.7f, 1.5f))+"-->"+String.format ("%.1f",MathUtils.clamp((0.8f-(cannon3upgradeLevel+1)*0.1f), 0.7f, 1.5f)), 252+menu_offset, 290, 100,1, false);
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

        MenuBg.draw(batch, 1);

        if(info){
            infoBg.draw(batch, 1);
            font_main.getData().setScale(0.48f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "ø¤º°°º¤ø,¸¸,ø¤º°[ Info ]°º¤ø,¸¸,ø¤º°°º¤ø", 5, 460, 531, 1, false);
            font_main.setColor(Color.valueOf("#00ff55"));
            font_main.draw(batch, "Made by Deoxys", 5, 429, 531, 1, false);
            font_main.draw(batch, "Textures by DefenceX, VKLowe, Deoxys", 5, 407, 531, 1, false);
            font_main.setColor(Color.CYAN);
            font_main.draw(batch, "Music by ________", 5, 377, 531, 1, false);
            font_main.setColor(Color.valueOf("#5DBCD2"));
            font_main.draw(batch, "Inspired by DefenseX, PetruCHIOrus", 5, 345, 531, 1, false);
            font_main.setColor(Color.valueOf("#cccc22"));
            font_main.draw(batch, "Testers: Misterowl, Nikita.Beloglazov", 5, 305, 531, 1, false);
            font_main.draw(batch, "Kisliy_xleb, Watermelon0guy, PYTHØN", 5, 275, 531, 1, false);
            font_main.draw(batch, "Ha4upelmeney, Lukmanov, ZerOn", 5, 245, 531, 1, false);
            font_main.setColor(Color.valueOf("#0FE500"));
            font_main.draw(batch, "Contributors: Volkov, DefenseX", 5, 210, 531, 1, false);
            font_main.draw(batch, "Zsingularityz", 5, 176, 531, 1, false);
            font_main.setColor(Color.valueOf("#CAE500"));
            font_main.draw(batch, "Deltacore", 5, 135, 531, 1, false);
            font_main.draw(batch,"® All right reserved", 5, 95, 531, 1, false);
        }

        if(settings) {
            infoBg.draw(batch, 1);
            font_main.getData().setScale(0.5f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "ø¤º°°º¤ø,¸¸,ø¤[ Settings ]¤ø,¸¸,ø¤º°°º¤ø", 5, 460, 531, 1, false);
            font_main.setColor(Color.valueOf("#0FE500"));
            MusicVolume = (int) (musicVolume.getValue() * 100);
            SoundVolume = (int) (soundEffectsVolume.getValue() * 100);
            font_main.draw(batch, "Music: " + MusicVolume + "%", 95, 420, 132, 1, false);
            font_main.draw(batch, "Sound effects: " + SoundVolume + "%", 95, 370, 132, 1, false);
            font_main.draw(batch, "Show Fps", 65, 320, 132, 1, false);
            font_main.draw(batch, "Bloom", 230, 320, 132, 1, false);
            font_main.draw(batch, "Prefs Logging", 385, 330, 132, 1, true);
            uiScale = (int) (uiScaling.getValue() * 100);
            font_main.draw(batch, "Ui Scaling: " + uiScale + " %", 325, 260, 132, 1, false);
            font_main.draw(batch, "(In game)", 325, 230, 132, 1, false);
            font_main.draw(batch, "Semi-transparent UI", 140, 128, 132, 1, false);
        }

        if(more){
            infoBg.draw(batch, 1);
            font_main.setColor(Color.valueOf("#0FE500"));
            font_main.getData().setScale(0.45f);
            font_main.draw(batch, "Game source code", 110, 440, 132, 1, false);
            font_main.draw(batch, "Official Trello list of planned features", 232.5f, 380, 132, 1, false);
            font_main.draw(batch, "Redeem secret code", 123, 320, 132, 1, false);
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

            millis = millis + 50 * delta;
            millis2 = millis2 + 0.5f * delta;

            if(millis2 > 100){
                music.dispose();
                music = Gdx.audio.newMusic(Gdx.files.internal("music/ambient"+ DUtils.getRandomInRange(1, 5)+".ogg"));
                music.setPosition(0);
                music.setVolume(0);
                music.play();
                millis2 = 0;
            }

        }

        if(play){
            infoBg.draw(batch, 1);
            font_main.getData().setScale(0.5f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "ø¤º°º¤ø,¸¸,ø¤[ Play Menu ]¤ø,¸¸,ø¤º°º¤ø", 5, 460, 531, 1, false);
            font_main.setColor(new Color().fromHsv(120-difficultyControl.getValue()*20, 1.5f, 1).add(0,0,0,1));
            font_main.draw(batch, "Difficulty: X"+(float)((int)(difficultyControl.getValue()*100))/100, 30, 315, 531, 1, false);
        }

        if(crafting){
            infoBg.draw(batch, 1);
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
        number = MathUtils.clamp(number, 0, 1);
        Lamp.setColor(1,1, 1, number);
        Lamp.draw(batch, 1);

        batch.end();

        Menu.draw();
        Menu.act(delta);

        batch.begin();
        font_main.getData().setScale(0.35f);
        font_main.setColor(Color.GOLD);
        font_main.draw(batch, "V 0.0.3 Build 1", 5, 35, 150, 1, false);
        if(easterEgg){
            font_main.getData().setScale(0.2f);
            font_main.setColor(Color.ORANGE);
            font_main.draw(batch, "cat edition", 5, 20, 150, 1, false);
        }

        for(int i = 0; i< fillingThreshold; i++){
            FillTexture.setPosition(0, -72*(i+1));
            FillTexture.draw(batch, 1);
            FillTexture.setPosition(456, -72*(i+1));
            FillTexture.draw(batch, 1);
            FillTexture.setPosition(0, 408+72*(i+1));
            FillTexture.draw(batch, 1);
            FillTexture.setPosition(456, 408+72*(i+1));
            FillTexture.draw(batch, 1);
        }

        for(int i = 0; i<(fillingThreshold /6)+1; i++) {
            for(int i2=0; i2<7; i2++) {
                FillTexture.setPosition(-456 - 456 * i, 408-i2*72);
                FillTexture.draw(batch, 1);
                FillTexture.setPosition(800 + 456 * i, 408-i2*72);
                FillTexture.draw(batch, 1);
            }
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(400, 240, 0);
        float tempScaleH = height/480.0f;
        float tempScaleW = width/800.0f;
        float zoom;
        if(tempScaleH<=tempScaleW){
            zoom = tempScaleH;
        }else{
            zoom = tempScaleW;
        }
        camera.zoom = 1/zoom;
        camera.update();
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
        music.stop();
        game.getScreen().dispose();
    }

    @Override
    public void dispose() {
        Menu.dispose();
        ShopStage.dispose();
        music.dispose();
        font_main.dispose();
        font_numbers.dispose();
        fire.dispose();
        fire2.dispose();
    }

   private void Hide(int type){
           switch (type){
               case(0):
                   fps.setVisible(false);
                   bloom.setVisible(false);
                   logging.setVisible(false);
                   uiScaling.setVisible(false);
                   musicVolume.setVisible(false);
                   soundEffectsVolume.setVisible(false);
                   difficultyControl.setVisible(false);
                   newGameButton.setVisible(false);
                   continueGameButton.setVisible(false);
                   shopButton.setVisible(false);
                   workshopButton.setVisible(false);
                   transparency.setVisible(false);
                   yes.setVisible(false);
                   no.setVisible(false);
                   upgrade.setVisible(false);
                   Engine1.setVisible(false);
                   Engine2.setVisible(false);
                   Engine3.setVisible(false);
                   CategoryGun.setVisible(false);
                   CategoryGun2.setVisible(false);
                   CategoryEngine.setVisible(false);
                   Cannon1.setVisible(false);
                   Cannon2.setVisible(false);
                   Cannon3.setVisible(false);
                   trelloLink.setVisible(false);
                   gitHubLink.setVisible(false);
                   secretCode.setVisible(false);
                   musicLink.setVisible(false);
                   slotManager.setVisible(false);
                   break;
               case(1):
                   Hide(0);
                   fps.setVisible(true);
                   bloom.setVisible(true);
                   logging.setVisible(true);
                   uiScaling.setVisible(true);
                   musicVolume.setVisible(true);
                   soundEffectsVolume.setVisible(true);
                   transparency.setVisible(true);
                   break;
               case(2):
                   Hide(0);
                   difficultyControl.setVisible(true);
                   newGameButton.setVisible(true);
                   continueGameButton.setVisible(true);
                   shopButton.setVisible(true);
                   workshopButton.setVisible(true);
                   break;
               case(3):
                   Hide(0);
                   CategoryGun.setVisible(true);
                   CategoryGun2.setVisible(true);
                   CategoryEngine.setVisible(true);
                   Engine1.setVisible(true);
                   Engine2.setVisible(true);
                   Engine3.setVisible(true);
                   yes.setVisible(true);
                   no.setVisible(true);
                   upgrade.setVisible(true);
                   break;
               case(4):
                   Hide(0);
                   CategoryGun.setVisible(true);
                   CategoryGun2.setVisible(true);
                   CategoryEngine.setVisible(true);
                   Cannon1.setVisible(true);
                   Cannon2.setVisible(true);
                   Cannon3.setVisible(true);
                   yes.setVisible(true);
                   no.setVisible(true);
                   upgrade.setVisible(true);
                   break;
               case(5):
                   Hide(0);
                   CategoryGun.setVisible(true);
                   CategoryGun2.setVisible(true);
                   CategoryEngine.setVisible(true);
                   yes.setVisible(true);
                   no.setVisible(true);
                   upgrade.setVisible(true);
                   break;
               case(6):
                   Hide(0);
                   trelloLink.setVisible(true);
                   gitHubLink.setVisible(true);
                   secretCode.setVisible(true);
                   break;
               case(7):
                   Hide(0);
                   slotManager.setVisible(true);
                   break;
               case(8):
                   Hide(0);
                   musicLink.setVisible(true);
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