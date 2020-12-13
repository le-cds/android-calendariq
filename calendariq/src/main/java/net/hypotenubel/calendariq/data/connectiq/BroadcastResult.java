package net.hypotenubel.calendariq.data.connectiq;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

/**
 * Results of a broadcast attempt through {@link ConnectIQAppBroadcaster}. Each instance can be
 * turned into a String and parsed from a string. That way, the result can be persisted as a
 * preference to be shown in a "Last synced" kind of preference thingy. Create new instances through
 * the static methods.
 *
 * <p>A broadcast result can be in one of two states: success or error. The state determines the
 * associated information.</p>
 */
public final class BroadcastResult {

    /**
     * Enumeration of the possible result states. ProGuard must keep this class in tact because
     * we'll be serializing and deserializing the enumeration constant names.
     */
    @Keep
    public enum State {

        /**
         * Indicates a successful broadcast. The result will provide the number of synchronised apps
         * and the synchronisation timestamp.
         */
        SUCCESS,
        /**
         * Indicates a failed broadcast. The result will contain some kind of error message and the
         * synchronisation timestamp.
         */
        FAILURE;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State

    /** The result state. */
    private final State state;
    /** Time when the broadcast finished. */
    private final long utcTimestampMillis;
    /** Number of apps the broadcast included. */
    private final int apps;
    /** The message associated with error states. */
    private final String message;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Creation

    private BroadcastResult(State state, long timestamp, int apps, String message) {
        this.state = state;
        this.utcTimestampMillis = System.currentTimeMillis();
        this.apps = apps;
        this.message = message;
    }

    /**
     * Create a result that represents successful broadcast to the given number of apps at the
     * current system time.
     */
    public static BroadcastResult success(int apps) {
        return new BroadcastResult(State.SUCCESS, System.currentTimeMillis(), apps, null);
    }

    /**
     * Create a result that represents a broadcast attempt that failed for the given reason at the
     * current system time.
     */
    public static BroadcastResult failure(String message) {
        return new BroadcastResult(State.FAILURE, System.currentTimeMillis(), 0, message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Serialization / Deserialization

    /**
     * Deserializes an instance from the given string representation created by {@link #toString()}.
     */
    public static BroadcastResult deserialize(String serialized) {
        // Split the string at the pipe symbol
        String[] components = serialized.split("\\|");

        if (components.length != 3) {
            throw new IllegalArgumentException("String format incompatible: " + serialized);
        }

        try {
            State state = State.valueOf(components[0]);
            long timestamp = Long.parseLong(components[1]);
            int apps = 0;
            String message = null;

            switch (state) {
                case SUCCESS:
                    apps = Integer.parseInt(components[2]);
                    break;

                case FAILURE:
                    message = components[2];
                    break;
            }

            return new BroadcastResult(state, timestamp, apps, message);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to deserialize: " + serialized);
        }
    }

    @NonNull
    @Override
    public String toString() {
        switch (state) {
            case SUCCESS:
                return state.name() + "|" + utcTimestampMillis + "|" + apps;
            case FAILURE:
                return state.name() + "|" + utcTimestampMillis + "|" + message;
            default:
                // Should not happen
                return "";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters

    /**
     * Whether the broadcast was successful.
     */
    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    /**
     * Returns the timestamp of when the broadcast finished.
     */
    public long getUtcTimestampMillis() {
        return utcTimestampMillis;
    }

    /**
     * Returns the number of apps a message was broadcast to.
     */
    public int getApps() {
        return apps;
    }

    /**
     * If a broadcast failed, this will return a message explaining the failure.
     */
    public String getMessage() {
        return message;
    }

}
