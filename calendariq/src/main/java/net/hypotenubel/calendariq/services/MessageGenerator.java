package net.hypotenubel.calendariq.services;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import net.hypotenubel.calendariq.calendar.ICalendarSource;
import net.hypotenubel.calendariq.util.Preferences;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Knows how to assemble messages ready to be sent to devices. The message format is documented in
 * a file in the repository's root folder.
 */
public class MessageGenerator {

    /**
     * Processes an appointment request by replying with the time of the next appointment.
     */
    public static List<Object> prepareMessage(Context context) {
        List<Object> msg = new ArrayList<>();

        // Timestamp in seconds UTC. Note that, at least according to the documentation, MonkeyC
        // doesn't support Java's long type, just ints. The following cast doesn't truncate until
        // 2038-01-19 at 03:14:07
        msg.add((int) (System.currentTimeMillis() / 1000));

        // Add all remaining info
        appendAppointments(context, msg);
        appendSyncInterval(context, msg);
        appendBatteryState(context, msg);

        return msg;
    }

    private static void appendAppointments(Context context, List<Object> msg) {
        // Obtain the list of active calendar IDs
        Collection<Integer> activeCalIds = Preferences.ACTIVE_CALENDARS.loadIntSet(context);

        // Obtain the upcoming appointments
        ICalendarSource provider = Utilities.obtainCalendarProvider(context);
        List<Long> appointments = provider.loadUpcomingAppointments(
                Preferences.APPOINTMENTS.loadInt(context),
                Preferences.INTERVAL.loadInt(context),
                activeCalIds);

        // Append!
        msg.add(appointments.size());

        for (long app : appointments) {
            msg.add((int) app);
        }
    }

    private static void appendSyncInterval(Context context, List<Object> msg) {
        msg.add(Preferences.FREQUENCY.loadInt(context));
    }

    private static void appendBatteryState(Context context, List<Object> msg) {
        // See https://developer.android.com/training/monitoring-device-state/battery-monitoring
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (int) Math.round(level * 100 / (double) scale);

        // We send a positive integer if we're not charging, and a negative one if we are
        msg.add(isCharging ? -batteryPct : batteryPct);
    }

}
