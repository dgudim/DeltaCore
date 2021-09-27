package com.deo.flapd.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.model.enemies.Bosses;

import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static com.deo.flapd.utils.DUtils.ItemTextureModifier.NORMAL;
import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.LogLevel.ERROR;
import static java.lang.Math.floor;
import static java.lang.Math.min;

public class DUtils {
    
    private static final Preferences prefs = Gdx.app.getPreferences("Preferences");
    private static final JsonEntry itemNames = new JsonEntry(new JsonReader().parse(Gdx.files.internal("shop/itemNames.json")));
    private static final String androidRootDir = "Logs/";
    private static final String pcRootDir = "!DeltaCore/";
    private static final String currentRootDir = getRootDir();
    public static boolean logging = prefs.getBoolean("logging");
    private static String lastLine = "";
    private static String logBuffer = "";
    private static final float logBufferSizeFlushThreshold = 500;
    private static final FileHandle logFile = Gdx.files.external(currentRootDir + "lastLog.txt");
    
    private static String getRootDir() {
        return Gdx.app.getType() == Application.ApplicationType.Android ? androidRootDir : pcRootDir;
    }
    
    public enum LogLevel {DEBUG, INFO, WARNING, ERROR, CRITICAL_ERROR}
    
    public static void log(String contents, LogLevel logLevel) {
        contents = "[" + logLevel + "]: " + contents;
        if (contents.equals(lastLine)) {
            logBuffer += " + 1";
        } else {
            logBuffer += "\n" + contents;
        }
        lastLine = contents;
        if (logBuffer.length() >= logBufferSizeFlushThreshold) {
            flushLogBuffer();
        }
    }
    
    public static void flushLogBuffer() {
        logFile.writeString(logBuffer, true);
        logBuffer = "";
        lastLine = "";
    }
    
    public static void clearLog() {
        logFile.writeString("", false);
        logBuffer = "";
        lastLine = "";
    }
    
    public static int getRandomInRange(int min, int max) {
        return MathUtils.random(max - min) + min;
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
        
        try {
            ObjectOutputStream s = new ObjectOutputStream(file.write(false));
            s.writeObject(prefs.get());
            s.close();
            return file.file().getPath();
        } catch (Exception e) {
            logException(e);
            return "error";
        }
    }
    
    public static Object readObjectFromFile(FileHandle file) {
        try {
            ObjectInputStream s = new ObjectInputStream(file.read());
            Object obj = s.readObject();
            s.close();
            return obj;
        } catch (Exception e) {
            logException(e);
            return null;
        }
    }
    
    public static void loadPrefsFromFile() throws FileNotFoundException {
        
        FileHandle file = Gdx.files.external(currentRootDir + "saveGame.save");
        
        try {
            ObjectInputStream s = new ObjectInputStream(file.read());
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
        return prefs.getInteger(key);
    }
    
    public static float getFloat(String key) {
        if (logging) {
            log("got float " + prefs.getFloat(key) + " with key " + key, DEBUG);
        }
        return prefs.getFloat(key);
    }
    
    public static boolean getBoolean(String key) {
        if (logging) {
            log("got boolean " + prefs.getBoolean(key) + " with key " + key, DEBUG);
        }
        return prefs.getBoolean(key);
    }
    
    public static String getString(String key) {
        if (logging) {
            log("got string " + prefs.getString(key) + " with key " + key, DEBUG);
        }
        return prefs.getString(key);
    }
    
    public static long getLong(String key) {
        if (logging) {
            log("got long " + prefs.getLong(key) + " with key " + key, DEBUG);
        }
        return prefs.getLong(key);
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
        return prefs.contains(key);
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
    
    public enum ItemTextureModifier {NORMAL, DISABLED, OVER, ENABLED}
    
    public static String getItemTextureNameByName(String name) {
        return getItemTextureNameByName(name, NORMAL);
    }
    
    public static String getItemTextureNameByName(String name, ItemTextureModifier itemTextureModifier) {
        return itemNames.getString("ohno", name) + (itemTextureModifier.equals(NORMAL) ? "" : ("_" + itemTextureModifier.name().toLowerCase()));
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
        
        return (float) (offsetValue - floor(offsetValue / width) * width + start);
        // + start to reset back to start of original range
    }
    
    public static float lerpAngleWithConstantSpeed(float from, float to, float speed, float delta) {
        from = normaliseAngle(from, 0, 360);
        to = normaliseAngle(to, 0, 360);
        
        float distance1 = Math.abs(from - to);
        float distance2 = 360 - distance1;
        
        if (distance1 < distance2 && to > from || distance1 > distance2 && to < from) {
            return from + speed * delta;
        } else {
            return from - speed * delta;
        }
        
    }
    
    public static String getNameFromPath(String path) {
        int lastIndex = path.lastIndexOf(".");
        return path.substring(path.lastIndexOf("/") + 1, lastIndex == -1 ? path.length() : lastIndex);
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
    
    public static void drawParticleEffectBounds(ShapeRenderer shapeRenderer, ParticleEffectPool.PooledEffect particleEffect) {
        shapeRenderer.rectLine(particleEffect.getBoundingBox().getCenterX() - particleEffect.getBoundingBox().getWidth() / 2f, particleEffect.getBoundingBox().getCenterY(), particleEffect.getBoundingBox().getCenterX() + particleEffect.getBoundingBox().getWidth() / 2f, particleEffect.getBoundingBox().getCenterY(), particleEffect.getBoundingBox().getHeight());
    }
    
    public static void drawScreenExtenders(SpriteBatch batch, Texture fillTexture, int verticalFillingThreshold, int horizontalFillingThreshold) {
        batch.setColor(1, 1, 1, 1);
        for (int i = 0; i < verticalFillingThreshold; i++) {
            batch.draw(fillTexture, 0, -72 * (i + 1), 456, 72);
            batch.draw(fillTexture, 456, -72 * (i + 1), 456, 72);
            batch.draw(fillTexture, 0, 408 + 72 * (i + 1), 456, 72);
            batch.draw(fillTexture, 456, 408 + 72 * (i + 1), 456, 72);
        }
        
        for (int i = 0; i < horizontalFillingThreshold; i++) {
            for (int i2 = 0; i2 < 7; i2++) {
                batch.draw(fillTexture, -456 - 456 * i, 408 - i2 * 72, 456, 72);
                batch.draw(fillTexture, 800 + 456 * i, 408 - i2 * 72, 456, 72);
            }
        }
    }
    
    public static void drawBg(SpriteBatch batch, Texture bg1, Texture bg2, float warpSpeed, float movement) {
        if (warpSpeed > 0) {
            batch.setColor(1, 1, 1, 0.5f);
        }
        for (int i = 0; i < warpSpeed / 7 + 1; i++) {
            batch.draw(bg1, 0, 0, (int) (movement * 50) - i * 3, -240, 800, 720);
        }
        if (warpSpeed > 0) {
            batch.setColor(1, 1, 1, 1);
        }
        if (warpSpeed < 20) {
            batch.setColor(1, 1, 1, (35 - warpSpeed) / 35f);
            batch.draw(bg2, 0, 0, (int) (movement * 53), -240, 800, 720);
            batch.setColor(1, 1, 1, 1);
        }
    }
}
