package com.deo.flapd.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.DUtils;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.MusicManager;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.SoundManager;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.utils.ui.UIComposer;
import com.deo.flapd.view.dialogues.ConfirmationDialogue;
import com.deo.flapd.view.overlays.CategoryManager;
import com.deo.flapd.view.overlays.ItemSlotManager;
import com.deo.flapd.view.overlays.UpgradeMenu;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.lerp;
import static com.badlogic.gdx.utils.TimeUtils.millis;
import static com.deo.flapd.Main.VERSION_NAME;
import static com.deo.flapd.utils.DUtils.LogLevel.ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.clearPrefs;
import static com.deo.flapd.utils.DUtils.drawBg;
import static com.deo.flapd.utils.DUtils.drawScreenExtenders;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.getVerticalAndHorizontalFillingThresholds;
import static com.deo.flapd.utils.DUtils.handleDebugInput;
import static com.deo.flapd.utils.DUtils.initNewGame;
import static com.deo.flapd.utils.DUtils.loadPrefsFromFile;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putLong;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.savePrefsToFile;
import static com.deo.flapd.utils.DUtils.updateCamera;
import static com.deo.flapd.view.screens.LoadingScreen.particleEffectPoolLoader;

public class MenuScreen implements Screen {
    
    private final SpriteBatch batch;
    
    private final OrthographicCamera camera;
    private final Viewport viewport;
    
    private final Texture bg1;
    private final Texture bg2;
    public Image ship;
    private Image ship_touchLayer;
    private float originalShipWidth;
    public float targetShipScaleFactor = 2.1f;
    private float originalShipHeight;
    private float targetShipX;
    private float previousFireMotionScale = 1;
    private float previousFireSizeScale = 1;
    
    private Animation<TextureRegion> enemyAnimation;
    private Array<TextureRegionDrawable> enemyAnimation_drawables;
    private float animationPosition;
    private boolean playerHasAnimation;
    private final JsonEntry shipConfigs;
    private JsonEntry shipConfig;
    private float shipUpgradeAnimationPosition = 1;
    private byte shipUpgradeAnimationDirection = -1;
    
    private final Texture fillTexture;
    private final Image lamp;
    private final BitmapFont font_main;
    
    private final Stage menuStage;
    
    private float movement;
    
    private final InputMultiplexer multiplexer;
    
    private final MusicManager musicManager;
    private final SoundManager soundManager;
    
    private final Game game;
    
    private final Array<ParticleEffectPool.PooledEffect> fires;
    private final Array<Float> fireOffsetsX;
    private final Array<Float> fireOffsetsY;
    
    private boolean drawScreenExtenders = true;
    private int horizontalFillingThreshold;
    private int verticalFillingThreshold;
    
    private final CompositeManager compositeManager;
    private final AssetManager assetManager;
    private final PostProcessor blurProcessor;
    private final LocaleManager localeManager;
    
    private boolean enableShader;
    
    private final CategoryManager workshopCategoryManager;
    
    private final Slider musicVolumeS;
    
    private final JsonEntry treeJson;
    
    private final String currentShip;
    private String lastFireEffect;
    
    private boolean isConfirmationDialogActive = false;
    
    private float warpSpeed = 0;
    private float warpXOffset = 0;
    private boolean warpAnimationActive = false;
    private boolean newGameAfterWarp = true;
    
