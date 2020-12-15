package net.hypotenubel.calendariq.data.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.hypotenubel.calendariq.data.access.stats.BroadcastStatisticsDatabase;
import net.hypotenubel.calendariq.data.access.stats.IBroadcastStatisticsDao;
import net.hypotenubel.calendariq.data.connectiq.ConnectIQAppBroadcaster;
import net.hypotenubel.calendariq.data.connectiq.IBroadcasterEventListener;
import net.hypotenubel.calendariq.data.model.msg.AppointmentsConnectMessagePart;
import net.hypotenubel.calendariq.data.model.msg.BatteryChargeConnectMessagePart;
import net.hypotenubel.calendariq.data.model.msg.ConnectMessage;
import net.hypotenubel.calendariq.data.model.msg.SyncIntervalConnectMessagePart;
import net.hypotenubel.calendariq.data.model.stats.BroadcastStatistics;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    // Message field values that indicate that a message to the handler thread was sent by us
    private static final int SERVICE_MESSAGE_WHAT = 0xdabcd41f;
    private static final int SERVICE_MESSAGE_ARG_1 = 0x2676922f;
    private static final int SERVICE_MESSAGE_ARG_2 = 0x67487fd4;

    /** Synchronization lock to wait for the broadcaster to finish. */
    private final Object lock = new Object();
    /** Whether the broadcaster has already told us that it's finished. */
    private boolean finished = false;


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Management

    /**
     * Ensures that our synchronization worker is run by the work manager API.
     *
     * @param appContext the application's context to run the work manager in.
     * @param interval the interval in minutes it should be periodically run in.
     * @param replaceExisting {@code true} if an existing worker should be replaced. If this is
     *                                    {@code false}, nothing happens if a worker already exists.
     */
    public static void runSyncWorker(final Context appContext, int interval,
                                     boolean replaceExisting) {

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
                .getInstance(appContext)
                .enqueueUniquePeriodicWork(
                        SYNC_WORK_NAME,
                        policy,
                        request);
    }

    /**
     * Ensures that our synchronization worker is run once by the work manager API. Returns the
     * operation to be run so that callers can wait for that application to complete to show some
     * kind of a notification.
     *
     * @param appContext the application's context to run the work manager in.
     */
    public static Operation runSyncWorkerOnce(final Context appContext) {
        Log.d(LOG_TAG,"Running sync worker once");

        // Build a new periodic work request and register it if none was already registered
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WatchSyncWorker.class).build();
        return WorkManager
                .getInstance(appContext)
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

        Log.d(LOG_TAG, "Finished sending appointments to Garmin devices...");

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
        public void handleMessage(@NonNull  Message msg) {
            Context context = getApplicationContext();
            List<Object> appointmentMsg = new ConnectMessage()
                    .addMessagePart(AppointmentsConnectMessagePart.fromPreferences(context))
                    .addMessagePart(SyncIntervalConnectMessagePart.fromPreferences(context))
                    .addMessagePart(BatteryChargeConnectMessagePart.fromCurrentDeviceState(context))
                    .encode();

            if (Utilities.isEmulator()) {
                // If this is run inside the emulator, just pretend to have done something, randomly
                // being successful or not
                BroadcastEventListener listener = new BroadcastEventListener();

                Random rand = new Random();
                if (rand.nextBoolean()) {
                    new BroadcastEventListener().broadcastFinished(BroadcastStatistics.success(
                            1 + rand.nextInt(9)));
                } else {
                    int totalApps = 1 + rand.nextInt(9);
                    new BroadcastEventListener().broadcastFinished(BroadcastStatistics.failure(
                            totalApps,
                            1 + rand.nextInt(totalApps),
                            "Oh noes: something went terribly wrong at random!"));
                }

            } else {
                // If the message comes from us, load appointments and broadcast them
                ConnectIQAppBroadcaster.broadcast(
                        appointmentMsg,
                        getApplicationContext(),
                        Utilities.APP_IDS,
                        Utilities.getIQConnectType(),
                        new BroadcastEventListener());
            }
        }
    }

    /**
     * Listens to broadcasts being finished.
     */
    private final class BroadcastEventListener implements IBroadcasterEventListener {
        @Override
        public void broadcastFinished(BroadcastStatistics stats) {
            synchronized (lock) {
                finished = true;

                IBroadcastStatisticsDao dao = BroadcastStatisticsDatabase
                        .getInstance(getApplicationContext())
                        .getDao();
                dao.addWithoutGrowing(stats);

                lock.notifyAll();
            }
        }
    }

}
