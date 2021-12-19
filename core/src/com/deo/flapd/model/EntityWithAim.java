package com.deo.flapd.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EntityWithAim extends Entity {
    
    protected Entity homingTarget;
    protected boolean canAim;
    
    public void setHomingTarget(Entity homingTarget) {
        if(canAim){
            if(homingTarget == null){
                this.homingTarget = null;
            } else if (this.homingTarget != null) {
                if (this.homingTarget.isDead) {
                    if (homingTarget.x - x > 100) {
                        this.homingTarget = homingTarget;
                    } else {
                        this.homingTarget = null;
                    }
                } else if (homingTarget.x - x < -50) {
                    this.homingTarget = null;
                }
            } else {
                if (homingTarget.x - x > 100) {
                    this.homingTarget = homingTarget;
                }
            }
        }
    }
    
    public void drawAim(ShapeRenderer shapeRenderer, float xOffset, float yOffset, Color color) {
        if (canAim && homingTarget != null) {
            if (!homingTarget.isDead) {
                shapeRenderer.setColor(color);
                shapeRenderer.line(x + xOffset, y + yOffset, homingTarget.x + homingTarget.width / 2f, homingTarget.y + homingTarget.height / 2f);
            }
        }
    }
    
}
