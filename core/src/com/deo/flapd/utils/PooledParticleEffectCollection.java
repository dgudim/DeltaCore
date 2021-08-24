package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;

public class PooledParticleEffectCollection{
    
    String path;
    int initialCapacity;
    int currentCapacity;
    String name;
    ParticleEffectPool pool;
    ParticleEffect templateParticleEffect;
    
    PooledParticleEffectCollection(String path, int initialCapacity, String name) {
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
        };
        pool.fill(initialCapacity / 2);
    }
    
    ParticleEffectPool.PooledEffect obtainEffect() {
        return pool.obtain();
    }
    
    void dispose() {
       pool.clear();
    }
}
