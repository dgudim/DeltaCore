package com.deo.flapd.view.dialogues;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.deo.flapd.utils.ui.UIComposer;

public class ConfirmationDialogue extends Dialogue{

    public ConfirmationDialogue(AssetManager assetManager, Stage stage, String message, InputListener okButtonListener, InputListener exitButtonListener){
        BitmapFont font = assetManager.get("fonts/font2(old).fnt");
        Skin skin = new Skin();
        skin.addRegions(assetManager.get("shop/workshop.atlas"));

        UIComposer uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("workshopRed", "workshopGreen");

        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.background = skin.getDrawable("exitDialog");
        final Dialog dialog = new Dialog("", dialogStyle);

        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;

        TextButton yes = uiComposer.addTextButton("workshopGreen", "yes", 0.12f);
        TextButton no = uiComposer.addTextButton("workshopRed", "no", 0.12f);
        yes.setBounds(45, 3, 39, 22);
        no.setBounds(3, 3, 39, 22);

        no.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        yes.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        no.addListener(exitButtonListener);
        yes.addListener(okButtonListener);

        Label text = new Label(message, yellowLabelStyle);
        text.setBounds(3, 28, 81, 39);
        text.setWrap(true);
        text.setFontScale(0.1f);
        text.setAlignment(Align.center);

        dialog.addActor(yes);
        dialog.addActor(no);
        dialog.addActor(text);
        dialog.setScale(4);
        dialog.setSize(87, 70);
        dialog.setPosition(95, 130);
        stage.addActor(dialog);
    }

    public ConfirmationDialogue(AssetManager assetManager, Stage stage, String message, InputListener okButtonListener){
        new ConfirmationDialogue(assetManager, stage, message, okButtonListener, new ClickListener());
    }

}
