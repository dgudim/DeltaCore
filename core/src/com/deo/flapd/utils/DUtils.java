package com.deo.flapd.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.model.enemies.Bosses;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.LogLevel.ERROR;
import static com.deo.flapd.utils.DUtils.LogLevel.INFO;
import static java.lang.Math.floor;
import static java.lang.Math.min;

public class DUtils {
    
    public enum LogLevel {DEBUG, INFO, WARNING, ERROR, CRITICAL_ERROR}
    
    private static final Preferences prefs = Gdx.app.getPreferences("Preferences");
    public static boolean logging = prefs.getBoolean("logging");
    private static final JsonEntry itemNames = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/itemNames.json")));
    public static int bulletDisposes;
    public static int bulletTrailDisposes;
    public static int enemyDisposes;
    public static int enemyFireDisposes;
    public static int enemyBulletDisposes;
    public static int enemyBulletTrailDisposes;
    
    private static final String androidRootDir = "Logs/";
    private static final String pcRootDir = "!DeltaCore/";
    
    private static final String currentRootDir = getRootDir();
    
    private static String getRootDir() {
        return (Gdx.app.getType() == Application.ApplicationType.Android) ? androidRootDir : pcRootDir;
    }
    
    public static void log(String contents, LogLevel logLevel) {
        String logLevelStr = "[" + logLevel + "]: ";
        FileHandle file = Gdx.files.external(currentRootDir + "logFull.txt");
        file.writeString(logLevelStr + contents + "\n", true);
        FileHandle file2 = Gdx.files.external(currentRootDir + "log.txt");
        file2.writeString(logLevelStr + contents + "\n", true);
    }
    
    public static void clearLog() {
        FileHandle file = Gdx.files.external(currentRootDir + "log.txt");
        FileHandle file2 = Gdx.files.external(currentRootDir + "logFull.txt");
        file.writeString("", false);
        if (file2.file().length() > 3145728) {
            FileHandle file3 = Gdx.files.external(currentRootDir + "logFull(old).txt");
            file3.writeString(file2.readString(), false);
            file2.writeString("", false);
            log("log too big, creating second file", INFO);
        }
    }
    
    public static int getRandomInRange(int min, int max) {
        return (MathUtils.random(max - min) + min);
    }
    
    public static boolean getRandomBoolean(float positiveChance) {
        int intPositiveChance = (int) (positiveChance * 100);
        return getRandomInRange(intPositiveChance - 10000, intPositiveChance) >= 0;
    }
    
    public static void putInteger(String key, int val) {
        prefs.putInteger(key, val);
        prefs.flush();
        if (logging) {
            log("put integer " + val + " with key " + key, DEBUG);
        }
    }
    
    public static void putString(String key, String val) {
        prefs.putString(key, val);
        prefs.flush();
        if (logging) {
            log("put string " + val + " with key " + key, DEBUG);
        }
    }
    
    public static void putFloat(String key, float val) {
        prefs.putFloat(key, val);
        prefs.flush();
        if (logging) {
            log("put float " + val + " with key " + key, DEBUG);
        }
    }
    
    public static void putBoolean(String key, boolean val) {
        prefs.putBoolean(key, val);
        prefs.flush();
        if (logging) {
            log("put boolean " + val + " with key " + key, DEBUG);
        }
    }
    
    public static void putLong(String key, long val) {
        prefs.putLong(key, val);
        prefs.flush();
        if (logging) {
            log("put long " + val + " with key " + key, DEBUG);
        }
    }
    
    public static void addInteger(String key, int val) {
        int before = prefs.getInteger(key);
        int after = before + val;
        prefs.putInteger(key, after);
        prefs.flush();
        if (logging) {
            log("added integer " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")", DEBUG);
        }
    }
    
    public static void addFloat(String key, float val) {
        float before = prefs.getFloat(key);
        float after = before + val;
        prefs.putFloat(key, after);
        prefs.flush();
        if (logging) {
            log("added float " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")", DEBUG);
        }
    }
    
    public static void addString(String key, String val) {
        String before = prefs.getString(key);
        String after = before + val;
        prefs.putString(key, after);
        prefs.flush();
        if (logging) {
            log("added string " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")", DEBUG);
        }
    }
    
