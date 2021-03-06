package net.hypotenubel.calendariq.injection;

import android.content.Context;

import net.hypotenubel.calendariq.data.apps.source.ITargetAppIdsSource;
import net.hypotenubel.calendariq.data.apps.source.XmlResourceTargetAppIdsSource;
import net.hypotenubel.calendariq.data.calendar.source.AndroidCalendarSource;
import net.hypotenubel.calendariq.data.calendar.source.ICalendarSource;
import net.hypotenubel.calendariq.data.stats.source.BroadcastStatisticsDatabase;
import net.hypotenubel.calendariq.data.stats.source.IBroadcastStatisticsDao;
import net.hypotenubel.calendariq.sync.synchroniser.ConnectBroadcastStrategy;
import net.hypotenubel.calendariq.sync.synchroniser.IBroadcastStrategy;
import net.hypotenubel.calendariq.util.DevicePrerequisitesChecker;
import net.hypotenubel.calendariq.util.IPrerequisitesChecker;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Defines dependency injection bindings for our standard release version.
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class DefaultApplicationModule {

    @Binds
    abstract IPrerequisitesChecker bindPrerequisiteChecker(DevicePrerequisitesChecker c);

    @Binds
    abstract ITargetAppIdsSource bindTargetAppSource(XmlResourceTargetAppIdsSource ids);

    @Binds
    abstract ICalendarSource bindCalendarSource(AndroidCalendarSource cs);

    @Binds
    abstract IBroadcastStrategy bindBroadcastStrategy(ConnectBroadcastStrategy bs);

    @Provides
    @Singleton
    static BroadcastStatisticsDatabase provideBroadcastStatisticsDatabase(
            @ApplicationContext Context context) {

        return BroadcastStatisticsDatabase.create(context);
    }

    @Provides
    static IBroadcastStatisticsDao provideBroadcastStatisticsDao(BroadcastStatisticsDatabase db) {
        return db.getDao();
    }

}
