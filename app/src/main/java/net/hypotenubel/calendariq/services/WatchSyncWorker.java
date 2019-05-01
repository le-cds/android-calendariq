package net.hypotenubel.calendariq.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import net.hypotenubel.calendariq.connectiq.ConnectIQAppBroadcaster;
import net.hypotenubel.calendariq.connectiq.IBroadcasterEventListener;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * A worker that can be invoked regularly to send appointments to devices. Provides static methods
 * to run it immediately or to schedule it to be run.
 */
public class WatchSyncWorker extends Worker {

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Fields

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(WatchSyncWorker.class);

    /** ID of the work item we're using to run our worker periodically. */
    private static final String SYNC_WORK_NAME = "sync_devices";
    /** ID of the work item we're using to run our worker once. */
    private static final String SYNC_ONCE_WORK_NAME = "sync_devices_once";
    /** Key of the preference that contains the sync interval. */
    private static final String SYNC_INTERVAL_PREFERENCE_KEY = "frequency";
    /** Default synchronization interval in minutes. */
    private static final int DEFAULT_SYNC_INTERVAL = 15;

    // Message field values that indicate that a message to the handler thread was sent by us
    private static final int SERVICE_MESSAGE_WHAT = 0xdabcd41f;
    private static final int SERVICE_MESSAGE_ARG_1 = 0x2676922f;
    private static final int SERVICE_MESSAGE_ARG_2 = 0x67487fd4;

    /** Synchronization lock to wait for the broadcaster to finish. */
    private Object lock = new Object();
    /** Whether the broadcaster has already told us that it's finished. */
    private boolean finished = false;


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Management

    /**
     * Ensures that our synchronization worker is run by the work manager API.
     *
     * @param interval the interval it should be periodically run in.
     * @param replaceExisting {@code true} if an existing worker should be replaced. If this is
     *                                    {@code false}, nothing happens if a worker already exists.
     */
    public static void runSyncWorker(int interval, boolean replaceExisting) {
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
                .getInstance()
                .enqueueUniquePeriodicWork(
                        SYNC_WORK_NAME,
                        policy,
                        request);
    }

    /**
     * Ensures that our synchronization worker is run once by the work manager API. Returns the
     * operation to be run so that callers can wait for that application to complete to show some
     * kind of a notification.
     */
    public static Operation runSyncWorkerOnce() {
        Log.d(LOG_TAG,"Running sync worker once");

        // Build a new periodic work request and register it if none was already registered
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WatchSyncWorker.class).build();
        return WorkManager
                .getInstance()
                .enqueueUniqueWork(
                        SYNC_ONCE_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        request);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Construction

    public WatchSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Worker Implementation

    @NonNull
    @Override
    public Result doWork() {
        Log.d(LOG_TAG, "Sending appointments to Garmin devices...");

        // TODO Run off if Bluetooth is currently disabled

        // ConnectIQ requires a thread with a looper :/
        HandlerThread handlerThread = new HandlerThread("WatchSyncWorkerThread");
        handlerThread.start();
        ConnectIQThreadHandler handler = new ConnectIQThreadHandler(handlerThread.getLooper());

        // Broadcast the whole thing
        synchronized (lock) {
            finished = false;

            // Tell the handler thread to start the broadcast
            handler
                    .obtainMessage(
                            SERVICE_MESSAGE_WHAT,
                            SERVICE_MESSAGE_ARG_1,
                            SERVICE_MESSAGE_ARG_2)
                    .sendToTarget();

            // Wait for the broadcast to complete
            while (!finished) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG, "Worker interrupted", e);
                }
            }

        }

        // Kill the handler thread again
        handlerThread.quit();

        return Result.success();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Support Classes

    /**
     * Handler for our ConnectIQ thread. This initiates the actual broadcast.
     */
    private final class ConnectIQThreadHandler extends Handler {
        public ConnectIQThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // If the message comes from us, load appointments and broadcast them
            List<Object> appointmentMsg = AppointmentMessageGenerator.prepareAppointmentMessage(
                    getApplicationContext());
            ConnectIQAppBroadcaster.broadcast(
                    appointmentMsg,
                    getApplicationContext(),
                    Utilities.APP_ID,
                    Utilities.getIQConnectType(),
                    new EventListener());
        }
    }

    /**
     * Listens to broadcasts being finished.
     */
    private final class EventListener implements IBroadcasterEventListener {
        @Override
        public void broadcastFinished() {
            synchronized (lock) {
                finished = true;
                lock.notifyAll();
            }
        }
    }

}
