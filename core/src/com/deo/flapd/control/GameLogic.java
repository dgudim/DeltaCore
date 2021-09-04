package com.deo.flapd.control;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Checkpoint;
import com.deo.flapd.model.Meteorites;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.JsonEntry;

import java.util.Random;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getRandomBoolean;
import static com.deo.flapd.utils.DUtils.getString;


public class GameLogic {
    
    private final Player player;
    
    private final Random random;
    
    private final float difficulty;
    
    public static int bonuses_collected;
    
    public static boolean bossWave, has1stBossSpawned, has2ndBossSpawned;
    
    public static int lastCheckpoint;
    
    public static int score;
    public static int enemiesKilled;
    public static int money, moneyEarned;
    
    private float speedMultiplier;
    
    private final Game game;
    
    private final Bullet playerBullet;
    private final Meteorites meteorites;
    private final Checkpoint checkpoint;
    
    public GameLogic(Player player, boolean newGame, Game game, Meteorites meteorites, Checkpoint checkpoint) {
        this.player = player;
        random = new Random();
        
        this.game = game;
        
        playerBullet = this.player.bullet;
        this.meteorites = meteorites;
        this.checkpoint = checkpoint;
        
        difficulty = getFloat("difficulty");
        // TODO: 5/6/2021 implement difficulty
        
        JsonEntry treeJson = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/tree.json")));
        
        speedMultiplier = treeJson.getFloatArray(new float[]{}, getString("currentEngine"), "parameterValues")[0];
        
        float[] shipParamValues = treeJson.getFloatArray(new float[]{}, getString("currentArmour"), "parameterValues");
        String[] shipParams = treeJson.getStringArray(new String[]{}, getString("currentArmour"), "parameters");
        
        float[] coreParamValues = treeJson.getFloatArray(new float[]{}, getString("currentCore"), "parameterValues");
        String[] coreParams = treeJson.getStringArray(new String[]{}, getString("currentCore"), "parameters");
        
        for (int i = 0; i < shipParamValues.length; i++) {
            if (shipParams[i].endsWith("speed multiplier")) {
                speedMultiplier *= shipParamValues[i];
            }
        }
        
        for (int i = 0; i < coreParams.length; i++) {
            if (coreParams[i].endsWith("speed multiplier")) {
                speedMultiplier *= coreParamValues[i];
            }
        }
        
        if (!newGame) {
            bonuses_collected = getInteger("bonuses_collected");
            lastCheckpoint = getInteger("lastCheckpoint");
            has1stBossSpawned = getBoolean("has1stBossSpawned");
            has2ndBossSpawned = getBoolean("has2ndBossSpawned");
            
            score = getInteger("Score");
            moneyEarned = getInteger("moneyEarned");
            
            enemiesKilled = getInteger("enemiesKilled");
        } else {
            bonuses_collected = 0;
            lastCheckpoint = 0;
            has1stBossSpawned = false;
            has2ndBossSpawned = false;
            
            score = 0;
            moneyEarned = 0;
            
            enemiesKilled = 0;
        }
        money = getInteger("money");
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
            playerBullet.Spawn(1, false);
        }
        
        if (is_firing_secondary) {
            playerBullet.Spawn(1.5f, true);
        }
        
        if (playerBullet.isLaser) {
            playerBullet.updateLaser(is_firing || is_firing_secondary);
        }
        
        if (getRandomBoolean(0.05f) && delta > 0) {
            meteorites.Spawn(random.nextInt(480), (random.nextInt(60) - 30) / 10f, random.nextInt(10) + 5);
        }
        
        if (!bossWave) {
            if (score > lastCheckpoint + 9000) {
                lastCheckpoint = score;
                checkpoint.Spawn(random.nextInt(300) + 150, random.nextInt(201) + 100, 1);
            }
        }
    }
}
