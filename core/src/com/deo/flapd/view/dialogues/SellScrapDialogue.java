package com.deo.flapd.view.dialogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.Scaling;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.ui.UIComposer;
import com.deo.flapd.view.overlays.ItemSlotManager;

import static com.deo.flapd.utils.DUtils.ItemTextureModifier.DISABLED;
import static com.deo.flapd.utils.DUtils.ItemTextureModifier.ENABLED;
import static com.deo.flapd.utils.DUtils.ItemTextureModifier.OVER;
import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemTextureNameByName;
import static com.deo.flapd.utils.DUtils.subtractInteger;

public class SellScrapDialogue extends MoneyDialogue {
    
    public SellScrapDialogue(CompositeManager compositeManager, final Stage stage, final ItemSlotManager itemSlotManager, int availableQuantity, final String item) {
        
        AssetManager assetManager = compositeManager.getAssetManager();
        
        BitmapFont font = assetManager.get("fonts/font2(old).fnt");
        Skin skin = new Skin();
        skin.addRegions(assetManager.get("shop/workshop.atlas"));
        
        UIComposer uiComposer = new UIComposer(compositeManager);
        uiComposer.loadStyles("workshopRed", "workshopGreen", "workshopPurple", "sliderDefaultSmall", "arrowRightSmall", "arrowLeftSmall");
        
        final TextureAtlas itemAtlas = assetManager.get("items/items.atlas");
        
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.background = skin.getDrawable("blankDialogue");
        final Dialog dialog = new Dialog("", dialogStyle);
        
        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;
        
        final TextButton sell = uiComposer.addTextButton("workshopGreen", "sell", 0.12f);
        TextButton cancel = uiComposer.addTextButton("workshopRed", "cancel", 0.12f);
        sell.setBounds(86, 3, 39, 22);
        cancel.setBounds(3, 3, 39, 22);
        
        cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        
        final Array<Label> labels = new Array<>();
        Table ingredientsTable = new Table();
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        final String[] items = treeJson.getStringArray(new String[]{}, item, "items");
        final int[] itemCounts = treeJson.getIntArray(new int[]{}, item, "itemCounts");
        for (int i = 0; i < itemCounts.length; i++) {
            itemCounts[i] = MathUtils.clamp(itemCounts[i] / 2, 1, itemCounts[i]);
        }
        
        boolean isEndItem = false;
        
        if (items[0].equals("")) {
            isEndItem = true;
        }
        
        if (!isEndItem) {
            for (int i = 0; i < items.length; i++) {
                Table requirement = new Table();
                Label itemText = new Label(items[i] + " " + getInteger("item_" + getItemTextureNameByName(items[i])) + "+" + itemCounts[i], yellowLabelStyle);
                itemText.setFontScale(0.07f);
                itemText.setWrap(true);
                labels.add(itemText);
                ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
                itemButtonStyle.imageUp = new Image(itemAtlas.findRegion(getItemTextureNameByName(items[i]))).getDrawable();
                itemButtonStyle.imageDisabled = new Image(itemAtlas.findRegion(getItemTextureNameByName(items[i], DISABLED))).getDrawable();
                itemButtonStyle.imageDown = new Image(itemAtlas.findRegion(getItemTextureNameByName(items[i], ENABLED))).getDrawable();
                itemButtonStyle.imageOver = new Image(itemAtlas.findRegion(getItemTextureNameByName(items[i], OVER))).getDrawable();
                ImageButton itemImageButton = new ImageButton(itemButtonStyle);
                final int finalI = i;
                
                requirement.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        new SellScrapDialogue(compositeManager, stage, itemSlotManager, getInteger("item_" + getItemTextureNameByName(item)), items[finalI]);
                    }
                });
                
                float scale = 7 / Math.max(itemImageButton.getWidth(), itemImageButton.getHeight());
                float width = itemImageButton.getWidth() * scale;
                float height = itemImageButton.getHeight() * scale;
                
