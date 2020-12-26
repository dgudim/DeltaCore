package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;

import static com.deo.flapd.utils.DUtils.getRandomInRange;

public class MusicManager {

    private Music music;
    private String musicPath;
    private int minMusicIndex, maxMusicIndex;
    private float delayBetweenSongs;
    private float currentDelay;
    private boolean isWaitingForNewSong;
    private float currentVolume;

    MusicManager(String musicPath, int minMusicIndex, int maxMusicIndex, float delayBetweenSongs) {
        this.musicPath = musicPath;
        this.minMusicIndex = minMusicIndex;
        this.maxMusicIndex = maxMusicIndex;
        this.delayBetweenSongs = delayBetweenSongs;
        music = Gdx.audio.newMusic(Gdx.files.internal(musicPath + getRandomInRange(minMusicIndex, maxMusicIndex) + ".ogg"));
        music.setVolume(0);
        music.play();
        music.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                music.dispose();
                isWaitingForNewSong = true;
            }
        });
    }

    public void resume(){
        if(!music.isPlaying()){
            music.play();
        }
    }

    public void update(float volume, float delta) {
        if(!isWaitingForNewSong) {
            if (volume > currentVolume) {
                currentVolume = (float) MathUtils.clamp(currentVolume + 0.05 * delta, 0, 1);
                resume();
                music.setVolume(currentVolume);
            } else if (volume < currentVolume) {
                currentVolume = (float) MathUtils.clamp(currentVolume - 0.05 * delta, 0, 1);
                resume();
                music.setVolume(currentVolume);
            }
            if (music.isPlaying() && volume == 0) {
                music.pause();
            }
        }else{
            if(currentDelay < delayBetweenSongs) {
                currentDelay += delta;
            }else{
                currentDelay = 0;
                currentVolume = 0;
                isWaitingForNewSong = false;
                music = Gdx.audio.newMusic(Gdx.files.internal(musicPath + getRandomInRange(minMusicIndex, maxMusicIndex) + ".ogg"));
                music.setVolume(0);
                music.play();
            }
        }
    }

}
