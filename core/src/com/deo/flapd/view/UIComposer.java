package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;

class UIComposer {

    Array<Button.ButtonStyle> buttonStyles;
    Array<Slider.SliderStyle> sliderStyles;
    Array<CheckBox.CheckBoxStyle> checkBoxStyles;

    Array<String> buttonStyleNames;
    Array<String> sliderStyleNames;
    Array<String> checkBoxStyleNames;

    private AssetManager assetManager;

    UIComposer(AssetManager assetManager){
        this.assetManager = assetManager;
        buttonStyles = new Array<>();
        checkBoxStyleNames = new Array<>();
        checkBoxStyles = new Array<>();
        buttonStyleNames = new Array<>();
        sliderStyleNames = new Array<>();
        sliderStyles = new Array<>();
    }

    void loadStyles(String... styleNames){
        JsonValue styles = new JsonReader().parse(Gdx.files.internal("shop/styles.json"));
        Array<BitmapFont> fonts = new Array<>();
        String[] fontNames = styles.get("fonts").asStringArray();
        Array<String> dependencies = new Array<>();
        Skin textures = new Skin();
        for (int i = 0; i<fontNames.length; i++){
            BitmapFont font = assetManager.get(fontNames[i]);
            font.setUseIntegerPositions(false);
            fonts.add(font);
        }
        for(int i = 0; i<styleNames.length; i++) {
            loadStyle(styles, styleNames[i], textures, dependencies, fonts);
        }
    }

    private void loadStyle(JsonValue treeJson, String style, Skin textures, Array<String> dependencies, Array<BitmapFont> fonts){
        String dependency;
        if(treeJson.get(style) == null) {
            throw new IllegalArgumentException("No style defined with name " + style);
        }else{
            dependency = treeJson.get(style).get("loadFrom").asString();
        }
        if(!dependencies.contains(dependency, false)) {
            dependencies.add(dependency);
            textures.addRegions((TextureAtlas)assetManager.get(dependency));
        }
        switch (treeJson.get(style).get("type").asString()){
            case ("buttonStyle"):
                loadButtonStyle(treeJson.get(style), textures);
                break;
            case ("textButtonStyle"):
                loadTextButtonStyle(treeJson.get(style), textures, fonts);
                break;
            case("checkBoxStyle"):
                loadCheckBoxStyle(treeJson.get(style), textures, fonts);
                break;
            case("sliderStyle"):
                loadSliderStyle(treeJson.get(style), textures);
                break;
        }
    }

    private void loadButtonStyle(JsonValue style, Skin textures){
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        setButtonStyleDimensionsAndTextures(style, buttonStyle, textures);
        addButtonStyle(buttonStyle, style.name);
    }

    private void loadTextButtonStyle(JsonValue style, Skin textures, Array<BitmapFont> fonts){
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        setButtonStyleDimensionsAndTextures(style, buttonStyle, textures);
        setTextButtonFontStyle(buttonStyle, style, fonts);
        addButtonStyle(buttonStyle, style.name);
    }

    private void loadCheckBoxStyle(JsonValue style, Skin textures, Array<BitmapFont> fonts){
        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        setCheckBoxStyleDimensionsAndTextures(style, checkBoxStyle, textures);
        setCheckBoxFontStyle(checkBoxStyle, style, fonts);
        addCheckBoxStyleStyle(checkBoxStyle, style.name);
    }

    private void loadSliderStyle(JsonValue style, Skin textures){
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        setSliderStyleDimensionsAndTextures(style, sliderStyle, textures);
        addSliderStyle(sliderStyle, style.name);
    }

