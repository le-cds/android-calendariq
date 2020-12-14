package net.hypotenubel.calendariq.data.model.stats;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import net.hypotenubel.calendariq.data.connectiq.ConnectIQAppBroadcaster;

/**
 * Results of a broadcast attempt through {@link ConnectIQAppBroadcaster}. Each instance can be
 * turned into a String and parsed from a string. That way, the result can be persisted as a
 * preference to be shown in a "Last synced" kind of preference thingy, if required. Instances of
 * this class can also be used with the Room library to be persisted in a database.
 *
 * <p>A broadcast statistic can be in one of two states: success or error. The state determines the
 * associated information.</p>
 */
@Entity
public final class BroadcastStatistics {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State

    /** Time when the broadcast finished. */
    @PrimaryKey
    private final long utcTimestampMillis;
    /** Number of apps that we wanted to broadcast to. */
    private final int totalApps;
    /** Number of apps that were successfully contacted. */
    private final int contactedApps;
    /** The message associated with error states. */
    private final String message;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Creation

    /**
     * Create a new instance that represents the given data. The message is only necessary if not
     * all apps could be reached.
     */
    public BroadcastStatistics(long utcTimestampMillis, int totalApps, int contactedApps,
                               String message) {
        this.utcTimestampMillis = utcTimestampMillis;
        this.totalApps = totalApps;
        this.contactedApps = contactedApps;
        this.message = message;
    }

    /**
     * Create a result that represents successful broadcast to the given number of apps at the
     * current system time.
     */
    public static BroadcastStatistics success(int apps) {
        return new BroadcastStatistics(System.currentTimeMillis(), apps, apps, null);
    }

    /**
     * Create a result that represents a broadcast attempt that failed for the given reason at the
     * current system time.
     */
    public static BroadcastStatistics failure(int totalApps, int contactedApps, String message) {
        return new BroadcastStatistics(
                System.currentTimeMillis(), totalApps, contactedApps, message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Serialization / Deserialization

    /**
     * Deserializes an instance from the given string representation created by {@link #toString()}.
     */
    public static BroadcastStatistics deserialize(String serialized) {
        // Split the string at the pipe symbol
        String[] components = serialized.split("\\|", 4);

        try {
            long timestamp = Long.parseLong(components[0]);
            int totalApps = Integer.parseInt(components[1]);
            int contactedApps = Integer.parseInt(components[2]);

            String message = components[3];
            if (message.length() == 0) {
                message = null;
            }

            return new BroadcastStatistics(timestamp, totalApps, contactedApps, message);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to deserialize: " + serialized);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return utcTimestampMillis + "|" + totalApps + "|" + contactedApps + "|"
                + (message == null ? "" : message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters

    /**
     * Returns the timestamp of when the broadcast finished.
     */
    public long getUtcTimestampMillis() {
        return utcTimestampMillis;
    }

    /**
     * The total number of apps discovered by the broadcaster.
     */
    public int getTotalApps() {
        return totalApps;
    }

    /**
     * Returns the number of apps that the message was successfully broadcast to.
     */
    public int getContactedApps() {
        return contactedApps;
    }

    /**
     * If a broadcast failed, this will return a message explaining the failure.
     */
    public String getMessage() {
        return message;
    }

}
