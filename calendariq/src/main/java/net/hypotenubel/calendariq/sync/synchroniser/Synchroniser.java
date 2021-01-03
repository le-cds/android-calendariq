package net.hypotenubel.calendariq.sync.synchroniser;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import net.hypotenubel.calendariq.data.calendar.source.ICalendarSource;
import net.hypotenubel.calendariq.data.msg.model.AppointmentsConnectMessagePart;
import net.hypotenubel.calendariq.data.msg.model.BatteryChargeConnectMessagePart;
import net.hypotenubel.calendariq.data.msg.model.ConnectMessage;
import net.hypotenubel.calendariq.data.msg.model.SyncIntervalConnectMessagePart;
import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;
import net.hypotenubel.calendariq.data.stats.source.IBroadcastStatisticsDao;
import net.hypotenubel.calendariq.sync.connectiq.IBroadcasterEventListener;
import net.hypotenubel.calendariq.util.Utilities;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Assembles and sends data and updates the broadcast statistics. How the actual broadcast is
 * performed is controlled through an {@link IBroadcastStrategy}.
 */
public class Synchroniser implements Runnable {

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(Synchroniser.class);

    // Message field values that indicate that a message to the handler thread was sent by us
    private static final int SERVICE_MESSAGE_WHAT = 0xdabcd41f;
    private static final int SERVICE_MESSAGE_ARG_1 = 0x2676922f;
    private static final int SERVICE_MESSAGE_ARG_2 = 0x67487fd4;

    /** Application context. */
    private final Context appContext;
    /** Access to appointments. */
    private final ICalendarSource calendarSource;
    /** How exactly we'll broadcast our message. */
    private final IBroadcastStrategy broadcastStrategy;
    /** Access to the broadcast statistics database. */
    private final IBroadcastStatisticsDao broadcastStatisticsDao;

    /** Synchronization lock to wait for the broadcaster to finish. */
    private final Object lock = new Object();
    /** Whether the broadcaster has already told us that it's finished. */
    private boolean finished = false;

    @Inject
    public Synchroniser(@ApplicationContext Context context, ICalendarSource calendarSource,
                        IBroadcastStrategy broadcastStrategy,
                        IBroadcastStatisticsDao statsDao) {
        this.appContext = context;
        this.calendarSource = calendarSource;
        this.broadcastStrategy = broadcastStrategy;
        this.broadcastStatisticsDao = statsDao;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Synchronisation

    @Override
    public void run() {
        Log.d(LOG_TAG, "Sending appointments to Garmin devices...");

        // ConnectIQ requires a thread with a looper :/
        HandlerThread handlerThread = new HandlerThread("SyncThread");
        handlerThread.start();
        SyncHandler handler = new SyncHandler(handlerThread.getLooper());

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
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Support Classes

    /**
     * Handler for our ConnectIQ thread. This initiates the actual broadcast.
     */
    private final class SyncHandler extends Handler {
        public SyncHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull  Message msg) {
            ConnectMessage connectMessage = new ConnectMessage()
                    .addMessagePart(AppointmentsConnectMessagePart.fromPreferences(appContext, calendarSource))
                    .addMessagePart(SyncIntervalConnectMessagePart.fromPreferences(appContext))
                    .addMessagePart(BatteryChargeConnectMessagePart.fromCurrentDeviceState(appContext));
            broadcastStrategy.broadcast(connectMessage, appContext, new BroadcastEventListener());
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

                // TODO Replace AsyncTask as soon as we have a broadcast statistics repository.
                AsyncTask.execute(() -> {
                    broadcastStatisticsDao.addWithoutGrowing(stats);
                });

                lock.notifyAll();
            }
        }
    }

}
