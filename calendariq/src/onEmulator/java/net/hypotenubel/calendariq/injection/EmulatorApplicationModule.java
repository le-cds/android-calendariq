package net.hypotenubel.calendariq.injection;

import android.content.Context;

import net.hypotenubel.calendariq.data.apps.source.ITargetAppIdsSource;
import net.hypotenubel.calendariq.data.apps.source.XmlResourceTargetAppIdsSource;
import net.hypotenubel.calendariq.data.calendar.source.ICalendarSource;
import net.hypotenubel.calendariq.data.calendar.source.SampleCalendarSource;
import net.hypotenubel.calendariq.data.stats.source.BroadcastStatisticsDatabase;
import net.hypotenubel.calendariq.data.stats.source.IBroadcastStatisticsDao;
import net.hypotenubel.calendariq.sync.synchroniser.IBroadcastStrategy;
import net.hypotenubel.calendariq.sync.synchroniser.RandomBroadcastStrategy;
import net.hypotenubel.calendariq.util.EmulatorPrerequisitesChecker;
import net.hypotenubel.calendariq.util.IPrerequisitesChecker;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Defines dependency injection bindings for our emulator version.
 */
@Module
@InstallIn(ApplicationComponent.class)
public abstract class EmulatorApplicationModule {

    @Binds
    abstract IPrerequisitesChecker bindPrerequisiteChecker(EmulatorPrerequisitesChecker c);

    @Binds
    abstract ICalendarSource bindCalendarSource(SampleCalendarSource cs);

    @Binds
    abstract ITargetAppIdsSource bindTargetAppSource(XmlResourceTargetAppIdsSource ids);

    @Binds
    abstract IBroadcastStrategy bindBroadcastStrategy(RandomBroadcastStrategy bs);

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
