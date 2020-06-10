package com.deo.flapd.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.deo.flapd.Main;

public class DesktopLauncher {

	public static void main (String[] arg) {
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 480;
		config.title = "Deltacore";
		config.allowSoftwareMode = true;
		//config.fullscreen = true;
		config.vSyncEnabled = true;
		new LwjglApplication(new Main(), config);
	}
}
