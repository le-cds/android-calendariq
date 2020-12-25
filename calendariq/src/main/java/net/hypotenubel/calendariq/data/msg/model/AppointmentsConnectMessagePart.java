package net.hypotenubel.calendariq.data.msg.model;

import android.content.Context;

import net.hypotenubel.calendariq.data.Preferences;
import net.hypotenubel.calendariq.data.calendar.source.ICalendarSource;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a list of upcoming appointments that can be encoded to be sent through Connect IQ. An
 * instance of this class can load appointments from an {@link ICalendarSource} according to a set
 * number of options:
 * <dl>
 *     <dt>Calendar IDs</dt>
 *     <dd>IDs of calendars appointments are loaded from.</dd>
 *
 *     <dt>Max Appointments</dt>
 *     <dd>The maximum number of appointments loaded.</dd>
 *
 *     <dt>Max Days</dt>
 *     <dd>How far in the future appointments may lie.</dd>
 * </dl>
 * <p>
 *     Configure through {@code with...} methods and call {@link #loadAppointments(ICalendarSource)}
 *     to actually load appointments.
 * </p>
 */
public class AppointmentsConnectMessagePart implements IConnectMessagePart {

    /** IDs of the calendars the appointments are loaded from. */
    private final Set<Integer> calendarIDs = new HashSet<>();
    /** The maximum number of appointments to be loaded. */
    private int maxAppointments = 10;
    /** How far in the future the appointments may be. */
    private int maxDays = 7;
    /** The actual list of appointments. */
    private final List<Long> appointments = new ArrayList<>();

    /**
     * Loads all the required settings from the preferences and returns the resulting list of
     * appointments.
     */
    public static AppointmentsConnectMessagePart fromPreferences(Context context) {
        return new AppointmentsConnectMessagePart()
                .withActiveCalendarIDs(Preferences.ACTIVE_CALENDARS.loadIntSet(context))
                .withMaxAppointments(Preferences.APPOINTMENTS.loadInt(context))
                .withMaxDays(Preferences.INTERVAL.loadInt(context))
                .loadAppointments(Utilities.obtainCalendarProvider(context));
    }

    /**
     * Creates a new instance initialized with default values.
     */
    public AppointmentsConnectMessagePart() {
    }

    public AppointmentsConnectMessagePart withActiveCalendarIDs(Collection<Integer> calendarIDs) {
        this.calendarIDs.addAll(calendarIDs);
        return this;
    }

    public AppointmentsConnectMessagePart withMaxAppointments(int maxAppointments) {
        this.maxAppointments = maxAppointments;
        return this;
    }

    public AppointmentsConnectMessagePart withMaxDays(int maxDays) {
        this.maxDays = maxDays;
        return this;
    }

    public AppointmentsConnectMessagePart loadAppointments(ICalendarSource calendarSource) {
        appointments.clear();
        appointments.addAll(calendarSource.loadUpcomingAppointments(
                maxAppointments,
                maxDays,
                calendarIDs));
        return this;
    }

    @Override
    public void encodeAndAppend(List<Object> target) {
        target.add(appointments.size());

        // Timestamp in seconds UTC. Note that, at least according to the documentation, MonkeyC
        // doesn't support Java's long type, just ints. The following cast doesn't truncate until
        // 2038-01-19 at 03:14:07
        for (long appointmentTime : appointments) {
            target.add((int) appointmentTime);
        }
    }

}
