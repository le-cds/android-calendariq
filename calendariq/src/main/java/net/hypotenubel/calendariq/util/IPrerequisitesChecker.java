package net.hypotenubel.calendariq.util;

import android.content.Context;

/**
 * Implementations of this interface check whether Garmin Connect is installed and whether we have
 * access to the calendar. Most clients will simply call {@link #arePrerequisitesMet(Context)}.
 */
public interface IPrerequisitesChecker {

    /** Package ID of the Garmin ConnectIQ app. Used to ensure its existence on the phone. */
    String GARMIN_PACKAGE_ID = "com.garmin.android.apps.connectmobile";

    /**
     * Checks whether or not Garmin Connect is currently installed.
     */
    boolean isGarminConnectInstalled(Context context);

    /**
     * Checks whether or not we have permission to access the calendar.
     */
    boolean isCalendarAccessible(Context context);

    /**
     * Convenience methods that checks whether all of our prerequisites are met.
     */
    default boolean arePrerequisitesMet(Context context) {
        return isGarminConnectInstalled(context) && isCalendarAccessible(context);
    }

    // TODO This method must be removed once everything is properly dependency-injected
    boolean isEmulator();

}
