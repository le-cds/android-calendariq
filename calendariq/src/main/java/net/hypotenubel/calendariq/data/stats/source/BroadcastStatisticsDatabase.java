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

    // TODO The singleton code below needs to be removed as soon as dependency injection works

    // This could be improved by injecting a singleton database object into client objects
    private static BroadcastStatisticsDatabase singleton = null;
    public static BroadcastStatisticsDatabase getInstance(Context context) {
        // Common case (singleton != null) should be fast, and thus not be synchronized
        if (singleton == null) {
            synchronized (DB_NAME) {
                if (singleton == null) {
                    singleton = create(context);
                }
            }
        }
        return singleton;
    }

    public abstract IBroadcastStatisticsDao getDao();

}
