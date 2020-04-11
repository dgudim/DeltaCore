package com.deo.flapd.view;

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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;

import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.subtractInteger;

public class PurchaseDialogue{

    private JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("shop/tree.json"));

    PurchaseDialogue(final AssetManager assetManager, final Stage stage, final String result, int availableQuantity, final ItemSlotManager itemSlotManager){

        BitmapFont font = assetManager.get("fonts/font2(old).fnt");
        Skin skin = new Skin();
        skin.addRegions((TextureAtlas)assetManager.get("shop/workshop.atlas"));

        final TextureAtlas itemAtlas = assetManager.get("items/items.atlas");

        UIComposer uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("workshopRed", "workshopGreen", "sliderDefaultSmall", "questionButton");

        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.background = skin.getDrawable("craftingTerminal");
        final Dialog dialog = new Dialog("", dialogStyle);

        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;

        TextButton yes = uiComposer.addTextButton("workshopGreen", "buy", 0.12f);
        TextButton no = uiComposer.addTextButton("workshopRed", "cancel", 0.12f);
        yes.setBounds(86, 3, 39, 22);
        no.setBounds(3, 3, 39, 22);

        no.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        final Slider quantity = uiComposer.addSlider("sliderDefaultSmall", 1, availableQuantity, 1);
        quantity.setBounds(46.5f, 6, 35, 10);

        final Label quantityText = new Label("quantity:1", yellowLabelStyle);
        quantityText.setFontScale(0.1f);
        quantityText.setPosition(49, 16);
        quantityText.setSize(30, 10);
        quantityText.setAlignment(Align.center);

        Label topText = new Label("Price:", yellowLabelStyle);
        topText.setPosition(19, 56);
        topText.setFontScale(0.1f);
        topText.setAlignment(Align.center);

        Image product = new Image(itemAtlas.findRegion(getItemCodeNameByName(result)));
        product.setBounds(88, 40, 35, 25);
        product.setScaling(Scaling.fit);

        final Label productName = new Label(result + " " + getInteger("item_" + getItemCodeNameByName(result)) + "+1", yellowLabelStyle);
        productName.setFontScale(0.08f);
        productName.setBounds(86, 29, 39, 10);
        productName.setWrap(true);
        productName.setAlignment(Align.center);

        final int[] price = getPrice(result);
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
        holder2.add(new Image((Texture)assetManager.get("bonus_part.png"))).size(10, 10);
        holder2.add(cogs_text).padLeft(1);
        if(price[1]>0){
            requirements.add(holder2).align(Align.left).padLeft(1).padTop(1).row();
        }
        requirements.align(Align.left);
        requirements.setBounds(3, 28, 80, 39);

        quantity.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                quantityText.setText("quantity:"+(int)quantity.getValue());
                productName.setText(result + " " + getInteger("item_" + getItemCodeNameByName(result)) + "+" + (int)(quantity.getValue()));
                uraniumCells_text.setText(getInteger("money")+"/"+(int)(price[0]*quantity.getValue()));
                cogs_text.setText(getInteger("cogs")+"/"+(int)(price[1]*quantity.getValue()));
                if (getInteger("money") < price[0]*quantity.getValue()) {
                    uraniumCells_text.setColor(Color.valueOf("#DD0000"));
                }else{
                    uraniumCells_text.setColor(Color.YELLOW);
                }

                if (getInteger("cogs") < price[1]*quantity.getValue()) {
                    cogs_text.setColor(Color.valueOf("#DD0000"));
                }else{
                    cogs_text.setColor(Color.YELLOW);
                }
            }
        });

        yes.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getInteger("money") >= price[0]*quantity.getValue() && getInteger("cogs") >= price[1]*quantity.getValue()) {
                    subtractInteger("money", (int)(price[0]*quantity.getValue()));
                    subtractInteger("cogs", (int)(price[1]*quantity.getValue()));
                    addInteger("item_"+getItemCodeNameByName(result), (int)quantity.getValue());

                    JsonValue slotsJson = new JsonReader().parse(getString("savedSlots"));
                    Array<String> items = new Array<>();
                    items.addAll(slotsJson.get("slots").asStringArray());
                    Array<Integer> quantities = new Array<>();

                    for(int i = 0; i<slotsJson.get("productQuantities").asIntArray().length; i++){
                        quantities.add(slotsJson.get("productQuantities").asIntArray()[i]);
                    }

                    int index = items.indexOf(result, false);

                    quantities.set(index, (int)(quantities.get(index)-quantity.getValue()));

                    if(quantities.get(index)==0) {
                        items.removeIndex(index);
                        quantities.removeIndex(index);
                    }

                    putString("savedSlots", "{\"slots\":" + items.toString() + ","+ "\"productQuantities\":" + quantities.toString() + "}");

                    LoadingScreen.craftingTree.update();
                    itemSlotManager.update();
                    dialog.hide();
                }
            }
        });

        Button question = uiComposer.addButton("questionButton");
        question.setBounds(119, 61, 6, 6);

        question.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new CraftingDialogue(stage, assetManager, result,true);
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
                priceArray[0] += Math.ceil(buffer[0]/treeJson.get(result).get("resultCount").asFloat());
                priceArray[1] += buffer[1] + 1;
            }
        }else{
            return new int[]{price.asInt(), 0};
        }
        priceArray[1] = MathUtils.clamp((int)(Math.ceil(priceArray[1]/2f)-1)*4, 0, 15);
        return priceArray;
    }

}
