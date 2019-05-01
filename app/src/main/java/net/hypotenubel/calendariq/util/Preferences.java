package net.hypotenubel.calendariq.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Helper class to cope with preferences. Each preference has an associated type, and its value can
 * be retrieved and stored by calling one of its {@code loadTYPE} and {@code storeTYPE} methods,
 * respectively. Calling the wrong method for a type will result in a {@link ClassCastException}.
 */
public enum Preferences {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Preference Definitions

    APPOINTMENTS("appointments", "10", Integer.class),
    INTERVAL("interval", "7", Integer.class),
    FREQUENCY("frequency", "15", Integer.class),
    LAST_SYNC("last_sync", "", String.class);


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Creation

    /** The preference's key. */
    private final String key;
    /** The preference's default value. */
    private final String defaultValue;
    /** The preference's type. Determines which load method can be called. */
    private final Class<?> type;


    Preferences(String key, String defaultValue, Class<?> type) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.type = type;
    }


    /**
     * Returns this preference's key.
     */
    public String getKey() {
        return key;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Internal Utilities

    /**
     * Ensures that the type the caller expects matches the preference's type.
     */
    private void ensureProperType(Class<?> expected) {
        if (!expected.equals(type)) {
            throw new ClassCastException(
                    "Preference of type " + type.getName() + " loaded with wrong load method.");
        }
    }

    /**
     * Loads the preference from the shared preferences that belong to the context. Always returns
     * a string to be compatible with the Settings framework.
     */
    private String load(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, defaultValue);
    }

    /**
     * Stores the value under the preference.
     */
    private void store(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(key, value).apply();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Access

    /**
     * Loads a string preference.
     */
    public String loadString(Context context) {
        ensureProperType(String.class);
        return load(context);
    }

    /**
     * Stores a string preference.
     */
    public void storeString(Context context, String value) {
        ensureProperType(String.class);
        store(context, value);
    }

    /**
     * Loads an int preference.
     */
    public int loadInt(Context context) {
        ensureProperType(Integer.class);
        return Integer.parseInt(load(context));
    }

    /**
     * Stores an int preference.
     */
    public void storeInt(Context context, int value) {
        ensureProperType(Integer.class);
        store(context, String.valueOf(value));
    }

}
