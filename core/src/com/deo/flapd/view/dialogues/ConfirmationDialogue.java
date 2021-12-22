package com.deo.flapd.view.dialogues;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.deo.flapd.utils.CompositeManager;

public class ConfirmationDialogue extends Dialogue {
    
    public ConfirmationDialogue(CompositeManager compositeManager, Stage stage, String message, DialogueActionListener dialogueActionListener) {
        super(compositeManager, "exitDialog");
        
        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;
        
        TextButton yes = uiComposer.addTextButton("workshopGreen", localeManager.get("general.yes"), 0.12f);
        TextButton no = uiComposer.addTextButton("workshopRed", localeManager.get("general.no"), 0.12f);
        yes.setBounds(45, 3, 39, 22);
        no.setBounds(3, 3, 39, 22);
        
        no.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        yes.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        
        no.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialogueActionListener.onCancel();
            }
        });
        yes.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialogueActionListener.onConfirm();
            }
        });
        
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
}
