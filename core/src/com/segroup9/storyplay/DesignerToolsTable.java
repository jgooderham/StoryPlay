package com.segroup9.storyplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class DesignerToolsTable extends Table {

    private final Label pageNumLbl, pageNameLbl;
    private final Label helpLbl;
    private StoryPlay storyPlay;

    public DesignerToolsTable(Skin skin, final StoryPlay storyPlay) {
        this.storyPlay = storyPlay;

        top().row().fill().expandX().pad(5);
        setFillParent(true);

        TextButton reloadFileBtn = new TextButton("Reload from File", skin);
        reloadFileBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                storyPlay.loadFromFile(true);
            }
        });
        add(reloadFileBtn);

        TextButton saveFileBtn = new TextButton("Save to File", skin);
        saveFileBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                storyPlay.saveToFile();
            }
        });
        add(saveFileBtn);

        TextButton insertPgBtn = new TextButton("Insert Page", skin);
        insertPgBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (storyPlay.isLive())
                    storyPlay.setLive(false);
                storyPlay.saveCurrentPage();
                storyPlay.insertPage(false);
                updatePageLabels();
            }
        });
        add(insertPgBtn);

        TextButton removePgBtn = new TextButton("Remove Page", skin);
        removePgBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (storyPlay.isLive())
                    storyPlay.setLive(false);
                storyPlay.removeCurrentPage();
                pageNumLbl.setText(storyPlay.getPageLabel());
                pageNameLbl.setText(storyPlay.getPageName());
            }
        });
        add(removePgBtn);

        TextButton appendPgBtn = new TextButton("Append Page", skin);
        appendPgBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (storyPlay.isLive())
                    storyPlay.setLive(false);
                storyPlay.saveCurrentPage();
                storyPlay.insertPage(true);
                pageNumLbl.setText(storyPlay.getPageLabel());
                pageNameLbl.setText(storyPlay.getPageName());
            }
        });
        add(appendPgBtn);

        TextButton prevPgBtn = new TextButton("<< Previous Page", skin);
        prevPgBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (storyPlay.isLive())
                    storyPlay.setLive(false);
                storyPlay.saveCurrentPage();
                storyPlay.prevPage();
                pageNumLbl.setText(storyPlay.getPageLabel());
                pageNameLbl.setText(storyPlay.getPageName());
            }
        });
        add(prevPgBtn);

        pageNumLbl = new Label("", skin);
        pageNumLbl.setAlignment(Align.center);
        add(pageNumLbl);

        pageNameLbl = new Label("", skin);
        pageNameLbl.setAlignment(Align.center);
        add(pageNameLbl);
        updatePageLabels();

        TextButton nextPgBtn = new TextButton("Next Page >>", skin);
        nextPgBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (storyPlay.isLive())
                    storyPlay.setLive(false);
                storyPlay.saveCurrentPage();
                storyPlay.nextPage();
                pageNumLbl.setText(storyPlay.getPageLabel());
                pageNameLbl.setText(storyPlay.getPageName());
            }
        });
        add(nextPgBtn);

        helpLbl = new Label("A: Add new actor\n" +
                "R: Hold to rotate selected actor\n" +
                "S: Hold to scale selected actor\n" +
                "F: Flip actor horizontally\n" +
                "X: Delete selected actor\n" +
                "[,]: Shift draw order of selected actor\n" +
                "P: Edit selected actor's properties\n" +
                "MouseWheel: Scroll through available actors\n" +
                "Left Click: Select actor on stage\n" +
                "N: Edit page name\n" +
                "M: Edit page narration text\n" +
                "<spacebar>: Play from storyplay current page\n" +
                "H: Hide/Show this help menu", skin);
        helpLbl.setTouchable(Touchable.disabled);
        row().bottom().left().expandY();
        add(helpLbl);
    }

    public void updatePageLabels() {
        pageNumLbl.setText(storyPlay.getPageLabel());
        if (!storyPlay.getPageName().equals(""))
            pageNameLbl.setText("Page Name: " + storyPlay.getPageName());
    }

    public void toggleHelp() { helpLbl.setVisible(!helpLbl.isVisible()); }
}
