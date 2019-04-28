package net.hypotenubel.calendariq.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.Operation;

import android.os.Bundle;
import android.widget.Toast;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.services.WatchSyncWorker;

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
        setSupportActionBar((Toolbar) findViewById(R.id.settingsActivity_appBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    /**
     * Ensures that our synchronization worker is run once by the work manager API.
     */
    private void runSyncWorkerOnce() {
        Operation syncOperation = WatchSyncWorker.runSyncWorkerOnce();

        // Display a toast when the operation finishes
        syncOperation.getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                // This is only called if things were successful
                Toast.makeText(
                        SettingsActivity.this,
                        "Appointments sent",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }
    }

}
