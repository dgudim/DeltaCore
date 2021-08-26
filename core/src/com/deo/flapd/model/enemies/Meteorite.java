package com.deo.flapd.model.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deo.flapd.model.Entity;

import static com.deo.flapd.view.LoadingScreen.particleEffectPoolLoader;

public class Meteorite extends Entity {
    
    private final float flyingDirection;
    private final float rotationSpeed;
    public float radius;
    private final ParticleEffectPool.PooledEffect trail;
    
    public boolean remove;
    
    public Meteorite(AssetManager assetManager, float x, float flyingDirection, float radius) {
        
        entitySprite = new Sprite((Texture) assetManager.get("Meteo.png"));
        
        setSize(radius * 2, radius * 2);
        
        rotationSpeed = 1050 / radius;
        
        this.x = x;
        y = 480;
        
        super.init();
        
        trail = particleEffectPoolLoader.getParticleEffectByPath("particles/particle_nowind.p");
        trail.scaleEffect(radius / 25);
        trail.setPosition(x + originX, y + originY);
        
        this.flyingDirection = flyingDirection;
        this.radius = radius;
        
    }
    
    public void drawEffect(SpriteBatch batch) {
        trail.draw(batch);
    }
    
    public void draw(SpriteBatch batch) {
        entitySprite.draw(batch);
    }
    
    public void update(float delta) {
        updateEntity(delta);
        
        x += 53 * flyingDirection * delta;
        y -= 53 * delta;
        
        rotation += rotationSpeed * delta;
        
        trail.setPosition(x + originX, y + originY);
        trail.update(delta);
        
        remove = x < -radius - 300 || x > 1110 || y < -300;
    }
    
    public void dispose() {
        trail.free();
    }
    
}
