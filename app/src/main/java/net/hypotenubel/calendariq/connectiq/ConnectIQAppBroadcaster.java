package net.hypotenubel.calendariq.connectiq;

import android.content.Context;
import android.util.Log;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.InvalidStateException;

import net.hypotenubel.calendariq.util.Utilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Use this class to broadcast a message to each installation of an app on any device that is
 * currently connected. Call {@link #broadcast(Object, Context, String, ConnectIQ.IQConnectType)}
 * to use the class. The method should be called from a separate thread.
 */
public class ConnectIQAppBroadcaster {

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(ConnectIQAppBroadcaster.class);


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State

    /** The event listener called when a broadcast has finished. */
    private final IBroadcasterEventListener listener;
    /** Our current context. */
    private final Context context;
    /** ConnectIQ instance we're using to communicate with devices. */
    private final ConnectIQ connectIQ;
    /** List of devices we'll have to ask for an installed app. */
    private final Queue<IQDevice> devicesToQuery = new LinkedList<>();
    /** The device currently being queried whether it has our app installed. */
    private IQDevice deviceCurrentlyQueried = null;
    /** Map of device / app object combinations that we'll send the message to. */
    private final Map<IQDevice, IQApp> messageRecipients = new HashMap<>();

    /** ID of the app we're communicating with. */
    private final String appId;
    /** Message to send to the app. */
    private final Object msg;

    /** Listener for initialization events. */
    private final InitializationListener initializationListener = new InitializationListener();
    /** Listener for application infos. */
    private final AppInfoListener applicationInfoListener = new AppInfoListener();
    /** Listener for message events. */
    private final ConnectIQ.IQSendMessageListener sendMessageListener = new SendMessageListener();


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    /**
     * Creates a new instance and sends the given message to the given app.
     */
    private ConnectIQAppBroadcaster(Object msg, Context context, String appId,
                                    ConnectIQ.IQConnectType connectionType,
                                    IBroadcasterEventListener listener) {

        this.listener = listener;
        this.msg = msg;
        this.context = context;
        this.appId = appId;

        // Obtain a ConnectIQ instance and start it up
        Log.d(LOG_TAG, "Obtaining ConnectIQ instance for " + connectionType.name());
        connectIQ = ConnectIQ.getInstance(context, connectionType);
        connectIQ.initialize(context, false, initializationListener);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Public Interface

    /**
     * Sends the message to the app with the given ID on any device where it's installed.
     *
     * @param msg the message to send.
     * @param context the context we're operating in.
     * @param appId the receiving application's ID.
     * @param connectionType the connection type.
     * @param listener optional event listener to be notified as the broadcast finishes.
     */
    public static void broadcast(Object msg, Context context, String appId,
                                 ConnectIQ.IQConnectType connectionType,
                                 IBroadcasterEventListener listener) {

        // The act of creating a new instance starts the sending process
        new ConnectIQAppBroadcaster(msg, context, appId, connectionType, listener);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Implementation

    private void queryNextDevice() {
        if (devicesToQuery.isEmpty()) {
            // We've finished querying devices, so send the messages
            sendMessages();

        } else {
            // Continue querying devices until a query succeeds
            boolean querySuccessful = false;
            while (!querySuccessful) {
                try {
                    deviceCurrentlyQueried = devicesToQuery.poll();

                    Log.d(LOG_TAG, "Obtaining application info for "
                            + deviceCurrentlyQueried.getDeviceIdentifier()
                            + " (" + deviceCurrentlyQueried.getFriendlyName() + ")");

                    connectIQ.getApplicationInfo(
                            appId, deviceCurrentlyQueried, applicationInfoListener);
                    querySuccessful = true;

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception while obtaining application info", e);
                }
            }
        }
    }

    /**
     * Sends the message to all device / app combinations we've found.
     */
    private void sendMessages() {
        if (!messageRecipients.isEmpty()) {
            for (Map.Entry<IQDevice, IQApp> entry : messageRecipients.entrySet()) {
                try {
                    IQDevice device = entry.getKey();
                    Log.d(LOG_TAG, "Sending message to "
                            + device.getDeviceIdentifier()
                            + " (" + device.getFriendlyName() + ")");
                    connectIQ.sendMessage(
                            device,
                            entry.getValue(),
                            msg,
                            sendMessageListener);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception while sending message", e);
                }
            }
        }

        finish();
    }

    /**
     * Shuts down ConnectIQ and notifies our listener, if any.
     */
    private void finish() {
        Log.d(LOG_TAG, "Broadcast finished, shutting down ConnectIQ");

        try {
            connectIQ.shutdown(context);
        } catch (InvalidStateException e) {
            Log.d(LOG_TAG, "Exception shutting down ConnectIQ", e);
        }

        // Notifiy the listener, if present
        if (listener != null) {
            listener.broadcastFinished();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener Classes

    /**
     * Implements reactions to ConnectIQ SDK initialization attempts.
     */
    private class InitializationListener implements ConnectIQ.ConnectIQListener {
        @Override
        public void onSdkReady() {
            Log.d(LOG_TAG, "ConnectIQ ready, discovering connected devices...");

            // Start querying devices
            try {
                devicesToQuery.addAll(connectIQ.getConnectedDevices());
                queryNextDevice();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while trying to obtain connected devices", e);
            }
        }

        @Override
        public void onInitializeError(ConnectIQ.IQSdkErrorStatus iqSdkErrorStatus) {
            Log.e(LOG_TAG, iqSdkErrorStatus.name());
        }

        @Override
        public void onSdkShutDown() {
            Log.d(LOG_TAG, "ConnectIQ shut down");
        }
    }

    /**
     * Finds out whether our app is installed on a device. If so, the message is sent to that
     * device. In any case, we query the next device for whether the app is installed there.
     */
    private class AppInfoListener implements ConnectIQ.IQApplicationInfoListener {

        @Override
        public void onApplicationInfoReceived(IQApp iqApp) {
            Log.d(LOG_TAG, "App found on " + deviceCurrentlyQueried.getDeviceIdentifier()
                    + " (" + deviceCurrentlyQueried.getFriendlyName() + ")");
            messageRecipients.put(deviceCurrentlyQueried, iqApp);
            queryNextDevice();
        }

        @Override
        public void onApplicationNotInstalled(String s) {
            Log.d(LOG_TAG, "App not found on " + deviceCurrentlyQueried.getDeviceIdentifier()
                    + " (" + deviceCurrentlyQueried.getFriendlyName() + "): " + s);
            queryNextDevice();
        }
    }

    private static class SendMessageListener implements ConnectIQ.IQSendMessageListener {
        @Override
        public void onMessageStatus(IQDevice iqDevice, IQApp iqApp,
                                    ConnectIQ.IQMessageStatus iqMessageStatus) {

            Log.d(LOG_TAG, "Message sent to " + iqApp.getApplicationId()
                    + " on " + iqDevice.getDeviceIdentifier()
                    + " with status " + iqMessageStatus.name());
        }
    }

}