    private void setButtonStyleDimensionsAndTextures(JsonValue styleJson, Button.ButtonStyle style, Skin textures){

        style.up = textures.getDrawable(styleJson.get("up").get("texture").asString());
        style.over = textures.getDrawable(styleJson.get("over").get("texture").asString());
        style.down = textures.getDrawable(styleJson.get("down").get("texture").asString());

        if(!styleJson.get("up").get("size").asStringArray()[0].equals("default")){
            style.up.setMinWidth(styleJson.get("up").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("up").get("size").asStringArray()[1].equals("default")){
            style.up.setMinHeight(styleJson.get("up").get("size").asIntArray()[1]);
        }
        if(!styleJson.get("down").get("size").asStringArray()[0].equals("default")){
            style.down.setMinWidth(styleJson.get("down").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("down").get("size").asStringArray()[1].equals("default")){
            style.down.setMinHeight(styleJson.get("down").get("size").asIntArray()[1]);
        }
        if(!styleJson.get("over").get("size").asStringArray()[0].equals("default")){
            style.over.setMinWidth(styleJson.get("over").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("over").get("size").asStringArray()[1].equals("default")){
            style.over.setMinHeight(styleJson.get("over").get("size").asIntArray()[1]);
        }
    }

    private void setCheckBoxStyleDimensionsAndTextures(JsonValue styleJson, CheckBox.CheckBoxStyle style, Skin textures){

        style.checkboxOn = textures.getDrawable(styleJson.get("on").get("texture").asString());
        style.checkboxOnOver = textures.getDrawable(styleJson.get("onOver").get("texture").asString());
        style.checkboxOff = textures.getDrawable(styleJson.get("off").get("texture").asString());
        style.checkboxOver = textures.getDrawable(styleJson.get("offOver").get("texture").asString());

        if(!styleJson.get("on").get("size").asStringArray()[0].equals("default")){
            style.checkboxOn.setMinWidth(styleJson.get("on").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("on").get("size").asStringArray()[1].equals("default")){
            style.checkboxOn.setMinHeight(styleJson.get("on").get("size").asIntArray()[1]);
        }
        if(!styleJson.get("onOver").get("size").asStringArray()[0].equals("default")){
            style.checkboxOnOver.setMinWidth(styleJson.get("onOver").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("onOver").get("size").asStringArray()[1].equals("default")){
            style.checkboxOnOver.setMinHeight(styleJson.get("onOver").get("size").asIntArray()[1]);
        }
        if(!styleJson.get("off").get("size").asStringArray()[0].equals("default")){
            style.checkboxOff.setMinWidth(styleJson.get("off").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("off").get("size").asStringArray()[1].equals("default")){
            style.checkboxOff.setMinHeight(styleJson.get("off").get("size").asIntArray()[1]);
        }
        if(!styleJson.get("offOver").get("size").asStringArray()[0].equals("default")){
            style.checkboxOver.setMinWidth(styleJson.get("offOver").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("offOver").get("size").asStringArray()[1].equals("default")){
            style.checkboxOver.setMinHeight(styleJson.get("offOver").get("size").asIntArray()[1]);
        }
    }

    private void setTextButtonFontStyle(TextButton.TextButtonStyle buttonStyle, JsonValue styleJson, Array<BitmapFont> fonts){
        buttonStyle.font = fonts.get(styleJson.get("font").asInt());
        if(!styleJson.get("up").get("fontColor").asString().equals("default")) {
            buttonStyle.fontColor = Color.valueOf(styleJson.get("up").get("fontColor").asString());
        }
        if(!styleJson.get("over").get("fontColor").asString().equals("default")) {
            buttonStyle.overFontColor = Color.valueOf(styleJson.get("over").get("fontColor").asString());
        }
        if(!styleJson.get("down").get("fontColor").asString().equals("default")) {
            buttonStyle.downFontColor = Color.valueOf(styleJson.get("down").get("fontColor").asString());
        }
    }

    private void setSliderStyleDimensionsAndTextures(JsonValue styleJson, Slider.SliderStyle style, Skin textures){

        style.background = textures.getDrawable(styleJson.get("background").get("texture").asString());
        style.knob = textures.getDrawable(styleJson.get("knob").get("texture").asString());
        style.knobOver = textures.getDrawable(styleJson.get("knobOver").get("texture").asString());
        style.knobDown = textures.getDrawable(styleJson.get("knobDown").get("texture").asString());

        if(!styleJson.get("background").get("size").asStringArray()[0].equals("default")){
            style.background.setMinWidth(styleJson.get("background").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("background").get("size").asStringArray()[1].equals("default")){
            style.background.setMinHeight(styleJson.get("background").get("size").asIntArray()[1]);
        }
        if(!styleJson.get("knob").get("size").asStringArray()[0].equals("default")){
            style.knob.setMinWidth(styleJson.get("knob").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("knob").get("size").asStringArray()[1].equals("default")){
            style.knob.setMinHeight(styleJson.get("knob").get("size").asIntArray()[1]);
        }
        if(!styleJson.get("knobOver").get("size").asStringArray()[0].equals("default")){
            style.knobOver.setMinWidth(styleJson.get("knobOver").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("knobOver").get("size").asStringArray()[1].equals("default")){
            style.knobOver.setMinHeight(styleJson.get("knobOver").get("size").asIntArray()[1]);
        }
        if(!styleJson.get("knobDown").get("size").asStringArray()[0].equals("default")){
            style.knobDown.setMinWidth(styleJson.get("knobDown").get("size").asIntArray()[0]);
        }
        if(!styleJson.get("knobDown").get("size").asStringArray()[1].equals("default")){
            style.knobDown.setMinHeight(styleJson.get("knobDown").get("size").asIntArray()[1]);
        }
    }

    private void setCheckBoxFontStyle(CheckBox.CheckBoxStyle checkBoxStyle, JsonValue styleJson, Array<BitmapFont> fonts){
        checkBoxStyle.font = fonts.get(styleJson.get("font").asInt());
        if(!styleJson.get("off").get("fontColor").asString().equals("default")) {
            checkBoxStyle.fontColor = Color.valueOf(styleJson.get("off").get("fontColor").asString());
        }
        if(!styleJson.get("on").get("fontColor").asString().equals("default")) {
            checkBoxStyle.checkedFontColor = Color.valueOf(styleJson.get("on").get("fontColor").asString());
        }
        if(!styleJson.get("offOver").get("fontColor").asString().equals("default")) {
            checkBoxStyle.overFontColor = Color.valueOf(styleJson.get("offOver").get("fontColor").asString());
        }
        if(!styleJson.get("onOver").get("fontColor").asString().equals("default")) {
            checkBoxStyle.checkedOverFontColor = Color.valueOf(styleJson.get("onOver").get("fontColor").asString());
        }
    }

    Actor addScrollText(String text, BitmapFont font, float fontScale, boolean scrollable, boolean horizontal, float x, float y, float width, float height){
        font.getData().markupEnabled = true;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        Label label = new Label(text, labelStyle);
        label.setFontScale(fontScale);
        label.setAlignment(Align.center);
        ScrollPane scrollPane = new ScrollPane(label);
        if(scrollable) {
            scrollPane.setScrollingDisabled(!horizontal, horizontal);
        }else{
            scrollPane.setScrollingDisabled(true, true);
        }
        scrollPane.setBounds(x, y, width, height);
        Actor returnActor = scrollPane;
        if(!scrollable) {
            returnActor = label;
        }
        return returnActor;
    }

    public Label addText(String text, BitmapFont font, float fontScale){
        font.getData().markupEnabled = true;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        Label label = new Label(text, labelStyle);
        label.setFontScale(fontScale);
        label.setAlignment(Align.center);
        return label;
    }


    private void addButtonStyle(Button.ButtonStyle buttonStyle, String assignmentName){
        buttonStyles.add(buttonStyle);
        buttonStyleNames.add(assignmentName);
    }

    private void addSliderStyle(Slider.SliderStyle sliderStyle, String assignmentName){
        sliderStyles.add(sliderStyle);
        sliderStyleNames.add(assignmentName);
    }

    private void addCheckBoxStyleStyle(CheckBox.CheckBoxStyle checkBoxStyle, String assignmentName){
        checkBoxStyles.add(checkBoxStyle);
        checkBoxStyleNames.add(assignmentName);
    }

    Table addCheckBox(String style, String text, final String valueKey){
        if (checkBoxStyleNames.indexOf(style, false) == -1) throw new IllegalArgumentException("Style not loaded: "+style);
        Table cell = new Table();
        final CheckBox checkBox = new CheckBox(text, checkBoxStyles.get(checkBoxStyleNames.indexOf(style, false)));
        checkBox.getLabel().setFontScale(0.48f);
        checkBox.setChecked(getBoolean(valueKey));
        checkBox.getImageCell().padRight(5);
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                putBoolean(valueKey, checkBox.isChecked());
            }
        });
        cell.add(checkBox);
        return cell;
    }

    Table addSlider(String style, int min, int max, float step, final String text, final String postText, final String valueKey, final ScrollPane scrollHolder) {
        Table slider = addSlider(style, min, max, step, text, postText, valueKey);
        slider.getCells().get(0).getActor().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                scrollHolder.cancel();
            }
        });
        return slider;
    }

    Table addSlider(String style, int min, int max, float step, final String text, final String postText, final String valueKey){
        Table cell = new Table();
        final Slider slider = addSlider(style, min, max, step);
        slider.setValue(getFloat(valueKey));
        final Label textLabel = addText(text + String.format("%.1f", getFloat(valueKey)) + postText, (BitmapFont)assetManager.get("fonts/font2(old).fnt"), 0.48f);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                textLabel.setText(text + String.format("%.1f", slider.getValue()) + postText);
                putFloat(valueKey, slider.getValue());
            }
        });
        cell.add(slider).padRight(4);
        cell.add(textLabel);
        return cell;
    }

    Slider addSlider(String style, int min, int max, float step){
        if (sliderStyleNames.indexOf(style, false) == -1) throw new IllegalArgumentException("Style not loaded: "+style);
        return new Slider(min, max, step, false, sliderStyles.get(sliderStyleNames.indexOf(style, false)));
    }

    Button addButton(String style){
        if (buttonStyleNames.indexOf(style, false) == -1) throw new IllegalArgumentException("Style not loaded: "+style);
        return new Button(buttonStyles.get(buttonStyleNames.indexOf(style, false)));
    }

    Table addButton(String style, String text, float fontScale){
        Table table = new Table();
        table.add(addButton(style));
        table.add(addText(text, (BitmapFont)assetManager.get("fonts/font2(old).fnt"), fontScale));
        return table;
    }

    TextButton addTextButton(String style, String text, float fontScale){
        if (buttonStyleNames.indexOf(style, false) == -1) throw new IllegalArgumentException("Style not loaded: "+style);
        TextButton button = new TextButton(text, (TextButton.TextButtonStyle)buttonStyles.get(buttonStyleNames.indexOf(style, false)));
        button.getLabel().setSize(5, 5);
        button.getLabel().setFontScale(fontScale);
        return button;
    }

    Table addLinkButton(String style, String text, final String link){
        Table cell = addButton(style, text, 0.4f);
        cell.getCells().get(1).padLeft(5);
        cell.getCells().get(0).getActor().addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(link);
            }
        });
        return cell;
    }

    ScrollPane createScrollGroup(float x, float y, float width, float height, boolean horizontal, boolean vertical){
        ScrollPane scrollPane = new ScrollPane(new Table());
        scrollPane.setBounds(x, y, width, height);
        scrollPane.setScrollingDisabled(!horizontal, !vertical);
        return scrollPane;
    }

}
