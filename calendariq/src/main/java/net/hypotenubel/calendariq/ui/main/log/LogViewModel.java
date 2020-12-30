package net.hypotenubel.calendariq.ui.main.log;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;
import net.hypotenubel.calendariq.data.stats.source.IBroadcastStatisticsDao;

import java.util.List;

/**
 * View model for the list of calendars. The activity state of the calendars is automatically
 * synchronised with the preferences.
 */
public class LogViewModel extends ViewModel {

    private final LiveData<List<BroadcastStatistics>> logItems;

    @ViewModelInject
    public LogViewModel(IBroadcastStatisticsDao broadcastStatsDao) {
        logItems = broadcastStatsDao.getAllLive();
    }

    public LiveData<List<BroadcastStatistics>> getLogItems() {
        return logItems;
    }
}
