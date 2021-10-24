package com.deo.flapd.utils.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;

import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putInteger;

public class PooledParticleEffectCollection {
    
    String path;
    int initialCapacity;
    int currentCapacity;
    String name;
    ParticleEffectPool pool;
    ParticleEffect templateParticleEffect;
    
    PooledParticleEffectCollection(String path, int initialCapacity, String name) {
        
        final boolean[] initialized = new boolean[]{false};
        
        this.path = path;
        this.initialCapacity = initialCapacity;
        currentCapacity = initialCapacity;
        this.name = name;
        templateParticleEffect = new ParticleEffect();
        templateParticleEffect.load(Gdx.files.internal(path), Gdx.files.internal("particles/"));
        pool = new ParticleEffectPool(templateParticleEffect, initialCapacity, Integer.MAX_VALUE) {
            @Override
            protected void discard(PooledEffect effect) {
                effect.dispose();
            }
            
            @Override
            protected PooledEffect newObject() {
                if (initialized[0]) {
                    currentCapacity++;
                    putInteger("pool_" + path, currentCapacity);
                    log("expanding pooled particle effect collection: " + name + ", path: " + path + ", new size: " + currentCapacity + ", overhead: " + currentCapacity / (float) initialCapacity * 100 + "%", INFO);
                }
                return super.newObject();
            }
        };
        pool.fill(initialCapacity);
        initialized[0] = true;
    }
    
    ParticleEffectPool.PooledEffect obtainEffect() {
        return pool.obtain();
    }
    
    void dispose() {
        pool.clear();
        templateParticleEffect.dispose();
    }
}
