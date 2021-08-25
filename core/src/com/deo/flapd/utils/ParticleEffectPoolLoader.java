package com.deo.flapd.utils;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.log;

public class ParticleEffectPoolLoader {
    
    Array<PooledParticleEffectCollection> effectCollections = new Array<>();
    
    public ParticleEffectPoolLoader() {
        log("preparing particle pool", INFO);
        long time = TimeUtils.millis();
        effectCollections.add(new PooledParticleEffectCollection("particles/bullet_trail_highEmission.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/bullet_trail_left.p", 100, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/bullet_trail_left_blue.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/bullet_trail_left_yellow.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/engine_warp.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion.p", 9, "basic explosion"));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion2.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion2_cyan.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion2_green.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion2_purple.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion3.p", 50, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion3_2.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion3_3.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion3_4.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion4.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion4_1.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion4_2.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion4_3.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion_destroyer.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion_evil.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/explosion_evil_small.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire2.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire3.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire_down.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire_engine_left_blue.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire_engine_left_blue_purple.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire_engine_left_red.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire_engine_left_red_green.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/fire_engine_left_red_purple.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/laser_powerup_red.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/particle_nowind.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/particle_nowind2.p", 1, ""));
        effectCollections.add(new PooledParticleEffectCollection("particles/smoke.p", 1, ""));
        log("prepared particle pool in " + TimeUtils.timeSinceMillis(time) + "ms", INFO);
    }
    
    public ParticleEffectPool.PooledEffect getParticleEffectByName(String name) {
        for (int i = 0; i < effectCollections.size; i++) {
            if (effectCollections.get(i).name.equals(name)) {
                return effectCollections.get(i).obtainEffect();
            }
        }
        return null;
    }
    
    public ParticleEffectPool.PooledEffect getParticleEffectByPath(String path) {
        for (int i = 0; i < effectCollections.size; i++) {
            if (effectCollections.get(i).path.equals(path)) {
                return effectCollections.get(i).obtainEffect();
            }
        }
        return null;
    }
    
    public void dispose() {
        for (int i = 0; i < effectCollections.size; i++) {
            effectCollections.get(i).dispose();
        }
        effectCollections.clear();
    }
    
}