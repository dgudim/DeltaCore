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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.utils.DUtils;
import com.deo.flapd.utils.postprocessing.PostProcessor;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putInteger;
import static com.deo.flapd.utils.DUtils.updateCamera;


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

    private Image Lamp;

    private BitmapFont font_main, font_buttons;

    private boolean Music;

    private Stage Menu, ShopStage;

    private int movement;

    private InputMultiplexer multiplexer;

    private Music music;
    private float millis;
    private float millis2;

    private Game game;

    private ParticleEffect fire, fire2;

    private boolean easterEgg;

    private int easterEggCounter;

    private final int fillingThreshold = 7;

    private PostProcessor blurProcessor;

    private boolean enableShader;

    private Tree craftingTree;

    private CategoryManager menuCategoryManager, workshopCategoryManager;

    private final Slider musicVolumeS;

       public MenuScreen(final Game game, final SpriteBatch batch, final AssetManager assetManager, final PostProcessor blurProcessor){
        this.game = game;

        this.blurProcessor = blurProcessor;

        Music = getFloat("musicVolume") > 0;

        easterEgg = getBoolean("easterEgg");

        this.batch = batch;

        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);

        MenuBg = new Image((Texture)assetManager.get("menuBg.png"));
        Lamp = new Image((Texture)(assetManager.get("lamp.png")));
        MenuBg.setBounds(0, 0, 800, 480);

        font_main = assetManager.get("fonts/font2(old).fnt");
        font_buttons = assetManager.get("fonts/font2.fnt");

        Bg = assetManager.get("bg_old.png");
        Bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        FillTexture = new Image((Texture)assetManager.get("menuFill.png"));
        FillTexture.setSize(456, 72);

        Ship = assetManager.get("ship.png");

        Image buildNumber = new Image((Texture) assetManager.get("greyishButton.png"));
        buildNumber.setBounds(5,5,150, 50);

        Lamp.setBounds(730, 430, 15, 35);

        UIComposer uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("defaultDark", "sliderDefaultNormal", "checkBoxDefault", "gitHub", "trello");

        Table playScreenTable = new Table();
        playScreenTable.align(Align.topLeft);
        playScreenTable.setBounds(15, 60, 531, 410);
        TextButton newGame = uiComposer.addTextButton("defaultDark", "new game", 0.4f);
        TextButton continueGame = uiComposer.addTextButton("defaultDark", "continue", 0.4f);
        TextButton workshop = uiComposer.addTextButton("defaultDark", "workshop", 0.4f);
        final Table difficultyT = uiComposer.addSlider("sliderDefaultNormal", 1, 5, 0.1f, "Difficulty ","X", "difficulty");
        playScreenTable.add(newGame).padTop(5).padBottom(5).align(Align.left).row();
        playScreenTable.add(continueGame).padTop(5).padBottom(5).align(Align.left).row();
        playScreenTable.add(workshop).padTop(5).padBottom(5).align(Align.left).row();
        playScreenTable.add(difficultyT).padTop(5).padBottom(5).align(Align.left).row();
        final Slider difficultyControl = (Slider)difficultyT.getCells().get(0).getActor();
        difficultyT.getCells().get(1).getActor().setColor(new Color().fromHsv(120-difficultyControl.getValue()*20, 1.5f, 1).add(0,0,0,1));

        ScrollPane settingsPane = uiComposer.createScrollGroup(15, 62, 531, 412, false, true);
        Table settingsGroup = (Table)settingsPane.getActor();
        settingsGroup.align(Align.left);
        Table musicVolumeT, soundVolumeT, uiScaleT, bloomT, transparentUIT, prefsLoggingT, showFpsT;
        musicVolumeT = uiComposer.addSlider("sliderDefaultNormal", 0, 100, 1, "[#32ff32]Music volume ","%", "musicVolume");
        soundVolumeT = uiComposer.addSlider("sliderDefaultNormal", 0, 100, 1, "[#32ff32]Sound volume ", "%", "soundVolume");
        uiScaleT = uiComposer.addSlider("sliderDefaultNormal", 1, 2, 0.25f, "[#32ff32]Ui scale ", "X", "ui");
        bloomT = uiComposer.addCheckBox("checkBoxDefault", "[#32ff32]Bloom", "bloom");
        transparentUIT = uiComposer.addCheckBox("checkBoxDefault", "[#32ff32]Semi-transparent ui", "transparency");
        prefsLoggingT = uiComposer.addCheckBox("checkBoxDefault", "[#32ff32]Prefs logging", "logging");
        showFpsT = uiComposer.addCheckBox("checkBoxDefault", "[#32ff32]Show fps", "showFps");
        settingsGroup.add(musicVolumeT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(soundVolumeT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(uiScaleT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(bloomT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(prefsLoggingT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(transparentUIT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(showFpsT).padTop(5).padBottom(5).align(Align.left).row();

        Table moreTable = new Table();
        moreTable.align(Align.topLeft);
        moreTable.setBounds(15, 62, 531, 410);
        moreTable.add(uiComposer.addLinkButton("gitHub", "[#32ff32]Game source code", "https://github.com/dgudim/DeltaCore_")).padTop(5).padBottom(5).align(Align.left).row();
        moreTable.add(uiComposer.addLinkButton("trello", "[#32ff32]Official trello list of planned features", "https://trello.com/b/FowZ4XAO/delta-core")).padTop(5).padBottom(5).align(Align.left).row();

        musicVolumeS = (Slider)musicVolumeT.getCells().get(0).getActor();
        final CheckBox bloomS = (CheckBox)bloomT.getCells().get(0).getActor();
        final CheckBox loggingS = (CheckBox)prefsLoggingT.getCells().get(0).getActor();

        Menu = new Stage(viewport, batch);

        Menu.addActor(buildNumber);

        ScrollPane infoText = (ScrollPane) uiComposer.addScrollText(
                "[#00ff55]Made by Deoxys\n" +
                "Textures by DefenceX, VKLowe, Deoxys\n" +
                "Music by EvanKing\n" +
                "[#5DBCD2]Inspired by DefenseX, PetruCHIOrus\n" +
                "[#cccc22]Testers: Misterowl, Nikita.Beloglazov\n" +
                "Kisliy_xleb, Watermelon0guy, PYTHØN\n" +
                "Ha4upelmeney, Lukmanov, ZerOn\n" +
                "[#0FE500]Contributors: Volkov, DefenseX\n" +
                "Zsingularityz\n" +
                "[#CAE500]Deltacore\n" +
                "® All right reserved",
                font_main, 0.48f, true, false, 5, 100, 531, 410);

        menuCategoryManager = new CategoryManager(assetManager, font_buttons, 250, 75, 2.5f, 0.5f, 2, true, true, "lastClickedMenuButton");
        menuCategoryManager.addCategory(playScreenTable, "play");
        menuCategoryManager.addCategory(playScreenTable, "online");
        menuCategoryManager.addCategory(settingsPane, "settings");
        menuCategoryManager.addCategory(infoText, "info");
        menuCategoryManager.addCategory(moreTable, "more");
        menuCategoryManager.setBounds(545, 3, 400);
        menuCategoryManager.setBackgroundBounds(5, 62, 531, 410);

        craftingTree = LoadingScreen.craftingTree;
        craftingTree.setVisible(false);

        workshopCategoryManager = new CategoryManager(assetManager, font_buttons, 90, 40, 2.5f, 0.25f, 1, true, false, "lastClickedWorkshopButton");
        workshopCategoryManager.addCategory(craftingTree.treeScrollView, "crafting");
        workshopCategoryManager.addCloseButton();
        workshopCategoryManager.setBounds(12.5f, 67, 403);
        workshopCategoryManager.setBackgroundBounds(5, 62, 531, 410);
        workshopCategoryManager.setVisible(false);
        menuCategoryManager.addOverrideActor(workshopCategoryManager);

        menuCategoryManager.attach(Menu);
        Menu.addActor(infoText);
        Menu.addActor(playScreenTable);
        Menu.addActor(moreTable);
        Menu.addActor(settingsPane);
        workshopCategoryManager.attach(Menu);
        infoText.setVisible(false);
        moreTable.setVisible(false);
        playScreenTable.setVisible(false);
        settingsPane.setVisible(false);

        craftingTree.attach(Menu);

        ShopStage = new Stage(viewport, batch);

        fire = new ParticleEffect();
        fire2 = new ParticleEffect();

           switch (1){
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

        musicVolumeS.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                putFloat("musicVolume", musicVolumeS.getValue());

                Music = musicVolumeS.getValue() > 0;

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

        musicVolumeS.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(music.isPlaying()) {
                    music.setVolume(musicVolumeS.getValue()/100f);
                }
            }
        });

        difficultyControl.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                difficultyT.getCells().get(1).getActor().setColor(new Color().fromHsv(120-difficultyControl.getValue()*20, 1.5f, 1).add(0,0,0,1));
            }
        });

        newGame.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                putInteger("enemiesKilled",0);
                putInteger("moneyEarned",0);
                putInteger("enemiesSpawned",0);
                putInteger("Score",0);
                putFloat("Health",100);
                putFloat("Shield",100);
                putBoolean("has1stBossSpawned", false);
                putBoolean("has2ndBossSpawned", false);
                putInteger("bonuses_collected", 0);
                putInteger("lastCheckpoint", 0);
                putInteger("bulletsShot", 0);
                putInteger("meteoritesDestroyed", 0);
                putFloat("ShipX", 0);
                putFloat("ShipY", 220);
                game.setScreen(new GameScreen(game, batch, assetManager, blurProcessor, true));
            }
        });

        continueGame.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(getFloat("Health")>0) {
                    game.setScreen(new GameScreen(game, batch, assetManager, blurProcessor, false));
                }
            }
        });

        workshop.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
               workshopCategoryManager.setVisible(!workshopCategoryManager.isVisible());
            }
        });

        bloomS.addListener(new ChangeListener() {
               @Override
               public void changed(ChangeEvent event, Actor actor) {
               enableShader = bloomS.isChecked();
               putBoolean("bloom", bloomS.isChecked());;
               }
        });

        loggingS.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DUtils.logging = loggingS.isChecked();
                putBoolean("logging", loggingS.isChecked());
            }
        });

        buildNumber.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                easterEggCounter++;
                if(easterEggCounter>10){
                    easterEgg = !easterEgg;
                    putBoolean("easterEgg", easterEgg);
                    easterEggCounter = 0;
                }
            }
        });

        music = Gdx.audio.newMusic(Gdx.files.internal("music/ambient"+ getRandomInRange(1, 5)+".ogg"));

        Gdx.input.setCatchKey(Input.Keys.BACK, false);

        enableShader = getBoolean("bloom");
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

        fire.setPosition(230, 268);
        fire2.setPosition(224, 290);
        fire.draw(batch);
        fire.update(delta);
        fire2.draw(batch);
        fire2.update(delta);

        if (enableShader) {
            batch.end();
            blurProcessor.render();
            batch.begin();
        }

        batch.draw(Ship, 220, 250, 76.8f, 57.6f);

        batch.end();
        ShopStage.draw();
        ShopStage.act(delta);
        batch.begin();

        MenuBg.draw(batch, 1);

        if(Music) {

            if (millis > 10) {
                if (music.getPosition() > 65 && music.getPosition() < 69 && music.getVolume() > 0) {
                    music.setVolume(music.getVolume() - 0.05f);
                }
                if (music.getPosition() > 0 && music.getPosition() < 4 && music.getVolume() < musicVolumeS.getValue()/100f) {
                    music.setVolume(music.getVolume() + 0.05f);
                }
                millis = 0;
            }

            millis = millis + 50 * delta;
            millis2 = millis2 + 0.5f * delta;

            if(millis2 > 100){
                music.dispose();
                music = Gdx.audio.newMusic(Gdx.files.internal("music/ambient"+ getRandomInRange(1, 5)+".ogg"));
                music.setPosition(0);
                music.setVolume(0);
                music.play();
                millis2 = 0;
            }

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
        music.stop();
        game.getScreen().dispose();
    }

    @Override
    public void dispose() {
        Menu.dispose();
        ShopStage.dispose();
        music.dispose();
        fire.dispose();
        fire2.dispose();
    }
}