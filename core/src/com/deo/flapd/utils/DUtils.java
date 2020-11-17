package com.deo.flapd.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.model.enemies.Bosses;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Map;

public abstract class DUtils {

    private static Preferences prefs = Gdx.app.getPreferences("Preferences");
    public static boolean logging = prefs.getBoolean("logging");
    private static JsonValue itemNames = new JsonReader().parse(Gdx.files.internal("shop/itemNames.json"));
    public static int bulletDisposes;
    public static int bulletTrailDisposes;
    public static int enemyDisposes;
    public static int enemyFireDisposes;
    public static int enemyBulletDisposes;
    public static int enemyBulletTrailDisposes;

    public static void log(String contents) {
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            FileHandle file = Gdx.files.external("Android/data/!DeltaCore/logFull.txt");
            file.writeString(contents, true);
            FileHandle file2 = Gdx.files.external("Android/data/!DeltaCore/log.txt");
            file2.writeString(contents, true);
        } else {
            FileHandle file = Gdx.files.external("!DeltaCore/logFull.txt");
            file.writeString(contents, true);
            FileHandle file2 = Gdx.files.external("!DeltaCore/log.txt");
            file2.writeString(contents, true);
        }
    }

    public static void clearLog() {
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            FileHandle file = Gdx.files.external("Android/data/!DeltaCore/log.txt");
            FileHandle file2 = Gdx.files.external("Android/data/!DeltaCore/logFull.txt");
            file.writeString("", false);
            if (file2.file().length() > 3145728) {
                FileHandle file3 = Gdx.files.external("Android/data/!DeltaCore/logFull(old).txt");
                file3.writeString(file2.readString(), false);
                file2.writeString("", false);
                log("\n log too big, creating second file");
            }
        } else {
            FileHandle file = Gdx.files.external("!DeltaCore/log.txt");
            FileHandle file2 = Gdx.files.external("!DeltaCore/logFull.txt");
            file.writeString("", false);
            if (file2.file().length() > 3145728) {
                FileHandle file3 = Gdx.files.external("!DeltaCore/logFull(old).txt");
                file3.writeString(file2.readString(), false);
                file2.writeString("", false);
                log("\n log too big, creating second file");
            }
        }
    }

    public static int getRandomInRange(int min, int max) {
        return (MathUtils.random(max - min) + min);
    }

    public static void putInteger(String key, int val) {
        prefs.putInteger(key, val);
        prefs.flush();
        if (logging) {
            log("\n put integer " + val + " with key " + key);
        }
    }

    public static void putString(String key, String val) {
        prefs.putString(key, val);
        prefs.flush();
        if (logging) {
            log("\n put string " + val + " with key " + key);
        }
    }

    public static void putFloat(String key, float val) {
        prefs.putFloat(key, val);
        prefs.flush();
        if (logging) {
            log("\n put float " + val + " with key " + key);
        }
    }

    public static void putBoolean(String key, boolean val) {
        prefs.putBoolean(key, val);
        prefs.flush();
        if (logging) {
            log("\n put boolean " + val + " with key " + key);
        }
    }

    public static void putLong(String key, long val) {
        prefs.putLong(key, val);
        prefs.flush();
        if (logging) {
            log("\n put long " + val + " with key " + key);
        }
    }

    public static void addInteger(String key, int val) {
        int before = prefs.getInteger(key);
        int after = before + val;
        prefs.putInteger(key, after);
        prefs.flush();
        if (logging) {
            log("\n added integer " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void addFloat(String key, float val) {
        float before = prefs.getFloat(key);
        float after = before + val;
        prefs.putFloat(key, after);
        prefs.flush();
        if (logging) {
            log("\n added float " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void addString(String key, String val) {
        String before = prefs.getString(key);
        String after = before + val;
        prefs.putString(key, after);
        prefs.flush();
        if (logging) {
            log("\n added string " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void addLong(String key, long val) {
        long before = prefs.getLong(key);
        long after = before + val;
        prefs.putLong(key, after);
        prefs.flush();
        if (logging) {
            log("\n added long " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static String getPrefs() {

        StringBuilder prefsString = new StringBuilder();

        int size = prefs.get().size();

        Object[] keys = prefs.get().keySet().toArray();
        Object[] values = prefs.get().values().toArray();
        for (int i = 0; i < size; i++) {
            prefsString.append(keys[i]);
            prefsString.append(" = ");
            prefsString.append(values[i]);
            prefsString.append(";\n");
        }
        if (logging) {
            log("\n dumped preferences \n");
        }
        return prefsString.toString();
    }

    public static String savePrefsToFile() {
        String path;

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            path = "Android/data/!DeltaCore/saveGame.save";
        } else {
            path = "!DeltaCore/saveGame.save";
        }

        FileHandle file = Gdx.files.external(path);
        file.writeString("", false);

        try {
            FileOutputStream f = new FileOutputStream(file.file());
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(prefs.get());
            s.close();
            return file.file().getPath();
        } catch (Exception e) {
            logException(e);
        }

        return "error";
    }

    public static void loadPrefsFromFile() throws FileNotFoundException {

        String path;

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            path = "Android/data/!DeltaCore/saveGame.save";
        } else {
            path = "!DeltaCore/saveGame.save";
        }

        FileHandle file = Gdx.files.external(path);

        try {
            FileInputStream f = new FileInputStream(file.file());
            ObjectInputStream s = new ObjectInputStream(f);
            prefs.put((Map<String, ?>) s.readObject());
            prefs.flush();
            s.close();
        } catch (Exception e) {
            logException(e);
            throw new FileNotFoundException("no save file found in " + file.file().getPath());
        }

    }

    public static int getInteger(String key) {
        if (logging) {
            log("\n got integer " + prefs.getInteger(key) + " with key " + key);
        }
        return (prefs.getInteger(key));
    }

    public static float getFloat(String key) {
        if (logging) {
            log("\n got float " + prefs.getFloat(key) + " with key " + key);
        }
        return (prefs.getFloat(key));
    }

    public static boolean getBoolean(String key) {
        if (logging) {
            log("\n got boolean " + prefs.getBoolean(key) + " with key " + key);
        }
        return (prefs.getBoolean(key));
    }

    public static String getString(String key) {
        if (logging) {
            log("\n got string " + prefs.getString(key) + " with key " + key);
        }
        return (prefs.getString(key));
    }

    public static long getLong(String key) {
        if (logging) {
            log("\n got long " + prefs.getLong(key) + " with key " + key);
        }
        return (prefs.getLong(key));
    }

    public static void removeKey(String key) {
        prefs.remove(key);
        prefs.flush();
        if (logging) {
            log("\n removed key " + key);
        }
    }

    public static boolean containsKey(String key) {
        if (logging) {
            if (prefs.contains(key)) {
                log("\n preferences contain key " + key);
            } else {
                log("\n preferences don't contain key " + key);
            }
        }
        return (prefs.contains(key));
    }

    public static void subtractInteger(String key, int val) {
        int before = prefs.getInteger(key);
        int after = before - val;
        prefs.putInteger(key, after);
        prefs.flush();
        if (logging) {
            log("\n subtracted integer " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void subtractFloat(String key, float val) {
        float before = prefs.getFloat(key);
        float after = before - val;
        prefs.putFloat(key, after);
        prefs.flush();
        if (logging) {
            log("\n subtracted float " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void subtractLong(String key, long val) {
        long before = prefs.getLong(key);
        long after = before - val;
        prefs.putLong(key, after);
        prefs.flush();
        if (logging) {
            log("\n subtracted long " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void clearPrefs() {
        prefs.clear();
        prefs.flush();
        if (logging) {
            log("\n cleared preferences");
        }
    }

    public static String getItemCodeNameByName(String name) {
        String item;
        try {
            item = itemNames.getString(name);
        } catch (Exception e) {
            item = "ohno";
            log("\n no texture for item " + name);
        }
        return item;
    }

    public static void updateCamera(OrthographicCamera camera, Viewport viewport, int width, int height) {
        viewport.update(width, height);
        camera.position.set(400, 240, 0);
        float tempScaleH = height / 480.0f;
        float tempScaleW = width / 800.0f;
        float zoom = Math.min(tempScaleH, tempScaleW);
        camera.zoom = 1 / zoom;
        camera.update();
    }

    public static void logException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String fullStackTrace = sw.toString();
        log("\n\n" + fullStackTrace + "\n");
    }

    public static void initNewGame() {
        putInteger("enemiesKilled", 0);
        putInteger("moneyEarned", 0);
        putInteger("Score", 0);
        putFloat("Health", 1000);
        putFloat("Shield", 1000);
        putFloat("Charge", 1000);
        for (int i = 0; i < Bosses.bosses.size; i++) {
            putBoolean("boss_spawned_" + Bosses.bosses.get(i).bossName, false);
        }
        putInteger("bonuses_collected", 0);
        putInteger("lastCheckpoint", 0);
        putInteger("bulletsShot", 0);
        putInteger("meteoritesDestroyed", 0);
        putFloat("ShipX", 0);
        putFloat("ShipY", 220);
    }

    public static <T> T concatArray(T a, T b) {
        if (!a.getClass().isArray() || !b.getClass().isArray()) {
            throw new IllegalArgumentException();
        }

        Class<?> resCompType;
        Class<?> aCompType = a.getClass().getComponentType();
        Class<?> bCompType = b.getClass().getComponentType();

        if (aCompType.isAssignableFrom(bCompType)) {
            resCompType = aCompType;
        } else if (bCompType.isAssignableFrom(aCompType)) {
            resCompType = bCompType;
        } else {
            throw new IllegalArgumentException();
        }

        int aLen = Array.getLength(a);
        int bLen = Array.getLength(b);

        T result = (T) Array.newInstance(resCompType, aLen + bLen);
        System.arraycopy(a, 0, result, 0, aLen);
        System.arraycopy(b, 0, result, aLen, bLen);

        return result;
    }

    public static float lerpWithConstantSpeed(float from, float to, float speed, float delta) {
        if (from + speed * delta < to) {
            return from + speed * delta;
        } else if (from - speed * delta > to) {
            return from - speed * delta;
        } else {
            return to;
        }
    }

    public static float lerpAngleWithConstantSpeed(float from, float to, float speed, float delta) {

        if (to < 0) {
            to += 360;
        }
        if (to > 360) {
            to -= 360;
        }
        if (from < 0) {
            from += 360;
        }
        if (from > 360) {
            from -= 360;
        }

        // TODO fixme

        float distance1 = Math.abs(from - to);
        float distance2 = 360 - distance1;

        if (distance1 <= distance2) {
            return from + speed * delta;
        } else {
            return from - speed * delta;
        }
    }

    public static void logVariables() {
        log("\n total bullet dispose calls: " + bulletDisposes);
        log("\n total bullet trail particle effects dispose calls: " + bulletTrailDisposes);
        log("\n total enemy bullet dispose calls: " + enemyBulletDisposes);
        log("\n total enemy bullet trail particle effects dispose calls: " + enemyBulletTrailDisposes);
        log("\n total enemy dispose calls: " + enemyDisposes);
        log("\n total enemy fire particle effects dispose calls: " + enemyFireDisposes);
        bulletDisposes = 0;
        bulletTrailDisposes = 0;
        enemyDisposes = 0;
        enemyFireDisposes = 0;
        enemyBulletDisposes = 0;
        enemyBulletTrailDisposes = 0;
    }

    public static TextureRegionDrawable constructFilledImageWithColor(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        TextureRegionDrawable image = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return image;
    }
}
