package net.hypotenubel.calendariq.data.stats.source;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;

@Database(entities = BroadcastStatistics.class, version = 1)
public abstract class BroadcastStatisticsDatabase extends RoomDatabase {

    private static final String DB_NAME = "broadcast-statistics";

    // This could be improved by injecting a singleton database object into client objects
    private static BroadcastStatisticsDatabase singleton = null;
    public static BroadcastStatisticsDatabase getInstance(Context context) {
        // Common case (singleton != null) should be fast, and thus not be synchronized
        if (singleton == null) {
            synchronized (DB_NAME) {
                if (singleton == null) {
                    singleton = Room
                            .databaseBuilder(context, BroadcastStatisticsDatabase.class, DB_NAME)
                            .build();
                }
            }
        }
        return singleton;
    }

    public abstract IBroadcastStatisticsDao getDao();

}
