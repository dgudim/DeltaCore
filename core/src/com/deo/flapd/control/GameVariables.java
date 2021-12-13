package com.deo.flapd.control;

import static com.deo.flapd.utils.DUtils.getInteger;

import com.deo.flapd.utils.Keys;


public class GameVariables {
    public static int bonuses_collected;
    
    public static boolean bossWave;
    
    public static int score;
    public static int enemiesKilled;
    public static int money, moneyEarned, bulletsShot;
    
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
