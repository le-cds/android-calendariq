package net.hypotenubel.calendariq.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * A calendar provider that returns sample data. To be used in the Android emulator, since that
 * usually doesn't have proper calendar data available.
 */
public class SampleCalendarInterface implements ICalendarInterface {

    /** The number of seconds that pass by each half hour. */
    private static final long SECONDS_PER_HALF_HOUR = 30 * 60;

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
            int calIndex = id % CALENDAR_NAMES.length;
            int accIndex = id / CALENDAR_NAMES.length;

            calendars.add(new CalendarDescriptor(
                    id,
                    CALENDAR_NAMES[calIndex],
                    ACCOUNT_NAMES[accIndex],
                    CALENDAR_COLOURS[calIndex]
            ));
        }

        return calendars;
    }

    @Override
    public List<Long> loadUpcomingAppointments(int maxCount, Collection<Integer> from) {
        // Return appointments in 30 minute increments, starting in 30 minutes
        Calendar nowCal = Calendar.getInstance();
        long nowMillis = nowCal.getTimeInMillis();

        List<Long> result = new ArrayList<>();
        for (int i = 1; i <= maxCount; i++) {
            result.add(nowMillis + i * SECONDS_PER_HALF_HOUR);
        }
        return result;
    }

}
