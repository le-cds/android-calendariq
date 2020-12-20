package net.hypotenubel.calendariq.ui.main.log;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import net.hypotenubel.calendariq.data.access.stats.BroadcastStatisticsDatabase;
import net.hypotenubel.calendariq.data.model.stats.BroadcastStatistics;

import java.util.List;

/**
 * View model for the list of calendars. The activity state of the calendars is automatically
 * synchronised with the preferences.
 */
public class LogViewModel extends AndroidViewModel {

    private LiveData<List<BroadcastStatistics>> logItems;

    public LogViewModel(Application application) {
        super(application);

        logItems = BroadcastStatisticsDatabase
                .getInstance(application)
                .getDao()
                .getAllLive();
    }

    public LiveData<List<BroadcastStatistics>> getLogItems() {
        return logItems;
    }
}
