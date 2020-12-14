package net.hypotenubel.calendariq.data.model.msg;

import android.content.Context;

import net.hypotenubel.calendariq.data.Preferences;

import java.util.List;

/**
 * Represents the interval between successive sync attempts in minutes.
 */
public class SyncIntervalConnectMessagePart implements IConnectMessagePart {

    /** Whether the battery is being charged or not. */
    private final int interval;

    /**
     * Creates a new instance initialized from the preferences.
     */
    public static SyncIntervalConnectMessagePart fromPreferences(Context context) {
        return new SyncIntervalConnectMessagePart(Preferences.FREQUENCY.loadInt(context));
    }

    /**
     * Creates a new instance with the given interval.
     */
    public SyncIntervalConnectMessagePart(int interval) {
        this.interval = interval;
    }

    @Override
    public void encodeAndAppend(List<Object> target) {
        target.add(interval);
    }

}
