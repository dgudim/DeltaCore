package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
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
import com.deo.flapd.utils.DUtils;

public class SlotManager{

    private BitmapFont font;
    private Table table;
    private Skin slotSkin;
    private TextureAtlas items;
    private ScrollPane scrollPane;
    private Stage stage;
    private AssetManager assetManager;
    private Array<ImageButton> slots;
    private Array<String> results;
    private SlotManager slotManager;

    public SlotManager(AssetManager assetManager){

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

        slotManager = this;
    }

    public void addSlots(String category){
        switch (category){
            case("items"):
                JsonValue tree = getCraftingTree();
                boolean nextRow = false;
                for(int i = 0; i<tree.size; i++){
                   addSlot(tree.get(i).name, nextRow);
                    nextRow = !nextRow;
                }
                break;
            case("parts"):
                break;
        }
    }

    private void addSlot(final String result, boolean nextRow){

        ImageButton.ImageButtonStyle slotStyle;
        ImageButton.ImageButtonStyle lockedSlotStyle;

        final boolean locked = !DUtils.getBoolean("enabled_" + getItemCodeNameByName(result));

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
                new CraftingDialogue(stage, assetManager, result, locked, false, slotManager, null);
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

    private String getItemCodeNameByName(String name){
        String item = "ohno";
        switch (name){
            case("coloring crystal"):
                item = "crystal";
                break;
            case("ore"):
            case("prism"):
            case("bolt"):
            case("cable"):
            case("cog"):
            case("plastic"):
            case("transistor"):
            case("wire"):
            case("rubber"):
                item = name;
                break;
            case("iron shard"):
                item = "ironShard";
                break;
            case("iron plate"):
                item = "ironPlate";
                break;
            case("glass shard"):
                item = "glassShard";
                break;
            case("cyan warp shard"):
                item = "bonus_warp";
                break;
            case("green warp shard"):
                item = "bonus_warp2";
                break;
            case("red crystal"):
                item = "redCrystal";
                break;
            case("energy cell"):
                item = "energyCell";
                break;
            case("core shard"):
                item = "fragment_core";
                break;
            case("green coil"):
                item = "green_coil";
                break;
            case("cyan coil"):
                item = "neon_coil";
                break;
            case("cyan crystal"):
                item = "cyanCrystal";
                break;
            case("orange crystal"):
                item = "orangeCrystal";
                break;
            case("green crystal"):
                item = "greenCrystal";
                break;
            case("purple crystal"):
                item = "purpleCrystal";
                break;
            case("drone engine"):
                item = "drone_engine";
                break;
            case("red fuel cell"):
                item = "fuelCell";
                break;
            case("cyan fuel cell"):
                item = "fuelCell2";
                break;
            case("motherboard"):
                item = "chipset";
                break;
            case("motherboard lvl2"):
                item = "chipset_big";
                break;
            case("energy crystal"):
                item = "energyCrystal";
                break;
            case("blue ore"):
                item = "warp_ore";
                break;
            case("crafting card"):
                item = "craftingCard";
                break;
            case("memory cell"):
                item = "cell";
                break;
            case("memory cell lvl2"):
                item = "cell2";
                break;
            case("cyan blank card"):
                item = "card1";
                break;
            case("orange blank card"):
                item = "card2";
                break;
            case("ai card"):
                item = "aiCard";
                break;
            case("ai processor"):
                item = "aiChip";
                break;
            case("processor"):
                item = "processor1";
                break;
            case("processor lvl2"):
                item = "processor2";
                break;
            case("processor lvl3"):
                item = "processor3";
                break;
            case("reinforced iron plate"):
                item = "ironPlate2";
                break;
            case("memory card"):
                item = "memoryCard";
                break;
            case("screen card"):
                item = "screenCard";
                break;
            case("green core"):
                item = "warpCore";
                break;
            case("yellow core"):
                item = "core_yellow";
                break;
            case("laser emitter"):
                item = "bonus laser";
                break;
            case("laser coil"):
                item = "gun";
                break;
            case("fiber cable"):
                item = "cable_fiber";
                break;
            case("advanced chip"):
                item = "advancedChip";
                break;
            case("circuit board"):
                item = "Circuit_board";
                break;
            case("cooling unit"):
                item = "coolingUnit";
                break;
        }
        return item;
    }

    public void setBounds(float x, float y, float width, float height) {
        scrollPane.setBounds(x, y, width, height);
    }

    public void setVisible(boolean visible){
        scrollPane.setVisible(visible);
    }

    private JsonValue getCraftingTree(){
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
                new CraftingDialogue(stage, assetManager, result_name, false, false, slotManager, null);
            }
        });
    }

}
