package net.hypotenubel.calendariq.ui.pref;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Operation;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.data.Preferences;
import net.hypotenubel.calendariq.data.connectiq.BroadcastResult;
import net.hypotenubel.calendariq.data.service.WatchSyncWorker;

/**
 * Fragment that displays our list of settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

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

        // Listen to frequency changes to we can restart the sync worker
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

    @Override
    public void onResume() {
        super.onResume();

        // Update last sync time
        updateLastSyncSummary();

        // Register for preference change events
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister for preference change events
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Last Synced

    /**
     * Permanent handle to the last sync preference. We'll update its summary from time to time.
     */
    private Preference lastSyncPreference;

    /**
     * Listens for changes to shared preferences. This is basically only there to check whether
     * the last sync time has changed and to update the preference's summary accordingly.
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener
            = (sharedPreferences, key) -> {
                if (key.equals(Preferences.LAST_SYNC.getKey())) {
                    updateLastSyncSummary();
                }
            };

    /**
     * Provides a summary for the last synchronisation. This is more complex, so we handle it in
     * this rather special method.
     */
    private void updateLastSyncSummary() {
        // Not sure how to obtain the preference value except for actually looking it up in the
        // shared preferences
        String value = Preferences.LAST_SYNC.loadString(getContext());

        if (value == null || value.equals("")) {
            lastSyncPreference.setSummary(getString(R.string.pref_last_sync_summary_never));
        } else {
            // Turn the string into stats and put them into our string
            BroadcastResult stats = BroadcastResult.deserialize(value);

            if (stats.isSuccess()) {
                String summary = getContext().getResources().getQuantityString(
                        R.plurals.pref_last_sync_summary,
                        stats.getApps(),
                        stats.getApps(),
                        stats.getUtcTimestampMillis());
                lastSyncPreference.setSummary(summary);
            } else {
                lastSyncPreference.setSummary(stats.getMessage());
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
