package com.segroup9.storyplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class ActionDef {

    enum ActionType { Delay, MoveTo, FadeIn, FadeOut, Sway };
    ActionType type = ActionType.Delay;
    float[] params = new float[6];
    enum InterpType { Linear, Smooth, Smooth2, Smoother, Bounce, BounceIn, BounceOut, Elastic, ElasticIn, ElasticOut,
        Exp, ExpIn, ExpOut, Pow, PowIn, PowOut, Swing, SwingIn, SwingOut };
    InterpType interpType = InterpType.Linear;

    public ActionDef() {}

    public Action getAction() {
        Interpolation interp;
        switch (interpType) {
            case Smooth:
                interp = Interpolation.smooth;
                break;
            case Smooth2:
                interp = Interpolation.smooth2;
                break;
            case Smoother:
                interp = Interpolation.smoother;
                break;
            case Bounce:
                interp = Interpolation.bounce;
                break;
            case BounceIn:
                interp = Interpolation.bounceIn;
                break;
            case BounceOut:
                interp = Interpolation.bounceOut;
                break;
            case Elastic:
                interp = Interpolation.elastic;
                break;
            case ElasticIn:
                interp = Interpolation.elasticIn;
                break;
            case ElasticOut:
                interp = Interpolation.elasticOut;
                break;
            case Exp:
                interp = Interpolation.exp5;
                break;
            case ExpIn:
                interp = Interpolation.exp5In;
                break;
            case ExpOut:
                interp = Interpolation.exp5Out;
                break;
            case Pow:
                interp = Interpolation.pow2;
                break;
            case PowIn:
                interp = Interpolation.pow2In;
                break;
            case PowOut:
                interp = Interpolation.pow2Out;
                break;
            case Swing:
                interp = Interpolation.swing;
                break;
            case SwingIn:
                interp = Interpolation.swingIn;
                break;
            case SwingOut:
                interp = Interpolation.swingOut;
                break;
            default:
                interp = Interpolation.linear;
        }

        Action action;
        switch (type) {
            case Sway:
                float x = 0.5f * params[1];
                int c = params[2] == -1 ? (int)params[2] : (int)(params[2] / params[1]);
                action = Actions.repeat(c, Actions.sequence(Actions.rotateBy(15, x, interp),
                        Actions.rotateBy(-15, x, interp)));
                break;
            case MoveTo:
                action = Actions.sequence(Actions.delay(params[0]),
                        Actions.moveTo(params[1] * Gdx.graphics.getWidth(),
                                params[2] * Gdx.graphics.getHeight(), params[3], interp));
                break;
            case FadeIn:
                action = Actions.sequence(Actions.alpha(0), Actions.delay(params[0]),
                        Actions.fadeIn(params[1], interp));
                break;
            case FadeOut:
                action = Actions.sequence(Actions.delay(params[0]), Actions.fadeOut(params[1], interp));
                break;
            default:
                action = Actions.delay(params[0]);
        }
        return action;
    }

    // create ui controls to edit the parameters for a particular action
    public Table getParamControls(Skin skin) {
        boolean hasInterp = false;
        String[] paramLabels = new String[] {"Duration:"};
        switch (type) {
            case FadeIn:
            case FadeOut:
                paramLabels = new String[] {"Delay:", "Duration:"};
                hasInterp = true;
                break;
            case MoveTo:
                paramLabels = new String[] {"Delay:", "X:", "Y:", "Duration:"};
                hasInterp = true;
                break;
            case Sway:
                paramLabels = new String[] {"Delay:", "Speed:", "Duration"};
                hasInterp = true;
                break;
        }
        // create the appropriate number of parameter boxes for the desired action type
        Table t = new Table();
        for (int i = 0; i < paramLabels.length; i++) {
            final int index = i;
            TextField tf = new TextField("0", skin);
            tf.setTextFieldFilter(new FloatFilter());
            tf.setText(String.valueOf(params[i]));
            tf.addCaptureListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        params[index] = Float.parseFloat(((TextField) actor).getText());
                    } catch (Exception e) {
                        params[index] = 0;
                    }
                }
            });
            t.row().expand().fill().pad(2);
            Label lbl = new Label(paramLabels[i], skin);
            lbl.setAlignment(Align.right);
            t.add(lbl);
            t.add(tf);
        }

        // setup interpolation type select box for actions that can specify an interpolation type
        if (hasInterp) {
            final SelectBox<InterpType> interpBox = new SelectBox<>(skin);
            interpBox.setItems(InterpType.values());
            interpBox.addCaptureListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    interpType = interpBox.getSelected();
                }
            });
            interpBox.setSelected(interpType);
            t.row().expand().fill().pad(2);
            Label lbl = new Label("Interpolation:", skin);
            lbl.setAlignment(Align.right);
            t.add(lbl);
            t.add(interpBox);
        }
        return t;
    }

    // text filter for float characters to constrain input on parameter boxes
    static public class FloatFilter implements TextField.TextFieldFilter {
        public boolean acceptChar (TextField textField, char c) {
            return Character.isDigit(c) || c == '.' || (c == '-' && textField.getText().length() == 0);
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case MoveTo:
                return "Wait " + params[0] + " secs then " + type + ": (" + params[1] + ", " + params[2] +
                        ") taking " + params[3] + " secs";
            case FadeIn:
            case FadeOut:
                return "Wait " + params[0] + " secs then " + type + " for " + params[1] + " secs";
            case Sway:
                return "Wait " + params[0] + " secs then " + type + " for" +
                        (params[2] == -1 ? "ever" : " " + params[2] + " secs");
            default:
                return "Wait " + params[0];
        }
    }
}
