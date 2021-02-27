package net.hypotenubel.calendariq.ui.main.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import net.hypotenubel.calendariq.data.calendar.CalendarRepository;
import net.hypotenubel.calendariq.data.calendar.model.AccountDescriptor;
import net.hypotenubel.calendariq.data.calendar.model.CalendarDescriptor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * View model for the calendar list. This view model provides not just the list of calendars, but
 * also inserts The activity state of the calendars is automatically
 * synchronised with the preferences. This is basically just a bit of glue code between the
 * {@link CalendarRepository} and the {@link androidx.recyclerview.widget.RecyclerView}.
 */
@HiltViewModel
public class CalendarViewModel extends ViewModel {

    private final CalendarRepository calendarRepository;

    /** The data to be observed by the UI. */
    private final LiveData<List<Object>> accountsAndCalendars;

    /**
     * Creates a new instance in the given application context.
     */
    @Inject
    public CalendarViewModel(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;

        // We basically pipe the repository's calendars through to the UI, but insert a
        // transformation in between
        accountsAndCalendars = Transformations.map(
                calendarRepository.getAvailableCalendars(),
                CalendarViewModel::insertAccountsIntoCalendarList);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Accessors and Actions

    public LiveData<List<Object>> getAccountsAndCalendars() {
        return accountsAndCalendars;
    }

    public void refresh() {
        calendarRepository.refreshAvailableCalendars();
    }

    public void storeActiveCalendarIds() {
        calendarRepository.storeActiveCalendarIds();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Utils

    private static List<Object> insertAccountsIntoCalendarList(List<CalendarDescriptor> calendars) {
        List<Object> result = new ArrayList<>();

        AccountDescriptor currAccount = null;
        for (CalendarDescriptor currCalendar : calendars) {
            if (currAccount == null || !currAccount.equals(currCalendar.getAccount())) {
                // We've encountered the first calendar of a new account
                currAccount = currCalendar.getAccount();
                result.add(currAccount);
            }

            result.add(currCalendar);
        }

        return result;
    }

}
