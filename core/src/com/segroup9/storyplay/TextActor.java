package com.segroup9.storyplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class TextActor extends Group {
    private Label label;

    public TextActor(String text, Skin skin) {
        setTransform(true);
        label = new Label(text, skin, "place-holder");
        addActor(label);
        setText(text);
    }

    @Override
    public void setColor(Color color) {
        label.setColor(color);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a);
    }

    @Override
    public Color getColor() {
        return label.getColor();
    }

    @Override
    public void setScale(float scaleXY) {
        super.setScale(scaleXY);
        label.setFontScale(scaleXY);
        label.setWidth(scaleXY * 100);
    }

    public void setText(String text) {
        label.setText(text);
        this.setWidth(label.getWidth());
        this.setHeight(label.getHeight());
        this.setOrigin(0.5f*label.getWidth(),0.5f*label.getHeight());
    }
}
