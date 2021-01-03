package net.hypotenubel.calendariq.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import javax.inject.Inject;

public class EmulatorPrerequisitesChecker implements IPrerequisitesChecker {

    @Inject
    public EmulatorPrerequisitesChecker() {
        // Make injectable
    }

    @Override
    public boolean isGarminConnectInstalled(Context context) {
        return true;
    }

    @Override
    public boolean isCalendarAccessible(Context context) {
        int permissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

}
