package com.deo.flapd.model.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.model.Player;

import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class Bosses {
    
    private final AssetManager assetManager;
    public static Array<Boss> bosses;
    public static final String[] bossNames = new String[]{"boss_ship", "boss_evil", "boss_star_destroyer", "boss_ufo", "boss_station"};
    
    public Bosses(AssetManager assetManager) {
        this.assetManager = assetManager;
        bosses = new Array<>();
    }
    
    public void loadBosses() {
        for (String bossName : bossNames) {
            bosses.add(new Boss(bossName, assetManager));
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
        for (int i = 0; i < bosses.size; i++) {
            bosses.get(i).dispose();
        }
    }
}
