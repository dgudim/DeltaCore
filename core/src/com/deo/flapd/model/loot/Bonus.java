package com.deo.flapd.model.loot;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.Entity;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.Keys;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Bonus extends Entity {
    
    float angle;
    Bonuses.BonusType bonusType;
    Player player;
    Rectangle playerBounds;
    Bonuses bonuses;
    
    public Bonus(float maxSize, TextureAtlas bonusesAtlas, Player player, Bonuses bonuses, int type, Rectangle spawnAt) {
        this.player = player;
        this.bonuses = bonuses;
        playerBounds = player.entityHitBox;
        
        bonusType = Bonuses.BonusType.values()[type];
        entitySprite = new Sprite(bonusesAtlas.findRegion(("bonus_" + bonusType).toLowerCase()));
        setSize(maxSize, maxSize);
        setPositionAndRotation(spawnAt.getX() + spawnAt.width / 2f - width / 2, spawnAt.getY() + spawnAt.height / 2f - height / 2, 0);
        init();
        angle = getRandomInRange(0, 1000) / 500f - 1;
    }
    
    public void draw(SpriteBatch batch){
        entitySprite.draw(batch);
    }
    
    public void update(float delta){
        y -= angle * 15 * delta;
        x -= 50 * delta;
    
        if (player.magnetField.getBoundingRectangle().overlaps(entityHitBox) && player.charge >= player.bonusPowerConsumption * delta) {
            x = MathUtils.lerp(x, playerBounds.getX() + playerBounds.getWidth() / 2, delta / 2);
            y = MathUtils.lerp(y, playerBounds.getY() + playerBounds.getHeight() / 2, delta / 2);
            player.charge -= player.bonusPowerConsumption * delta;
        }
    
        updateEntity(delta);
        
        if (y < -height || y > 480 || x < -width || x > 800) {
            isDead = true;
        } else if (entityHitBox.overlaps(playerBounds)) {
            isDead = true;
            switch (bonusType) {
                case ENERGY:
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
                        addInteger(Keys.cogAmount, 1);
                    }
                    break;
                case PART:
                    addInteger(Keys.cogAmount, 1);
                    break;
            }
        }
    }
}
