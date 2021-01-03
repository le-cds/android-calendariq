package net.hypotenubel.calendariq.sync.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import net.hypotenubel.calendariq.util.Utilities;

import java.util.concurrent.TimeUnit;

/**
 * Controls our {@link WorkManager}-based synchronisation service.
 */
public class SyncWorkerController {

    private static final String LOG_TAG = Utilities.logTag(SyncWorkerController.class);

    /** ID of the work item we're using to run our worker periodically. */
    private static final String SYNC_WORK_NAME = "calendariq_sync_worker";

    /**
     * Ensures the service is running with the given synchronisation interval.
     *
     * @param forceRestart if {@code true}, the service is restarted even if it is currently
     *                    running. Otherwise, the service is only started if it is not running.
     */
    public static void start(Context appContext, int interval, boolean forceRestart) {
        // Let it be known in the kingdom that we shall unleash the workers!
        if (forceRestart) {
            Log.d(LOG_TAG,"(Re-)Starting sync worker with interval of " + interval + " minutes");
        } else {
            Log.d(LOG_TAG,"Running sync worker with interval of " + interval + " minutes");
        }

        // Build a new periodic work request and register it if none was already registered
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                interval,
                TimeUnit.MINUTES)
                .build();

        ExistingPeriodicWorkPolicy policy = forceRestart
                ? ExistingPeriodicWorkPolicy.REPLACE
                : ExistingPeriodicWorkPolicy.KEEP;
        WorkManager
                .getInstance(appContext)
                .enqueueUniquePeriodicWork(
                        SYNC_WORK_NAME,
                        policy,
                        request);
    }

    /**
     * Stops the service.
     */
    public static void stop(Context appContext) {
        WorkManager
                .getInstance(appContext)
                .cancelUniqueWork(SYNC_WORK_NAME);
    }

}
