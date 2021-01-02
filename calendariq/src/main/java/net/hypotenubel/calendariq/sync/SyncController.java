package net.hypotenubel.calendariq.sync;

import android.content.Context;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import net.hypotenubel.calendariq.data.Preferences;
import net.hypotenubel.calendariq.util.IPrerequisitesChecker;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * This class is responsible for starting and stopping our synchronisation services.
 */
public class SyncController {

    private static final String LOG_TAG = Utilities.logTag(SyncController.class);

    /** ID of the work item we're using to run our worker periodically. */
    private static final String SYNC_WORK_NAME = "sync_devices";
    /** ID of the work item we're using to run our worker once. */
    private static final String SYNC_ONCE_WORK_NAME = "sync_devices_once";

    private final IPrerequisitesChecker prerequisitesChecker;
    private final Context context;

    @Inject
    public SyncController(@ApplicationContext Context context,
                          IPrerequisitesChecker prerequisitesChecker) {
        this.context = context;
        this.prerequisitesChecker = prerequisitesChecker;
    }

    /**
     * Runs a single synchronisation attempt.
     */
    public void syncOnce() {
        if (prerequisitesChecker.arePrerequisitesMet(context)) {
            Log.d(LOG_TAG,"Synchronising once");

            // Build a new periodic work request and register it if none was already registered
            // TODO Do this in our application, not with the WorkManager
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WatchSyncWorker.class).build();
            WorkManager
                    .getInstance(context)
                    .enqueueUniqueWork(
                            SYNC_ONCE_WORK_NAME,
                            ExistingWorkPolicy.REPLACE,
                            request);
        } else {
            Log.d(LOG_TAG,"Not synchronising once since prerequisites are not met");
        }
    }

    /**
     * Starts and stops our synchronisation services as configured in the preferences.
     */
    public void reconfigureSyncServices() {
        boolean prerequisitesMet = prerequisitesChecker.arePrerequisitesMet(context);
        int interval = Preferences.INTERVAL.loadInt(context);

        if (prerequisitesMet) {
            reconfigureWorkManager(interval);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // WorkManager

    // TODO This should have a parameter indicating whether the service should be running or not
    private void reconfigureWorkManager(int interval) {
        // TODO Handle this differently
        boolean replaceExisting = true;

        // Let it be known in the kingdom that we shall unleash the workers!
        if (replaceExisting) {
            Log.d(LOG_TAG,"Replacing sync worker with interval of " + interval + " minutes");
        } else {
            Log.d(LOG_TAG,"Running sync worker with interval of " + interval + " minutes");
        }

        // Build a new periodic work request and register it if none was already registered
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WatchSyncWorker.class,
                interval,
                TimeUnit.MINUTES)
                .build();

        ExistingPeriodicWorkPolicy policy = replaceExisting
                ? ExistingPeriodicWorkPolicy.REPLACE
                : ExistingPeriodicWorkPolicy.KEEP;
        WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                        SYNC_WORK_NAME,
                        policy,
                        request);
    }

}
