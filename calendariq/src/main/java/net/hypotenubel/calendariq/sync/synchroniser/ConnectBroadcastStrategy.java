package net.hypotenubel.calendariq.sync.synchroniser;

import android.content.Context;

import com.garmin.android.connectiq.ConnectIQ;

import net.hypotenubel.calendariq.data.apps.model.TargetApps;
import net.hypotenubel.calendariq.data.apps.source.ITargetAppIdsSource;
import net.hypotenubel.calendariq.data.msg.model.ConnectMessage;
import net.hypotenubel.calendariq.sync.connectiq.ConnectIQAppBroadcaster;
import net.hypotenubel.calendariq.sync.connectiq.IBroadcasterEventListener;

import javax.inject.Inject;

/**
 * Broadcasts to watchfaces.
 */
public class ConnectBroadcastStrategy implements IBroadcastStrategy {

    private final TargetApps targetApps;

    @Inject
    public ConnectBroadcastStrategy(ITargetAppIdsSource targetAppSource) {
        this.targetApps = targetAppSource.getTargetApps();
    }

    @Override
    public void broadcast(ConnectMessage msg, Context appContext, IBroadcasterEventListener listener) {
        ConnectIQAppBroadcaster.broadcast(
                msg.encode(),
                appContext,
                targetApps.getTargetAppIds(),
                ConnectIQ.IQConnectType.WIRELESS,
                listener);
    }

}
