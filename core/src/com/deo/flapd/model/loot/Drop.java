package com.deo.flapd.model.loot;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static java.lang.StrictMath.abs;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.deo.flapd.model.Entity;

public class Drop extends Entity {
    
    private float timer;
    private final float angle;
    private final float uiScale;
    
    float pack_level;
    boolean isMoneyDrop;
    
    String type;
    
    public Drop(float x, float y, float timer, int rarity, boolean isMoneyDrop, Drops drops, TextureAtlas itemAtlas, float maxSize, float uiScale){
        this.uiScale = uiScale;
        
        this.isMoneyDrop = isMoneyDrop;
        
        if(isMoneyDrop){
            type = "uraniumCell";
            pack_level = clamp((getRandomInRange(0, 3) + 1), 1, 4);
        }else{
            type = drops.availableDrops[clamp(getRandomInRange(0, rarity + 10), 0, drops.availableDrops.length - 1)];
        }
        
        entitySprite = new Sprite(itemAtlas.findRegion(type));
        setPositionAndRotation(x, y, 0);
        width = entitySprite.getWidth();
        height = entitySprite.getHeight();
        scaleBy(maxSize / Math.max(height, width));
        init();
        
        this.timer = timer * (getRandomInRange(0, 1000) / 1000f + 1);
        angle = getRandomInRange(0, 1000) * 0.36f;
    }
    
    public void update(float delta){
        
        if (timer > 0) {
            setPositionAndRotation(x - MathUtils.cosDeg(angle) * timer * 30 * delta, y - MathUtils.sinDeg(angle) * timer * 30 * delta, 0);
        } else {
            Vector2 pos1 = new Vector2();
            pos1.set(x, y);
            Vector2 pos2 = new Vector2();
            pos2.set(390 - width / 2 - 400 * (uiScale - 1), 455 - height / 2 - 20 * (uiScale - 1));
            pos1.lerp(pos2, 4.5f * delta);
    
            setPositionAndRotation(pos1.x, pos1.y, 0);
        
            if (abs(pos1.x - pos2.x) < 10 && abs(pos1.y - pos2.y) < 10) {
                isDead = true;
            }
        }
        if(isMoneyDrop){
            color.set(1 - (pack_level - 1) / 4, 1 - (pack_level - 1) / 4, 1, 1);
        }
        updateEntity(delta);
        if(!isDead){
            timer -= delta;
        }
    }
    
    public void draw(SpriteBatch batch){
        if(!isDead) {
            entitySprite.draw(batch);
        }
    }
    
}
