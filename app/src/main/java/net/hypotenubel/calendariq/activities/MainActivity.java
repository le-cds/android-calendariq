package net.hypotenubel.calendariq.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.services.WatchSyncWorker;
import net.hypotenubel.calendariq.util.Preferences;
import net.hypotenubel.calendariq.util.Utilities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    /** Log tag used to log log messages in a logging fashion. */
    private static final String LOG_TAG = Utilities.logTag(MainActivity.class);

    /** Constant that identifies our permission request. */
    private static final int PERMISSION_REQUEST_READ_CALENDAR = 0;
    /** Package ID of the Garmin ConnectIQ app. Used to ensure its existence on the phone. */
    private static final String GARMIN_PACKAGE_ID = "com.garmin.android.apps.connectmobile";

    /** Whether the Garmin Connect app is installed. */
    private boolean garminInstalled = false;

    /** The label that displays the description text. */
    private TextView descriptionView;
    /** Our calendar list view. */
    private RecyclerView calendarView;
    /** View model for our calendars. */
    private CalendarViewModel calendarViewModel;
    /** Adapter feeding the calendar list. */
    private CalendarAdapter calendarAdapter;
    /** Layout for the calendar list. */
    private LinearLayoutManager calendarLayoutManager;


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // UI Events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the view and our action bar
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.mainActivity_appBar));

        descriptionView = findViewById(R.id.mainActivity_description);

        // Check for emulator
        boolean isEmulator = Utilities.isEmulator();

        // Ensure that everything is set up as expected
        if (!isConnectIQInstalled() && !isEmulator) {
            showConnectIQError();
            return;
        }

        if (ensurePermissions()) {
            initCalendarView();

            // Ensure that our sync service is running
            if (!isEmulator) {
                WatchSyncWorker.runSyncWorker(
                        Preferences.FREQUENCY.loadInt(this),
                        false);
            }
        } else {
            showPermissionsError();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (calendarViewModel != null) {
            calendarViewModel.storeActiveCalendarIds();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        // Show calendar-related items if we have calendar permissions
        if (calendarViewModel == null) {
            menu.findItem(R.id.mainActivity_menu_requestPermissions).setVisible(garminInstalled);
        } else {
            menu.findItem(R.id.mainActivity_menu_requestPermissions).setVisible(false);
            menu.findItem(R.id.mainActivity_menu_refresh).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainActivity_menu_requestPermissions:
                if (ensurePermissions()) {
                    initCalendarView();
                }
                return true;

            case R.id.mainActivity_menu_refresh:
                // This will automatically cause our list to update
                calendarViewModel.refresh();
                return true;

            case R.id.mainActivity_menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // UI Initialization

    /**
     * Configures the calendar view.
     */
    private void initCalendarView() {
        calendarView = findViewById(R.id.mainActivity_calendars);

        // Instantiate our view model
        calendarViewModel = new CalendarViewModel(getApplication());

        // Setup layout
        calendarLayoutManager = new LinearLayoutManager(this);
        calendarView.setLayoutManager(calendarLayoutManager);

        // Setup adapter
        calendarAdapter = new CalendarAdapter(this, calendarViewModel);
        calendarView.setAdapter(calendarAdapter);

        // Show the calendar-related menu items in the action bar as well as the correct description
        descriptionView.setText(R.string.mainActivity_description_success);
        invalidateOptionsMenu();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ConnectIQ

    /**
     * Ensures that ConnectIQ is installed.
     */
    private boolean isConnectIQInstalled() {
        try {
            // Try to find the app, which must also correspond to a minimum version (see ConnectIQ
            // mobile SDK code)
            PackageInfo info = getPackageManager().getPackageInfo(GARMIN_PACKAGE_ID, 0);
            return info.versionCode >= 2000;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showConnectIQError() {
        descriptionView.setText(R.string.mainActivity_description_connectIQError);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Permission Handling

    /**
     * Ensures that we have permission to read calendars. Without this, our app would be
     * impressively useless.
     */
    private boolean ensurePermissions() {
        if (Utilities.ensureCalendarPermission(this)) {
            return true;
        } else {
            // Ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_CALENDAR},
                    PERMISSION_REQUEST_READ_CALENDAR);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_CALENDAR) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Load our calendars
                initCalendarView();
            }
        }
    }

    private void showPermissionsError() {
        descriptionView.setText(R.string.mainActivity_description_permissionsError);
    }

}
