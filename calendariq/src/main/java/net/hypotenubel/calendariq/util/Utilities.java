package net.hypotenubel.calendariq.util;

/**
 * Contains glorious utility methods.
 */
public final class Utilities {

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
