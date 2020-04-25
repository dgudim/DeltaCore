package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.log;

class Tree {

    private AssetManager assetManager;
    private Table treeTable;
    private Array<Array<Node>> nodes;
    private float height;
    private int branchCount;
    ScrollPane treeScrollView;
    private JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("shop/tree.json"));

    Tree(AssetManager assetManager, float x, float y, float width, float height){
        this.height = height;
        treeTable = new Table();
        treeTable.setTransform(true);
        treeTable.setLayoutEnabled(false);
        nodes = new Array<>();
        this.assetManager = assetManager;
        addBase();
        Array<String> categories;
        Array<Array<String>> items;
        items = new Array<>();
        categories = new Array<>();

        log("\n parsing and building crafting tree");
        long loadingTime = TimeUtils.millis();

        for(int i = 0; i<treeJson.size; i++){
            if(!categories.contains(treeJson.get(i).get("category").asString(), false)) {
                categories.add(treeJson.get(i).get("category").asString());
                Array<String> items1 = new Array<>();
                items1.add(treeJson.get(i).name);
                items.add(items1);
            }else{
                int index = categories.indexOf(treeJson.get(i).get("category").asString(), false);
                items.get(index).add(treeJson.get(i).name);
            }
        }

        addItems(categories, items, "root", nodes.get(0).get(0), nodes.get(0).get(0).node.getY()+10);

        int targetWidth = 0;

        for (int i = 0; i < nodes.size; i++){
            for(int i2 = 0; i2 < nodes.get(i).size; i2++){
                treeTable.addActor(nodes.get(i).get(i2).node);
                if(nodes.get(i).get(i2).node.getX() > targetWidth){
                    targetWidth = (int)(nodes.get(i).get(i2).node.getX()+50);
                }
            }
        }

        targetWidth +=5;

        Pixmap pixmap0 = new Pixmap(targetWidth, 1, Pixmap.Format.RGBA8888);
        pixmap0.setColor(Color.CLEAR);
        pixmap0.fill();
        TextureRegionDrawable branchBg = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap0)));
        pixmap0.dispose();
        treeTable.add(new Image(branchBg));

        for (int i = 0; i < nodes.get(0).get(0).children.size; i++){
            nodes.get(0).get(0).children.get(i).setVisible(false, false);
        }

        nodes.get(0).get(0).initialize().update();

        float elapsedTime = TimeUtils.timeSinceMillis(loadingTime);
        float relativePercentage = (100 - elapsedTime/(branchCount-1) * 100f / 0.29f);
        if(relativePercentage>=0){
            log("\n done, elapsed time " + elapsedTime + "ms(" + relativePercentage + "% better than average), total branch count " + (branchCount-1));
        }else{
            log("\n done, elapsed time " + elapsedTime + "ms(" + -relativePercentage + "% worse than average), total branch count " + (branchCount-1));
        }

        Skin buttonSkin = new Skin();
        buttonSkin.addRegions((TextureAtlas)assetManager.get("menuButtons/menuButtons.atlas"));

        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = new NinePatchDrawable(new NinePatch((Texture)assetManager.get("9bg.png"), 5, 5, 5, 5));

        Pixmap pixmap = new Pixmap(4, 5, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fill();
        TextureRegionDrawable scrollBar = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        scrollPaneStyle.hScrollKnob = scrollBar;

        treeScrollView = new ScrollPane(treeTable, scrollPaneStyle);
        treeScrollView.setBounds(x, y, width, height);
    }

    private void addBase(){
        Array<Node> base = new Array<>();
        base.add(new Node(assetManager, "ship", 5, height-85, 70, 70, treeTable, treeJson));
        nodes.add(base);
    }

    private void addItems(Array<String> categories, Array<Array<String>> items, String category, Node root, float maxHeight){
        int index = categories.indexOf(category, false);
        if(index>=0) {
            Array<String> currentCategories = items.get(index);
            Array<Node> currentCategoryNodes = new Array<>();

            for (int i = 0; i < currentCategories.size; i++) {
                Node nextNode = new Node(assetManager, currentCategories.get(i), root.node.getX() + 85, maxHeight - 55 * i, 50, 50, treeTable, treeJson);
                addItems(categories, items, nextNode.name, nextNode, maxHeight);
                currentCategoryNodes.add(nextNode);
                branchCount++;
            }

            root.assignChildren(currentCategoryNodes, category.equals("root"));
            nodes.add(currentCategoryNodes);
        }else{
            loadCraftingRecipe(category, root, maxHeight);
        }
    }

    private void loadCraftingRecipe(String item, Node root, float maxHeight){
        String[] items = getRequiredItemsFromCraftingTree(item);
        if(!items[0].equals("")) {
            Array<Node> currentCategoryNodes = new Array<>();

            for (int i = 0; i < items.length; i++) {
                Node nextNode = new Node(assetManager, items[i], root.node.getX() + 80, maxHeight - 55 * i, 50, 50, treeTable, treeJson);
                loadCraftingRecipe(items[i], nextNode, maxHeight);
                currentCategoryNodes.add(nextNode);
                branchCount++;
            }

            root.assignChildren(currentCategoryNodes, false);
            nodes.add(currentCategoryNodes);
        }
    }

    private String[] getRequiredItemsFromCraftingTree(String result){
        return treeJson.get(result).get("items").asStringArray();
    }

    void hide(){
        treeScrollView.setVisible(false);
    }

    void attach(Stage stage){
        stage.addActor(treeScrollView);
    }

    void update(){
        new Thread(){
            @Override
            public void run() {
                nodes.get(0).get(0).updateRoot();
            }
        }.start();
    }

}

