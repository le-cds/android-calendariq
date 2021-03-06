package net.hypotenubel.calendariq.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import javax.inject.Inject;

public class DevicePrerequisitesChecker implements IPrerequisitesChecker {

    @Inject
    public DevicePrerequisitesChecker() {
        // Make injectable
    }

    @Override
    public boolean isGarminConnectInstalled(Context context) {
        try {
            // Try to find the app, which must also correspond to a minimum version (see ConnectIQ
            // mobile SDK code)
            PackageInfo info = context.getPackageManager().getPackageInfo(GARMIN_PACKAGE_ID, 0);
            return info.versionCode >= 2000;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isCalendarAccessible(Context context) {
        int permissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

}
