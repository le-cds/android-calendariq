package net.hypotenubel.calendariq.sync.synchroniser;

import android.content.Context;

import com.garmin.android.connectiq.ConnectIQ;

import net.hypotenubel.calendariq.data.msg.model.ConnectMessage;
import net.hypotenubel.calendariq.sync.connectiq.ConnectIQAppBroadcaster;
import net.hypotenubel.calendariq.sync.connectiq.IBroadcasterEventListener;
import net.hypotenubel.calendariq.util.Utilities;

import javax.inject.Inject;

/**
 * Broadcasts to watchfaces.
 */
public class ConnectBroadcastStrategy implements IBroadcastStrategy {

    @Inject
    public ConnectBroadcastStrategy() {
    }

    @Override
    public void broadcast(ConnectMessage msg, Context appContext, IBroadcasterEventListener listener) {
        ConnectIQAppBroadcaster.broadcast(
                msg.encode(),
                appContext,
                Utilities.APP_IDS,
                ConnectIQ.IQConnectType.WIRELESS,
                listener);
    }

}
