package net.hypotenubel.calendariq.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;

import net.hypotenubel.calendariq.connectiq.ConnectIQAppTransceiver;
import net.hypotenubel.calendariq.connectiq.ITransceiverEventListener;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.List;

public class WatchSyncService extends Service implements ITransceiverEventListener {

    /** Log tag used to log log messages in a logging fashion. */
    private static final String LOG_TAG = Utilities.logTag(WatchSyncService.class);
    /** The message that identifies an appointment request. */
    private static final String APPOINTMENT_REQUEST_MSG = "calendarservice.requestnextappointment";

    // Several message field values that indicate that a message was sent by us
    private static final int SERVICE_MESSAGE_WHAT = 0xdabcd41f;
    private static final int SERVICE_MESSAGE_ARG_1 = 0x2676922f;
    private static final int SERVICE_MESSAGE_ARG_2 = 0x67487fd4;

    /** The calendar transceiver listens for and replies to appointment request from devices. */
    private volatile ConnectIQAppTransceiver appTransceiver;

    /** Our handler thread used to run the app transceiver. */
    private HandlerThread handlerThread;
    /** THe handler we're using to communicate with the handler thread. */
    private volatile ConnectIQThreadHandler handler;


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Service Lifecycle

    @Override
    public void onCreate() {
        super.onCreate();

        // Start up our handler thread
        handlerThread = new HandlerThread("WatchSyncServiceThread");
        handlerThread.start();

        // Associate our custom handler with the looper
        handler = new ConnectIQThreadHandler(handlerThread.getLooper());

        // Send a message to the handler to start up the framework
        handler
                .obtainMessage(SERVICE_MESSAGE_WHAT, SERVICE_MESSAGE_ARG_1, SERVICE_MESSAGE_ARG_2)
                .sendToTarget();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Implement device refresh intents

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LOG_TAG, "onDestroy() called");

        // Stop the transceiver service if it's still running
        if (appTransceiver != null && appTransceiver.isRunning()) {
            // Removing us as a listener will prevent the onStop method from being called and
            // trying to shut down the service even though it's already shutting down
            appTransceiver.removeAppTransceiverListener(this);
            appTransceiver.stop();
        }

        // Stop our handler thread
        if (handlerThread != null) {
            handlerThread.quit();

            handlerThread = null;
            handler = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Handler Thread

    /**
     * Handles messages to our handler thread.
     */
    private final class ConnectIQThreadHandler extends Handler {
        public ConnectIQThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Check if the message originated from our service
            if (msg.what != SERVICE_MESSAGE_WHAT
                    || msg.arg1 != SERVICE_MESSAGE_ARG_1
                    || msg.arg2 != SERVICE_MESSAGE_ARG_2) {

                return;
            }

            if (appTransceiver == null) {
                // Create and start the transceiver
                appTransceiver = new ConnectIQAppTransceiver(
                        WatchSyncService.this,
                        Utilities.APP_ID,
                        Utilities.getIQConnectType());
                appTransceiver.addAppTransceiverListener(WatchSyncService.this);
                appTransceiver.start();

            } else {
                if (appTransceiver.isRunning()) {
                    // TODO We already have an app transceiver, so update its list of devices if it's running
                }
            }
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    // ConnectIQ Events

    @Override
    public void onMessageReceived(final IQDevice device, final IQApp app, final List<Object> msg) {
        Log.d(LOG_TAG, "Received request");
        if (isAppointmentRequest(msg)) {
            Log.d(LOG_TAG, "Request is an appointment request");
            appTransceiver.sendMessage(
                    device,
                    app,
                    AppointmentLoader.prepareAppointmentMessage(this));
        }
    }

    @Override
    public void onStopped() {
        // This is not called when the service is already shutting down
        stopSelf();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Appointment Request Handling

    /**
     * Checks if the given message was an appointment request.
     */
    private boolean isAppointmentRequest(List<Object> msg) {
        return msg.size() == 1 && APPOINTMENT_REQUEST_MSG.equals(msg.get(0));
    }

}
