package com.deo.flapd.model.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Entity {

    public float x;
    public float y;
    public float width;
    public float height;
    float offsetX = 0;
    float offsetY = 0;
    float originX;
    float originY;
    float rotation = 0;
    public float health;
    public float speed;
    public boolean isDead = false;
    public boolean isExploded = false;
    boolean canAimAt = false;
    Color color;

    public Sprite entitySprite;
    public Rectangle entityHitBox;

    public void init(){
        entitySprite.setSize(width, height);
        entitySprite.setOrigin(originX, originY);
        entitySprite.setPosition(x, y);
        entityHitBox = entitySprite.getBoundingRectangle();
        color = Color.WHITE;
    }

    protected void update(){
        entitySprite.setPosition(x, y);
        entitySprite.setRotation(rotation);
        entitySprite.setColor(color);
    }

    void collideWith(String entityName){

    }

}