class Node {

    ImageButton node;
    private Table holder;
    private Vector2 bounds;
    private Array<Image> branches;
    Array<Node> children;
    private Node parent;
    String name;
    private AssetManager assetManager;
    private int requestedQuantity = 1;
    private Label quantity;
    private JsonValue treeJson;
    private float resultCount = 1;

    Node (final AssetManager assetManager, final String item, float x, float y, float width, float height, final Table holder, JsonValue treeJson){
        this.holder = holder;
        this.assetManager = assetManager;
        this.treeJson = treeJson;
        name = item;
        bounds = new Vector2(x+width/2f, y+height/2f);
        branches = new Array<>();
        children = new Array<>();
        final TextureAtlas items = assetManager.get("items/items.atlas");
        Skin nodeSkin = new Skin();
        nodeSkin.addRegions((TextureAtlas)assetManager.get("shop/slots.atlas"));
        ImageButton.ImageButtonStyle nodeStyle = new ImageButton.ImageButtonStyle();
        nodeStyle.imageUp = new Image(items.findRegion(getItemCodeNameByName(item))).getDrawable();
        nodeStyle.imageOver = new Image(items.findRegion("over_"+getItemCodeNameByName(item))).getDrawable();
        nodeStyle.imageDown = new Image(items.findRegion("enabled_"+getItemCodeNameByName(item))).getDrawable();
        nodeStyle.imageDisabled = new Image(items.findRegion("disabled_"+getItemCodeNameByName(item))).getDrawable();
        nodeStyle.up = nodeSkin.getDrawable("slot");
        nodeStyle.down = nodeSkin.getDrawable("enabled_slot");
        nodeStyle.over = nodeSkin.getDrawable("over_slot");
        nodeStyle.disabled= nodeSkin.getDrawable("disabled_slot");
        node = new ImageButton(nodeStyle){
            @Override
            public void draw(Batch batch, float parentAlpha) {
                try {
                    super.draw(batch, parentAlpha);
                } catch (Exception e) {
                    log("\n error drawing "+getItemCodeNameByName(item) + "\n" +items.findRegion(getItemCodeNameByName(item))+ "\n" +items.findRegion("over_"+getItemCodeNameByName(item))+ "\n" +items.findRegion("enabled_"+getItemCodeNameByName(item))+ "\n" +items.findRegion("disabled_"+getItemCodeNameByName(item)));
                }
            }
        };
        switch (item){
            case("ship"):
                nodeStyle.imageUp = new Image((Texture) assetManager.get("ship.png")).getDrawable();
                node.setTouchable(Touchable.disabled);
                break;
            case("shields"):
                nodeStyle.imageUp = new Image((Texture) assetManager.get("ColdShield.png")).getDrawable();
                nodeStyle.imageDown = new Image((Texture) assetManager.get("enabled_ColdShield.png")).getDrawable();
                nodeStyle.imageOver = new Image((Texture) assetManager.get("over_ColdShield.png")).getDrawable();
                nodeStyle.imageDisabled = new Image((Texture) assetManager.get("disabledColdShield.png")).getDrawable();
                break;
        }
        node.setStyle(nodeStyle);
        node.getImageCell().size(width*0.75f, height*0.75f).align(Align.center);
        node.setColor(getCategoryTint());
        node.setBounds(x, y, width, height);

        Label.LabelStyle descriptionStyle = new Label.LabelStyle();
        descriptionStyle.font = assetManager.get("fonts/font2(old).fnt");
        descriptionStyle.fontColor = Color.YELLOW;

        node.setDisabled(getPartLockState());

        node.addListener(new ActorGestureListener(20, 0.4f, 0.6f, 0.15f){
            @Override
            public boolean longPress(Actor actor, float x, float y) {
                new CraftingDialogue(holder.getStage(), assetManager, name, (int)Math.ceil((requestedQuantity-getInteger("item_"+getItemCodeNameByName(name)))/resultCount), false, null);
                return true;
            }
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                hideNeighbours();
                setVisible(true, false);
            }
        });
    }

    private void connectBranch(Array<Node> nodes, boolean startFromMiddle){
        for (int i = 0; i < nodes.size; i++){
            connectNinetyDegreeBranch(bounds.x, bounds.y, nodes.get(i).bounds.x, nodes.get(i).bounds.y, startFromMiddle);
        }
    }

    private void connectBranch(float x, float y, float x2, float y2){
        Pixmap pixmap0 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap0.setColor(Color.GREEN);
        pixmap0.fill();
        TextureRegionDrawable branchBg = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap0)));
        pixmap0.dispose();

        Image branch = new Image(branchBg);
        float len1, len2, thickness;
        thickness = 4f;
        len1 = x2 - x;
        len2 = y2 - y;
        if(len1 == 0){
            branch.setSize(thickness, len2);
            x-=thickness/2;
        }else if(len2 == 0){
            branch.setSize(len1, thickness);
            y-=thickness/2;
        }
        branch.setPosition(x, y);
        holder.addActor(branch);
        branches.add(branch);
    }

    private void connectNinetyDegreeBranch(float x, float y, float x2, float y2, boolean startFromMiddle){
        float jlen = Math.abs(x - x2)/2f;
        if (startFromMiddle){
            connectBranch(x, y, x, y2);
            connectBranch(x, y2, x2, y2);
        }else {
            connectBranch(x, y, x + jlen, y);
            connectBranch(x + jlen, y, x + jlen, y2);
            connectBranch(x + jlen, y2, x2, y2);
        }
    }

    void setVisible(boolean visible, boolean hideItself){
        for (int i = 0; i < branches.size; i++) {
            branches.get(i).setVisible(visible);
        }
        if(visible) {
            for (int i = 0; i < children.size; i++) {
                children.get(i).node.setVisible(true);
            }
        }else{
            node.setVisible(!hideItself);
            for (int i = 0; i < children.size; i++) {
                if(children.get(i).node.isVisible()) {
                    children.get(i).setVisible(false, true);
                }
            }
        }
    }

    private void hideNeighbours(){
        for (int i = 0; i<parent.children.size; i++){
            if(!this.equals(parent.children.get(i)) && parent.children.get(i).node.isVisible()) {
                parent.children.get(i).setVisible(false, false);
            }
        }
    }

    void assignChildren(Array<Node> children, boolean connectFromMiddle){
        this.children = children;
        for (int i = 0; i<children.size; i++){
            children.get(i).parent = this;
            children.get(i).addQuantityLabel();
        }
        connectBranch(children, connectFromMiddle);
    }

    private void addQuantityLabel(){
        if (getType().equals("item") || getType().equals("endItem")) {
            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = assetManager.get("fonts/font2(old).fnt");
            Label quantity = new Label("", labelStyle);
            quantity.setFontScale(0.2f);
            quantity.setAlignment(Align.bottomLeft);
            quantity.setPosition(7, 3);
            node.addActor(quantity);
            this.quantity = quantity;
        }
    }

    Node initialize(){
        if (getType().equals("item") || getType().equals("endItem")) {
            resultCount = getResultCount(false);
        }
        for (int i = 0; i<children.size; i++){
            children.get(i).initialize();
        }
        return this;
    }

    void updateRoot(){
        Node root = this;
        while(root.parent != null){
            root = root.parent;
        }
        root.update();
    }

    void update(){
        if (getType().equals("item") || getType().equals("endItem")) {
            requestedQuantity = MathUtils.ceil(getRequestedQuantity()*MathUtils.clamp(parent.requestedQuantity-getInteger("item_"+getItemCodeNameByName(parent.name)), 1, 10000)/getResultCount(true));
            quantity.setText(""+getInteger("item_"+getItemCodeNameByName(name))+"/"+requestedQuantity);
            if(getInteger("item_"+getItemCodeNameByName(name))>=requestedQuantity){
                quantity.setColor(Color.YELLOW);
            }else{
                quantity.setColor(Color.ORANGE);
            }
        }
        node.setDisabled(getPartLockState());
        node.setColor(getCategoryTint());
        for (int i = 0; i<children.size; i++){
            children.get(i).update();
        }
    }

    private Color getCategoryTint(){
        Color color = Color.WHITE;
            switch (getType()) {
                case ("basePart"):
                    if(getString(treeJson.get(name).get("saveTo").asString()).equals(name)){
                        color = Color.GREEN;
                    }
                    break;
                case ("baseCategory"):
                    color = Color.GREEN;
                    break;
                case ("part"):
                    color = Color.SKY;
                    if(getString(treeJson.get(name).get("saveTo").asString()).equals(name)){
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

    private boolean getPartLockState(){
        boolean locked = false;
        if(getType().equals("part")) {
            String[] requiredItems = treeJson.get(name).get("requires").asStringArray();
            for (int i = 0; i < requiredItems.length; i++) {
                locked = !getBoolean("unlocked_" + getItemCodeNameByName(requiredItems[i]));
                if (locked) {
                    break;
                }
            }
        }
        return locked;
    }

    private String getType(){
        if(name.equals("ship")) {
            return "root";
        }else {
            if(treeJson.get(name) == null) throw new IllegalArgumentException("no item declared with name "+name);
            return treeJson.get(name).get("type").asString();
        }
    }

    private int getRequestedQuantity(){
        if(parent.children.indexOf(this, false)>=parent.getRequiredItems().length) throw new IllegalArgumentException("item count mismatch at "+parent.name);
        return parent.getRequiredItems()[parent.children.indexOf(this, false)];
    }

    private int[] getRequiredItems(){
        return treeJson.get(name).get("itemCounts").asIntArray();
    }

    private float getResultCount(boolean parent){
        if(parent) {
            return treeJson.get(this.parent.name).get("resultCount").asInt();
        }else{
            return treeJson.get(name).get("resultCount").asInt();
        }
    }
}
