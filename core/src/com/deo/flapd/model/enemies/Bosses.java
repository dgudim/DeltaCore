package com.deo.flapd.model.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.MusicManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Bosses {
    
    public static Array<Boss> bosses;
    public static final String[] bossNames = new String[]{"boss_ship", "boss_evil", "boss_star_destroyer", "boss_ufo", "boss_station", "boss_ultimate_destroyer"};
    static final ExecutorService secondThread = Executors.newFixedThreadPool(10);
    static boolean stopThread = false;
    
    public Bosses() {
        bosses = new Array<>();
    }
    
    public void loadBosses(AssetManager assetManager, MusicManager musicManager) {
        for (String bossName : bossNames) {
            bosses.add(new Boss(bossName, assetManager, musicManager));
        }
    }
    
    public void setTargetPlayer(Player player) {
        for (int i = 0; i < bosses.size; i++) {
            bosses.get(i).setTargetPlayer(player);
        }
    }
    
    public void update(float delta) {
        for (int i = 0; i < bosses.size; i++) {
            bosses.get(i).update(delta);
        }
    }
    
    public void draw(SpriteBatch batch, float delta) {
        for (int i = 0; i < bosses.size; i++) {
            bosses.get(i).draw(batch, delta);
        }
    }
    
    public void drawDebug(ShapeRenderer shapeRenderer) {
        for (int i = 0; i < bosses.size; i++) {
            bosses.get(i).drawDebug(shapeRenderer);
        }
    }
    
    public void spawnRandomBoss() {
        bosses.get(getRandomInRange(0, bosses.size - 1)).spawn();
    }
    
    public void dispose() {
        stopThread = true;
        for (int i = 0; i < bosses.size; i++) {
            bosses.get(i).dispose();
        }
    }
}
