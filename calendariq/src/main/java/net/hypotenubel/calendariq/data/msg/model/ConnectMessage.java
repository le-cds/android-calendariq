package net.hypotenubel.calendariq.data.msg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Something that can be sent to devices over ConnectIQ. The content of a message is determined by
 * a prefix, followed by zero or more {@link IConnectMessagePart}s.
 */
public class ConnectMessage {

    /** Message parts that will define the message's content. */
    private final List<IConnectMessagePart> parts = new ArrayList<>();

    public ConnectMessage addMessagePart(IConnectMessagePart part) {
        parts.add(part);
        return this;
    }

    /**
     * Returns a list of objects that encode this message and are ready to be sent via ConnectIQ.
     */
    public List<Object> encode() {
        List<Object> msg = new ArrayList<>();

        // Timestamp in seconds UTC. Note that, at least according to the documentation, MonkeyC
        // doesn't support Java's long type, just ints. The following cast doesn't truncate until
        // 2038-01-19 at 03:14:07
        msg.add((int) (System.currentTimeMillis() / 1000));

        parts.stream().forEach(part -> part.encodeAndAppend(msg));

        return msg;
    }

}
