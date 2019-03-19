package net.hypotenubel.calendariq.connectiq;

import android.content.Context;
import android.util.Log;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.InvalidStateException;

import net.hypotenubel.calendariq.util.Utilities;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <h3>Possible Problems</h3>
 *
 * <ul>
 * <li>If a new device is paired with ConnectIQ, the transceiver needs to be stopped and started
 * again to refresh its list of devices.</li>
 * <li>If our app is installed on a device, that device must be disconnected and reconnected again
 * for us to discover the app. Alternatively, the transceiver needs to be stopped and started
 * again.</li>
 * <li>There may be multithreading bugs in here.</li>
 * </ul>
 */
public class ConnectIQAppTransceiver {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Definitions

    /**
     * All the possible states the transceiver can be in.
     */
    private enum State {
        /** The ConnectIQ library is not initialized. */
        STOPPED,
        /** The ConnectIQ library is being started. */
        STARTING,
        /** The ConnectIQ library is scheduled to be stopped. */
        STOPPING,
        /** The ConnectIQ library is fully initialized. */
        STARTED;
    }

    /** Log tag for log messages. */
    private static final String LOG_TAG = Utilities.logTag(ConnectIQAppTransceiver.class);


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State

    /** Our current context. */
    private final Context context;
    /** ConnectIQ instance we're using to communicate with devices. */
    private final ConnectIQ connectIQ;

    /** ID of the app we're communicating with. */
    private final String appId;
    /** The IQApp object we're using to represent our app. */
    private final IQApp iqApp;

    /** Event listeners listening to what we have to say. */
    private final Set<ITransceiverEventListener> eventListeners = new LinkedHashSet<>();

    /** Listener for initialization events. */
    private final InitializationListener initializationListener = new InitializationListener();
    /** Listener for application-related events. */
    private final ApplicationEventListener appEventListener = new ApplicationEventListener();
    /** Listener for device events. */
    private final DeviceListener deviceListener = new DeviceListener();
    /** Listener for message events. */
    private final ConnectIQ.IQSendMessageListener sendMessageListener = new SendMessageListener();

    /** The state we're currently in. */
    private State state = State.STOPPED;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor

