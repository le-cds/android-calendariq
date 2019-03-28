package net.hypotenubel.calendariq.services;

import android.content.Context;
import android.content.SharedPreferences;

import net.hypotenubel.calendariq.calendar.ICalendarInterface;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Knows how to load appointments and turn them into
 */
public class AppointmentMessageGenerator {

    /** The maximum number of upcoming appointments to load. */
    private static final int MAX_APPOINTMENT_COUNT = 10;


    /**
     * Processes an appointment request by replying with the time of the next appointment.
     */
    public static List<Object> prepareAppointmentMessage(Context context) {
        // Obtain the list of active calendar IDs
        SharedPreferences preferences = Utilities.obtainSharedPreferences(context);
        Collection<Integer> activeCalIds = Utilities.loadActiveCalendarIds(preferences);

        // Obtain the upcoming appointments
        ICalendarInterface provider = Utilities.obtainCalendarProvider(context);
        List<Long> appointments = provider.loadUpcomingAppointments(
                MAX_APPOINTMENT_COUNT, activeCalIds);

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
