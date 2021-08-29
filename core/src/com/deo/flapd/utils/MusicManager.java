package com.deo.flapd.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;
import static com.deo.flapd.utils.MusicManager.SourceType.COLLECTION;
import static com.deo.flapd.utils.MusicManager.SourceType.SINGLE;

public class MusicManager {
    
    enum SourceType{SINGLE, COLLECTION}
    
    private Music music;
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
        if(sourceType.equals(COLLECTION)){
            path = musicPath + getRandomInRange(minMusicIndex, maxMusicIndex) + ".ogg";
        }else{
            path = musicPath;
        }
        isWaitingForNewSong = false;
        currentDelay = 0;
        if(music != null){
            music.stop();
        }
        music = assetManager.get(path, Music.class);
        log("playing " + path, INFO);
        music.setVolume(0);
        currentVolume = 0;
        music.setOnCompletionListener(music -> { isWaitingForNewSong = true; });
        try{
            music.play();
        }catch (GdxRuntimeException e){
            isWaitingForNewSong = true;
            logException(e);
        }
    }
    
    public void resume() {
        if (!music.isPlaying()) {
            music.play();
        }
    }
    
    public void setVolume(float volume) {
        targetVolume = volume;
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
                if (targetVolume > currentVolume) {
                    currentVolume = clamp(currentVolume + delta, 0, 1);
                    resume();
                    music.setVolume(currentVolume);
                } else if (targetVolume < currentVolume) {
                    currentVolume = clamp(currentVolume - delta, 0, 1);
                    resume();
                    music.setVolume(currentVolume);
                }
                if (music.isPlaying() && currentVolume <= 0.008) {
                    music.pause();
                }
            }
        }
    }
}