    public MenuScreen(CompositeManager compositeManager) {
        long genTime = TimeUtils.millis();
        log("time to generate menu", INFO);
        
        this.compositeManager = compositeManager;
        game = compositeManager.getGame();
        blurProcessor = compositeManager.getBlurProcessor();
        assetManager = compositeManager.getAssetManager();
        batch = compositeManager.getBatch();
        soundManager = compositeManager.getSoundManager();
        musicManager = compositeManager.getMusicManager();
        localeManager = compositeManager.getLocaleManager();
        
        treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        
        TextureAtlas menuUiAtlas = assetManager.get("ui/menuUi.atlas", TextureAtlas.class);
        
        Image menuBg = new Image(menuUiAtlas.findRegion("menuBg"));
        lamp = new Image(menuUiAtlas.findRegion("lamp"));
        menuBg.setBounds(0, 0, 800, 480);
        lamp.setBounds(725, 430, 25, 35);
        
        font_main = assetManager.get("fonts/pixel.ttf");
        
        bg1 = assetManager.get("backgrounds/bg_layer1.png");
        bg2 = assetManager.get("backgrounds/bg_layer2.png");
        
        bg1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        bg2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        
        fillTexture = assetManager.get("screenFill.png");
        
        menuStage = new Stage(viewport, batch);
        
        currentShip = getString(Keys.currentShip);
        shipConfigs = new JsonEntry(new JsonReader().parse(Gdx.files.internal("player/shipConfigs.json")));
        initializeShip();
        
        fires = new Array<>();
        fireOffsetsX = new Array<>();
        fireOffsetsY = new Array<>();
        
        Image buildNumber = new Image(menuUiAtlas.findRegion("greyishButton"));
        buildNumber.setBounds(5, 5, 150, 50);
        
        long uiGenTime = millis();
        
        UIComposer uiComposer = compositeManager.getUiComposer();
        
        Table playScreenTable = new Table();
        playScreenTable.align(Align.topLeft);
        playScreenTable.setBounds(15, 60, 531, 410);
        TextButton newGame = uiComposer.addTextButton("defaultLight", localeManager.get("play.newGame"), 0.3f);
        TextButton continueGame = uiComposer.addTextButton("defaultLight", localeManager.get("play.continue"), 0.3f);
        TextButton workshop = uiComposer.addTextButton("defaultLight", localeManager.get("play.workshop"), 0.3f);
        final Table difficultyT = uiComposer.addSlider("sliderDefaultNormal", 1, 5, 0.1f, localeManager.get("play.difficulty") + " ", "X", "difficulty");
        playScreenTable.add(newGame).size(160, 35).padTop(5).padBottom(5).align(Align.left).row();
        playScreenTable.add(continueGame).size(160, 35).padTop(5).padBottom(5).align(Align.left).row();
        playScreenTable.add(workshop).size(160, 35).padTop(5).padBottom(5).align(Align.left).row();
        playScreenTable.add(difficultyT).padTop(5).padBottom(5).align(Align.left).row();
        final Slider difficultyControl = (Slider) difficultyT.getCells().get(0).getActor();
        difficultyT.getCells().get(1).getActor().setColor(new Color().fromHsv(120 - difficultyControl.getValue() * 20, 1.5f, 1).add(0, 0, 0, 1));
        
        ScrollPane settingsPane = uiComposer.createScrollGroup(15, 70, 531, 400, false, true);
        settingsPane.setCancelTouchFocus(false);
        Table settingsGroup = (Table) settingsPane.getActor();
        settingsGroup.align(Align.left);
        final Table musicVolumeT, soundVolumeT, uiScaleT, joystickOffsetX, joystickOffsetY;
        final CheckBox bloomT, showFpsT, transparentUIT, prefsLoggingT;
        musicVolumeT = uiComposer.addSlider("sliderDefaultNormal", 0, 100, 1, "[#32ff32]" + localeManager.get("settings.musicVolume") + " ", "%", "musicVolume", settingsPane);
        soundVolumeT = uiComposer.addSlider("sliderDefaultNormal", 0, 100, 1, "[#32ff32]" + localeManager.get("settings.soundVolume") + " ", "%", "soundVolume", settingsPane);
        uiScaleT = uiComposer.addSlider("sliderDefaultNormal", 1, 2, 0.25f, "[#32ff32]" + localeManager.get("settings.uiScale") + " ", "X", "ui", settingsPane);
        bloomT = uiComposer.addCheckBox("checkBoxDefault", "[#32ff32]" + localeManager.get("settings.bloom"), "bloom");
        transparentUIT = uiComposer.addCheckBox("checkBoxDefault", "[#32ff32]" + localeManager.get("settings.uiTransparency"), "transparency");
        prefsLoggingT = uiComposer.addCheckBox("checkBoxDefault", "[#32ff32]" + localeManager.get("settings.logPreferences"), "logging");
        showFpsT = uiComposer.addCheckBox("checkBoxDefault", "[#32ff32]" + localeManager.get("settings.showFps"), "showFps");
        joystickOffsetX = uiComposer.addSlider("sliderDefaultNormal", 0, 50, 0.1f, "[#32ff32]" + localeManager.get("settings.joystickOffsetX") + " ", "px", "joystickOffsetX", settingsPane);
        joystickOffsetY = uiComposer.addSlider("sliderDefaultNormal", 0, 50, 0.1f, "[#32ff32]" + localeManager.get("settings.joystickOffsetX") + " ", "px", "joystickOffsetY", settingsPane);
        settingsGroup.add(musicVolumeT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(soundVolumeT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(uiScaleT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(bloomT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(prefsLoggingT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(transparentUIT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(showFpsT).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(joystickOffsetX).padTop(5).padBottom(5).align(Align.left).row();
        settingsGroup.add(joystickOffsetY).padTop(5).padBottom(5).align(Align.left).row();
        
        Table moreTable = new Table();
        moreTable.align(Align.topLeft);
        moreTable.setBounds(15, 62, 531, 410);
        moreTable.add(uiComposer.addLinkButton("gitHub", "[#32ff32]"+localeManager.get("more.github"), "https://github.com/dgudim/DeltaCore_")).padTop(5).padBottom(5).align(Align.left).row();
        moreTable.add(uiComposer.addLinkButton("trello", "[#32ff32]"+localeManager.get("more.trello"), "https://trello.com/b/FowZ4XAO/delta-core")).padTop(5).padBottom(5).align(Align.left).row();
        final TextButton exportGameData = uiComposer.addTextButton("defaultLight", localeManager.get("more.exportData"), 0.3f);
        TextButton importGameData = uiComposer.addTextButton("defaultLight", localeManager.get("more.importData"), 0.3f);
        TextButton clearGameData = uiComposer.addTextButton("defaultLight", localeManager.get("more.clearData"), 0.3f);
        
        final Label exportMessage = uiComposer.addText("", assetManager.get("fonts/bold_main.ttf"), 0.3f);
        exportMessage.setWrap(true);
        
        exportGameData.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exportMessage.setText("[#FFFF00]"+localeManager.get("more.savedData")+" " + savePrefsToFile());
            }
        });
        
        importGameData.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    loadPrefsFromFile();
                    game.setScreen(new LoadingScreen(compositeManager));
                } catch (Exception e) {
                    exportMessage.setText("[#FF3300]" + e.getMessage());
                }
            }
        });
        
