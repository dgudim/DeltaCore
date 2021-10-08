package com.deo.flapd.view.dialogues;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
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
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.ui.UIComposer;

import java.util.Arrays;

import static com.deo.flapd.utils.DUtils.constructFilledImageWithColor;
import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putString;
import static com.deo.flapd.utils.DUtils.subtractInteger;
import static com.deo.flapd.view.screens.LoadingScreen.particleEffectPoolLoader;

public class ColorCustomizationDialogue extends Dialogue {
    
    private final Dialog dialog;
    public boolean drawFire = true;
    
    ColorCustomizationDialogue(CompositeManager compositeManager, final String particleEffect, final Stage stage) {
        
        AssetManager assetManager = compositeManager.getAssetManager();
        
        BitmapFont font = assetManager.get("fonts/font2(old).fnt");
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.13f * 2);
        font.getData().markupEnabled = true;
        
        Skin textures = new Skin();
        textures.addRegions(assetManager.get("shop/workshop.atlas"));
        final Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.background = textures.getDrawable("colorDialog");
        dialogStyle.titleFont = font;
        
        ParticleEffectPool.PooledEffect[] fires = loadFire(particleEffect);
        
        final ParticleEffectPool.PooledEffect fire = fires[0];
        final ParticleEffectPool.PooledEffect fire2 = fires[1];
        
        dialog = new Dialog("", dialogStyle);
        
        UIComposer uiComposer = compositeManager.getUiComposer();
        
        TextButton close = uiComposer.addTextButton("workshopRed", "cancel", 0.455f);
        TextButton ok = uiComposer.addTextButton("workshopGreen", "apply", 0.455f);
        TextButton reset = uiComposer.addTextButton("workshopGreen", "reset", 0.28f);
        
        close.setBounds(10.5f, 10.5f, 147, 87.5f);
        ok.setBounds(171.5f, 10.5f, 147, 87.5f);
        reset.setBounds(10.5f, 248.5f, 70, 24.5f);
        
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fire.free();
                fire2.free();
                drawFire = false;
                dialog.hide();
            }
        });
        TextureAtlas items = assetManager.get("items/items.atlas");
        Table crystal = new Table();
        ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
        itemButtonStyle.imageUp = new Image(items.findRegion("crystal")).getDrawable();
        itemButtonStyle.imageDisabled = new Image(items.findRegion("crystal_disabled")).getDrawable();
        itemButtonStyle.imageDown = new Image(items.findRegion("crystal_enabled")).getDrawable();
        itemButtonStyle.imageOver = new Image(items.findRegion("crystal_over")).getDrawable();
        ImageButton item = new ImageButton(itemButtonStyle);
        crystal.add(item).size(35, 35);
        Label quantity = uiComposer.addText(getInteger("item_crystal") + "/1", assetManager.get("fonts/font2(old).fnt"), 0.455f);
        
        if (getInteger("item_crystal") < 1) {
            quantity.setColor(Color.RED);
        } else {
            quantity.setColor(Color.YELLOW);
        }
        
        crystal.add(quantity);
        
        item.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new CraftingDialogue(compositeManager, stage, "coloring crystal");
            }
        });
        
        Image Ship = new Image(assetManager.get("items/items.atlas", TextureAtlas.class).findRegion("ship")) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                if(drawFire) {
                    fire.draw(batch);
                    fire2.draw(batch);
                }
                super.draw(batch, parentAlpha);
            }
            
            @Override
            public void act(float delta) {
                if(drawFire){
                    fire.update(delta);
                    fire2.update(delta);
                }
                super.act(delta);
            }
            
            @Override
            public void setPosition(float x, float y) {
                super.setPosition(x, y);
                fire.setPosition(x + 17.5f, y + 42);
                fire2.setPosition(x + 3.5f, y + 94.5f);
            }
        };
        
        Ship.setSize(179.2f, 134.4f);
        Ship.setPosition(122.5f, 280);
        
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
                fire.free();
                fire2.free();
                new ColorCustomizationDialogue(compositeManager, particleEffect, stage);
                dialog.hide();
            }
        });
        
        final float[] colors = fire.getEmitters().get(0).getTint().getColors();
        final float[] colors2 = fire2.getEmitters().get(0).getTint().getColors();
        Array<Array<Float>> colorChannels = new Array<>();
        
        ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getInteger("item_crystal") >= 1) {
                    writeFireColor(colors, particleEffect);
                    if (!Arrays.equals(colors, notEditedColors)) {
                        subtractInteger("item_crystal", 1);
                    }
                    fire.free();
                    fire2.free();
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
            
            final Image colorSquare = new Image(constructFilledImageWithColor(35, 35, Color.WHITE));
            colorSquare.setColor(new Color().add(colors[3 * i], colors[3 * i + 1], colors[3 * i + 2], 1));
            
            final Slider redSlider, greenSlider, blueSlider;
            
            redSlider = uiComposer.addSlider("sliderDefaultNormal", 0, 1, 0.01f);
            greenSlider = uiComposer.addSlider("sliderDefaultNormal", 0, 1, 0.01f);
            blueSlider = uiComposer.addSlider("sliderDefaultNormal", 0, 1, 0.01f);
            
            redSlider.setValue(colors[3 * i]);
            greenSlider.setValue(colors[3 * i + 1]);
            blueSlider.setValue(colors[3 * i + 2]);
            
            final Table redSliderTable = new Table();
            redSliderTable.add(uiComposer.addText("[#FF0000]R:", assetManager.get("fonts/font2(old).fnt"), 0.455f)).padLeft(3.5f);
            redSliderTable.add(redSlider).width(122.5f).padLeft(3.5f);
            
            Table greenSliderTable = new Table();
            greenSliderTable.add(uiComposer.addText("[#00FF00]G:", assetManager.get("fonts/font2(old).fnt"), 0.455f)).padLeft(3.5f);
            greenSliderTable.add(greenSlider).width(122.5f).padLeft(3.5f);
            greenSliderTable.add(colorSquare);
            
            Table blueSliderTable = new Table();
            blueSliderTable.add(uiComposer.addText("[#0000FF]B:", assetManager.get("fonts/font2(old).fnt"), 0.455f)).padLeft(3.5f);
            blueSliderTable.add(blueSlider).width(122.5f).padLeft(3.5f);
            
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
            
            colorCustomisationTable.add(setHolder).padRight(17.5f);
        }
        
        final ScrollPane colorCustomisationScrollPane = new ScrollPane(colorCustomisationTable);
        colorCustomisationScrollPane.setBounds(10.5f, 105, 308, 136.5f);
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
        dialog.add(crystal).padRight(15).padBottom(245);
        dialog.setSize(329, 448);
        dialog.setPosition(180, 15);
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
    
    private ParticleEffectPool.PooledEffect[] loadFire(String particleEffect) {
        ParticleEffectPool.PooledEffect fire = particleEffectPoolLoader.getParticleEffectByPath("particles/" + particleEffect + ".p");
        ParticleEffectPool.PooledEffect fire2 = particleEffectPoolLoader.getParticleEffectByPath("particles/" + particleEffect + ".p");
        fire.scaleEffect(2.45f);
        fire2.scaleEffect(2.45f);
        return new ParticleEffectPool.PooledEffect[]{fire, fire2};
    }
    
}
