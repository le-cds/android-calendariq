package net.hypotenubel.calendariq.ui.main.checks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.util.IPrerequisitesChecker;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment which checks whether Garmin Connect is installed. If so, moves along. Otherwise,
 * displays an appropriate message to the user.
 */
@AndroidEntryPoint
public class CheckConnectIQFragment extends Fragment {

    @Inject
    IPrerequisitesChecker prerequisitesChecker;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle

    @Override
    public void onResume() {
        super.onResume();

        checkForGarminConnect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_check_connectiq, container, false);

        // Attach event listener explicitly since we're inside a fragment
        Button playStoreButton = view.findViewById(R.id.checkConnectIQFragment_playStoreButton);
        playStoreButton.setOnClickListener(this::onPlayStoreButtonClick);

        return view;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // UI Events

    public void onPlayStoreButtonClick(View view) {
        if (view.getId() == R.id.checkConnectIQFragment_playStoreButton) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="
                    + IPrerequisitesChecker.GARMIN_PACKAGE_ID));
            intent.setPackage("com.android.vending");
            startActivity(intent);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utilities

    /**
     * Checks whether Garmin Connect is installed. If so, navigates to the next fragment.
     */
    private void checkForGarminConnect() {
        View container = getActivity().findViewById(R.id.checkConnectIQFragment_container);

        if (prerequisitesChecker.isGarminConnectInstalled(getContext())) {
            container.setVisibility(View.INVISIBLE);
            Navigation.findNavController(getView()).navigate(
                    R.id.action_checkConnectIQFragment_to_checkCalendarPermFragment);

        } else {
            container.setVisibility(View.VISIBLE);
        }
    }

}