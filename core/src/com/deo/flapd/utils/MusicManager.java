package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.lerp;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;
import static com.deo.flapd.utils.DUtils.readObjectFromFile;
import static com.deo.flapd.utils.MusicManager.SourceType.COLLECTION;
import static com.deo.flapd.utils.MusicManager.SourceType.SINGLE;

public class MusicManager {
    
    enum SourceType {SINGLE, COLLECTION}
    
    private Music music;
    private ArrayList<Float> amplitude;
    private String musicPath;
    private int minMusicIndex;
    private int maxMusicIndex;
    private float delayBetweenSongs;
    private float currentDelay;
    private boolean isWaitingForNewSong = true;
    private float currentVolume;
    private SourceType sourceType;
    public float targetVolume;
    private boolean sourceChanged;
    
    private final AssetManager assetManager;
    
    public void setNewMusicSource(final String musicPath, int minMusicIndex, int maxMusicIndex, float delayBetweenSongs) {
        this.musicPath = musicPath;
        this.minMusicIndex = minMusicIndex;
        this.maxMusicIndex = maxMusicIndex;
        this.delayBetweenSongs = delayBetweenSongs;
        sourceChanged = true;
        sourceType = COLLECTION;
    }
    
    public void setNewMusicSource(final String musicPath, float delayBetweenSongs) {
        this.musicPath = musicPath;
        this.delayBetweenSongs = delayBetweenSongs;
        sourceChanged = true;
        sourceType = SINGLE;
    }
    
    public MusicManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    private void loadNextMusic() {
        String path;
        String amplitudePath;
        if (sourceType.equals(COLLECTION)) {
            path = musicPath + getRandomInRange(minMusicIndex, maxMusicIndex);
            amplitudePath = path + ".snc";
            path += ".ogg";
        } else {
            path = musicPath;
            amplitudePath = path.replace(".ogg", "") + ".snc";
        }
        isWaitingForNewSong = false;
        currentDelay = 0;
        if (music != null) {
            music.stop();
            amplitude = null;
        }
        new Thread(() -> amplitude = (ArrayList<Float>) readObjectFromFile(Gdx.files.internal(amplitudePath))).start();
        music = assetManager.get(path, Music.class);
        log("playing " + path, INFO);
        music.setVolume(0);
        currentVolume = 0;
        music.setOnCompletionListener(music -> isWaitingForNewSong = true);
        try {
            music.play();
        } catch (GdxRuntimeException e) {
            isWaitingForNewSong = true;
            amplitude = null;
            logException(e);
        }
    }
    
    public void setVolume(float volume) {
        targetVolume = volume;
    }
    
    public float getAmplitude() {
        if (music != null && amplitude != null) {
            int arrayPos = (int) (music.getPosition() * 1000);
            arrayPos = clamp(arrayPos, 0, amplitude.size() - 2);
            float lerpPos = (music.getPosition() * 1000_000 - arrayPos * 1000) / 1000f;
            float amplitudeCurr = amplitude.get(arrayPos);
            float amplitudeNext = amplitude.get(arrayPos + 1);
            return lerp(amplitudeCurr, amplitudeNext, lerpPos);
        }
        return 0;
    }
    
    public void update(float delta) {
        delta = clamp(delta, 0, 0.2f) * 0.5f;
        if (sourceChanged) {
            loadNextMusic();
            sourceChanged = false;
        } else {
            if (isWaitingForNewSong) {
                if (currentDelay < delayBetweenSongs) {
                    currentDelay += delta;
                } else {
                    loadNextMusic();
                }
            } else {
                if (targetVolume - currentVolume >= delta) {
                    currentVolume = clamp(currentVolume + delta, 0, 1);
                    music.setVolume(currentVolume);
                } else if (targetVolume - currentVolume <= -delta) {
                    currentVolume = clamp(currentVolume - delta, 0, 1);
                    music.setVolume(currentVolume);
                }
                if (music.isPlaying() && currentVolume <= delta && targetVolume == 0) {
                    music.setVolume(0);
                    currentVolume = 0;
                }
            }
        }
    }
}
