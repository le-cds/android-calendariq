package net.hypotenubel.calendariq.ui.pref;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;
import net.hypotenubel.calendariq.data.stats.source.IBroadcastStatisticsDao;
import net.hypotenubel.calendariq.sync.SyncController;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment that displays our list of settings.
 */
@AndroidEntryPoint
public class SettingsFragment extends PreferenceFragmentCompat {

    /** We'll use this to check for the most recent sync event. */
    @Inject
    IBroadcastStatisticsDao broadcastStatsDao;

    /** We'll use this to fire off manual synchronisations and update the service frequency. */
    @Inject
    SyncController syncController;

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Extract preferences
        Preference appointments = findPreference("appointments");
        Preference interval = findPreference("interval");
        Preference frequency = findPreference("frequency");
        lastSyncPreference = findPreference("last_sync");

        // Install summary providers
        appointments.setSummaryProvider(new FormattingSummaryProvider(getString(
                R.string.pref_appointments_summary)));
        interval.setSummaryProvider(new FormattingSummaryProvider(getString(
                R.string.pref_interval_summary)));
        frequency.setSummaryProvider(new FormattingSummaryProvider(getString(
                R.string.pref_frequency_summary)));

        // Obtain LiveData view on the most recent synchronization attempt and hook up an update
        // method as an observer
        broadcastStatsDao
                .getNewestLive(1)
                .observe(this, this::updateLastSyncSummary);

        // Listen to synchronisation requests
        lastSyncPreference.setOnPreferenceClickListener(preference -> {
            runSyncWorkerOnce();
            return true;
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        syncController.reconfigureSyncServices();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Last Synced

    /**
     * Permanent handle to the last sync preference. We'll update its summary from time to time.
     */
    private Preference lastSyncPreference;

    /**
     * Provides a summary for the last synchronisation. This is more complex, so we handle it in
     * this rather special method.
     */
    private void updateLastSyncSummary(List<BroadcastStatistics> newestStatList) {
        if (newestStatList == null || newestStatList.size() != 1) {
            lastSyncPreference.setSummary(null);
        } else {
            BroadcastStatistics newestStat = newestStatList.get(0);
            if (newestStat.getMessage() == null) {
                String summary = getContext().getResources().getQuantityString(
                        R.plurals.pref_last_sync_summary,
                        newestStat.getContactedApps(),
                        newestStat.getContactedApps(),
                        newestStat.getUtcTimestampMillis());
                lastSyncPreference.setSummary(summary);
            } else {
                lastSyncPreference.setSummary(newestStat.getMessage());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Sync Now

    /**
     * Ensures that our synchronization worker is run once by the work manager API.
     */
    private void runSyncWorkerOnce() {
        syncController.syncOnce();

        Toast.makeText(
                SettingsFragment.this.getActivity(),
                getString(R.string.settingsActivity_sending),
                Toast.LENGTH_SHORT)
                .show();
    }
}
