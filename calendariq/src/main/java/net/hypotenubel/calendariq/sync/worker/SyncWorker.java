package net.hypotenubel.calendariq.sync.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.Assisted;
import androidx.hilt.work.WorkerInject;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.hypotenubel.calendariq.sync.synchroniser.Synchroniser;
import net.hypotenubel.calendariq.util.Utilities;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Thin wrapper around {@link Synchroniser} for use with {@link androidx.work.WorkManager}.
 */
public class SyncWorker extends Worker {

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(SyncWorker.class);

    private final Synchroniser synchroniser;

    @WorkerInject
    public SyncWorker(@Assisted @ApplicationContext Context context,
                      @Assisted WorkerParameters workerParams, Synchroniser synchroniser) {
        super(context, workerParams);

        this.synchroniser = synchroniser;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(LOG_TAG, "Starting SyncWorker...");

        synchroniser.run();

        return Result.success();
    }

}
