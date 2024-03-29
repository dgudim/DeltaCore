package com.deo.flapd.view.dialogues;

import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.subtractInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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

public class SellScrapDialogue extends MoneyDialogue {
    
    public SellScrapDialogue(CompositeManager compositeManager, final Stage stage, final ItemSlotManager itemSlotManager, int availableQuantity, final String item) {
        
        super(compositeManager, "blankDialogue");
       
        final TextureAtlas itemAtlas = assetManager.get("items/items.atlas");
        
        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;
        
        final TextButton sell = uiComposer.addTextButton("workshopGreen", "sell", 0.48f);
        TextButton cancel = uiComposer.addTextButton("workshopRed", "cancel", 0.48f);
        sell.setBounds(344, 12, 156, 88);
        cancel.setBounds(12, 12, 156, 88);
        
        cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        
        final Array<Label> labels = new Array<>();
        Table ingredientsTable = new Table();
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        JsonEntry items = treeJson.get(item, "items");
        
        boolean loadItems = !items.isNull();
        final int[] itemCounts = new int[loadItems ? items.size : 0];
        
        if (loadItems) {
            for (int i = 0; i < items.size; i++) {
                itemCounts[i] = MathUtils.clamp(items.getInt(1, i) / 2, 1, itemCounts[i]);
            }
            for (int i = 0; i < items.size; i++) {
                String currentItem = items.get(i).name;
                Table requirement = new Table();
                Label itemText = new Label(localeManager.get(currentItem) + " " + getInteger("item_" + currentItem) + "+" + itemCounts[i], yellowLabelStyle);
                itemText.setFontScale(0.28f);
                itemText.setWrap(true);
                labels.add(itemText);
                ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
                itemButtonStyle.imageUp = new Image(itemAtlas.findRegion(currentItem)).getDrawable();
                itemButtonStyle.imageDisabled = new Image(itemAtlas.findRegion(currentItem + "_disabled")).getDrawable();
                itemButtonStyle.imageDown = new Image(itemAtlas.findRegion(currentItem + "_enabled")).getDrawable();
                itemButtonStyle.imageOver = new Image(itemAtlas.findRegion(currentItem + "_over")).getDrawable();
                ImageButton itemImageButton = new ImageButton(itemButtonStyle);
                
                requirement.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        new SellScrapDialogue(compositeManager, stage, itemSlotManager, getInteger("item_" + item), currentItem);
                    }
                });
                
                float scale = 28 / Math.max(itemImageButton.getWidth(), itemImageButton.getHeight());
                float width = itemImageButton.getWidth() * scale;
                float height = itemImageButton.getHeight() * scale;
                
