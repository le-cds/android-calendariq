package net.hypotenubel.calendariq.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Operation;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.connectiq.BroadcastStats;
import net.hypotenubel.calendariq.services.WatchSyncWorker;
import net.hypotenubel.calendariq.util.Preferences;

/**
 * Presents the user with a list of settings. Since we only have a single list, we're not using
 * all the power and might of the preferences framework.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup the action bar
        setSupportActionBar(findViewById(R.id.settingsActivity_appBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    /**
     * Fragment that displays our list of settings.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {

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

            // Unreagister for preference change events
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
                BroadcastStats stats = new BroadcastStats(value);

                String summary = getContext().getResources().getQuantityString(
                        R.plurals.pref_last_sync_summary,
                        stats.getDevices(),
                        stats.getDevices(),
                        stats.getUtcTimestampMillis());
                lastSyncPreference.setSummary(summary);
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


    /**
     * Summary provider that works with a format string the preference value is passed to.
     */
    private static class FormattingSummaryProvider
            implements Preference.SummaryProvider<Preference> {

        /** The format string we'll be using to generate summaries. */
        private final String formatString;

        /**
         * Creates a new summary provider for the given format string.
         *
         * @param formatString the format string to use.
         */
        public FormattingSummaryProvider(String formatString) {
            this.formatString = formatString;
        }

        @Override
        public CharSequence provideSummary(Preference preference) {
            String prefValue = null;

            // Extract proper preference value
            if (preference instanceof ListPreference) {
                CharSequence prefValueSequence = ((ListPreference) preference).getEntry();
                if (prefValueSequence != null) {
                    prefValue = prefValueSequence.toString();
                }

            } else if (preference instanceof EditTextPreference) {
                prefValue = ((EditTextPreference) preference).getText();

            }

            // Return proper summary
            if (prefValue == null) {
                return "";
            } else {
                return String.format(formatString, prefValue);
            }
        }
    }

}
