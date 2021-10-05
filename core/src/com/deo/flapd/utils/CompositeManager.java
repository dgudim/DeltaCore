package com.deo.flapd.utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.utils.postprocessing.effects.Bloom;

public class CompositeManager {
    
    private AssetManager assetManager;
    private MusicManager musicManager;
    private SoundManager soundManager;
    
    private PostProcessor blurProcessor;
    private Bloom bloom;
    
    private SpriteBatch batch;
    
    private Game game;
    
    public CompositeManager() {
    }
    
    public SpriteBatch getBatch() {
        return batch;
    }
    
    public Game getGame() {
        return game;
    }
    
    public AssetManager getAssetManager() {
        return assetManager;
    }
    
    public MusicManager getMusicManager() {
        return musicManager;
    }
    
    public SoundManager getSoundManager() {
        return soundManager;
    }
    
    public PostProcessor getBlurProcessor() {
        return blurProcessor;
    }
    
    public Bloom getBloom() {
        return bloom;
    }
    
    public void setBlurProcessor(PostProcessor blurProcessor) {
        this.blurProcessor = blurProcessor;
    }
    
    public void setBloom(Bloom bloom) {
        this.bloom = bloom;
    }
    
    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    public void setMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }
    
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public void preloadSounds() {
        soundManager.loadSounds();
    }
    
    public void dispose() {
        assetManager.dispose();
    }
}
