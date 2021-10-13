package com.deo.flapd.view.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.TimeUtils;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;

import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.connectNinetyDegreeBranch;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.log;

public class Tree {
    
    private final CompositeManager compositeManager;
    
    private final Table treeTable;
    private final Array<Array<Node>> nodes;
    private final float height;
    private int branchCount;
    public ScrollPane treeScrollView;
    private final JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
    
    public Tree(CompositeManager compositeManager, float x, float y, float width, float height) {
        this.height = height;
        this.compositeManager = compositeManager;
        AssetManager assetManager = compositeManager.getAssetManager();
        
        treeTable = new Table();
        treeTable.setTransform(true);
        treeTable.setLayoutEnabled(false);
        nodes = new Array<>();
        
        addBase();
        Array<Array<String>> items = new Array<>();
        Array<String> categories = new Array<>();
        
        log("parsing and building crafting tree", INFO);
        long loadingTime = TimeUtils.millis();
        
        for (int i = 0; i < treeJson.size; i++) {
            if (!categories.contains(treeJson.getString("noCategory", i, "category"), false)) {
                categories.add(treeJson.getString("noCategory", i, "category"));
                Array<String> items1 = new Array<>();
                items1.add(treeJson.get(i).name);
                items.add(items1);
            } else {
                int index = categories.indexOf(treeJson.getString("noCategory", i, "category"), false);
                items.get(index).add(treeJson.get(i).name);
            }
        }
        
        addItems(categories, items, "root", nodes.get(0).get(0), nodes.get(0).get(0).node.getY() + 10);
        
        int targetWidth = 0;
        
        for (int i = 0; i < nodes.size; i++) {
            for (int i2 = 0; i2 < nodes.get(i).size; i2++) {
                treeTable.addActor(nodes.get(i).get(i2).node);
                if (nodes.get(i).get(i2).node.getX() > targetWidth) {
                    targetWidth = (int) (nodes.get(i).get(i2).node.getX() + 50);
                }
            }
        }
        
        targetWidth += 5;
        
        treeTable.add(new Image(constructFilledImageWithColor(targetWidth, 1, Color.CLEAR)));
        
        for (int i = 0; i < nodes.get(0).get(0).children.size; i++) {
            nodes.get(0).get(0).children.get(i).setVisible(false, false);
        }
        
        nodes.get(0).get(0).initialize().update();
        
        float elapsedTime = TimeUtils.timeSinceMillis(loadingTime);
        log("done, took " + elapsedTime + "ms, total branch count " + (branchCount - 1), INFO);
        
        Skin buttonSkin = new Skin();
        buttonSkin.addRegions(assetManager.get("menuButtons/menuButtons.atlas"));
        
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        
        scrollPaneStyle.background = new NinePatchDrawable(assetManager.get("ui/menuUi.atlas", TextureAtlas.class).createPatch("9bg"));
        
        scrollPaneStyle.hScrollKnob = constructFilledImageWithColor(4, 5, Color.DARK_GRAY);
        
        treeScrollView = new ScrollPane(treeTable, scrollPaneStyle);
        treeScrollView.setupOverscroll(10, 10, 30);
        treeScrollView.setBounds(x, y, width, height);
    }
    
    private void addBase() {
        Array<Node> base = new Array<>();
        base.add(new Node(compositeManager, "root", 5, height - 85, 70, 70, treeTable, treeJson));
        nodes.add(base);
    }
    
    private void addItems(Array<String> categories, Array<Array<String>> items, String category, Node root, float maxHeight) {
        int index = categories.indexOf(category, false);
        if (index >= 0) {
            Array<String> currentCategoryItems = items.get(index);
            Array<Node> currentCategoryNodes = new Array<>();
            
            for (int i = 0; i < currentCategoryItems.size; i++) {
                Node nextNode = new Node(compositeManager, currentCategoryItems.get(i), root.node.getX() + 85, maxHeight - 55 * i, 50, 50, treeTable, treeJson);
                addItems(categories, items, nextNode.name, nextNode, maxHeight);
                currentCategoryNodes.add(nextNode);
                branchCount++;
            }
            
            root.assignChildren(currentCategoryNodes, category.equals("root"));
            nodes.add(currentCategoryNodes);
        } else {
            loadCraftingRecipe(category, root, maxHeight);
        }
    }
    
    private void loadCraftingRecipe(String item, Node root, float maxHeight) {
        String[] items = getRequiredItemsFromCraftingTree(item);
        if (items.length > 0) {
            Array<Node> currentCategoryNodes = new Array<>();
            
            for (int i = 0; i < items.length; i++) {
                Node nextNode = new Node(compositeManager, items[i], root.node.getX() + 80, maxHeight - 55 * i, 50, 50, treeTable, treeJson);
                loadCraftingRecipe(items[i], nextNode, maxHeight);
                currentCategoryNodes.add(nextNode);
                branchCount++;
            }
            
            root.assignChildren(currentCategoryNodes, false);
            nodes.add(currentCategoryNodes);
        }
    }
    
