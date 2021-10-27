package com.deo.flapd.view.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.SoundManager;
import com.deo.flapd.utils.ui.UIComposer;
import com.deo.flapd.view.dialogues.CraftingDialogue;
import com.deo.flapd.view.dialogues.DialogueActionListener;
import com.deo.flapd.view.screens.MenuScreen;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.connectFortyFiveDegreeBranch;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.scaleDrawables;

public class UpgradeMenu extends Actor {
    
    private final Drawable up_current;
    private final Drawable over_current;
    private final Drawable down_current;
    private final Drawable up_available;
    private final Drawable over_available;
    private final Drawable down_available;
    private final Drawable up;
    private final Drawable over;
    private final Drawable down;
    
    CompositeManager compositeManager;
    AssetManager assetManager;
    TextureAtlas itemsAtlas;
    LocaleManager localeManager;
    UIComposer uiComposer;
    SoundManager soundManager;
    
    private final Array<Image> connectingBranches;
    private final Array<Float> originalBranchHeights;
    private final Array<Vector2> originalBranchPositions;
    
    CheckBox openMenuCheckBox;
    Image lastBranch;
    boolean opensToTheLeft;
    ScrollPane itemSelection_outerHolder;
    float itemSelectionHeight = 200;
    
    Array<String> parts;
    Array<ImageTextButton> partsButtons;
    String saveTo;
    
    private final float branchThickness = 4;
    private byte animationDirection = -1;
    private float animationPosition = 0;
    
    private final Image ship;
    private final float targetShipScaleFactor;
    private final Vector2 originalShipPosition;
    private final Vector2 originalShipDimensions;
    private final Vector2 originalAnchorPosition;
    
    JsonEntry treeJson;
    
    private final MenuScreen menuScreen;
    