    public static void addLong(String key, long val) {
        long before = prefs.getLong(key);
        long after = before + val;
        prefs.putLong(key, after);
        prefs.flush();
        if (logging) {
            log("added long " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")", DEBUG);
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
            log("dumped preferences \n", DEBUG);
        }
        return prefsString.toString();
    }
    
    public static String savePrefsToFile() {
        
        FileHandle file = Gdx.files.external(currentRootDir + "saveGame.save");
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
        
        FileHandle file = Gdx.files.external(currentRootDir + "saveGame.save");
        
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
            log("got integer " + prefs.getInteger(key) + " with key " + key, DEBUG);
        }
        return (prefs.getInteger(key));
    }
    
    public static float getFloat(String key) {
        if (logging) {
            log("got float " + prefs.getFloat(key) + " with key " + key, DEBUG);
        }
        return (prefs.getFloat(key));
    }
    
    public static boolean getBoolean(String key) {
        if (logging) {
            log("got boolean " + prefs.getBoolean(key) + " with key " + key, DEBUG);
        }
        return (prefs.getBoolean(key));
    }
    
    public static String getString(String key) {
        if (logging) {
            log("got string " + prefs.getString(key) + " with key " + key, DEBUG);
        }
        return (prefs.getString(key));
    }
    
    public static long getLong(String key) {
        if (logging) {
            log("got long " + prefs.getLong(key) + " with key " + key, DEBUG);
        }
        return (prefs.getLong(key));
    }
    
    public static void removeKey(String key) {
        prefs.remove(key);
        prefs.flush();
        if (logging) {
            log("removed key " + key, DEBUG);
        }
    }
    
    public static boolean containsKey(String key) {
        if (logging) {
            if (prefs.contains(key)) {
                log("preferences contain key " + key, DEBUG);
            } else {
                log("preferences don't contain key " + key, DEBUG);
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
            log("subtracted integer " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")", DEBUG);
        }
    }
    
    public static void subtractFloat(String key, float val) {
        float before = prefs.getFloat(key);
        float after = before - val;
        prefs.putFloat(key, after);
        prefs.flush();
        if (logging) {
            log("subtracted float " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")", DEBUG);
        }
    }
    
    public static void subtractLong(String key, long val) {
        long before = prefs.getLong(key);
        long after = before - val;
        prefs.putLong(key, after);
        prefs.flush();
        if (logging) {
            log("subtracted long " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")", DEBUG);
        }
    }
    
    public static void clearPrefs() {
        prefs.clear();
        prefs.flush();
        if (logging) {
            log("cleared preferences", DEBUG);
        }
    }
    
    public static String getItemCodeNameByName(String name) {
        return itemNames.getString("ohno", name);
    }
    
    public static void updateCamera(OrthographicCamera camera, Viewport viewport, int width, int height) {
        viewport.update(width, height);
        camera.position.set(400, 240, 0);
        float tempScaleH = height / 480.0f;
        float tempScaleW = width / 800.0f;
        float zoom = min(tempScaleH, tempScaleW);
        camera.zoom = 1 / zoom;
        camera.update();
    }
    
    public static void logException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String fullStackTrace = sw.toString();
        log(fullStackTrace, ERROR);
    }
    
    public static void initNewGame() {
        putInteger("enemiesKilled", 0);
        putInteger("moneyEarned", 0);
        putInteger("Score", 0);
        putFloat("Health", 1000);
        putFloat("Shield", 1000);
        putFloat("Charge", 1000);
        for (int i = 0; i < Bosses.bossNames.length; i++) {
            putBoolean("boss_spawned_" + Bosses.bossNames[i], false);
        }
        putInteger("bonuses_collected", 0);
        putInteger("lastCheckpoint", 0);
        putInteger("bulletsShot", 0);
        putInteger("meteoritesDestroyed", 0);
        putFloat("ShipX", 0);
        putFloat("ShipY", 220);
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
    
    public static float normaliseAngle(float angle, float start, float end) {
        final float width = end - start;
        final float offsetValue = angle - start;   // value relative to 0
        
        return (float) ((offsetValue - (floor(offsetValue / width) * width)) + start);
        // + start to reset back to start of original range
    }
    
    public static float lerpAngleWithConstantSpeed(float from, float to, float speed, float delta) {
        from = normaliseAngle(from, 0, 360);
        to = normaliseAngle(to, 0, 360);
        
        float distance1 = Math.abs(from - to);
        float distance2 = 360 - distance1;
        
        if ((distance1 < distance2 && to > from) || (distance1 > distance2 && to < from)) {
            return from + speed * delta;
        } else {
            return from - speed * delta;
        }
        
    }
    
    public static void logVariables() {
        log("total bullet dispose calls: " + bulletDisposes, INFO);
        log("total bullet trail particle effects dispose calls: " + bulletTrailDisposes, INFO);
        log("total enemy bullet dispose calls: " + enemyBulletDisposes, INFO);
        log("total enemy bullet trail particle effects dispose calls: " + enemyBulletTrailDisposes, INFO);
        log("total enemy dispose calls: " + enemyDisposes, INFO);
        log("total enemy fire particle effects dispose calls: " + enemyFireDisposes, INFO);
        bulletDisposes = 0;
        bulletTrailDisposes = 0;
        enemyDisposes = 0;
        enemyFireDisposes = 0;
        enemyBulletDisposes = 0;
        enemyBulletTrailDisposes = 0;
    }
    
    public static float convertPercentsToAbsoluteValue(String percentValue, float maxValue) {
        return Float.parseFloat(percentValue.replace("%", "").trim()) / 100f * maxValue;
    }
    
    public static Array<String> getTargetsFromGroup(String groupOfTargets, JsonEntry partGroups) {
        Array<String> targets = new Array<>();
        targets.addAll(groupOfTargets.replace(" ", "").split(","));
        for (int i = 0; i < targets.size; i++) {
            if (targets.get(i).startsWith("group:")) {
                targets.addAll(partGroups.getString("", targets.get(i).replace("group:", "")).replace(" ", "").split(","));
                targets.removeIndex(i);
            }
        }
        return targets;
    }
    
    public static TextureRegionDrawable constructFilledImageWithColor(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        TextureRegionDrawable image = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return image;
    }
    
    public static void lerpToColor(Color from, Color to, float speed, float delta) {
        if (from.r < to.r) {
            from.r = MathUtils.clamp(from.r + delta * speed, 0, 1);
        }
        if (from.g < to.g) {
            from.g = MathUtils.clamp(from.g + delta * speed, 0, 1);
        }
        if (from.b < to.b) {
            from.b = MathUtils.clamp(from.b + delta * speed, 0, 1);
        }
    }
    
    public static float getDistanceBetweenTwoPoints(float x1, float y1, float x2, float y2) {
        final float x_delta = x2 - x1;
        final float y_delta = y2 - y1;
        return (float) Math.sqrt(x_delta * x_delta + y_delta * y_delta);
    }
    
    public static void drawParticleEffectBounds(ShapeRenderer shapeRenderer, ParticleEffect particleEffect) {
        shapeRenderer.rectLine(particleEffect.getBoundingBox().getCenterX() - particleEffect.getBoundingBox().getWidth() / 2f, particleEffect.getBoundingBox().getCenterY(), particleEffect.getBoundingBox().getCenterX() + particleEffect.getBoundingBox().getWidth() / 2f, particleEffect.getBoundingBox().getCenterY(), particleEffect.getBoundingBox().getHeight());
    }
    
    public static int[] getPrice(String result, JsonEntry treeJson, float priceCoefficient) {
        JsonEntry price = treeJson.get(result, "price");
        int[] priceArray = new int[]{0, 0};
        if (price.asString().equals("auto")) {
            String[] items = treeJson.getStringArray(new String[]{}, result, "items");
            int[] itemCounts = treeJson.getIntArray(new int[]{}, result, "itemCounts");
            for (int i = 0; i < items.length; i++) {
                int[] buffer = getPrice(items[i], treeJson, priceCoefficient);
                priceArray[0] += Math.ceil(buffer[0] / treeJson.getFloat(1, result, "resultCount") * itemCounts[i]);
                priceArray[1] += buffer[1] + 1;
            }
        } else {
            return new int[]{price.asInt(), 0};
        }
        priceArray[1] = (int) MathUtils.clamp((Math.ceil(priceArray[1] / 2f) - 1) * priceCoefficient, 0, 100);
        return priceArray;
    }
}
