package com.deo.flapd.utils.particles;

import static com.deo.flapd.utils.DUtils.LogLevel.ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.LogLevel.WARNING;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.log;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class ParticleEffectPoolLoader {
    
    Array<PooledParticleEffectCollection> effectCollections = new Array<>();
    String[][] allParticleEffects = new String[][]{
            {"particles/bullet_trail_left.p", "default_red_bullet_trail"},
            {"particles/bullet_trail_left_blue.p", "default_blue_bullet_trail"},
            {"particles/bullet_trail_left_yellow.p", "default_yellow_bullet_trail"},
            {"particles/engine_warp.p", "plasma_engine_fire_windy"},
            {"particles/explosion.p", "basic explosion"},
            {"particles/explosion2.p", "big_default_core_explosion"},
            {"particles/explosion2_cyan.p", "big_cyan_core_explosion"},
            {"particles/explosion2_green.p", "big_green_core_explosion"},
            {"particles/explosion2_purple.p", "big_purple_core_explosion"},
            {"particles/explosion3.p", "small_reb_bullet_explosion"},
            {"particles/explosion3_2.p", "small_yellow_bullet_explosion"},
            {"particles/explosion3_3.p", "small_blue_bullet_explosion"},
            {"particles/explosion3_4.p", "small_green_bullet_explosion"},
            {"particles/explosion4.p", "bonus_pickup_cyan"},
            {"particles/explosion4_1.p", "bonus_pickup_red"},
            {"particles/explosion4_2.p", "bonus_pickup_yellow"},
            {"particles/explosion4_3.p", "bonus_pickup_white"},
            {"particles/explosion_destroyer.p", "destroyer_boss_explosion"},
            {"particles/explosion_evil.p", "evil_boss_body_explosion"},
            {"particles/explosion_evil_small.p", "evil_boss_cannon_explosion"},
            {"particles/fire.p", "default_fire"},
            {"particles/fire2.p", "red_fire"},
            {"particles/fire3.p", "blue_fire"},
            {"particles/fire_down.p", "checkpoint_fire"},
            {"particles/fire_engine_left_blue.p", "enemy_fire"},
            {"particles/fire_engine_left_blue_purple.p", "plasma_engine_fire"},
            {"particles/fire_engine_left_red.p", "nuclear_engine_fire_right"},
            {"particles/fire_engine_left_red_green.p", "default_engine_fire"},
            {"particles/fire_engine_left_red_purple.p", "nuclear_engine_fire"},
            {"particles/laser_powerup_red.p", "laser_powerup"},
            {"particles/particle_nowind.p", "meteorite_trail"},
            {"particles/particle_nowind2.p", "bomb_trail"},
            {"particles/smoke.p", "default_smoke"},
            {"particles/explosion_station_core.p", "core_explosion"},
            {"particles/fire_big.p", "destroyer_boss_fire"}
    };
    
    public ParticleEffectPoolLoader() {
        log("preparing particle pool", INFO);
        long time = TimeUtils.millis();
        int totalParticles = 0;
        for (String[] pathAndName : allParticleEffects) {
            int particlesToPreload = getInteger("pool_" + pathAndName[0]) == 0 ? 1 : getInteger("pool_" + pathAndName[0]);
            
            for (int i = 0; i < effectCollections.size; i++) {
                if (effectCollections.get(i).path.equals(pathAndName[0])) {
                    log("duplicate particle effect path detected: " + pathAndName[0], WARNING);
                }
                if (effectCollections.get(i).name.equals(pathAndName[1])) {
                    log("duplicate particle effect name detected: " + pathAndName[0], WARNING);
                }
            }
            
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
        log("no particle effect with name: " + name, ERROR);
        return null;
    }
    
    public ParticleEffectPool.PooledEffect getParticleEffectByPath(String path) {
        for (int i = 0; i < effectCollections.size; i++) {
            if (effectCollections.get(i).path.equals(path)) {
                return effectCollections.get(i).obtainEffect();
            }
        }
        log("no particle effect with path: " + path, ERROR);
        return null;
    }
    
    public void dispose() {
        for (int i = 0; i < effectCollections.size; i++) {
            effectCollections.get(i).dispose();
        }
        effectCollections.clear();
    }
    
}