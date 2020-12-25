package net.hypotenubel.calendariq.sync.connectiq;

import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;

/**
 * Classes implementing this interface can listen and react to events on
 * {@link ConnectIQAppBroadcaster}s.
 */
public interface IBroadcasterEventListener {

    /**
     * Called when a broadcast has finished.
     *
     * @param stats statistics about the broadcast.
     */
    void broadcastFinished(BroadcastStatistics stats);

}
