package net.hypotenubel.calendariq.data.calendar.source;

import net.hypotenubel.calendariq.data.calendar.model.CalendarDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * Implementations of this interface can load calendars and their upcoming appointments. The
 * standard implementation, {@link AndroidCalendarSource}, loads the calendars registered with
 * Android. Other implementations might return test data.
 *
 * @see AndroidCalendarSource
 */
public interface ICalendarSource {

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
     * @param maxDays maximum number of upcoming days to load appointments from.
     * @param from IDs of calendars the appointments may come from.
     * @return list of appointments, given in seconds UTC.
     */
    List<Long> loadUpcomingAppointments(int maxCount, int maxDays, Collection<Integer> from);

}
