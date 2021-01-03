package net.hypotenubel.calendariq.util;

import java.util.Arrays;
import java.util.List;

/**
 * Contains glorious utility methods.
 */
public final class Utilities {

    // TODO Move this to a configuration file or something
    /** Identifier of our watchface running on the watch. */
    public static final List<String> APP_IDS = Arrays.asList(
            "d7d720e4-e397-43fe-b4ef-7df656ac5766",    // Facey McWatchface Debug Version
            "1a8f8f5b-b8f4-43e9-b16f-82d9d3eceafb");   // Facey McWatchface Release Version


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

}
