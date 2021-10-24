package com.deo.flapd.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.deo.flapd.model.enemies.Bosses;

import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.LogLevel.ERROR;
import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

public class DUtils {
    
    private static final Preferences prefs = Gdx.app.getPreferences("Preferences");
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
        return random(max - min) + min;
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
            return file.file().getPath().replace("\\", "/");
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
    
    public static void updateCamera(OrthographicCamera camera, Viewport viewport, int width, int height) {
        viewport.update(width, height);
        camera.position.set(400, 240, 0);
        float tempScaleH = height / 480.0f;
        float tempScaleW = width / 800.0f;
        float zoom = min(tempScaleH, tempScaleW);
        camera.zoom = 1 / zoom;
        camera.update();
    }
    
    public static int[] getVerticalAndHorizontalFillingThresholds(Viewport viewport) {
        float targetHeight = viewport.getScreenHeight();
        float targetWidth = viewport.getScreenWidth();
        
        float sourceHeight = 480.0f;
        float sourceWidth = 800.0f;
        
        float targetRatio = targetHeight / targetWidth;
        float sourceRatio = sourceHeight / sourceWidth;
        float scale;
        if (targetRatio > sourceRatio) {
            scale = targetWidth / sourceWidth;
        } else {
            scale = targetHeight / sourceHeight;
        }
        
        int actualWidth = (int) (sourceWidth * scale);
        int actualHeight = (int) (sourceHeight * scale);
        
        return new int[]{(int) Math.ceil((targetHeight - actualHeight) / 144), (int) Math.ceil((targetWidth - actualWidth) / 912)};
    }
    
    public static void logException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String fullStackTrace = sw.toString();
        log(fullStackTrace, ERROR);
    }
    
    public static void initNewGame(JsonEntry treeJson) {
        
        putInteger(Keys.enemiesKilled, 0);
        putInteger(Keys.moneyEarned, 0);
        putInteger(Keys.playerScore, 0);
        
        putFloat(Keys.playerChargeValue,
                treeJson.getFloat(1, getString(Keys.currentBattery), "parameters", "parameter.capacity")
                        * treeJson.getFloat(false, 1, getString(Keys.currentCore), "parameters", "parameter.charge_capacity_multiplier"));
        
        putFloat(Keys.playerHealthValue,
                treeJson.getFloat(1, getString(Keys.currentHull), "parameters", "parameter.health")
                        * treeJson.getFloat(false, 1, getString(Keys.currentCore), "parameters", "parameter.health_multiplier"));
        
        putFloat(Keys.playerShieldValue,
                treeJson.getFloat(1, getString(Keys.currentShield), "parameters", "parameter.shield_capacity")
                        * treeJson.getFloat(false, 1, getString(Keys.currentCore), "parameters", "parameter.shield_strength_multiplier"));
        
        for (int i = 0; i < Bosses.bossNames.length; i++) {
            putBoolean("boss_spawned_" + Bosses.bossNames[i], false);
        }
        putInteger(Keys.bonusesCollected, 0);
        putInteger(Keys.lastCheckpointScore, 0);
        putInteger(Keys.bulletsShot, 0);
        putFloat(Keys.lastPLayerX, 0);
        putFloat(Keys.lastPLayerY, 220);
    }
    
    public static float lerpWithConstantSpeed(float from, float to, float speed, float delta) {
        if (from + speed * delta < to) {
            return from + speed * delta;
        } else return max(from - speed * delta, to);
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
        
        float distance1 = abs(from - to);
        float distance2 = 360 - distance1;
        
        if (distance1 < distance2 && to > from || distance1 > distance2 && to < from) {
            return from + speed * delta;
        } else {
            return from - speed * delta;
        }
        
    }
    
    public static void connectVerticalOrHorizontalBranch(float x, float y, float x2, float y2, float thickness, Color color, Array<Image> addTo, Table holder) {
        Image branch = new Image(constructFilledImageWithColor(1, 1, color));
        float len1, len2;
        len1 = x2 - x;
        len2 = y2 - y;
        if (len1 == 0) {
            branch.setSize(thickness, len2);
            x -= thickness / 2;
        } else if (len2 == 0) {
            branch.setSize(len1, thickness);
            y -= thickness / 2;
        }
        branch.setPosition(x, y);
        addTo.add(branch);
        holder.addActor(branch);
    }
    
    public static void connectNinetyDegreeBranch(float x, float y, float x2, float y2, float thickness, boolean startFromMiddle, Color color, Array<Image> addTo, Table holder) {
        float jlen = Math.abs(x - x2) / 2f;
        if (startFromMiddle) {
            connectVerticalOrHorizontalBranch(x, y, x, y2, thickness, color, addTo, holder);
            connectVerticalOrHorizontalBranch(x, y2, x2, y2, thickness, color, addTo, holder);
        } else {
            float xOffset = x + jlen;
            connectVerticalOrHorizontalBranch(x, y, xOffset, y, thickness, color, addTo, holder);
            connectVerticalOrHorizontalBranch(xOffset, y, xOffset, y2, thickness, color, addTo, holder);
            connectVerticalOrHorizontalBranch(xOffset, y2, x2, y2, thickness, color, addTo, holder);
        }
    }
    
    public static void connectArbitraryBranch(float x, float y, float x2, float y2, float thickness, Color color, Array<Image> addTo, Table holder) {
        Image branch = new Image(constructFilledImageWithColor(1, 1, color));
        float len;
        len = getDistanceBetweenTwoPoints(x, y, x2, y2);
        float angle = MathUtils.atan2(x - x2, y2 - y);
        branch.setSize(thickness, len + thickness / 4f);
        branch.setRotation((float) (MathUtils.radiansToDegrees * angle));
        branch.setPosition((float) (x - thickness / 2f * cos(angle)), (float) (y - thickness / 2f * sin(angle)));
        addTo.add(branch);
        holder.addActor(branch);
    }
    
    public static void connectFortyFiveDegreeBranch(float x, float y, float x2, float y2, float thickness, Color color, Array<Image> addTo, Table holder) {
        boolean invert = (x2 - x > 0 && y2 - y < 0) || (x2 - x < 0 && y2 - y > 0);
        float intermediatePos = x + (y2 - y) * (invert ? -1 : 1);
        if (x2 - x == 0 || y2 - y == 0) {
            connectArbitraryBranch(x, y, x2, y2, thickness, color, addTo, holder);
        } else {
            connectArbitraryBranch(x, y, intermediatePos, y2, thickness, color, addTo, holder);
            connectArbitraryBranch(intermediatePos, y2, x2, y2, thickness, color, addTo, holder);
        }
    }
    
    public static void scaleDrawables(float targetSize, Drawable... drawables) {
        float scale = 80 / Math.max(drawables[0].getMinWidth(), drawables[0].getMinHeight());
        float width = drawables[0].getMinWidth() * scale;
        float height = drawables[0].getMinHeight() * scale;
        for (Drawable drawable : drawables) {
            drawable.setMinWidth(width);
            drawable.setMinHeight(height);
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
        return (float) sqrt(x_delta * x_delta + y_delta * y_delta);
    }
    
    public static float getDistanceBetweenTwoPoints(Vector2 from, Vector2 to) {
        return getDistanceBetweenTwoPoints(from.x, from.y, to.x, to.y);
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
    
    public static boolean handleDebugInput(OrthographicCamera camera, boolean drawScreenExtenders) {
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            camera.zoom *= 1.01;
            camera.update();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            camera.zoom *= 0.99;
            camera.update();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            return !drawScreenExtenders;
        }
        return drawScreenExtenders;
    }
}

