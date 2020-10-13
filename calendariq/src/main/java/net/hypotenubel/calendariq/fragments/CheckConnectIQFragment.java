package net.hypotenubel.calendariq.fragments;

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
import net.hypotenubel.calendariq.util.Utilities;

/**
 * A fragment which checks whether Garmin Connect is installed. If so, moves along. Otherwise,
 * displays an appropriate message to the user.
 */
public class CheckConnectIQFragment extends Fragment {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Construction

    public CheckConnectIQFragment() {
        // Required empty public constructor
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle

    @Override
    public void onResume() {
        super.onResume();

        // Check whether ConnectIQ is installed now
        checkForConnectIQ();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
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
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.garmin.android.apps.connectmobile"));
            intent.setPackage("com.android.vending");
            startActivity(intent);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utilities

    /**
     * Checks whether ConnectIQ is installed. If so, navigates to the next fragment.
     */
    private void checkForConnectIQ() {
        boolean isConnectInstalled = Utilities.isConnectIQInstalled(this.getContext())
                || Utilities.isEmulator();
        View container = getActivity().findViewById(R.id.checkConnectIQFragment_container);

        if (isConnectInstalled) {
            container.setVisibility(View.INVISIBLE);
            Navigation.findNavController(getView()).navigate(
                    R.id.action_checkConnectIQFragment_to_checkCalendarPermFragment);

        } else {
            container.setVisibility(View.VISIBLE);
        }
    }

}