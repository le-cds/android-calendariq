package net.hypotenubel.calendariq.util;

import android.content.Context;

import net.hypotenubel.calendariq.data.calendar.source.AndroidCalendarSource;
import net.hypotenubel.calendariq.data.calendar.source.ICalendarSource;
import net.hypotenubel.calendariq.data.calendar.source.SampleCalendarSource;

import java.util.Arrays;
import java.util.List;

/**
 * Contains glorious utility methods.
 */
public final class Utilities {

    /** Identifier of our watchface running on the watch. */
    public static final List<String> APP_IDS = Arrays.asList(
            "d7d720e4-e397-43fe-b4ef-7df656ac5766",    // Facey McWatchface Debug Version
            "1a8f8f5b-b8f4-43e9-b16f-82d9d3eceafb");   // Facey McWatchface Release Version


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
    // Emulation

    /**
     * Returns an instance of {@link ICalendarSource} that is appropriate for the given context.
     * When running in an emulator, a sample data provider is returned. Otherwise, a provider is
     * returned that provides access to the user's Android calendars.
     *
     * TODO Remove as soon as the ICalendarSource is properly dependency-injected everywhere
     *
     * @param context the context from which this method is called.
     * @return a suitable calendar provider.
     */
    public static ICalendarSource obtainCalendarProvider(Context context) {
        if (new DefaultPrerequisitesChecker().isEmulator()) {
            return new SampleCalendarSource();
        } else {
            return new AndroidCalendarSource(context);
        }
    }

}
