package com.deo.flapd.utils;

import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.LogLevel.WARNING;
import static com.deo.flapd.utils.DUtils.log;

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
     * {@link JsonValue} for how to iterate efficiently.
     */
    public JsonEntry get(boolean showWarnings, int index) {
        if (jsonValue.get(index) == null) {
            if (showWarnings) {
                log("No key with index " + index + ", trace info: " + jsonValue.trace(), WARNING);
            }
            return new JsonEntry();
        }
        return new JsonEntry(jsonValue.get(index));
    }
    
    /**
     * Returns the child at the specified index. This requires walking the linked list to the specified entry, see
     * {@link JsonValue} for how to iterate efficiently.
     */
    public JsonEntry get(int index) {
        return get(true, index);
    }
    
    /**
     * Returns the child with the specified name.
     */
    public JsonEntry get(boolean showWarnings, String name) {
        return getWithFallBack(new JsonEntry(), showWarnings, name);
    }
    
    public JsonEntry getWithFallBack(JsonEntry fallback, boolean showWarnings, String name){
        if (jsonValue.get(name) == null) {
            if (showWarnings) {
                log("No key named " + name + " (path: " + jsonValue.trace() + ")", WARNING);
            }
            return fallback;
        }
        return new JsonEntry(jsonValue.get(name));
    }
    
    /**
     * Returns the child with the specified name.
     */
    public JsonEntry get(String name) {
        return get(true, name);
    }
    
    /**
     * Returns the child with the specified name and path.
     */
    public JsonEntry get(boolean showWarnings, String... keys) {
        return getWithFallBack(new JsonEntry(), showWarnings, keys);
    }
    
    public JsonEntry getWithFallBack(JsonEntry fallback, boolean showWarnings, String... keys) {
        JsonEntry entry = this;
        for (String key : keys) {
            if (entry.get(showWarnings, key).isNull()) {
                return fallback;
            }
            entry = entry.get(showWarnings, key);
        }
        return entry;
    }
    
    /**
     * Returns the child with the specified name and path.
     */
    public JsonEntry get(String... keys) {
        return get(true, keys);
    }
    
    /**
     * Returns the child with the specified index of the child with the specified name.
     *
     * @return May be null.
     */
    public JsonEntry get(boolean showWarnings, String key, int index) {
        if (!(get(showWarnings, key).isNull())) {
            if (!(get(showWarnings, key).get(showWarnings, index).isNull())) {
                return get(showWarnings, key).get(showWarnings, index);
            }
        }
        return null;
    }
    
    /**
     * Returns the child with the specified index of the child with the specified name.
     *
     * @return May be null.
     */
    public JsonEntry get(String key, int index) {
        return get(true, key, index);
    }
    
    /**
     * Returns the parent for this value.
     */
    public JsonEntry parent(boolean showWarnings) {
        if (!jsonValue.parent().isNull()) {
            return new JsonEntry(jsonValue.parent());
        } else {
            if (showWarnings) {
                log("Json entry " + jsonValue.name + " has no parent, first child: " + jsonValue.child(), WARNING);
            }
            return new JsonEntry();
        }
    }
    
    /**
     * Returns the parent for this value.
     */
    public JsonEntry parent() {
        return parent(true);
    }
    
    public void replaceValue(JsonEntry valueToAdd) {
        removeValue(valueToAdd.name);
        addValue(valueToAdd);
    }
    
    public void removeValue(String name) {
        jsonValue.remove(name);
        size--;
    }
    
    public void addValue(JsonEntry valueToAdd) {
        JsonValue lastValue = jsonValue.get(size - 1);
        lastValue.next = valueToAdd.jsonValue;
        valueToAdd.jsonValue.parent = valueToAdd.jsonValue;
        valueToAdd.jsonValue.prev = lastValue;
        jsonValue.size++;
        size++;
    }
    
    /**
     * Finds the child with the specified index and returns it as a string.
     */
    public String getString(boolean showWarnings, String defaultValue, int index) {
        if (get(false, index).isNull()) {
            if (showWarnings) {
                log("No value specified for index " + index + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, index).jsonValue.asString();
    }
    
    /**
     * Finds the child with the specified index and returns it as a string.
     */
    public String getString(String defaultValue, int index) {
        return getString(true, defaultValue, index);
    }
    
    /**
     * Finds the child with the specified index and returns it as a float.
     */
    public float getFloat(boolean showWarnings, float defaultValue, int index) {
        if (get(false, index).isNull()) {
            if (showWarnings) {
                log("No value specified for index " + index + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, index).jsonValue.asFloat();
    }
    
    /**
     * Finds the child with the specified index and returns it as a float.
     */
    public float getFloat(float defaultValue, int index) {
        return getFloat(true, defaultValue, index);
    }
    
    /**
     * Finds the child with the specified key and returns it's child with specified index as a float.
     */
    public float getFloat(boolean showWarnings, float defaultValue, String key, int index) {
        if (!(get(false, key).isNull())) {
            if (!(get(false, key).get(false, index).isNull())) {
                return get(showWarnings, key).get(showWarnings, index).jsonValue.asFloat();
            }
        }
        if (showWarnings) {
            log("No value specified for key " + key + " and index " + index + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
        }
        return defaultValue;
    }
    
    /**
     * Finds the child with the specified key and returns it's child with specified index as a float.
     */
    public float getFloat(float defaultValue, String key, int index) {
        return getFloat(true, defaultValue, key, index);
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a boolean.
     */
    public boolean getBoolean(boolean showWarnings, boolean defaultValue, String... keys) {
        if (get(false, keys).isNull()) {
            if (showWarnings) {
                log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, keys).jsonValue.asBoolean();
    }
    
    public boolean getBooleanWithFallback(JsonEntry fallback, boolean showWarnings, boolean defaultValue, String... keys) {
        if (get(false, keys).isBoolean()) {
            return getBoolean(false, false, keys);
        } else {
            return fallback.getBoolean(showWarnings, defaultValue, keys);
        }
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a boolean.
     */
    public boolean getBoolean(boolean defaultValue, String... keys) {
        return getBoolean(true, defaultValue, keys);
    }
    
    /**
     * Finds the child with the specified name and path and returns it as an integer.
     */
    public int getInt(boolean showWarnings, int defaultValue, String... keys) {
        if (get(false, keys).isNull()) {
            if (showWarnings) {
                log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, keys).jsonValue.asInt();
    }
    
    public int getIntWithFallback(JsonEntry fallback, boolean showWarnings, int defaultValue, String... keys) {
        if (get(false, keys).isNumber()) {
            return getInt(false, 0, keys);
        } else {
            return fallback.getInt(showWarnings, defaultValue, keys);
        }
    }
    
    /**
     * Finds the child with the specified name and path and returns it as an integer.
     */
    public int getInt(int defaultValue, String... keys) {
        return getInt(true, defaultValue, keys);
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a float.
     */
    public float getFloat(boolean showWarnings, float defaultValue, String... keys) {
        if (get(false, keys).isNull()) {
            if (showWarnings) {
                log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, keys).jsonValue.asFloat();
    }
    
    public float getFloatWithFallback(JsonEntry fallback, boolean showWarnings, float defaultValue, String... keys) {
        if (get(false, keys).isNumber()) {
            return getFloat(false, 0, keys);
        } else {
            return fallback.getFloat(showWarnings, defaultValue, keys);
        }
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a float.
     */
    public float getFloat(float defaultValue, String... keys) {
        return getFloat(true, defaultValue, keys);
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a string.
     */
    public String getString(boolean showWarnings, String defaultValue, String... keys) {
        if (get(false, keys).isNull()) {
            if (showWarnings) {
                log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, keys).jsonValue.asString();
    }
    
    public String getStringWithFallback(JsonEntry fallback, boolean showWarnings, String defaultValue, String... keys) {
        if (get(false, keys).isString()) {
            return getString(false, null, keys);
        } else {
            return fallback.getString(showWarnings, defaultValue, keys);
        }
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a string.
     */
    public String getString(String defaultValue, String... keys) {
        return getString(true, defaultValue, keys);
    }
    
    /**
     * Finds the child with the specified name and path and returns it as an integer array.
     */
    public int[] getIntArray(boolean showWarnings, int[] defaultValue, String... keys) {
        if (get(false, keys).isNull()) {
            if (showWarnings) {
                log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + ((defaultValue.length > 0) ? (defaultValue[0] + "..." + defaultValue[defaultValue.length - 1]) : "empty array") + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, keys).jsonValue.asIntArray();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as an integer array.
     */
    public int[] getIntArray(int[] defaultValue, String... keys) {
        return getIntArray(true, defaultValue, keys);
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a float array.
     */
    public float[] getFloatArray(boolean showWarnings, float[] defaultValue, String... keys) {
        if (get(false, keys).isNull()) {
            if (showWarnings) {
                log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + ((defaultValue.length > 0) ? (defaultValue[0] + "..." + defaultValue[defaultValue.length - 1]) : "empty array") + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, keys).jsonValue.asFloatArray();
    }
    
    public float[] getFloatArrayWithFallback(JsonEntry fallback, boolean showWarnings, float[] defaultValue, String... keys) {
        if (get(false, keys).isNull()) {
            return fallback.getFloatArray(showWarnings, defaultValue, keys);
           
        } else {
            return getFloatArray(false, null, keys);
        }
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a float array.
     */
    public float[] getFloatArray(float[] defaultValue, String... keys) {
        return getFloatArray(true, defaultValue, keys);
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a string array.
     */
    public String[] getStringArray(boolean showWarnings, String[] defaultValue, String... keys) {
        if (get(false, keys).isNull()) {
            if (showWarnings) {
                log("No value specified for key path " + keys[0] + "..." + keys[keys.length - 1] + " in entry: " + name + ", using default (" + ((defaultValue.length > 0) ? (defaultValue[0] + "..." + defaultValue[defaultValue.length - 1]) : "empty array") + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, keys).jsonValue.asStringArray();
    }
    
    /**
     * Finds the child with the specified name and path and returns it as a string array.
     */
    public String[] getStringArray(String[] defaultValue, String... keys) {
        return getStringArray(true, defaultValue, keys);
    }
    
    /**
     * Returns the child with the specified index of the child with the specified name and returns it as a string.
     */
    public String getString(boolean showWarnings, String defaultValue, String key, int index) {
        if (get(false, key).isNull()) {
            if (showWarnings) {
                log("No value specified for key " + key + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, key).getString(showWarnings, defaultValue, index);
    }
    
    /**
     * Returns the child with the specified index of the child with the specified name and returns it as a string.
     */
    public String getString(String defaultValue, String key, int index) {
        return getString(true, defaultValue, key, index);
    }
    
    /**
     * Returns the child with the specified name of the child with the specified index and returns it as a string.
     */
    public String getString(boolean showWarnings, String defaultValue, int index, String key) {
        if (get(false, index).isNull()) {
            if (showWarnings) {
                log("No value specified for index " + index + " in entry: " + name + ", using default (" + defaultValue + ")", WARNING);
            }
            return defaultValue;
        }
        return get(showWarnings, index).getString(showWarnings, defaultValue, key);
    }
    
    /**
     * Returns the child with the specified name of the child with the specified index and returns it as a string.
     */
    public String getString(String defaultValue, int index, String key) {
        return getString(true, defaultValue, index, key);
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
    
    public boolean isNumber() {
        if (isNull()) {
            return false;
        } else {
            return jsonValue.isNumber();
        }
    }
    
    public boolean isBoolean() {
        if (isNull()) {
            return false;
        }
        return jsonValue.isBoolean();
    }
    
    public boolean isString() {
        if (isNull()) {
            return false;
        }
        return jsonValue.isString();
    }
    
    public boolean isObject() {
        if (isNull()) {
            return false;
        }
        return jsonValue.isObject();
    }
    
    public boolean isNull() {
        return jsonValue == null;
    }
    
    @Override
    public String toString() {
        return jsonValue.toString();
    }
}