    /**
     * Creates a new instance that listens for messages from the given app.
     *
     * @param context
     *         the context we're operating in.
     * @param appId
     *         ID of the app whose messages to listen for.
     * @param connectionType
     *         whether we're connecting to Bluetooth devices or to the simulator.
     */
    public ConnectIQAppTransceiver(Context context, String appId,
                                   ConnectIQ.IQConnectType connectionType) {

        this.context = context;
        this.appId = appId;
        this.iqApp = new IQApp(appId);

        // Obtain a ConnectIQ instance
        Log.d(LOG_TAG, "Obtaining ConnectIQ instance for " + connectionType.name());
        connectIQ = ConnectIQ.getInstance(context, connectionType);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Public Interface

    /**
     * Starts up the transceiver by initializing the ConnectIQ framework. Once that is initialized,
     * we subscribe to all devices and each instance of the app we're going for.
     *
     * @throws IllegalStateException
     *         if the transceiver is not currently stopped.
     */
    public final void start() {
        if (state != State.STOPPED) {
            throw new IllegalStateException(state.name());
        }

        Log.d(LOG_TAG, state.name() + " -> STARTING");
        state = State.STARTING;
        connectIQ.initialize(context, false, initializationListener);
    }

    /**
     * Stops the transceiver by shutting down ConnectIQ. This method is only valid to be called if
     * the framework is not
     *
     * @throws IllegalStateException
     *         if this method is called while the transceiver is not running.
     */
    public final void stop() {
        switch (state) {
            case STARTING:
                // If this is called before initialization has finished, indicate that we want to
                // stop directly after initialization has finished in the listener
                Log.d(LOG_TAG, state.name() + " -> STOPPING");
                state = State.STOPPING;
                break;

            case STARTED:
                // Actually do stop
                doStop();

            case STOPPING:
                // We're already stopping, so do nothing...

            default:
                throw new IllegalStateException(state.name());
        }
    }

    /**
     * Checks whether the transceiver is currently running and can be used to send and receive
     * stuff.
     *
     * @return whether the transceiver is currently running.
     */
    public final boolean isRunning() {
        return state == State.STARTED;
    }

    /**
     * Sends the given message to the app on the given device.
     *
     * @param device the device the target app is supposed to be running on.
     * @param app the app to send the message to.
     * @param msg the message itself.
     * @throws IllegalStateException if {@link #isRunning()} returns {@code false}.
     */
    public final void sendMessage(final IQDevice device, final IQApp app, final List<Object> msg) {
        if (!isRunning()) {
            throw new IllegalStateException("Sending a message requires the framework to be "
                    + "started. Current state: " + state.name());
        }

        Log.d(LOG_TAG, "Sending message to " + app.getApplicationId()
                + " on " + device.getDeviceIdentifier());

        // TODO Enqueue send requests and process them on a separate worker thread?
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectIQ.sendMessage(device, app, msg, sendMessageListener);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception while trying to send a message", e);
                }
            }
            }).start();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Internal Stuff

    /**
     * Shuts down ConnectIQ and sets the state to {@link State#STOPPED}.
     */
    private void doStop() {
        try {
            connectIQ.shutdown(context);
        } catch (InvalidStateException e) {
            Log.e(LOG_TAG, "Exception while trying to shot down ConnectIQ", e);
        }

        Log.d(LOG_TAG, state.name() + " -> STOPPED");
        state = State.STOPPED;

        // Let listeners react
        onStopped();
    }

    /**
     * Obtains all devices and remembers those that have our app installed.
     */
    private void discoverDevices() {
        Log.d(LOG_TAG, "Discovering devices...");

        try {
            // Find all known devices and register for events
            List<IQDevice> devices = connectIQ.getKnownDevices();
            for (IQDevice device : devices) {
                Log.d(LOG_TAG, "Registering for events on " + device.getDeviceIdentifier()
                        + " (" + device.getFriendlyName() + ")");

                /* Normally, we'd want to check whether our app is installed on the device. That
                 * would require us to send an application info request once the device is connected
                 * and have an ApplicationInfoListener wait for the response. The way the ConnectIQ
                 * library works, however, only one such listener can be active at a time, over all
                 * devices. We would thus have to sequentialize application info requests, querying
                 * the next device once the request to the previous has been answered. This could be
                 * done in the future, but for now we just build our own IQApp and simply register
                 * for device and app events, whether it's installed on the device or not.
                 */
                connectIQ.registerForEvents(device, deviceListener, iqApp, appEventListener);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while trying to obtain known devices", e);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Events and Listeners

    /**
     * Adds a listener to the list of event listeners.
     *
     * @param listener the listener to add.
     */
    public final void addAppTransceiverListener(ITransceiverEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        eventListeners.add(listener);
    }

    /**
     * Removes a listener from the list of event listeners.
     *
     * @param listener the listener to remove.
     */
    public final void removeAppTransceiverListener(ITransceiverEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        eventListeners.remove(listener);
    }

    /**
     * Called whenever a message is received from the app on a device. Override this method to
     * react appropriately. Implementations of this method should probably be thread-safe.
     *
     * @param device the device that sent the message.
     * @param app app object.
     * @param msg the message itself.
     */
    private void onMessageReceived(IQDevice device, IQApp app, List<Object> msg) {
        for (ITransceiverEventListener listener : eventListeners) {
            listener.onMessageReceived(device, app, msg);
        }
    }

    /**
     * Called once we've stopped connecting to the ConnectIQ library. This might be because
     * initialization has failed or in response to the {@link #stop()} method having been called.
     */
    private void onStopped() {
        for (ITransceiverEventListener listener : eventListeners) {
            listener.onStopped();
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
            // If stop() was called in the meantime, stop the SDK again; otherwise, we're up and
            // running
            if (state == State.STOPPING) {
                doStop();
            } else {
                Log.d(LOG_TAG, state.name() + " -> STARTED");
                state = State.STARTED;

                discoverDevices();
            }
        }

        @Override
        public void onInitializeError(ConnectIQ.IQSdkErrorStatus iqSdkErrorStatus) {
            Log.e(LOG_TAG, iqSdkErrorStatus.name());
            Log.d(LOG_TAG, state.name() + " -> STOPPED");
            state = State.STOPPED;
            onStopped();
        }

        @Override
        public void onSdkShutDown() {
            Log.d(LOG_TAG, state.name() + " -> STOPPED");
            state = State.STOPPED;
            onStopped();
        }
    }

    /**
     * Listens for events on a device. When a device becomes available, we check whether our app is
     * installed there. If so, the appropriate listener will register for that app's events and add
     * it to our list of app instances to send messages to.
     */
    private class DeviceListener implements ConnectIQ.IQDeviceEventListener {
        @Override
        public void onDeviceStatusChanged(IQDevice device, IQDevice.IQDeviceStatus status) {
            Log.d(LOG_TAG, device.getDeviceIdentifier() + " changed status to " + status.name());

            if (status == IQDevice.IQDeviceStatus.CONNECTED) {
//                try {
//                    connectIQ.registerForAppEvents(device, iqApp, appEventListener);
//                } catch (InvalidStateException e) {
//                    Log.e(LOG_TAG, "Failed to register for application events", e);
//                }

            } else {
//                try {
//                    // Ensure that we stop listening for app events
//                    connectIQ.unregisterForApplicationEvents(device, iqApp);
//                } catch (InvalidStateException e) {
//                    Log.e(LOG_TAG, "Failed to unregister for application events", e);
//                }
            }
        }

    }

    /**
     * Listens for application messages and calls the appropriate event method.
     */
    private class ApplicationEventListener implements ConnectIQ.IQApplicationEventListener {
        @Override
        public void onMessageReceived(IQDevice iqDevice, IQApp iqApp, List<Object> list,
                                      ConnectIQ.IQMessageStatus iqMessageStatus) {

            Log.d(LOG_TAG, "Message received from " + iqApp.getApplicationId()
                    + " on " + iqDevice.getDeviceIdentifier()
                    + " with status " + iqMessageStatus.name());
            if (list == null) {
                Log.d(LOG_TAG, "Message has null payload");
            } else {
                Log.d(LOG_TAG, "Message has payload of " + list.size() + " items");
            }

            ConnectIQAppTransceiver.this.onMessageReceived(iqDevice, iqApp, list);
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
