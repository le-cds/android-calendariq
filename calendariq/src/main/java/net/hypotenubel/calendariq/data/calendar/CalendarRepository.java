package net.hypotenubel.calendariq.data.calendar;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.hypotenubel.calendariq.data.Preferences;
import net.hypotenubel.calendariq.data.calendar.model.CalendarDescriptor;
import net.hypotenubel.calendariq.data.calendar.source.ICalendarSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Repository for accessing calendars and managing which ones are active and which ones are not.
 */
public class CalendarRepository {

    private final Context context;

    private final ICalendarSource calendarSource;

    /** Live data for available calendars. Updated upon refresh. */
    private final MutableLiveData<List<CalendarDescriptor>> availableCalendars
            = new MutableLiveData<>();

    /**
     * Creates a new instance that retrieves calendars from the given calendar source.
     */
    @Inject
    public CalendarRepository(@ApplicationContext Context context, ICalendarSource calendarSource) {
        this.context = context;
        this.calendarSource = calendarSource;
        refreshAvailableCalendars();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Accessors

    /**
     * Returns the calendars available in the system.
     */
    public LiveData<List<CalendarDescriptor>> getAvailableCalendars() {
        return availableCalendars;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Calendar Loading

    /**
     * Triggers a refresh of all available calendars and updates the associated live data.
     */
    public void refreshAvailableCalendars() {
        // Fire off a thread that loads a new list of calendars
        new Thread(() -> {
            // Load available calendars
            List<CalendarDescriptor> calList = calendarSource.getAvailableCalendars();

            // Sort the list so that the UI won't have to
            Collections.sort(calList);

            // Set the calendar activity flags
            loadActiveCalendarIds(calList);

            // Update live data
            availableCalendars.postValue(calList);
        }).start();
    }

    /**
     * Loads the set of active calendars and applies them to the given set of calendar descriptor
     * instances accordingly. If we haven't loaded any calendars yet, this method loads activity
     * flags from the preferences. Otherwise, it extracts IDs of active calendars from our current
     * list and applies them to the new list. It it the latter which supports refreshing the
     * calendar list.
     */
    private void loadActiveCalendarIds(List<CalendarDescriptor> calList) {
        Set<Integer> activeCalIds;

        // Obtain the active calendar IDs from preferences or current list of calendars
        if (availableCalendars.getValue() == null) {
            activeCalIds = Preferences.ACTIVE_CALENDARS.loadIntSet(context);
        } else {
            activeCalIds = new HashSet<>();
            for (CalendarDescriptor cal : availableCalendars.getValue()) {
                if (cal.isActive()) {
                    activeCalIds.add(cal.getId());
                }
            }
        }

        // Go through the calendars and set their activity flags
        for (CalendarDescriptor cal : calList) {
            cal.setActive(activeCalIds.contains(cal.getId()));
        }
    }

    /**
     * Stores the IDs of active calendars to our preferences.
     */
    public void storeActiveCalendarIds() {
        if (availableCalendars.getValue() != null) {
            // Build a set of IDs of our active calendars
            Set<Integer> activeCalendarIds = new HashSet<>();
            for (CalendarDescriptor cal : availableCalendars.getValue()) {
                if (cal.isActive()) {
                    activeCalendarIds.add(cal.getId());
                }
            }

            Preferences.ACTIVE_CALENDARS.storeIntSet(context, activeCalendarIds);
        }
    }

}
