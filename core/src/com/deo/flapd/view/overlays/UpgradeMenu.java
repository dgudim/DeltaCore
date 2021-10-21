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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.ui.UIComposer;
import com.deo.flapd.view.screens.MenuScreen;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.connectFortyFiveDegreeBranch;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;

public class UpgradeMenu extends Actor {
    
    private final Array<Image> connectingBranches;
    private final Array<Float> originalBranchHeights;
    private final Array<Vector2> originalBranchPositions;
    
    CheckBox openMenuCheckBox;
    Image lastBranch;
    boolean opensToTheLeft;
    ScrollPane itemSelection_outerHolder;
    float itemSelectionHeight = 200;
    
    private final float branchThickness = 4;
    private byte animationDirection = -1;
    private float animationPosition = 0;
    
    private final Image ship;
    private final float targetShipScaleFactor;
    private final Vector2 originalShipPosition;
    private final Vector2 originalShipDimensions;
    private final Vector2 originalAnchorPosition;
    
    public UpgradeMenu(CompositeManager compositeManager, Stage stage, MenuScreen menuScreen, Array<UpgradeMenu> upgradeMenus, String category, Vector2 anchorPosition, Vector2 targetPosition) {
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        
        UIComposer uiComposer = compositeManager.getUiComposer();
        AssetManager assetManager = compositeManager.getAssetManager();
        LocaleManager localeManager = compositeManager.getLocaleManager();
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
        
        Table itemSelectionTable = new Table();
        Label rootCategoryLabel = uiComposer.addText(localeManager.get(category), assetManager.get("fonts/pixel.ttf"), 0.48f);
        rootCategoryLabel.getStyle().background = constructFilledImageWithColor(1, 1, Color.DARK_GRAY);
        itemSelectionTable.add(rootCategoryLabel).align(Align.left).width(width - 8).row();
        Array<String> subcategories = new Array<>();
        for (int i = 0; i < treeJson.size; i++) {
            if (treeJson.getString("", i, "category").equals(category) && treeJson.getString("", i, "type").equals("subcategory")) {
                subcategories.add(treeJson.get(i).name);
                Label subcategoryLabel = uiComposer.addText(localeManager.get(treeJson.get(i).name), assetManager.get("fonts/pixel.ttf"), 0.48f);
                itemSelectionTable.add(subcategoryLabel).align(Align.left).padLeft(10).row();
            }
        }
        
        itemSelectionTable.align(Align.topLeft);
        ScrollPane itemSelection = new ScrollPane(itemSelectionTable);
        
        Table itemSelectionHolderTable = new Table();
        itemSelectionHolderTable.setBackground(new NinePatchDrawable(assetManager.get("ui/menuUi.atlas", TextureAtlas.class).createPatch("tableBg")));
        itemSelectionHolderTable.add(itemSelection).size(width - 8, itemSelectionHeight - 8);
        
        itemSelection_outerHolder = new ScrollPane(itemSelectionHolderTable);
        itemSelection_outerHolder.setScrollingDisabled(true, true);
        itemSelection_outerHolder.setSize(width, 0);
        
        stage.addActor(itemSelection_outerHolder);
        stage.addActor(this);
    }
    
    void close() {
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
