package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;

import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getLong;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putLong;
import static com.deo.flapd.utils.DUtils.putString;

public class ItemSlotManager {

    private BitmapFont font;
    private Table table;
    private Skin slotSkin;
    private TextureAtlas items;
    ScrollPane scrollPane;
    private Stage stage;
    private AssetManager assetManager;

    ItemSlotManager(AssetManager assetManager){

        slotSkin = new Skin();
        slotSkin.addRegions((TextureAtlas)assetManager.get("shop/workshop.atlas"));

        this.assetManager = assetManager;

        items = assetManager.get("items/items.atlas");

        font = assetManager.get("fonts/font2(old).fnt");
        font.getData().setScale(0.2f);

        table = new Table();

        scrollPane = new ScrollPane(table);

        long lastGenerationTime = getLong("lastGenTime");
        if(TimeUtils.timeSinceMillis(lastGenerationTime)>18000000){
            putLong("lastGenTime", TimeUtils.millis());
            generateSlots();
        }else{
            loadSlots();
        }
    }

    private void generateSlots(){
        Array<String> itemsToAdd = new Array<>();
        JsonValue tree = new JsonReader().parse(Gdx.files.internal("items/tree.json"));
        Array<Integer> quantities= new Array<>();

        for(int i = 0; i<tree.size; i++){
            if(tree.get(i).get("category").asString().equals("recepies")){
                itemsToAdd.add(tree.get(i).name);
            }
        }

        boolean nextRow = false;
        Array<String> addedItems = new Array<>();
        int slotQuantity = getRandomInRange(itemsToAdd.size/4, itemsToAdd.size/2);
        for(int i = 0; i<slotQuantity; i++){
            int index = getRandomInRange(0, itemsToAdd.size-1);
            int quantity = getRandomInRange(5, 15);
            addedItems.add(itemsToAdd.get(index));
            quantities.add(quantity);
            addSlot(itemsToAdd.get(index), quantity, nextRow);
            itemsToAdd.removeIndex(index);
            nextRow = !nextRow;
        }

        putString("savedSlots", "{\"slots\":" + addedItems.toString() + ","+ "\"productQuantities\":" + quantities.toString() + "}");
    }

    private void loadSlots(){
        JsonValue slotsJson = new JsonReader().parse(getString("savedSlots"));
        int[] productQuantities = slotsJson.get("productQuantities").asIntArray();
        String[] slotNames = slotsJson.get("slots").asStringArray();
        boolean nextRow = false;
        for(int i = 0; i<productQuantities.length; i++){
            addSlot(slotNames[i], productQuantities[i], nextRow);
            nextRow = !nextRow;
        }
    }

    private void addSlot(final String result, final int availableQuantity, boolean nextRow){

        ImageButton.ImageButtonStyle slotStyle;
        ImageButton.ImageButtonStyle lockedSlotStyle;

        lockedSlotStyle = new ImageButton.ImageButtonStyle();
        lockedSlotStyle.up = slotSkin.getDrawable("slot_disabled");
        lockedSlotStyle.down = slotSkin.getDrawable("slot_disabled_down");
        lockedSlotStyle.over = slotSkin.getDrawable("slot_disabled_over");

        slotStyle = new ImageButton.ImageButtonStyle();
        slotStyle.up = slotSkin.getDrawable("slot");
        slotStyle.over = slotSkin.getDrawable("slot_over");
        slotStyle.down = slotSkin.getDrawable("slot_enabled");

        Image imageUp_scaled = new Image(this.items.findRegion(getItemCodeNameByName(result)));
        Image imageOver_scaled = new Image(this.items.findRegion("over_"+getItemCodeNameByName(result)));
        Image imageDisabled_scaled = new Image(this.items.findRegion("disabled"+getItemCodeNameByName(result)));
        Image imageDown_scaled = new Image(this.items.findRegion("enabled_"+getItemCodeNameByName(result)));

        float heightBefore = imageUp_scaled.getHeight();
        float widthBefore = imageUp_scaled.getWidth();
        float height = 64;
        float width = (height/heightBefore)*widthBefore;

        imageDisabled_scaled.setSize(width, height);
        imageDown_scaled.setSize(width, height);
        imageOver_scaled.setSize(width, height);
        imageUp_scaled.setSize(width, height);

        slotStyle.imageUp = imageUp_scaled.getDrawable();
        slotStyle.imageOver = imageOver_scaled.getDrawable();
        slotStyle.imageDown = imageDown_scaled.getDrawable();

        lockedSlotStyle.imageUp = imageDisabled_scaled.getDrawable();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;

        ImageButton slot = new ImageButton(slotStyle);

        Label text = new Label(result, labelStyle);
        text.setFontScale(0.28f, 0.3f);
        text.setColor(Color.YELLOW);

        Label quantity = new Label(""+availableQuantity, labelStyle);
        quantity.setFontScale(0.4f);
        quantity.setColor(Color.YELLOW);
        quantity.setBounds(133, 88, 50, 20);
        quantity.setAlignment(Align.right);

        slot.getImageCell().size(width, height).padBottom(5).row();
        slot.add(text).padRight(10).padLeft(10).padTop(10);
        slot.addActor(quantity);

        slot.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new PurchaseDialogue(assetManager, stage, result, availableQuantity, ItemSlotManager.this);
            }
        });

        if(nextRow) {
            table.add(slot).padBottom(5).padTop(5).padLeft(5).size(205, 120);
            table.row();
        }else{
            table.add(slot).padBottom(5).padTop(5).padRight(5).size(205, 120);
        }
    }

    void attach(Stage stage){
        stage.addActor(scrollPane);
        this.stage = stage;
    }

    public void setBounds(float x, float y, float width, float height) {
        scrollPane.setBounds(x, y, width, height);
    }

    void update(){
        table.clearChildren();
        loadSlots();
    }
}
