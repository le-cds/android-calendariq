package net.hypotenubel.calendariq.data.stats.source;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;

import java.util.List;

@Dao
public interface IBroadcastStatisticsDao {

    /**
     * Adds the given statistic to the database.
     */
    @Insert
    void add(BroadcastStatistics stat);

    /**
     * Same as {@link #getAll()}, but wraps the result in a {@link LiveData} object.
     */
    @Query("SELECT * FROM BroadcastStatistics ORDER BY utcTimestampMillis DESC")
    LiveData<List<BroadcastStatistics>> getAllLive();

    /**
     * Same as {@link #getNewest(int)}, but wraps the result in a {@link LiveData} object.
     */
    @Query("SELECT * FROM BroadcastStatistics ORDER BY utcTimestampMillis DESC LIMIT :n")
    LiveData<List<BroadcastStatistics>> getNewestLive(int n);

    /**
     * Returns the {@code n} oldest log items, or all of them if fewer exist.
     */
    @Query("SELECT * FROM BroadcastStatistics ORDER BY utcTimestampMillis ASC LIMIT :n")
    List<BroadcastStatistics> getOldest(int n);

    /**
     * Returns the number of database items.
     */
    @Query("SELECT COUNT(*) FROM BroadcastStatistics")
    int size();

    /**
     * Deletes the given statistics from the database and returns the number of items that were
     * in fact deleted.
     */
    @Delete
    void delete(List<BroadcastStatistics> stats);

}
