package com.segroup9.storyplay.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.segroup9.storyplay.MyGdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = MyGdxGame.SCREEN_WIDTH;
		config.height = MyGdxGame.SCREEN_HEIGHT;
		config.forceExit = false;
		new LwjglApplication(new MyGdxGame(), config) {
			@Override
			public void exit()
			{
				MyGdxGame game = ((MyGdxGame)getApplicationListener());
				if (game.canExit())
					super.exit();
				game.tryExit();
			}
		};
	}
}
