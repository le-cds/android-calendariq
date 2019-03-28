package net.hypotenubel.calendariq.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import com.garmin.android.connectiq.ConnectIQ;

import net.hypotenubel.calendariq.calendar.AndroidCalendarInterface;
import net.hypotenubel.calendariq.calendar.ICalendarInterface;
import net.hypotenubel.calendariq.calendar.SampleCalendarInterface;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.core.content.ContextCompat;

/**
 * Contains glorious utility methods.
 */
public final class Utilities {

    /** Identifier of our watchface running on the watch. */
    public static final String APP_ID = "d7d720e4-e397-43fe-b4ef-7df656ac5766";

    /** Preference key for the active calendars setting. */
    public static final String PREF_ACTIVE_CALENDARS = "activeCalendars";


    /**
     * Prevent instantiation.
     */
    private Utilities() {
    }


    /**
     * Returns the log tag the given class should use for log messages.
     *
     * @param clazz the class.
     * @return the log tag to be used.
     */
    public static String logTag(Class<?> clazz) {
        return "CalendarIQ." + clazz.getSimpleName();
    }

    /**
     * Returns the ConnectIQ connection type to use.
     *
     * @return the connection type.
     */
    public static ConnectIQ.IQConnectType getIQConnectType() {
        //return ConnectIQ.IQConnectType.TETHERED;
        return ConnectIQ.IQConnectType.WIRELESS;
    }

    /**
     * Makes an effort to check whether we're currently running on an emulator.
     *
     * @return {@code true} if we're running on an emulator.
     */
    public static boolean isEmulator() {
        // This heap of tests comes straight from trusty internet sources
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || (Build.PRODUCT).equals("google_sdk");
    }

    /**
     * Returns an instance of {@link ICalendarInterface} that is appropriate for the given context.
     * When running in an emulator, a sample data provider is returned. Otherwise, a provider is
     * returned that provides access to the user's Android calendars.
     *
     * @param context the context from which this method is called.
     * @return a suitable calendar provider.
     */
    public static ICalendarInterface obtainCalendarProvider(Context context) {
        if (Utilities.isEmulator()) {
            return new SampleCalendarInterface();
        } else {
            return new AndroidCalendarInterface(context);
        }
    }

    /**
     * Checks whether we have permission to read calendars.
     *
     * @param context the context from which this method is called.
     * @return {@code true} if we can read calendars.
     */
    public static boolean ensureCalendarPermission(Context context) {
        int permissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns the shared preferences that can be used all over the application.
     *
     * @param context a context to retrieve the preferences from.
     * @return the shared preferences.
     */
    public static SharedPreferences obtainSharedPreferences(Context context) {
        return context.getSharedPreferences(
                context.getPackageName() + ".CalendarIQ",
                Context.MODE_PRIVATE);
    }

    /**
     * Loads the set of active calendar IDs from the shared preferences.
     */
    public static Set<Integer> loadActiveCalendarIds(SharedPreferences preferences) {
        Set<String> idStrings = preferences.getStringSet(
                Utilities.PREF_ACTIVE_CALENDARS, Collections.<String>emptySet());

        Set<Integer> ids = new HashSet<>();
        for (String idString : idStrings) {
            ids.add(Integer.parseInt(idString));
        }

        return ids;
    }
}
