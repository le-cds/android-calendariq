package net.hypotenubel.calendariq.data.calendar.source;

import net.hypotenubel.calendariq.data.calendar.model.CalendarDescriptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * A calendar provider that returns sample data. To be used in the Android emulator, since that
 * usually doesn't have proper calendar data available.
 */
public class SampleCalendarSource implements ICalendarSource {

    /** The number of seconds that pass by each half hour. */
    private static final long SECONDS_PER_HALF_HOUR = 30 * 60;

    /** List of calendar descriptors returned by this thing. */
    private List<CalendarDescriptor> descriptors;


    @Override
    public List<CalendarDescriptor> getAvailableCalendars() {
        if (descriptors == null) {
            buildDescriptors();
        }

        return descriptors;
    }

    @Override
    public List<Long> loadUpcomingAppointments(int maxCount, int maxDays,
                                               Collection<Integer> from) {

        // Return appointments in 30 minute increments, starting in 30 minutes
        Calendar nowCal = Calendar.getInstance();
        long nowInSeconds = nowCal.getTimeInMillis() / 1000;

        List<Long> result = new ArrayList<>();
        for (int i = 1; i <= maxCount; i++) {
            result.add(nowInSeconds + i * SECONDS_PER_HALF_HOUR);
        }
        return result;
    }

    /**
     * Builds an assortment of example calendars.
     */
    private void buildDescriptors() {
        descriptors = new CalendarBuilder()
                .newAccount("Accountable Account")
                .newCalendar("Holidays", 0xfffdbd)

                .newAccount("Precious Private Account")
                .newCalendar("Friends", 0xe697ce)
                .newCalendar("Family", 0xed9191)

                .newAccount("Wretched Work Account")
                .newCalendar("Meetings", 0xa6c2e3)
                .newCalendar("Clients", 0xade0da)
                .newCalendar("Vacation", 0xbaed93)

                .getDescriptors();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Sample Data Model

    /**
     * Helper class for building calendar descriptors conveniently.
     */
    private static class CalendarBuilder {

        /** Name of the account new calendars will be associated with. */
        private String currentAccountName;
        /** ID assigned to the next calendar. */
        private int nextCalendarId = 0;
        /** Descriptors of the calendars we have created. */
        private final List<CalendarDescriptor> calendarDescriptors = new ArrayList<>();

        /**
         * Starts a new account that new calendars will be associated with.
         */
        private CalendarBuilder newAccount(String accountName) {
            currentAccountName = accountName;
            return this;
        }

        /**
         * Add a new calendar.
         */
        private CalendarBuilder newCalendar(String name, int color) {
            CalendarDescriptor descriptor = new CalendarDescriptor(
                    nextCalendarId,
                    name,
                    currentAccountName,
                    color);
            calendarDescriptors.add(descriptor);

            nextCalendarId++;
            return this;
        }

        /**
         * Returns the descriptors of all calendars we have created.
         */
        private List<CalendarDescriptor> getDescriptors() {
            return calendarDescriptors;
        }

    }

}
