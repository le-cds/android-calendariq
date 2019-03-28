package net.hypotenubel.calendariq.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * A calendar provider that loads calendars from the Android APIs.
 */
public class AndroidCalendarInterface implements ICalendarInterface {

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(AndroidCalendarInterface.class);

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
    public AndroidCalendarInterface(Context context) {
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
                cursor.getInt(CALENDAR_PROJECTION_COLOR)
        );
    }

    @Override
    public List<Long> loadUpcomingAppointments(int maxCount, Collection<Integer> from) {
        List<Long> result = new ArrayList<>(maxCount);
        if (from.isEmpty()) {
            return result;
        }

        // We'll be loading events from the upcoming seven days, max
        Calendar nowCal = Calendar.getInstance();
        long startMillis = nowCal.getTimeInMillis();
        long endMillis = startMillis + 7 * MILLISECONDS_PER_DAY;

        // The query specifies the start and end times of event instances we're interested in
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // We select events from the given calendar that are not all-day events
        String selection = buildSelectionExpression(from.size());

        String[] selectionArgs = new String[from.size()];
        int currIdx = 0;
        for (Integer calId : from) {
            selectionArgs[currIdx++] = calId.toString();
        }

        // Actually perform the query
        Cursor cursor =  context.getContentResolver().query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                CalendarContract.Instances.BEGIN + " ASC");

        for (int i = 0; i < maxCount && cursor.moveToNext(); i++) {
            // Convert from UTC milliseconds to UTC seconds
            result.add(cursor.getLong(INSTANCE_PROJECTION_BEGIN) / 1000);
        }

        Log.d(LOG_TAG, "Loaded " + result.size() + " appointments");

        return result;
    }

    /**
     * Builds a selection expression with the given number of placeholders for calendar IDs.
     */
    private String buildSelectionExpression(int calendarIdCount) {
        String selection = CalendarContract.Instances.ALL_DAY + " = 0 and (";

        for (int i = 0; i < calendarIdCount; i++) {
            if (i != 0) {
                selection += " or ";
            }
            selection += CalendarContract.Instances.CALENDAR_ID + " = ?";
        }
        selection += ")";

        return "(" + selection + ")";
    }

}
