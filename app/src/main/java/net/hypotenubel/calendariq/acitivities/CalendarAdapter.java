package net.hypotenubel.calendariq.acitivities;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.calendar.CalendarDescriptor;
import net.hypotenubel.calendariq.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapts a list of {@link CalendarDescriptor} instances for a recycler view. The adapter internally
 * works on a copy of the list, so changes to the list passed to its constructor won't be reflected
 * in any way. Since each calendar has an associated switch, this adapter also listens for changes
 * to the switch's checked state. To save the state, call {@link #saveActiveCalendarIds()}.
 */
public class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** Log tag used to log log messages in a logging fashion. */
    private static final String LOG_TAG = Utilities.logTag(CalendarAdapter.class);

    /** View type for displaying account names. */
    private static final int VIEW_TYPE_ACCOUNT = 1;
    /** View type for displaying calendars. */
    private static final int VIEW_TYPE_CALENDAR = 2;

    /** Shared preferences to load and store active calendars. */
    private final SharedPreferences preferences;
    /** The calendars we're adapting. */
    private final List<Object> calendars = new ArrayList<>();
    /** IDs of those calendars that are marked as being active for easy lookup. */
    private final Set<Integer> activeCalendarIds = new HashSet<>();


    /**
     * Creates a new instance for the given calendars.
     */
    public CalendarAdapter(List<CalendarDescriptor> calendars, SharedPreferences preferences) {
        this.preferences = preferences;
        loadActiveCalendarIds();

        updateList(calendars, false);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization

    /**
     * Updates the list of calendars and refreshes our view.
     */
    public void updateList(List<CalendarDescriptor> calendars) {
        updateList(calendars, true);
    }

    /**
     * Updates the list of calendars and optionally refreshes our view.
     */
    private void updateList(List<CalendarDescriptor> calendars, boolean notifyView) {
        // Ensure the original list is sorted
        Collections.sort(calendars);

        // Add account name headers and collect calendar IDs on the way
        this.calendars.clear();

        Set<Integer> existingCalendarIds = new HashSet<>();

        String currAccountName = null;
        for (CalendarDescriptor descriptor : calendars) {
            if (currAccountName == null || !currAccountName.equals(descriptor.getAccName())) {
                currAccountName = descriptor.getAccName();
                this.calendars.add(currAccountName);
            }

            this.calendars.add(descriptor);
            existingCalendarIds.add(descriptor.getId());
        }

        // Ensure that the active calendar IDs only contain IDs of existing calendars
        activeCalendarIds.retainAll(existingCalendarIds);

        if (notifyView) {
            notifyDataSetChanged();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Update Preferences

    /**
     * Populates our set of active calendar IDs.
     */
    private void loadActiveCalendarIds() {
        activeCalendarIds.addAll(Utilities.loadActiveCalendarIds(preferences));
    }

    /**
     * Saves the set of active calendar IDs.
     */
    public void saveActiveCalendarIds() {
        Set<String> activeCalendarIdStrings = new HashSet<>();
        for (Integer id : activeCalendarIds) {
            activeCalendarIdStrings.add(id.toString());
        }

        preferences
                .edit()
                .putStringSet(Utilities.PREF_ACTIVE_CALENDARS, activeCalendarIdStrings)
                .apply();
    }

    /**
     * Activates the calendar with the given ID and saves the preferences.
     */
    private void activateCalendar(int id) {
        activeCalendarIds.add(id);
    }

    /**
     * Deactivates the calendar with the given ID and saves the preferences.
     */
    private void deactivateCalendar(int id) {
        activeCalendarIds.remove(id);
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
                    R.layout.account_view_item, parent, false));
        } else if (viewType == VIEW_TYPE_CALENDAR) {
            return new CalendarViewHolder(inflater.inflate(
                    R.layout.calendar_view_item, parent, false));
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
            calViewHolder.activeSwitch.setChecked(activeCalendarIds.contains(descriptor.getId()));

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
        private TextView accountNameView;

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

        private GradientDrawable colorShape;
        private TextView calendarNameView;
        private Switch activeSwitch;

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
            CalendarDescriptor calendar = (CalendarDescriptor) calendars.get(getAdapterPosition());

            if (isChecked) {
                activateCalendar(calendar.getId());
            } else {
                deactivateCalendar(calendar.getId());
            }
        }
    }
}
