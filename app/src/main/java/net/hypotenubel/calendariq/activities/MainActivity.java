package net.hypotenubel.calendariq.acitivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.calendar.CalendarDescriptor;
import net.hypotenubel.calendariq.calendar.ICalendarInterface;
import net.hypotenubel.calendariq.services.WatchSyncService;
import net.hypotenubel.calendariq.services.WatchSyncWorker;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    // TODO Load calendar data in another thread?
    // TODO Make calendar data a view model with live data objects?

    /** Log tag used to log log messages in a logging fashion. */
    private static final String LOG_TAG = Utilities.logTag(MainActivity.class);

    /** Synchronization interval in minutes. */
    private static final int SYNC_INTERVAL = 15;

    /** Constant that identifies our permission request. */
    private static final int PERMISSION_REQUEST_READ_CALENDAR = 0;
    /** Package ID of the Garmin ConnectIQ app. Used to ensure its existence on the phone. */
    private static final String GARMIN_PACKAGE_ID = "com.garmin.android.apps.connectmobile";

    /** Whether the Garmin Connect app is installed. */
    private boolean garminInstalled = false;

    /** The list of calendars. */
    private List<CalendarDescriptor> calendars;

    /** The label that displays the description text. */
    private TextView descriptionView;
    /** Our calendar list view. */
    private RecyclerView calendarView;
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
                runSyncWorker();
//                runSyncService();
            }
        } else {
            showPermissionsError();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (calendarAdapter != null) {
            calendarAdapter.saveActiveCalendarIds();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        // Show calendar-related items if we have calendar permissions
        if (calendars == null) {
            menu.findItem(R.id.mainActivity_menu_requestPermissions).setVisible(garminInstalled);
        } else {
            menu.findItem(R.id.mainActivity_menu_requestPermissions).setVisible(false);
            menu.findItem(R.id.mainActivity_menu_refresh).setVisible(true);
            menu.findItem(R.id.mainActivity_menu_sync).setVisible(true);
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
                loadCalendars();
                calendarAdapter.updateList(calendars);
                return true;

            case R.id.mainActivity_menu_sync:
                runSyncWorkerOnce();
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

        // Setup layout
        calendarLayoutManager = new LinearLayoutManager(this);
        calendarView.setLayoutManager(calendarLayoutManager);

        // Setup adapter
        loadCalendars();
        calendarAdapter = new CalendarAdapter(
                calendars,
                Utilities.obtainSharedPreferences(this));
        calendarView.setAdapter(calendarAdapter);

        // Show the calendar-related menu items in the action bar as well as the correct description
        descriptionView.setText(R.string.mainActivity_description_success);
        invalidateOptionsMenu();
    }

    /**
     * Populates the {@link #calendars} field.
     */
    private void loadCalendars() {
        ICalendarInterface provider = Utilities.obtainCalendarProvider(this);
        calendars = provider.getAvailableCalendars();
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
    // Services

    /** ID of the work item we're using to run our worker. */
    private String SYNC_WORK_NAME = "sync_devices";

    /**
     * Ensures that our synchronization worker is run by the work manager API.
     */
    private void runSyncWorker() {
        // Build a new periodic work request and register it if none was already registered
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                        WatchSyncWorker.class,
                        SYNC_INTERVAL,
                        TimeUnit.MINUTES)
                .build();

        WorkManager
                .getInstance()
                .enqueueUniquePeriodicWork(
                        SYNC_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request);
    }

    /**
     * Ensures that our synchronization worker is run once by the work manager API.
     */
    private void runSyncWorkerOnce() {
        // Build a new periodic work request and register it if none was already registered
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WatchSyncWorker.class).build();
        Operation operation = WorkManager
                .getInstance()
                .enqueueUniqueWork(
                        SYNC_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        request);

        operation.getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                // This is only called if things were successful
                Toast.makeText(
                            MainActivity.this,
                            "Appointments sent",
                            Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Runs our synchronization service.
     */
    private void runSyncService() {
        startService(new Intent(this, WatchSyncService.class));
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
