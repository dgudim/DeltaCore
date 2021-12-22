package com.deo.flapd.view.dialogues;

import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.subtractInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.Scaling;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.view.overlays.ItemSlotManager;

public class PurchaseDialogue extends MoneyDialogue {
    
    public PurchaseDialogue(CompositeManager compositeManager, final Stage stage, final String result, int availableQuantity, int requestedQuantity, final Dialogue previousDialogue) {
        this(compositeManager, stage, result, availableQuantity, requestedQuantity, null, previousDialogue);
    }
    
    public PurchaseDialogue(CompositeManager compositeManager, final Stage stage, final String result, int availableQuantity, final ItemSlotManager itemSlotManager) {
        this(compositeManager, stage, result, availableQuantity, 1, itemSlotManager, null);
    }
    
    public PurchaseDialogue(CompositeManager compositeManager, final Stage stage, final String result, int availableQuantity, int requestedQuantity, final ItemSlotManager itemSlotManager, final Dialogue previousDialogue) {
        super(compositeManager, "craftingTerminal");
        
        requestedQuantity = MathUtils.clamp(requestedQuantity, 1, availableQuantity);
        
        final TextureAtlas itemAtlas = assetManager.get("items/items.atlas");
        
        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;
        
        TextButton yes = uiComposer.addTextButton("workshopGreen", localeManager.get("purchaseDialogue.buy"), 0.48f);
        TextButton no = uiComposer.addTextButton("workshopRed", localeManager.get("purchaseDialogue.cancel"), 0.48f);
        yes.setBounds(344, 12, 156, 88);
        no.setBounds(12, 12, 156, 88);
        
        no.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        
        final Slider quantity = uiComposer.addSlider("sliderDefaultNormal", 1, availableQuantity, 1);
        
        quantity.setBounds(186, 24, 140, 40);
        quantity.setValue(requestedQuantity);
        
        final Label quantityText = new Label(localeManager.get("dialogue.quantity") + ":" + requestedQuantity, yellowLabelStyle);
        quantityText.setFontScale(0.4f);
        quantityText.setPosition(196, 64);
        quantityText.setSize(120, 40);
        quantityText.setAlignment(Align.center);
        
        Label topText = new Label(localeManager.get("purchaseDialogue.price") + ":", yellowLabelStyle);
        topText.setPosition(76, 235);
        topText.setFontScale(0.4f);
        topText.setAlignment(Align.center);
        
        Image product = new Image(itemAtlas.findRegion(result));
        product.setBounds(352, 160, 140, 100);
        product.setScaling(Scaling.fit);
        
        final Label productName = new Label(localeManager.get(result) + " " + getInteger("item_" + result) + "+" + requestedQuantity, yellowLabelStyle);
        productName.setFontScale(0.32f);
        productName.setBounds(344, 116, 156, 40);
        productName.setWrap(true);
        productName.setAlignment(Align.center);
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        final int[] price = getPrice(result, treeJson, 1.7f);
        final Table requirements = new Table();
        Table holder = new Table();
        final Label uraniumCells_text = new Label(getInteger(Keys.moneyAmount) + "/" + price[0] * requestedQuantity, yellowLabelStyle);
        final Label cogs_text = new Label(getInteger(Keys.cogAmount) + "/" + price[1] * requestedQuantity, yellowLabelStyle);
        uraniumCells_text.setFontScale(0.52f);
        cogs_text.setFontScale(0.52f);
        
        if (getInteger(Keys.moneyAmount) < price[0] * requestedQuantity) {
            uraniumCells_text.setColor(Color.valueOf("#DD0000"));
        }
        
        if (getInteger(Keys.cogAmount) < price[1] * requestedQuantity) {
            cogs_text.setColor(Color.valueOf("#DD0000"));
        }
        
        final Image uraniumCell = new Image(itemAtlas.findRegion("uraniumCell"));
        uraniumCell.setScaling(Scaling.fit);
        holder.add(uraniumCell).size(40, 40);
        holder.add(uraniumCells_text).padLeft(4);
        requirements.add(holder).align(Align.left).padLeft(4).row();
        
        Table holder2 = new Table();
        holder2.add(new Image(assetManager.get("bonuses.atlas", TextureAtlas.class).findRegion("bonus_part"))).size(40, 40);
        holder2.add(cogs_text).padLeft(4);
        if (price[1] > 0) {
            requirements.add(holder2).align(Align.left).padLeft(4).padTop(4).row();
        }
        requirements.align(Align.left);
        requirements.setBounds(12, 112, 320, 156);
        
        quantity.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                quantityText.setText(localeManager.get("dialogue.quantity") + ":" + (int) quantity.getValue());
                productName.setText(localeManager.get(result) + " " + getInteger("item_" + result) + "+" + (int) (quantity.getValue()));
                uraniumCells_text.setText(getInteger(Keys.moneyAmount) + "/" + (int) (price[0] * quantity.getValue()));
                cogs_text.setText(getInteger(Keys.cogAmount) + "/" + (int) (price[1] * quantity.getValue()));
                if (getInteger(Keys.moneyAmount) < price[0] * quantity.getValue()) {
                    uraniumCells_text.setColor(Color.valueOf("#DD0000"));
                } else {
                    uraniumCells_text.setColor(Color.YELLOW);
                }
                
                if (getInteger(Keys.cogAmount) < price[1] * quantity.getValue()) {
                    cogs_text.setColor(Color.valueOf("#DD0000"));
                } else {
                    cogs_text.setColor(Color.YELLOW);
                }
            }
        });
        
        yes.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getInteger(Keys.moneyAmount) >= price[0] * quantity.getValue() && getInteger(Keys.cogAmount) >= price[1] * quantity.getValue()) {
                    subtractInteger(Keys.moneyAmount, (int) (price[0] * quantity.getValue()));
                    subtractInteger(Keys.cogAmount, (int) (price[1] * quantity.getValue()));
                    addInteger("item_" + result, (int) quantity.getValue());
                    
                    JsonEntry slotsJson = new JsonEntry(new JsonReader().parse("{\"slots\":" + getString(Keys.savedShopSlots) + "," + "\"productQuantities\":" + getString(Keys.savedShopSlotsQuantities) + "}"));
                    Array<String> items = new Array<>();
                    items.addAll(slotsJson.getStringArray(new String[]{}, "slots"));
                    Array<Integer> quantities = new Array<>();
                    
                    for (int i = 0; i < slotsJson.getIntArray(new int[]{}, "productQuantities").length; i++) {
                        quantities.add(slotsJson.getIntArray(new int[]{}, "productQuantities")[i]);
                    }
                    
                    int index = items.indexOf(result, false);
                    
                    quantities.set(index, (int) (quantities.get(index) - quantity.getValue()));
                    
                    if (quantities.get(index) == 0) {
                        items.removeIndex(index);
                        quantities.removeIndex(index);
                    }
                    
                    putString(Keys.savedShopSlots, items.toString());
                    putString(Keys.savedShopSlotsQuantities, quantities.toString());
                    
                    if (itemSlotManager != null) {
                        itemSlotManager.update();
                    }
                    if (previousDialogue != null) {
                        previousDialogue.update();
                    }
                    dialog.hide();
                }
            }
        });
        
        Button question = uiComposer.addButton("questionButton");
        question.setBounds(476, 244, 24, 24);
        
        question.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new CraftingDialogue(compositeManager, stage, result, true);
            }
        });
        
        dialog.addActor(requirements);
        dialog.addActor(yes);
        dialog.addActor(no);
        dialog.addActor(quantity);
        dialog.addActor(topText);
        dialog.addActor(quantityText);
        dialog.addActor(product);
        dialog.addActor(productName);
        dialog.addActor(question);
        dialog.setSize(512, 280);
        dialog.setPosition(15, 150);
        stage.addActor(dialog);
    }
}