        clearGameData.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearPrefs();
                game.setScreen(new LoadingScreen(compositeManager));
            }
        });
        
        moreTable.add(exportGameData).size(310, 50).padTop(5).padBottom(5).align(Align.left).row();
        moreTable.add(importGameData).size(310, 50).padTop(5).padBottom(5).align(Align.left).row();
        moreTable.add(clearGameData).size(310, 50).padTop(5).padBottom(5).align(Align.left).row();
        moreTable.add(exportMessage).padTop(5).padBottom(5).width(510).row();
        
        musicVolumeS = (Slider) musicVolumeT.getCells().get(0).getActor();
        
        Slider soundVolumeS = (Slider) soundVolumeT.getCells().get(0).getActor();
        soundVolumeS.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                soundManager.notifyVolumeUpdated();
            }
        });
        
        ScrollPane infoText = (ScrollPane) uiComposer.addScrollText(
                localeManager.get("mainMenu.infoContent"),
                font_main, 0.48f, true, false, 5, 100, 531, 410);
        
        
        final ItemSlotManager blackMarket = new ItemSlotManager(compositeManager);
        blackMarket.addShopSlots();
        blackMarket.setBounds(105, 70, 425, 400);
        
        final ItemSlotManager inventory = new ItemSlotManager(compositeManager);
        inventory.addInventorySlots();
        inventory.setBounds(105, 70, 425, 400);
        
        workshopCategoryManager = new CategoryManager(compositeManager, 90, 40, 2.5f, 0.25f, "defaultLight", "infoBg2", "treeBg", false, "lastClickedWorkshopButton");
        workshopCategoryManager.addCategory(blackMarket.holderGroup, localeManager.get("workshop.market")).addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                blackMarket.update();
            }
        });
        workshopCategoryManager.addCategory(inventory.holderGroup, localeManager.get("workshop.inventory"), 0.23f).addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                inventory.update();
            }
        });
        workshopCategoryManager.addCloseButton().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                updateFire();
            }
        });
        workshopCategoryManager.setBounds(13, 67, 400);
        workshopCategoryManager.setBackgroundBounds(5, 65, 531, 410);
        workshopCategoryManager.setTableBackgroundBounds(10, 70, 95, 400);
        workshopCategoryManager.setVisible(false);
        
        CategoryManager menuCategoryManager = new CategoryManager(compositeManager, 250, 75, 2.5f, 0.5f, "defaultDark", "infoBg", "", true, "lastClickedMenuButton");
        menuCategoryManager.addCategory(playScreenTable, localeManager.get("mainMenu.play")).addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                updateFire();
                updateShip();
            }
        });
        menuCategoryManager.addCategory(playScreenTable, localeManager.get("mainMenu.playOnline"));
        menuCategoryManager.addCategory(settingsPane, localeManager.get("mainMenu.settings"));
        menuCategoryManager.addCategory(infoText, localeManager.get("mainMenu.info"));
        menuCategoryManager.addCategory(moreTable, localeManager.get("mainMenu.more"));
        menuCategoryManager.setBounds(545, 3, 400);
        menuCategoryManager.setBackgroundBounds(5, 65, 531, 410);
        menuCategoryManager.addOverrideActor(workshopCategoryManager);
        
        menuStage.addActor(ship);
        menuStage.addActor(menuBg);
        menuStage.addActor(ship_touchLayer);
        menuStage.addActor(buildNumber);
        
        menuCategoryManager.attach(menuStage);
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        Array<UpgradeMenu> upgradeMenus = new Array<>();
        for (int i = 0; i < treeJson.size; i++) {
            if (treeJson.getString("item", i, "type").equals("category")) {
                float[] coords = shipConfig.get("upgradeMenus").getFloatArray(new float[]{0, 0, 0, 0}, treeJson.get(i).name);
                upgradeMenus.add(new UpgradeMenu(
                        compositeManager, menuStage, this, upgradeMenus,
                        treeJson.get(i).name,
                        new Vector2(coords[0], coords[1]),
                        new Vector2(coords[2], coords[3])));
            }
        }
        
        menuStage.addActor(infoText);
        menuStage.addActor(playScreenTable);
        menuStage.addActor(moreTable);
        menuStage.addActor(settingsPane);
        workshopCategoryManager.attach(menuStage);
        infoText.setVisible(false);
        moreTable.setVisible(false);
        playScreenTable.setVisible(false);
        settingsPane.setVisible(false);
        
        blackMarket.attach(menuStage);
        inventory.attach(menuStage);
        
        menuStage.addActor(lamp);
        
        lastFireEffect = " ";
        updateFire();
        
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(menuStage);
        
        difficultyControl.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                difficultyT.getCells().get(1).getActor().setColor(new Color().fromHsv(120 - difficultyControl.getValue() * 20, 1.5f, 1).add(0, 0, 0, 1));
            }
        });
        
        newGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new ConfirmationDialogue(compositeManager, menuStage, localeManager.get("newGame.alert"), new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        initNewGame();
                        startWarpAnimation(menuCategoryManager, true);
                    }
                });
            }
        });
        
        continueGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getFloat(Keys.playerHealthValue) > 0) {
                    startWarpAnimation(menuCategoryManager, false);
                }
            }
        });
        
        workshop.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                workshopCategoryManager.setVisible(!workshopCategoryManager.isVisible());
            }
        });
        
        bloomT.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                enableShader = bloomT.isChecked();
                putBoolean(Keys.enableBloom, bloomT.isChecked());
            }
        });
        
        prefsLoggingT.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DUtils.logging = prefsLoggingT.isChecked();
                putBoolean(Keys.logPreferences, prefsLoggingT.isChecked());
            }
        });
        
        buildNumber.addListener(new ActorGestureListener(20, 0.4f, 60, 0.15f) {
            @Override
            public boolean longPress(Actor actor, float x, float y) {
                addInteger(Keys.moneyAmount, 100000);
                addInteger(Keys.cogAmount, 100000);
                putLong(Keys.shopLastGenerationTime, 0);
                return true;
            }
        });
        
        musicManager.setNewMusicSource("music/ambient", 1, 5, 5);
        musicManager.setVolume(getFloat(Keys.musicVolume) / 100f);
        musicVolumeS.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                putFloat(Keys.musicVolume, musicVolumeS.getValue());
                musicManager.setVolume(musicVolumeS.getValue() / 100f);
            }
        });
        
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        
        enableShader = getBoolean(Keys.enableBloom);
        log("generated ui in " + TimeUtils.timeSinceMillis(uiGenTime) + "ms", INFO);
        log("done, took " + TimeUtils.timeSinceMillis(genTime) + "ms", INFO);
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }
    
    private void updateAnimations(float delta) {
        movement += delta * (warpSpeed + 1);
        float newFireMotionScale = previousFireSizeScale;
        
        float prevAnimPos = shipUpgradeAnimationPosition;
        shipUpgradeAnimationPosition = clamp(shipUpgradeAnimationPosition + delta * shipUpgradeAnimationDirection * 1.5f, 1, targetShipScaleFactor + 1);
        float shipUpgradeAnimationPosition_normalized = (shipUpgradeAnimationPosition - 1) / targetShipScaleFactor;
        boolean upgradeAnimationActive = !(prevAnimPos - shipUpgradeAnimationPosition == 0);
        
        if (warpAnimationActive) {
            soundManager.setPitch("ftl_flight", (float) (0.5 + clamp(warpSpeed / 53.34f, 0, 1.5)));
            
            warpSpeed = clamp(warpSpeed + delta * 50, 0, 80);
            if (ship.getX() > 590) {
                soundManager.playSound("ftl");
                warpAnimationActive = false;
                soundManager.stopSound("ftl_flight");
                game.setScreen(new GameScreen(compositeManager, newGameAfterWarp));
            }
            
            newFireMotionScale = warpSpeed / 17.5f;
            if (!upgradeAnimationActive) {
                newFireMotionScale++;
            }
            
            warpXOffset += warpSpeed * delta * 5;
        }
        
        if (upgradeAnimationActive) {
            ship.setSize(originalShipWidth * shipUpgradeAnimationPosition, originalShipHeight * shipUpgradeAnimationPosition);
            
            if (warpAnimationActive) {
                newFireMotionScale += 1 - shipUpgradeAnimationPosition_normalized + 0.1f;
                warpSpeed -= shipUpgradeAnimationPosition_normalized;
            } else {
                newFireMotionScale = 1 - shipUpgradeAnimationPosition_normalized + 0.1f;
                warpSpeed = -shipUpgradeAnimationPosition_normalized;
            }
        }
        
        if (warpAnimationActive || upgradeAnimationActive) {
            ship.setPosition(lerp(50, targetShipX, shipUpgradeAnimationPosition_normalized) + warpXOffset, 279 - ship.getHeight() / 2);
            updateFirePositions();
            scaleFire(shipUpgradeAnimationPosition, newFireMotionScale);
        }
    }
    
    @Override
    public void render(float delta) {
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            if (!isConfirmationDialogActive) {
                new ConfirmationDialogue(compositeManager, menuStage, localeManager.get("exit.alert"), new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Gdx.app.exit();
                    }
                }, new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        isConfirmationDialogActive = false;
                    }
                });
            }
            isConfirmationDialogActive = true;
        }
        
        musicManager.update(delta);
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.setProjectionMatrix(camera.combined);
        
        if (enableShader) {
            blurProcessor.capture();
        }
        
        batch.begin();
        batch.enableBlending();
        batch.setColor(1, 1, 1, 1);
        drawBg(batch, bg1, bg2, warpSpeed, movement);
        
        updateAnimations(delta);
        
        for (int i = 0; i < fires.size; i++) {
            fires.get(i).draw(batch, delta);
        }
        
        if (enableShader) {
            batch.end();
            blurProcessor.render();
            batch.begin();
        }
        batch.end();
        
        if (playerHasAnimation) {
            ship.setDrawable(enemyAnimation_drawables.get(enemyAnimation.getKeyFrameIndex(animationPosition)));
            animationPosition += delta;
        }
        
        lamp.setColor(1, 1, 1, musicManager.getAmplitude());
        
        menuStage.draw();
        menuStage.act(delta);
        
        batch.begin();
        font_main.getData().setScale(0.35f);
        font_main.setColor(Color.GOLD);
        font_main.draw(batch, VERSION_NAME, 5, 35, 150, 1, false);
        
        if (drawScreenExtenders = handleDebugInput(camera, drawScreenExtenders)) {
            drawScreenExtenders(batch, fillTexture, verticalFillingThreshold, horizontalFillingThreshold);
        }
        
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        updateCamera(camera, viewport, width, height);
        int[] fillingThresholds = getVerticalAndHorizontalFillingThresholds(viewport);
        verticalFillingThreshold = fillingThresholds[0];
        horizontalFillingThreshold = fillingThresholds[1];
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
        game.getScreen().dispose();
    }
    
    @Override
    public void dispose() {
        menuStage.dispose();
        clearFireArray();
    }
    
    public void startWarpAnimation(CategoryManager categoryManager, boolean newGameAfterWarp) {
        categoryManager.closeAll();
        categoryManager.setTouchable(Touchable.disabled);
        warpAnimationActive = true;
        this.newGameAfterWarp = newGameAfterWarp;
        shipUpgradeAnimationDirection = -1;
        ship.setTouchable(Touchable.disabled);
        soundManager.playSound("ftl_flight", 0.5f);
    }
    
    private void updateFirePositions() {
        for (int i = 0; i < fires.size; i++) {
            fires.get(i).setPosition(ship.getX() + fireOffsetsX.get(i) * shipUpgradeAnimationPosition, ship.getY() + fireOffsetsY.get(i) * shipUpgradeAnimationPosition);
        }
    }
    
    private void clearFireArray() {
        for (int i = 0; i < fires.size; i++) {
            fires.get(i).free();
        }
        fires.clear();
    }
    
    public void scaleFire(float sizeScale, float motionScale) {
        for (int i = 0; i < fires.size; i++) {
            fires.get(i).scaleEffect((1 / previousFireSizeScale) * sizeScale, (1 / previousFireMotionScale) * motionScale);
        }
        previousFireSizeScale = sizeScale;
        previousFireMotionScale = motionScale;
    }
    
    private void loadFire() {
        
        int fireCount = shipConfig.getInt(1, "fireCount");
        
        clearFireArray();
        
        String fireEffect = treeJson.getString("fire_engine_left_red", getString(Keys.currentEngine), "usesEffect");
        
        for (int i = 0; i < fireCount; i++) {
            ParticleEffectPool.PooledEffect fire = particleEffectPoolLoader.getParticleEffectByPath("particles/" + fireEffect + ".p");
            fireOffsetsX.add(shipConfig.getFloat(0, "fires", "fire" + i + "OffsetX"));
            fireOffsetsY.add(shipConfig.getFloat(0, "fires", "fire" + i + "OffsetY"));
            fires.add(fire);
        }
        updateFirePositions();
        lastFireEffect = fireEffect;
    }
    
    private void updateFire() {
        try {
            if (lastFireEffect.equals(" ")) {
                loadFire();
            }
            if (!lastFireEffect.equals(treeJson.getString("fire_engine_left_red", getString(Keys.currentEngine), "usesEffect"))) {
                loadFire();
            }
            String key = treeJson.getString("fire_engine_left_red", getString(Keys.currentEngine), "usesEffect") + "_color";
            if (getString(key).equals("")) {
                setFireToDefault(key);
            } else {
                float[] colors = new JsonReader().parse("{\"colors\":" + getString(key) + "}").get("colors").asFloatArray();
                for (int i = 0; i < fires.size; i++) {
                    fires.get(i).getEmitters().get(0).getTint().setColors(colors);
                }
            }
        } catch (Exception e) {
            log("corrupted fire color array", ERROR);
            logException(e);
            setFireToDefault(treeJson.getString("fire_engine_left_red", getString(Keys.currentEngine), "usesEffect") + "_color");
        }
    }
    
    private void updateShip() {
        if (!currentShip.equals(getString(Keys.currentShip))) {
            initializeShip();
            lastFireEffect = " ";
            updateFire();
        }
    }
    
    private void initializeShip() {
        
        shipUpgradeAnimationDirection = -1;
        shipUpgradeAnimationPosition = 0;
        
        shipConfig = shipConfigs.get(getString(Keys.currentShip));
        playerHasAnimation = shipConfig.getBoolean(false, "hasAnimation");
        
        ship_touchLayer = new Image();
        ship = new Image() {
            @Override
            public void setBounds(float x, float y, float width, float height) {
                super.setBounds(x, y, width, height);
                ship_touchLayer.setBounds(x, y, width, height);
            }
            
            @Override
            public void setPosition(float x, float y) {
                super.setPosition(x, y);
                ship_touchLayer.setPosition(x, y);
            }
            
            @Override
            public void setX(float x) {
                super.setX(x);
                ship_touchLayer.setX(x);
            }
            
            @Override
            public void setSize(float width, float height) {
                super.setSize(width, height);
                ship_touchLayer.setSize(width, height);
            }
            
            @Override
            public void act(float delta) {
                super.act(delta);
                ship_touchLayer.act(delta);
            }
            
            @Override
            public void setTouchable(Touchable touchable) {
                ship_touchLayer.setTouchable(touchable);
            }
        };
        
        if (!playerHasAnimation) {
            ship.setDrawable(new TextureRegionDrawable(
                    assetManager.get("items/items.atlas", TextureAtlas.class)
                            .findRegion(getString(Keys.currentShip))));
        } else {
            enemyAnimation = new Animation<>(
                    shipConfig.getFloat(3, "frameDuration"),
                    assetManager.get("player/animations/" + shipConfig.getString("noAnimation", "animation") + ".atlas", TextureAtlas.class)
                            .findRegions(shipConfig.getString("noAnimation", "animation")),
                    Animation.PlayMode.LOOP);
            enemyAnimation_drawables = new Array<>();
            for (TextureRegion region : enemyAnimation.getKeyFrames()) {
                enemyAnimation_drawables.add(new TextureRegionDrawable(region));
            }
        }
        
        float width = shipConfig.getFloat(1, "width");
        float height = shipConfig.getFloat(1, "height");
        
        originalShipHeight = height;
        originalShipWidth = width;
        targetShipX = 230 - originalShipWidth * targetShipScaleFactor / 2f;
        ship.setBounds(50, 279 - height / 2, width, height);
        
        ship_touchLayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shipUpgradeAnimationDirection *= -1;
            }
        });
    }
    
    private void setFireToDefault(String key) {
        float[] colors = fires.get(0).getEmitters().get(0).getTint().getColors();
        
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        buffer.append(colors[0]);
        for (int i = 1; i < colors.length; i++) {
            buffer.append(", ");
            buffer.append(colors[i]);
        }
        buffer.append("]");
        putString(key, buffer.toString());
    }
    
}