package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;

import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;

public class SellUncraftDialogue {

    private JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("shop/tree.json"));

    public SellUncraftDialogue(final AssetManager assetManager, final Stage stage, final ItemSlotManager itemSlotManager, int availableQuantity, final String item){

        BitmapFont font = assetManager.get("fonts/font2(old).fnt");
        Skin skin = new Skin();
        skin.addRegions((TextureAtlas)assetManager.get("shop/workshop.atlas"));

        UIComposer uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("workshopRed", "workshopGreen", "workshopPurple", "sliderDefaultSmall");

        final TextureAtlas itemAtlas = assetManager.get("items/items.atlas");

        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.background = skin.getDrawable("blankDialogue");
        final Dialog dialog = new Dialog("", dialogStyle);

        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;

        TextButton sell = uiComposer.addTextButton("workshopGreen", "sell", 0.12f);
        TextButton cancel = uiComposer.addTextButton("workshopRed", "cancel", 0.12f);
        sell.setBounds(86, 3, 39, 22);
        cancel.setBounds(3, 3, 39, 22);

        cancel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        final Label quantityText = new Label("quantity:1", yellowLabelStyle);
        quantityText.setFontScale(0.1f);
        quantityText.setPosition(49, 16);
        quantityText.setSize(30, 10);
        quantityText.setAlignment(Align.center);

        Label topText = new Label("Sell/scrap your items:", yellowLabelStyle);
        topText.setPosition(19, 56);
        topText.setFontScale(0.1f);
        topText.setAlignment(Align.center);

        final Array<Label> labels = new Array<>();
        Table ingredientsTable = new Table();

        final String[] items = treeJson.get(item).get("items").asStringArray();
        final int[] itemCounts = treeJson.get(item).get("itemCounts").asIntArray();
        for(int i = 0; i<itemCounts.length; i++){
            itemCounts[i] = MathUtils.clamp(itemCounts[i]/2, 1, itemCounts[i]);
        }

        boolean isEndItem = false;

        if (items[0].equals("")){
            isEndItem = true;
        }

        if(!isEndItem) {
            for (int i = 0; i < items.length; i++) {
                Table requirement = new Table();
                Label itemText = new Label(getInteger("item_" + getItemCodeNameByName(item)) + "+" + itemCounts[i], yellowLabelStyle);
                itemText.setFontScale(0.1f);
                labels.add(itemText);
                ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
                itemButtonStyle.imageUp = new Image(itemAtlas.findRegion(getItemCodeNameByName(items[i]))).getDrawable();
                itemButtonStyle.imageDisabled = new Image(itemAtlas.findRegion("disabled_" + getItemCodeNameByName(items[i]))).getDrawable();
                itemButtonStyle.imageDown = new Image(itemAtlas.findRegion("enabled_" + getItemCodeNameByName(items[i]))).getDrawable();
                itemButtonStyle.imageOver = new Image(itemAtlas.findRegion("over_" + getItemCodeNameByName(items[i]))).getDrawable();
                ImageButton itemImageButton = new ImageButton(itemButtonStyle);
                final int finalI = i;

                requirement.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        new SellUncraftDialogue(assetManager, stage, itemSlotManager, getInteger("item_" + getItemCodeNameByName(item)), items[finalI]);
                    }
                });

                float scale = 10 / Math.max(itemImageButton.getWidth(), itemImageButton.getHeight());
                float width = itemImageButton.getWidth() * scale;
                float height = itemImageButton.getHeight() * scale;

                requirement.add(itemImageButton).size(width, height);
                requirement.add(itemText).pad(1).padLeft(2);
                ingredientsTable.add(requirement).padTop(1).padBottom(1).align(Align.left).row();
                ingredientsTable.align(Align.left).padLeft(1);
            }
        }

        ScrollPane ingredients = new ScrollPane(ingredientsTable);

        final int[] price = getPrice(item);
        final Table requirements = new Table();
        Table holder = new Table();
        final Label uraniumCells_text = new Label(getInteger("money")+"/"+price[0], yellowLabelStyle);
        final Label cogs_text = new Label(getInteger("cogs")+"/"+price[1], yellowLabelStyle);
        uraniumCells_text.setFontScale(0.13f);
        cogs_text.setFontScale(0.13f);

        if (getInteger("money") < price[0]) {
            uraniumCells_text.setColor(Color.valueOf("#DD0000"));
        }

        if (getInteger("cogs") < price[1]) {
            cogs_text.setColor(Color.valueOf("#DD0000"));
        }

        final Image uraniumCell = new Image((Texture)assetManager.get("uraniumCell.png"));
        uraniumCell.setScaling(Scaling.fit);
        holder.add(uraniumCell).size(10, 10);
        holder.add(uraniumCells_text).padLeft(1);
        requirements.add(holder).align(Align.left).padLeft(1).row();

        Table holder2 = new Table();
        holder2.add(new Image(assetManager.get("bonuses.atlas", TextureAtlas.class).findRegion("bonus_part"))).size(10, 10);
        holder2.add(cogs_text).padLeft(1);
        if(price[1]>0){
            requirements.add(holder2).align(Align.left).padLeft(1).padTop(1).row();
        }
        requirements.align(Align.left);
        requirements.setBounds(43, 28, 40, 39);

        ingredients.setBounds(3, 28, 40, 39);
        dialog.addActor(ingredients);
        dialog.add(requirements);

        dialog.addActor(sell);
        dialog.addActor(cancel);
        dialog.setScale(4);
        dialog.setSize(128, 70);
        dialog.setPosition(15, 130);
        stage.addActor(dialog);
    }

    private int[] getPrice(String result){
        JsonValue price = treeJson.get(result).get("price");
        int[] priceArray = new int[]{0, 0};
        if(price.asString().equals("auto")){
            String[] items = treeJson.get(result).get("items").asStringArray();
            for(int i = 0; i<items.length; i++){
                int[] buffer = getPrice(items[i]);
                priceArray[0] += Math.ceil(buffer[0]/treeJson.get(result).get("resultCount").asFloat()/2f);
                priceArray[1] += buffer[1] + 1;
            }
        }else{
            return new int[]{price.asInt(), 0};
        }
        priceArray[1] = (int)MathUtils.clamp((Math.ceil(priceArray[1]/2f)-1)*1.5f, 0, 100)/2;
        return priceArray;
    }

}
