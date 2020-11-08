package com.segroup9.storyplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;

public class StoryActorDef {
    String imageName;
    String text;
    float posX, posY, rotation, scale;
    Color color = Color.WHITE.cpy();
    String targetPage = "";
    Array<ActionDef> actionDefs = new Array<>();

    public StoryActorDef() {}

    public StoryActorDef(Actor actor) {
        getActorProps(actor);
        // by default add a smooth fade-in to all new actors
        ActionDef ad = new ActionDef();
        ad.type = ActionDef.ActionType.FadeIn;
        ad.params[0] = 0.1f;
        ad.params[1] = 1.0f;
        ad.interpType = ActionDef.InterpType.Smooth2;
        actionDefs.add(ad);
        actor.setUserObject(this);
    }

    // update this ActorDef with all current actor properties
    public void getActorProps(Actor actor) {
        imageName = actor.getName();
        posX = actor.getX();
        posY = actor.getY();
        rotation = actor.getRotation();
        if (actor instanceof Label)
            scale = ((Label) actor).getFontScaleX();
        else
            scale = actor.getScaleX();
        color.set(actor.getColor());
    }
}
