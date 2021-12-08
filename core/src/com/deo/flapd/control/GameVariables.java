package com.deo.flapd.control;

import static com.deo.flapd.utils.DUtils.getInteger;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Player;
import com.deo.flapd.model.bullets.PlayerBullet;
import com.deo.flapd.utils.Keys;


public class GameVariables {
    public static int bonuses_collected;
    
    public static boolean bossWave;
    
    public static int score;
    public static int enemiesKilled;
    public static int money, moneyEarned;
    
    public static void init(boolean newGame) {
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
}
