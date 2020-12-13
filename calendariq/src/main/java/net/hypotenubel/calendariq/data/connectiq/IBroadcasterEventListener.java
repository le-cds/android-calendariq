package net.hypotenubel.calendariq.data.connectiq;

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
    void broadcastFinished(BroadcastResult stats);

}
