package net.hypotenubel.calendariq.calendar;

import java.util.Collection;
import java.util.List;

/**
 * Implementations of this interface can load calendars.
 */
public interface ICalendarInterface {

    /**
     * Load all available calendars.
     *
     * @return possibly empty list of available calendars.
     */
    List<CalendarDescriptor> getAvailableCalendars();

    /**
     * Loads the upcoming appointments from the calendars with the given IDs.
     *
     * @param maxCount maximum number of appointments to load.
     * @param from IDs of calendars the appointments may come from.
     * @return list of appointments, given in seconds UTC.
     */
    List<Long> loadUpcomingAppointments(int maxCount, Collection<Integer> from);

}
