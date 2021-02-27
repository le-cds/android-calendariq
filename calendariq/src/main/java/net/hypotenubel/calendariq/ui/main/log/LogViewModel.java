package net.hypotenubel.calendariq.ui.main.log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import net.hypotenubel.calendariq.data.stats.BroadcastStatisticsRepository;
import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * View model for the list of calendars. The activity state of the calendars is automatically
 * synchronised with the preferences.
 */
@HiltViewModel
public class LogViewModel extends ViewModel {

    private final LiveData<List<BroadcastStatistics>> logItems;

    @Inject
    public LogViewModel(BroadcastStatisticsRepository broadcastStatsRepo) {
        logItems = broadcastStatsRepo.getBroadcastStats();
    }

    public LiveData<List<BroadcastStatistics>> getLogItems() {
        return logItems;
    }
}
