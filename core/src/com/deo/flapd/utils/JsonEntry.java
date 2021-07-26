package com.deo.flapd.utils;

import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.LogLevel.WARNING;

public class JsonEntry {
    
    public JsonValue jsonValue;
    public String name;
    public int size;
    
    public JsonEntry() {
    }
    
    public JsonEntry(JsonValue value) {
        set(value);
    }
    
    public void set(JsonValue value) {
        jsonValue = value;
        name = value.name;
        size = value.size;
    }
    
    /**
     * Returns the child at the specified index. This requires walking the linked list to the specified entry, see
     * {@link JsonEntry} for how to iterate efficiently.
     *
     * @return May be null.
     */
    public JsonEntry get(int index) {
        if (jsonValue.get(index) == null) {
            log("No key with index " + index + ", trace info: " + jsonValue.trace(), WARNING);
            return new JsonEntry();
        }
        return new JsonEntry(jsonValue.get(index));
    }
    
    /**
     * Returns the child with the specified name.
     *
     * @return May be null.
     */
    public JsonEntry get(String name) {
        if (jsonValue.get(name) == null) {
            log("No key named " + name + " (path: " + jsonValue.trace() + ")", WARNING);
            return new JsonEntry();
        }
        return new JsonEntry(jsonValue.get(name));
    }
    
    /**
     * Returns the child with the specified name and path.
     *
     * @return May be null.
     */
    public JsonEntry get(String... keys) {
        JsonEntry entry = this;
        for (String key : keys) {
            if (entry.get(key).isNull()) {
                return new JsonEntry();
            }
            entry = entry.get(key);
        }
        return entry;
    }
    
    /**
     * Returns the child with the specified index of the child with the specified name.
     *
     * @return May be null.
     */
    public JsonEntry get(String key, int index) {
        if (!(get(key).isNull())) {
            if (!(get(key).get(index).isNull())) {
                return get(key).get(index);
            }
        }
        return null;
    }
    
    /**
     * Returns the parent for this value.
     *
     * @return May be null.
     */
    public JsonEntry parent() {
        if (!jsonValue.parent().isNull()) {
            return new JsonEntry(jsonValue.parent());
        } else {
            log("Json entry " + jsonValue.name + " has no parent, first child: " + jsonValue.child(), WARNING);
            return new JsonEntry();
        }
    }
    
    /**
     * Finds the child with the specified index and returns it as a string.
     */
    public String getString(String defaultValue, int index) {
        if (get(index).isNull()) {
            log("No value specified for index " + index + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            return defaultValue;
        }
        return get(index).jsonValue.asString();
    }
    
    /**
     * Finds the child with the specified index and returns it as a float.
     */
    public float getFloat(float defaultValue, int index) {
        if (get(index).isNull()) {
            log("No value specified for index " + index + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            return defaultValue;
        }
        return get(index).jsonValue.asFloat();
    }
    
    /**
     * Finds the child with the specified key and returns it's child with specified index as a float.
     */
    public float getFloat(float defaultValue, String key, int index) {
        if (!(get(key).isNull())) {
            if (!(get(key).get(index).isNull())) {
                return get(key).get(index).jsonValue.asFloat();
            }
        }
        log("No value specified for key " + key + " and index " + index + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
        return defaultValue;
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a boolean.
     */
    public boolean getBoolean(boolean defaultValue, String... keys) {
        if (get(keys).isNull()) {
            log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            return defaultValue;
        }
        return get(keys).jsonValue.asBoolean();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as an integer.
     */
    public int getInt(int defaultValue, String... keys) {
        if (get(keys).isNull()) {
            log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            return defaultValue;
        }
        return get(keys).jsonValue.asInt();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a float.
     */
    public float getFloat(float defaultValue, String... keys) {
        if (get(keys).isNull()) {
            log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            return defaultValue;
        }
        return get(keys).jsonValue.asFloat();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a string.
     */
    public String getString(String defaultValue, String... keys) {
        if (get(keys).isNull()) {
            log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            return defaultValue;
        }
        return get(keys).jsonValue.asString();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a boolean array.
     */
    public boolean[] getBooleanArray(boolean[] defaultValue, String... keys) {
        if (get(keys).isNull()) {
            log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + ((defaultValue.length > 0) ? (defaultValue[0] + "..." + defaultValue[defaultValue.length - 1]) : "empty array") + ")", WARNING);
            return defaultValue;
        }
        return get(keys).jsonValue.asBooleanArray();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as an integer array.
     */
    public int[] getIntArray(int[] defaultValue, String... keys) {
        if (get(keys).isNull()) {
            log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + ((defaultValue.length > 0) ? (defaultValue[0] + "..." + defaultValue[defaultValue.length - 1]) : "empty array") + ")", WARNING);
            return defaultValue;
        }
        return get(keys).jsonValue.asIntArray();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a float array.
     */
    public float[] getFloatArray(float[] defaultValue, String... keys) {
        if (get(keys).isNull()) {
            log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + ((defaultValue.length > 0) ? (defaultValue[0] + "..." + defaultValue[defaultValue.length - 1]) : "empty array") + ")", WARNING);
            return defaultValue;
        }
        return get(keys).jsonValue.asFloatArray();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a string array.
     */
    public String[] getStringArray(String[] defaultValue, String... keys) {
        if (get(keys).isNull()) {
            log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + ((defaultValue.length > 0) ? (defaultValue[0] + "..." + defaultValue[defaultValue.length - 1]) : "empty array") + ")", WARNING);
            return defaultValue;
        }
        return get(keys).jsonValue.asStringArray();
    }
    
    /**
     * Returns the child with the specified index of the child with the specified name and returns it as a string.
     */
    public String getString(String defaultValue, String key, int index) {
        if (get(key).isNull()) {
            log("No value specified for key " + key + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            return defaultValue;
        }
        return get(key).getString(defaultValue, index);
    }
    
    /**
     * Returns the child with the specified name of the child with the specified index and returns it as a string.
     */
    public String getString(String defaultValue, int index, String key) {
        if (get(index).isNull()) {
            log("No value specified for index " + index + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            return defaultValue;
        }
        return get(index).getString(defaultValue, key);
    }
    
    public String asString() {
        return jsonValue.asString();
    }
    
    public int asInt() {
        return jsonValue.asInt();
    }
    
    public int[] asIntArray() {
        return jsonValue.asIntArray();
    }
    
    public boolean isBoolean(boolean defaultValue) {
        if (isNull()) {
            return defaultValue;
        }
        return jsonValue.isBoolean();
    }
    
    public boolean isNull() {
        return jsonValue == null;
    }
    
    @Override
    public String toString() {
        return jsonValue.toString();
    }
}
