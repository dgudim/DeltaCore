package com.deo.flapd.view;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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
    private float buttonWidth, buttonHeight, pad, fontScale;
    private Array<Actor> targets, overrideActors;
    private int categoryCounter;
    private Image background, Tbackground;
    private boolean useBg;
    private boolean closeAtSecondClick;
    private String key;
    private UIComposer uiComposer;
    private String style;
    private boolean useTbg;

    CategoryManager(AssetManager assetManager, float buttonWidth, float buttonHeight, float pad, float fontScale, String style, String background, String tableBackground, boolean closeAtSecondClick, String personalKey){

        buttons = new Table();

        targets = new Array<>();
        overrideActors = new Array<>();

        Skin mainSkin = new Skin();
        mainSkin.addRegions((TextureAtlas)assetManager.get("menuButtons/menuButtons.atlas"));

        uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles(style);
        this.style = style;

        this.background = new Image();
        Tbackground = new Image();

        if(!background.equals("")) {
            this.background = new Image((Texture) assetManager.get(background+".png"));
            this.background.setVisible(false);
        }
        if(!tableBackground.equals("")) {
            Tbackground = new Image((Texture) assetManager.get(tableBackground+".png"));
            Tbackground.setVisible(false);
        }

        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
        this.pad = pad;
        this.fontScale = fontScale;
        this.closeAtSecondClick = closeAtSecondClick;
        key = personalKey;
        useBg = !background.equals("");
        useTbg = !tableBackground.equals("");

        buttons.addActor(this.background);
        buttons.addActor(Tbackground);
    }

    Button addCategory(Actor whatToOpen, String name){
        TextButton category = uiComposer.addTextButton(style, name, fontScale);
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

        return category;
    }

    Button addCloseButton(){
        TextButton category = uiComposer.addTextButton(style, "back", fontScale);

        category.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });

        category.setBounds(0, pad, buttonWidth, buttonHeight);
        buttons.addActor(category);
        return category;
    }

    public void setBounds(float x, float y, float height){
        buttons.setBounds(x, y, buttonWidth, height);
    }

    void setBackgroundBounds(float x, float y, float width, float height){
        background.setBounds(x-buttons.getX(), y-buttons.getY(), width, height);
    }

    void setTableBackgroundBounds(float x, float y, float width, float height){
        Tbackground.setBounds(x-buttons.getX(), y-buttons.getY(), width, height);
    }

    @Override
    public void setDebug(boolean debug){
        for(int i = 0; i<targets.size; i++){
            targets.get(i).setDebug(debug);
        }
        buttons.setDebug(debug);
        background.setDebug(debug);
        Tbackground.setDebug(debug);
    }

    private void open(int target){
        hideAll();
        targets.get(target).setVisible(true);
        background.setVisible(useBg);
        Tbackground.setVisible(useTbg);
        putInteger(key, target);
    }

    private void close(){
        hideAll();
        background.setVisible(false);
        Tbackground.setVisible(false);
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
        if(useTbg) {
            Tbackground.setVisible(visible);
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
