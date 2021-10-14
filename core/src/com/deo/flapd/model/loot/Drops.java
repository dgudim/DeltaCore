package com.deo.flapd.model.loot;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static java.lang.StrictMath.abs;

public class Drops {
    
    String[] availableDrops = {
            "item.coloring_crystal", "item.ore", "item.metal_shard", "item.rubber", "item.plastic", "item.wire", "item.bolt", "item.cog",
            "item.glass_shard", "item.iron_sheet", "item.red_crystal",
            "item.energy_cell", "item.cyan_warp_shard", "item.green_warp_shard", "item.purple_crystal", "item.core_shard"};
    
    private final TextureAtlas itemAtlas;
    private final Array<Float> timers;
    private final Array<Float> degrees;
    private final Array<Sprite> drops;
    private final Array<String> types;
    
    private final float maxSize;
    private final float uiScaling;
    
    public Drops(AssetManager assetManager, float maxSize, float uiScaling) {
        
        this.uiScaling = uiScaling;
        
        itemAtlas = assetManager.get("items/items.atlas");
        
        timers = new Array<>();
        drops = new Array<>();
        types = new Array<>();
        degrees = new Array<>();
        this.maxSize = maxSize;
        
    }
    
    public void drop(Rectangle originEnemy, int count, float timer, int rarity) {
        drop(originEnemy.getX() + originEnemy.width / 2 - maxSize / 2, originEnemy.getY() + originEnemy.height / 2 - maxSize / 2, count, timer, rarity);
    }
    
    public void drop(float x, float y, int count, float timer, int rarity) {
        for (int i = 0; i < count; i++) {
            int type = clamp(getRandomInRange(1, rarity + 10), 0, availableDrops.length);
            types.add(availableDrops[type]);
            Sprite drop = new Sprite(itemAtlas.findRegion(availableDrops[type]));
            drop.setPosition(x, y);
            drop.setScale(maxSize / Math.max(drop.getHeight(), drop.getWidth()));
            drops.add(drop);
            
            timers.add(timer * (getRandomInRange(0, 1000) / 1000f + 1));
            degrees.add(getRandomInRange(0, 1000) * 0.36f);
        }
    }
    
    public void draw(SpriteBatch batch, float delta) {
        
        for (int i = 0; i < drops.size; i++) {
            Sprite drop = drops.get(i);
            
            float degree = degrees.get(i);
            float timer = timers.get(i);
            boolean dropRemoved = false;
            
            drop.draw(batch);
            
            if (timer > 0) {
                drop.setX(drop.getX() -  MathUtils.cosDeg(degree) * timer * 30 * delta);
                drop.setY(drop.getY() -  MathUtils.sinDeg(degree) * timer * 30 * delta);
            } else {
                Vector2 pos1 = new Vector2();
                pos1.set(drop.getX(), drop.getY());
                Vector2 pos2 = new Vector2();
                pos2.set(390 - drop.getWidth() / 2 - 400 * (uiScaling - 1), 455 - drop.getHeight() / 2 - 20 * (uiScaling - 1));
                pos1.lerp(pos2, 4.5f * delta);
                
                drop.setPosition(pos1.x, pos1.y);
    
                if (abs(pos1.x - pos2.x) < 20 && abs(pos1.y - pos2.y) < 20) {
                    removeDrop(i);
                    dropRemoved = true;
                }
            }
            if(!dropRemoved){
                timer -= delta;
                timers.set(i, timer);
            }
        }
    }
    
    private void removeDrop(int i) {
        addInteger("item_" + types.get(i), 1);
        timers.removeIndex(i);
        drops.removeIndex(i);
        degrees.removeIndex(i);
        types.removeIndex(i);
    }
    
    public void dispose() {
        timers.clear();
        drops.clear();
        degrees.clear();
        types.clear();
    }
}
