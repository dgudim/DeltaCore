package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;

import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.log;

public class MusicManager {

    private Music music;
    private String musicPath;
    private int minMusicIndex, maxMusicIndex;
    private float delayBetweenSongs;
    private float currentDelay;
    private boolean isWaitingForNewSong;
    private float currentVolume;

    public float targetVolume;

    public MusicManager(String musicPath, int minMusicIndex, int maxMusicIndex, float delayBetweenSongs) {
        this.musicPath = musicPath;
        this.minMusicIndex = minMusicIndex;
        this.maxMusicIndex = maxMusicIndex;
        this.delayBetweenSongs = delayBetweenSongs;
        loadNextMusic();
    }

    private void loadNextMusic() {
        String songName = musicPath + getRandomInRange(minMusicIndex, maxMusicIndex) + ".ogg";
        music = Gdx.audio.newMusic(Gdx.files.internal(songName));
        log("\n playing " + songName);
        music.setVolume(0);
        currentVolume = 0;
        music.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                dispose();
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
        if (delta > 0.2f) {
            // if the fps is less than 5, don't fade
            delta = 0;
        }
        if (!isWaitingForNewSong) {
            if (targetVolume > currentVolume) {
                currentVolume = (float) MathUtils.clamp(currentVolume + 0.5 * delta, 0, 1);
                resume();
                music.setVolume(currentVolume);
            } else if (targetVolume < currentVolume) {
                currentVolume = (float) MathUtils.clamp(currentVolume - 0.5 * delta, 0, 1);
                resume();
                music.setVolume(currentVolume);
            }
            if (music.isPlaying() && currentVolume == 0) {
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

    public void dispose() {
        if (!isWaitingForNewSong) {
            music.stop();
            music.dispose();
        }
    }

}
