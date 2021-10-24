package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.deo.flapd.utils.DUtils.LogLevel.DEBUG;
import static com.deo.flapd.utils.DUtils.containsKey;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;
import static com.deo.flapd.utils.DUtils.putString;

public class LocaleManager {
    
    Array<Properties> locales;
    String currentLocale;
    int currentLocaleIndex = -1;
    String[] localeNames = {"en", "ru"};
    String[] localeDisplayNames = {"English", "Russian"};
    
    public LocaleManager() {
        locales = new Array<>();
        if (containsKey(Keys.locale)) {
            currentLocale = getString(Keys.locale);
            log("last locale: " + currentLocale, DEBUG);
        } else {
            currentLocale = System.getProperty("user.language");
            log("system locale: " + currentLocale, DEBUG);
        }
        
        long time = TimeUtils.millis();
        for (int i = 0; i < localeNames.length; i++) {
            if (localeNames[i].equals(currentLocale)) {
                currentLocaleIndex = i;
            }
            try {
                Properties locale_map = new Properties();
                final InputStreamReader in = new InputStreamReader(Gdx.files.internal("localization/" + localeNames[i] + ".properties").read(), StandardCharsets.UTF_8);
                locale_map.load(in);
                in.close();
                locales.add(locale_map);
                log("loaded locale: " + localeNames[i] + ", " + locale_map.size() + " strings", DEBUG);
            } catch (IOException e) {
                logException(e);
            }
        }
        log("loaded locale maps in " + TimeUtils.timeSinceMillis(time) + "ms", DEBUG);
        if (currentLocaleIndex == -1) {
            currentLocale = localeNames[0];
            currentLocaleIndex = 0;
        }
        if (!containsKey(Keys.locale)) {
            putString(Keys.locale, currentLocale);
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
                break;
            }
        }
        putString(Keys.locale, currentLocale);
    }
    
    public String get(String key) {
        String value = (String) locales.get(currentLocaleIndex).get(key);
        if (value == null) {
            return key;
        }
        return value;
    }
}