    public UpgradeMenu(CompositeManager compositeManager, Stage stage, MenuScreen menuScreen, Array<UpgradeMenu> upgradeMenus, String category, Vector2 anchorPosition, Vector2 targetPosition) {
        up = constructFilledImageWithColor(1, 1, Color.valueOf("#44444444"));
        over = constructFilledImageWithColor(1, 1, Color.valueOf("#66666644"));
        down = constructFilledImageWithColor(1, 1, Color.valueOf("#88888844"));
        
        up_current = constructFilledImageWithColor(1, 1, Color.valueOf("#11AA1144"));
        over_current = constructFilledImageWithColor(1, 1, Color.valueOf("#33CC3344"));
        down_current = constructFilledImageWithColor(1, 1, Color.valueOf("#55FF5544"));
        
        up_available = constructFilledImageWithColor(1, 1, Color.valueOf("#AAAA1144"));
        over_available = constructFilledImageWithColor(1, 1, Color.valueOf("#CCCC3344"));
        down_available = constructFilledImageWithColor(1, 1, Color.valueOf("#FFFF5544"));
        
        treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        parts = new Array<>();
        partsButtons = new Array<>();
        
        this.menuScreen = menuScreen;
        this.compositeManager = compositeManager;
        uiComposer = compositeManager.getUiComposer();
        assetManager = compositeManager.getAssetManager();
        itemsAtlas = assetManager.get("items/items.atlas", TextureAtlas.class);
        localeManager = compositeManager.getLocaleManager();
        soundManager = compositeManager.getSoundManager();
        opensToTheLeft = targetPosition.x < 0;
        
        connectingBranches = new Array<>();
        originalBranchHeights = new Array<>();
        originalBranchPositions = new Array<>();
        
        ship = menuScreen.ship;
        targetShipScaleFactor = menuScreen.targetShipScaleFactor + 1;
        originalShipPosition = new Vector2(ship.getX(), ship.getY());
        originalShipDimensions = new Vector2(ship.getWidth(), ship.getHeight());
        originalAnchorPosition = anchorPosition;
        
        Vector2 anchorPosition_shifted = new Vector2(ship.getX() + anchorPosition.x, ship.getY() + anchorPosition.y);
        Vector2 targetPosition_shifted = new Vector2(anchorPosition_shifted.x + targetPosition.x, anchorPosition_shifted.y + targetPosition.y);
        
        openMenuCheckBox = uiComposer.addCheckBox("circle", "");
        openMenuCheckBox.setTouchable(Touchable.disabled);
        openMenuCheckBox.setColor(1, 1, 1, 0);
        
        openMenuCheckBox.setPosition(anchorPosition_shifted.x + 1.5f, anchorPosition_shifted.y, Align.center);
        
        openMenuCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animationDirection *= -1;
                if (animationDirection == 1) {
                    for (int i = 0; i < upgradeMenus.size; i++) {
                        if (upgradeMenus.get(i) != UpgradeMenu.this) {
                            upgradeMenus.get(i).close();
                        }
                    }
                }
            }
        });
        
        Table holder = new Table();
        connectFortyFiveDegreeBranch(anchorPosition_shifted.x, anchorPosition_shifted.y, targetPosition_shifted.x, targetPosition_shifted.y, branchThickness, Color.valueOf("#97d760"), connectingBranches, holder);
        for (int i = 0; i < connectingBranches.size; i++) {
            originalBranchHeights.add(connectingBranches.get(i).getHeight());
            originalBranchPositions.add(new Vector2(connectingBranches.get(i).getX(), connectingBranches.get(i).getY()));
            connectingBranches.get(i).setHeight(0);
        }
        lastBranch = connectingBranches.get(connectingBranches.size - 1);
        float width = originalBranchHeights.get(connectingBranches.size - 1);
        
        stage.addActor(holder);
        stage.addActor(openMenuCheckBox);
        
        saveTo = treeJson.getString("errorSaving" + category, category, "saveTo");
        
        Table itemSelectionTable = new Table();
        loadItemsForCategory(category, itemSelectionTable, width, true);
        for (int i = 0; i < treeJson.size; i++) {
            if (treeJson.getString(false, "", i, "category").equals(category) && treeJson.getString("", i, "type").equals("subcategory")) {
                loadItemsForCategory(treeJson.get(i).name, itemSelectionTable, width, false);
            }
        }
        updateStates();
        
        itemSelectionTable.align(Align.topLeft);
        ScrollPane itemSelection = new ScrollPane(itemSelectionTable);
        itemSelection.setupOverscroll(7, 7, 30);
        
        Table itemSelectionHolderTable = new Table();
        itemSelectionHolderTable.setBackground(new NinePatchDrawable(assetManager.get("ui/menuUi.atlas", TextureAtlas.class).createPatch("tableBg")));
        itemSelectionHolderTable.add(itemSelection).size(width - 8, itemSelectionHeight - 8);
        
        itemSelection_outerHolder = new ScrollPane(itemSelectionHolderTable);
        itemSelection_outerHolder.setScrollingDisabled(true, true);
        itemSelection_outerHolder.setSize(width, 0);
        itemSelection_outerHolder.setupOverscroll(0, 0, 0);
        
        stage.addActor(itemSelection_outerHolder);
        stage.addActor(this);
    }
    
    void loadItemsForCategory(String category, Table addTo, float width, boolean root) {
        Table titleTable = new Table();
        titleTable.align(Align.left);
        titleTable.setBackground(constructFilledImageWithColor(1, 1, Color.valueOf(root ? "#262626" : "#464646")));
        if (!root) {
            Image subcategoryIcon = new Image(itemsAtlas.findRegion(category));
            titleTable.add(subcategoryIcon).padRight(5).padLeft(5).size(20);
        }
        Label categoryLabel = uiComposer.addText(localeManager.get(category), assetManager.get("fonts/pixel.ttf"), 0.48f);
        categoryLabel.getStyle().fontColor = Color.valueOf(root ? "#ffb121" : "#ffd343");
        categoryLabel.setFillParent(true);
        categoryLabel.setAlignment(root ? Align.center : Align.left);
        titleTable.add(categoryLabel);
        addTo.add(titleTable).width(width - 8).row();
        
        for (int i = 0; i < treeJson.size; i++) {
            String type = treeJson.getString("", i, "type");
            if (treeJson.getString(false, "", i, "category").equals(category) && (type.equals("part") || type.equals("basePart"))) {
                
                ImageTextButton.ImageTextButtonStyle itemButtonStyle = new ImageTextButton.ImageTextButtonStyle();
                itemButtonStyle.imageUp = new Image(itemsAtlas.findRegion(treeJson.get(i).name)).getDrawable();
                itemButtonStyle.imageDisabled = new Image(itemsAtlas.findRegion(treeJson.get(i).name + "_disabled")).getDrawable();
                itemButtonStyle.imageDown = new Image(itemsAtlas.findRegion(treeJson.get(i).name + "_enabled")).getDrawable();
                itemButtonStyle.imageOver = new Image(itemsAtlas.findRegion(treeJson.get(i).name + "_over")).getDrawable();
                itemButtonStyle.font = assetManager.get("fonts/pixel.ttf");
                itemButtonStyle.fontColor = Color.valueOf("#CCCCCC");
                itemButtonStyle.overFontColor = Color.valueOf("#DDDDDD");
                itemButtonStyle.downFontColor = Color.WHITE;
                ImageTextButton textImageButton = new ImageTextButton(localeManager.get(treeJson.get(i).name), itemButtonStyle);
                // TODO: 27/10/2021 fix smatt font after game
                textImageButton.setDebug(true, true);
                parts.add(treeJson.get(i).name);
                partsButtons.add(textImageButton);
                
                int finalI = i;
                textImageButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        soundManager.playSound_noLink("click");
                        new CraftingDialogue(compositeManager, addTo.getStage(), treeJson.get(finalI).name, new DialogueActionListener() {
                            @Override
                            public void onConfirm() {
                                updateStates();
                                updateShip();
                            }
                            @Override
                            public void onStateChanged() {
                                updateStates();
                                updateShip();
                            }
                        }, UpgradeMenu.this);
                    }
                });
                
                scaleDrawables(80, itemButtonStyle.imageUp, itemButtonStyle.imageDown, itemButtonStyle.imageDisabled, itemButtonStyle.imageOver);
                textImageButton.align(Align.left);
                textImageButton.getImageCell().width(80);
                textImageButton.getLabel().setAlignment(Align.left);
                
                addTo.add(textImageButton).width(width - 8).padTop(5).row();
            }
        }
    }
    
    public void updateFire(){
        menuScreen.updateFire();
    }
    
    public void updateShip(){
        menuScreen.updateShip();
    }
    
    void updateStates() {
        for (int i = 0; i < parts.size; i++) {
            if (parts.get(i).equals(getString(saveTo))) {
                partsButtons.get(i).getStyle().up = up_current;
                partsButtons.get(i).getStyle().over = over_current;
                partsButtons.get(i).getStyle().down = down_current;
            } else if (getBoolean("unlocked_" + parts.get(i))) {
                partsButtons.get(i).getStyle().up = up;
                partsButtons.get(i).getStyle().over = over;
                partsButtons.get(i).getStyle().down = down;
            } else if (isPartUnlocked(parts.get(i))) {
                partsButtons.get(i).getStyle().up = up_available;
                partsButtons.get(i).getStyle().over = over_available;
                partsButtons.get(i).getStyle().down = down_available;
            }
            partsButtons.get(i).setDisabled(!isPartUnlocked(parts.get(i)));
        }
    }
    
    private boolean isPartUnlocked(String part) {
        for (String requiredItem : treeJson.getStringArray(false, new String[]{}, part, "requires")) {
            if (!getBoolean("unlocked_" + requiredItem)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        itemSelection_outerHolder.setVisible(visible);
        openMenuCheckBox.setVisible(visible);
        for (int i = 0; i < connectingBranches.size; i++) {
            connectingBranches.get(i).setVisible(visible);
        }
    }
    
    public void close() {
        openMenuCheckBox.setChecked(false);
        animationDirection = -1;
    }
    
    void animate(float delta) {
        float prevAnimPos = animationPosition;
        animationPosition = clamp(animationPosition + delta * animationDirection * 4, 0, 2);
        if (!(prevAnimPos - animationPosition == 0)) {
            for (int branch = 0; branch < connectingBranches.size; branch++) {
                connectingBranches.get(branch).setHeight(clamp(animationPosition * connectingBranches.size - branch, 0, 1) * originalBranchHeights.get(branch));
            }
            itemSelection_outerHolder.setHeight(clamp(animationPosition - 1, 0, 1) * itemSelectionHeight);
        }
    }
    
    @Override
    public void act(float delta) {
        animate(delta);
        float scale = ship.getHeight() / originalShipDimensions.y;
        if (scale != 1) {
            float xOffset = ship.getX() - originalShipPosition.x;
            float yOffset = ship.getY() - originalShipPosition.y;
            
            xOffset += originalAnchorPosition.x * (scale - 1);
            yOffset += originalAnchorPosition.y * (scale - 1);
            
            Color color = new Color(1, 1, 1, clamp(scale - targetShipScaleFactor + 1, 0, 1));
            
            for (int branch = 0; branch < connectingBranches.size; branch++) {
                connectingBranches.get(branch).setPosition(
                        originalBranchPositions.get(branch).x + xOffset,
                        originalBranchPositions.get(branch).y + yOffset);
                connectingBranches.get(branch).setColor(color);
            }
            itemSelection_outerHolder.setPosition(lastBranch.getX() - itemSelection_outerHolder.getWidth() * (opensToTheLeft ? 1 : 0), lastBranch.getY() - itemSelection_outerHolder.getHeight() - branchThickness * (opensToTheLeft ? 0 : 1));
            itemSelection_outerHolder.setColor(color);
            
            openMenuCheckBox.setPosition(
                    ship.getX() + originalAnchorPosition.x * scale + 1.5f,
                    ship.getY() + originalAnchorPosition.y * scale, Align.center);
            
            openMenuCheckBox.setColor(color);
            
            if (color.a > 0.8 && openMenuCheckBox.getTouchable() != Touchable.enabled) {
                openMenuCheckBox.setTouchable(Touchable.enabled);
            } else if (color.a <= 0.8 && openMenuCheckBox.getTouchable() != Touchable.disabled) {
                openMenuCheckBox.setTouchable(Touchable.disabled);
                close();
            }
        }
    }
}
