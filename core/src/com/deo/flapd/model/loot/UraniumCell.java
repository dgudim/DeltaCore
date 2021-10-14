package com.deo.flapd.model.loot;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class UraniumCell {
    
    private final Sprite cell;
    private static Array<Float> timers;
    private static Array<Vector2> cells;
    private static Array<Float> degrees;
    private static Array<Integer> pack_levels;
    private final float uiScaling;
    
    public UraniumCell(AssetManager assetManager) {
        
        cell = new Sprite((Texture) assetManager.get("uraniumCell.png"));
        
        cells = new Array<>();
        timers = new Array<>();
        degrees = new Array<>();
        pack_levels = new Array<>();
        
        uiScaling = getFloat("ui");
    }
    
    public static void Spawn(Rectangle originEnemy, int count, float timer) {
        Spawn(originEnemy.getX() + originEnemy.width / 2 - 19, originEnemy.getY() + originEnemy.height / 2 - 22, count, timer);
    }
    
    public static void Spawn(float x, float y, int count, float timer) {
        for (int i = 0; i < count; i++) {
            cells.add(new Vector2(x, y));
            timers.add(timer * (getRandomInRange(1, 2) / 1.5f));
            degrees.add(getRandomInRange(0, 1000) * 0.36f);
            pack_levels.add(clamp((getRandomInRange(0, 3) + 1), 1, 4));
        }
    }
    
    public void draw(SpriteBatch batch, float delta) {
        for (int i = 0; i < cells.size; i++) {
            Vector2 cell = cells.get(i);
            float degree = degrees.get(i);
            float timer = timers.get(i);
            float pack_level = pack_levels.get(i);
            
            this.cell.setPosition(cell.x, cell.y);
            
            this.cell.setColor(1 - (pack_level - 1) / 4, 1 - (pack_level - 1) / 4, 1, 1);
            this.cell.draw(batch);
            
            if (timer > 0) {
                cell.x -= MathUtils.cosDeg(degree) * timer * 30 * delta;
                cell.y -= MathUtils.sinDeg(degree) * timer * 30 * delta;
            } else {
                Vector2 pos1 = new Vector2();
                pos1.set(cell.x, cell.y);
                Vector2 pos2 = new Vector2();
                pos2.set(376 - 400 * (uiScaling - 1), 435 - 20 * (uiScaling - 1));
                pos1.lerp(pos2, 4.5f * delta);
                
                cell.x = pos1.x;
                cell.y = pos1.y;
            }
            timer = timer - delta;
            timers.set(i, timer);
            
            if (cell.y > 435 - 20 * (uiScaling - 1) - 2 * uiScaling && cell.x > 376 - 400 * (uiScaling - 1) - 10 * uiScaling && cell.x < 376 - 400 * (uiScaling - 1) + 10 * uiScaling) {
                removeCell(i);
            }
            
        }
    }
    
    private void removeCell(int i) {
        timers.removeIndex(i);
        cells.removeIndex(i);
        degrees.removeIndex(i);
        GameLogic.money += pack_levels.get(i);
        GameLogic.moneyEarned += pack_levels.get(i);
        pack_levels.removeIndex(i);
    }
    
    public void dispose() {
        timers.clear();
        cells.clear();
        degrees.clear();
        pack_levels.clear();
    }
    
}
