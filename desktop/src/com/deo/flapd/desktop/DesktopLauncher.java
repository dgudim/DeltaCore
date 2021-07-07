package com.deo.flapd.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.deo.flapd.Main;

public class DesktopLauncher {

    public static void main(String[] arg) {
        final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1200;
        config.height = 720;
        config.title = "Deltacore";
        config.fullscreen = false;
        new LwjglApplication(new Main(), config);
    }
}
