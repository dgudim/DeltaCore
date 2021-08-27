package com.deo.flapd.utils;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.log;

public class ParticleEffectPoolLoader {
    
    Array<PooledParticleEffectCollection> effectCollections = new Array<>();
    String[][] allParticleEffects = new String[][]{
            {"particles/bullet_trail_highEmission.p", " "},
            {"particles/bullet_trail_left.p", " "},
            {"particles/bullet_trail_left_blue.p", " "},
            {"particles/bullet_trail_left_yellow.p", " "},
            {"particles/engine_warp.p", " "},
            {"particles/explosion.p", "basic explosion"},
            {"particles/explosion2.p", " "},
            {"particles/explosion2_cyan.p", " "},
            {"particles/explosion2_green.p", " "},
            {"particles/explosion2_purple.p", " "},
            {"particles/explosion3.p", " "},
            {"particles/explosion3_2.p", " "},
            {"particles/explosion3_3.p", " "},
            {"particles/explosion3_4.p", " "},
            {"particles/explosion4.p", " "},
            {"particles/explosion4_1.p", " "},
            {"particles/explosion4_2.p", " "},
            {"particles/explosion4_3.p", " "},
            {"particles/explosion_destroyer.p", " "},
            {"particles/explosion_evil.p", " "},
            {"particles/explosion_evil_small.p", " "},
            {"particles/fire.p", " "},
            {"particles/fire2.p", " "},
            {"particles/fire3.p", " "},
            {"particles/fire_down.p", " "},
            {"particles/fire_engine_left_blue.p", " "},
            {"particles/fire_engine_left_blue_purple.p", " "},
            {"particles/fire_engine_left_red.p", " "},
            {"particles/fire_engine_left_red_green.p", " "},
            {"particles/fire_engine_left_red_purple.p", " "},
            {"particles/laser_powerup_red.p", " "},
            {"particles/particle_nowind.p", " "},
            {"particles/particle_nowind2.p", " "},
            {"particles/smoke.p", " "}
    };
    
    public ParticleEffectPoolLoader() {
        log("preparing particle pool", INFO);
        long time = TimeUtils.millis();
        int totalParticles = 0;
        for (String[] pathAndName : allParticleEffects) {
            int particlesToPreload = getInteger("pool_" + pathAndName[0]) == 0 ? 1 : getInteger("pool_" + pathAndName[0]);
            effectCollections.add(new PooledParticleEffectCollection(pathAndName[0], particlesToPreload, pathAndName[1]));
            totalParticles += particlesToPreload;
        }
        log("prepared particle pool in " + TimeUtils.timeSinceMillis(time) + "ms, total particles: " + totalParticles, INFO);
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