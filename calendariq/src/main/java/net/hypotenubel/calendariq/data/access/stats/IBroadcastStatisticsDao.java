package net.hypotenubel.calendariq.data.access.stats;

import androidx.room.Dao;
import androidx.room.Query;

import net.hypotenubel.calendariq.data.model.stats.BroadcastStatistics;

import java.util.List;

@Dao
public interface IBroadcastStatisticsDao {

    /**
     * Returns all {@link BroadcastStatistics}, ordered from newest to oldest.
     */
    @Query("SELECT * FROM BroadcastStatistics ORDER BY utcTimestampMillis DESC")
    List<BroadcastStatistics> getBroadcastStatistics();

}
