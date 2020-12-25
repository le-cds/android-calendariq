package net.hypotenubel.calendariq.data.msg.model;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.util.List;

/**
 * Represents the device's battery charge at the time of creation.
 */
public class BatteryChargeConnectMessagePart implements IConnectMessagePart {

    /** Whether the battery is being charged or not. */
    private final boolean isCharging;
    /** Battery charge, between 0 and 100. */
    private final int chargePercentage;

    /**
     * Creates a new instance initialized with the device's current state.
     */
    public static BatteryChargeConnectMessagePart fromCurrentDeviceState(Context context) {
        // See https://developer.android.com/training/monitoring-device-state/battery-monitoring
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int chargePercentage = (int) Math.round(level * 100 / (double) scale);

        return new BatteryChargeConnectMessagePart(isCharging, chargePercentage);
    }

    /**
     * Creates a new instance with the given data.
     */
    public BatteryChargeConnectMessagePart(boolean isCharging, int chargePercentage) {
        if (chargePercentage < 0 || chargePercentage > 100) {
            throw new IllegalArgumentException("Battery charge was " + chargePercentage);
        }

        this.isCharging = isCharging;
        this.chargePercentage = chargePercentage;
    }

    @Override
    public void encodeAndAppend(List<Object> target) {
        // We send a positive integer if we're not charging, and a negative one if we are
        target.add(isCharging ? -chargePercentage : chargePercentage);
    }

}
