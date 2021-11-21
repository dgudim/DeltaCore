package com.deo.flapd.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import static com.badlogic.gdx.math.MathUtils.clamp;

public class Entity {
    
    public float x = 0;
    public float y = 0;
    public float width;
    public float height;
    public float offsetX = 0;
    public float offsetY = 0;
    public float originX;
    public float originY;
    public float rotation = 0;
    public float health;
    public float maxHealth;
    public float regeneration = 0;
    public float speed;
    public boolean isDead = false;
    public boolean active = true;
    public Color color = new Color(Color.WHITE);
    
    public Sprite entitySprite;
    public boolean hasAnimation;
    public Animation<TextureRegion> entityAnimation;
    public float animationPosition;
    public Rectangle entityHitBox;
    
    public void init() {
        entitySprite.setOrigin(originX, originY);
        entitySprite.setRotation(rotation);
        entitySprite.setBounds(x, y, width, height);
        entityHitBox = entitySprite.getBoundingRectangle();
    }
    
    public void setPositionAndRotation(float x, float y, float rotation) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
    
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        originX = width / 2f;
        originY = height / 2f;
    }
    
    public void scaleBy(float scale) {
        setSize(width * scale, height * scale);
    }
    
    protected void updateEntity(float delta) {
        entitySprite.setPosition(x, y);
        entitySprite.setRotation(rotation);
        entitySprite.setColor(color);
        updateHealth(delta);
    }
    
    protected void updateHealth(float delta) {
        if (health > 0) {
            entityHitBox.setPosition(x, y);
            if (regeneration > 0 && maxHealth > 0) {
                health = clamp(health + regeneration * delta, 0, maxHealth);
            }
        } else {
            entityHitBox.setPosition(-1000, -1000).setSize(0, 0);
        }
    }
    
    public void drawDebug(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(entityHitBox.x, entityHitBox.y, entityHitBox.width, entityHitBox.height);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(x + originX, y + originY, 5);
    }
    
    public boolean overlaps(Entity entity) {
        return entity.entityHitBox.overlaps(entityHitBox) && !entity.isDead && !isDead;
    }
    
    public boolean overlaps(Rectangle hitBox) {
        return hitBox.overlaps(entityHitBox) && !isDead;
    }
    
}
