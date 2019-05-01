package net.hypotenubel.calendariq.connectiq;

import androidx.annotation.NonNull;

/**
 * Statistics about a broadcast collected by {@link ConnectIQAppBroadcaster}. Each instance can be
 * turned into a String and parsed from a string.
 */
public final class BroadcastStats {

    /** Time when the broadcast finished. */
    private final long utcTimestampMillis;
    /** Number of devices the broadcast included. */
    private final int devices;


    /**
     * Creates a new instance.
     *
     * @param devices the number of devices the broadcast included.
     * @param utcTimestampMillis time the broadcast finished.
     */
    public BroadcastStats(int devices, long utcTimestampMillis) {
        this.utcTimestampMillis = utcTimestampMillis;
        this.devices = devices;
    }

    /**
     * Creates a new instance parsed from a String originally obtained by calling
     * {@link #toString()}.
     *
     * @param s the string.
     */
    public BroadcastStats(String s) {
        // Split the string at the pipe symbol
        String[] components = s.split("\\|");

        if (components.length != 2) {
            throw new IllegalArgumentException("String format incompatible: " + s);
        }

        utcTimestampMillis = Long.parseLong(components[0]);
        devices = Integer.parseInt(components[1]);
    }


    /**
     * Returns the timestamp of when the broadcast finished.
     */
    public long getUtcTimestampMillis() {
        return utcTimestampMillis;
    }

    /**
     * Returns the number of devices a message was broadcast to.
     */
    public int getDevices() {
        return devices;
    }


    @NonNull
    @Override
    public String toString() {
        return utcTimestampMillis + "|" + devices;
    }
}
