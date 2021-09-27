package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.getNameFromPath;
import static com.deo.flapd.utils.DUtils.log;

public class SoundManager {
    
    String[] soundPaths = {"sfx/click.ogg", "sfx/explosion.ogg", "sfx/ftl.ogg", "sfx/ftl_flight.ogg",
            "sfx/gun1.ogg", "sfx/gun2.ogg", "sfx/gun3.ogg", "sfx/gun4.ogg", "sfx/laser.ogg"};
    HashMap<String, Sound> soundHandles;
    HashMap<String, Array<Long>> soundIds;
    
    public SoundManager(AssetManager assetManager) {
        soundHandles = new HashMap<>();
        soundIds = new HashMap<>();
        for (String soundPath : soundPaths) {
            String name = getNameFromPath(soundPath);
            log("Loaded sound "+name, DEBUG);
            soundHandles.put(name, assetManager.get(soundPath));
            soundIds.put(name, new Array<>());
        }
    }
    
    public long playSound(String name, float volume){
        return playSound(name, volume, 1);
    }
    
    public long playSound(String name, float volume, float pitch){
        long soundId = soundHandles.get(name).play(volume, pitch, 0);
        soundIds.get(name).add(soundId);
        return soundId;
    }
    
    public boolean stopSound(String name, long soundId){
        soundHandles.get(name).stop(soundId);
        return soundIds.get(name).removeValue(soundId, true);
    }
    
    public boolean stopSound(String name){
        soundHandles.get(name).stop();
        soundIds.get(name).clear();
        return true;
    }
    
}
