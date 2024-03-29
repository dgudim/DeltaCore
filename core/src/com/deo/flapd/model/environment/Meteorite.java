package com.deo.flapd.model.environment;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.deo.flapd.utils.CompositeManager;

public class Meteorite extends EnvironmentalEffect {
    
    private final float flyingDirection;
    private final float rotationSpeed;
    public float radius;
    
    public Meteorite(CompositeManager compositeManager, float x, float flyingDirection, float radius) {
        
        entitySprite = new Sprite((Texture) compositeManager.getAssetManager().get("Meteo.png"));
        
        setSize(radius * 2, radius * 2);
        
        rotationSpeed = 1050 / radius;
        
        this.x = x;
        y = 480;
        
        init();
        
        effect = compositeManager.getParticleEffectPool().getParticleEffectByPath("particles/particle_nowind.p");
        effect.scaleEffect(radius / 25);
        effect.setPosition(x + originX, y + originY);
        
        this.flyingDirection = flyingDirection;
        this.radius = radius;
        
    }
    
    @Override
    public void update(float delta) {
        updateEntity(delta);
        
        x += 53 * flyingDirection * delta;
        y -= 53 * delta;
        
        rotation += rotationSpeed * delta;
        
        effect.setPosition(x + originX, y + originY);
        effect.update(delta);
        
        remove = x < -radius * 2 - 300 || x > 1110 || y < -300;
    }
}
