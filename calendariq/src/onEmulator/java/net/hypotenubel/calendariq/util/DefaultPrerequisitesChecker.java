package net.hypotenubel.calendariq.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import javax.inject.Inject;

// TODO Change this class name as soon as this is dependency-injected everywhere.
//      Once that is done we could have the different IPrerequisitesChecker live
//      in the main source set again and only extract the DI modules to different
//      source sets.
public class DefaultPrerequisitesChecker implements IPrerequisitesChecker {

    @Inject
    public DefaultPrerequisitesChecker() {
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
