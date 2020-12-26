package com.deo.flapd.utils;

import com.badlogic.gdx.utils.JsonValue;

import static com.deo.flapd.utils.DUtils.log;

public class JsonEntry {

    private JsonValue jsonValue;
    public String name;
    public int size;

    public JsonEntry(JsonValue value) {
        jsonValue = value;
        name = value.name;
        size = value.size;
    }

    /**
     * Returns the child at the specified index. This requires walking the linked list to the specified entry, see
     * {@link JsonEntry} for how to iterate efficiently.
     *
     * @param index
     * @return May be null.
     */
    public JsonEntry get(int index) {
        if (jsonValue.get(index) == null) {
            log("\n No key with index " + index + ", trace info: " + jsonValue.trace());
            return null;
        }
        return new JsonEntry(jsonValue.get(index));
    }

    /**
     * Returns the child with the specified name.
     *
     * @param name
     * @return May be null.
     */

    public JsonEntry get(String name) {

        if (jsonValue.get(name) == null) {
            log("\n No key named " + name + ", trace info: " + jsonValue.trace());
            return null;
        }
        return new JsonEntry(jsonValue.get(name));

    }

    /**
     * Returns the child with the specified name and path.
     *
     * @param keys
     * @return May be null.
     */
    public JsonEntry get(String... keys) {
        JsonEntry entry = this;
        for (int i = 0; i < keys.length; i++) {
            entry = entry.get(keys[i]);
        }
        return entry;
    }

    /**
     * Returns the child with the specified index of the child with the specified name.
     *
     * @param key
     * @param index
     * @return May be null.
     */
    public JsonEntry get(String key, int index) {
        return get(key).get(index);
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
            log("\n Json entry " + jsonValue.name + " has no parent, first child: " + jsonValue.child());
            return null;
        }
    }

    /**
     * Finds the child with the specified index and returns it as a string.
     *
     * @param index
     */
    public String getString(int index) {
        return get(index).jsonValue.asString();
    }

    /**
     * Finds the child with the specified name and returns it as a string.
     *
     * @param name
     */
    public String getString(String name) {
        return get(name).jsonValue.asString();
    }

    /**
     * Finds the child with the specified name and returns it as a float.
     *
     * @param name
     */
    public float getFloat(String name) {
        return get(name).jsonValue.asFloat();
    }

    /**
     * Finds the child with the specified index and returns it as a float.
     *
     * @param index
     */
    public float getFloat(int index) {
        return get(index).jsonValue.asFloat();
    }

    /**
     * Finds the child with the specified name and returns it as an int.
     *
     * @param name
     */
    public int getInt(String name) {
        return get(name).jsonValue.asInt();
    }

    /**
     * Finds the child with the specified name and returns it as a boolean.
     *
     * @param name
     */
    public boolean getBoolean(String name) {
        return get(name).jsonValue.asBoolean();
    }

    /**
     * Finds the child with the specified name and path and returns it as a boolean.
     *
     * @param keys
     */
    public boolean getBoolean(String... keys) {
        return get(keys).jsonValue.asBoolean();
    }

    /**
     * Finds the child with the specified name and path and returns it as an integer.
     *
     * @param keys
     */
    public int getInt(String... keys) {
        return get(keys).jsonValue.asInt();
    }

    /**
     * Finds the child with the specified name and path and returns it as a float.
     *
     * @param keys
     */
    public float getFloat(String... keys) {
        return get(keys).jsonValue.asFloat();
    }

    /**
     * Finds the child with the specified name and path and returns it as a string.
     *
     * @param keys
     */
    public String getString(String... keys) {
        return get(keys).jsonValue.asString();
    }

    /**
     * Finds the child with the specified name and path and returns it as a boolean array.
     *
     * @param keys
     */
    public boolean[] getBooleanArray(String... keys) {
        return get(keys).jsonValue.asBooleanArray();
    }

    /**
     * Finds the child with the specified name and path and returns it as an integer array.
     *
     * @param keys
     */
    public int[] getIntArray(String... keys) {
        return get(keys).jsonValue.asIntArray();
    }

    /**
     * Finds the child with the specified name and path and returns it as a float array.
     *
     * @param keys
     */
    public float[] getFloatArray(String... keys) {
        return get(keys).jsonValue.asFloatArray();
    }

    /**
     * Finds the child with the specified name and path and returns it as a string array.
     *
     * @param keys
     */
    public String[] getStringArray(String... keys) {
        return get(keys).jsonValue.asStringArray();
    }

    /**
     * Returns the child with the specified index of the child with the specified name and returns it as a string.
     *
     * @param key
     * @param index
     * @return May be null.
     */
    public String getString(String key, int index) {
        return get(key).getString(index);
    }

    /**
     * Returns the child with the specified name of the child with the specified index and returns it as a string.
     *
     * @param key
     * @param index
     * @return May be null.
     */
    public String getString(int index, String key) {
        return get(index).getString(key);
    }

    public String asString() {
        return jsonValue.asString();
    }

    public int asInt() {
        return jsonValue.asInt();
    }

    public boolean isBoolean() {
        return jsonValue.isBoolean();
    }

}
