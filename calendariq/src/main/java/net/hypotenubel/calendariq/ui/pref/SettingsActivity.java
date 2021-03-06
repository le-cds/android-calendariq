package net.hypotenubel.calendariq.ui.pref;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.hypotenubel.calendariq.R;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Presents the user with a list of settings. Since we only have a single list, we're not using
 * all the power and might of the preferences framework.
 */
@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup the action bar
        setSupportActionBar(findViewById(R.id.settingsActivity_appBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}
