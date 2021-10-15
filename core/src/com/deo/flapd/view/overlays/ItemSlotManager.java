package com.deo.flapd.view.overlays;

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
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.ui.UIComposer;
import com.deo.flapd.view.dialogues.ConfirmationDialogue;
import com.deo.flapd.view.dialogues.PurchaseDialogue;
import com.deo.flapd.view.dialogues.SellScrapDialogue;

import java.util.concurrent.TimeUnit;

import static com.badlogic.gdx.utils.TimeUtils.millis;
import static com.deo.flapd.utils.DUtils.LogLevel.CRITICAL_ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getLong;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putLong;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.subtractInteger;
import static com.deo.flapd.view.overlays.SlotManagerMode.INVENTORY;
import static com.deo.flapd.view.overlays.SlotManagerMode.SHOP;
import static java.lang.StrictMath.sqrt;

enum SlotManagerMode {INVENTORY, SHOP}

public class ItemSlotManager {
    
    private final BitmapFont font;
    private final Table table;
    private final Table inventoryLabel;
    public Group holderGroup;
    private final Skin slotSkin;
    private final TextureAtlas items;
    private final ScrollPane scrollPane;
    private Stage stage;
    
    private final CompositeManager compositeManager;
    private final AssetManager assetManager;
    private final LocaleManager localeManager;
    
    private SlotManagerMode slotManagerMode;
    private final UIComposer uiComposer;
    
    private final JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
    
    public ItemSlotManager(CompositeManager compositeManager) {
        
        this.compositeManager = compositeManager;
        assetManager = compositeManager.getAssetManager();
        uiComposer = compositeManager.getUiComposer();
        localeManager = compositeManager.getLocaleManager();
        
        slotSkin = new Skin();
        slotSkin.addRegions(assetManager.get("shop/workshop.atlas"));
        
        items = assetManager.get("items/items.atlas");
        font = assetManager.get("fonts/pixel.ttf");
        font.getData().setScale(0.2f);
        
        inventoryLabel = new Table();
        inventoryLabel.setBackground(new TextureRegionDrawable(assetManager.get("ui/gameUi.atlas", TextureAtlas.class).findRegion("buttonPauseBlank_disabled")));
        
        holderGroup = new Group();
        table = new Table();
        scrollPane = new ScrollPane(table);
        
        holderGroup.addActor(scrollPane);
        holderGroup.addActor(inventoryLabel);
    }
    
