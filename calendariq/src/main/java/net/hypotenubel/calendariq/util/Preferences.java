package net.hypotenubel.calendariq.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * An enumeration of all our preferences along with their default values and types. A preference's
 * value can be retrieved and stored by calling one of its {@code loadTYPE} and {@code storeTYPE}
 * methods, respectively. Calling the wrong method for a type will result in a
 * {@link ClassCastException}.
 *
 * <p>This class currently supports the following data types for preferences:</p>
 * <ul>
 *     <li>{@code String}</li>
 *     <li>{@code int}</li>
 *     <li>{@code Set<Integer>}</li>
 * </ul>
 */
public enum Preferences {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Preference Definitions

    APPOINTMENTS("appointments", "10", Integer.class, null),
    INTERVAL("interval", "7", Integer.class, null),
    FREQUENCY("frequency", "15", Integer.class, null),
    LAST_SYNC("last_sync", "", String.class, null),
    ACTIVE_CALENDARS("activeCalendars", null, Set.class, Integer.class);


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Creation

    /**
     * The preference's key.
     */
    private final String key;
    /**
     * The preference's default value.
     */
    private final String defaultValue;
    /**
     * The preference's type. Determines which load method can be called.
     */
    private final Class<?> type;
    /**
     * For collections, this is the type of the collection's elements.
     */
    private final Class<?> elementType;


    Preferences(String key, String defaultValue, Class<?> type, Class<?> elementType) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.type = type;
        this.elementType = elementType;
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
    private void ensureProperType(Class<?> expectedType) {
        ensureProperType(expectedType, null);
    }

    /**
     * Ensures that the collection type the caller expects matches the preference's type.
     */
    private void ensureProperType(Class<?> expectedType, Class<?> expectedElementType) {
        // The expected type could be wrong
        boolean wrongType = !expectedType.equals(type);

        // There could be an unexpected element type
        wrongType |= expectedElementType == null && elementType != null;
        wrongType |= expectedElementType != null && elementType == null;

        // There could be a wrong element type
        wrongType |= expectedElementType != null && !expectedElementType.equals(elementType);

        if (wrongType) {
            String elementTypeName = elementType != null
                    ? "<" + elementType.getName() + ">"
                    : "";
            throw new ClassCastException("Preference of type " + type.getName() + elementTypeName
                    + " loaded with wrong load method.");
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
     * Loads the preference from the shared preferences that belong to the context. This method does
     * not care about the default value and always returns an empty set for non-existent
     * preferences.
     */
    private Set<String> loadSet(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(key, new HashSet<>());
    }

    /**
     * Stores the value under the preference.
     */
    private void store(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Stores the value under the preference.
     */
    private void storeSet(Context context, Set<String> value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putStringSet(key, value).apply();
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

    /**
     * Loads a set of integers.
     */
    public Set<Integer> loadIntSet(Context context) {
        ensureProperType(Set.class, Integer.class);
        Set<String> encodedData = loadSet(context);

        Set<Integer> convertedData = new HashSet<>();
        for (String s : encodedData) {
            convertedData.add(Integer.parseInt(s));
        }

        return convertedData;
    }

    /**
     * Stores a set of integers.
     */
    public void storeIntSet(Context context, Set<Integer> value) {
        Set<String> valueStrings = new HashSet<>();
        for (Integer val : value) {
            valueStrings.add(Integer.toString(val));
        }

        storeSet(context, valueStrings);
    }

}
