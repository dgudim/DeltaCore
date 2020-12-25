package com.deo.flapd.view.dialogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.deo.flapd.view.UIComposer;

import java.util.Arrays;

import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.subtractInteger;

public class ColorCustomizationDialogue extends Dialogue {

    private Dialog dialog;

    ColorCustomizationDialogue(final AssetManager assetManager, final String particleEffect, final Stage stage) {
        BitmapFont font = assetManager.get("fonts/font2(old).fnt");
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.13f);
        font.getData().markupEnabled = true;

        Skin textures = new Skin();
        textures.addRegions((TextureAtlas) assetManager.get("shop/workshop.atlas"));
        final Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.background = textures.getDrawable("colorDialog");
        dialogStyle.titleFont = font;

        Array<ParticleEffect> fires = loadFire(particleEffect);

        final ParticleEffect fire = fires.get(0);
        final ParticleEffect fire2 = fires.get(1);

        dialog = new Dialog("", dialogStyle);

        UIComposer uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("workshopRed", "workshopGreen", "sliderDefaultSmall");

        TextButton close = uiComposer.addTextButton("workshopRed", "cancel", 0.13f);
        TextButton ok = uiComposer.addTextButton("workshopGreen", "apply", 0.13f);
        TextButton reset = uiComposer.addTextButton("workshopGreen", "reset", 0.08f);

