package com.segroup9.storyplay;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.esotericsoftware.spine.utils.SkeletonActor;

public class SetAnimAction extends Action {

    String animName;

    public SetAnimAction() {}
    public SetAnimAction(String name) {
        animName = name;
    }

    @Override
    public boolean act(float delta) {
        if (getActor() instanceof SkeletonActor) {
            SkeletonActor skeletonActor = (SkeletonActor)getActor();
            try {
                skeletonActor.getAnimationState().setAnimation(0, animName, true);
            } catch (IllegalArgumentException e) {
                System.out.println("Animation not found: " + animName);
            }
        }
        return true;
    }
}
