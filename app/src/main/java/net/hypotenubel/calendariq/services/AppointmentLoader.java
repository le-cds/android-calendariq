package net.hypotenubel.calendariq.services;

import android.content.Context;
import android.content.SharedPreferences;

import net.hypotenubel.calendariq.calendar.CalendarDescriptor;
import net.hypotenubel.calendariq.calendar.ICalendarDescriptorProvider;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Knows how to load appointments and turn them into
 */
public class AppointmentLoader {

    /**
     * Processes an appointment request by replying with the time of the next appointment.
     */
    public static List<Object> prepareAppointmentMessage(Context context) {
        CalendarDescriptor nextAppointment =
                calendarWithNextAppointment(loadActiveCalendars(context));
        List<Object> reply = null;

        if (nextAppointment != null) {
            return packAppointmentReply(nextAppointment.getUpcomingAppointment());
        } else {
            return packAppointmentReply(0);
        }
    }

    /**
     * Loads the active calendars that we can take events from.
     */
    private static List<CalendarDescriptor> loadActiveCalendars(Context context) {
        ICalendarDescriptorProvider calendarProvider = Utilities.obtainCalendarProvider(context);
        SharedPreferences preferences = Utilities.obtainSharedPreferences(context);

        List<CalendarDescriptor> activeCalendars = new ArrayList<>();
        for (int calendarId : Utilities.loadActiveCalendarIds(preferences)) {
            CalendarDescriptor calendar = calendarProvider.loadCalendar(calendarId);
            if (calendar != null) {
                activeCalendars.add(calendar);
            }
        }
        return activeCalendars;
    }

    /**
     * Finds the calendar with the nearest upcoming appointment among the list of calendars, if any.
     */
    private static CalendarDescriptor calendarWithNextAppointment(
            List<CalendarDescriptor> calendars) {

        CalendarDescriptor result = null;

        for (CalendarDescriptor calendar : calendars) {
            if (calendar.hasUpcomingAppointment()) {
                if (result == null) {
                    result = calendar;
                } else if (calendar.getUpcomingAppointment() < result.getUpcomingAppointment()) {
                    result = calendar;
                }
            }
        }

        return result;
    }

    /**
     * Assembles a message describing the time of the given appointment, ready to be sent to the
     * device.
     */
    private static List<Object> packAppointmentReply(long nextAppointment) {
        List<Object> reply = new ArrayList<>();

        // The message consists of the time of the next appointment and the current timestamp, both
        // in seconds UTC. Note that, at least according to the documentation, MonkeyC doesn't
        // support Java's long type, just ints. The following casts don't truncate until
        // 2038-01-19 at 03:14:07
        reply.add((int) nextAppointment);
        reply.add((int) (System.currentTimeMillis() / 1000));

        return reply;
    }

}
