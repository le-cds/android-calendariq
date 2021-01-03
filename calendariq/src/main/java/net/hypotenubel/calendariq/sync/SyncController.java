package net.hypotenubel.calendariq.sync;

import android.content.Context;
import android.util.Log;

import net.hypotenubel.calendariq.data.Preferences;
import net.hypotenubel.calendariq.sync.synchroniser.Synchroniser;
import net.hypotenubel.calendariq.sync.worker.SyncWorkerController;
import net.hypotenubel.calendariq.util.IPrerequisitesChecker;
import net.hypotenubel.calendariq.util.Utilities;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * This class is responsible for starting and stopping our synchronisation services.
 */
public class SyncController {

    private static final String LOG_TAG = Utilities.logTag(SyncController.class);

    private final IPrerequisitesChecker prerequisitesChecker;
    private final Synchroniser synchroniser;
    private final Context appContext;

    @Inject
    public SyncController(@ApplicationContext Context appContext,
                          IPrerequisitesChecker prerequisitesChecker,
                          Synchroniser synchroniser) {
        this.appContext = appContext;
        this.prerequisitesChecker = prerequisitesChecker;
        this.synchroniser = synchroniser;
    }

    /**
     * Runs a single synchronisation attempt.
     */
    public void syncOnce() {
        if (prerequisitesChecker.arePrerequisitesMet(appContext)) {
            Log.d(LOG_TAG,"Synchronising once");
            new Thread(synchroniser).start();

        } else {
            Log.d(LOG_TAG,"Not synchronising once since prerequisites are not met");
        }
    }

    /**
     * Ensures that the sync services are running. If they are, no interval changes are applied.
     */
    public void ensureSyncServicesAreRunning() {
        controlSyncServices(false);
    }

    /**
     * Starts and stops our synchronisation services as configured in the preferences.
     */
    public void reconfigureSyncServices() {
        controlSyncServices(true);
    }

    private void controlSyncServices(boolean forceRestart) {
        boolean prerequisitesMet = prerequisitesChecker.arePrerequisitesMet(appContext);

        // TODO This will have to be adapted as soon as we support different sync services
        if (prerequisitesMet) {
            int interval = Preferences.INTERVAL.loadInt(appContext);
            SyncWorkerController.start(appContext, interval, forceRestart);
        } else {
            SyncWorkerController.stop(appContext);
        }
    }

}
