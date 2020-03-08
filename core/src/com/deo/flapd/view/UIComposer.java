package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import static com.deo.flapd.utils.DUtils.getBoolean;
import static com.deo.flapd.utils.DUtils.getFloat;
import static com.deo.flapd.utils.DUtils.putBoolean;
import static com.deo.flapd.utils.DUtils.putFloat;
import static com.deo.flapd.utils.DUtils.putInteger;

public class UIComposer {

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


    void addButtonStyle(Button.ButtonStyle buttonStyle, String assignmentName){
        buttonStyles.add(buttonStyle);
        buttonStyleNames.add(assignmentName);
    }

    void addSliderStyle(Slider.SliderStyle sliderStyle, String assignmentName){
        sliderStyles.add(sliderStyle);
        sliderStyleNames.add(assignmentName);
    }

    void addCheckBoxStyleStyle(CheckBox.CheckBoxStyle checkBoxStyle, String assignmentName){
        checkBoxStyles.add(checkBoxStyle);
        checkBoxStyleNames.add(assignmentName);
    }

    Table addCheckBox(String style, String text, final String valueKey){
        Table cell = new Table();
        final CheckBox checkBox = new CheckBox(text, checkBoxStyles.get(checkBoxStyleNames.indexOf(style, false)));
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

    Table addSlider(String style, int min, int max, float step, final String text, final String postText, final String valueKey){
        Table cell = new Table();
        final Slider slider = new Slider(min, max, step, false, sliderStyles.get(sliderStyleNames.indexOf(style, false)));
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

    Button addButton(String style){
        return new Button(buttonStyles.get(buttonStyleNames.indexOf(style, false)));
    }

    Table addButton(String style, String text){
        Table table = new Table();
        table.add(new Button(buttonStyles.get(buttonStyleNames.indexOf(style, false))));
        table.add(addText(text, (BitmapFont)assetManager.get("fonts/font2(old).fnt"), 0.48f));
        return table;
    }

    TextButton addTextButton(String style, String text, float fontScale){
        TextButton button = new TextButton(text, (TextButton.TextButtonStyle)buttonStyles.get(buttonStyleNames.indexOf(style, false)));
        button.getLabel().setSize(5, 5);
        button.getLabel().setFontScale(fontScale);
        return button;
    }

    Table addLinkButton(String style, String text, final String link){
        Table cell = new Table();
        Button button = new Button(buttonStyles.get(buttonStyleNames.indexOf(style, false)));
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(link);
            }
        });
        cell.add(button);
        cell.add(addText(text, (BitmapFont)assetManager.get("fonts/font2(old).fnt"), 0.43f)).padLeft(5);
        return cell;
    }

    ScrollPane createScrollGroup(float x, float y, float width, float height, boolean horizontal, boolean vertical){
        ScrollPane scrollPane = new ScrollPane(new Table());
        scrollPane.setBounds(x, y, width, height);
        scrollPane.setScrollingDisabled(!horizontal, !vertical);
        return scrollPane;
    }

}
