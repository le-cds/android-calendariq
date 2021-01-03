package net.hypotenubel.calendariq.data.stats;

import androidx.lifecycle.LiveData;

import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;
import net.hypotenubel.calendariq.data.stats.source.IBroadcastStatisticsDao;

import java.util.List;

import javax.inject.Inject;

/**
 * Provides proper access to the broadcast statistics. The repository also ensures that the number
 * of broadcast statistics that we keep lying around is fixed.
 */
public class BroadcastStatisticsRepository {

    /** The number of items to keep. */
    private static final int MAX_ITEM_COUNT = 100;

    private IBroadcastStatisticsDao dao;

    @Inject
    public BroadcastStatisticsRepository(IBroadcastStatisticsDao dao) {
        this.dao = dao;
    }

    public LiveData<List<BroadcastStatistics>> getBroadcastStats() {
        return dao.getAllLive();
    }

    public LiveData<List<BroadcastStatistics>> getNewestBroadcastStats() {
        return dao.getNewestLive(1);
    }

    public void addBroadcastStats(BroadcastStatistics stats) {
        // We need to run this in a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                dao.add(stats);

                int size = dao.size();
                if (size > MAX_ITEM_COUNT) {
                    dao.delete(dao.getOldest(size - MAX_ITEM_COUNT));
                }
            }
        }).start();
    }

}
