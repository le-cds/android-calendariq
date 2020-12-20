package net.hypotenubel.calendariq.data.access.stats;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import net.hypotenubel.calendariq.data.model.stats.BroadcastStatistics;

import java.util.List;

@Dao
public interface IBroadcastStatisticsDao {

    /** The number of items to keep. */
    int MAX_ITEM_COUNT = 100;

    /**
     * Adds the given statistic to the database.
     */
    @Insert
    void add(BroadcastStatistics stat);

    /**
     * Adds the given statistic object, but keeps the database from growing beyond
     * {@link #MAX_ITEM_COUNT} items.
     */
    default void addWithoutGrowing(BroadcastStatistics stat) {
        add(stat);
        keepMostRecent();
    }

    /**
     * Returns all {@link BroadcastStatistics}, ordered from newest to oldest.
     */
    @Query("SELECT * FROM BroadcastStatistics ORDER BY utcTimestampMillis DESC")
    List<BroadcastStatistics> getAll();

    /**
     * Same as {@link #getAll()}, but wraps the result in a {@link LiveData} object.
     */
    @Query("SELECT * FROM BroadcastStatistics ORDER BY utcTimestampMillis DESC")
    LiveData<List<BroadcastStatistics>> getAllLive();

    /**
     * Returns the {@code n} newest log items, or all of them if fewer exist.
     */
    @Query("SELECT * FROM BroadcastStatistics ORDER BY utcTimestampMillis DESC LIMIT :n")
    List<BroadcastStatistics> getNewest(int n);

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
    int delete(List<BroadcastStatistics> stats);

    /**
     * Removes all be the most recent {@link #MAX_ITEM_COUNT} items.
     */
    default void keepMostRecent() {
        keepMostRecent(MAX_ITEM_COUNT);
    }

    /**
     * Removes all but the most recent {@code n} items.
     */
    default void keepMostRecent(int n) {
        int itemCount = size();
        if (itemCount > n) {
            delete(getOldest(itemCount - n));
        }
    }

}
