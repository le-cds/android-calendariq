package net.hypotenubel.calendariq.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.activities.SettingsActivity;
import net.hypotenubel.calendariq.services.WatchSyncWorker;
import net.hypotenubel.calendariq.util.Preferences;
import net.hypotenubel.calendariq.util.Utilities;

/**
 * Our main fragment which shows a list of calendars that can be activated and deactivated.
 */
public class CalendarListFragment extends Fragment {

    /** View model for our calendars. */
    private CalendarViewModel calendarViewModel;


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Creation

    public CalendarListFragment() {
        // Required empty public constructor
    }

    public static CalendarListFragment newInstance() {
        return new CalendarListFragment();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We have stuff to put into the options menu!
        setHasOptionsMenu(true);

        // Take this opportunity to ensure that our sync service is working (unless we're running
        // in the emulator)
        if (Utilities.checkCalendarPermission(getContext()) && !Utilities.isEmulator()) {
            WatchSyncWorker.runSyncWorker(
                    Preferences.FREQUENCY.loadInt(getContext()),
                    false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView calendarView = view.findViewById(R.id.calendarListFragment_calendars);
        calendarViewModel = new CalendarViewModel(getActivity().getApplication());

        // Setup layout
        LinearLayoutManager calendarLayoutManager = new LinearLayoutManager(getContext());
        calendarView.setLayoutManager(calendarLayoutManager);

        // Setup adapter
        CalendarAdapter calendarAdapter = new CalendarAdapter(this, calendarViewModel);
        calendarView.setAdapter(calendarAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_calendar_list, menu);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (calendarViewModel != null) {
            calendarViewModel.storeActiveCalendarIds();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // UI Events

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_calendar_list_menu_refresh:
                // This will automatically cause our list to update
                calendarViewModel.refresh();
                return true;

            case R.id.fragment_calendar_list_menu_settings:
                startActivity(new Intent(this.getActivity(), SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}