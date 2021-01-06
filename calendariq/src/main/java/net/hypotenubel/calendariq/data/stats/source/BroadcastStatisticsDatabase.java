package net.hypotenubel.calendariq.data.stats.source;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;

@Database(entities = BroadcastStatistics.class, version = 1)
public abstract class BroadcastStatisticsDatabase extends RoomDatabase {

    public static final String DB_NAME = "broadcast-statistics";

    /**
     * Returns a new instance for the given context. This is not a singleton.
     */
    public static BroadcastStatisticsDatabase create(Context context) {
        return Room
                .databaseBuilder(context, BroadcastStatisticsDatabase.class, DB_NAME)
                .build();
    }

    public abstract IBroadcastStatisticsDao getDao();

}
