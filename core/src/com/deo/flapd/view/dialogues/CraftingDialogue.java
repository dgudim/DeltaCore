package com.deo.flapd.view.dialogues;

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
import com.badlogic.gdx.utils.Scaling;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.ui.UIComposer;

import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.subtractInteger;

public class CraftingDialogue extends Dialogue {
    
    private final Dialog dialog;
    private final Stage stage;
    
    private final CompositeManager compositeManager;
    private final LocaleManager localeManager;
    
    private final int requestedQuantity;
    private final Array<TextButton> buyShortcuts;
    private Array<Label> tableLabels;
    private final JsonEntry items;
    ;
    private final String result;
    private int resultCount;
    private final UIComposer uiComposer;
    private final Label.LabelStyle yellowLabelStyle;
    private final Label.LabelStyle defaultLabelStyle;
    private final Slider quantity;
    private final TextureAtlas itemAtlas;
    private final JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
    
    public CraftingDialogue(CompositeManager compositeManager, final Stage stage, final String result) {
        this(compositeManager, stage, result, 1, false, null);
    }
    
    public CraftingDialogue(CompositeManager compositeManager, final Stage stage, final String result, boolean showDescription) {
        this(compositeManager, stage, result, 1, showDescription, null);
    }
    
    private CraftingDialogue(CompositeManager compositeManager, final Stage stage, final String result, int requestedQuantity, boolean showDescription, final Dialogue previousDialogue) {
        this.stage = stage;
        this.compositeManager = compositeManager;
        AssetManager assetManager = compositeManager.getAssetManager();
        localeManager = compositeManager.getLocaleManager();
        this.result = result;
        this.requestedQuantity = MathUtils.clamp(requestedQuantity, 1, 50);
        
        buyShortcuts = new Array<>();
        tableLabels = new Array<>();
        items = treeJson.get(result, "items");
        
        BitmapFont font = assetManager.get("fonts/pixel.ttf");
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.52f);
        font.getData().markupEnabled = true;
        
        uiComposer = compositeManager.getUiComposer();
        
        Skin buttonSkin = new Skin();
        buttonSkin.addRegions(assetManager.get("shop/workshop.atlas"));
        
        itemAtlas = assetManager.get("items/items.atlas");
        
        TextButton yes = uiComposer.addTextButton("workshopGreen", localeManager.get("craftingDialogue.craft"), 0.48f);
        TextButton no = uiComposer.addTextButton("workshopRed", localeManager.get("craftingDialogue.cancel"), 0.48f);
        TextButton yes2 = uiComposer.addTextButton("workshopGreen", localeManager.get("craftingDialogue.ok"), 0.48f);
        TextButton yes3 = uiComposer.addTextButton("workshopGreen", localeManager.get("craftingDialogue.gotYou"), 0.48f);
        TextButton equip = uiComposer.addTextButton("workshopCyan", localeManager.get("craftingDialogue.equip"), 0.48f);
        TextButton customize = uiComposer.addTextButton("workshopPurple", localeManager.get("craftingDialogue.customize"), 0.4f);
        yes.setBounds(344, 12, 156, 88);
        no.setBounds(12, 12, 156, 88);
        yes2.setBounds(12, 12, 156, 88);
        yes3.setBounds(180, 12, 152, 88);
        equip.setBounds(180, 12, 152, 88);
        customize.setBounds(12, 12, 156, 88);
        
        yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;
        
        defaultLabelStyle = new Label.LabelStyle();
        defaultLabelStyle.font = font;
        
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.background = buttonSkin.getDrawable("craftingTerminal");
        dialog = new Dialog("", dialogStyle);
        
        dialog.addActor(getProductImage());
        
        quantity = uiComposer.addSlider("sliderDefaultNormal", 1, 50, 1);
        
        Label text = new Label(localeManager.get("craftingDialogue.basePartMessage"), yellowLabelStyle);
        text.setPosition(40, 244);
        text.setFontScale(0.32f);
        text.setAlignment(Align.center);
        
