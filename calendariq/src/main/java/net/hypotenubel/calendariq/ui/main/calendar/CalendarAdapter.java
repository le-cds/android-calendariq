package net.hypotenubel.calendariq.ui.main.calendar;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.data.model.calendar.CalendarDescriptor;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts a {@link CalendarViewModel} for a recycler view. The adapter internally works on a
 * complete copy of the list of calendars since we expect that there won't be that many. It
 * refreshes itself automatically if the view model's list of calendars is refreshed.
 */
public class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** Log tag used to log log messages in a logging fashion. */
    private static final String LOG_TAG = Utilities.logTag(CalendarAdapter.class);

    /** View type for displaying account names. */
    private static final int VIEW_TYPE_ACCOUNT = 1;
    /** View type for displaying calendars. */
    private static final int VIEW_TYPE_CALENDAR = 2;

    /** The calendars we're adapting. */
    private final List<Object> calendars = new ArrayList<>();


    /**
     * Creates a new instance owned by the given lifecycle owner to display the given view model.
     */
    public CalendarAdapter(LifecycleOwner owner, CalendarViewModel viewModel) {
        viewModel.getCalendars().observe(owner, this::updateList);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization

    /**
     * Updates the list of calendars and refreshes our view.
     */
    private void updateList(List<CalendarDescriptor> newCalendars) {
        // We'll rebuild our list in a moment
        calendars.clear();

        // Add calendars to our list, but insert account name headers along the way
        String currAccountName = null;
        for (CalendarDescriptor descriptor : newCalendars) {
            if (currAccountName == null || !currAccountName.equals(descriptor.getAccName())) {
                currAccountName = descriptor.getAccName();
                calendars.add(currAccountName);
            }

            calendars.add(descriptor);
        }

        notifyDataSetChanged();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Adapter

    @Override
    public int getItemCount() {
        return calendars.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = calendars.get(position);
        if (o instanceof CalendarDescriptor) {
            return VIEW_TYPE_CALENDAR;
        } else if (o instanceof String) {
            return VIEW_TYPE_ACCOUNT;
        } else {
            throw new IllegalStateException();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_ACCOUNT) {
            return new AccountViewHolder(inflater.inflate(
                    R.layout.calendar_list_account_view_item, parent, false));
        } else if (viewType == VIEW_TYPE_CALENDAR) {
            return new CalendarViewHolder(inflater.inflate(
                    R.layout.calendar_list_calendar_view_item, parent, false));
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Object o = calendars.get(i);

        if (o instanceof CalendarDescriptor) {
            CalendarDescriptor descriptor = (CalendarDescriptor) o;
            CalendarViewHolder calViewHolder = (CalendarViewHolder) viewHolder;

            // Xor colour value with this value to make the colour solid as opposed to translucent
            calViewHolder.colorShape.setColor(descriptor.getColour() | 0xFF000000);
            calViewHolder.calendarNameView.setText(descriptor.getCalName());
            calViewHolder.activeSwitch.setChecked(descriptor.isActive());

        } else if (o instanceof String) {
            String account = (String) o;
            AccountViewHolder accViewHolder = (AccountViewHolder) viewHolder;

            accViewHolder.accountNameView.setText(account);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // View Holders

    /**
     * Keeps references to the most important layout elements of an account view.
     */
    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        private final TextView accountNameView;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);

            // Extract relevant views
            accountNameView = itemView.findViewById(R.id.accountViewItem_accountName);
        }
    }


    /**
     * Keeps references to the most important layout elements of a calendar view. Not static since
     * it references the list of calendars.
     */
    public class CalendarViewHolder extends RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener {

        private final GradientDrawable colorShape;
        private final TextView calendarNameView;
        private final SwitchMaterial activeSwitch;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);

            // Extract relevant views
            calendarNameView = itemView.findViewById(R.id.calendarViewItem_calendarName);
            activeSwitch = itemView.findViewById(R.id.calendarViewItem_calendarActiveSwitch);

            // The shape is a bit more complicated
            ImageView shapeView = itemView.findViewById(R.id.calendarViewItem_calendarColor);
            colorShape = (GradientDrawable) shapeView.getDrawable();

            // We listen for stuff to happen
            activeSwitch.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // Retrieve the calendar we're representing right now
            CalendarDescriptor calendar = (CalendarDescriptor) calendars.get(getBindingAdapterPosition());
            calendar.setActive(isChecked);
        }
    }
}
