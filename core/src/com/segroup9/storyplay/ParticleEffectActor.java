package com.segroup9.storyplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ParticleEffectActor extends Actor {
    ParticleEffectPool.PooledEffect particleEffect;
    Vector2 acc = new Vector2();
    private boolean active = false;

    public ParticleEffectActor(ParticleEffectPool.PooledEffect particleEffect) {
        super();
        this.particleEffect = particleEffect;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
        particleEffect.draw(batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        acc.set(getWidth()/2, getHeight()/2);
        localToStageCoordinates(acc);
        particleEffect.setPosition(acc.x, acc.y);
        if (active)
            particleEffect.update(delta);
    }

    public void start() {
        active = true;
        particleEffect.start();
    }

    public void stop() {
        active = false;
        particleEffect.reset();
    }

    public void allowCompletion() {
        particleEffect.allowCompletion();
    }

    public void freeEffect() {
        particleEffect.free();
    }
}