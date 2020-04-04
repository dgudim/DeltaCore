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
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.subtractInteger;

class CraftingDialogue {

    private Dialog dialog;
    private Stage stage;
    private AssetManager assetManager;
    private int requestedQuantity;
    private Node clickedOn;
    private Array<ImageButton> requirementButtons;
    private Array<String> requirementNames;
    private Slider quantityGlobal;
    private String result;
    private int resultCount;
    private UIComposer uiComposer;
    private Label.LabelStyle yellowLabelStyle, defaultLabelStyle;
    private final Slider quantity;
    private TextureAtlas itemAtlas;
    private JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("items/tree.json"));

    CraftingDialogue(final Stage stage, final AssetManager assetManager, final String result) {
        this(stage, assetManager, result, 1, false, null, null);
    }

    CraftingDialogue(final Stage stage, final AssetManager assetManager, final String result, boolean showDescription) {
        this(stage, assetManager, result, 1, showDescription, null, null);
    }

    CraftingDialogue(final Stage stage, final AssetManager assetManager, final String result, int requestedQuantity, boolean showDescription, final CraftingDialogue previousDialogue, final Node clickedOn){
        this.stage = stage;
        this.assetManager = assetManager;
        this.result = result;
        this.requestedQuantity = MathUtils.clamp(requestedQuantity, 1, 50);
        this.clickedOn = clickedOn;

        requirementButtons = new Array<>();
        requirementNames = new Array<>();

        BitmapFont font = assetManager.get("fonts/font2(old).fnt");
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.13f);
        font.getData().markupEnabled = true;

        uiComposer = new UIComposer(assetManager);

        uiComposer.loadStyles("workshopGreen", "workshopRed", "workshopCyan", "workshopPurple", "questionButton", "sliderDefaultSmall");

        Skin buttonSkin = new Skin();
        buttonSkin.addRegions((TextureAtlas) assetManager.get("shop/workshop.atlas"));

        itemAtlas = assetManager.get("items/items.atlas");

        TextButton yes = uiComposer.addTextButton("workshopGreen", "craft", 0.12f);
        TextButton no = uiComposer.addTextButton("workshopRed", "cancel", 0.12f);
        TextButton yes2 = uiComposer.addTextButton("workshopGreen", "ok", 0.12f);
        TextButton yes3 = uiComposer.addTextButton("workshopGreen", "got you", 0.12f);
        TextButton equip = uiComposer.addTextButton("workshopCyan", "equip", 0.12f);
        TextButton customize = uiComposer.addTextButton("workshopPurple", "customize", 0.10f);
        yes.setBounds(86, 3, 39, 22);
        no.setBounds(3, 3, 39, 22);
        yes2.setBounds(3, 3, 39, 22);
        yes3.setBounds(45, 3, 38, 22);
        equip.setBounds(45, 3, 38, 22);
        customize.setBounds(3, 3, 39, 22);

        yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;

        defaultLabelStyle = new Label.LabelStyle();
        defaultLabelStyle.font = font;

        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.background = buttonSkin.getDrawable("craftingTerminal");
        dialog = new Dialog("", dialogStyle);

        dialog.addActor(getProductImage(result));

        quantity = uiComposer.addSlider("sliderDefaultSmall", 1, 50, 1);

        Label text = new Label("This is a base part", yellowLabelStyle);
        text.setPosition(10, 61);
        text.setFontScale(0.08f);
        text.setAlignment(Align.center);

        if(!showDescription && !getPartLockState()) {
            switch (getType()) {
                case ("baseCategory"):

                    if(saveTo().equals("currentEngine")){
                        customize.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                new ColorCustomizationDialogue(assetManager, treeJson.get(result).get("usesEffect").asString(), stage);
                            }
                        });
                        yes.setText("ok");
                        addButtons(yes, customize);
                        addCloseListener(yes);
                    }else{
                        yes.setText("fine");
                        addButtons(yes, yes2);
                        addCloseListener(yes, yes2);
                    }

                    addEquipButton(equip);

                    addProductName();
                    addDescription(true);
                    dialog.addActor(text);
                    break;
                case ("part"):
                case ("item"):
                    if((!getBoolean("unlocked_" + getItemCodeNameByName(result)) && getType().equals("part")) || getType().equals("item")) {
                        final String[] items = getRequiredItems(result);
                        final int[] itemCounts = getRequiredItemCounts(result);
                        resultCount = getResultCount(result);
                        requirementNames.addAll(items);

                        addQuestion();
                        addCloseListener(no);

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
                                    if (getType().equals("part")) {
                                        putBoolean("unlocked_" + getItemCodeNameByName(result), true);
                                        putString(saveTo(), result);
                                    } else {
                                        addInteger("item_" + getItemCodeNameByName(result), (int) (resultCount * quantity.getValue()));
                                    }
                                    dialog.hide();
                                    if (previousDialogue != null) {
                                        previousDialogue.updateRequirements();
                                    }
                                    if(clickedOn != null) {
                                        clickedOn.updateRoot();
                                    }
                                }
                            }
                        });

                        dialog.addActor(no);
                        dialog.addActor(yes);

                        if (getType().equals("part")) {
                            addRequirementsTable(items, itemCounts);
                            addProductName();
                            equip.setTouchable(Touchable.disabled);
                            equip.setColor(Color.LIGHT_GRAY);
                            addButtons(equip);
                        } else {
                            addQuantitySelector(addRequirementsTable(items, itemCounts), itemCounts, items);
                        }
                    }else{
                        text.setText("Part already crafted");

                        if(saveTo().equals("currentEngine")) {
                            customize.addListener(new ClickListener() {
                                @Override
                                public void clicked(InputEvent event, float x, float y) {
                                    new ColorCustomizationDialogue(assetManager, treeJson.get(result).get("usesEffect").asString(), stage);
                                }
                            });
                            yes.setText("ok");
                            addButtons(yes, customize);
                            addCloseListener(yes);
                        }else{
                            yes.setText("fine");
                            addButtons(yes, yes2);
                            addCloseListener(yes, yes2);
                        }

                        addEquipButton(equip);
                        addProductName();
                        addDescription(true);

                        dialog.addActor(text);
                    }
                    break;
                case ("endItem"):
                    yes.setText("fine");

                    addCloseListener(yes, yes2, yes3);

                    text.setText("Item can't be crafted");

                    addProductQuantity(false);

                    addDescription(false);

                    dialog.addActor(text);
                    addButtons(yes, yes2, yes3);
                    break;
            }
        }else if(showDescription){
            yes.setText("fine");

            addCloseListener(yes, yes2, yes3);

            text.setText("Description:");

            if (getType().equals("part")) {
                addProductName();
                addDescription(true);
            }else{
                addProductQuantity(false);
                addDescription(false);
            }

            dialog.addActor(text);
            addButtons(yes, yes2, yes3);
        }else{
            text.setText("This part requires other parts:");
            yes.setText("fine");
            dialog.setBackground(buttonSkin.getDrawable("craftingTerminal_locked"));

            addCloseListener(yes, yes2);

            addRequirements();

            addProductName();

            addQuestion();

            dialog.addActor(text);
            addButtons(yes, yes2);
        }
        dialog.setScale(4);
        dialog.setSize(128, 70);
        dialog.setPosition(15, 130);
        stage.addActor(dialog);

    }

    private String[] getRequiredItems(String result){
        return treeJson.get(result).get("items").asStringArray();
    }

    private String getDescription(String result){
        return treeJson.get(result).get("description").asString();
    }

    private String getStats(){
        StringBuilder stats = new StringBuilder();
        String[] statsArray = treeJson.get(result).get("parameters").asStringArray();
        float[] statsValues = treeJson.get(result).get("parameterValues").asFloatArray();
        for (int i = 0; i<statsArray.length; i++){
            stats.append("\n").append(statsArray[i]).append(": ").append(statsValues[i]);
        }
        return stats.toString();
    }

    private int[] getRequiredItemCounts(String result){
        return treeJson.get(result).get("itemCounts").asIntArray();
    }

    private int getResultCount(String result){
        return treeJson.get(result).getInt("resultCount");
    }

    private void updateRequirements(){
        if(!getType().equals("part")) {
            quantityGlobal.setValue(quantityGlobal.getValue() + 1);
            quantityGlobal.setValue(quantityGlobal.getValue() - 1);
        }
    }

    private Image getProductImage(String result){
        Image product = new Image(itemAtlas.findRegion(getItemCodeNameByName(result)));
        product.setBounds(88, 40, 35, 25);
        product.setScaling(Scaling.fit);
        return product;
    }

    private String getType(){
        return treeJson.get(result).get("type").asString();
    }

    private String saveTo(){
        return treeJson.get(result).get("saveTo").asString();
    }

    private void addCloseListener(TextButton... buttons){
        for(int i = 0; i<buttons.length; i++)
        buttons[i].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
    }

    private void addButtons(TextButton... buttons){
        for(int i = 0; i<buttons.length; i++){
            dialog.addActor(buttons[i]);
        }
    }

    private Array<Label> addRequirementsTable(final String[] items, final int[] itemCounts){
        final Array<Label> labels = new Array<>();
        Table ingredientsTable = new Table();

        for (int i = 0; i < items.length; i++) {
            Table requirement = new Table();
            Label itemText = new Label(items[i] + " " + getInteger("item_" + getItemCodeNameByName(items[i])) + "/" + itemCounts[i]*requestedQuantity, yellowLabelStyle);
            itemText.setFontScale(0.1f);
            if (itemCounts[i]*requestedQuantity > getInteger("item_" + getItemCodeNameByName(items[i]))) {
                itemText.setColor(Color.valueOf("#DD0000"));
            }
            labels.add(itemText);
            ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
            itemButtonStyle.imageUp = new Image(itemAtlas.findRegion(getItemCodeNameByName(items[i]))).getDrawable();
            itemButtonStyle.imageDisabled = new Image(itemAtlas.findRegion("disabled" + getItemCodeNameByName(items[i]))).getDrawable();
            itemButtonStyle.imageDown = new Image(itemAtlas.findRegion("enabled_" + getItemCodeNameByName(items[i]))).getDrawable();
            itemButtonStyle.imageOver = new Image(itemAtlas.findRegion("over_" + getItemCodeNameByName(items[i]))).getDrawable();
            ImageButton item = new ImageButton(itemButtonStyle);
            final int finalI = i;

            item.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new CraftingDialogue(stage, assetManager, items[finalI],
                            MathUtils.clamp((int)(itemCounts[finalI] * quantity.getValue())-getInteger("item_" + getItemCodeNameByName(items[finalI])), 1, 50),
                            false, CraftingDialogue.this, clickedOn);
                }
            });

            requirementButtons.add(item);
            requirement.add(item).size(10, 10);
            requirement.add(itemText).pad(1).padLeft(2);
            ingredientsTable.add(requirement).padTop(1).padBottom(1).align(Align.left).row();
            ingredientsTable.align(Align.left).padLeft(1);
        }

        ScrollPane ingredients = new ScrollPane(ingredientsTable);

        ingredients.setBounds(3, 28, 80, 39);
        dialog.addActor(ingredients);
        return labels;
    }

    private void addQuantitySelector(final Array<Label> labels, final int[] itemCounts, final String[] items){

        final Label quantityText = new Label("quantity:"+requestedQuantity, yellowLabelStyle);
        quantityText.setFontScale(0.1f);
        quantityText.setAlignment(Align.center);

        quantity.setValue(requestedQuantity);
        quantityGlobal = quantity;

        final Label productQuantity = addProductQuantity(true);

        quantity.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                quantityText.setText("quantity:" + (int) quantity.getValue());
                for (int i = 0; i < itemCounts.length; i++) {
                    labels.get(i).setText(items[i] + " " + getInteger("item_" + getItemCodeNameByName(items[i])) + "/" + (int)(itemCounts[i] * quantity.getValue()));
                    if (itemCounts[i] * quantity.getValue() > getInteger("item_" + getItemCodeNameByName(items[i]))) {
                        labels.get(i).setColor(Color.valueOf("#DD0000"));
                    }else{
                        labels.get(i).setColor(Color.YELLOW);
                    }
                }
                productQuantity.setText(result + " " + getInteger("item_" + getItemCodeNameByName(result)) + "+" + (int) (resultCount * quantity.getValue()));
            }
        });

        quantity.setBounds(46.5f, 6, 35, 10);
        quantityText.setPosition(47, 17);

        dialog.addActor(quantityText);
        dialog.addActor(quantity);
    }

    private void addQuestion(){
        Button question = uiComposer.addButton("questionButton");
        question.setBounds(119, 61, 6, 6);

        question.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new CraftingDialogue(stage, assetManager, result, 1,true, null, clickedOn);
            }
        });
        dialog.addActor(question);
    }

    private Label addProductQuantity(boolean showCraftableQuantity){
        Label productQuantity = addProductName();
        if (showCraftableQuantity) {
            productQuantity.setText(result + " " + getInteger("item_" + getItemCodeNameByName(result)) + "+" + (int) (resultCount * quantity.getValue()));
        }else{
            productQuantity.setText(result + " " + getInteger("item_" + getItemCodeNameByName(result)));
        }
        return productQuantity;
    }

    private Label addProductName(){
        Label productName = new Label(result, yellowLabelStyle);
        productName.setFontScale(0.09f);
        productName.setBounds(86, 29, 39, 10);
        productName.setWrap(true);
        productName.setAlignment(Align.center);
        dialog.addActor(productName);
        return productName;
    }

    private void addDescription(boolean showStats){
        Label description = new Label("[#FEDE15]"+getDescription(result), defaultLabelStyle);
        if(showStats){
            description.setText(description.getText() + getStats());
        }
        description.setWidth(78);
        description.setWrap(true);
        description.setFontScale(0.1f);
        ScrollPane descriptionPane = new ScrollPane(description);
        descriptionPane.setBounds(4, 28, 78, 30);
        dialog.addActor(descriptionPane);
    }

    private boolean getPartLockState(){
        boolean locked = false;
        if(getType().equals("part")) {
            String[] requiredItems = treeJson.get(result).get("requires").asStringArray();
            for (int i = 0; i < requiredItems.length; i++) {
                locked = !getBoolean("unlocked_" + getItemCodeNameByName(requiredItems[i]));
                if (locked) {
                    break;
                }
            }
        }
        return locked;
    }

    private void addRequirements(){
        final String[] requiredItems = treeJson.get(result).get("requires").asStringArray();
        Table ingredientsTable = new Table();

        for (int i = 0; i < requiredItems.length; i++) {
            Table requirement = new Table();
            Label itemText = new Label(requiredItems[i], yellowLabelStyle);
            itemText.setFontScale(0.1f);
            if (!getBoolean("unlocked_"+getItemCodeNameByName(requiredItems[i]))) {
                itemText.setColor(Color.valueOf("#DD0000"));
            }
            ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
            itemButtonStyle.imageUp = new Image(itemAtlas.findRegion(getItemCodeNameByName(requiredItems[i]))).getDrawable();
            itemButtonStyle.imageDisabled = new Image(itemAtlas.findRegion("disabled" + getItemCodeNameByName(requiredItems[i]))).getDrawable();
            itemButtonStyle.imageDown = new Image(itemAtlas.findRegion("enabled_" + getItemCodeNameByName(requiredItems[i]))).getDrawable();
            itemButtonStyle.imageOver = new Image(itemAtlas.findRegion("over_" + getItemCodeNameByName(requiredItems[i]))).getDrawable();
            ImageButton item = new ImageButton(itemButtonStyle);
            final int finalI = i;

            item.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new CraftingDialogue(stage, assetManager, requiredItems[finalI], 1, false, CraftingDialogue.this, clickedOn);
                    dialog.hide();
                }
            });

            requirementButtons.add(item);
            requirement.add(item).size(20, 20);
            requirement.add(itemText).pad(1).padLeft(2).align(Align.center);
            ingredientsTable.add(requirement).padTop(1).padBottom(1).align(Align.left).row();
            ingredientsTable.align(Align.left).padLeft(1);
            ScrollPane ingredients = new ScrollPane(ingredientsTable);

            ingredients.setBounds(3, 28, 80, 39);
            dialog.addActor(ingredients);
        }
    }

    private void addEquipButton(final TextButton equip){
        if(getString(saveTo()).equals(result)){
            equip.setColor(Color.GREEN);
            equip.getLabel().setText("equipped");
            equip.getLabel().setFontScale(0.11f);
        }

        equip.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                putString(saveTo(), result);
                equip.setColor(Color.GREEN);
                equip.getLabel().setText("equipped");
                equip.getLabel().setFontScale(0.11f);
            }
        });

        addButtons(equip);
    }
}