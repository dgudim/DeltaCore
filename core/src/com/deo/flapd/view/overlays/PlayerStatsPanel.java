package com.deo.flapd.view.overlays;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getString;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.ui.UIComposer;

public class PlayerStatsPanel extends Group {
    
    private final UIComposer uiComposer;
    private final AssetManager assetManager;
    private final LocaleManager localeManager;
    private final JsonEntry treeJson;
    
    private final Table statsContainer;
    
    private float animationPosition = 0;
    private float animationDirection = -1;
    
    public PlayerStatsPanel(CompositeManager compositeManager, Stage stage) {
        treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        
        assetManager = compositeManager.getAssetManager();
        uiComposer = compositeManager.getUiComposer();
        localeManager = compositeManager.getLocaleManager();
        statsContainer = new Table();
        
        rebuild();
        
        statsContainer.setBackground(new TextureRegionDrawable(assetManager.get("ui/menuUi.atlas", TextureAtlas.class).findRegion("stats")));
        statsContainer.setBounds(545, 5, 250, 395);
        statsContainer.align(Align.top);
    
        Image touchBlock = new Image();
        touchBlock.setBounds(545, 5, 250, 395);
        
        addActor(touchBlock);
        addActor(statsContainer);
        setVisible(false);
        stage.addActor(this);
    }
    
    public void addText(String text, boolean title, String color) {
        Table titleTable = new Table();
        titleTable.align(Align.left);
        if (title) {
            titleTable.setBackground(constructFilledImageWithColor(1, 1, Color.valueOf("#262626")));
        }
        Label categoryLabel = uiComposer.addText(text, assetManager.get("fonts/pixel.ttf"), title? 0.48f : 0.42f);
        categoryLabel.getStyle().fontColor = Color.valueOf(color);
        categoryLabel.setFillParent(true);
        categoryLabel.setAlignment(title ? Align.center : Align.left);
        titleTable.add(categoryLabel).padLeft(title ? 0 : 5);
        statsContainer.add(titleTable).width(246).row();
    }
    
    public void toggle(){
        animationDirection *= -1;
    }
    
    public void close(){
        animationDirection = -1;
    }
    
    public void rebuild(){
    
        statsContainer.clear();
        
        JsonEntry weaponConfig = treeJson.get(getString(Keys.currentWeapon), "parameters");
        JsonEntry coreConfig = treeJson.get(getString(Keys.currentCore), "parameters");
        JsonEntry moduleConfig = treeJson.get(getString(Keys.currentModule), "parameters");
        JsonEntry engineConfig = treeJson.get(getString(Keys.currentModule), "parameters");
        JsonEntry hullConfig = treeJson.get(getString(Keys.currentHull), "parameters");
        JsonEntry batteryConfig = treeJson.get(getString(Keys.currentBattery), "parameters");
        JsonEntry shieldConfig = treeJson.get(getString(Keys.currentShield), "parameters");
    
        float weight =
                weaponConfig.getFloat(false, 1, "parameter.weight") +
                        coreConfig.getFloat(false, 1, "parameter.weight") +
                        (moduleConfig == null ? 0 : moduleConfig.getFloat(false, 1, "parameter.weight")) +
                        engineConfig.getFloat(false, 1, "parameter.weight") +
                        hullConfig.getFloat(false, 1, "parameter.weight") +
                        batteryConfig.getFloat(false, 1, "parameter.weight") +
                        shieldConfig.getFloat(false, 1, "parameter.weight");
    
        float accelerationForce = engineConfig.getFloat(false, 1, "parameter.acceleration_force");
        float acceleration = accelerationForce / weight;
    
        float topSpeed = engineConfig.getFloat(false, 1, "parameter.speed")
                * coreConfig.getFloat(false, 1, "parameter.speed_multiplier");
    
        float damage = weaponConfig.getFloat(false, 1, "parameter.damage")
                * coreConfig.getFloat(false, 1, "parameter.damage_multiplier");
    
        float health = hullConfig.getFloat(false, 1, "parameter.health")
                * coreConfig.getFloat(false, 1, "parameter.health_multiplier");
    
        float shield_health = shieldConfig.getFloat(false, 1, "parameter.shield_capacity")
                * coreConfig.getFloat(false, 1, "parameter.shield_strength_multiplier");
    
        float shield_regeneration = shieldConfig.getFloat(false, 1, "parameter.regeneration_speed");
    
        float capacity = batteryConfig.getFloat(false, 1, "parameter.capacity")
                * coreConfig.getFloat(false, 1, "parameter.charge_capacity_multiplier");
    
        float powerGeneration = coreConfig.getFloat(false, 1, "parameter.power_generation");
    
        float powerConsumption =
                weaponConfig.getFloat(false, 0, "parameter.power_consumption") +
                        (moduleConfig == null ? 0 : moduleConfig.getFloat(false, 1, "parameter.power_consumption")) +
                        shieldConfig.getFloat(false, 1, "parameter.power_consumption");
    
        addText(localeManager.get("menu.player_stats"), true, "#ffb121");
        addText(localeManager.get("stats.total_damage") + ": " + damage, false, "#ff5141");
        addText(localeManager.get("stats.total_health") + ": " + health, false, "#ff5141");
        addText(localeManager.get("stats.total_shield_health") + ": " + shield_health, false, "#21b1ff");
        addText(localeManager.get("stats.shield_regeneration") + ": " + shield_regeneration, false, "#21b1ff");
        addText(localeManager.get("stats.top_speed") + ": " + topSpeed, false, "#efff21");
        addText(localeManager.get("stats.acceleration") + ": " + acceleration, false, "#efff21");
        addText(localeManager.get("stats.capacity") + ": " + capacity, false, "#ffffcc");
        addText(localeManager.get("stats.power_generation") + ": " + powerGeneration, false, "#ffffcc");
        addText(localeManager.get("stats.power_consumption") + ": " + powerConsumption, false, "#ffffcc");
        addText(localeManager.get("stats.power_balance") + ": " + (powerGeneration - powerConsumption), false, (powerGeneration - powerConsumption) < 1 ? "#ff5141" : "#b1ff21");
    }
    
    @Override
    public void act(float delta) {
        animationPosition = clamp(animationPosition + delta * animationDirection, 0, 1);
        setColor(1, 1, 1, animationPosition);
        if(animationPosition == 0 && isVisible()){
            setVisible(false);
        }else if(animationPosition > 0 && !isVisible()){
            setVisible(true);
        }
    }
    
}
