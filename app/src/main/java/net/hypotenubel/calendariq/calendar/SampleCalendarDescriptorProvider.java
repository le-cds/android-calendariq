package net.hypotenubel.calendariq.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A calendar provider that returns sample data. To be used in the Android emulator, since that
 * usually doesn't have proper calendar data available.
 */
public class SampleCalendarDescriptorProvider implements ICalendarDescriptorProvider {

    /** The number of milliseconds that pass by each hour. */
    private static final long MILLISECONDS_PER_HOUR = 60 * 60 * 1000;

    /** Names to be used for the sample calendars. */
    private static final String[] CALENDAR_NAMES = {
            "Work Appointments", "Private Appointments", "Fun Appointments", "Other Appointments"
    };

    /** Names to be used for the sample accounts. */
    private static final String[] ACCOUNT_NAMES = {
            "Excellent Google Account", "Glorious CalDAV Provider", "Birthday Account"
    };

    /** Colours to be used for the sample calendars. */
    private static final int[] CALENDAR_COLOURS = {
            0xFF0000, 0x00FF00, 0x0000FF, 0xFF00FF
    };


    @Override
    public List<CalendarDescriptor> getAvailableCalendars() {
        List<CalendarDescriptor> calendars = new ArrayList<>();

        // Each calendar exists once per account
        int calCount = CALENDAR_NAMES.length * ACCOUNT_NAMES.length;
        for (int id = 0; id < calCount; id++) {
            calendars.add(loadCalendar(id));
        }

        return calendars;
    }

    @Override
    public CalendarDescriptor loadCalendar(int id) {
        // Check if the ID is valid
        if (id < 0 || id >= CALENDAR_NAMES.length * ACCOUNT_NAMES.length) {
            return null;
        }

        int calIndex = id % CALENDAR_NAMES.length;
        int accIndex = id / CALENDAR_NAMES.length;

        // Compute next appointment as being an hour in the future
        Calendar nowCal = Calendar.getInstance();
        long nowMillis = nowCal.getTimeInMillis();
        long hourFromNowMillis = nowMillis + MILLISECONDS_PER_HOUR;

        return new CalendarDescriptor(
                id,
                CALENDAR_NAMES[calIndex],
                ACCOUNT_NAMES[accIndex],
                CALENDAR_COLOURS[calIndex],
                hourFromNowMillis / 1000
        );
    }

}