        if (!showDescription && !isPartLocked()) {
            switch (getType()) {
                case ("basePart"):
                case ("category"):
                case ("subcategory"):
                    if (saveTo().equals(Keys.currentEngine)) {
                        customize.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                new ColorCustomizationDialogue(compositeManager, treeJson.getString("fire_engine_left_red", result, "usesEffect"), stage);
                            }
                        });
                        yes.setText(localeManager.get("craftingDialogue.ok"));
                        addButtons(yes, customize);
                        addCloseListener(yes);
                    } else {
                        yes.setText(localeManager.get("craftingDialogue.fine"));
                        addButtons(yes, yes2);
                        addCloseListener(yes, yes2);
                    }
                    
                    if (getType().equals("basePart")) {
                        addEquipButton(equip);
                    } else {
                        addButtons(yes3);
                        addCloseListener(yes3);
                        text.setText(localeManager.get("craftingDialogue.categoryMessage"));
                    }
                    
                    addProductName();
                    addDescription(getType().equals("basePart"));
                    dialog.addActor(text);
                    break;
                case ("part"):
                case ("item"):
                    if ((!getBoolean("unlocked_" + result) && getType().equals("part")) || getType().equals("item")) {
                        
                        resultCount = treeJson.getInt(1, result, "resultCount");
                        
                        addQuestion();
                        addCloseListener(no);
                        
                        yes.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                boolean craftingAllowed = true;
                                for (int i = 0; i < items.size; i++) {
                                    if (getInteger("item_" + items.get(i).name) < (int) (items.getInt(1, i) * quantity.getValue())) {
                                        craftingAllowed = false;
                                    }
                                }
                                if (craftingAllowed) {
                                    for (int i = 0; i < items.size; i++) {
                                        subtractInteger("item_" + items.get(i).name, (int) (items.getInt(1, i) * quantity.getValue()));
                                    }
                                    if (getType().equals("part")) {
                                        putBoolean("unlocked_" + result, true);
                                        putString(saveTo(), result);
                                    } else {
                                        addInteger("item_" + result, (int) (resultCount * quantity.getValue()));
                                    }
                                    dialog.hide();
                                    if (previousDialogue != null) {
                                        previousDialogue.update();
                                    }
                                    //LoadingScreen.craftingTree.update();
                                }
                            }
                        });
                        
                        dialog.addActor(no);
                        dialog.addActor(yes);
                        
                        if (getType().equals("part")) {
                            addProductName();
                            equip.setTouchable(Touchable.disabled);
                            equip.setColor(Color.LIGHT_GRAY);
                            addButtons(equip);
                            addRequirementsTable();
                        } else {
                            addQuantitySelector();
                        }
                    } else {
                        text.setText(localeManager.get("craftingDialogue.partCraftedMessage"));
                        
                        if (saveTo().equals("currentEngine")) {
                            customize.addListener(new ClickListener() {
                                @Override
                                public void clicked(InputEvent event, float x, float y) {
                                    new ColorCustomizationDialogue(compositeManager, treeJson.getString("fire_engine_left_red", result, "usesEffect"), stage);
                                }
                            });
                            yes.setText(localeManager.get("craftingDialogue.ok"));
                            addButtons(yes, customize);
                            addCloseListener(yes);
                        } else {
                            yes.setText(localeManager.get("craftingDialogue.fine"));
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
                    yes.setText(localeManager.get("craftingDialogue.fine"));
                    
                    addCloseListener(yes, yes2, yes3);
                    
                    text.setText(localeManager.get("craftingDialogue.cantBeCraftedMessage"));
                    
                    addProductQuantity(false);
                    
                    addDescription(false);
                    
                    dialog.addActor(text);
                    addButtons(yes, yes2, yes3);
                    break;
            }
        } else if (showDescription) {
            yes.setText(localeManager.get("craftingDialogue.fine"));
            
            addCloseListener(yes, yes2, yes3);
            
            text.setText(localeManager.get("craftingDialogue.description") + ":");
            
            if (getType().equals("part")) {
                addProductName();
                addDescription(true);
            } else {
                addProductQuantity(false);
                addDescription(false);
            }
            
            dialog.addActor(text);
            addButtons(yes, yes2, yes3);
        } else {
            text.setText(localeManager.get("craftingDialogue.requiresOtherPartsMessage") + ":");
            yes.setText(localeManager.get("craftingDialogue.fine"));
            dialog.setBackground(buttonSkin.getDrawable("craftingTerminal_locked"));
            
            addCloseListener(yes, yes2);
            
            addDependencies();
            
            addProductName();
            
            addQuestion();
            
            dialog.addActor(text);
            addButtons(yes, yes2);
        }
        dialog.setSize(512, 280);
        dialog.setPosition(15, 150);
        stage.addActor(dialog);
        
    }
    
    private String getStats() {
        StringBuilder stats = new StringBuilder();
        JsonEntry statsArray = treeJson.get(result, "parameters");
        for (int i = 0; i < statsArray.size; i++) {
            stats.append("\n").append(localeManager.get(statsArray.get(i).name)).append(": ").append(statsArray.getFloat(0, i));
        }
        return stats.toString();
    }
    
    public void update() {
        for (int i = 0; i < items.size; i++) {
            String currentItem = items.get(i).name;
            int itemQuantity = items.getInt(1, i);
            tableLabels.get(i).setText(localeManager.get(currentItem) + " " + getInteger("item_" + currentItem) + "/" + (int) (itemQuantity * quantity.getValue()));
            if (itemQuantity * quantity.getValue() > getInteger("item_" + currentItem)) {
                tableLabels.get(i).setColor(Color.valueOf("#DD0000"));
            } else {
                tableLabels.get(i).setColor(Color.YELLOW);
            }
        }
        for (int i = 0; i < items.size; i++) {
            if (!getString(Keys.savedShopSlots).contains(items.get(i).name)) {
                buyShortcuts.get(i).setTouchable(Touchable.disabled);
                buyShortcuts.get(i).setColor(Color.GRAY);
            }
        }
    }
    
    private Image getProductImage() {
        Image product = new Image(itemAtlas.findRegion(result));
        product.setBounds(352, 160, 140, 100);
        product.setScaling(Scaling.fit);
        return product;
    }
    
    private String getType() {
        return treeJson.getString("item", result, "type");
    }
    
    private String saveTo() {
        return treeJson.getString("noSaveToLocation", result, "saveTo");
    }
    
    private void addCloseListener(TextButton... buttons) {
        for (TextButton button : buttons)
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                }
            });
    }
    
    private void addButtons(TextButton... buttons) {
        for (TextButton button : buttons) {
            dialog.addActor(button);
        }
    }
    
    private void addRequirementsTable() {
        final Array<Label> labels = new Array<>();
        Table ingredientsTable = new Table();
        
        final JsonEntry slotsJson = new JsonEntry(new JsonReader().parse("{\"slots\":" + getString(Keys.savedShopSlots) + "," + "\"productQuantities\":" + getString(Keys.savedShopSlotsQuantities) + "}"));
        final Array<String> Jitems = new Array<>();
        Jitems.addAll(slotsJson.getStringArray(new String[]{}, "slots"));
        
        for (int i = 0; i < items.size; i++) {
            String currentItem = items.get(i).name;
            int itemQuantity = items.getInt(1, i);
            final Table requirement = new Table();
            Label itemText = new Label(localeManager.get(currentItem) + " " + getInteger("item_" + currentItem) + "/" + itemQuantity * requestedQuantity, yellowLabelStyle);
            itemText.setFontScale(0.4f);
            itemText.setWrap(true);
            if (itemQuantity * requestedQuantity > getInteger("item_" + currentItem)) {
                itemText.setColor(Color.valueOf("#DD0000"));
            }
            labels.add(itemText);
            
            Button.ButtonStyle itemButtonStyle = new Button.ButtonStyle();
            itemButtonStyle.up = new Image(itemAtlas.findRegion(currentItem)).getDrawable();
            itemButtonStyle.disabled = new Image(itemAtlas.findRegion(currentItem + "_disabled")).getDrawable();
            itemButtonStyle.down = new Image(itemAtlas.findRegion(currentItem + "_enabled")).getDrawable();
            itemButtonStyle.over = new Image(itemAtlas.findRegion(currentItem + "_over")).getDrawable();
            Button item = new Button(itemButtonStyle);
            
            TextButton buyShortcut = uiComposer.addTextButton("workshopPurple", localeManager.get("craftingDialogue.buy"), 0.21000001f);
            if (Jitems.indexOf(currentItem, false) == -1) {
                buyShortcut.setTouchable(Touchable.disabled);
                buyShortcut.setColor(Color.GRAY);
            }
            
            buyShortcut.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    
                    Array<Integer> quantities = new Array<>();
                    
                    for (int i = 0; i < slotsJson.getIntArray(new int[]{}, "productQuantities").length; i++) {
                        quantities.add(slotsJson.getIntArray(new int[]{}, "productQuantities")[i]);
                    }
                    
                    new PurchaseDialogue(compositeManager, stage, currentItem, quantities.get(Jitems.indexOf(currentItem, false)),
                            (int) Math.ceil((itemQuantity * quantity.getValue() - getInteger("item_" + currentItem)) / treeJson.get(currentItem).getFloat(1, "resultCount")),
                            CraftingDialogue.this);
                }
            });
            
            item.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new CraftingDialogue(compositeManager, stage, currentItem,
                            (int) Math.ceil((itemQuantity * quantity.getValue() - getInteger("item_" + currentItem)) / treeJson.get(currentItem).getFloat(1, "resultCount")),
                            false, CraftingDialogue.this);
                }
            });
            
            itemText.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new CraftingDialogue(compositeManager, stage, currentItem,
                            (int) Math.ceil((itemQuantity * quantity.getValue() - getInteger("item_" + currentItem)) / treeJson.get(currentItem).getFloat(1, "resultCount")),
                            false, CraftingDialogue.this);
                }
            });
            
            buyShortcuts.add(buyShortcut);
            requirement.add(item).size(40, 40);
            requirement.add(itemText).width(208).pad(4).padLeft(8);
            requirement.add(buyShortcut).size(40, 20).padLeft(8);
            ingredientsTable.add(requirement).padTop(4).padBottom(4).align(Align.left).row();
            ingredientsTable.align(Align.left).padLeft(4);
        }
        
        ScrollPane ingredients = new ScrollPane(ingredientsTable);
        ingredients.setupOverscroll(20, 10, 30);
        
        ingredients.setBounds(12, 112, 320, 156);
        dialog.addActor(ingredients);
        tableLabels = labels;
    }
    
    private void addQuantitySelector() {
        
        addRequirementsTable();
        
        final Label quantityText = new Label(localeManager.get("dialogue.quantity") + ":" + requestedQuantity, yellowLabelStyle);
        quantityText.setFontScale(0.4f);
        quantityText.setAlignment(Align.center);
        
        quantity.setValue(requestedQuantity);
        
        final Label productQuantity = addProductQuantity(true);
        
        quantity.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                quantityText.setText(localeManager.get("dialogue.quantity") + ":" + (int) quantity.getValue());
                update();
                productQuantity.setText(localeManager.get(result) + " " + getInteger("item_" + result) + "+" + (int) (resultCount * quantity.getValue()));
            }
        });
        
        quantity.setBounds(186, 24, 140, 40);
        quantityText.setPosition(188, 68);
        
        dialog.addActor(quantityText);
        dialog.addActor(quantity);
    }
    
    private void addQuestion() {
        Button question = uiComposer.addButton("questionButton");
        question.setBounds(476, 244, 24, 24);
        
        question.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new CraftingDialogue(compositeManager, stage, result, true);
            }
        });
        
        dialog.addActor(question);
    }
    
    private Label addProductQuantity(boolean showCraftableQuantity) {
        Label productQuantity = addProductName();
        if (showCraftableQuantity) {
            productQuantity.setText(localeManager.get(result) + " " + getInteger("item_" + result) + "+" + (int) (resultCount * quantity.getValue()));
        } else {
            productQuantity.setText(localeManager.get(result) + " " + getInteger("item_" + result));
        }
        return productQuantity;
    }
    
    private Label addProductName() {
        Label productName = new Label(localeManager.get(result), yellowLabelStyle);
        productName.setFontScale(0.32f);
        productName.setBounds(344, 116, 156, 40);
        productName.setWrap(true);
        productName.setAlignment(Align.center);
        dialog.addActor(productName);
        return productName;
    }
    
    private void addDescription(boolean showStats) {
        Label description = new Label("[#FEDE15]" + localeManager.get(treeJson.getString(localeManager.get("craftingDialogue.descriptionPlaceholder"), result, "description")), defaultLabelStyle);
        if (showStats) {
            description.setText(description.getText() + getStats());
        }
        description.setWidth(312);
        description.setWrap(true);
        description.setFontScale(0.4f);
        ScrollPane descriptionPane = new ScrollPane(description);
        descriptionPane.setupOverscroll(20, 10, 30);
        descriptionPane.setBounds(16, 112, 312, 120);
        dialog.addActor(descriptionPane);
    }
    
    private boolean isPartLocked() {
        boolean locked = false;
        if (getType().equals("part")) {
            String[] requiredItems = treeJson.getStringArray(new String[]{}, result, "requires");
            for (String requiredItem : requiredItems) {
                locked = !getBoolean("unlocked_" + requiredItem);
                if (locked) {
                    break;
                }
            }
        }
        return locked;
    }
    
    private void addDependencies() {
        final String[] requiredItems = treeJson.getStringArray(new String[]{}, result, "requires");
        Table ingredientsTable = new Table();
        
        for (int i = 0; i < requiredItems.length; i++) {
            Table requirement = new Table();
            Label itemText = new Label(requiredItems[i], yellowLabelStyle);
            itemText.setFontScale(0.4f);
            if (!getBoolean("unlocked_" + requiredItems[i])) {
                itemText.setColor(Color.valueOf("#DD0000"));
            }
            ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
            itemButtonStyle.imageUp = new Image(itemAtlas.findRegion(requiredItems[i])).getDrawable();
            itemButtonStyle.imageDisabled = new Image(itemAtlas.findRegion(requiredItems[i] + "_disabled")).getDrawable();
            itemButtonStyle.imageDown = new Image(itemAtlas.findRegion(requiredItems[i] + "_enabled")).getDrawable();
            itemButtonStyle.imageOver = new Image(itemAtlas.findRegion(requiredItems[i] + "_over")).getDrawable();
            ImageButton item = new ImageButton(itemButtonStyle);
            final int finalI = i;
            
            item.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new CraftingDialogue(compositeManager, stage, requiredItems[finalI], 1, false, CraftingDialogue.this);
                    dialog.hide();
                }
            });
            
            float scale = 80 / Math.max(item.getWidth(), item.getHeight());
            float width = item.getWidth() * scale;
            float height = item.getHeight() * scale;
            
            requirement.add(item).size(width, height);
            requirement.add(itemText).pad(4).padLeft(8).align(Align.center);
            ingredientsTable.add(requirement).padTop(4).padBottom(4).align(Align.left).row();
            ingredientsTable.align(Align.left).padLeft(4);
            ScrollPane ingredients = new ScrollPane(ingredientsTable);
            ingredients.setupOverscroll(20, 10, 30);
            
            ingredients.setBounds(12, 112, 320, 128);
            dialog.addActor(ingredients);
        }
    }
    
    private void addEquipButton(final TextButton equip) {
        if (getString(saveTo()).equals(result)) {
            equip.setColor(Color.GREEN);
            equip.getLabel().setText(localeManager.get("craftingDialogue.equipped"));
            equip.getLabel().setFontScale(0.44f);
        }
        
        equip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                putString(saveTo(), result);
                equip.setColor(Color.GREEN);
                equip.getLabel().setText(localeManager.get("craftingDialogue.equipped"));
                equip.getLabel().setFontScale(0.44f);
                //LoadingScreen.craftingTree.update();
            }
        });
        
        addButtons(equip);
    }
}