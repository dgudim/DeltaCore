package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.enemies.Boss_battleShip;
import com.deo.flapd.model.enemies.Boss_evilEye;

import java.util.Random;

import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getFloat;

public class Bonus {

    private Polygon bounds;
    private static Array<Rectangle> bonuses;
    private static Array<Integer> types;
    private static Array<Float> anglesY;
    private Array<ParticleEffect> explosions;
    private Sprite bonus_health, bonus_charge, bonus_shield, bonus_part, bonus_bullets, boss;

    private static float width, height;

    private BitmapFont font_text;

    private float uiScale;

    private Texture bonus_bullets_t;
    private static Random random;

    private Boss_battleShip boss_battleShip;
    private Boss_evilEye boss_evilEye;

    public Bonus(AssetManager assetManager, float width, float height, Polygon shipBounds, Boss_battleShip boss_battleShip, Boss_evilEye boss_evilEye) {
        bounds = shipBounds;

        random = new Random();

        TextureAtlas bonusesAtlas = assetManager.get("bonuses.atlas");
        bonus_health = new Sprite(bonusesAtlas.findRegion("bonus_health"));
        bonus_shield = new Sprite(bonusesAtlas.findRegion("bonus_shield"));
        bonus_charge = new Sprite(bonusesAtlas.findRegion("bonus_energy"));
        bonus_part = new Sprite(bonusesAtlas.findRegion("bonus_part"));
        bonus_bullets = new Sprite(bonusesAtlas.findRegion("bonus_bullets"));
        boss = new Sprite(bonusesAtlas.findRegion("bonus_boss"));

        bonus_health.setSize(width, height);
        bonus_shield.setSize(width, height);
        bonus_charge.setSize(width, height);
        bonus_part.setSize(width, height);
        bonus_bullets.setSize(width, height);
        boss.setSize(width, height);

        bonus_health.setOrigin(bonus_health.getWidth() / 2f, bonus_health.getHeight() / 2f);
        bonus_shield.setOrigin(bonus_shield.getWidth() / 2f, bonus_shield.getHeight() / 2f);
        bonus_charge.setOrigin(bonus_charge.getWidth() / 2f, bonus_charge.getHeight() / 2f);
        bonus_part.setOrigin(bonus_part.getWidth() / 2f, bonus_part.getHeight() / 2f);
        bonus_bullets.setOrigin(bonus_bullets.getWidth() / 2f, bonus_bullets.getHeight() / 2f);
        boss.setOrigin(boss.getWidth() / 2f, boss.getHeight() / 2f);

        bonus_bullets_t = bonusesAtlas.findRegion("bonus_bullets").getTexture();

        Bonus.width = width;
        Bonus.height = height;

        bonuses = new Array<>();
        types = new Array<>();
        explosions = new Array<>();
        anglesY = new Array<>();

        font_text = assetManager.get("fonts/font2(old).fnt");

        uiScale = getFloat("ui");

        this.boss_battleShip = boss_battleShip;
        this.boss_evilEye = boss_evilEye;
    }

    public static void Spawn(int type, Rectangle enemy) {
        Spawn(type, enemy.getX() + enemy.width / 2 - width / 2, enemy.getY() + enemy.height / 2 - height / 2);
    }

    public static void Spawn(int type, float x, float y) {

        Rectangle bonus = new Rectangle();

        bonus.x = x;
        bonus.y = y;

        bonus.setSize(width, height);

        bonuses.add(bonus);
        types.add(type);
        anglesY.add(random.nextFloat() * 2 - 1);
    }

