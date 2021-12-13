package com.deo.flapd.utils;

import com.badlogic.gdx.Game;
import com.deo.flapd.view.screens.GameOverScreen;
import com.deo.flapd.view.screens.GameScreen;
import com.deo.flapd.view.screens.LoadingScreen;
import com.deo.flapd.view.screens.MenuScreen;

public class ScreenManager {
    
    private final CompositeManager compositeManager;
    private final Game game;
    
    private GameScreen gameScreen;
    private GameOverScreen gameOverScreen;
    private MenuScreen menuScreen;
    private LoadingScreen loadingScreen;
    
    public ScreenManager(CompositeManager compositeManager) {
        this.compositeManager = compositeManager;
        game = compositeManager.getGame();
    }
    
    public void setCurrentScreenGameScreen(boolean newGame) {
        if (gameScreen != null) {
            gameScreen.reset(newGame);
        } else {
            gameScreen = new GameScreen(compositeManager, newGame);
        }
        game.setScreen(gameScreen);
    }
    
    public void setCurrentScreenMenuScreen() {
        if (menuScreen != null) {
            menuScreen.reset();
        } else {
            menuScreen = new MenuScreen(compositeManager);
        }
        game.setScreen(menuScreen);
    }
    
    public void setCurrentScreenLoadingScreen() {
        if (loadingScreen != null) {
            loadingScreen.reset();
        } else {
            loadingScreen = new LoadingScreen(compositeManager);
        }
        game.setScreen(loadingScreen);
    }
    
    public void setCurrentScreenGameOverScreen() {
        if (gameOverScreen != null) {
            gameOverScreen.reset();
        } else {
            gameOverScreen = new GameOverScreen(compositeManager);
        }
        game.setScreen(gameOverScreen);
    }
    
    public void pauseGame(){
        game.pause();
    }
    
    public void dispose(){
        if (loadingScreen != null) {
            loadingScreen.dispose();
        }
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        if (menuScreen != null) {
            menuScreen.dispose();
        }
        if (gameOverScreen != null) {
            gameOverScreen.dispose();
        }
    }
}
