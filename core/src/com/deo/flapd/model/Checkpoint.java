package com.deo.flapd.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.model.enemies.Bosses;
import com.deo.flapd.utils.Keys;

import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putInteger;
import static com.deo.flapd.view.screens.LoadingScreen.particleEffectPoolLoader;

public class Checkpoint {
    
    private final Sprite checkpoint_blue;
    private final Sprite checkpoint_green;
    private boolean checkpointState, effects;
    private final Polygon bounds;
    private final Rectangle shipBounds;
    private final Player player;
    private ParticleEffectPool.PooledEffect fire;
    private ParticleEffectPool.PooledEffect fire2;
    private float speed;
    private float destination_posX;
    private float destination_posY;
    
    public Checkpoint(AssetManager assetManager, Player player) {
        checkpoint_blue = new Sprite((Texture) assetManager.get("checkpoint.png"));
        checkpoint_green = new Sprite((Texture) assetManager.get("checkpoint_green.png"));
        
        bounds = new Polygon(new float[]{0f, 0f, 102, 0f, 102, 102, 0f, 102});
        
        this.player = player;
        
        shipBounds = this.player.entityHitBox;
        
        bounds.setPosition(-200, -200);
        
        checkpoint_blue.setSize(0, 0);
        checkpoint_green.setSize(0, 0);
        checkpoint_blue.setPosition(1000, 1000);
        checkpoint_green.setPosition(1000, 1000);
    }
    
    public void spawn(float destination_posX, float destination_posY, float speed) {
        this.destination_posX = destination_posX;
        this.destination_posY = destination_posY;
        this.speed = speed;
        
        bounds.setPosition(950, getRandomInRange(0, 201) + 100);
        
        fire = particleEffectPoolLoader.getParticleEffectByPath("particles/fire_down.p");
        fire2 = particleEffectPoolLoader.getParticleEffectByPath("particles/fire_down.p");
        
        effects = true;
        
        checkpointState = false;
    }
    
    public void drawEffects(SpriteBatch batch, float delta) {
        if (effects) {
            fire.setPosition(bounds.getX() + 18, bounds.getY() + 14);
            fire2.setPosition(bounds.getX() + 84, bounds.getY() + 14);
            
            fire.draw(batch, delta);
            fire2.draw(batch, delta);
        }
    }
    
    public void drawBase(SpriteBatch batch) {
        
        if (destination_posX < bounds.getX()) {
            bounds.setPosition(bounds.getX() - speed, bounds.getY());
        }
        
        if (destination_posY < bounds.getY()) {
            bounds.setPosition(bounds.getX(), bounds.getY() - speed);
        }
        
        if (destination_posY > bounds.getY()) {
            bounds.setPosition(bounds.getX(), bounds.getY() + speed);
        }
        
        if (checkpointState) {
            checkpoint_green.setPosition(bounds.getX(), bounds.getY());
            checkpoint_green.setOrigin(bounds.getBoundingRectangle().getWidth() / 2, bounds.getBoundingRectangle().getHeight() / 2);
            checkpoint_green.setSize(bounds.getBoundingRectangle().getWidth(), bounds.getBoundingRectangle().getHeight());
            checkpoint_green.draw(batch);
        } else {
            checkpoint_blue.setPosition(bounds.getX(), bounds.getY());
            checkpoint_blue.setOrigin(bounds.getBoundingRectangle().getWidth() / 2, bounds.getBoundingRectangle().getHeight() / 2);
            checkpoint_blue.setSize(bounds.getBoundingRectangle().getWidth(), bounds.getBoundingRectangle().getHeight());
            checkpoint_blue.draw(batch);
        }
        
        if (shipBounds.overlaps(bounds.getBoundingRectangle()) && player.health > 0 && !checkpointState) {
            checkpointState = true;
            destination_posY = 900;
            destination_posX = bounds.getX();
            speed = 5;
            putInteger(Keys.enemiesKilled, GameLogic.enemiesKilled);
            putInteger(Keys.moneyEarned, GameLogic.moneyEarned);
            putInteger(Keys.moneyAmount, GameLogic.money);
            putInteger(Keys.playerScore, GameLogic.score);
            putFloat(Keys.playerHealthValue, player.health);
            putFloat(Keys.playerShieldValue, player.shieldCharge);
            putFloat(Keys.playerChargeValue, player.charge);
            for (int i = 0; i < Bosses.bosses.size; i++) {
                putBoolean("boss_spawned_" + Bosses.bossNames[i], Bosses.bosses.get(i).hasAlreadySpawned);
            }
            putInteger(Keys.bonusesCollected, GameLogic.bonuses_collected);
            putInteger(Keys.lastCheckpointScore, GameLogic.lastCheckpoint);
            putInteger(Keys.bulletsShot, player.bulletsShot);
            putFloat(Keys.lastPLayerX, shipBounds.getX());
            putFloat(Keys.lastPLayerY, shipBounds.getY());
        }
        
        if (bounds.getY() > 850 && checkpointState) {
            fire.free();
            fire2.free();
            checkpointState = false;
            effects = false;
        }
        
    }
    
}
