package com.deo.flapd.model.loot;

import static com.deo.flapd.utils.DUtils.addInteger;
import static com.deo.flapd.utils.DUtils.getFloat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameVariables;
import com.deo.flapd.model.Player;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.Keys;

public class Drops {
    
    String[] availableDrops = {
            "item.coloring_crystal", "item.ore", "item.metal_shard", "item.rubber", "item.plastic", "item.wire", "item.bolt", "item.cog",
            "item.glass_shard", "item.metal_sheet", "item.red_crystal",
            "item.energy_cell",  "item.green_warp_shard", "item.cyan_warp_shard", "item.purple_crystal", "item.core_shard"};
    
    private final TextureAtlas itemAtlas;
    private final Array<Drop> drops;
    
    private final float maxSize;
    private final float uiScale;
    
    private final Bonuses bonuses;
    
    public Drops(CompositeManager compositeManager, float maxSize, Player player) {
        uiScale = getFloat(Keys.uiScale);
        itemAtlas = compositeManager.getAssetManager().get("items/items.atlas");
        drops = new Array<>();
        this.maxSize = maxSize;
        bonuses = new Bonuses(compositeManager, maxSize, player);
    }
    
    public void dropBonus(int type, Rectangle enemy) {
        bonuses.drop(type, enemy);
    }
    
    public void drop(Rectangle originEnemy, int count, float timer, int rarity) {
        drop(originEnemy.getX() + originEnemy.width / 2 - maxSize / 2, originEnemy.getY() + originEnemy.height / 2 - maxSize / 2, count, timer, rarity);
    }
    
    public void dropMoney(Rectangle originEnemy, int count, float timer) {
        dropMoney(originEnemy.getX() + originEnemy.width / 2 - 19, originEnemy.getY() + originEnemy.height / 2 - 22, count, timer);
    }
    
    public void drop(float x, float y, int count, float timer, int rarity) {
        for (int i = 0; i < count; i++) {
            drops.add(new Drop(x, y, timer, rarity, false, this, itemAtlas, maxSize, uiScale));
        }
    }
    
    public void dropMoney(float x, float y, int count, float timer) {
        for (int i = 0; i < count; i++) {
            drops.add(new Drop(x, y, timer, 0, true, this, itemAtlas, maxSize, uiScale));
        }
    }
    
    public void draw(SpriteBatch batch, float delta) {
        for (int i = 0; i < drops.size; i++) {
            drops.get(i).update(delta);
            drops.get(i).draw(batch);
            if (drops.get(i).isDead) {
                removeDrop(i);
            }
        }
        bonuses.draw(batch, delta);
    }
    
    public void drawDebug(ShapeRenderer shapeRenderer){
        bonuses.drawDebug(shapeRenderer);
    }
    
    private void removeDrop(int i) {
        if (drops.get(i).isMoneyDrop) {
            GameVariables.money += drops.get(i).pack_level;
            GameVariables.moneyEarned += drops.get(i).pack_level;
        } else {
            addInteger("item_" + drops.get(i).type, 1);
        }
        drops.removeIndex(i);
    }
    
    public void dispose() {
        drops.clear();
        bonuses.dispose();
    }
}
