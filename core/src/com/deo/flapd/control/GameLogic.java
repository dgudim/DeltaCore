package com.deo.flapd.control;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.bullets.PlayerBullet;
import com.deo.flapd.model.environment.EnvironmentalEffects;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.Keys;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getRandomBoolean;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getString;


public class GameLogic {
    
    private final Player player;
    
    public static int bonuses_collected;
    
    public static boolean bossWave;
    
    public static int lastCheckpoint;
    
    public static int score;
    public static int enemiesKilled;
    public static int money, moneyEarned;
    
    private final float speedMultiplier;
    
    private final Game game;
    
    private final PlayerBullet playerBullet;
    private final EnvironmentalEffects environmentalEffects;
    private final Checkpoint checkpoint;
    
    public GameLogic(Player player, boolean newGame, Game game, EnvironmentalEffects environmentalEffects, Checkpoint checkpoint) {
        this.player = player;
        
        this.game = game;
        
        playerBullet = this.player.bullet;
        this.environmentalEffects = environmentalEffects;
        this.checkpoint = checkpoint;
        
        //difficulty = getFloat("difficulty");
        // TODO: 5/6/2021 implement difficulty
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        
        speedMultiplier = treeJson.getFloat(false, 1, getString(Keys.currentEngine), "parameters", "parameter.speed_multiplier")
                * treeJson.getFloat(false, 1, getString(Keys.currentShip), "parameters", "parameter.speed_multiplier")
                * treeJson.getFloat(false, 1, getString(Keys.currentCore), "parameters", "parameter.speed_multiplier");
        
        if (!newGame) {
            bonuses_collected = getInteger(Keys.bonusesCollected);
            lastCheckpoint = getInteger(Keys.lastCheckpointScore);
            
            score = getInteger(Keys.playerScore);
            moneyEarned = getInteger(Keys.moneyEarned);
            
            enemiesKilled = getInteger(Keys.enemiesKilled);
        } else {
            bonuses_collected = 0;
            lastCheckpoint = 0;
            
            score = 0;
            moneyEarned = 0;
            
            enemiesKilled = 0;
        }
        money = getInteger(Keys.moneyAmount);
        bossWave = false;
    }
    
    public void handleInput(float delta, float deltaX, float deltaY, boolean is_firing, boolean is_firing_secondary) {
        deltaX *= speedMultiplier;
        deltaY *= speedMultiplier;
        
        if (Gdx.input.isKeyPressed(Input.Keys.W))
            deltaY = speedMultiplier;
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            deltaX = -speedMultiplier;
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            deltaY = -speedMultiplier;
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            deltaX = speedMultiplier;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            is_firing = true;
        if (Gdx.input.isKeyPressed(Input.Keys.M))
            is_firing_secondary = true;
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
            game.pause();
        
        if (delta > 0 && player.health > 0) {
            player.setPositionAndRotation(player.x + 250 * deltaX * delta, player.y + 250 * deltaY * delta, clamp((deltaY - deltaX) * 7, -9, 9));
        }
        
        player.x = clamp(player.x, 0, 800 - player.width);
        player.y = clamp(player.y, 0, 480 - player.height);
        
        if (is_firing) {
            playerBullet.spawn(1, false);
        }
        
        if (is_firing_secondary) {
            playerBullet.spawn(1.5f, true);
        }
        
        if (playerBullet.isLaser) {
            playerBullet.updateLaser(is_firing || is_firing_secondary);
        }
        
        if (getRandomBoolean(0.05f) && delta > 0) {
            environmentalEffects.spawnMeteorite(getRandomInRange(0, 480), (getRandomInRange(0, 60) - 30) / 10f, getRandomInRange(0, 10) + 5);
        }
        
        if (getRandomBoolean(0.005f) && delta > 0) {
            environmentalEffects.spawnFallingShip(getRandomInRange(0, 480));
        }
        
        if (!bossWave) {
            if (score > lastCheckpoint + 9000) {
                lastCheckpoint = score;
                checkpoint.spawn(getRandomInRange(0, 300) + 150, getRandomInRange(0, 201) + 100, 1);
            }
        }
    }
}