        close.setBounds(3, 3, 42, 25);
        ok.setBounds(49, 3, 42, 25);
        reset.setBounds(3, 71, 20, 7);

        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire.dispose();
                fire2.dispose();
                dialog.hide();
            }
        });

        TextureAtlas items = assetManager.get("items/items.atlas");
        Table crystal = new Table();
        ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
        itemButtonStyle.imageUp = new Image(items.findRegion("crystal")).getDrawable();
        itemButtonStyle.imageDisabled = new Image(items.findRegion("disabled_crystal")).getDrawable();
        itemButtonStyle.imageDown = new Image(items.findRegion("enabled_crystal")).getDrawable();
        itemButtonStyle.imageOver = new Image(items.findRegion("over_crystal")).getDrawable();
        ImageButton item = new ImageButton(itemButtonStyle);
        crystal.add(item).size(10, 10);
        Label quantity = uiComposer.addText(getInteger("item_crystal") + "/1", (BitmapFont) assetManager.get("fonts/font2(old).fnt"), 0.13f);

        if (getInteger("item_crystal") < 1) {
            quantity.setColor(Color.RED);
        } else {
            quantity.setColor(Color.YELLOW);
        }

        crystal.add(quantity);

        item.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new CraftingDialogue(stage, assetManager, "coloring crystal");
            }
        });

        Image Ship = new Image(assetManager.get("items/items.atlas", TextureAtlas.class).findRegion("ship")) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                fire.draw(batch);
                fire2.draw(batch);
                super.draw(batch, parentAlpha);
            }

            @Override
            public void act(float delta) {
                fire.update(delta);
                fire2.update(delta);
                super.act(delta);
            }

            @Override
            public void setPosition(float x, float y) {
                super.setPosition(x, y);
                fire.setPosition(x + 5, y + 12);
                fire2.setPosition(x + 1, y + 27);
            }
        };

        Ship.setSize(51.2f, 38.4f);
        Ship.setPosition(35, 80);

        Table colorCustomisationTable = new Table();

        final float[] notEditedColors;
        final float[] originalColors = fire.getEmitters().get(0).getTint().getColors().clone();

        if (getString(particleEffect + "_color").equals("")) {
            writeFireColor(fire.getEmitters().get(0).getTint().getColors(), particleEffect);
            notEditedColors = originalColors;
        } else {
            float[] colors = new JsonReader().parse("{\"colors\":" + getString(particleEffect + "_color") + "}").get("colors").asFloatArray();
            notEditedColors = colors.clone();
            fire.getEmitters().get(0).getTint().setColors(colors);
            fire2.getEmitters().get(0).getTint().setColors(colors);
        }

        reset.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                writeFireColor(originalColors, particleEffect);
                fire.dispose();
                fire2.dispose();
                new ColorCustomizationDialogue(assetManager, particleEffect, stage);
                dialog.hide();
            }
        });

        final float[] colors = fire.getEmitters().get(0).getTint().getColors();
        final float[] colors2 = fire2.getEmitters().get(0).getTint().getColors();
        Array<Array<Float>> colorChannels = new Array<>();

        ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getInteger("item_crystal") >= 5) {
                    writeFireColor(colors, particleEffect);
                    if (!Arrays.equals(colors, notEditedColors)) {
                        subtractInteger("item_crystal", 5);
                    }
                    fire.dispose();
                    fire2.dispose();
                    dialog.hide();
                }
            }
        });

        Array<Slider> sliders = new Array<>();
        for (int i = 0; i < colors.length / 3; i++) {
            Array<Float> channel = new Array<>();
            channel.add(colors[3 * i], colors[3 * i + 1], colors[3 * i] + 2);
            colorChannels.add(channel);
            Table setHolder = new Table();

            final Image colorSquare = new Image(constructFilledImageWithColor(10, 10, Color.WHITE));
            colorSquare.setColor(new Color().add(colors[3 * i], colors[3 * i + 1], colors[3 * i + 2], 1));

            final Slider redSlider, greenSlider, blueSlider;

            redSlider = uiComposer.addSlider("sliderDefaultSmall", 0, 1, 0.01f);
            greenSlider = uiComposer.addSlider("sliderDefaultSmall", 0, 1, 0.01f);
            blueSlider = uiComposer.addSlider("sliderDefaultSmall", 0, 1, 0.01f);

            redSlider.setValue(colors[3 * i]);
            greenSlider.setValue(colors[3 * i + 1]);
            blueSlider.setValue(colors[3 * i + 2]);

            final Table redSliderTable = new Table();
            redSliderTable.add(uiComposer.addText("[#FF0000]R:", (BitmapFont) assetManager.get("fonts/font2(old).fnt"), 0.13f)).padLeft(1);
            redSliderTable.add(redSlider).width(35).padLeft(1);

            Table greenSliderTable = new Table();
            greenSliderTable.add(uiComposer.addText("[#00FF00]G:", (BitmapFont) assetManager.get("fonts/font2(old).fnt"), 0.13f)).padLeft(1);
            greenSliderTable.add(greenSlider).width(35).padLeft(1);
            greenSliderTable.add(colorSquare);

            Table blueSliderTable = new Table();
            blueSliderTable.add(uiComposer.addText("[#0000FF]B:", (BitmapFont) assetManager.get("fonts/font2(old).fnt"), 0.13f)).padLeft(1);
            blueSliderTable.add(blueSlider).width(35).padLeft(1);

            sliders.add(redSlider, greenSlider, blueSlider);

            final int finalI = i;
            redSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    colors[3 * finalI] = redSlider.getValue();
                    colors2[3 * finalI] = redSlider.getValue();
                    colorSquare.setColor(new Color().add(colors[3 * finalI], colors[3 * finalI + 1], colors[3 * finalI + 2], 1));
                }
            });

            greenSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    colors[3 * finalI + 1] = greenSlider.getValue();
                    colors2[3 * finalI + 1] = greenSlider.getValue();
                    colorSquare.setColor(new Color().add(colors[3 * finalI], colors[3 * finalI + 1], colors[3 * finalI + 2], 1));
                }
            });

            blueSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    colors[3 * finalI + 2] = blueSlider.getValue();
                    colors2[3 * finalI + 2] = blueSlider.getValue();
                    colorSquare.setColor(new Color().add(colors[3 * finalI], colors[3 * finalI + 1], colors[3 * finalI + 2], 1));
                }
            });

            setHolder.add(redSliderTable).align(Align.left).row();
            setHolder.add(greenSliderTable).align(Align.left).row();
            setHolder.add(blueSliderTable).align(Align.left).row();

            colorCustomisationTable.add(setHolder).padRight(5);
        }

        final ScrollPane colorCustomisationScrollPane = new ScrollPane(colorCustomisationTable);
        colorCustomisationScrollPane.setBounds(3, 30, 88, 39);
        colorCustomisationScrollPane.setCancelTouchFocus(false);

        for (int i = 0; i < sliders.size; i++) {
            sliders.get(i).addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    colorCustomisationScrollPane.cancel();
                }
            });
        }

        dialog.addActor(Ship);
        dialog.addActor(ok);
        dialog.addActor(close);
        dialog.addActor(colorCustomisationScrollPane);
        dialog.addActor(reset);
        dialog.add(crystal).padRight(5).padBottom(73);
        dialog.scaleBy(2);
        dialog.setSize(94, 128);
        dialog.setPosition(130, 78);
        stage.addActor(dialog);
    }

    private void writeFireColor(float[] colors, String particleEffect) {
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        buffer.append(colors[0]);
        for (int i = 1; i < colors.length; i++) {
            buffer.append(", ");
            buffer.append(colors[i]);
        }
        buffer.append("]");
        putString(particleEffect + "_color", buffer.toString());
    }

    private Array<ParticleEffect> loadFire(String particleEffect) {
        ParticleEffect fire = new ParticleEffect();
        ParticleEffect fire2 = new ParticleEffect();
        fire.load(Gdx.files.internal("particles/" + particleEffect + ".p"), Gdx.files.internal("particles"));
        fire2.load(Gdx.files.internal("particles/" + particleEffect + ".p"), Gdx.files.internal("particles"));
        fire.scaleEffect(0.7f);
        fire2.scaleEffect(0.7f);
        fire.start();
        fire2.start();
        Array<ParticleEffect> fires = new Array<>();
        fires.add(fire, fire2);
        return fires;
    }

}
