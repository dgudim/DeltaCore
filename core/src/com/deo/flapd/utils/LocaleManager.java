package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.io.IOException;

import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;

public class LocaleManager {
    
    Array<ObjectMap<String, String>> locales;
    String currentLocale;
    int currentLocaleIndex = -1;
    String[] localeNames = {"en", "ru"};
    String[] localeDisplayNames = {"English", "Russian"};
    
    public LocaleManager() {
        locales = new Array<>();
        currentLocale = System.getProperty("user.language");
        log("system locale: " + currentLocale, DEBUG);
        long time = TimeUtils.millis();
        for (int i = 0; i < localeNames.length; i++) {
            if (localeNames[i].equals(currentLocale)) {
                currentLocaleIndex = i;
            }
            try {
                ObjectMap<String, String> locale_map = new ObjectMap<>();
                PropertiesUtils.load(locale_map, Gdx.files.internal("localization/" + localeNames[i] + ".properties").reader());
                locales.add(locale_map);
                log("loaded locale: " + localeNames[i] + ", " + locale_map.size + " strings", DEBUG);
            } catch (IOException e) {
                logException(e);
            }
        }
        log("loaded locale maps in " + TimeUtils.timeSinceMillis(time) + "ms", DEBUG);
        if (currentLocaleIndex == -1) {
            currentLocale = localeNames[0];
            currentLocaleIndex = 0;
        }
        log("current locale: " + currentLocale, DEBUG);
    }
    
    String[] getLocales() {
        return localeDisplayNames;
    }
    
    public void setLocale(String locale) {
        for (int i = 0; i < locales.size; i++) {
            if (localeNames[i].equals(locale) || localeDisplayNames[i].equals(locale)) {
                currentLocaleIndex = i;
                currentLocale = localeNames[i];
            }
        }
    }
    
    public String get(String key) {
        String value = locales.get(currentLocaleIndex).get(key);
        if (value == null) {
            return key;
        }
        return value;
    }
    
}
