package com.deo.flapd.utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.deo.flapd.model.loot.Drops;
import com.deo.flapd.utils.postprocessing.PostProcessor;
import com.deo.flapd.utils.postprocessing.effects.Bloom;
import com.deo.flapd.utils.postprocessing.effects.MotionBlur;
import com.deo.flapd.utils.ui.UIComposer;

public class CompositeManager {
    
    private AssetManager assetManager;
    private MusicManager musicManager;
    private SoundManager soundManager;
    private LocaleManager localeManager;
    
    private PostProcessor blurProcessor;
    private Bloom bloom;
    private MotionBlur motionBlur;
    
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    
    private UIComposer uiComposer;
    
    private Game game;
    
    Drops drops;
    
    public CompositeManager() {
    }
    
    public UIComposer getUiComposer() {
        return uiComposer;
    }
    
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
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
    
    public LocaleManager getLocaleManager() {
        return localeManager;
    }
    
    public PostProcessor getBlurProcessor() {
        return blurProcessor;
    }
    
    public MotionBlur getMotionBlur() {
        return motionBlur;
    }
    
    public Bloom getBloom() {
        return bloom;
    }
    
    public Drops getDrops() {
        return drops;
    }
    
    public void setDrops(Drops drops) {
        this.drops = drops;
    }
    
    public void setUiComposer(UIComposer uiComposer) {
        this.uiComposer = uiComposer;
    }
    
    public void setBlurProcessor(PostProcessor blurProcessor) {
        this.blurProcessor = blurProcessor;
    }
    
    public void setBloom(Bloom bloom) {
        this.bloom = bloom;
    }
    
    public void setMotionBlur(MotionBlur motionBlur) {
        this.motionBlur = motionBlur;
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
    
    public void setLocaleManager(LocaleManager localeManager) {
        this.localeManager = localeManager;
    }
    
    public void setShapeRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
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
