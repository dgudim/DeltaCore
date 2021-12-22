package com.deo.flapd.view.dialogues;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.LocaleManager;
import com.deo.flapd.utils.ui.UIComposer;

public class Dialogue {
    
    protected BitmapFont font;
    protected AssetManager assetManager;
    protected UIComposer uiComposer;
    protected LocaleManager localeManager;
    
    protected Skin skin;
    protected Dialog dialog;
    
    Dialogue(CompositeManager compositeManager, String dialogTextureName){
        assetManager = compositeManager.getAssetManager();
        uiComposer = compositeManager.getUiComposer();
        localeManager = compositeManager.getLocaleManager();
        font = assetManager.get("fonts/pixel.ttf");
        font.setUseIntegerPositions(false);
        font.getData().markupEnabled = true;
    
        skin = new Skin();
        skin.addRegions(assetManager.get("shop/workshop.atlas"));
    
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.background = skin.getDrawable(dialogTextureName);
        dialog = new Dialog("", dialogStyle);
        
    }
    
    void update(){}

}

