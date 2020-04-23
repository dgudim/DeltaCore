package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getString;

public abstract class ShipObject {

    public static Polygon bounds;
    private Sprite ship;
    private Sprite shield;

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

    private boolean isFireStarted1, isFireStarted2, isFireStarted3;

    public static float Health, Shield, Charge;

    private float powerGeneration;

    private float shieldRegenerationSpeed;

    public static float shieldStrength;

    private float shieldPowerConsumption;

    public static float chargeCapacity;

    public static float healthMultiplier = 1;

    ShipObject(AssetManager assetManager, float x, float y, float width, float height, boolean newGame) {

        JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("shop/tree.json"));
        String[] params = treeJson.get(getString("currentCore")).get("parameters").asStringArray();
        float[] paramValues = treeJson.get(getString("currentCore")).get("parameterValues").asFloatArray();
        for (int i = 0; i < params.length; i++) {
            if (params[i].endsWith("power generation")) {
                powerGeneration = paramValues[i];
            }
            if (params[i].endsWith("health multiplier")) {
                Health *= paramValues[i];
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

        chargeCapacity = treeJson.get(getString("currentBattery")).get("parameterValues").asFloatArray()[0];

        ship = new Sprite((Texture)assetManager.get("ship.png"));
        shield = new Sprite(assetManager.get("shields.atlas", TextureAtlas.class).findRegion(treeJson.get(getString("currentShield")).get("usesEffect").asString()));

        bounds = new Polygon(new float[]{0f, 0f, width, 0f, width, height, 0f, height});
        if (!newGame) {
            Shield = getFloat("Shield");
            Health = getFloat("Health");
            Charge = getFloat("Charge");
            bounds.setPosition(getFloat("ShipX"), getFloat("ShipY"));
        } else {
            Shield = shieldStrength;
            Health = 100*healthMultiplier;
            Charge = chargeCapacity;
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

        red = 1;
        green = 1;
        blue = 1;
        red2 = 1;
        green2 = 1;
        blue2 = 1;
        exploded = false;
        isFireStarted1 = false;
        isFireStarted2 = false;
        isFireStarted3 = false;
        soundVolume = getFloat("soundVolume");

        explosion = Gdx.audio.newSound(Gdx.files.internal("sfx/explosion.ogg"));
    }

    public void drawEffects(SpriteBatch batch, float delta) {
        if (!exploded) {
            fire.setPosition(bounds.getX() + 10, bounds.getY() + 18);
            fire.draw(batch);
            fire2.setPosition(bounds.getX() + 4, bounds.getY() + 40);
            fire2.draw(batch);
            damage_fire.setPosition(bounds.getX() + 10, bounds.getY() + 25);
            damage_fire2.setPosition(bounds.getX() + 10, bounds.getY() + 25);
            damage_fire3.setPosition(bounds.getX() + 10, bounds.getY() + 25);

            fire.update(delta);
            fire2.update(delta);

            if (Health < 70) {
                if (!isFireStarted1) {
                    damage_fire.start();
                }
                damage_fire.draw(batch, delta);
            } else if (isFireStarted1) {
                damage_fire.reset();
                isFireStarted1 = false;
            }

            if (Health < 50) {
                if (!isFireStarted2) {
                    damage_fire2.start();
                }
                damage_fire2.draw(batch, delta);
            } else if (isFireStarted2) {
                damage_fire2.reset();
                isFireStarted2 = false;
            }

            if (Health < 30) {
                if (!isFireStarted3) {
                    damage_fire3.start();
                }
                damage_fire3.draw(batch, delta);
            } else if (isFireStarted3) {
                damage_fire3.reset();
                isFireStarted3 = false;
            }
        }
    }

    public void draw(SpriteBatch batch, float delta) {
        if (!exploded) {
            ship.setPosition(bounds.getX(), bounds.getY());
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

            ship.draw(batch);

            if (Charge + powerGeneration * delta < chargeCapacity) {
                Charge += powerGeneration * delta;
            } else {
                Charge = chargeCapacity;
            }
            if (Shield < shieldStrength && Charge>=shieldPowerConsumption*delta) {
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

            if (red2 < 1) {
                red2 = red2 + 5 * delta;
            }
            if (green2 < 1) {
                green2 = green2 + 5 * delta;
            }
            if (blue2 < 1) {
                blue2 = blue2 + 5 * delta;
            }

            shield.draw(batch);
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
