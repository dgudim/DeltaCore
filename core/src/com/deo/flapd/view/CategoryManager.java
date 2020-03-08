package com.deo.flapd.view;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.putInteger;

public class CategoryManager extends Actor{

    private Table buttons;
    private TextButton.TextButtonStyle buttonStyle;
    private float buttonWidth, buttonHeight, pad, fontScale;
    private Array<Actor> targets, overrideActors;
    private int categoryCounter;
    private Image background;
    private boolean useBg;
    private boolean closeAtSecondClick;
    private String key;

    public CategoryManager(AssetManager assetManager, BitmapFont font, float buttonWidth, float buttonHeight, float pad, float fontScale, int style, boolean useBackground, boolean closeAtSecondClick, String personalKey){

        buttons = new Table();

        targets = new Array<>();
        overrideActors = new Array<>();

        Skin mainSkin = new Skin();
        mainSkin.addRegions((TextureAtlas)assetManager.get("menuButtons/menuButtons.atlas"));

        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        if(style == 1) {
            buttonStyle.downFontColor = Color.valueOf("#22370E");
            buttonStyle.overFontColor = Color.valueOf("#3D51232");
            buttonStyle.fontColor = Color.valueOf("#3D4931");
            buttonStyle.over = mainSkin.getDrawable("blank_over");
            buttonStyle.down = mainSkin.getDrawable("blank_enabled");
            buttonStyle.up = mainSkin.getDrawable("button_blank");
        }else if(style == 2){
            buttonStyle.downFontColor = Color.valueOf("#31FF25");
            buttonStyle.overFontColor = Color.valueOf("#00DC00");
            buttonStyle.fontColor = Color.valueOf("#46D33E");
            buttonStyle.over = mainSkin.getDrawable("blank2_over");
            buttonStyle.down = mainSkin.getDrawable("blank2_enabled");
            buttonStyle.up = mainSkin.getDrawable("blank2_disabled");
        }

        background = new Image((Texture)assetManager.get("infoBg.png"));
        background.setVisible(false);

        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
        this.pad = pad;
        this.fontScale = fontScale;
        this.closeAtSecondClick = closeAtSecondClick;
        key = personalKey;
        useBg = useBackground;

        buttons.addActor(background);
    }

    public void addCategory(Actor whatToOpen, String name){
        TextButton category = new TextButton(name, buttonStyle);
        category.getLabel().setFontScale(fontScale);
        targets.add(whatToOpen);

        final int target = categoryCounter;
        category.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!targets.get(target).isVisible()){
                    open(target);
                }else if(closeAtSecondClick){
                    close();
                }
            }
        });

        categoryCounter++;

        buttons.add(category).padTop(pad).padBottom(pad).size(buttonWidth, buttonHeight).row();
    }

    public void addCloseButton(){
        TextButton category = new TextButton("back", buttonStyle);
        category.getLabel().setFontScale(fontScale);
        category.setText("back");

        category.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        buttons.add(category).padTop(pad).padBottom(pad).size(buttonWidth, buttonHeight).row();
    }

    public void setBounds(float x, float y){
        buttons.setBounds(x, y, buttonWidth, targets.size*(buttonHeight+2*pad));
    }

    public void setBackgroundBounds(float x, float y, float width, float height){
        background.setBounds(x - buttons.getX(), y, width, height);
    }

    public void open(int target){
        hideAll();
        targets.get(target).setVisible(true);
        background.setVisible(useBg);
        putInteger(key, target);
    }

    public void close(){
        hideAll();
        background.setVisible(false);
    }

    void attach(Stage stage){
        stage.addActor(buttons);
    }

    @Override
    public void setVisible(boolean visible){
        buttons.setVisible(visible);
        if(useBg) {
            background.setVisible(visible);
        }
        if(visible){
            open(getInteger(key));
        }else{
            hideAll();
        }
    }

    @Override
    public boolean isVisible() {
        return buttons.isVisible();
    }

    void addOverrideActor(Actor actorToOverride){
        overrideActors.add(actorToOverride);
    }

    private void hideAll(){
        for(int i = 0; i < overrideActors.size; i++){
            overrideActors.get(i).setVisible(false);
        }
        for(int i = 0; i<targets.size; i++){
            targets.get(i).setVisible(false);
        }
    }
}
