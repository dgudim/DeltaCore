package com.deo.flapd.model.environment;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deo.flapd.model.Entity;

public abstract class EnvironmentalEffect extends Entity {
    
    ParticleEffectPool.PooledEffect effect;
    
    public boolean remove;
    
    public void drawEffect(SpriteBatch batch) {
        effect.draw(batch);
    }
    
    public void draw(SpriteBatch batch) {
        entitySprite.draw(batch);
    }
    
    public void dispose() {
        effect.free();
    }
    
    public abstract void update(float delta);
}
