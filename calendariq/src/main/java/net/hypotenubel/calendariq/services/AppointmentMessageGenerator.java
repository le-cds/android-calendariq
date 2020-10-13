package net.hypotenubel.calendariq.services;

import android.content.Context;

import net.hypotenubel.calendariq.calendar.ICalendarSource;
import net.hypotenubel.calendariq.util.Preferences;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Knows how to load appointments and turn them into
 */
public class AppointmentMessageGenerator {

    /**
     * Processes an appointment request by replying with the time of the next appointment.
     */
    public static List<Object> prepareAppointmentMessage(Context context) {
        // Obtain the list of active calendar IDs
        Collection<Integer> activeCalIds = Preferences.ACTIVE_CALENDARS.loadIntSet(context);

        // Obtain the upcoming appointments
        ICalendarSource provider = Utilities.obtainCalendarProvider(context);
        List<Long> appointments = provider.loadUpcomingAppointments(
                Preferences.APPOINTMENTS.loadInt(context),
                Preferences.INTERVAL.loadInt(context),
                activeCalIds);

        return packAppointmentReply(appointments);
    }

    /**
     * Assembles a message describing the time of the given appointment, ready to be sent to the
     * device.
     */
    private static List<Object> packAppointmentReply(List<Long> nextAppointments) {
        List<Object> reply = new ArrayList<>();

        // The message consists of a timestamp, the number of appointments to follow, and the
        // actual appointments. Times are in seconds UTC. Note that, at least according to the
        // documentation, MonkeyC doesn't support Java's long type, just ints. The following casts
        // don't truncate until // 2038-01-19 at 03:14:07
        reply.add((int) (System.currentTimeMillis() / 1000));
        reply.add(nextAppointments.size());

        for (long app : nextAppointments) {
            reply.add((int) app);
        }

        return reply;
    }

}
