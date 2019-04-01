package net.hypotenubel.calendariq.activities;

import android.app.Application;

import net.hypotenubel.calendariq.calendar.CalendarDescriptor;
import net.hypotenubel.calendariq.calendar.ICalendarInterface;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * View model for the list of calendars.
 */
public class CalendarViewModel extends AndroidViewModel {

    /** List of calendars. */
    private MutableLiveData<List<CalendarDescriptor>> calendars;

    /**
     * Creates a new instance in the given application context.
     */
    public CalendarViewModel(Application application) {
        super(application);
    }

    /**
     * Returns a {@link LiveData} object that contains a list of active calendars.
     *
     * @return list of calendars.
     */
    public LiveData<List<CalendarDescriptor>> getCalendars() {
        if (calendars == null) {
            refresh();
        }

        return calendars;
    }

    /**
     * Refreshes our list of calendars.
     */
    public void refresh() {
        // Ensure that the calendars are not null
        if (calendars == null) {
            calendars = new MutableLiveData<>();
        }

        // Fire off a thread that loads a new list of calendars
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Load available calendars
                ICalendarInterface provider = Utilities.obtainCalendarProvider(getApplication());
                List<CalendarDescriptor> calList = provider.getAvailableCalendars();

                // Sort the list so that the UI won't have to
                Collections.sort(calList);

                // Set the calendar activity flags
                applyActivityFlags(calList);

                // Update live data
                calendars.postValue(calList);
            }
        }).start();
    }

    /**
     * Sets activity flags for the given calendars. If we haven't loaded any calendars yet, this
     * method loads activity flags from the preferences. Otherwise, it extracts IDs of active
     * calendars from our current list and applies them to the new list.
     */
    private void applyActivityFlags(List<CalendarDescriptor> calList) {
        Set<Integer> activeCalIds = null;

        // Obtain the active calendar IDs from preferences or current list of calendars
        if (calendars.getValue() == null) {
            activeCalIds = Utilities.loadActiveCalendarIds(
                    Utilities.obtainSharedPreferences(getApplication()));
        } else {
            activeCalIds = new HashSet<>();
            for (CalendarDescriptor cal : calendars.getValue()) {
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
        if (calendars == null || calendars.getValue() == null) {
            return;
        }

        // Build a set of strings with the IDs of active calendars
        Set<String> activeCalendarIdStrings = new HashSet<>();
        for (CalendarDescriptor cal : calendars.getValue()) {
            if (cal.isActive()) {
                activeCalendarIdStrings.add(Integer.toString(cal.getId()));
            }
        }

        // Save the whole thing
        Utilities.obtainSharedPreferences(getApplication())
                .edit()
                .putStringSet(Utilities.PREF_ACTIVE_CALENDARS, activeCalendarIdStrings)
                .apply();
    }

}
