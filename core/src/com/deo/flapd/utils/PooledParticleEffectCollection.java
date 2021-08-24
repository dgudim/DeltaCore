package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Array;

import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.log;

public class PooledParticleEffectCollection {
    
    String path;
    int initialCapacity;
    int currentCapacity;
    String name;
    Array<PooledParticleEffect> freeParticleEffects;
    
    PooledParticleEffectCollection(String path, int initialCapacity, String name) {
        this.path = path;
        this.initialCapacity = initialCapacity;
        currentCapacity = initialCapacity;
        this.name = name;
        freeParticleEffects = new Array<>();
    }
    
    void load() {
        for (int i = 0; i < initialCapacity; i++) {
            freeParticleEffects.add(new PooledParticleEffect(path));
        }
    }
    
    PooledParticleEffect getParticleEffect() {
        if (freeParticleEffects.isEmpty()) {
            currentCapacity++;
            log("expanding pooled particle effect collection " + name + ", path: " + path + ", new size: " + freeParticleEffects.size + ", overhead: " + currentCapacity / (float) initialCapacity * 100 + "%", INFO);
        }
        return freeParticleEffects.isEmpty() ? new PooledParticleEffect(path) : freeParticleEffects.pop();
    }
    
    void free(PooledParticleEffect pooledParticleEffect) {
        pooledParticleEffect.reset(true);
        freeParticleEffects.add(pooledParticleEffect);
    }
    
    void dispose() {
        for (int i = 0; i < freeParticleEffects.size; i++) {
            freeParticleEffects.get(i).dispose();
        }
    }
    
    public class PooledParticleEffect extends ParticleEffect {
        
        PooledParticleEffect(String path) {
            super();
            load(Gdx.files.internal(path), Gdx.files.internal("particles"));
        }
        
        public void free() {
            PooledParticleEffectCollection.this.free(this);
        }
        
    }
    
}
