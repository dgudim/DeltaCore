package com.deo.flapd.model.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Entity {
    
    public float x = 0;
    public float y = 0;
    public float width;
    public float height;
    float offsetX = 0;
    float offsetY = 0;
    public float originX;
    public float originY;
    public float rotation = 0;
    public float health;
    public float speed;
    public boolean isDead = false;
    Color color;
    
    public Sprite entitySprite;
    Rectangle entityHitBox;
    
    public void init() {
        entitySprite.setSize(width, height);
        entitySprite.setOrigin(originX, originY);
        entitySprite.setPosition(x, y);
        entitySprite.setRotation(rotation);
        entityHitBox = entitySprite.getBoundingRectangle();
        color = Color.WHITE;
    }
    
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        originX = width / 2f;
        originY = height / 2f;
    }
    
    protected void update() {
        entitySprite.setPosition(x, y);
        entitySprite.setRotation(rotation);
        entitySprite.setColor(color);
        if (health > 0) {
            entityHitBox.setPosition(entitySprite.getX(), entitySprite.getY());
        } else {
            entityHitBox.setPosition(-1000, -1000).setSize(0, 0);
        }
    }
    
    public boolean overlaps(Entity entity) {
        return entity.entityHitBox.overlaps(entityHitBox) && !entity.isDead && !isDead;
    }
    
    public boolean overlaps(Rectangle hitBox) {
        return hitBox.overlaps(entityHitBox) && !isDead;
    }
    
}
