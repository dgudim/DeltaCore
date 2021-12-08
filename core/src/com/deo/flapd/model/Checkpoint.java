package com.deo.flapd.model;

import static com.deo.flapd.control.GameVariables.bossWave;
import static com.deo.flapd.control.GameVariables.score;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.lerpWithConstantSpeed;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putInteger;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deo.flapd.control.GameVariables;
import com.deo.flapd.model.enemies.Bosses;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.Keys;
import com.deo.flapd.utils.particles.ParticleEffectPoolLoader;

public class Checkpoint extends Entity {
    
    private final ParticleEffectPoolLoader particleEffectPool;
    
    private final AssetManager assetManager;
    
    private boolean checkpointState, effects;
    private final Player player;
    private ParticleEffectPool.PooledEffect fire;
    private ParticleEffectPool.PooledEffect fire2;
    private float destination_posX;
    private float destination_posY;
    
    private int lastCheckpoint;
    private final int checkpointSpawnSpacing = 5000;
    
    public Checkpoint(CompositeManager compositeManager, Player player, boolean newGame) {
        assetManager = compositeManager.getAssetManager();
        particleEffectPool = compositeManager.getParticleEffectPool();
        
        entitySprite = new Sprite((Texture) assetManager.get("checkpoint.png"));
        setSize(100, 100);
        setPositionAndRotation(1000, 1000, 0);
        init();
        
        destination_posX = 1000;
        destination_posY = 1000;
        speed = 100;
        this.player = player;
        
        if (!newGame) {
            lastCheckpoint = getInteger(Keys.lastCheckpointScore);
        } else {
            lastCheckpoint = 0;
        }
    }
    
    public void update(float delta) {
        updateEntity(delta);
        if (!bossWave) {
            if (score > lastCheckpoint + checkpointSpawnSpacing) {
                lastCheckpoint = score;
                spawn(getRandomInRange(0, 300) + 150, getRandomInRange(0, 201) + 100);
            }
        }
        x = lerpWithConstantSpeed(x, destination_posX, speed, delta);
        y = lerpWithConstantSpeed(y, destination_posY, speed, delta);
    }
    
    public void spawn(float destination_posX, float destination_posY) {
        this.destination_posX = destination_posX;
        this.destination_posY = destination_posY;
        
        entitySprite.setRegion((Texture) assetManager.get("checkpoint.png"));
        
        x = 950;
        y = getRandomInRange(0, 201) + 100;
        
        fire = particleEffectPool.getParticleEffectByPath("particles/fire_down.p");
        fire2 = particleEffectPool.getParticleEffectByPath("particles/fire_down.p");
        
        effects = true;
        
        checkpointState = false;
    }
    
    public void drawEffects(SpriteBatch batch, float delta) {
        if (effects) {
            fire.setPosition(x + 18, y + 14);
            fire2.setPosition(x + 82, y + 14);
            
            fire.draw(batch, delta);
            fire2.draw(batch, delta);
        }
    }
    
    public void drawBase(SpriteBatch batch) {
        
        entitySprite.draw(batch);
        
        if (player.overlaps(this) && player.health > 0 && !checkpointState) {
            checkpointState = true;
            entitySprite.setRegion((Texture) assetManager.get("checkpoint_green.png"));
            destination_posY = 900;
            putInteger(Keys.enemiesKilled, GameVariables.enemiesKilled);
            putInteger(Keys.moneyEarned, GameVariables.moneyEarned);
            putInteger(Keys.moneyAmount, GameVariables.money);
            putInteger(Keys.playerScore, score);
            putFloat(Keys.playerHealthValue, player.health);
            putFloat(Keys.playerShieldValue, player.shieldCharge);
            putFloat(Keys.playerChargeValue, player.charge);
            for (int i = 0; i < Bosses.bosses.size; i++) {
                putBoolean("boss_spawned_" + Bosses.bossNames[i], Bosses.bosses.get(i).hasAlreadySpawned);
            }
            putInteger(Keys.bonusesCollected, GameVariables.bonuses_collected);
            putInteger(Keys.lastCheckpointScore, lastCheckpoint);
            putInteger(Keys.bulletsShot, player.bulletsShot);
            putFloat(Keys.lastPLayerX, player.x);
            putFloat(Keys.lastPLayerY, player.y);
        }
        
        if (y > 850 && checkpointState) {
            fire.free();
            fire2.free();
            checkpointState = false;
            entitySprite.setRegion((Texture) assetManager.get("checkpoint.png"));
            effects = false;
        }
        
    }
    
}
