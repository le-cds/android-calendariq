package net.hypotenubel.calendariq.ui.main.checks;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.util.Utilities;

/**
 * A fragment which checks whether we have calendar access permissions or not. If so, moves along.
 * Otherwise, displays an appropriate message to the user.
 */
public class CheckCalendarPermFragment extends Fragment {

    /** The launcher we use to start permission requests. */
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::permissionResult);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Construction

    public CheckCalendarPermFragment() {
        // Required empty public constructor
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_check_calendarperm, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Take the opportunity to check whether we have calendar permissions
        checkForCalendarPermissions();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utilities

    /**
     * Ensures that we have permission to read calendars. Without this, our app would be
     * impressively useless.
     */
    private void checkForCalendarPermissions() {
        boolean calendarPermissions = Utilities.checkCalendarPermission(getContext());

        if (calendarPermissions) {
            proceedToNextScreen();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR);
        }
    }

    private void proceedToNextScreen() {
        Navigation.findNavController(getView()).navigate(
                R.id.action_checkCalendarPermFragment_to_calendarListFragment);
        requestPermissionLauncher.unregister();
    }

    public void permissionResult(boolean isGranted) {
        View container = getActivity().findViewById(R.id.checkCalendarPermFragment_container);
        if (isGranted) {
            container.setVisibility(View.INVISIBLE);
            proceedToNextScreen();
        } else {
            container.setVisibility(View.VISIBLE);
        }
    }

}