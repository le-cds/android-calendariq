package net.hypotenubel.calendariq.ui.main.calendar;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import net.hypotenubel.calendariq.data.calendar.model.CalendarDescriptor;
import net.hypotenubel.calendariq.data.calendar.source.CalendarRepository;

import java.util.List;

/**
 * View model for the list of calendars. The activity state of the calendars is automatically
 * synchronised with the preferences. This is basically just a bit of glue code between the
 * {@link CalendarRepository} and the {@link androidx.recyclerview.widget.RecyclerView}.
 */
public class CalendarViewModel extends ViewModel {

    // TODO Move the calendar account logic from the adapter to this view model. This is where it
    //      belongs

    private final CalendarRepository calendarRepository;

    /**
     * Creates a new instance in the given application context.
     */
    @ViewModelInject
    public CalendarViewModel(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    public LiveData<List<CalendarDescriptor>> getCalendars() {
        return calendarRepository.getAvailableCalendars();
    }

    public void refresh() {
        calendarRepository.refreshAvailableCalendars();
    }

    public void storeActiveCalendarIds() {
        calendarRepository.storeActiveCalendarIds();
    }

}
