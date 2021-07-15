package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.view.dialogues.ConfirmationDialogue;
import com.deo.flapd.view.dialogues.PurchaseDialogue;
import com.deo.flapd.view.dialogues.SellScrapDialogue;

import java.util.concurrent.TimeUnit;

import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getLong;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putLong;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.subtractInteger;
import static com.deo.flapd.utils.LogLevel.CRITICAL_ERROR;
import static com.deo.flapd.utils.LogLevel.ERROR;
import static com.deo.flapd.view.SlotManagerMode.INVENTORY;
import static com.deo.flapd.view.SlotManagerMode.SHOP;

enum SlotManagerMode {INVENTORY, SHOP}

public class ItemSlotManager {
    
    private final BitmapFont font;
    private final Table table;
    private final Table inventory;
    Group holderGroup;
    private final Skin slotSkin;
    private final TextureAtlas items;
    private final ScrollPane scrollPane;
    private Stage stage;
    private final AssetManager assetManager;
    private SlotManagerMode slotManagerMode;
    private final UIComposer uiComposer;
    
    private final JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
    
    ItemSlotManager(AssetManager assetManager) {
        
        slotSkin = new Skin();
        slotSkin.addRegions((TextureAtlas) assetManager.get("shop/workshop.atlas"));
        
        uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("workshopGreen");
        
        this.assetManager = assetManager;
        
        items = assetManager.get("items/items.atlas");
        
        font = assetManager.get("fonts/font2(old).fnt");
        font.getData().setScale(0.2f);
        
        table = new Table();
        holderGroup = new Group();
        inventory = new Table();
        
        inventory.setBackground(new TextureRegionDrawable(new Texture("buttonPauseBlank_disabled.png")));
        
        scrollPane = new ScrollPane(table);
        
        holderGroup.addActor(scrollPane);
        holderGroup.addActor(inventory);
    }
    
