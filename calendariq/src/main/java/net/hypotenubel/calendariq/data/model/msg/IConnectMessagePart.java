package net.hypotenubel.calendariq.data.model.msg;

import java.util.List;

/**
 * One part of a message. A message part knows how to encode itself as a list of objects that can be
 * sent via Connect IQ.
 */
public interface IConnectMessagePart {

    /**
     * Encodes and appends this part's data to the given list of objects.
     */
    void encodeAndAppend(List<Object> target);

}
