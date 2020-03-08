package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;

public class ItemSlotManager {

    BitmapFont font;
    Table table;
    Skin slotSkin;
    TextureAtlas items;
    ScrollPane scrollPane;
    Stage stage;
    AssetManager assetManager;
    Array<ImageButton> slots;
    Array<String> results;
    ItemSlotManager itemSlotManager;

    public ItemSlotManager(AssetManager assetManager){

        slotSkin = new Skin();
        slotSkin.addRegions((TextureAtlas)assetManager.get("shop/workshop.atlas"));

        this.assetManager = assetManager;

        items = assetManager.get("items/items.atlas");

        font = assetManager.get("fonts/font2.fnt");
        font.getData().setScale(0.2f);

        table = new Table();

        scrollPane = new ScrollPane(table);

        slots = new Array<>();
        results = new Array<>();

        itemSlotManager = this;
    }

    public void addSlots(){
        Array<String> notCraftableItems = new Array<>();
        JsonValue tree = getCraftingTree();
        boolean nextRow = false;
        for(int i = 0; i<tree.size; i++){
            if(isCraftable(tree.get(i).name)) {
                addSlot(tree.get(i).name, nextRow);
                nextRow = !nextRow;
            }else{
                notCraftableItems.add(tree.get(i).name);
            }
        }
        for(int i = 0; i<notCraftableItems.size; i++){
            addSlot(notCraftableItems.get(i), nextRow);
            nextRow = !nextRow;
        }
    }

    void addSlot(final String result, boolean nextRow){

        ImageButton.ImageButtonStyle slotStyle;
        ImageButton.ImageButtonStyle lockedSlotStyle;

        final boolean locked = !getBoolean("enabled_" + getItemCodeNameByName(result));

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
        if(locked){
            slot.setStyle(lockedSlotStyle);
        }
        if(!isCraftable(result)){
            slot.setColor(0.85f, 0.85f, 1f, 1);
        }

        Label text = new Label(result, labelStyle);
        if(locked) {
            text.setColor(Color.GRAY);
        }else{
            text.setColor(Color.YELLOW);
        }
        slot.getImageCell().size(width, height).padBottom(5).row();
        slot.add(text).padRight(10).padLeft(10).padTop(10);

        slot.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new CraftingDialogue(stage, assetManager, result, 1, locked, false, itemSlotManager, null);
            }
        });

        slots.add(slot);
        results.add(getItemCodeNameByName(result));

        table.add(slot).pad(5).size(210, 120);
        if(nextRow){
            table.row();
        }
    }

    public void attach(Stage stage){
        stage.addActor(scrollPane);
        this.stage = stage;
    }

    public void draw(Batch batch, float parentAlpha, float delta) {
        batch.begin();
        table.draw(batch, parentAlpha);
        table.act(delta);
        batch.end();
    }

    public void setBounds(float x, float y, float width, float height) {
        scrollPane.setBounds(x, y, width, height);
    }

    JsonValue getCraftingTree(){
        JsonReader json = new JsonReader();
        return json.parse(Gdx.files.internal("items/craftingRecepies.json"));
    }

    public void unlockSlot(final String result_name){

        String result = getItemCodeNameByName(result_name);

        int i = results.indexOf(result, false);

        Image imageUp_scaled = new Image(this.items.findRegion(result));
        Image imageOver_scaled = new Image(this.items.findRegion("over_"+result));
        Image imageDown_scaled = new Image(this.items.findRegion("enabled_"+result));

        float heightBefore = imageUp_scaled.getHeight();
        float widthBefore = imageUp_scaled.getWidth();
        float height = 64;
        float width = (height/heightBefore)*widthBefore;

        imageDown_scaled.setSize(width, height);
        imageOver_scaled.setSize(width, height);
        imageUp_scaled.setSize(width, height);

        slots.get(i).getStyle().imageUp = imageUp_scaled.getDrawable();
        slots.get(i).getStyle().imageDown = imageDown_scaled.getDrawable();
        slots.get(i).getStyle().imageOver = imageOver_scaled.getDrawable();
        slots.get(i).getStyle().up = slotSkin.getDrawable("slot");
        slots.get(i).getStyle().over = slotSkin.getDrawable("slot_over");
        slots.get(i).getStyle().down = slotSkin.getDrawable("slot_enabled");
        slots.get(i).getCells().get(1).getActor().setColor(Color.YELLOW);

        slots.get(i).removeListener(slots.get(i).getListeners().get(2));
        slots.get(i).addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new CraftingDialogue(stage, assetManager, result_name, 1, false, false, itemSlotManager, null);
            }
        });
    }

    private boolean isCraftable(String result){
        JsonReader json = new JsonReader();
        JsonValue base = json.parse(Gdx.files.internal("items/craftingRecepies.json"));
        JsonValue craftingState = base.get(result).get("isCraftable");

        return craftingState.asBoolean();
    }
}
