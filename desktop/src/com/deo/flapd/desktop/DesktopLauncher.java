package com.deo.flapd.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.deo.flapd.Main;

public class DesktopLauncher {

    public static void main(String[] arg) {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Deltacore");
        config.setAudioConfig(32, 512, 10);
        config.useVsync(true);
        config.setWindowedMode(1200, 720);
        config.setWindowIcon("ic_launcher.png");
        new Lwjgl3Application(new Main(), config);
        System.exit(0);
    }
}
