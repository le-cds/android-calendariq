package net.hypotenubel.calendariq.data.connectiq;

import android.content.Context;
import android.util.Log;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.InvalidStateException;

import net.hypotenubel.calendariq.data.model.BroadcastStatistics;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Use this class to broadcast a message to each installation of a list of apps on any device that
 * is currently connected. Call one of the {@code broadcast(...)} methods to get things rolling.
 * They should be called from a separate thread.
 *
 * <p>The way this thing works is a little convoluted thanks to all of the asynchronous operations.
 * Basically, this is what happens if everything goes according to plan:</p>
 * <ol>
 *     <li>We initialize ConnectIQ in the constructor.</li>
 *     <li>As soon as ConnectIQ responds, we continue in the {@link InitializationListener}. If
 *       initialization failed, we stop right there, set the error state, and call
 *       {@link #finish()}. If initialization was successful, we assemble the cross product of
 *       known apps and connected devices.</li>
 *     <li>Calling {@link #queryNextInstallation()}, we start working our way through that list,
 *       either confirming or denying that a given app is installed on a given device. We do so by
 *       issuing an application info request to ConnectIQ.</li>
 *     <li>The request's reply is received by an {@link AppInfoListener} instance. If the current
 *       app is installed on the current device, we add the pair as a recipient to send our
 *       broadcast message to later on. Either way, the listener calls
 *       {@link #queryNextInstallation()} again to issue a query for the next app-device pair.</li>
 *     <li>Once {@link #queryNextInstallation()} detects that there are no app-device pairs left to
 *       query, it calls {@link #sendMessages()} to start the broadcast.</li>
 *     <li>Once all messages have been sent, {@link #finish()} tries to shutdown the SDK and
 *       notifies the listener (if any) of the broadcast result.</li>
 * </ol>
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
    /** List of app installations we'll have to check for. */
    private final Queue<AppInstallation> installationsToQuery = new LinkedList<>();
    /** The installation currently being queried. */
    private AppInstallation installationCurrentlyQueried = null;
    /** Map of device / app object combinations that we'll send the message to. */
    private final List<AppInstallation> messageRecipients = new ArrayList<>();

    /** The number of messages we have to send to reach each app installation. */
    private int messagesToSend = 0;
    /** Number of messages we have tried to send. */
    private int sentMessages = 0;

    /** IDs of the apps we're communicating with. */
    private final List<String> appIds = new ArrayList<>();
    /** Message to send to the app. */
    private final Object msg;

    /** If this ceases to be {@code null}, an error has occurred. */
    private String errorMessage;

    /** Listener for application infos. */
    private final AppInfoListener applicationInfoListener = new AppInfoListener();
    /** Listener for message events. */
    private final ConnectIQ.IQSendMessageListener sendMessageListener = new SendMessageListener();


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    /**
     * Creates a new instance and sends the given message to the given app.
     */
    private ConnectIQAppBroadcaster(Object msg, Context context, List<String> appIds,
                                    ConnectIQ.IQConnectType connectionType,
                                    IBroadcasterEventListener listener) {

        this.listener = listener;
        this.msg = msg;
        this.context = context;
        this.appIds.addAll(appIds);

        // Obtain a ConnectIQ instance and start it up
        Log.d(LOG_TAG, "Obtaining ConnectIQ instance for " + connectionType.name());
        connectIQ = ConnectIQ.getInstance(context, connectionType);

        // Once finished this will pass control to the InitializationListener below
        connectIQ.initialize(context, false, new InitializationListener());
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
        List<String> ids = new ArrayList<>();
        ids.add(appId);

        new ConnectIQAppBroadcaster(msg, context, ids, connectionType, listener);
    }

    /**
     * Sends the message to the apps with the given IDs on any device where they are installed.
     *
     * @param msg the message to send.
     * @param context the context we're operating in.
     * @param appIds the receiving applications' IDs.
     * @param connectionType the connection type.
     * @param listener optional event listener to be notified as the broadcast finishes.
     */
    public static void broadcast(Object msg, Context context, List<String> appIds,
                                 ConnectIQ.IQConnectType connectionType,
                                 IBroadcasterEventListener listener) {

        // The act of creating a new instance starts the sending process
        new ConnectIQAppBroadcaster(msg, context, appIds, connectionType, listener);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Implementation

    private void queryNextInstallation() {
        // Be sure to stop if an error has occurred
        if (isError()) {
            return;
        }

        if (installationsToQuery.isEmpty()) {
            // We've finished querying devices, so send the messages
            sendMessages();

        } else {
            // Continue querying devices until a query succeeds
            boolean querySuccessful = false;
            while (!querySuccessful) {
                try {
                    installationCurrentlyQueried = installationsToQuery.poll();

                    Log.d(LOG_TAG, "Querying app "
                            + installationCurrentlyQueried.appId
                            + " on device "
                            + installationCurrentlyQueried.device.getDeviceIdentifier()
                            + " (" + installationCurrentlyQueried.device.getFriendlyName() + ")");

                    connectIQ.getApplicationInfo(
                            installationCurrentlyQueried.appId,
                            installationCurrentlyQueried.device,
                            applicationInfoListener);
                    querySuccessful = true;

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception while obtaining application info", e);
                    error(e.getClass().getSimpleName()
                        + " while obtaining application info: "
                        + e.getMessage());
                }
            }
        }
    }

    /**
     * Sends the message to all device / app combinations we've found.
     */
    private void sendMessages() {
        messagesToSend = messageRecipients.size();

        for (AppInstallation appInstallation : messageRecipients) {
            // Be sure to stop if an error has occurred
            if (isError()) {
                Log.d(LOG_TAG, "Not sending messages because the error flag is set.");
                return;
            }

            try {
                IQDevice device = appInstallation.device;
                Log.d(LOG_TAG, "Sending message to "
                        + appInstallation.app.getDisplayName()
                        + " on "
                        + appInstallation.device.getDeviceIdentifier()
                        + " (" + appInstallation.device.getFriendlyName() + ")");
                connectIQ.sendMessage(
                        appInstallation.device,
                        appInstallation.app,
                        msg,
                        sendMessageListener);
                sentMessages++;

            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while sending message", e);
                error(e.getClass().getSimpleName()
                    + " while sending messages: "
                    + e.getMessage());
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
            // We don't set an error state here because this might mask a more important previous
            // error
            Log.d(LOG_TAG, "Exception shutting down ConnectIQ", e);
        }

        // Notifiy the listener, if present
        if (listener != null) {
            if (isError()) {
                listener.broadcastFinished(BroadcastStatistics.failure(
                        messagesToSend, sentMessages, errorMessage));
            } else {
                listener.broadcastFinished(BroadcastStatistics.success(sentMessages));
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Utilities

    /**
     * Whether an error has occurred during broadcast. If this is {@code true}, it's time to
     * {@link #finish()}.
     */
    private boolean isError() {
        return errorMessage != null;
    }

    /**
     * Set the error state to the given message and call {@link #finish()}.
     * @param message
     */
    private void error(String message) {
        errorMessage = message;
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

            // Start querying installations
            try {
                // We'll look for each app on every connected device
                for (IQDevice device : connectIQ.getConnectedDevices()) {
                    for (String appId : appIds) {
                        installationsToQuery.add(new AppInstallation(device, appId));
                    }
                }

                queryNextInstallation();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while trying to obtain connected devices", e);
                error(e.getClass().getSimpleName()
                        + " while trying to obtain connected devices: "
                        + e.getMessage());
            }
        }

        @Override
        public void onInitializeError(ConnectIQ.IQSdkErrorStatus iqSdkErrorStatus) {
            Log.e(LOG_TAG, iqSdkErrorStatus.name());
            error("Unable to initialize ConnectIQ. SDK error status " + iqSdkErrorStatus.name());
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
            Log.d(LOG_TAG, "App "
                    + installationCurrentlyQueried.appId
                    + " found on "
                    + installationCurrentlyQueried.device.getDeviceIdentifier()
                    + " (" + installationCurrentlyQueried.device.getFriendlyName() + ")");

            // Store the IQApp object
            installationCurrentlyQueried.app = iqApp;
            messageRecipients.add(installationCurrentlyQueried);

            queryNextInstallation();
        }

        @Override
        public void onApplicationNotInstalled(String s) {
            Log.d(LOG_TAG, "App "
                    + installationCurrentlyQueried.appId
                    + " not found on "
                    + installationCurrentlyQueried.device.getDeviceIdentifier()
                    + " (" + installationCurrentlyQueried.device.getFriendlyName() + "): " + s);
            queryNextInstallation();
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Data Holding

    /**
     * Represents an app on a device. If we haven't queried the device for the app yet, this object
     * will only cary the app ID. Once we found that the app is installed, it will carry the
     * associated app object as well.
     */
    private static class AppInstallation {

        /** The device the app might be or is installed on. */
        private final IQDevice device;
        /** The app's ID. */
        private final String appId;
        /** The application object, provided it is installed on the device. */
        private IQApp app;

        private AppInstallation(IQDevice device, String appId) {
            this.device = device;
            this.appId = appId;
        }

    }

}
