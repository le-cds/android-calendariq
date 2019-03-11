package net.hypotenubel.calendariq.connectiq;

import android.content.Context;
import android.util.Log;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.InvalidStateException;

import net.hypotenubel.calendariq.util.Utilities;

import java.util.HashMap;
import java.util.HashSet;
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
        /** The ConnectIQ library is currently being initialized. */
        INITIALIZING,
        /** The ConnectIQ library is up and running. */
        RUNNING,
        /** The ConnectIQ library is currently being or is scheduled to shut down. */
        STOPPING;
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

    /** Event listeners listening to what we have to say. */
    private final Set<ITransceiverEventListener> eventListeners = new LinkedHashSet<>();

    /** The one application event listener we're using. */
    private final ApplicationEventListener appEventListener = new ApplicationEventListener();
    /** The one send message listener we're using. */
    private final ConnectIQ.IQSendMessageListener sendMessageListener = new SendMessageListener();

    /** The state we're currently in. */
    private State state = State.STOPPED;

    /** Set of devices that we still have to finish querying about their application status. */
    private final Set<IQDevice> unqueriedDevices = new HashSet<>();
    /** Map of devices to the copy of our app installed there. */
    private final Map<IQDevice, IQApp> deviceApps = new HashMap<>();


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

        Log.d(LOG_TAG, state.name() + " -> INITIALIZING");
        state = State.INITIALIZING;
        connectIQ.initialize(context, false, new InitializationListener());
    }

    /**
     * Stops the transceiver by shutting down ConnectIQ.
     *
     * @throws IllegalStateException
     *         if this method is called while the transceiver is not running.
     */
    public final void stop() {
        if (state == State.INITIALIZING) {
            // If this is called before initialization has finished, indicate that we want to stop
            // directly after initialization has finished in the listener
            Log.d(LOG_TAG, state.name() + " -> STOPPING");
            state = State.STOPPING;

        } else if (state == State.RUNNING) {
            // Actually do stop
            doStop();

        } else {
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
        return state == State.RUNNING;
    }

    /**
     * Sends the given message to the app on the given device.
     *
     * @param device the device the target app is supposed to be running on.
     * @param app the app to send the message to.
     * @param msg the message itself.
     */
    public final void sendMessage(final IQDevice device, final IQApp app, final List<Object> msg) {
            Log.d(LOG_TAG, "Sending message to " + app.getDisplayName()
                    + " on " + device.getFriendlyName());

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

    /**
     * Sends the given message to all of the app's instances over all devices.
     *
     * @param msg the message.
     */
    public final void broadcastMessage(List<Object> msg) {
        Log.d(LOG_TAG, "Broadcasting message");
        for (Map.Entry<IQDevice, IQApp> app : deviceApps.entrySet()) {
            sendMessage(app.getKey(), app.getValue(), msg);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Internal Stuff

    /**
     * Shuts down ConnectIQ and sets the state to {@link State#STOPPED}.
     */
    private void doStop() {
        try {
            connectIQ.unregisterAllForEvents();
            connectIQ.shutdown(context);
        } catch (InvalidStateException e) {
            Log.e(LOG_TAG, "Exception while trying to shot down ConnectIQ", e);
        }

        Log.d(LOG_TAG, state.name() + " -> STOPPED");
        state = State.STOPPED;

        // Ensure that our set and map are clear
        synchronized (unqueriedDevices) {
            unqueriedDevices.clear();
        }

        synchronized (deviceApps) {
            deviceApps.clear();
        }

        onStopped();
    }

    /**
     * Obtains all devices and remembers those that have our app installed.
     */
    private void discoverDevices() {
        Log.d(LOG_TAG, "Discovering decives...");

        try {
            // Create a DeviceListener for each device. If the app is available there, it will
            // find out
            List<IQDevice> devices = connectIQ.getConnectedDevices();
            while (devices == null || devices.isEmpty()) {
                Thread.sleep(500);
                devices = connectIQ.getConnectedDevices();
            }

            synchronized (unqueriedDevices) {
                unqueriedDevices.addAll(devices);
            }

            for (IQDevice device : devices) {
                Log.d(LOG_TAG, "Creating DeviceListener for " + device.getFriendlyName());
                new DeviceListener(device);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while trying to obtain connected devices", e);
        }
    }

    /**
     * Notifies us that we have finished determining the app installation status on the given
     * device. Once all devices have finished querying, we can transition into
     * {@link State#RUNNING}.
     */
    private void removeUnqueriedDevice(IQDevice device) {
        Log.d(LOG_TAG, "Querying " + device.getFriendlyName() + " finished");
        boolean unqueriedDevicesEmpty = false;

        // Keep the atomic section as short as possible
        synchronized (unqueriedDevices) {
            unqueriedDevices.remove(device);
            unqueriedDevicesEmpty = unqueriedDevices.isEmpty();
        }

        if (unqueriedDevicesEmpty) {
            Log.d(LOG_TAG, state.name() + " -> RUNNING");
            state = State.RUNNING;
            onRunning();
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
            throw new IllegalArgumentException("listener must not be null");
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
            throw new IllegalArgumentException("listener must not be null");
        }

        eventListeners.remove(listener);
    }

    /**
     * Called once all devices have finished reporting their application installation status.
     * Override this method to send a message to all known installations of our app as soon as
     * possible.
     */
    protected void onRunning() {
        Log.d(LOG_TAG, "onRunning");
        for (ITransceiverEventListener listener : eventListeners) {
            listener.onRunning();
        }
    }

    /**
     * Called when a new device with an app is discovered after the initial discovery phase.
     * Override if you need to start communicating with apps as soon as their device is paired.
     *
     * @param device the device.
     * @param app the app.
     */
    protected void onAppDiscovered(IQDevice device, IQApp app) {
        Log.d(LOG_TAG, "onAppDiscovered for " + app.getDisplayName()
                + " on " + device.getFriendlyName());
        for (ITransceiverEventListener listener : eventListeners) {
            listener.onAppDiscovered(device, app);
        }
    }

    /**
     * Called whenever a message is received from the app on a device. Override this method to
     * react appropriately. Implementations of this method should probably be thread-safe.
     *
     * @param device the device that sent the message.
     * @param app app object.
     * @param msg the message itself.
     */
    protected void onMessageReceived(IQDevice device, IQApp app, List<Object> msg) {
        Log.d(LOG_TAG, "onMessageReceived from " + app.getDisplayName()
                + " on " + device.getFriendlyName());
        for (ITransceiverEventListener listener : eventListeners) {
            listener.onMessageReceived(device, app, msg);
        }
    }

    /**
     * Called once we've stopped connecting to the ConnectIQ library. This might be because
     * initialization has failed or in response to the {@link #stop()} method having been called.
     */
    protected void onStopped() {
        Log.d(LOG_TAG, "onStopped");
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

        public DeviceListener(IQDevice device) {
            try {
                connectIQ.registerForDeviceEvents(device, this);
                discoverApp(device);

            } catch (InvalidStateException e) {
                removeUnqueriedDevice(device);

                Log.e(LOG_TAG, "Exception while trying to register for device events", e);
            }
        }

        @Override
        public void onDeviceStatusChanged(IQDevice device, IQDevice.IQDeviceStatus status) {
            Log.d(LOG_TAG, device.getFriendlyName() + " changed status to " + status.name());

            if (status == IQDevice.IQDeviceStatus.CONNECTED) {
                // Check if the app is on the device
                discoverApp(device);

            } else {
                synchronized (deviceApps) {
                    if (deviceApps.containsKey(device)) {
                        try {
                            // Ensure that we stop listening for app events
                            connectIQ.unregisterForApplicationEvents(
                                    device, deviceApps.get(device));
                        } catch (InvalidStateException e) {
                            Log.e(LOG_TAG, "Failed to unregister for application events", e);
                        }

                        // Remove this device from our list of devices to send messages to
                        deviceApps.remove(device);
                    }
                }
            }
        }

        private void discoverApp(IQDevice device) {
            Log.d(LOG_TAG, "Attempting to discover app on " + device.getFriendlyName());

            try {
                connectIQ.getApplicationInfo(appId, device, new ApplicationInfoListener(device));
            } catch (Exception e) {
                removeUnqueriedDevice(device);

                Log.e(LOG_TAG, "Exception while trying to obtain application info", e);
            }
        }

    }

    /**
     * Listens for replies to app discovery requests. If an app is installed on a device, this
     * listener will put the device and its app object into our device-application map. Otherwise,
     * it won't do anything.
     */
    private class ApplicationInfoListener implements ConnectIQ.IQApplicationInfoListener {

        /** The device we're listening on. */
        private IQDevice device;

        public ApplicationInfoListener(IQDevice device) {
            this.device = device;
        }

        @Override
        public void onApplicationInfoReceived(IQApp iqApp) {
            Log.d(LOG_TAG, "Received application info for " + iqApp.getDisplayName()
                    + " on " + device.getFriendlyName());

            try {
                // Start listening for messages from that application
                connectIQ.registerForAppEvents(device, iqApp, appEventListener);

                // Remember the combination of device and app
                synchronized (deviceApps) {
                    deviceApps.put(device, iqApp);
                }

            } catch (InvalidStateException e) {
                Log.e(LOG_TAG, "Failed to register for application events", e);
            }

            if (state == State.RUNNING) {
                // We've discovered a new app, so call the appropriate event
                onAppDiscovered(device, iqApp);
            } else {
                removeUnqueriedDevice(device);
            }
        }

        @Override
        public void onApplicationNotInstalled(String s) {
            Log.d(LOG_TAG, "App not installed: " + s);
            removeUnqueriedDevice(device);
        }

    }

    /**
     * Listens for application messages and calls the appropriate event method.
     */
    private class ApplicationEventListener implements ConnectIQ.IQApplicationEventListener {

        @Override
        public void onMessageReceived(IQDevice iqDevice, IQApp iqApp, List<Object> list,
                                      ConnectIQ.IQMessageStatus iqMessageStatus) {

            ConnectIQAppTransceiver.this.onMessageReceived(iqDevice, iqApp, list);
        }

    }

    private static class SendMessageListener implements ConnectIQ.IQSendMessageListener {

        @Override
        public void onMessageStatus(IQDevice iqDevice, IQApp iqApp,
                                    ConnectIQ.IQMessageStatus iqMessageStatus) {

            Log.d(LOG_TAG, "Message to " + iqApp.getDisplayName()
                    + " on " + iqDevice.getFriendlyName()
                    + " sent with status " + iqMessageStatus.name());
        }
    }

}
