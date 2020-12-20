package net.hypotenubel.calendariq.ui.main.log;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.hypotenubel.calendariq.R;

/**
 * Shows the contents of our sync log. Not much going on here, really.
 */
public class LogFragment extends Fragment {

    public LogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.log_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView logView = view.findViewById(R.id.logFragment_logEntries);
        logView.setLayoutManager(new LinearLayoutManager(getContext()));

        LogAdapter logAdapter = new LogAdapter();
        logView.setAdapter(logAdapter);

        LogViewModel logViewModel = new ViewModelProvider(this).get(LogViewModel.class);
        logViewModel.getLogItems().observe(getViewLifecycleOwner(), logAdapter::submitList);
    }

}