    void addShopSlots() {
        slotManagerMode = SHOP;
        int slotCount;
        long lastGenerationTime = getLong("lastGenTime");
        if (TimeUtils.timeSinceMillis(lastGenerationTime) > 18000000) {
            putLong("lastGenTime", TimeUtils.millis());
            slotCount = generateSlots();
        } else {
            slotCount = loadSlots();
        }
        if (slotCount == 0) {
            final TextButton update = uiComposer.addTextButton("workshopGreen", "reset for 3500?", 0.48f);
            update.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new ConfirmationDialogue(assetManager, stage, "reset the shop for 3500?", new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (getInteger("money") >= 3500) {
                                subtractInteger("money", 3500);
                                putLong("lastGenTime", 0);
                                ItemSlotManager.this.update();
                            }
                        }
                    });
                }
            });
            table.add(update).align(Align.center).size(300, 40);
        }
        addInfo();
    }
    
    void addInventorySlots() {
        slotManagerMode = INVENTORY;
        boolean nextRow = false;
        for (int i = 0; i < treeJson.size; i++) {
            if (treeJson.getString("noCategory", i, "category").equals("recepies") && getInteger("item_" + getItemCodeNameByName(treeJson.get(i).name)) > 0) {
                addInventorySlot(treeJson.get(i).name, getInteger("item_" + getItemCodeNameByName(treeJson.get(i).name)), nextRow);
                nextRow = !nextRow;
            }
        }
        addInfo();
    }
    
    private int generateSlots() {
        Array<String> itemsToAdd = new Array<>();
        Array<Integer> quantities = new Array<>();
        
        for (int i = 0; i < treeJson.size; i++) {
            if (treeJson.getString("noCategory", i, "category").equals("recepies")) {
                itemsToAdd.add(treeJson.get(i).name);
            }
        }
        
        boolean nextRow = false;
        Array<String> addedItems = new Array<>();
        int slotQuantity = getRandomInRange(itemsToAdd.size / 4, itemsToAdd.size / 2);
        for (int i = 0; i < slotQuantity; i++) {
            int index = getRandomInRange(0, itemsToAdd.size - 1);
            // TODO: 7/7/2021 quantity always 1
            int quantity = MathUtils.clamp(getRandomInRange(5, 15) - getComplexity(itemsToAdd.get(index)), 1, 15);
            addedItems.add(itemsToAdd.get(index));
            quantities.add(quantity);
            addShopSlot(itemsToAdd.get(index), quantity, nextRow);
            itemsToAdd.removeIndex(index);
            nextRow = !nextRow;
        }
        
        putString("savedSlots", addedItems.toString());
        putString("savedSlotQuantities", quantities.toString());
        return slotQuantity;
    }
    
    private int loadSlots() {
        JsonEntry slotsJson = new JsonEntry(new JsonReader().parse("{\"slots\":" + getString("savedSlots") + "," + "\"productQuantities\":" + getString("savedSlotQuantities") + "}"));
        int[] productQuantities = slotsJson.getIntArray(new int[]{}, "productQuantities");
        String[] slotNames = slotsJson.getStringArray(new String[]{}, "slots");
        boolean nextRow = false;
        for (int i = 0; i < productQuantities.length; i++) {
            addShopSlot(slotNames[i], productQuantities[i], nextRow);
            nextRow = !nextRow;
        }
        return productQuantities.length;
    }
    
    private ImageButton addSlot(final String result, final int quantity, boolean nextRow) {
        
        ImageButton.ImageButtonStyle slotStyle;
        ImageButton.ImageButtonStyle lockedSlotStyle;
        
        lockedSlotStyle = new ImageButton.ImageButtonStyle();
        lockedSlotStyle.up = slotSkin.getDrawable("slot_disabled");
        lockedSlotStyle.down = slotSkin.getDrawable("slot_disabled_down");
        lockedSlotStyle.over = slotSkin.getDrawable("slot_disabled_over");
        
        slotStyle = new ImageButton.ImageButtonStyle();
        slotStyle.up = slotSkin.getDrawable("slot");
        slotStyle.over = slotSkin.getDrawable("slot_over");
        slotStyle.down = slotSkin.getDrawable("slot_enabled");
        
        Image imageUp_scaled = new Image(this.items.findRegion(getItemCodeNameByName(result)));
        Image imageOver_scaled = new Image(this.items.findRegion("over_" + getItemCodeNameByName(result)));
        Image imageDisabled_scaled = new Image(this.items.findRegion("disabled_" + getItemCodeNameByName(result)));
        Image imageDown_scaled = new Image(this.items.findRegion("enabled_" + getItemCodeNameByName(result)));
        
        float heightBefore = imageUp_scaled.getHeight();
        float widthBefore = imageUp_scaled.getWidth();
        float height = 64;
        float width = (height / heightBefore) * widthBefore;
        
        slotStyle.imageUp = imageUp_scaled.getDrawable();
        slotStyle.imageOver = imageOver_scaled.getDrawable();
        slotStyle.imageDown = imageDown_scaled.getDrawable();
        
        lockedSlotStyle.imageUp = imageDisabled_scaled.getDrawable();
        
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        
        ImageButton slot = new ImageButton(slotStyle) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                try {
                    super.draw(batch, parentAlpha);
                } catch (Exception e) {
                    log("error drawing " +
                            getItemCodeNameByName(result) +
                            "\n" + items.findRegion(getItemCodeNameByName(result)) +
                            "\n" + items.findRegion("over_" + getItemCodeNameByName(result)) +
                            "\n" + items.findRegion("enabled_" + getItemCodeNameByName(result)) +
                            "\n" + items.findRegion("disabled_" + getItemCodeNameByName(result)), CRITICAL_ERROR);
                }
            }
        };
        
        Label text = new Label(result, labelStyle);
        text.setFontScale(0.28f, 0.3f);
        text.setColor(Color.YELLOW);
        
        Label quantityLabel = new Label("" + quantity, labelStyle);
        quantityLabel.setFontScale(0.4f);
        quantityLabel.setColor(Color.YELLOW);
        quantityLabel.setBounds(133, 88, 50, 20);
        quantityLabel.setAlignment(Align.right);
        
        slot.getImageCell().size(width, height).padBottom(5).row();
        slot.add(text).padRight(10).padLeft(10).padTop(10);
        slot.addActor(quantityLabel);
        
        if (nextRow) {
            table.add(slot).padBottom(5).padTop(5).padLeft(5).size(205, 120);
            table.row();
        } else {
            table.add(slot).padBottom(5).padTop(5).padRight(5).size(205, 120);
        }
        
        return slot;
    }
    
    private void addShopSlot(final String result, final int quantity, boolean nextRow) {
        addSlot(result, quantity, nextRow).addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new PurchaseDialogue(assetManager, stage, result, quantity, ItemSlotManager.this);
            }
        });
    }
    
    private void addInventorySlot(final String result, final int quantity, boolean nextRow) {
        addSlot(result, quantity, nextRow).addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new SellScrapDialogue(assetManager, stage, ItemSlotManager.this, quantity, result);
            }
        });
    }
    
    private void addInfo() {
        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;
        
        Table holder = new Table();
        Label uraniumCells_text = new Label("" + getInteger("money"), yellowLabelStyle);
        Label cogs_text = new Label("" + getInteger("cogs"), yellowLabelStyle);
        uraniumCells_text.setFontScale(0.5f);
        cogs_text.setFontScale(0.5f);
        
        Image uraniumCell = new Image((Texture) assetManager.get("uraniumCell.png"));
        uraniumCell.setScaling(Scaling.fit);
        holder.add(uraniumCell).size(30, 30);
        holder.add(uraniumCells_text).padLeft(5);
        
        Table holder2 = new Table();
        holder2.add(new Image(assetManager.get("bonuses.atlas", TextureAtlas.class).findRegion("bonus_part"))).size(30, 30);
        holder2.add(cogs_text).padLeft(5);
        inventory.align(Align.left);
        final long[] nextUpdateTime = {getLong("lastGenTime") + 18000000};
        long lastReset = nextUpdateTime[0] - TimeUtils.millis();
        int hours = (int) TimeUnit.MILLISECONDS.toHours(lastReset);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(lastReset) - hours * 60;
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(lastReset) - hours * 3600 - minutes * 60;
        Label resetTime = new Label("||Reset in: " + hours + "h " + minutes + "m " + seconds + "s||", yellowLabelStyle) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                long nextReset = nextUpdateTime[0] - TimeUtils.millis();
                int hours = (int) TimeUnit.MILLISECONDS.toHours(nextReset);
                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(nextReset) - hours * 60;
                int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(nextReset) - hours * 3600 - minutes * 60;
                this.setText("||Reset in: " + hours + "h " + minutes + "m " + seconds + "s||");
                if (hours <= 0 && minutes <= 0 && seconds <= 0) {
                    putLong("lastGenTime", TimeUtils.millis());
                    nextUpdateTime[0] = TimeUtils.millis() + 18000000;
                    table.clearChildren();
                    generateSlots();
                }
            }
        };
        resetTime.setFontScale(0.5f);
        resetTime.setColor(Color.ORANGE);
        resetTime.setWrap(true);
        Table moneyAndCogs = new Table();
        moneyAndCogs.add(holder).align(Align.left).padLeft(10).padBottom(5).row();
        moneyAndCogs.add(holder2).align(Align.left).padLeft(10);
        
        inventory.add(moneyAndCogs);
        
        if (slotManagerMode == SHOP) {
            inventory.add(resetTime).padLeft(10).align(Align.center).width(160).expand();
        } else {
            Label name = new Label("Inventory", yellowLabelStyle);
            name.setColor(Color.ORANGE);
            name.setFontScale(0.6f);
            inventory.add(name).expand().align(Align.center);
        }
    }
    
    void attach(Stage stage) {
        stage.addActor(holderGroup);
        this.stage = stage;
    }
    
    public void setBounds(float x, float y, float width, float height) {
        height -= 85;
        inventory.setBounds(x, y + height, width, 85);
        scrollPane.setBounds(x, y, width, height);
    }
    
    public void update() {
        inventory.clearChildren();
        table.clearChildren();
        if (slotManagerMode == SHOP) {
            addShopSlots();
        } else {
            addInventorySlots();
        }
    }
    
    private int getComplexity(String result) {
        int complexity = 0;
        if (treeJson.get(result) == null) {
            log("no item declared with name " + result, ERROR);
            return 100;
        } else {
            
            String[] items = treeJson.getStringArray(new String[]{}, result, "items");
            for (String item : items) {
                int buffer = getComplexity(item);
                complexity += buffer + 1;
            }
            
            complexity = MathUtils.clamp((int) (Math.ceil(complexity / 2f) - 1) * 4, 0, 15);
            return complexity;
        }
    }
}
