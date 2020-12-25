package com.deo.flapd.utils;

import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.DUtils.logException;

public class JsonEntry extends com.badlogic.gdx.utils.JsonValue {


    public JsonEntry(ValueType type) {
        super(type);
    }

    /**
     * @param value May be null.
     */
    public JsonEntry(String value) {
        super(value);
    }

    public JsonEntry(double value) {
        super(value);
    }

    public JsonEntry(long value) {
        super(value);
    }

    public JsonEntry(double value, String stringValue) {
        super(value, stringValue);
    }

    public JsonEntry(long value, String stringValue) {
        super(value, stringValue);
    }

    public JsonEntry(boolean value) {
        super(value);
    }

    /**
     * Returns the child at the specified index. This requires walking the linked list to the specified entry, see
     * {@link JsonEntry} for how to iterate efficiently.
     *
     * @param index
     * @return May be null.
     */
    @Override
    public JsonEntry get(int index) {
        try {
            return (JsonEntry) super.get(index);
        } catch (Exception e) {
            log("\n No key named " + name + ", trace info: " + trace());
            logException(e);
        }
        return this;
    }

    /**
     * Returns the child with the specified name.
     *
     * @param name
     * @return May be null.
     */
    @Override
    public JsonEntry get(String name) {
        try {
            return (JsonEntry) super.get(name);
        } catch (Exception e) {
            log("\n No key named " + name + ", trace info: " + trace());
            logException(e);
        }
        return this;
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
    @Override
    public JsonEntry parent() {
        if (!super.parent().isNull()) {
            return (JsonEntry) super.parent();
        } else {
            log("\n Json entry " + name + " has no parent, first child: " + child());
            return this;
        }
    }

    /**
     * Finds the child with the specified name and returns it as a string.
     *
     * @param name
     */
    @Override
    public String getString(String name) {
        return get(name).asString();
    }

    /**
     * Finds the child with the specified name and returns it as a float.
     *
     * @param name
     */
    @Override
    public float getFloat(String name) {
        return get(name).asFloat();
    }

    /**
     * Finds the child with the specified name and returns it as an int.
     *
     * @param name
     */
    @Override
    public int getInt(String name) {
        return get(name).asInt();
    }

    /**
     * Finds the child with the specified name and returns it as a boolean.
     *
     * @param name
     */
    @Override
    public boolean getBoolean(String name) {
        return get(name).asBoolean();
    }

    /**
     * Finds the child with the specified name and path and returns it as a boolean.
     *
     * @param keys
     */
    public boolean getBoolean(String... keys) {
        return get(keys).asBoolean();
    }

    /**
     * Finds the child with the specified name and path and returns it as an integer.
     *
     * @param keys
     */
    public int getInt(String... keys) {
        return get(keys).asInt();
    }

    /**
     * Finds the child with the specified name and path and returns it as a float.
     *
     * @param keys
     */
    public float getFloat(String... keys) {
        return get(keys).asFloat();
    }

    /**
     * Finds the child with the specified name and path and returns it as a string.
     *
     * @param keys
     */
    public String getString(String... keys) {
        return get(keys).asString();
    }

    /**
     * Finds the child with the specified name and path and returns it as a boolean array.
     *
     * @param keys
     */
    public boolean[] getBooleanArray(String... keys) {
        return get(keys).asBooleanArray();
    }

    /**
     * Finds the child with the specified name and path and returns it as an integer array.
     *
     * @param keys
     */
    public int[] getIntArray(String... keys) {
        return get(keys).asIntArray();
    }

    /**
     * Finds the child with the specified name and path and returns it as a float array.
     *
     * @param keys
     */
    public float[] getFloatArray(String... keys) {
        return get(keys).asFloatArray();
    }

    /**
     * Finds the child with the specified name and path and returns it as a string array.
     *
     * @param keys
     */
    public String[] getStringArray(String... keys) {
        return get(keys).asStringArray();
    }

    // overriding useless default value method so it doesn't interfere with getString(String... keys)

    @Override
    public String getString(String key, String secondKey) {
        return get(key).getString(secondKey);
    }
}
