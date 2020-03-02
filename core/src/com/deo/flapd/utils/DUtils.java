package com.deo.flapd.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;

import java.util.Map;

public abstract class DUtils {

    public static Preferences prefs = Gdx.app.getPreferences("Preferences");
    public static boolean logging = prefs.getBoolean("logging");

    public static void log(String contents){
        if(Gdx.app.getType() == Application.ApplicationType.Android) {
            FileHandle file = Gdx.files.external("Android/data/!DeltaCore/logFull.txt");
            file.writeString(contents, true);
            FileHandle file2 = Gdx.files.external("Android/data/!DeltaCore/log.txt");
            file2.writeString(contents, true);
        }else {
            FileHandle file = Gdx.files.external("!DeltaCore/logFull.txt");
            file.writeString(contents, true);
            FileHandle file2 = Gdx.files.external("!DeltaCore/log.txt");
            file2.writeString(contents, true);
        }
    }

    public static void clearLog(){
        if(Gdx.app.getType() == Application.ApplicationType.Android) {
            FileHandle file = Gdx.files.external("Android/data/!DeltaCore/log.txt");
            FileHandle file2 = Gdx.files.external("Android/data/!DeltaCore/logFull.txt");
            file.writeString("", false);
            if(file2.file().length()>3145728){
                FileHandle file3 = Gdx.files.external("Android/data/!DeltaCore/logFull(old).txt");
                file3.writeString(file2.readString(), false);
                file2.writeString("", false);
                DUtils.log("\n log too big, creating second file");
            }
        }else{
            FileHandle file = Gdx.files.external("!DeltaCore/log.txt");
            FileHandle file2 = Gdx.files.external("!DeltaCore/logFull.txt");
            file.writeString("", false);
            if(file2.file().length()>3145728){
                FileHandle file3 = Gdx.files.external("!DeltaCore/logFull(old).txt");
                file3.writeString(file2.readString(), false);
                file2.writeString("", false);
                DUtils.log("\n log too big, creating second file");
            }
        }
    }

    public static int getRandomInRange(int min, int max){
        return(MathUtils.random(max-min)+min);
    }

    public static void putInteger(String key, int val){
        prefs.putInteger(key, val);
        prefs.flush();
        if(logging) {
            DUtils.log("\n put integer " + val + " with key " + key);
        }
    }

    private static void putString(String key, String val){
        prefs.putString(key, val);
        prefs.flush();
        if(logging) {
            DUtils.log("\n put string " + val + " with key " + key);
        }
    }

    public static void putFloat(String key, float val) {
        prefs.putFloat(key, val);
        prefs.flush();
        if(logging) {
            DUtils.log("\n put float " + val + " with key " + key);
        }
    }

    public static void putBoolean(String key, boolean val) {
        prefs.putBoolean(key, val);
        prefs.flush();
        if(logging) {
            DUtils.log("\n put boolean " + val + " with key " + key);
        }
    }

    public static void putLong(String key, long val) {
        prefs.putLong(key, val);
        prefs.flush();
        if(logging) {
            DUtils.log("\n put long " + val + " with key " + key);
        }
    }

    public static void addInteger(String key, int val) {
        int before = prefs.getInteger(key);
        int after = before+val;
        prefs.putInteger(key, after);
        prefs.flush();
        if(logging) {
            DUtils.log("\n added integer " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void addFloat(String key, float val) {
        float before = prefs.getFloat(key);
        float after = before+val;
        prefs.putFloat(key, after);
        prefs.flush();
        if(logging) {
            DUtils.log("\n added float " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void addString(String key, String val) {
        String before = prefs.getString(key);
        String after = before+val;
        prefs.putString(key, after);
        prefs.flush();
        if(logging) {
            DUtils.log("\n added string " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void addLong(String key, long val) {
        long before = prefs.getLong(key);
        long after = before+val;
        prefs.putLong(key, after);
        prefs.flush();
        if(logging) {
            DUtils.log("\n added long " + val + " to integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static Map<String, ?> getPrefs() {
        if(logging) {
            DUtils.log("\n dumped preferences \n");
        }
        return (prefs.get());
    }

    public static int getInteger(String key){
        if(logging) {
            DUtils.log("\n got integer " + prefs.getInteger(key) + " with key " + key);
        }
        return (prefs.getInteger(key));
    }

    public static float getFloat(String key){
        if(logging) {
            DUtils.log("\n got float " + prefs.getFloat(key) + " with key " + key);
        }
        return (prefs.getFloat(key));
    }

    public static boolean getBoolean(String key){
        if(logging) {
            DUtils.log("\n got boolean " + prefs.getBoolean(key) + " with key " + key);
        }
        return (prefs.getBoolean(key));
    }

    public static String getString(String key){
        if(logging) {
            DUtils.log("\n got string " + prefs.getString(key) + " with key " + key);
        }
        return (prefs.getString(key));
    }

    public static long getLong(String key){
        if(logging) {
            DUtils.log("\n got long " + prefs.getLong(key) + " with key " + key);
        }
        return (prefs.getLong(key));
    }

    public static void removeKey(String key){
        prefs.remove(key);
        prefs.flush();
        if(logging) {
            DUtils.log("\n removed key " + key);
        }
    }

    public static boolean containsKey(String key) {
        if(logging) {
            if (prefs.contains(key)) {
                DUtils.log("\n preferences contain key " + key);
            } else {
                DUtils.log("\n preferences don't contain key " + key);
            }
        }
        return (prefs.contains(key));
    }

    public static void subtractInteger(String key, int val) {
        int before = prefs.getInteger(key);
        int after = before-val;
        prefs.putInteger(key, after);
        prefs.flush();
        if(logging) {
            DUtils.log("\n subtracted integer " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void subtractFloat(String key, float val) {
        float before = prefs.getFloat(key);
        float after = before-val;
        prefs.putFloat(key, after);
        prefs.flush();
        if(logging) {
            DUtils.log("\n subtracted float " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void subtractLong(String key, long val) {
        long before = prefs.getLong(key);
        long after = before-val;
        prefs.putLong(key, after);
        prefs.flush();
        if(logging) {
            DUtils.log("\n subtracted long " + val + " from integer " + before + " with key " + key + " (" + before + "-->" + after + ")");
        }
    }

    public static void clearPrefs(){
        prefs.clear();
        prefs.flush();
        if(logging) {
            DUtils.log("\n cleared preferences");
        }
    }
}
