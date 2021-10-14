package com.deo.flapd.model.loot;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.enemies.Bosses;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.view.screens.LoadingScreen.particleEffectPoolLoader;

public class Bonus {
    
    enum BonusType {HEALTH, SHIELD, CHARGE, PART, BULLETS, BOSS}
    
    private final Rectangle playerBounds;
    private final Player player;
    private static Array<Rectangle> bonuses;
    private static Array<BonusType> types;
    private static Array<Float> anglesY;
    private final Array<ParticleEffectPool.PooledEffect> explosions;
    private final Sprite bonus_health;
    private final Sprite bonus_charge;
    private final Sprite bonus_shield;
    private final Sprite bonus_part;
    private final Sprite bonus_bullets;
    private final Sprite boss;
    
    private final BitmapFont font_text;
    
    private final float uiScale;
    
    private final Image bonus_bullets_t;
    
    private final Bosses bosses;
    
    private static float height, width;
    
    public Bonus(AssetManager assetManager, float width, float height, Player player, Bosses bosses) {
        
        this.bosses = bosses;
        
        this.player = player;
        playerBounds = this.player.entityHitBox;
        
        uiScale = getFloat("ui");
        
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
        
        bonus_bullets_t = new Image(bonusesAtlas.findRegion("bonus_bullets"));
        bonus_bullets_t.setBounds(319 - 475 * (uiScale - 1), 475 - 50 * uiScale, 50 * uiScale, 50 * uiScale);
        
        Bonus.width = width;
        Bonus.height = height;
        
        bonuses = new Array<>();
        types = new Array<>();
        explosions = new Array<>();
        anglesY = new Array<>();
        
        font_text = assetManager.get("fonts/pixel.ttf");
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
        types.add(BonusType.values()[type]);
        anglesY.add(getRandomInRange(0, 1000) / 500f - 1);
    }
    
    public void draw(SpriteBatch batch, float delta) {
        
        for (int i = 0; i < bonuses.size; i++) {
            
            Rectangle bonusBounds = bonuses.get(i);
            BonusType type = types.get(i);
            
            float angleY = anglesY.get(i);
            
            switch (type) {
                case CHARGE:
                default:
                    this.bonus_charge.setPosition(bonusBounds.x, bonusBounds.y);
                    this.bonus_charge.draw(batch);
                    break;
                case SHIELD:
                    this.bonus_shield.setPosition(bonusBounds.x, bonusBounds.y);
                    this.bonus_shield.draw(batch);
                    break;
                case HEALTH:
                    this.bonus_health.setPosition(bonusBounds.x, bonusBounds.y);
                    this.bonus_health.draw(batch);
                    break;
                case BULLETS:
                    this.bonus_bullets.setPosition(bonusBounds.x, bonusBounds.y);
                    this.bonus_bullets.draw(batch);
                    break;
                case PART:
                    this.bonus_part.setPosition(bonusBounds.x, bonusBounds.y);
                    this.bonus_part.draw(batch);
                    break;
                case BOSS:
                    this.boss.setPosition(bonusBounds.x, bonusBounds.y);
                    this.boss.draw(batch);
                    break;
            }
            
            bonusBounds.y -= angleY * 15 * delta;
            bonusBounds.x -= 50 * delta;
            
            if (player.magnetField.getBoundingRectangle().overlaps(bonusBounds) && player.charge >= player.bonusPowerConsumption * delta) {
                bonusBounds.x = MathUtils.lerp(bonusBounds.x, playerBounds.getX() + playerBounds.getWidth() / 2, delta / 2);
                bonusBounds.y = MathUtils.lerp(bonusBounds.y, playerBounds.getY() + playerBounds.getHeight() / 2, delta / 2);
                player.charge -= player.bonusPowerConsumption * delta;
            }
            
            if (bonusBounds.y < -height || bonusBounds.y > 480 || bonusBounds.x < -width || bonusBounds.x > 800) {
                removeBonus(i, false);
            } else if (bonusBounds.overlaps(playerBounds)) {
                removeBonus(i, true);
                switch (type) {
                    case CHARGE:
                    default:
                        player.charge = clamp(player.charge + 5, -1000, player.chargeCapacity * player.chargeCapacityMultiplier);
                        break;
                    case SHIELD:
                        player.shieldCharge = clamp(player.shieldCharge + 15, -1000, player.shieldCapacity);
                        break;
                    case HEALTH:
                        player.health = clamp(player.health + 15, -1000, player.healthCapacity * player.healthMultiplier);
                        break;
                    case BULLETS:
                        if (GameLogic.bonuses_collected < 10) {
                            GameLogic.bonuses_collected += 1;
                        } else {
                            addInteger("cogs", 1);
                        }
                        break;
                    case PART:
                        addInteger("cogs", 1);
                        break;
                    case BOSS:
                        bosses.spawnRandomBoss();
                        break;
                }
            }
        }
        if (GameLogic.bonuses_collected > 0) {
            font_text.setColor(Color.WHITE);
            font_text.getData().setScale(0.3f * uiScale);
            font_text.draw(batch, "X" + GameLogic.bonuses_collected, 333 - 463 * (uiScale - 1), 425 - 55 * (uiScale - 1), 24 * uiScale, 1, false);
            bonus_bullets_t.draw(batch, 1);
            font_text.setColor(Color.BLACK);
        }
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).draw(batch, delta);
            if (explosions.get(i3).isComplete()) {
                explosions.get(i3).free();
                explosions.removeIndex(i3);
            }
        }
    }
    
    public void dispose() {
        bonuses.clear();
        types.clear();
        anglesY.clear();
        for (int i3 = 0; i3 < explosions.size; i3++) {
            explosions.get(i3).free();
        }
        explosions.clear();
        font_text.dispose();
    }
    
    private void removeBonus(int i, boolean explode) {
        if (explode) {
            String path;
            switch (types.get(i)) {
                case PART:
                    path = "particles/explosion4.p";
                    break;
                case HEALTH:
                case BOSS:
                    path = "particles/explosion4_1.p";
                    break;
                case CHARGE:
                case BULLETS:
                default:
                    path = "particles/explosion4_2.p";
                    break;
                case SHIELD:
                    path = "particles/explosion4_3.p";
                    break;
            }
            ParticleEffectPool.PooledEffect explosionEffect = particleEffectPoolLoader.getParticleEffectByPath(path);
            explosionEffect.setPosition(bonuses.get(i).x + bonuses.get(i).width / 2, bonuses.get(i).y + bonuses.get(i).height / 2);
            explosions.add(explosionEffect);
        }
        bonuses.removeIndex(i);
        types.removeIndex(i);
        anglesY.removeIndex(i);
    }
}
