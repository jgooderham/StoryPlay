package com.segroup9.storyplay;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;

import java.util.HashMap;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
	public static int SCREEN_WIDTH = 1280;
	public static int SCREEN_HEIGHT = 800;
	public static Color copyboardColor = new Color();
	public static ActionDef copyboardAction = new ActionDef();

	Stage stage;
	StoryPlay storyPlay;
	TextureAtlas atlas;
	Array<AtlasRegion> atlasRegions;
	Skin skin;
	HashMap<String, ParticleEffectPool> particleFX;
	HashMap<String, SkeletonData> spineSkeletons;

	boolean designerMode = false;
	Actor selectedActor;
	Image nextActor;
	int nextActorRegIndex = 0;
	Vector2 tmpPt = new Vector2();
	Vector2 downPt = new Vector2();
	boolean scaling = false, rotating = false;
	float initialValue;
	Vector2 initialPt = new Vector2();
	Color tmpColor = new Color();
	ColorAction colorThrobAction;

	// ui widgets to keep track of
	Table actionParamsTbl;
	DesignerToolsTable desToolsTbl;
	Dialog pgNameDlg, pgNarrationDlg, actionsDlg;
	TextArea narrationTA;
	TextField pgNameTF, targetPageTF, actorTextTF;
	List<ActionDef> actionsList;
	SelectBox<ActionDef.ActionType> actionTypeSB;
	ColorPicker pageCP, actorCP;

	Dialog exitDlg;
	private boolean canExit = false;

	@Override
	public void create () {
		// create the atlas for all our images
		atlas = new TextureAtlas(Gdx.files.internal("sprites.atlas"));
		atlasRegions = atlas.getRegions();

		// load all the particle effects
		particleFX = new HashMap<String, ParticleEffectPool>();
		String[] particleFiles = { "bigbubbles.p", "bubbles.p", "fish.p", "elephants.p" };
		for (String filename : particleFiles) {
			ParticleEffect p = new ParticleEffect();
			p.load(Gdx.files.internal("particleFX/" + filename), atlas);
			particleFX.put(filename, new ParticleEffectPool(p, 5, 5));
		}

		// load all the spine skeletons
		spineSkeletons = new HashMap<>();
		SkeletonBinary json = new SkeletonBinary(atlas);
		String[] skeletonFiles = { "helicopter.skel", "diver.skel", "sub.skel", "bird.skel", "digger.skel" };
		for(String filename : skeletonFiles) {
			SkeletonData skel = json.readSkeletonData(Gdx.files.internal("spine/" + filename));
			spineSkeletons.put(filename, skel);
		}

		// load the ui skin graphics, should only need this for designer mode once narration is added later
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		// no nice way to resize fonts within the skin file so we do it here for the narration style...
		skin.getFont("font-narration").getData().setScale(0.7f, 0.7f);

		designerMode = Gdx.app.getType() == Application.ApplicationType.Desktop;

		// setup the stage and storyplay
		stage = new Stage(new ScalingViewport(Scaling.fit, MyGdxGame.SCREEN_WIDTH, MyGdxGame.SCREEN_HEIGHT), new PolygonSpriteBatch());
		storyPlay = new StoryPlay(atlas, skin, particleFX, spineSkeletons);
		storyPlay.setLive(!designerMode);
		try {
			storyPlay.loadFromFile(designerMode);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		storyPlay.gotoPage(0);
		stage.addActor(storyPlay);

		// enable processing input (mouse, touch)
		Gdx.input.setInputProcessor(this);

		// setup designer mode
		if (designerMode) {
			// nextActor is a preview image that follows the mouse around
			nextActor = new Image(atlasRegions.first());
			nextActor.setTouchable(Touchable.disabled);
			nextActor.setColor(1, 1, 1, 0);
			nextActor.setVisible(false);
			stage.addActor(nextActor);

			// create a widget table that occupies the top of the screen
			desToolsTbl = new DesignerToolsTable(skin, storyPlay);
			stage.addActor(desToolsTbl);

			// create dialog to edit page name
			pgNameDlg = okayDialog("Page Name", new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					storyPlay.setPageName(pgNameTF.getText());
					desToolsTbl.updatePageLabels();
					pgNameDlg.hide();
					Gdx.input.setInputProcessor(MyGdxGame.this);
				}
			}, new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					pgNameDlg.hide();
					Gdx.input.setInputProcessor(MyGdxGame.this);
				}
			});
			pgNameTF = new TextField("", skin);
			Table ct = pgNameDlg.getContentTable();
			ct.row().fill().expand().pad(5);
			ct.add(pgNameTF);
			pgNameDlg.setModal(true);

			// create dialog to edit page narration text
			pgNarrationDlg = okayDialog("Page Narration", new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					storyPlay.setPageNarration(narrationTA.getText());
					pgNarrationDlg.hide();
					Gdx.input.setInputProcessor(MyGdxGame.this);
				}
			}, new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					pgNarrationDlg.hide();
					storyPlay.setPageColor(tmpColor);	// reset page color on cancel
					Gdx.input.setInputProcessor(MyGdxGame.this);
				}
			});
			narrationTA = new TextArea("", skin);
			pageCP = new ColorPicker(new Color(0.25f, 0.25f, 0.25f, 1), skin);
			pageCP.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					storyPlay.setPageColor(pageCP.color);
				}
			});
			ct = pgNarrationDlg.getContentTable();
			ct.row().fill().expand().pad(5);
			ct.add(narrationTA);
			ct.row().colspan(2);
			ct.add(new Label("Background Color", skin));
			ct.row();
			ct.add(pageCP);
			pgNarrationDlg.setModal(true);

			// create dialog to edit actor actions
			actionsDlg = okayDialog("Action List", new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					// save list action defs onto actor
					StoryActorDef actorDef = ((StoryActorDef)selectedActor.getUserObject());
					actorDef.actionDefs = new Array<ActionDef>(actionsList.getItems());
					selectedActor.setColor(tmpColor.set(colorThrobAction.getEndColor()));
					actionsDlg.hide();
					Gdx.input.setInputProcessor(MyGdxGame.this);
				}
			}, new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					colorThrobAction.setEndColor(tmpColor);
					selectedActor.setColor(tmpColor);
					actionsDlg.hide();
					Gdx.input.setInputProcessor(MyGdxGame.this);
				}
			});
			actionsDlg.setModal(true);
			actionsList = new List<>(skin);
			actionsList.addCaptureListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					ActionDef l = actionsList.getSelected();
					if (l != null) {
						actionTypeSB.setSelected(l.type);
						actionParamsTbl.clearChildren();
						actionParamsTbl.add(l.getParamControls(skin));
					}
				}
			});
			actionTypeSB = new SelectBox<>(skin);
			actionTypeSB.setItems(ActionDef.ActionType.values());
			actionTypeSB.addCaptureListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					ActionDef sel = actionsList.getSelected();
					if (sel != null)
						actionsList.getSelected().type = actionTypeSB.getSelected();
				}
			});
			TextButton actionsAdd = new TextButton("+", skin);
			actionsAdd.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					actionsList.getItems().add(new ActionDef());
					actionsList.setItems(actionsList.getItems()); // must re-set items to validate list display
				}
			});
			TextButton actionsRemove = new TextButton("-", skin);
			actionsRemove.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					actionsList.getItems().removeIndex(actionsList.getSelectedIndex());
					actionsList.setItems(actionsList.getItems()); // must re-set items to validate list display
				}
			});
			TextButton actionCopyBtn = new TextButton("Copy Action", skin);
			actionCopyBtn.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					copyboardAction.set(actionsList.getSelected());
				}
			});
			TextButton actionPasteBtn = new TextButton("Paste Action", skin);
			actionPasteBtn.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					actionsList.getSelected().set(copyboardAction);
				}
			});

			targetPageTF = new TextField("", skin);
			targetPageTF.addCaptureListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					StoryActorDef actorDef = ((StoryActorDef)selectedActor.getUserObject());
					actorDef.targetPage = targetPageTF.getText();
				}
			});
			actorTextTF = new TextField("", skin);
			actorTextTF.addCaptureListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					StoryActorDef actorDef = ((StoryActorDef)selectedActor.getUserObject());
					actorDef.text = actorTextTF.getText();
					if (selectedActor instanceof TextActor)
						((TextActor) selectedActor).setText(actorTextTF.getText());
					storyPlay.gotoPage(-1);    // reload page to update actor text
				}
			});
			actionParamsTbl = new Table();

			// layout the controls created above
			ct = actionsDlg.getContentTable();
			ct.row().expand().fill().pad(2);
			ct.add(new ScrollPane(actionsList));
			Table t = new Table();
			ct.add(t);
			t.row().fill();
			actionsAdd.top();
			t.add(actionsAdd);
			t.add(actionsRemove);
			t.row().expandX().fill().pad(2);
			Label lbl = new Label("Action Type:", skin);
			lbl.setAlignment(Align.right);
			t.add(lbl);
			t.add(actionTypeSB);
			t.row().colspan(2);
			t.add(actionParamsTbl);
			t.row().expandX().fill().pad(2);
			t.add(actionCopyBtn);
			t.add(actionPasteBtn);
			t.row().expandX().fill().pad(2);
			lbl = new Label("Target Page:", skin);
			lbl.setAlignment(Align.right);
			t.add(lbl);
			t.add(targetPageTF);
			t.row().expandX().fill().pad(2);
			lbl = new Label("Actor Text:", skin);
			lbl.setAlignment(Align.right);
			t.add(lbl);
			t.add(actorTextTF);
			actorCP = new ColorPicker(Color.WHITE, skin);
			actorCP.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					colorThrobAction.setEndColor(actorCP.color);
				}
			});
			t.row().colspan(2);
			t.add(new Label("Actor Color", skin));
			t.row().colspan(2);
			t.add(actorCP);

			exitDlg = new Dialog("Save to file and exit?", skin);
			TextButton saveButton = new TextButton("Save", skin);
			saveButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					storyPlay.saveToFile();
					canExit = true;
					Gdx.app.exit();
				}
			});
			exitDlg.getButtonTable().add(saveButton);
			TextButton exitButton = new TextButton("Don't Save", skin);
			exitButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					canExit = true;
					Gdx.app.exit();
				}
			});
			exitDlg.getButtonTable().add(exitButton);
			TextButton cancelButton = new TextButton("Cancel", skin);
			cancelButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					exitDlg.hide();
					Gdx.input.setInputProcessor(MyGdxGame.this);
				}
			});
			exitDlg.getButtonTable().add(cancelButton);
		}
	}

	private Dialog okayDialog(String name, ChangeListener okChange, ChangeListener cancelChange) {
		Dialog dlg = new Dialog(name, skin);
		TextButton okButton = new TextButton("OK", skin);
		okButton.addListener(okChange);
		dlg.getButtonTable().add(okButton);
		TextButton cancelButton = new TextButton("Cancel", skin);
		cancelButton.addListener(cancelChange);
		dlg.getButtonTable().add(cancelButton);
		return dlg;
	}

	@Override
	public void render () {
		// clear the screen to page color
		Color c = storyPlay.getBGColor();
		Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// update the stage actions and render it
		stage.act();
		stage.draw();
	}

	@Override
	public void dispose () {
		// cleanup
		stage.dispose();
		atlas.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height);
	}

	public boolean canExit() {
		return canExit;
	}

	public void tryExit() {
		actionsDlg.setModal(true);
		exitDlg.show(stage);
	}

	@Override
	public boolean keyDown(int keycode) {

		if (designerMode) {
			// play/stop storyplay from current page
			if (keycode == Input.Keys.SPACE) {
				if (!storyPlay.isLive()) {
					deselectActor();
					storyPlay.saveCurrentPage();	// save page before playing
					desToolsTbl.setVisible(false);	// hide dev ui when playing
				} else {
					desToolsTbl.setVisible(true);
				}
				storyPlay.setLive(!storyPlay.isLive());
			}

			// add new actor at current mouse position
			if (keycode == Input.Keys.A) {
				AtlasRegion region = atlasRegions.get(nextActorRegIndex);
				Image actor = new Image(region);
				actor.setOrigin(Align.center); // center actor origin (default is lower left corner)
				stage.screenToStageCoordinates(tmpPt.set(Gdx.input.getX(), Gdx.input.getY()));
				actor.setPosition(tmpPt.x, tmpPt.y);
				actor.setName(region.name); // name actor after texture region, for atlas lookup when reloaded
				storyPlay.addActor(actor);
				flashNextActor();
			}

			// bring up narration text dialog for current page
			if (keycode == Input.Keys.N) {
				pgNameTF.setText(storyPlay.getPageName());
				pgNameDlg.show(stage);
				pgNameDlg.setSize(0.3f * Gdx.graphics.getWidth(), 0.3f * Gdx.graphics.getHeight());
				pgNameDlg.setPosition(
						0.5f * Gdx.graphics.getWidth(), 0.5f * Gdx.graphics.getHeight(), Align.center);
				Gdx.input.setInputProcessor(stage);
				stage.setKeyboardFocus(pgNameDlg);
			}

			// bring up narration text dialog for current page
			if (keycode == Input.Keys.M) {
				deselectActor();
				narrationTA.setText(storyPlay.getPageNarration());
				pageCP.setValue(tmpColor.set(storyPlay.getPageColor()));
				pgNarrationDlg.show(stage);
				pgNarrationDlg.setSize(0.5f * Gdx.graphics.getWidth(), 0.5f * Gdx.graphics.getHeight());
				pgNarrationDlg.setPosition(
						0.5f * Gdx.graphics.getWidth(), 0.5f * Gdx.graphics.getHeight(), Align.center);
				Gdx.input.setInputProcessor(stage);
				stage.setKeyboardFocus(narrationTA);
			}

			if (selectedActor != null) {

				// open dialog to edit actions for selected actor
				if (keycode == Input.Keys.P) {
					actionsList.clearItems();
					StoryActorDef actorDef = ((StoryActorDef)selectedActor.getUserObject());
					if (actorDef != null) {
						actionsList.setItems(actorDef.actionDefs);
						targetPageTF.setText(actorDef.targetPage);
						actorTextTF.setText(actorDef.text);
						actorCP.setValue(tmpColor);
					}
					actionsDlg.show(stage);
					actionsDlg.setSize(0.8f * Gdx.graphics.getWidth(), 0.8f * Gdx.graphics.getHeight());
					actionsDlg.setPosition(
							0.5f * Gdx.graphics.getWidth(), 0.5f * Gdx.graphics.getHeight(), Align.center);
					Gdx.input.setInputProcessor(stage);
				}

				// begin scale operation on selected actor
				if (keycode == Input.Keys.S) {
					scaling = true;
					if (selectedActor instanceof Label)
						initialValue = ((Label) selectedActor).getFontScaleX();
					else
						initialValue = selectedActor.getScaleX();
					stage.screenToStageCoordinates(downPt.set(Gdx.input.getX(), Gdx.input.getY()));
					downPt.sub(selectedActor.getX(), selectedActor.getY());
				}

				// begin rotate operation on selected actor
				if (keycode == Input.Keys.R) {
					rotating = true;
					stage.screenToStageCoordinates(tmpPt.set(Gdx.input.getX(), Gdx.input.getY()));
					tmpPt.sub(selectedActor.getX(), selectedActor.getY());
					initialValue = MathUtils.atan2(tmpPt.y, tmpPt.x) * MathUtils.radDeg - selectedActor.getRotation();
				}

				// delete selected actor
				if (keycode == Input.Keys.X) {
					selectedActor.remove();
				}

				// flip actor horizontally
				if (keycode == Input.Keys.F)
					selectedActor.setScaleX(-selectedActor.getScaleX());

				// change draw order of selected actor
				if (keycode == Input.Keys.LEFT_BRACKET && selectedActor.getZIndex() > 0)
					selectedActor.setZIndex(selectedActor.getZIndex()-1);
				if (keycode == Input.Keys.RIGHT_BRACKET)
					selectedActor.setZIndex(selectedActor.getZIndex()+1);
			}

			// show/hide help menu
			if (keycode == Input.Keys.H)
				desToolsTbl.toggleHelp();
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (designerMode) {
			if (keycode == Input.Keys.R)
				rotating = false;
			if (keycode == Input.Keys.S)
				scaling = false;
		}

		return false;
	}

	@Override
	public boolean keyTyped(char character) {

		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		if (designerMode && !storyPlay.isLive()) {
			deselectActor();
			nextActor.setVisible(false);
			nextActor.clearActions();

			// try to select an actor on the stage
			stage.screenToStageCoordinates(downPt.set(screenX, screenY));
			selectedActor = stage.hit(downPt.x, downPt.y, true);
			if (selectedActor != null) {
				if (!selectedActor.isDescendantOf(storyPlay)) // only move our actors
					selectedActor = null;
				else {
					// text actors will be selected by their label, need to grab the parent instead
					if (selectedActor.getParent() instanceof TextActor)
						selectedActor = selectedActor.getParent();

					// add selected color throb action
					colorThrobAction = Actions.color(tmpColor, 0.5f, Interpolation.smooth);
					colorThrobAction.setEndColor(tmpColor.set(selectedActor.getColor()));
					selectedActor.addAction(Actions.forever(Actions.sequence(
							Actions.color(Color.ORANGE, 0.5f, Interpolation.smooth), colorThrobAction)));

					initialPt.set(selectedActor.getX(), selectedActor.getY());
					stage.screenToStageCoordinates(downPt.set(screenX, screenY));
				}
			}
		}
		return stage.touchDown(screenX, screenY, pointer, button);
	}

	private void deselectActor() {
		if (selectedActor != null) {
			selectedActor.clearActions();
			selectedActor.setColor(tmpColor);
		}
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return stage.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {

		if (designerMode) {
			// move selected actor on the stage
			if (selectedActor != null) {
				stage.screenToStageCoordinates(tmpPt.set(screenX, screenY));
				tmpPt.sub(downPt).add(initialPt);
				selectedActor.setPosition(tmpPt.x, tmpPt.y);
			}
		}

		return stage.touchDragged(screenX, screenY, pointer);
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {

		if (designerMode) {

			// update position of nextActor preview
			stage.screenToStageCoordinates(tmpPt.set(screenX, screenY));
			nextActor.setPosition(tmpPt.x, tmpPt.y);

			if (selectedActor != null) {
				// update rotating actor operations
				if (rotating) {
					stage.screenToStageCoordinates(tmpPt.set(screenX, screenY));
					tmpPt.sub(selectedActor.getX(), selectedActor.getY());
					selectedActor.setRotation(MathUtils.atan2(tmpPt.y, tmpPt.x) * MathUtils.radDeg - initialValue);
				}

				// update scaling actor operations
				if (scaling) {
					stage.screenToStageCoordinates(tmpPt.set(screenX, screenY));
					tmpPt.sub(selectedActor.getX(), selectedActor.getY());
					float scale = initialValue * (tmpPt.len() / downPt.len());
					if (selectedActor instanceof Label) {
						((Label) selectedActor).setFontScale(scale);
						selectedActor.setWidth(scale * 100);
					} else
						selectedActor.setScale(scale, Math.abs(scale)); // preserve flip horizontally but not vertically
				}
			}
		}

		return false;
	}

	@Override
	public boolean scrolled(int amount) {

		if (designerMode) {

			// on mouse scroll, flip through the available atlas images and briefly display next to mouse
			nextActorRegIndex = (nextActorRegIndex + (int)Math.signum(amount) + atlasRegions.size) % atlasRegions.size;
			nextActor.setDrawable(new TextureRegionDrawable(atlasRegions.get(nextActorRegIndex)));
			nextActor.setSize(nextActor.getPrefWidth(), nextActor.getPrefHeight());
			flashNextActor();
		}

		return false;
	}

	private void flashNextActor() {
		nextActor.setVisible(true);
		nextActor.setColor(1, 1, 1, 1);
		nextActor.clearActions();
		nextActor.addAction(Actions.sequence(Actions.delay(2), Actions.fadeOut(0.5f)));
	}
}