    public void draw(SpriteBatch batch, float delta) {

        for (int i = 0; i < bonuses.size; i++) {

            Rectangle bonus = bonuses.get(i);
            Integer type = types.get(i);

            float angleY = anglesY.get(i);

            switch (type) {
                case (0):
                    this.bonus_charge.setPosition(bonus.x, bonus.y);
                    this.bonus_charge.draw(batch);
                    break;
                case (1):
                    this.bonus_shield.setPosition(bonus.x, bonus.y);
                    this.bonus_shield.draw(batch);
                    break;
                case (2):
                    this.bonus_health.setPosition(bonus.x, bonus.y);
                    this.bonus_health.draw(batch);
                    break;
                case (3):
                    this.bonus_bullets.setPosition(bonus.x, bonus.y);
                    this.bonus_bullets.draw(batch);
                    break;
                case (4):
                    this.bonus_part.setPosition(bonus.x, bonus.y);
                    this.bonus_part.draw(batch);
                    break;
                case (5):
                    this.boss.setPosition(bonus.x, bonus.y);
                    this.boss.draw(batch);
                    break;
            }

            bonus.y -= angleY * 15 * delta;
            bonus.x -= 50 * delta;

            if (bonus.y < -height || bonus.y > 480 || bonus.x < -height || bonus.x > 800) {
                removeBonus(i, false);
            }

            if (bonus.overlaps(bounds.getBoundingRectangle())) {
                if (type == 0) {
                    removeBonus(i, true);
                    if (SpaceShip.Charge <= SpaceShip.chargeCapacity - 10) {
                        SpaceShip.Charge += 10;
                    } else {
                        SpaceShip.Charge = SpaceShip.chargeCapacity;
                    }
                }
                if (type == 1) {
                    removeBonus(i, true);
                    if (SpaceShip.Shield <= SpaceShip.shieldStrength - 10) {
                        SpaceShip.Shield += 10;
                    } else {
                        SpaceShip.Shield = SpaceShip.shieldStrength;
                    }
                }
                if (type == 2) {
                    removeBonus(i, true);
                    if (SpaceShip.Health <= SpaceShip.Health * SpaceShip.healthMultiplier - 10) {
                        SpaceShip.Health += 10;
                    } else {
                        SpaceShip.Health = SpaceShip.Health * SpaceShip.healthMultiplier;
                    }
                }
                if (type == 3) {
                    removeBonus(i, true);
                    if (GameLogic.bonuses_collected < 10) {
                        GameLogic.bonuses_collected += 1;
                    } else {
                        addInteger("cogs", 1);
                    }
                }
                if (type == 4) {
                    removeBonus(i, true);
                    addInteger("cogs", 1);
                }
                if (type == 5) {
                    removeBonus(i, true);
                    if (random.nextBoolean()) {
                        boss_battleShip.Spawn();
                    } else {
                        boss_evilEye.Spawn();
                    }
                    GameLogic.bossWave = true;
                }
            }
        }
        if (GameLogic.bonuses_collected > 0) {
            font_text.setColor(Color.WHITE);
            font_text.getData().setScale(0.3f * uiScale);
            font_text.draw(batch, "X" + GameLogic.bonuses_collected, 333 - 463 * (uiScale - 1), 425 - 55 * (uiScale - 1), 24 * uiScale, 1, false);
            batch.draw(bonus_bullets_t, 319 - 475 * (uiScale - 1), 475 - 50 * uiScale, 50 * uiScale, 50 * uiScale);
            font_text.setColor(Color.BLACK);
        }
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).draw(batch);
            explosions.get(i3).update(delta);
            if (explosions.get(i3).isComplete()) {
                explosions.get(i3).dispose();
                explosions.removeIndex(i3);
            }
        }
    }

    public void dispose() {
        bonuses.clear();
        types.clear();
        anglesY.clear();
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        font_text.dispose();
    }

    private void removeBonus(int i, boolean explode) {
        if (explode) {
            ParticleEffect explosionEffect = new ParticleEffect();
            switch (types.get(i)) {
                case (1):
                    explosionEffect.load(Gdx.files.internal("particles/explosion4.p"), Gdx.files.internal("particles"));
                    break;
                case (2):
                case (5):
                    explosionEffect.load(Gdx.files.internal("particles/explosion4_1.p"), Gdx.files.internal("particles"));
                    break;
                case (0):
                case (3):
                    explosionEffect.load(Gdx.files.internal("particles/explosion4_2.p"), Gdx.files.internal("particles"));
                    break;
                case (4):
                    explosionEffect.load(Gdx.files.internal("particles/explosion4_3.p"), Gdx.files.internal("particles"));
                    break;
            }
            explosionEffect.setPosition(bonuses.get(i).x + bonuses.get(i).width / 2, bonuses.get(i).y + bonuses.get(i).height / 2);
            explosionEffect.start();
            explosions.add(explosionEffect);
        }
        bonuses.removeIndex(i);
        types.removeIndex(i);
        anglesY.removeIndex(i);
    }
}
