package net.hypotenubel.calendariq.data.calendar.source;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import net.hypotenubel.calendariq.data.calendar.model.AccountDescriptor;
import net.hypotenubel.calendariq.data.calendar.model.CalendarDescriptor;
import net.hypotenubel.calendariq.util.IPrerequisitesChecker;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * An implementation of {@link ICalendarSource} that loads calendars from the Android APIs.
 */
public class AndroidCalendarSource implements ICalendarSource {

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(AndroidCalendarSource.class);

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

    private final IPrerequisitesChecker prerequisitesChecker;

    /** The context from which this provider was created. */
    private final Context context;

    /**
     * Creates a new instance in the given context.
     *
     * @param context the context from which the instance is created.
     * @param prerequisitesChecker thing to check whether we have calendar access permissions.
     */
    @Inject
    public AndroidCalendarSource(@ApplicationContext Context context,
                                 IPrerequisitesChecker prerequisitesChecker) {
        this.context = context;
        this.prerequisitesChecker = prerequisitesChecker;
    }

    @Override
    public List<CalendarDescriptor> getAvailableCalendars() {
        // Only try loading calendars if we have permission to do so
        if (!prerequisitesChecker.isCalendarAccessible(context)) {
            Log.d(LOG_TAG, "Missing calendar permission");
            return Collections.emptyList();
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
        List<CalendarDescriptor> calendars = toCalendars(cursor);
        cursor.close();

        return calendars;
    }

    /**
     * Turns the cursor's current row of data into a calendar instance.
     *
     * @param cursor the cursor that holds the data.
     * @return a list of calendars.
     */
    private List<CalendarDescriptor> toCalendars(Cursor cursor) {
        List<CalendarDescriptor> calendarDescriptors = new ArrayList<>();

        // Remember the account descriptors we've created
        Map<String, AccountDescriptor> accountDescriptors = new HashMap<>();

        while (cursor.moveToNext()) {
            // Look for an account object
            String accountName = cursor.getString(CALENDAR_PROJECTION_ACCOUNT_NAME);

            AccountDescriptor accountDescriptor = accountDescriptors.get(accountName);
            if (accountDescriptor == null) {
                accountDescriptor = new AccountDescriptor(accountName);
                accountDescriptors.put(accountName, accountDescriptor);
            }

            // Add new calendar descriptor
            calendarDescriptors.add(new CalendarDescriptor(
                    cursor.getInt(CALENDAR_PROJECTION_ID),
                    cursor.getString(CALENDAR_PROJECTION_DISPLAY_NAME),
                    accountDescriptor,
                    cursor.getInt(CALENDAR_PROJECTION_COLOR)));
        }

        return calendarDescriptors;
    }

    @Override
    public List<Long> loadUpcomingAppointments(int maxCount, int maxDays,
                                               Collection<Integer> from) {

        List<Long> result = new ArrayList<>(maxCount);
        if (from.isEmpty()) {
            return result;
        }

        // We'll be loading events from the upcoming seven days, max
        Calendar nowCal = Calendar.getInstance();
        long startMillis = nowCal.getTimeInMillis();
        long endMillis = startMillis + maxDays * MILLISECONDS_PER_DAY;

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

        cursor.close();

        Log.d(LOG_TAG, "Loaded " + result.size() + " appointments");

        return result;
    }

    /**
     * Builds a selection expression with the given number of placeholders for calendar IDs.
     */
    private String buildSelectionExpression(int calendarIdCount) {
        StringBuilder selectionBuilder = new StringBuilder(
                CalendarContract.Instances.ALL_DAY + " = 0 and (");

        for (int i = 0; i < calendarIdCount; i++) {
            if (i != 0) {
                selectionBuilder.append(" or ");
            }
            selectionBuilder.append(CalendarContract.Instances.CALENDAR_ID + " = ?");
        }

        selectionBuilder.append(")");

        return "(" + selectionBuilder.toString() + ")";
    }

}
