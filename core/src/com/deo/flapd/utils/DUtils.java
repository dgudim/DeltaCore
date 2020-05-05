package com.deo.flapd.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public abstract class DUtils {

    private static Preferences prefs = Gdx.app.getPreferences("Preferences");
    public static boolean logging = prefs.getBoolean("logging");

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
            throw new FileNotFoundException("no save file found in "+file.file().getPath());
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
        switch (name) {
            case ("coloring crystal"):
                item = "crystal";
                break;
            case ("ore"):
            case ("prism"):
            case ("bolt"):
            case ("cable"):
            case ("cog"):
            case ("plastic"):
            case ("transistor"):
            case ("rubber"):
            case ("wire"):
            case ("resistor"):
            case ("magnet"):
            case ("upgrades"):
            case ("shotgun"):
            case ("minigun"):
                item = name;
                break;
            case ("metal shard"):
                item = "ironShard";
                break;
            case ("iron plate"):
                item = "ironPlate";
                break;
            case ("glass shard"):
                item = "glassShard";
                break;
            case ("cyan warp shard"):
                item = "bonus_warp";
                break;
            case ("green warp shard"):
                item = "bonus_warp2";
                break;
            case ("purple warp shard"):
                item = "bonus_warp3";
                break;
            case ("red crystal"):
                item = "redCrystal";
                break;
            case ("energy cell"):
                item = "energyCell";
                break;
            case ("core shard"):
                item = "fragment_core";
                break;
            case ("green coil"):
                item = "green_coil";
                break;
            case ("cyan coil"):
                item = "neon_coil";
                break;
            case ("cyan crystal"):
                item = "cyanCrystal";
                break;
            case ("orange crystal"):
                item = "orangeCrystal";
                break;
            case ("green crystal"):
                item = "greenCrystal";
                break;
            case ("purple crystal"):
                item = "purpleCrystal";
                break;
            case ("drone engine"):
                item = "drone_engine";
                break;
            case ("red fuel cell"):
                item = "fuelCell";
                break;
            case ("cyan fuel cell"):
                item = "fuelCell2";
                break;
            case ("motherboard mk1"):
                item = "chipset";
                break;
            case ("motherboard mk2"):
                item = "chipset_big";
                break;
            case ("energy crystal"):
                item = "energyCrystal";
                break;
            case ("blue ore"):
                item = "warp_ore";
                break;
            case ("crafting card"):
                item = "craftingCard";
                break;
            case ("memory cell mk1"):
                item = "cell1";
                break;
            case ("memory cell mk2"):
                item = "cell2";
                break;
            case ("cyan blank card"):
                item = "card1";
                break;
            case ("orange blank card"):
                item = "card2";
                break;
            case ("ai card"):
                item = "aiCard";
                break;
            case ("ai processor"):
                item = "aiChip";
                break;
            case ("processor mk1"):
                item = "processor1";
                break;
            case ("processor mk2"):
                item = "processor2";
                break;
            case ("processor mk3"):
                item = "processor3";
                break;
            case ("reinforced iron plate"):
                item = "ironPlate2";
                break;
            case ("memory card"):
                item = "memoryCard";
                break;
            case ("screen card"):
                item = "screenCard";
                break;
            case ("green core"):
                item = "warpCore";
                break;
            case ("yellow core"):
                item = "core_yellow";
                break;
            case ("laser emitter"):
                item = "bonus_laser";
                break;
            case ("laser coil"):
                item = "gun";
                break;
            case ("fiber cable"):
                item = "cable_fiber";
                break;
            case ("advanced chip"):
                item = "advancedChip";
                break;
            case ("circuit board"):
                item = "Circuit_Board";
                break;
            case ("cooling unit"):
                item = "coolingUnit";
                break;
            case ("reinforced glass pane"):
                item = "IrradiantGlassPane";
                break;
            case ("composite plate"):
                item = "ReinforcedIridiumIronPlate";
                break;
            case ("grasshopper engine"):
                item = "engine1";
                break;
            case ("nuclear engine"):
                item = "engine2";
                break;
            case ("plasma engine"):
                item = "engine3";
                break;
            case ("small machine gun"):
                item = "Cannon1";
                break;
            case ("machine gun"):
                item = "Cannon2";
                break;
            case ("laser gun"):
                item = "Cannon3";
                break;
            case ("cores"):
                item = "energyCore";
                break;
            case ("copper coil"):
                item = "coil_copper";
                break;
            case ("glass rod"):
                item = "glass_panel";
                break;
            case ("star core"):
                item = "core_yellow";
                break;
            case ("warp core"):
                item = "warp_core";
                break;
            case ("energy core"):
                item = "energyCore";
                break;
            case ("rail gun"):
                item = "Cannon5";
                break;
            case ("coil gun"):
                item = "Cannon4";
                break;
            case ("ai chip"):
                item = "aiChip";
                break;
            case ("armour"):
                item = "grid";
                break;
            case ("shield generator mk1"):
                item = "shieldGeneratorMk1";
                break;
            case ("shield generator mk2"):
                item = "shieldGeneratorMk2";
                break;
            case ("shield generator mk3"):
                item = "shieldGeneratorMk3";
                break;
            case ("battery mk1"):
                item = "batteryMk1";
                break;
            case ("battery mk2"):
                item = "batteryMk2";
                break;
            case ("battery mk3"):
                item = "batteryMk3";
                break;
            case ("battery mk4"):
                item = "batteryMk4";
                break;
            case ("repellent field"):
                item = "repeller";
                break;
            case ("shield generator mk4"):
                item = "shieldMk4";
                break;
            case ("thermonuclear core"):
                item = "thermoNuclearCore";
                break;
            case ("processor crystal"):
                item = "cpuCrystal";
                break;
            case ("unstable crystal"):
                item = "unstableCrystal";
                break;
            case ("laser beam gun"):
                item = "laserBeamGun";
                break;
            case ("armour mk1"):
                item = "engineArmour1";
                break;
            case ("basic armour"):
                item = "ship";
                break;
            case ("armour mk2"):
                item = "engineArmour2";
                break;
            case ("armour mk3"):
                item = "engineArmour3";
                break;
            case ("root"):
                item = "tree";
                break;
            default:
                log("\n no texture for item " + name);
                item = "ohno";
                break;
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
}
