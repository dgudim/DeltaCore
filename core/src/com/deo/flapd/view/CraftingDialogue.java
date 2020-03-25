package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;

import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.subtractInteger;

class CraftingDialogue {

    private Dialog dialog;
    private Array<ImageButton> requirementButtons;
    private Array<String> requirementNames;
    private Slider quantityGlobal;
    private JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("items/tree.json"));

    CraftingDialogue(final Stage stage, final AssetManager assetManager, final String result, int requestedQuantity, boolean locked, boolean showDescription, final CraftingDialogue previousDialogue, final Node clickedOn){

        requirementButtons = new Array<>();
        requirementNames = new Array<>();

        Skin buttonSkin = new Skin();
        buttonSkin.addRegions((TextureAtlas) assetManager.get("shop/workshop.atlas"));

        BitmapFont font = assetManager.get("fonts/font2.fnt");
        BitmapFont font_text = assetManager.get("fonts/font2(old).fnt");
        font.getData().setScale(0.13f);
        font.setUseIntegerPositions(false);
        font_text.getData().setScale(0.13f);
        font_text.setUseIntegerPositions(false);

        TextButton.TextButtonStyle yesStyle = new TextButton.TextButtonStyle();
        yesStyle.up = buttonSkin.getDrawable("blank_shopButton_disabled");
        yesStyle.over = buttonSkin.getDrawable("blank_shopButton_over");
        yesStyle.down = buttonSkin.getDrawable("blank_shopButton_enabled");
        yesStyle.font = font;
        yesStyle.fontColor = Color.valueOf("#3D4931");
        yesStyle.overFontColor = Color.valueOf("#3D51232");
        yesStyle.downFontColor = Color.valueOf("#22370E");

        TextButton.TextButtonStyle noStyle = new TextButton.TextButtonStyle();
        noStyle.up = buttonSkin.getDrawable("blank_shopButton_red_disabled");
        noStyle.over = buttonSkin.getDrawable("blank_shopButton_red_over");
        noStyle.down = buttonSkin.getDrawable("blank_shopButton_red_enabled");
        noStyle.font = font;
        noStyle.fontColor = Color.valueOf("#493D31");
        noStyle.overFontColor = Color.valueOf("#513D232");
        noStyle.downFontColor = Color.valueOf("#37220E");

        TextButton.TextButtonStyle equipStyle = new TextButton.TextButtonStyle();
        equipStyle.up = buttonSkin.getDrawable("blank_shopButton_cyan_disabled");
        equipStyle.over = buttonSkin.getDrawable("blank_shopButton_cyan_over");
        equipStyle.down = buttonSkin.getDrawable("blank_shopButton_cyan_enabled");
        equipStyle.font = font;
        equipStyle.fontColor = Color.valueOf("#31493D");
        equipStyle.overFontColor = Color.valueOf("#32513D");
        equipStyle.downFontColor = Color.valueOf("#0E3732");

        TextureAtlas items1 = assetManager.get("items/items.atlas");

        if(!locked && isCraftable(result) && !showDescription) {
            final String[] items = getRequiredItems(result);
            final int[] itemCounts = getRequiredItemCounts(result);
            final int resultCount = getResultCount(result);
            requirementNames.addAll(items);

            buttonSkin.add("knob", assetManager.get("progressBarKnob.png"));
            buttonSkin.add("knob2", assetManager.get("progressBarKnob.png"));
            buttonSkin.add("bg", assetManager.get("progressBarBg.png"));
            buttonSkin.add("bg2", assetManager.get("progressBarBg.png"));
            buttonSkin.add("knob_over", assetManager.get("progressBarKnob_over.png"));
            buttonSkin.add("knob_enabled", assetManager.get("progressBarKnob_enabled.png"));

            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = font_text;
            labelStyle.fontColor = Color.YELLOW;

            Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
            sliderStyle.background = buttonSkin.getDrawable("bg");
            sliderStyle.knob = buttonSkin.getDrawable("knob");
            sliderStyle.knobOver = buttonSkin.getDrawable("knob_over");
            sliderStyle.knobDown = buttonSkin.getDrawable("knob_enabled");
            sliderStyle.knob.setMinHeight(10);
            sliderStyle.knob.setMinWidth(6);
            sliderStyle.knobOver.setMinHeight(10);
            sliderStyle.knobOver.setMinWidth(6);
            sliderStyle.knobDown.setMinHeight(10);
            sliderStyle.knobDown.setMinWidth(6);
            sliderStyle.background.setMinHeight(10);
            sliderStyle.background.setMinWidth(35);

            Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
            buttonStyle.up = buttonSkin.getDrawable("question");
            buttonStyle.over = buttonSkin.getDrawable("question_over");
            buttonStyle.down = buttonSkin.getDrawable("question_enabled");

            Button question = new Button(buttonStyle);
            question.setBounds(119, 61, 6, 6);

            question.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new CraftingDialogue(stage, assetManager, result, 1,true, true, null, clickedOn);
                }
            });

            final Slider quantity = new Slider(1, 50, 1, false, sliderStyle);
            quantity.setValue(requestedQuantity);
            quantityGlobal = quantity;

            TextButton yes = new TextButton("craft", yesStyle);
            TextButton no = new TextButton("cancel", noStyle);

            no.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });

            yes.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    boolean craftingAllowed = true;
                    for (int i = 0; i < itemCounts.length; i++) {
                        if (getInteger("item_" + getItemCodeNameByName(items[i])) < (int) (itemCounts[i] * quantity.getValue())) {
                            craftingAllowed = false;
                        }
                    }
                    if (craftingAllowed) {
                        for (int i = 0; i < itemCounts.length; i++) {
                            subtractInteger("item_" + getItemCodeNameByName(items[i]), (int) (itemCounts[i] * quantity.getValue()));
                        }
                        addInteger("item_" + getItemCodeNameByName(result), (int) (resultCount * quantity.getValue()));
                        dialog.hide();
                        if(previousDialogue != null) {
                            previousDialogue.updateRequirements();
                        }
                    }
                }
            });

            Table ingredientsTable = new Table();

            Window.WindowStyle dialogStyle = new Window.WindowStyle();
            dialogStyle.titleFont = font;
            dialogStyle.background = buttonSkin.getDrawable("craftingTerminal");

            final Label quantityText = new Label("quantity:"+requestedQuantity, labelStyle);
            quantityText.setFontScale(0.1f);
            quantityText.setAlignment(Align.center);

            final Label productQuantity = new Label(result + " " + getInteger("item_" + getItemCodeNameByName(result)) + "+" + resultCount * requestedQuantity, labelStyle);
            productQuantity.setFontScale(0.09f);

            Image product = getProductImage(result, items1);

            dialog = new Dialog("", dialogStyle);
            dialog.addActor(no);
            dialog.addActor(yes);
            final Array<Label> labels = new Array<>();
            for (int i = 0; i < items.length; i++) {
                Table requirement = new Table();
                Label itemText = new Label(items[i] + " " + getInteger("item_" + getItemCodeNameByName(items[i])) + "/" + itemCounts[i]*requestedQuantity, labelStyle);
                itemText.setFontScale(0.1f);
                if (itemCounts[i]*requestedQuantity > getInteger("item_" + getItemCodeNameByName(items[i]))) {
                    itemText.setColor(Color.valueOf("#DD0000"));
                }
                labels.add(itemText);
                ImageButton.ImageButtonStyle buttonStyle2 = new ImageButton.ImageButtonStyle();
                Image upFit = new Image(items1.findRegion(getItemCodeNameByName(items[i])));
                Image disabledFit = new Image(items1.findRegion("disabled" + getItemCodeNameByName(items[i])));
                Image downFit = new Image(items1.findRegion("enabled_" + getItemCodeNameByName(items[i])));
                Image overFit = new Image(items1.findRegion("over_" + getItemCodeNameByName(items[i])));
                upFit.setScaling(Scaling.fit);
                disabledFit.setScaling(Scaling.fit);
                downFit.setScaling(Scaling.fit);
                overFit.setScaling(Scaling.fit);
                buttonStyle2.imageUp = upFit.getDrawable();
                buttonStyle2.imageDisabled = disabledFit.getDrawable();
                buttonStyle2.imageDown = downFit.getDrawable();
                buttonStyle2.imageOver = overFit.getDrawable();
                ImageButton item = new ImageButton(buttonStyle2);
                final int finalI = i;
                item.addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        new CraftingDialogue(stage, assetManager, items[finalI],
                        MathUtils.clamp((int)(itemCounts[finalI] * quantity.getValue())-getInteger("item_" + getItemCodeNameByName(items[finalI])), 1, 50),
                        !getBoolean("enabled_" + getItemCodeNameByName(items[finalI])), false, CraftingDialogue.this, clickedOn);
                    }
                });
                item.setDisabled(!getBoolean("enabled_" + getItemCodeNameByName(items[i])));
                item.setBounds(0, -1, 10, 10);
                requirementButtons.add(item);
                requirement.addActor(item);
                requirement.add(itemText).pad(1).padLeft(12);
                ingredientsTable.add(requirement).padBottom(2).padTop(2).align(Align.left).row();
                ingredientsTable.align(Align.left).padLeft(1);
            }
            ScrollPane ingredients = new ScrollPane(ingredientsTable);
            dialog.addActor(ingredients);
            dialog.addActor(quantityText);
            quantityText.setPosition(47, 17);
            dialog.addActor(quantity);
            dialog.addActor(productQuantity);
            productQuantity.setPosition(86, 29);
            productQuantity.setSize(39, 10);
            productQuantity.setWrap(true);
            productQuantity.setAlignment(Align.center);
            quantity.setPosition(46.5f, 6);
            quantity.setSize(35, 10);
            quantity.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    quantityText.setText("quantity:" + (int) quantity.getValue());
                    for (int i = 0; i < itemCounts.length; i++) {
                        labels.get(i).setText(items[i] + " " + getInteger("item_" + getItemCodeNameByName(items[i])) + "/" + (int)(itemCounts[i] * quantity.getValue()));
                        if (itemCounts[i] * quantity.getValue() > getInteger("item_" + getItemCodeNameByName(items[i]))) {
                            labels.get(i).setColor(Color.valueOf("#DD0000"));
                        }
                    }
                    productQuantity.setText(result + " " + getInteger("item_" + getItemCodeNameByName(result)) + "+" + (int) (resultCount * quantity.getValue()));
                }
            });
            dialog.addActor(product);
            dialog.addActor(question);
            ingredients.setBounds(3, 28, 80, 39);
            no.setPosition(3, 3);
            yes.setPosition(86, 3);
            dialog.setScale(4);
            dialog.setSize(128, 70);

            dialog.setPosition(15, 130);

            stage.addActor(dialog);
        }else if(isCraftable(result) && !showDescription){

            TextButton yes = new TextButton("unlock", yesStyle);
            TextButton no = new TextButton("cancel", noStyle);

            no.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });

            yes.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(getInteger("item_craftingCard")>0){
                        subtractInteger("item_craftingCard", 1);
                        putBoolean("enabled_"+getItemCodeNameByName(result), true);
                        if(previousDialogue != null) {
                            previousDialogue.unlockItem(result);
                        }
                        dialog.hide();
                    }
                }
            });

            no.setPosition(3, 3);
            yes.setPosition(86, 3);

            Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
            buttonStyle.up = new Image(items1.findRegion("craftingCard")).getDrawable();
            buttonStyle.disabled = new Image(items1.findRegion("disabledcraftingCard")).getDrawable();
            buttonStyle.down = new Image(items1.findRegion("enabled_craftingCard")).getDrawable();
            buttonStyle.over = new Image(items1.findRegion("over_craftingCard")).getDrawable();
            Button craftingCard = new Button(buttonStyle);

            craftingCard.setBounds(54, 38, 20, 20);

            craftingCard.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new CraftingDialogue(stage, assetManager, "crafting card", 1,false, false, null, clickedOn);
                }
            });

            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = font;
            if(getInteger("item_craftingCard")>0) {
                labelStyle.fontColor = Color.YELLOW;
            }else{
                labelStyle.fontColor = Color.valueOf("#DF4400");
            }

            Label.LabelStyle labelStyle2 = new Label.LabelStyle();
            labelStyle2.font = font;
            labelStyle2.fontColor = Color.YELLOW;

            Label itemText = new Label("crafting card "+getInteger("item_craftingCard")+"/1", labelStyle);
            Label text = new Label("Recipe locked\n", labelStyle2);
            text.setPosition(64, 57, 1);
            itemText.setPosition(64, 33, 1);

            Window.WindowStyle dialogStyle = new Window.WindowStyle();
            dialogStyle.titleFont = font;
            dialogStyle.background = buttonSkin.getDrawable("craftingTerminal_locked");

            dialog = new Dialog("", dialogStyle);
            dialog.addActor(text);
            dialog.addActor(craftingCard);
            dialog.addActor(itemText);
            dialog.addActor(no);
            dialog.addActor(yes);
            dialog.setScale(4);
            dialog.setSize(128, 70);

            dialog.setPosition(15, 130);

            stage.addActor(dialog);

        }else if(!showDescription){

            TextButton yes = new TextButton("fine", yesStyle);
            TextButton yes2 = new TextButton("ok", yesStyle);
            TextButton yes3 = new TextButton("got you", yesStyle);
            yes.getLabel().setFontScale(0.12f);
            yes2.getLabel().setFontScale(0.12f);
            yes3.getLabel().setFontScale(0.12f);

            yes.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });

            yes2.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });

            yes3.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });

            yes.setPosition(3, 3);
            yes2.setPosition(45, 3);
            yes2.setWidth(38);
            yes3.setPosition(86, 3);

            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = font;
            labelStyle.fontColor = Color.YELLOW;

            Label text = new Label("Item can't be crafted", labelStyle);
            text.setPosition(64, 63, 1);
            text.setFontScale(0.08f);

            Image product = getProductImage(result, items1);

            Label productQuantity = new Label(result + " " + getInteger("item_" + getItemCodeNameByName(result)), labelStyle);
            productQuantity.setFontScale(0.06f);
            productQuantity.setPosition(86, 29);
            productQuantity.setSize(39, 10);
            productQuantity.setWrap(true);
            productQuantity.setAlignment(Align.center);

            Label description = new Label(getDescription(result), labelStyle);
            description.setWidth(78);
            description.setWrap(true);
            description.setFontScale(0.1f);
            description.setColor(Color.GOLDENROD);
            ScrollPane descriptionPane = new ScrollPane(description);
            descriptionPane.setBounds(4, 28, 78, 30);

            Window.WindowStyle dialogStyle = new Window.WindowStyle();
            dialogStyle.titleFont = font;
            dialogStyle.background = buttonSkin.getDrawable("craftingTerminal");

            dialog = new Dialog("", dialogStyle);
            dialog.addActor(text);
            dialog.addActor(product);
            dialog.addActor(productQuantity);
            dialog.addActor(descriptionPane);
            dialog.addActor(yes);
            dialog.addActor(yes2);
            dialog.addActor(yes3);
            dialog.setScale(4);
            dialog.setSize(128, 70);

            dialog.setPosition(15, 130);

            stage.addActor(dialog);
        }else{

            TextButton yes = new TextButton("fine", yesStyle);
            TextButton yes2 = new TextButton("ok", yesStyle);
            TextButton yes3 = new TextButton("got you", yesStyle);
            yes.getLabel().setFontScale(0.12f);
            yes2.getLabel().setFontScale(0.12f);
            yes3.getLabel().setFontScale(0.12f);

            yes.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });

            yes2.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });

            yes3.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });

            yes.setPosition(3, 3);
            yes2.setPosition(45, 3);
            yes2.setWidth(38);
            yes3.setPosition(86, 3);

            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = font;
            labelStyle.fontColor = Color.YELLOW;

            Image product = getProductImage(result, items1);

            Label productQuantity = new Label(result + " " + getInteger("item_" + getItemCodeNameByName(result)), labelStyle);
            productQuantity.setFontScale(0.06f);
            productQuantity.setPosition(86, 29);
            productQuantity.setSize(39, 10);
            productQuantity.setWrap(true);
            productQuantity.setAlignment(Align.center);

            Label description = new Label(getDescription(result), labelStyle);
            description.setWidth(78);
            description.setWrap(true);
            description.setFontScale(0.1f);
            description.setColor(Color.GOLDENROD);
            ScrollPane descriptionPane = new ScrollPane(description);
            descriptionPane.setBounds(4, 28, 78, 39);

            Window.WindowStyle dialogStyle = new Window.WindowStyle();
            dialogStyle.titleFont = font;
            dialogStyle.background = buttonSkin.getDrawable("craftingTerminal");

            dialog = new Dialog("", dialogStyle);
            dialog.addActor(product);
            dialog.addActor(productQuantity);
            dialog.addActor(descriptionPane);
            dialog.addActor(yes);
            dialog.addActor(yes2);
            dialog.addActor(yes3);
            dialog.setScale(4);
            dialog.setSize(128, 70);

            dialog.setPosition(15, 130);

            stage.addActor(dialog);
        }
    }

    private String[] getRequiredItems(String result){
        return treeJson.get(result).get("items").asStringArray();
    }

    private String getDescription(String result){
        return treeJson.get(result).get("description").asString();
    }

    private boolean isCraftable(String result){
        return treeJson.get(result).get("isCraftable").asBoolean();
    }

    private int[] getRequiredItemCounts(String result){
        return treeJson.get(result).get("itemCounts").asIntArray();
    }

    private int getResultCount(String result){
        return treeJson.get(result).getInt("resultCount");
    }

    private void unlockItem(String name){
        if(requirementNames.indexOf(name, false)>-1) {
            requirementButtons.get(requirementNames.indexOf(name, false)).setDisabled(false);
        }
    }

    private void updateRequirements(){
        quantityGlobal.setValue(quantityGlobal.getValue()+1);
        quantityGlobal.setValue(quantityGlobal.getValue()-1);
    }

    private Image getProductImage(String result, TextureAtlas items){
        Image product = new Image(items.findRegion(getItemCodeNameByName(result)));
        product.setBounds(93, 41, 25, 25);
        product.setScaling(Scaling.fit);
        return product;
    }

    private String getType(String result){
        return treeJson.get(result).get("type").asString();
    }

}