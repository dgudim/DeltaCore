package com.deo.flapd.view;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class SellUncraftDialogue {

    public SellUncraftDialogue(AssetManager assetManager, Stage stage, ItemSlotManager itemSlotManager, int availableQuantity, String item){

        BitmapFont font = assetManager.get("fonts/font2(old).fnt");
        Skin skin = new Skin();
        skin.addRegions((TextureAtlas)assetManager.get("shop/workshop.atlas"));

        UIComposer uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("workshopRed", "workshopGreen", "workshopPurple", "sliderDefaultSmall");

        final TextureAtlas itemAtlas = assetManager.get("items/items.atlas");

        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = font;
        dialogStyle.background = skin.getDrawable("blankDialogue");
        final Dialog dialog = new Dialog("", dialogStyle);

        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;

        TextButton sell = uiComposer.addTextButton("workshopGreen", "sell", 0.12f);
        TextButton cancel = uiComposer.addTextButton("workshopRed", "cancel", 0.12f);
        sell.setBounds(86, 3, 39, 22);
        cancel.setBounds(3, 3, 39, 22);

        cancel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        final Label quantityText = new Label("quantity:1", yellowLabelStyle);
        quantityText.setFontScale(0.1f);
        quantityText.setPosition(49, 16);
        quantityText.setSize(30, 10);
        quantityText.setAlignment(Align.center);

        Label topText = new Label("Sell/scrap your items:", yellowLabelStyle);
        topText.setPosition(19, 56);
        topText.setFontScale(0.1f);
        topText.setAlignment(Align.center);

        dialog.addActor(sell);
        dialog.addActor(cancel);
        dialog.setScale(4);
        dialog.setSize(128, 70);
        dialog.setPosition(15, 130);
        stage.addActor(dialog);
    }

}
