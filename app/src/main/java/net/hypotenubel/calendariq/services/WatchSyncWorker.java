package net.hypotenubel.calendariq.services;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import net.hypotenubel.calendariq.connectiq.ConnectIQAppBroadcaster;
import net.hypotenubel.calendariq.connectiq.IBroadcasterEventListener;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * A worker that can be invoked regularly to send appointments to devices.
 */
public class WatchSyncWorker extends Worker {

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(WatchSyncWorker.class);

    // Message field values that indicate that a message to the handler thread was sent by us
    private static final int SERVICE_MESSAGE_WHAT = 0xdabcd41f;
    private static final int SERVICE_MESSAGE_ARG_1 = 0x2676922f;
    private static final int SERVICE_MESSAGE_ARG_2 = 0x67487fd4;

    /** Synchronization lock to wait for the broadcaster to finish. */
    private Object lock;
    /** Whether the broadcaster has already told us that it's finished. */
    private boolean finished = false;


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
            List<Object> appointmentMsg = AppointmentLoader.prepareAppointmentMessage(
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
