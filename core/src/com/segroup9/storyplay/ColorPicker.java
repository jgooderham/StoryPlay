package com.segroup9.storyplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ColorPicker extends Table {

    Slider rSlider, gSlider, bSlider, aSlider;
    Color color = new Color();
    Color defColor = new Color();
    Color copyColor = new Color();

    public ColorPicker(Color defaultColor, Skin skin) {
        defColor.set(defaultColor);

        rSlider = new Slider(0f, 1f, 0.01f, false, skin);
        rSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                color.set(rSlider.getValue(), color.g, color.b, color.a);
            }
        });
        row();
        add(new Label("Red:", skin));
        add(rSlider);

        gSlider = new Slider(0f, 1f, 0.01f, false, skin);
        gSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                color.set(color.r, gSlider.getValue(), color.b, color.a);
            }
        });
        row();
        add(new Label("Green:", skin));
        add(gSlider);

        bSlider = new Slider(0f, 1f, 0.01f, false, skin);
        bSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                color.set(color.r, color.g, bSlider.getValue(), color.a);
            }
        });
        row();
        add(new Label("Blue:", skin));
        add(bSlider);

        aSlider = new Slider(0f, 1f, 0.01f, false, skin);
        aSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                color.set(color.r, color.g, color.b, aSlider.getValue());
            }
        });
        row();
        add(new Label("Alpha:", skin));
        add(aSlider);

        row().colspan(2);
        TextButton btn = new TextButton("Reset to default", skin);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setValue(defColor);
            }
        });
        add(btn);

        row();
        TextButton copybtn = new TextButton("Copy Color", skin);
        copybtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MyGdxGame.copyboardColor.set(color);
            }
        });
        add(copybtn);
        TextButton pastebtn = new TextButton("Paste Color", skin);
        pastebtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setValue(MyGdxGame.copyboardColor);
            }
        });
        add(pastebtn);
    }

    public void setValue(Color newColor) {
        color.set(newColor);
        rSlider.setValue(color.r);
        gSlider.setValue(color.g);
        bSlider.setValue(color.b);
        aSlider.setValue(color.a);
    }
}