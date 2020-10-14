package net.hypotenubel.calendariq.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.garmin.android.connectiq.ConnectIQ;

import net.hypotenubel.calendariq.calendar.AndroidCalendarSource;
import net.hypotenubel.calendariq.calendar.ICalendarSource;
import net.hypotenubel.calendariq.calendar.SampleCalendarSource;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains glorious utility methods.
 */
public final class Utilities {

    /** Identifier of our watchface running on the watch. */
    public static final List<String> APP_IDS = Arrays.asList(
            "d7d720e4-e397-43fe-b4ef-7df656ac5766");
    /** Package ID of the Garmin ConnectIQ app. Used to ensure its existence on the phone. */
    public static final String GARMIN_PACKAGE_ID = "com.garmin.android.apps.connectmobile";


    /**
     * Prevent instantiation.
     */
    private Utilities() {
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Logging

    /**
     * Returns the log tag the given class should use for log messages.
     *
     * @param clazz the class.
     * @return the log tag to be used.
     */
    public static String logTag(Class<?> clazz) {
        return "CalendarIQ." + clazz.getSimpleName();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ConnectIQ

    /**
     * Checks whether ConnectIQ is installed.
     */
    public static boolean isConnectIQInstalled(Context context) {
        try {
            // Try to find the app, which must also correspond to a minimum version (see ConnectIQ
            // mobile SDK code)
            PackageInfo info = context.getPackageManager().getPackageInfo(GARMIN_PACKAGE_ID, 0);
            return info.versionCode >= 2000;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
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


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Emulation

    /**
     * Makes an effort to check whether we're currently running on an emulator.
     *
     * @return {@code true} if we're running on an emulator.
     */
    public static boolean isEmulator() {
        // This just tests for the Android SDK emulator
        return Build.BRAND.equals("google")
                && Build.MANUFACTURER.equals("Google")
                && Build.PRODUCT.startsWith("sdk_gphone_")
                && Build.MODEL.startsWith("sdk_gphone_")
                && Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                && (Build.FINGERPRINT.endsWith(":user/release-keys")
                    || Build.FINGERPRINT.endsWith(":userdebug/dev-keys"));
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


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Permissions

    /**
     * Checks whether we have permission to read calendars.
     *
     * @param context the context from which this method is called.
     * @return {@code true} if we can read calendars.
     */
    public static boolean checkCalendarPermission(Context context) {
        int permissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

}
