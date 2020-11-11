package com.segroup9.storyplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import java.util.HashMap;

public class StoryPlay extends Group {
    private Array<StoryPage> pages;
    private int currentPage = 0;
    private final TextureAtlas textureAtlas;
    private final Skin skin;
    private boolean live = false;
    private final Group group;
    private final Actor bgColorActor;
    private final HashMap<String, ParticleEffectPool> particleFX;

    public StoryPlay(TextureAtlas atlas, Skin skin, HashMap<String, ParticleEffectPool> particles) {
        textureAtlas = atlas;
        this.skin = skin;
        particleFX = particles;

        group = new Group();
        bgColorActor = new Actor();
        pages = new Array<>();
        pages.add(new StoryPage());
        super.addActor(bgColorActor);
        super.addActor(group);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    public void setLive(boolean isLive) {
        live = isLive;
        gotoPage(currentPage);
    }

    public boolean isLive() {
        return live;
    }

    public void saveCurrentPage() {
        // save current page actors
        StoryPage page = pages.get(currentPage);
        page.actorDefs.clear();
        for (Actor child : group.getChildren())
            page.add(child);
    }

    private void loadPageActors() {
        // particle actors need to free their PooledEffect before being removed
        for (Actor actor : group.getChildren())
            if (actor instanceof ParticleEffectActor)
                ((ParticleEffectActor)actor).freeEffect();

        // remove all actors from stage and load current page
        group.clearChildren();
        StoryPage page = pages.get(currentPage);

        // set new page color, smooth transition if live
        if (live) {
            bgColorActor.clearActions();
            bgColorActor.addAction(Actions.color(page.backgroundColor, 1, Interpolation.smooth2));
        } else
            bgColorActor.setColor(page.backgroundColor);

        // load page actors to stage and initialize
        for (final StoryActorDef actorDef : page.actorDefs) {
            final Actor actor;
            TextureAtlas.AtlasRegion reg = textureAtlas.findRegion(actorDef.imageName);
            if (reg == null) {
                System.out.println("Image missing!: " + actorDef.imageName);
                reg = textureAtlas.findRegion("img-missing");
            }

            if ("".equals(actorDef.text) || actorDef.text == null)
                actor = new Image(reg);
            else {
                if (live && particleFX.containsKey(actorDef.text)) {
                    ParticleEffectActor p = new ParticleEffectActor(particleFX.get(actorDef.text).obtain());
                    p.stop();
                    actor = p;
                } else {
                    actor = new TextActor(actorDef.text, skin);
                }
            }

            actor.setUserObject(actorDef);
            actor.setOrigin(Align.center); // center actor origin (default is lower left corner)
            actor.setName(actorDef.imageName);
            actor.setPosition(actorDef.posX, actorDef.posY);
            actor.setRotation(actorDef.rotation);
            actor.setScale(actorDef.scale, Math.abs(actorDef.scale));
            actor.setColor(actorDef.color);

            // if we're live, setup actions on the actor
            if (live) {
                // these are the actions defined on the actor
                SequenceAction seq = Actions.sequence();
                for (ActionDef actionDef : actorDef.actionDefs)
                    seq.addAction(actionDef.getAction());

                // if it's
                if (actor instanceof ParticleEffectActor)
                    seq.addAction(Actions.run(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      ((ParticleEffectActor)actor).start();
                                                  }
                                              }
                    ));

                // if actor has a target page, make it a button that links to said page
                if (!"".equals(actorDef.targetPage)) {

                    // disable actor touch until after other actions have completed
                    actor.setTouchable(Touchable.disabled);
                    seq.addAction(Actions.touchable(Touchable.enabled));

                    // throb actor to indicate it is a button now
                    float s = actor.getScaleY();
                    seq.addAction(Actions.forever(Actions.sequence(
                            Actions.scaleTo(s*1.1f, s*1.1f, 1f, Interpolation.smooth),
                            Actions.scaleTo(s*0.9f, s*0.9f, 1f, Interpolation.smooth))));
                    actor.addCaptureListener(new ClickListener() {
                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                            StoryPlay.this.gotoPage(lookUpPageName(actorDef.targetPage));
                            return true;
                        }
                    });
                }
                actor.addAction(seq);
            }
            group.addActor(actor);
        }

        // if we're live, display the page's narration text
        if (live) {
            TextArea nar = new TextArea(getPageNarration(), skin, "narration");
            nar.setSize(Gdx.graphics.getWidth() - 80f, Gdx.graphics.getHeight());
            nar.setPosition(40f, -40f);
            nar.setTouchable(Touchable.disabled);
            nar.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.5f)));
            group.addActor(nar);
        }
    }

    // use negative index to reload current page
    public void gotoPage(int index) {
        if (index >= 0)
            currentPage = (index + pages.size) % pages.size;
        loadPageActors();
    }

    public void nextPage() {
        gotoPage(currentPage+1);
    }

    public void prevPage() {
        gotoPage(currentPage-1);
    }

    public void insertPage(boolean after) {
        if (after) {
            if (currentPage + 1 >= pages.size)
                pages.add(new StoryPage());
            else
                pages.insert(currentPage + 1, new StoryPage());
            gotoPage(currentPage + 1);
        } else {
            pages.insert(currentPage, new StoryPage());
            gotoPage(currentPage);
        }
    }

    public void removeCurrentPage() {
        pages.removeIndex(currentPage);
        if (pages.size == 0)
            pages.add(new StoryPage());
        gotoPage(currentPage);
    }

    public String getPageName() { return pages.get(currentPage).name; }
    public void setPageName(String name) { pages.get(currentPage).name = name; }
    public Color getBGColor() { return bgColorActor.getColor(); }
    public Color getPageColor() { return pages.get(currentPage).backgroundColor; }
    public void setPageColor(Color color) {
        bgColorActor.setColor(color);
        pages.get(currentPage).backgroundColor.set(color);
    }

    private int lookUpPageName(String searchName) {
        for (int i = 0; i < pages.size; i++) {
            if (pages.get(i).name.equals(searchName))
                return i;
        }
        return 0;
    }

    @Override
    public void addActor(Actor actor) {
        group.addActor(actor);
        pages.get(currentPage).add(actor);
    }

    // helper method for ui
    public String getPageLabel() {
        return "Page: " + (currentPage+1) + "/" + pages.size;
    }

    public String getPageNarration() {
        return pages.get(currentPage).narration;
    }
    public void setPageNarration(String newNarration) {
        pages.get(currentPage).narration = newNarration;
    }

    public void saveToFile() {
        if (isLive())
            setLive(false);
        saveCurrentPage();
        Json json = new Json();
        json.toJson(pages, Gdx.files.local("storyplay.json"));
    }

    public void loadFromFile() {
        Json json = new Json();
        pages = json.fromJson(Array.class, Gdx.files.local("storyPlay.json"));
        loadPageActors();
    }
}
