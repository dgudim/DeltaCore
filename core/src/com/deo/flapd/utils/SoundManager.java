package com.deo.flapd.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;

import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.LogLevel.WARNING;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.getNameFromPath;
import static com.deo.flapd.utils.DUtils.log;

public class SoundManager {
    
    private final AssetManager assetManager;
    
    String[] soundPaths = {"sfx/click.ogg", "sfx/explosion.ogg", "sfx/ftl.ogg", "sfx/ftl_flight.ogg",
            "sfx/gun1.ogg", "sfx/gun2.ogg", "sfx/gun3.ogg", "sfx/gun4.ogg", "sfx/laser.ogg"};
    HashMap<String, Sound> soundHandles;
    HashMap<String, Array<Long>> soundIds;
    float soundVolume;
    
    public SoundManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        soundHandles = new HashMap<>();
        soundIds = new HashMap<>();
        notifyVolumeUpdated();
    }
    
    public void loadSounds() {
        for (String soundPath : soundPaths) {
            String name = getNameFromPath(soundPath);
            if (!soundIds.containsKey(name)) {
                log("Loaded sound " + name, DEBUG);
                soundHandles.put(name, assetManager.get(soundPath));
                soundIds.put(name, new Array<>());
            }
        }
    }
    
    public void notifyVolumeUpdated() {
        soundVolume = getFloat(Keys.soundVolume) / 100f;
    }
    
    public long playSound(String name) {
        return playSound(name, 1);
    }
    
    public void playSound_noLink(String name) {
        playSound_noLink(name, 1);
    }
    
    //no need to call stopSound
    public void playSound_noLink(String name, float pitch) {
        if (soundVolume > 0) {
            try {
                soundHandles.get(name).play(soundVolume, pitch, 0);
            } catch (GdxRuntimeException e) {
                log("error playing sound: " + name + ", sound handle: " + soundHandles.get(name), WARNING);
            }
        }
    }
    
    //need to call stopSound after the sound is no longer playing or needed
    public long playSound(String name, float pitch) {
        if (soundVolume > 0) {
            try {
                long soundId = soundHandles.get(name).play(soundVolume, pitch, 0);
                soundIds.get(name).add(soundId);
                return soundId;
            } catch (GdxRuntimeException e) {
                log("error playing sound: " + name + ", sound handle: " + soundHandles.get(name), WARNING);
            }
        }
        return -1;
    }
    
    public boolean stopSound(String name, long soundId) {
        if (soundVolume > 0) {
            soundHandles.get(name).stop(soundId);
            return soundIds.get(name).removeValue(soundId, true);
        }
        return false;
    }
    
    public boolean stopSound(String name) {
        if (soundVolume > 0) {
            soundHandles.get(name).stop();
            soundIds.get(name).clear();
            return true;
        }
        return false;
    }
    
    public void setPitch(String name, float pitch) {
        if (soundVolume > 0) {
            Array<Long> ids = soundIds.get(name);
            Sound sound = soundHandles.get(name);
            for (int i = 0; i < ids.size; i++) {
                sound.setPitch(ids.get(i), pitch);
            }
        }
    }
    
}
