package com.deo.flapd.model.environment;

import static com.deo.flapd.utils.DUtils.getRandomBoolean;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.deo.flapd.utils.CompositeManager;

public class FallingShip extends EnvironmentalEffect {
    
    boolean rtl;
    
    FallingShip(CompositeManager compositeManager, float x) {
        entitySprite = new Sprite((Texture) compositeManager.getAssetManager().get("fallingShip.png"));
        
        rtl = getRandomBoolean(50);
        setSize(128 * (rtl ? -1 : 1), 128);
        
        this.x = x;
        y = 480;
        
        init();
        
        effect = compositeManager.getParticleEffectPool().getParticleEffectByPath("particles/particle_nowind.p");
        effect.scaleEffect(0.3f);
        effect.setPosition(x + originX, y + originY);
    }
    
    @Override
    public void update(float delta) {
        updateEntity(delta);
        
        x += 30 * delta * (rtl ? -1 : 1);
        y -= 30 * delta;
        
        effect.setPosition(x + originX, y + originY);
        effect.update(delta);
        
        remove = x < -height - 300 || x > 1110 || y < -width - 300;
    }
}
