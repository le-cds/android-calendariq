package net.hypotenubel.calendariq.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.garmin.android.connectiq.ConnectIQ;

import net.hypotenubel.calendariq.calendar.AndroidCalendarSource;
import net.hypotenubel.calendariq.calendar.ICalendarSource;
import net.hypotenubel.calendariq.calendar.SampleCalendarSource;

import androidx.core.content.ContextCompat;

/**
 * Contains glorious utility methods.
 */
public final class Utilities {

    /** Identifier of our watchface running on the watch. */
    public static final String APP_ID = "d7d720e4-e397-43fe-b4ef-7df656ac5766";


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
     * Returns an instance of {@link ICalendarSource} that is appropriate for the given context.
     * When running in an emulator, a sample data provider is returned. Otherwise, a provider is
     * returned that provides access to the user's Android calendars.
     *
     * @param context the context from which this method is called.
     * @return a suitable calendar provider.
     */
    public static ICalendarSource obtainCalendarProvider(Context context) {
        if (Utilities.isEmulator()) {
            return new SampleCalendarSource();
        } else {
            return new AndroidCalendarSource(context);
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

}
