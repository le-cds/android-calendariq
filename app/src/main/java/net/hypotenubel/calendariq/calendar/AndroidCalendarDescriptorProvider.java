package net.hypotenubel.calendariq.calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import net.hypotenubel.calendariq.util.Utilities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A calendar provider that loads calendars from the Android APIs.
 */
public class AndroidCalendarDescriptorProvider implements ICalendarDescriptorProvider {

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(AndroidCalendarDescriptorProvider.class);

    /** The number of milliseconds that pass by each day, mostly unnoticed by us mere humans. */
    private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

    /** The fields we query when we obtain calendar data. */
    private static final String[] CALENDAR_PROJECTION = {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR
    };
    // The following constants are the indices in the calendar projection
    private static final int CALENDAR_PROJECTION_ID = 0;
    private static final int CALENDAR_PROJECTION_DISPLAY_NAME = 1;
    private static final int CALENDAR_PROJECTION_ACCOUNT_NAME = 2;
    private static final int CALENDAR_PROJECTION_COLOR = 3;

    /** The fields we query when we obtain event instance data. */
    private static final String[] INSTANCE_PROJECTION = {
            CalendarContract.Instances.BEGIN
    };
    // The following constants are the indices in the instances projection
    private static final int INSTANCE_PROJECTION_BEGIN = 0;

    /** The context from which this provider was created. */
    private Context context;

    /**
     * Creates a new instance in the given context.
     *
     * @param context the context from which the instance is created.
     */
    public AndroidCalendarDescriptorProvider(Context context) {
        this.context = context;
    }

    @Override
    public List<CalendarDescriptor> getAvailableCalendars() {
        List<CalendarDescriptor> calendars = new ArrayList<>();

        // Only try loading calendars if we have permission to do so
        if (!Utilities.ensureCalendarPermission(context)) {
            Log.d(LOG_TAG, "Missing calendar permission");
            return calendars;
        }

        // Load all calendars
        Cursor cursor = context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                CALENDAR_PROJECTION,
                null,
                null,
                null);

        // Call loadCalendar(int) to load details about each specific calendar
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            calendars.add(toCalendar(cursor));
        }
        cursor.close();

        return calendars;
    }

    @Override
    public CalendarDescriptor loadCalendar(int id) {
        // If we don't have access to calendars, we won't be able to load any (citation needed)
        if (!Utilities.ensureCalendarPermission(context)) {
            return null;
        }

        // We select only the requested calendar
        String selection = "(" + CalendarContract.Calendars._ID + " = ?)";
        String[] selectionArgs = new String[] {String.valueOf(id)};

        // Perform the query
        Cursor cursor = context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                CALENDAR_PROJECTION,
                selection,
                selectionArgs,
                null);

        CalendarDescriptor cal = null;
        if (cursor.moveToNext()) {
            // Load the calendar and its next appointment
            cal = toCalendar(cursor);
        } else {
            Log.d(LOG_TAG, "Unable to load calenar with ID " + id);
        }

        cursor.close();

        return cal;
    }

    /**
     * Turns the cursor's current row of data into a calendar instance.
     *
     * @param cursor the cursor that holds the data.
     * @return a calendar.
     */
    private CalendarDescriptor toCalendar(Cursor cursor) {
        int calId = cursor.getInt(CALENDAR_PROJECTION_ID);
        return new CalendarDescriptor(
                calId,
                cursor.getString(CALENDAR_PROJECTION_DISPLAY_NAME),
                cursor.getString(CALENDAR_PROJECTION_ACCOUNT_NAME),
                cursor.getInt(CALENDAR_PROJECTION_COLOR),
                loadUpcomingAppointment(calId)
        );
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Appointment Management

    /**
     * Loads the calendar's next upcoming appointment, which is saved in said calendar.
     *
     * @param calId ID of the calendar whose upcoming appointment to retrieve.
     */
    private long loadUpcomingAppointment(int calId) {
        // The event must be between now and 24 hours from now
        Calendar nowCal = Calendar.getInstance();
        long nowMillis = nowCal.getTimeInMillis();
        long tomorrowMillis = nowMillis + MILLISECONDS_PER_DAY;

        // The query specifies the start and end times of event instances we're interested in
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, nowMillis);
        ContentUris.appendId(builder, tomorrowMillis);

        // We select events from the given calendar that are not all-day events
        String selection = "(" + CalendarContract.Instances.CALENDAR_ID + " = ?"
                + " and " + CalendarContract.Instances.ALL_DAY + " = 0"
                + " and " + CalendarContract.Instances.BEGIN + " > ?)";
        String[] selectionArgs = new String[] {String.valueOf(calId), String.valueOf(nowMillis)};

        // Actually perform the query
        Cursor cursor =  context.getContentResolver().query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                CalendarContract.Instances.BEGIN + " ASC");

        // If anything was returned, the first thing is what we're interested in
        long upcomingAppointment = CalendarDescriptor.NO_UPCOMING_APPOINTMENT;
        if (cursor.moveToNext()) {
            // Convert from UTC milliseconds to UTC seconds
            upcomingAppointment = cursor.getLong(INSTANCE_PROJECTION_BEGIN) / 1000;
            Log.d(LOG_TAG, "Upcoming appointment for calendar " + calId + " at "
                    + upcomingAppointment + " UTC");

        } else {
            Log.d(LOG_TAG, "No upcoming appointment for calendar " + calId);
        }

        return upcomingAppointment;
    }

}