    private String[] getRequiredItemsFromCraftingTree(String result) {
        return treeJson.getStringArray(new String[]{}, result, "items");
    }
    
    public void hide() {
        treeScrollView.setVisible(false);
    }
    
    public void attach(Stage stage) {
        stage.addActor(treeScrollView);
    }
    
    public void update() {
        new Thread(() -> nodes.get(0).get(0).updateRoot()).start();
    }
    
}

class Node {
    
    ImageButton node;
    private final Table holder;
    private final Vector2 bounds;
    private final Array<Image> branches;
    Array<Node> children;
    private Node parent;
    String name;
    private final AssetManager assetManager;
    private int requestedQuantity = 1;
    private Label quantity;
    private final JsonEntry treeJson;
    private float resultCount = 1;
    
    Node(CompositeManager compositeManager, final String item, float x, float y, float width, float height, final Table holder, JsonEntry treeJson) {
        this.holder = holder;
        this.assetManager = compositeManager.getAssetManager();
        this.treeJson = treeJson;
        name = item;
        bounds = new Vector2(x + width / 2f, y + height / 2f);
        branches = new Array<>();
        children = new Array<>();
        final TextureAtlas items = assetManager.get("items/items.atlas");
        Skin nodeSkin = new Skin();
        nodeSkin.addRegions(assetManager.get("shop/slots.atlas"));
        ImageButton.ImageButtonStyle nodeStyle = new ImageButton.ImageButtonStyle();
        //nodeStyle.imageUp = new Image(items.findRegion(getItemTextureNameByName(item))).getDrawable();
        //nodeStyle.imageOver = new Image(items.findRegion(getItemTextureNameByName(item, OVER))).getDrawable();
        //nodeStyle.imageDown = new Image(items.findRegion(getItemTextureNameByName(item, ENABLED))).getDrawable();
        //nodeStyle.imageDisabled = new Image(items.findRegion(getItemTextureNameByName(item, DISABLED))).getDrawable();
        nodeStyle.up = nodeSkin.getDrawable("slot");
        nodeStyle.down = nodeSkin.getDrawable("enabled_slot");
        nodeStyle.over = nodeSkin.getDrawable("over_slot");
        nodeStyle.disabled = nodeSkin.getDrawable("disabled_slot");
        node = new ImageButton(nodeStyle) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                try {
                    super.draw(batch, parentAlpha);
                } catch (Exception e) {
                    //log("error drawing " +
                            //getItemTextureNameByName(item) +
                            //"\nnormal: " + items.findRegion(getItemTextureNameByName(item)) +
                            //"\nover: " + items.findRegion(getItemTextureNameByName(item, OVER)) +
                            //"\nenabled: " + items.findRegion(getItemTextureNameByName(item, ENABLED)) +
                            //"\ndisabled: " + items.findRegion(getItemTextureNameByName(item, DISABLED)), CRITICAL_ERROR);
                }
            }
        };
        if (name.equals("root")) {
            node.setTouchable(Touchable.disabled);
        }
        node.setStyle(nodeStyle);
        node.getImageCell().size(width * 0.75f, height * 0.75f).align(Align.center);
        node.setColor(getCategoryTint());
        node.setBounds(x, y, width, height);
        
        Label.LabelStyle descriptionStyle = new Label.LabelStyle();
        descriptionStyle.font = assetManager.get("fonts/pixel.ttf");
        descriptionStyle.fontColor = Color.YELLOW;
        
        node.setDisabled(getPartLockState());
        
        node.addListener(new ActorGestureListener(20, 0.4f, 0.6f, 0.15f) {
            @Override
            public boolean longPress(Actor actor, float x, float y) {
                //new CraftingDialogue(compositeManager, holder.getStage(), name, (int) Math.ceil((requestedQuantity - getInteger("item_" + getItemTextureNameByName(name))) / resultCount));
                return true;
            }
            
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                hideNeighbours();
                setVisible(true, false);
            }
        });
    }
    
    private void connectBranch(Array<Node> nodes, boolean startFromMiddle) {
        for (int i = 0; i < nodes.size; i++) {
            connectNinetyDegreeBranch(bounds.x, bounds.y, nodes.get(i).bounds.x, nodes.get(i).bounds.y, 4, startFromMiddle, Color.GREEN, branches, holder);
        }
    }
    
    void setVisible(boolean visible, boolean hideItself) {
        for (int i = 0; i < branches.size; i++) {
            branches.get(i).setVisible(visible);
        }
        if (visible) {
            for (int i = 0; i < children.size; i++) {
                children.get(i).node.setVisible(true);
            }
        } else {
            node.setVisible(!hideItself);
            for (int i = 0; i < children.size; i++) {
                if (children.get(i).node.isVisible()) {
                    children.get(i).setVisible(false, true);
                }
            }
        }
    }
    
    private void hideNeighbours() {
        for (int i = 0; i < parent.children.size; i++) {
            if (!this.equals(parent.children.get(i)) && parent.children.get(i).node.isVisible()) {
                parent.children.get(i).setVisible(false, false);
            }
        }
    }
    
    void assignChildren(Array<Node> children, boolean connectFromMiddle) {
        this.children = children;
        for (int i = 0; i < children.size; i++) {
            children.get(i).parent = this;
            children.get(i).addQuantityLabel();
        }
        connectBranch(children, connectFromMiddle);
    }
    
    private void addQuantityLabel() {
        if (getType().equals("item") || getType().equals("endItem")) {
            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = assetManager.get("fonts/pixel.ttf");
            Label quantity = new Label("", labelStyle);
            quantity.setFontScale(0.2f);
            quantity.setAlignment(Align.bottomLeft);
            quantity.setPosition(7, 3);
            node.addActor(quantity);
            this.quantity = quantity;
        }
    }
    
    Node initialize() {
        if (getType().equals("item") || getType().equals("endItem")) {
            resultCount = getResultCount(false);
        }
        for (int i = 0; i < children.size; i++) {
            children.get(i).initialize();
        }
        return this;
    }
    
    void updateRoot() {
        Node root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        root.update();
    }
    
    void update() {
        if (getType().equals("item") || getType().equals("endItem")) {
            //requestedQuantity = MathUtils.ceil(getRequestedQuantity() * MathUtils.clamp(parent.requestedQuantity - getInteger("item_" + getItemTextureNameByName(parent.name)), 1, 10000) / getResultCount(true));
            //quantity.setText("" + getInteger("item_" + getItemTextureNameByName(name)) + "/" + requestedQuantity);
            //if (getInteger("item_" + getItemTextureNameByName(name)) >= requestedQuantity) {
            //    quantity.setColor(Color.YELLOW);
            //} else {
            //    quantity.setColor(Color.ORANGE);
            //}
        }
        node.setDisabled(getPartLockState());
        node.setColor(getCategoryTint());
        for (int i = 0; i < children.size; i++) {
            children.get(i).update();
        }
    }
    
    private Color getCategoryTint() {
        Color color = Color.WHITE;
        switch (getType()) {
            case ("basePart"):
                if (getString(treeJson.getString("noSaveToLocation", name, "saveTo")).equals(name)) {
                    color = Color.GREEN;
                }
                break;
            case ("baseCategory"):
                color = Color.GREEN;
                break;
            case ("part"):
                color = Color.SKY;
                if (getString(treeJson.getString("noSaveToLocation", name, "saveTo")).equals(name)) {
                    color = Color.GREEN;
                }
                break;
            case ("item"):
                color = Color.CYAN;
                break;
            case ("endItem"):
                color = Color.YELLOW;
                break;
            default:
                color = Color.WHITE;
                break;
        }
        return color;
    }
    
    private boolean getPartLockState() {
        boolean locked = false;
        if (getType().equals("part")) {
            String[] requiredItems = treeJson.getStringArray(new String[]{}, name, "requires");
            for (String requiredItem : requiredItems) {
                //locked = !getBoolean("unlocked_" + getItemTextureNameByName(requiredItem));
                if (locked) {
                    break;
                }
            }
        }
        return locked;
    }
    
    private String getType() {
        if (name.equals("root")) {
            return name;
        } else {
            if (treeJson.get(name).isNull())
                throw new IllegalArgumentException("no item declared with name " + name);
            return treeJson.getString("item", name, "type");
        }
    }
    
    private int getRequestedQuantity() {
        if (parent.children.indexOf(this, false) >= parent.getRequiredItems().length)
            throw new IllegalArgumentException("item count mismatch at " + parent.name);
        return parent.getRequiredItems()[parent.children.indexOf(this, false)];
    }
    
    private int[] getRequiredItems() {
        return treeJson.getIntArray(new int[]{}, name, "itemCounts");
    }
    
    private float getResultCount(boolean parent) {
        if (parent) {
            return treeJson.getInt(1, this.parent.name, "resultCount");
        } else {
            return treeJson.getInt(1, name, "resultCount");
        }
    }
}
