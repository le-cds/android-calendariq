package net.hypotenubel.calendariq.ui.pref;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.Operation;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.data.Preferences;
import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;
import net.hypotenubel.calendariq.data.stats.source.IBroadcastStatisticsDao;
import net.hypotenubel.calendariq.sync.WatchSyncWorker;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment that displays our list of settings.
 */
@AndroidEntryPoint
public class SettingsFragment extends PreferenceFragmentCompat {

    // TODO Thhings to inject:
    //      - Thing that controls our sync services

    /** We'll use this to check for the most recent sync event. */
    @Inject IBroadcastStatisticsDao broadcastStatsDao;

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

        // Listen to frequency changes to we can restart the sync worker
        // TODO This should be done when the fragment is closed
        frequency.setOnPreferenceChangeListener((preference, newValue) -> {
            onFrequencyChanged(newValue.toString());
            return true;
        });

        // Listen to synchronisation requests
        lastSyncPreference.setOnPreferenceClickListener(preference -> {
            runSyncWorkerOnce();
            return true;
        });
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
    // Sync Worker Updates

    /**
     * Re-register sync worker upon frequency changes.
     */
    private void onFrequencyChanged(String newValue) {
        WatchSyncWorker.runSyncWorker(
                getContext().getApplicationContext(),
                Preferences.FREQUENCY.loadInt(this.getContext()),
                true);
    }

    /**
     * Ensures that our synchronization worker is run once by the work manager API.
     */
    private void runSyncWorkerOnce() {
        Operation syncOperation = WatchSyncWorker.runSyncWorkerOnce(
                getContext().getApplicationContext());

        // Display a toast when the operation finishes
        syncOperation.getState().observe(this, state -> {
            // This is only called if things were successful
            Toast.makeText(
                    SettingsFragment.this.getActivity(),
                    getString(R.string.settingsActivity_sending),
                    Toast.LENGTH_SHORT)
                    .show();
        });
    }
}