                requirement.add(itemImageButton).size(width, height);
                requirement.add(itemText).pad(1).padLeft(2).width(37);
                ingredientsTable.add(requirement).padTop(1).padBottom(1).align(Align.left).row();
                ingredientsTable.align(Align.left).padLeft(1);
            }
        }
        
        ScrollPane ingredients = new ScrollPane(ingredientsTable);
        ingredients.setupOverscroll(5, 10, 30);
        ingredients.setScrollingDisabled(true, false);
        
        final int[] price = getPrice(item, treeJson, 1.5f);
        price[0] = MathUtils.clamp(price[0] / 3, 1, price[0]);
        price[1] = price[1] / 3;
        final Table requirements = new Table();
        Table holder = new Table();
        final Label uraniumCells_text = new Label(getInteger("money") + "+" + price[0], yellowLabelStyle);
        final Label cogs_text = new Label(getInteger("cogs") + "+" + price[1], yellowLabelStyle);
        uraniumCells_text.setFontScale(0.08f);
        cogs_text.setFontScale(0.08f);
        
        final Image uraniumCell = new Image((Texture) assetManager.get("uraniumCell.png"));
        uraniumCell.setScaling(Scaling.fit);
        holder.add(uraniumCell).size(7, 7);
        holder.add(uraniumCells_text).padLeft(1);
        requirements.add(holder).align(Align.left).padLeft(1).row();
        
        Table holder2 = new Table();
        holder2.add(new Image(assetManager.get("bonuses.atlas", TextureAtlas.class).findRegion("bonus_part"))).size(7, 7);
        holder2.add(cogs_text).padLeft(1);
        if (price[1] > 0) {
            requirements.add(holder2).align(Align.left).padLeft(1).padTop(1).row();
        }
        requirements.setBounds(80, 28, 45, 39);
        requirements.align(Align.right).padRight(1);
        
        ingredients.setBounds(3, 28, 45, 39);
        
        final CheckBox buy = uiComposer.addCheckBox("arrowRightSmall", "");
        final CheckBox scrap = uiComposer.addCheckBox("arrowLeftSmall", "");
        
        buy.setBounds(66, 29, 10, 10);
        scrap.setBounds(56, 29, 10, 10);
        buy.setChecked(true);
        
        buy.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                scrap.setChecked(!buy.isChecked());
                if (scrap.isChecked()) {
                    sell.setText("scrap");
                } else {
                    sell.setText("sell");
                }
            }
        });
        
        scrap.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buy.setChecked(!scrap.isChecked());
                if (buy.isChecked()) {
                    sell.setText("sell");
                } else {
                    sell.setText("scrap");
                }
            }
        });
        
        Label endItem = new Label("", yellowLabelStyle);
        endItem.setBounds(3, 28, 50, 39);
        endItem.setFontScale(0.07f);
        endItem.setWrap(true);
        endItem.setAlignment(Align.center);
        
        scrap.setDisabled(isEndItem);
        if (isEndItem) {
            buy.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    scrap.setChecked(false);
                    buy.setChecked(true);
                    sell.setText("sell");
                }
            });
            endItem.setText("this item can't be scrapped");
        }
        
        Label or = new Label("or", yellowLabelStyle);
        or.setPosition(61.5f, 23);
        or.setFontScale(0.1f);
        
        Image product = new Image(itemAtlas.findRegion(getItemTextureNameByName(item)));
        product.setScaling(Scaling.fit);
        product.setBounds(56.5f, 51, 15, 15);
        
        final Label productName = new Label(item + " " + getInteger("item_" + getItemTextureNameByName(item)) + "-1", yellowLabelStyle);
        productName.setFontScale(0.08f);
        productName.setBounds(49, 41, 30, 8);
        productName.setWrap(true);
        productName.setAlignment(Align.center);
        
        final Slider quantity = uiComposer.addSlider("sliderDefaultSmall", 1, MathUtils.clamp(availableQuantity, 1, 9999), 1);
        quantity.setBounds(46.5f, 6, 35, 10);
        
        final Label quantityText = new Label("quantity:1", yellowLabelStyle);
        quantityText.setFontScale(0.1f);
        quantityText.setPosition(49, 16);
        quantityText.setSize(30, 10);
        quantityText.setAlignment(Align.center);
        
        quantity.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                quantityText.setText("quantity:" + (int) quantity.getValue());
                productName.setText(item + " " + getInteger("item_" + getItemTextureNameByName(item)) + "-" + (int) quantity.getValue());
                for (int i = 0; i < labels.size; i++) {
                    labels.get(i).setText(items[i] + " " + getInteger("item_" + getItemTextureNameByName(items[i])) + "+" + (int) (itemCounts[i] * quantity.getValue()));
                }
                uraniumCells_text.setText(getInteger("money") + "+" + (int) (price[0] * quantity.getValue()));
                cogs_text.setText(getInteger("cogs") + "+" + (int) (price[1] * quantity.getValue()));
            }
        });
        
        sell.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (buy.isChecked()) {
                    addInteger("money", (int) (price[0] * quantity.getValue()));
                    addInteger("cogs", (int) (price[1] * quantity.getValue()));
                } else {
                    for (int i = 0; i < labels.size; i++) {
                        addInteger("item_" + getItemTextureNameByName(items[i]), (int) (itemCounts[i] * quantity.getValue()));
                    }
                }
                subtractInteger("item_" + getItemTextureNameByName(item), (int) quantity.getValue());
                itemSlotManager.update();
                dialog.hide();
            }
        });
        
        dialog.addActor(endItem);
        
        dialog.addActor(ingredients);
        dialog.addActor(requirements);
        
        dialog.addActor(product);
        dialog.addActor(productName);
        dialog.addActor(or);
        dialog.addActor(buy);
        dialog.addActor(scrap);
        
        dialog.addActor(sell);
        dialog.addActor(cancel);
        dialog.addActor(quantityText);
        dialog.addActor(quantity);
        dialog.setScale(4);
        dialog.setSize(128, 70);
        dialog.setPosition(15, 130);
        stage.addActor(dialog);
    }
}
