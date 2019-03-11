package net.hypotenubel.calendariq.calendar;

import java.util.List;

/**
 * Implementations of this interface can load calendars and their upcoming appointment.
 */
public interface ICalendarDescriptorProvider {

    /**
     * Load all available calendars.
     *
     * @return possibly empty list of available calendars.
     */
    public List<CalendarDescriptor> getAvailableCalendars();

    /**
     * Load the calendar with the given identifier.
     *
     * @param id
     *         the calendar's identifier.
     * @return the calendar, or {@code null} if none could be loaded.
     */
    public CalendarDescriptor loadCalendar(int id);

}
