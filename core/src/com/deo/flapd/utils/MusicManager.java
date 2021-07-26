package com.deo.flapd.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.LogLevel.INFO;

public class MusicManager {
    
    private Music music;
    private String musicPath;
    private int minMusicIndex;
    private int maxMusicIndex;
    private float delayBetweenSongs;
    private float currentDelay;
    private boolean isWaitingForNewSong = true;
    private float currentVolume;
    
    public float targetVolume;
    private boolean sourceChanged;
    
    private final AssetManager assetManager;
    public void setNewMusicSource(final String musicPath, int minMusicIndex, int maxMusicIndex, float delayBetweenSongs) {
        this.musicPath = musicPath;
        this.minMusicIndex = minMusicIndex;
        this.maxMusicIndex = maxMusicIndex;
        this.delayBetweenSongs = delayBetweenSongs;
        sourceChanged = true;
    }
    
    public MusicManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    private void loadNextMusic() {
        String songName = musicPath + getRandomInRange(minMusicIndex, maxMusicIndex) + ".ogg";
        isWaitingForNewSong = false;
        if(music != null){
            music.stop();
        }
        music = assetManager.get(songName, Music.class);
        log("playing " + songName, INFO);
        music.setVolume(0);
        currentVolume = 0;
        music.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                isWaitingForNewSong = true;
            }
        });
        music.play();
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
            if (!isWaitingForNewSong) {
                if (targetVolume - currentVolume > 0.5 * delta) {
                    currentVolume = clamp(currentVolume + delta, 0, 1);
                    resume();
                    music.setVolume(currentVolume);
                } else if (targetVolume - currentVolume < -0.5 * delta) {
                    currentVolume = clamp(currentVolume - delta, 0, 1);
                    resume();
                    music.setVolume(currentVolume);
                }
                if (music.isPlaying() && currentVolume <= 0.008) {
                    music.pause();
                }
            } else {
                if (currentDelay < delayBetweenSongs) {
                    currentDelay += delta;
                } else {
                    currentDelay = 0;
                    currentVolume = 0;
                    isWaitingForNewSong = false;
                    loadNextMusic();
                }
            }
        }
    }
}
