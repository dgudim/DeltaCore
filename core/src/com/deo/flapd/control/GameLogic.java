package com.deo.flapd.control;

import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.bullets.PlayerBullet;
import com.deo.flapd.utils.Keys;


public class GameLogic {
    
    private final Player player;
    
    public static int bonuses_collected;
    
    public static boolean bossWave;
    
    public static int score;
    public static int enemiesKilled;
    public static int money, moneyEarned;
    
    private final Game game;
    
    private final PlayerBullet playerBullet;
    
    public GameLogic(Player player, boolean newGame, Game game, Checkpoint checkpoint) {
        this.player = player;
        
        this.game = game;
        
        playerBullet = this.player.bullet;
        
        //difficulty = getFloat("difficulty");
        // TODO: 5/6/2021 implement difficulty
        
        if (!newGame) {
            bonuses_collected = getInteger(Keys.bonusesCollected);
            
            score = getInteger(Keys.playerScore);
            moneyEarned = getInteger(Keys.moneyEarned);
            
            enemiesKilled = getInteger(Keys.enemiesKilled);
        } else {
            bonuses_collected = 0;
            
            score = 0;
            moneyEarned = 0;
            
            enemiesKilled = 0;
        }
        money = getInteger(Keys.moneyAmount);
        bossWave = false;
    }
    
    public void handleInput(float delta, float deltaX, float deltaY, boolean is_firing, boolean is_firing_secondary) {
        if (Gdx.input.isKeyPressed(Input.Keys.W))
            deltaY = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            deltaX = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            deltaY = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            deltaX = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            is_firing = true;
        if (Gdx.input.isKeyPressed(Input.Keys.M))
            is_firing_secondary = true;
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
            game.pause();
    
        player.accelerate(deltaX, deltaY, delta);
        
        if (is_firing) {
            playerBullet.spawn(1, false);
        }
        
        if (is_firing_secondary) {
            playerBullet.spawn(1.5f, true);
        }
        
        if (playerBullet.isLaser) {
            playerBullet.updateLaser(is_firing || is_firing_secondary);
        }
    }
}
