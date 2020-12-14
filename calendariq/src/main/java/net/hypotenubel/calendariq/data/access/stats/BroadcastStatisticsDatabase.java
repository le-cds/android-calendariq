package net.hypotenubel.calendariq.data.access.stats;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import net.hypotenubel.calendariq.data.model.stats.BroadcastStatistics;

@Database(entities = BroadcastStatistics.class, version = 1)
public abstract class BroadcastStatisticsDatabase extends RoomDatabase {

    public abstract IBroadcastStatisticsDao getDao();

}
