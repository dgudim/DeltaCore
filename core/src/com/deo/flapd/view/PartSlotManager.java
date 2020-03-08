package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;

public class PartSlotManager extends ItemSlotManager{

    TextureAtlas parts;

    public PartSlotManager(AssetManager assetManager) {
        super(assetManager);
        parts = assetManager.get("items/parts.atlas");
    }

    @Override
    public void addSlots() {
        JsonValue tree = getCraftingTree();
        boolean nextRow = false;
        for(int i = 0; i<tree.size; i++){
            addSlot(tree.get(i).name, nextRow);
            nextRow = !nextRow;
        }
    }

    @Override
    void addSlot(final String result, boolean nextRow) {

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

        Image imageUp_scaled = new Image(this.parts.findRegion(getItemCodeNameByName(result)));
        Image imageOver_scaled = new Image(this.parts.findRegion("over_"+getItemCodeNameByName(result)));
        Image imageDisabled_scaled = new Image(this.parts.findRegion("disabled"+getItemCodeNameByName(result)));
        Image imageDown_scaled = new Image(this.parts.findRegion("enabled_"+getItemCodeNameByName(result)));

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

    @Override
    JsonValue getCraftingTree() {
        JsonReader json = new JsonReader();
        return json.parse(Gdx.files.internal("items/partCraftingRecepies.json"));
    }
}
