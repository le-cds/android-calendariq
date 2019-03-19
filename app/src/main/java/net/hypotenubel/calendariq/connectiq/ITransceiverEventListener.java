package net.hypotenubel.calendariq.connectiq;

import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;

import java.util.List;

/**
 * Classes implementing this interface can listen and react to events on
 * {@link ConnectIQAppTransceiver}s.
 */
public interface ITransceiverEventListener {

    /**
     * Called whenever a message is received from the app on a device.
     *
     * @param device the device that sent the message.
     * @param app app object.
     * @param msg the message itself.
     */
    void onMessageReceived(IQDevice device, IQApp app, List<Object> msg);

    /**
     * Called once we've stopped connecting to the ConnectIQ library. This might be because
     * initialization has failed or in response to the {@link ConnectIQAppTransceiver#stop()} method
     * having been called.
     */
    void onStopped();

}
