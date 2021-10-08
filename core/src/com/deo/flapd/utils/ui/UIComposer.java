package com.deo.flapd.utils.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.TimeUtils;
import com.deo.flapd.utils.CompositeManager;
import com.deo.flapd.utils.JsonEntry;
import com.deo.flapd.utils.SoundManager;

import java.util.Locale;

import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;

public class UIComposer {
    
    Array<Button.ButtonStyle> buttonStyles;
    Array<Slider.SliderStyle> sliderStyles;
    Array<CheckBox.CheckBoxStyle> checkBoxStyles;
    
    Array<String> buttonStyleNames;
    Array<String> sliderStyleNames;
    Array<String> checkBoxStyleNames;
    
    private final AssetManager assetManager;
    SoundManager soundManager;
    
    public UIComposer(CompositeManager compositeManager) {
        this.assetManager = compositeManager.getAssetManager();
        this.soundManager = compositeManager.getSoundManager();
        
        buttonStyles = new Array<>();
        checkBoxStyleNames = new Array<>();
        checkBoxStyles = new Array<>();
        buttonStyleNames = new Array<>();
        sliderStyleNames = new Array<>();
        sliderStyles = new Array<>();
    }
    
    public void loadStyles(String... styleNames) {
        
        log("Preparing to load styles...", DEBUG);
        long time = TimeUtils.millis();
        
        JsonEntry styles = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/styles.json")));
        
        Array<BitmapFont> fonts = new Array<>();
        String[] fontNames = styles.getStringArray(new String[]{"fonts/font2(old).fnt", "fonts/font2.fnt"}, "fonts");
        Array<String> dependencies = new Array<>();
        Skin textures = new Skin();
        
        for (String fontName : fontNames) {
            log("Loading font: " + fontName, DEBUG);
            BitmapFont font = assetManager.get(fontName);
            font.setUseIntegerPositions(false);
            fonts.add(font);
        }
        for (String styleName : styleNames) {
            log("Loading style: " + styleName, DEBUG);
            loadStyle(styles, styleName, textures, dependencies, fonts);
        }
        log("Loaded " + styleNames.length + " styles in " + TimeUtils.timeSinceMillis(time) + "ms", DEBUG);
    }
    
    private void loadStyle(JsonEntry treeJson, String style, Skin textures, Array<String> dependencies, Array<BitmapFont> fonts) {
        String dependency;
        if (treeJson.get(style).isNull()) {
            throw new IllegalArgumentException("No style defined with name " + style);
        } else {
            dependency = treeJson.getString("noStyleSpecified", style, "loadFrom");
        }
        if (!dependencies.contains(dependency, false)) {
            dependencies.add(dependency);
            textures.addRegions(assetManager.get(dependency));
        }
        switch (treeJson.getString("noStyleSpecified", style, "type")) {
            case ("buttonStyle"):
                loadButtonStyle(treeJson.get(style), textures);
                break;
            case ("textButtonStyle"):
                loadTextButtonStyle(treeJson.get(style), textures, fonts);
                break;
            case ("checkBoxStyle"):
                loadCheckBoxStyle(treeJson.get(style), textures, fonts);
                break;
            case ("sliderStyle"):
                loadSliderStyle(treeJson.get(style), textures);
                break;
        }
    }
    
    private void loadButtonStyle(JsonEntry style, Skin textures) {
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        setButtonStyleDimensionsAndTextures(style, buttonStyle, textures);
        addButtonStyle(buttonStyle, style.name);
    }
    
    private void loadTextButtonStyle(JsonEntry style, Skin textures, Array<BitmapFont> fonts) {
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        setButtonStyleDimensionsAndTextures(style, buttonStyle, textures);
        setTextButtonFontStyle(buttonStyle, style, fonts);
        addButtonStyle(buttonStyle, style.name);
    }
    
    private void loadCheckBoxStyle(JsonEntry style, Skin textures, Array<BitmapFont> fonts) {
        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        setCheckBoxStyleDimensionsAndTextures(style, checkBoxStyle, textures);
        setCheckBoxFontStyle(checkBoxStyle, style, fonts);
        addCheckBoxStyleStyle(checkBoxStyle, style.name);
    }
    
    private void loadSliderStyle(JsonEntry style, Skin textures) {
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        setSliderStyleDimensionsAndTextures(style, sliderStyle, textures);
        addSliderStyle(sliderStyle, style.name);
    }
    
