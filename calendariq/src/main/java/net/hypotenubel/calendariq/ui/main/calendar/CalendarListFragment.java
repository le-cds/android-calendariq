package net.hypotenubel.calendariq.ui.main.calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.data.Preferences;
import net.hypotenubel.calendariq.data.service.WatchSyncWorker;
import net.hypotenubel.calendariq.ui.pref.SettingsActivity;
import net.hypotenubel.calendariq.util.Utilities;

/**
 * Our main fragment which shows a list of calendars that can be activated and deactivated.
 */
public class CalendarListFragment extends Fragment {

    /** View model for our calendars. */
    private CalendarViewModel calendarViewModel;

    private SwipeRefreshLayout swipeContainer;


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
        Context context = getContext();
        if (Utilities.checkCalendarPermission(context) && !Utilities.isEmulator()) {
            WatchSyncWorker.runSyncWorker(
                    context.getApplicationContext(),
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
        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        // Setup layout
        LinearLayoutManager calendarLayoutManager = new LinearLayoutManager(getContext());
        calendarView.setLayoutManager(calendarLayoutManager);

        // Setup adapter
        CalendarAdapter calendarAdapter = new CalendarAdapter(this, calendarViewModel);
        calendarView.setAdapter(calendarAdapter);

        // Setup swipe refresh
        swipeContainer = view.findViewById(R.id.calendarListFragment_swipeContainer);
        swipeContainer.setOnRefreshListener(this::refreshViewModel);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
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
        if (item.getItemId() == R.id.calendar_list_fragment_menu_refresh) {
            // This will automatically cause our list to update
            swipeContainer.setRefreshing(true);
            refreshViewModel();
            return true;

        } else if (item.getItemId() == R.id.calendar_list_fragment_menu_log) {
            Navigation.findNavController(getView()).navigate(
                    R.id.action_calendarListFragment_to_logFragment);
            return true;

        } else if (item.getItemId() == R.id.calendar_list_fragment_menu_settings) {
            startActivity(new Intent(this.getActivity(), SettingsActivity.class));
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void refreshViewModel() {
        calendarViewModel.refresh();
        swipeContainer.setRefreshing(false);
    }

}