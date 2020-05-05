package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getString;

public abstract class ShipObject {

    public static Polygon bounds;
    private Sprite ship;
    private Sprite shield;
    static Sprite magnetField;
    public static Sprite repellentField;

    private ParticleEffect fire, fire2, damage_fire, damage_fire2, damage_fire3;

    private static float red;
    private static float green;
    private static float blue;
    private static float red2;
    private static float green2;
    private static float blue2;

    private boolean exploded;

    private Sound explosion;

    private float soundVolume;

    public static float Health, Shield, Charge;

    private float shieldRegenerationSpeed;
    private float shieldPowerConsumption;
    public static float shieldStrength;
    public static float shieldStrengthMultiplier = 1;

    private float powerGeneration;
    public static float chargeCapacity;
    public static float chargeCapacityMultiplier = 1;

    public static float healthCapacity;
    public static float healthMultiplier = 1;

    static float magnetPowerConsumption;

    public static float repellentPowerConsumption;

    ShipObject(AssetManager assetManager, float x, float y, float width, float height, boolean newGame) {

        TextureAtlas fields = assetManager.get("shields.atlas", TextureAtlas.class);

        JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("shop/tree.json"));
        String[] params = treeJson.get(getString("currentCore")).get("parameters").asStringArray();
        float[] paramValues = treeJson.get(getString("currentCore")).get("parameterValues").asFloatArray();
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("power generation")) {
                powerGeneration = paramValues[i];
            }
            if (params[i].endsWith("health multiplier")) {
                healthMultiplier = paramValues[i];
            }
            if (params[i].endsWith("shield strength multiplier")) {
                shieldStrengthMultiplier = paramValues[i];
            }
            if (params[i].endsWith("charge capacity multiplier")) {
                chargeCapacityMultiplier = paramValues[i];
            }
        }

        params = treeJson.get(getString("currentShield")).get("parameters").asStringArray();
        paramValues = treeJson.get(getString("currentShield")).get("parameterValues").asFloatArray();
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("power consumption")) {
                shieldPowerConsumption = paramValues[i];
            }
            if (params[i].endsWith("shield regeneration speed")) {
                shieldRegenerationSpeed = paramValues[i];
            }
            if (params[i].endsWith("shield strength")) {
                shieldStrength = paramValues[i];
            }
        }

        magnetField = new Sprite(fields.findRegion("field_attractor"));
        repellentField = new Sprite(fields.findRegion("field_repellent"));

        if (!getString("currentMagnet").equals("")) {
            params = treeJson.get(getString("currentMagnet")).get("parameters").asStringArray();
            paramValues = treeJson.get(getString("currentMagnet")).get("parameterValues").asFloatArray();
            for (int i = 0; i < params.length; i++) {
                if (params[i].endsWith("power consumption")) {
                    magnetPowerConsumption = paramValues[i];
                }
                if (params[i].endsWith("attraction radius")) {
                    magnetField.setSize(paramValues[i], paramValues[i]);
                }
            }
        }

        if (!getString("currentRepellent").equals("")) {
            params = treeJson.get(getString("currentRepellent")).get("parameters").asStringArray();
            paramValues = treeJson.get(getString("currentRepellent")).get("parameterValues").asFloatArray();
            for (int i = 0; i < params.length; i++) {
                if (params[i].endsWith("power consumption")) {
                    repellentPowerConsumption = paramValues[i];
                }
                if (params[i].endsWith("repellent radius")) {
                    repellentField.setSize(paramValues[i], paramValues[i]);
                }
            }
        }

        chargeCapacity = treeJson.get(getString("currentBattery")).get("parameterValues").asFloatArray()[0];
        healthCapacity = treeJson.get(getString("currentArmour")).get("parameterValues").asFloatArray()[0];

        ship = new Sprite(assetManager.get("items/items.atlas", TextureAtlas.class).findRegion(getItemCodeNameByName(getString("currentArmour"))));
        shield = new Sprite(fields.findRegion(treeJson.get(getString("currentShield")).get("usesEffect").asString()));

        bounds = new Polygon(new float[]{0f, 0f, width, 0f, width, height, 0f, height});

        if (!newGame) {
            Shield = MathUtils.clamp(getFloat("Shield"), 0, shieldStrength * shieldStrengthMultiplier);
            Health = MathUtils.clamp(getFloat("Health"), 0, healthCapacity * healthMultiplier);
            Charge = MathUtils.clamp(getFloat("Charge"), 0, chargeCapacity * chargeCapacityMultiplier);
            bounds.setPosition(getFloat("ShipX"), getFloat("ShipY"));
        } else {
            Shield = shieldStrength * shieldStrengthMultiplier;
            Health = healthCapacity * healthMultiplier;
            Charge = chargeCapacity * chargeCapacityMultiplier;
            bounds.setPosition(x, y);
        }

        ship.setOrigin(width / 2f, height / 2f);
        shield.setOrigin((width + 30) / 2f, (height + 30) / 2f);

        ship.setSize(width, height);
        ship.setPosition(x, y);
        shield.setSize(width + 30, height + 30);
        shield.setPosition(x, y - 10);

        fire = new ParticleEffect();
        fire2 = new ParticleEffect();

        fire.load(Gdx.files.internal("particles/" + treeJson.get(getString("currentEngine")).get("usesEffect").asString() + ".p"), Gdx.files.internal("particles"));
        fire2.load(Gdx.files.internal("particles/" + treeJson.get(getString("currentEngine")).get("usesEffect").asString() + ".p"), Gdx.files.internal("particles"));

        float[] colors = new JsonReader().parse("{\"colors\":" + getString(treeJson.get(getString("currentEngine")).get("usesEffect").asString() + "_color") + "}").get("colors").asFloatArray();
        fire.getEmitters().get(0).getTint().setColors(colors);
        fire2.getEmitters().get(0).getTint().setColors(colors);

        fire.start();
        fire2.start();

        damage_fire = new ParticleEffect();
        damage_fire.load(Gdx.files.internal("particles/fire.p"), Gdx.files.internal("particles"));

        damage_fire2 = new ParticleEffect();
        damage_fire2.load(Gdx.files.internal("particles/fire.p"), Gdx.files.internal("particles"));

        damage_fire3 = new ParticleEffect();
        damage_fire3.load(Gdx.files.internal("particles/fire.p"), Gdx.files.internal("particles"));

        damage_fire.start();
        damage_fire2.start();
        damage_fire3.start();

        red = 1;
        green = 1;
        blue = 1;
        red2 = 1;
        green2 = 1;
        blue2 = 1;
        exploded = false;
        soundVolume = getFloat("soundVolume");

        explosion = Gdx.audio.newSound(Gdx.files.internal("sfx/explosion.ogg"));
    }

    public void drawEffects(SpriteBatch batch, float delta) {
        if (!exploded) {
            fire.setPosition(bounds.getX() + 10, bounds.getY() + 18);
            fire.draw(batch, delta);
            fire2.setPosition(bounds.getX() + 4, bounds.getY() + 40);
            fire2.draw(batch, delta);
            damage_fire.setPosition(bounds.getX() + 10, bounds.getY() + 25);
            damage_fire2.setPosition(bounds.getX() + 10, bounds.getY() + 25);
            damage_fire3.setPosition(bounds.getX() + 10, bounds.getY() + 25);

            if (Health < 70) {
                damage_fire.draw(batch, delta);
            }

            if (Health < 50) {
                damage_fire2.draw(batch, delta);
            }

            if (Health < 30) {
                damage_fire3.draw(batch, delta);
            }
        }
    }

    public void draw(SpriteBatch batch, float delta) {
        if (!exploded) {
            ship.setPosition(bounds.getX(), bounds.getY());

            magnetField.setPosition(bounds.getX() + bounds.getBoundingRectangle().getWidth() / 2 - magnetField.getWidth() / 2, bounds.getY() + bounds.getBoundingRectangle().getHeight() / 2 - magnetField.getHeight() / 2);
            repellentField.setPosition(bounds.getX() + bounds.getBoundingRectangle().getWidth() / 2 - repellentField.getWidth() / 2, bounds.getY() + bounds.getBoundingRectangle().getHeight() / 2 - repellentField.getHeight() / 2);

            ship.setRotation(bounds.getRotation());
            ship.setColor(red, green, blue, 1);

            if (red < 1) {
                red = red + 5 * delta;
            }
            if (green < 1) {
                green = green + 5 * delta;
            }
            if (blue < 1) {
                blue = blue + 5 * delta;
            }

            magnetField.draw(batch, Charge / (chargeCapacity * chargeCapacityMultiplier));
            repellentField.draw(batch, Charge / (chargeCapacity * chargeCapacityMultiplier));

            ship.draw(batch);

            if (Charge + powerGeneration * delta < chargeCapacity * chargeCapacityMultiplier) {
                Charge += powerGeneration * delta;
            } else {
                Charge = chargeCapacity * chargeCapacityMultiplier;
            }
            if (Shield < shieldStrength * shieldStrengthMultiplier && Charge >= shieldPowerConsumption * delta) {
                Shield += shieldRegenerationSpeed * delta;
                Charge -= shieldPowerConsumption * delta;
            }
        } else {
            bounds.setScale(0, 0);
        }
    }

    void drawShield(SpriteBatch batch, float alpha, float delta) {
        if (!exploded) {
            shield.setPosition(bounds.getX() - 20, bounds.getY() - 15);
            shield.setRotation(bounds.getRotation());
            shield.setColor(red2, green2, blue2, alpha);
            shield.draw(batch);

            if (red2 < 1) {
                red2 = red2 + 5 * delta;
            }
            if (green2 < 1) {
                green2 = green2 + 5 * delta;
            }
            if (blue2 < 1) {
                blue2 = blue2 + 5 * delta;
            }

        }
    }

    public Polygon getBounds() {
        return bounds;
    }

    public void dispose() {
        fire.dispose();
        fire2.dispose();
        explosion.dispose();
        damage_fire.dispose();
        damage_fire2.dispose();
        damage_fire3.dispose();
    }

    private static void set_color(float red1, float green1, float blue1, boolean shield) {
        if (!shield) {
            red = red1;
            green = green1;
            blue = blue1;
        } else {
            red2 = red1;
            green2 = green1;
            blue2 = blue1;
        }
    }

    public static void takeDamage(float damage) {
        if (Shield >= damage) {
            Shield -= damage;
            set_color(1, 0, 1, true);
        } else {
            Health = Health - (damage - Shield) / 5;
            Shield = 0;
            set_color(1, 0, 1, false);
        }
    }

    public void explode() {
        exploded = true;
        if (soundVolume > 0) {
            explosion.play(soundVolume / 100);
        }
    }
}