    public void addShopSlots() {
        long timeSnap = millis();
        slotManagerMode = SHOP;
        int slotCount;
        long lastGenerationTime = getLong(Keys.shopLastGenerationTime);
        if (TimeUtils.timeSinceMillis(lastGenerationTime) > 18000000) {
            putLong(Keys.shopLastGenerationTime, millis());
            slotCount = generateShopSlots();
        } else {
            slotCount = loadSlots();
        }
        if (slotCount == 0) {
            final TextButton update = uiComposer.addTextButton("workshopGreen", localeManager.get("workshop.market.reset"), 0.48f);
            update.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    new ConfirmationDialogue(compositeManager, stage, localeManager.get("workshop.market.reset"), new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (getInteger(Keys.moneyAmount) >= 3500) {
                                subtractInteger(Keys.moneyAmount, 3500);
                                putLong(Keys.shopLastGenerationTime, 0);
                                ItemSlotManager.this.update();
                            }
                        }
                    });
                }
            });
            table.add(update).align(Align.center).size(300, 40);
        }
        addInfo();
        log("added shop slots in " + TimeUtils.timeSinceMillis(timeSnap) + "ms", INFO);
    }
    
    public void addInventorySlots() {
        long timeSnap = millis();
        slotManagerMode = INVENTORY;
        boolean nextRow = false;
        for (int i = 0; i < treeJson.size; i++) {
            int itemQuantity = getInteger("item_" + treeJson.get(i).name);
            if (itemQuantity > 0) {
                addInventorySlot(treeJson.get(i).name, itemQuantity, nextRow);
                nextRow = !nextRow;
            }
        }
        addInfo();
        log("added inventory slots in " + TimeUtils.timeSinceMillis(timeSnap) + "ms", INFO);
    }
    
    private int generateShopSlots() {
        long timeSnap = millis();
        Array<String> itemsToAdd = new Array<>();
        Array<Integer> quantities = new Array<>();
        
        for (int i = 0; i < treeJson.size; i++) {
            String type = treeJson.getString("item", i, "type");
            if (type.equals("item") || type.equals("endItem")) {
                itemsToAdd.add(treeJson.get(i).name);
            }
        }
        boolean nextRow = false;
        Array<String> addedItems = new Array<>();
        int slotQuantity = getRandomInRange(itemsToAdd.size / 4, itemsToAdd.size / 2);
        for (int i = 0; i < slotQuantity; i++) {
            int index = getRandomInRange(0, itemsToAdd.size - 1);
            int quantity = (int) MathUtils.clamp(getRandomInRange(3, 15) - sqrt(getComplexity(itemsToAdd.get(index))) * 2, 1, 15);
            addedItems.add(itemsToAdd.get(index));
            quantities.add(quantity);
            addShopSlot(itemsToAdd.get(index), quantity, nextRow);
            itemsToAdd.removeIndex(index);
            nextRow = !nextRow;
        }
        
        putString(Keys.savedShopSlots, addedItems.toString());
        putString(Keys.savedShopSlotsQuantities, quantities.toString());
        log("generated shop slots in " + TimeUtils.timeSinceMillis(timeSnap) + "ms", INFO);
        return slotQuantity;
    }
    
    private int loadSlots() {
        JsonEntry slotsJson = new JsonEntry(new JsonReader().parse("{\"slots\":" + getString(Keys.savedShopSlots) + "," + "\"productQuantities\":" + getString(Keys.savedShopSlotsQuantities) + "}"));
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
        lockedSlotStyle.up = new NinePatchDrawable(slotSkin.getPatch("slot_disabled"));
        lockedSlotStyle.down = new NinePatchDrawable(slotSkin.getPatch("slot_disabled_down"));
        lockedSlotStyle.over = new NinePatchDrawable(slotSkin.getPatch("slot_disabled_over"));
        
        slotStyle = new ImageButton.ImageButtonStyle();
        slotStyle.up = new NinePatchDrawable(slotSkin.getPatch("slot"));
        slotStyle.over = new NinePatchDrawable(slotSkin.getPatch("slot_over"));
        slotStyle.down = new NinePatchDrawable(slotSkin.getPatch("slot_enabled"));
        
        Image imageUp_scaled = new Image(this.items.findRegion(result));
        Image imageOver_scaled = new Image(this.items.findRegion(result + "_over"));
        Image imageDisabled_scaled = new Image(this.items.findRegion(result + "_disabled"));
        Image imageDown_scaled = new Image(this.items.findRegion(result + "_enabled"));
        
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
                            result +
                            "\nnormal: " + items.findRegion(result) +
                            "\nover: " + items.findRegion(result + "_over") +
                            "\nenabled: " + items.findRegion(result + "_enabled") +
                            "\ndisabled: " + items.findRegion(result + "_disabled"), CRITICAL_ERROR);
                }
            }
        };
        
        Label text = new Label(localeManager.get(result), labelStyle);
        text.setFontScale(0.28f, 0.3f);
        text.setColor(Color.YELLOW);
        
        Label quantityLabel = new Label("" + quantity, labelStyle);
        quantityLabel.setFontScale(0.4f);
        quantityLabel.setColor(Color.YELLOW);
        quantityLabel.setBounds(133, 88, 50, 20);
        quantityLabel.setAlignment(Align.right);
        
        slot.getImageCell().size(width, height).padTop(25).row();
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
                new PurchaseDialogue(compositeManager, stage, result, quantity, ItemSlotManager.this);
            }
        });
    }
    
    private void addInventorySlot(final String result, final int quantity, boolean nextRow) {
        addSlot(result, quantity, nextRow).addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new SellScrapDialogue(compositeManager, stage, ItemSlotManager.this, quantity, result);
            }
        });
    }
    
    private void addInfo() {
        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;
        
        Table holder = new Table();
        Label uraniumCells_text = new Label("" + getInteger(Keys.moneyAmount), yellowLabelStyle);
        Label cogs_text = new Label("" + getInteger(Keys.cogAmount), yellowLabelStyle);
        uraniumCells_text.setFontScale(0.5f);
        cogs_text.setFontScale(0.5f);
        
        Image uraniumCell = new Image((Texture) assetManager.get("uraniumCell.png"));
        uraniumCell.setScaling(Scaling.fit);
        holder.add(uraniumCell).size(30, 30);
        holder.add(uraniumCells_text).padLeft(5);
        
        Table holder2 = new Table();
        holder2.add(new Image(assetManager.get("bonuses.atlas", TextureAtlas.class).findRegion("bonus_part"))).size(30, 30);
        holder2.add(cogs_text).padLeft(5);
        inventoryLabel.align(Align.left);
        final long[] nextUpdateTime = {getLong(Keys.shopLastGenerationTime) + 18000000};
        long lastReset = nextUpdateTime[0] - millis();
        int hours = (int) TimeUnit.MILLISECONDS.toHours(lastReset);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(lastReset) - hours * 60;
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(lastReset) - hours * 3600 - minutes * 60;
        Label resetTime = new Label("||" + localeManager.get("workshop.market.resetIn") + " " + hours + "h " + minutes + "m " + seconds + "s||", yellowLabelStyle) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                long nextReset = nextUpdateTime[0] - millis();
                int hours = (int) TimeUnit.MILLISECONDS.toHours(nextReset);
                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(nextReset) - hours * 60;
                int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(nextReset) - hours * 3600 - minutes * 60;
                this.setText("||" + localeManager.get("workshop.market.resetIn") + " " + hours + "h " + minutes + "m " + seconds + "s||");
                if (hours <= 0 && minutes <= 0 && seconds <= 0) {
                    putLong(Keys.shopLastGenerationTime, millis());
                    nextUpdateTime[0] = millis() + 18000000;
                    table.clearChildren();
                    generateShopSlots();
                }
            }
        };
        resetTime.setFontScale(0.5f);
        resetTime.setColor(Color.ORANGE);
        resetTime.setWrap(true);
        Table moneyAndCogs = new Table();
        moneyAndCogs.add(holder).align(Align.left).padLeft(10).padBottom(5).row();
        moneyAndCogs.add(holder2).align(Align.left).padLeft(10);
        
        inventoryLabel.add(moneyAndCogs);
        
        if (slotManagerMode == SHOP) {
            inventoryLabel.add(resetTime).padLeft(10).align(Align.center).width(160).expand();
        } else {
            Label name = new Label(localeManager.get("workshop.inventory"), yellowLabelStyle);
            name.setColor(Color.ORANGE);
            name.setFontScale(0.6f);
            inventoryLabel.add(name).expand().align(Align.center);
        }
    }
    
    public void attach(Stage stage) {
        stage.addActor(holderGroup);
        this.stage = stage;
    }
    
    public void setBounds(float x, float y, float width, float height) {
        height -= 85;
        inventoryLabel.setBounds(x, y + height, width, 85);
        scrollPane.setBounds(x, y, width, height);
    }
    
    public void update() {
        inventoryLabel.clearChildren();
        table.clearChildren();
        if (slotManagerMode == SHOP) {
            addShopSlots();
        } else {
            addInventorySlots();
        }
    }
    
    private int getComplexity(String result) {
        int complexity = 0;
        if (treeJson.get(result).isNull()) {
            log("no item declared with name " + result, ERROR);
            return 0;
        } else {
            if (treeJson.getString("item", result, "type").equals("endItem")) {
                return 0;
            }
            JsonEntry items = treeJson.get(result, "items");
            for (int i = 0; i < items.size; i++) {
                int buffer = getComplexity(items.get(i).name);
                complexity += buffer + 1;
            }
            
            return complexity;
        }
    }
}
