package net.hypotenubel.calendariq.sync.synchroniser;

import android.content.Context;

import net.hypotenubel.calendariq.data.msg.model.ConnectMessage;
import net.hypotenubel.calendariq.sync.connectiq.IBroadcasterEventListener;

/**
 * Abstracts the exact way messages are broadcast. This allows to mock the actual broadcast during
 * tests or when running on an emulator.
 */
public interface IBroadcastStrategy {

    /**
     * Sends the given message and informs the given listener about the result.
     */
    void broadcast(ConnectMessage msg, Context appContext, IBroadcasterEventListener listener);

}