                requirement.add(itemImageButton).size(width, height);
                requirement.add(itemText).pad(4).padLeft(8).width(148);
                ingredientsTable.add(requirement).padTop(4).padBottom(4).align(Align.left).row();
                ingredientsTable.align(Align.left).padLeft(4);
            }
        }
        
        ScrollPane ingredients = new ScrollPane(ingredientsTable);
        ingredients.setupOverscroll(20, 10, 30);
        ingredients.setScrollingDisabled(true, false);
        
        final int[] price = getPrice(item, treeJson, 1.5f);
        price[0] = MathUtils.clamp(price[0] / 3, 1, price[0]);
        price[1] = price[1] / 3;
        final Table requirements = new Table();
        Table holder = new Table();
        final Label uraniumCells_text = new Label(getInteger(Keys.moneyAmount) + "+" + price[0], yellowLabelStyle);
        final Label cogs_text = new Label(getInteger(Keys.cogAmount) + "+" + price[1], yellowLabelStyle);
        uraniumCells_text.setFontScale(0.32f);
        cogs_text.setFontScale(0.32f);
        
        final Image uraniumCell = new Image(itemAtlas.findRegion("uraniumCell"));
        uraniumCell.setScaling(Scaling.fit);
        holder.add(uraniumCell).size(28, 28);
        holder.add(uraniumCells_text).padLeft(4);
        requirements.add(holder).align(Align.left).padLeft(4).row();
        
        Table holder2 = new Table();
        holder2.add(new Image(assetManager.get("bonuses.atlas", TextureAtlas.class).findRegion("bonus_part"))).size(28, 28);
        holder2.add(cogs_text).padLeft(4);
        if (price[1] > 0) {
            requirements.add(holder2).align(Align.left).padLeft(4).padTop(4).row();
        }
        requirements.setBounds(320, 112, 180, 156);
        requirements.align(Align.right).padRight(1);
        
        ingredients.setBounds(12, 112, 180, 156);
        
        final CheckBox buy = uiComposer.addCheckBox("arrowRightSmall", "");
        final CheckBox scrap = uiComposer.addCheckBox("arrowLeftSmall", "");
        
        buy.setPosition(258, 116);
        scrap.setPosition(218, 116);
        buy.setChecked(true);
        
        buy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                scrap.setChecked(!buy.isChecked());
                if (scrap.isChecked()) {
                    sell.setText(localeManager.get("scrapDialogue.scrap"));
                } else {
                    sell.setText(localeManager.get("scrapDialogue.sell"));
                }
            }
        });
        
        scrap.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buy.setChecked(!scrap.isChecked());
                if (buy.isChecked()) {
                    sell.setText(localeManager.get("scrapDialogue.sell"));
                } else {
                    sell.setText(localeManager.get("scrapDialogue.scrap"));
                }
            }
        });
        
        Label endItem = new Label("", yellowLabelStyle);
        endItem.setBounds(12, 112, 200, 156);
        endItem.setFontScale(0.28f);
        endItem.setWrap(true);
        endItem.setAlignment(Align.center);
        
        scrap.setDisabled(!loadItems);
        if (!loadItems) {
            buy.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    scrap.setChecked(false);
                    buy.setChecked(true);
                    sell.setText(localeManager.get("scrapDialogue.sell"));
                }
            });
            endItem.setText(localeManager.get("scrapDialogue.cantBeScrappedMessage"));
        }
        
        Image product = new Image(itemAtlas.findRegion(item));
        product.setScaling(Scaling.fit);
        product.setBounds(226, 204, 60, 60);
        
        final Label productName = new Label(localeManager.get(item) + " " + getInteger("item_" + item) + "-1", yellowLabelStyle);
        productName.setFontScale(0.32f);
        productName.setBounds(196, 164, 120, 32);
        productName.setWrap(true);
        productName.setAlignment(Align.center);
        
        final Slider quantity = uiComposer.addSlider("sliderDefaultNormal", 1, MathUtils.clamp(availableQuantity, 1, 9999), 1);
        quantity.setBounds(186, 24, 140, 40);
        
        final Label quantityText = new Label(localeManager.get("dialogue.quantity") + ":1", yellowLabelStyle);
        quantityText.setFontScale(0.4f);
        quantityText.setPosition(196, 64);
        quantityText.setSize(120, 40);
        quantityText.setAlignment(Align.center);
        
        quantity.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                quantityText.setText(localeManager.get("dialogue.quantity") + ":" + (int) quantity.getValue());
                productName.setText(localeManager.get(item) + " " + getInteger("item_" + item) + "-" + (int) quantity.getValue());
                for (int i = 0; i < labels.size; i++) {
                    String currentItem = items.get(i).name;
                    labels.get(i).setText(localeManager.get(currentItem) + " " + getInteger("item_" + currentItem) + "+" + (int) (itemCounts[i] * quantity.getValue()));
                }
                uraniumCells_text.setText(getInteger(Keys.moneyAmount) + "+" + (int) (price[0] * quantity.getValue()));
                cogs_text.setText(getInteger(Keys.cogAmount) + "+" + (int) (price[1] * quantity.getValue()));
            }
        });
        
        sell.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (buy.isChecked()) {
                    addInteger(Keys.moneyAmount, (int) (price[0] * quantity.getValue()));
                    addInteger(Keys.cogAmount, (int) (price[1] * quantity.getValue()));
                } else {
                    for (int i = 0; i < labels.size; i++) {
                        addInteger("item_" + items.get(i).name, (int) (itemCounts[i] * quantity.getValue()));
                    }
                }
                subtractInteger("item_" + item, (int) quantity.getValue());
                itemSlotManager.update();
                dialog.hide();
            }
        });
        
        dialog.addActor(endItem);
        
        dialog.addActor(ingredients);
        dialog.addActor(requirements);
        
        dialog.addActor(product);
        dialog.addActor(productName);
        dialog.addActor(buy);
        dialog.addActor(scrap);
        
        dialog.addActor(sell);
        dialog.addActor(cancel);
        dialog.addActor(quantityText);
        dialog.addActor(quantity);
        dialog.setSize(512, 280);
        dialog.setPosition(15, 150);
        stage.addActor(dialog);
    }
}