    private void setButtonStyleDimensionsAndTextures(JsonEntry styleJson, Button.ButtonStyle style, Skin textures) {
        
        style.up = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "up", "texture")));
        style.over = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "over", "texture")));
        style.down = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "down", "texture")));
        
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "up", "size")[0].equals("default")) {
            style.up.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "up", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "up", "size")[1].equals("default")) {
            style.up.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "up", "size")[1]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "down", "size")[0].equals("default")) {
            style.down.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "down", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "down", "size")[1].equals("default")) {
            style.down.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "down", "size")[1]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "over", "size")[0].equals("default")) {
            style.over.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "over", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "over", "size")[1].equals("default")) {
            style.over.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "over", "size")[1]));
        }
    }
    
    private void setCheckBoxStyleDimensionsAndTextures(JsonEntry styleJson, CheckBox.CheckBoxStyle style, Skin textures) {
        
        style.checkboxOn = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "on", "texture")));
        style.checkboxOnOver = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "onOver", "texture")));
        style.checkboxOff = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "off", "texture")));
        style.checkboxOver = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "offOver", "texture")));
        
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "on", "size")[0].equals("default")) {
            style.checkboxOn.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "on", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "on", "size")[1].equals("default")) {
            style.checkboxOn.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "on", "size")[1]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "onOver", "size")[0].equals("default")) {
            style.checkboxOnOver.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "onOver", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "onOver", "size")[1].equals("default")) {
            style.checkboxOnOver.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "onOver", "size")[1]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "off", "size")[0].equals("default")) {
            style.checkboxOff.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "off", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "off", "size")[1].equals("default")) {
            style.checkboxOff.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "off", "size")[1]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "offOver", "size")[0].equals("default")) {
            style.checkboxOver.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "offOver", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "offOver", "size")[1].equals("default")) {
            style.checkboxOver.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "offOver", "size")[1]));
        }
    }
    
    private void setTextButtonFontStyle(TextButton.TextButtonStyle buttonStyle, JsonEntry styleJson, Array<BitmapFont> fonts) {
        buttonStyle.font = fonts.get(styleJson.getInt(0, "font"));
        if (!styleJson.getString("default", "up", "fontColor").equals("default")) {
            buttonStyle.fontColor = Color.valueOf(styleJson.getString("#FFFFFF", "up", "fontColor"));
        }
        if (!styleJson.getString("default", "over", "fontColor").equals("default")) {
            buttonStyle.overFontColor = Color.valueOf(styleJson.getString("#FFFFFF", "over", "fontColor"));
        }
        if (!styleJson.getString("default", "down", "fontColor").equals("default")) {
            buttonStyle.downFontColor = Color.valueOf(styleJson.getString("#FFFFFF", "down", "fontColor"));
        }
    }
    
    private void setSliderStyleDimensionsAndTextures(JsonEntry styleJson, Slider.SliderStyle style, Skin textures) {
        
        style.background = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "background", "texture")));
        style.background.setLeftWidth(0);
        style.background.setRightWidth(0);
        
        style.knob = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "knob", "texture")));
        style.knobOver = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "knobOver", "texture")));
        style.knobDown = new NinePatchDrawable(textures.getPatch(styleJson.getString("noTexture", "knobDown", "texture")));
        
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "background", "size")[0].equals("default")) {
            style.background.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "background", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "background", "size")[1].equals("default")) {
            style.background.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "background", "size")[1]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "knob", "size")[0].equals("default")) {
            style.knob.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "knob", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "knob", "size")[1].equals("default")) {
            style.knob.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "knob", "size")[1]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "knobOver", "size")[0].equals("default")) {
            style.knobOver.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "knobOver", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "knobOver", "size")[1].equals("default")) {
            style.knobOver.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "knobOver", "size")[1]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "knobDown", "size")[0].equals("default")) {
            style.knobDown.setMinWidth(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "knobDown", "size")[0]));
        }
        if (!styleJson.getStringArray(new String[]{"default", "default"}, "knobDown", "size")[1].equals("default")) {
            style.knobDown.setMinHeight(Float.parseFloat(styleJson.getStringArray(new String[]{"0", "0"}, "knobDown", "size")[1]));
        }
    }
    
    private void setCheckBoxFontStyle(CheckBox.CheckBoxStyle checkBoxStyle, JsonEntry styleJson, Array<BitmapFont> fonts) {
        checkBoxStyle.font = fonts.get(styleJson.getInt(0, "font"));
        if (!styleJson.getString("default", "off", "fontColor").equals("default")) {
            checkBoxStyle.fontColor = Color.valueOf(styleJson.getString("#FFFFFF", "off", "fontColor"));
        }
        if (!styleJson.getString("default", "on", "fontColor").equals("default")) {
            checkBoxStyle.checkedFontColor = Color.valueOf(styleJson.getString("#FFFFFF", "on", "fontColor"));
        }
        if (!styleJson.getString("default", "offOver", "fontColor").equals("default")) {
            checkBoxStyle.overFontColor = Color.valueOf(styleJson.getString("#FFFFFF", "offOver", "fontColor"));
        }
        if (!styleJson.getString("default", "onOver", "fontColor").equals("default")) {
            checkBoxStyle.checkedOverFontColor = Color.valueOf(styleJson.getString("#FFFFFF", "onOver", "fontColor"));
        }
    }
    
    public Actor addScrollText(String text, BitmapFont font, float fontScale, boolean scrollable, boolean horizontal, float x, float y, float width, float height) {
        font.getData().markupEnabled = true;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        Label label = new Label(text, labelStyle);
        label.setFontScale(fontScale);
        label.setAlignment(Align.center);
        ScrollPane scrollPane = new ScrollPane(label);
        if (scrollable) {
            scrollPane.setScrollingDisabled(!horizontal, horizontal);
        } else {
            scrollPane.setScrollingDisabled(true, true);
        }
        scrollPane.setBounds(x, y, width, height);
        Actor returnActor = scrollPane;
        if (!scrollable) {
            returnActor = label;
        }
        return returnActor;
    }
    
    public Label addText(String text, BitmapFont font, float fontScale) {
        font.getData().markupEnabled = true;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        Label label = new Label(text, labelStyle);
        label.setFontScale(fontScale);
        label.setAlignment(Align.center);
        return label;
    }
    
    
    private void addButtonStyle(Button.ButtonStyle buttonStyle, String assignmentName) {
        buttonStyles.add(buttonStyle);
        buttonStyleNames.add(assignmentName);
    }
    
    private void addSliderStyle(Slider.SliderStyle sliderStyle, String assignmentName) {
        sliderStyles.add(sliderStyle);
        sliderStyleNames.add(assignmentName);
    }
    
    private void addCheckBoxStyleStyle(CheckBox.CheckBoxStyle checkBoxStyle, String assignmentName) {
        checkBoxStyles.add(checkBoxStyle);
        checkBoxStyleNames.add(assignmentName);
    }
    
    public CheckBox addCheckBox(String style, String text, final String valueKey) {
        final CheckBox checkBox = addCheckBox(style, text);
        checkBox.setChecked(getBoolean(valueKey));
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                putBoolean(valueKey, checkBox.isChecked());
            }
        });
        return checkBox;
    }
    
    public CheckBox addCheckBox(String style, String text) {
        if (checkBoxStyleNames.indexOf(style, false) == -1)
            throw new IllegalArgumentException("Style not loaded: " + style);
        final CheckBox checkBox = new CheckBox(text, checkBoxStyles.get(checkBoxStyleNames.indexOf(style, false)));
        checkBox.getLabel().setFontScale(0.48f);
        checkBox.getImageCell().padRight(5);
        addClickSoundListener(checkBox);
        return checkBox;
    }
    
    public Table addSlider(String style, int min, int max, float step, final String text, final String postText, final String valueKey, final ScrollPane scrollHolder) {
        Table slider = addSlider(style, min, max, step, text, postText, valueKey);
        slider.getCells().get(0).getActor().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scrollHolder.cancel();
            }
        });
        return slider;
    }
    
    public Table addSlider(String style, int min, int max, float step, final String text, final String postText, final String valueKey) {
        Table cell = new Table();
        final Slider slider = addSlider(style, min, max, step);
        slider.setValue(getFloat(valueKey));
        final Label textLabel = addText(text + String.format(Locale.ROOT, "%.1f", getFloat(valueKey)) + postText, assetManager.get("fonts/font2(old).fnt"), 0.48f);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                textLabel.setText(text + String.format(Locale.ROOT, "%.1f", slider.getValue()) + postText);
                putFloat(valueKey, slider.getValue());
            }
        });
        cell.add(slider).padRight(4);
        cell.add(textLabel);
        return cell;
    }
    
    public Slider addSlider(String style, int min, int max, float step) {
        if (sliderStyleNames.indexOf(style, false) == -1)
            throw new IllegalArgumentException("Style not loaded: " + style);
        return new Slider(min, max, step, false, sliderStyles.get(sliderStyleNames.indexOf(style, false)));
    }
    
    public Button addButton(String style) {
        if (buttonStyleNames.indexOf(style, false) == -1)
            throw new IllegalArgumentException("Style not loaded: " + style);
        Button button = new Button(buttonStyles.get(buttonStyleNames.indexOf(style, false)));
        addClickSoundListener(button);
        return button;
    }
    
    public Table addButton(String style, String text, float fontScale) {
        Table table = new Table();
        table.add(addButton(style));
        table.add(addText(text, assetManager.get("fonts/font2(old).fnt"), fontScale));
        return table;
    }
    
    public TextButton addTextButton(String style, String text, float fontScale) {
        if (buttonStyleNames.indexOf(style, false) == -1)
            throw new IllegalArgumentException("Style not loaded: " + style);
        TextButton button = new TextButton(text, (TextButton.TextButtonStyle) buttonStyles.get(buttonStyleNames.indexOf(style, false)));
        button.getLabel().setSize(5, 5);
        button.getLabel().setFontScale(fontScale);
        addClickSoundListener(button);
        return button;
    }
    
    public Table addLinkButton(String style, String text, final String link) {
        Table cell = addButton(style, text, 0.4f);
        cell.getCells().get(1).padLeft(5);
        cell.getCells().get(0).getActor().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                soundManager.playSound_noLink("click");
                Gdx.net.openURI(link);
            }
        });
        return cell;
    }
    
    public ScrollPane createScrollGroup(float x, float y, float width, float height, boolean horizontal, boolean vertical) {
        ScrollPane scrollPane = new ScrollPane(new Table());
        scrollPane.setBounds(x, y, width, height);
        scrollPane.setScrollingDisabled(!horizontal, !vertical);
        return scrollPane;
    }
    
    private void addClickSoundListener(Actor actor) {
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                soundManager.playSound_noLink("click");
            }
        });
    }
    
}
