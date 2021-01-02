package net.hypotenubel.calendariq.ui.main.calendar;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.data.calendar.model.AccountDescriptor;
import net.hypotenubel.calendariq.data.calendar.model.CalendarDescriptor;

/**
 * Adapts a {@link CalendarViewModel} for a recycler view.
 */
public class CalendarAdapter extends ListAdapter<Object, RecyclerView.ViewHolder> {

    /** View type for displaying account names. */
    private static final int VIEW_TYPE_ACCOUNT = 1;
    /** View type for displaying calendars. */
    private static final int VIEW_TYPE_CALENDAR = 2;


    public CalendarAdapter() {
        super(DIFF_CALLBACK);
    }


    @Override
    public int getItemViewType(int position) {
        Object o = getItem(position);
        if (o instanceof CalendarDescriptor) {
            return VIEW_TYPE_CALENDAR;
        } else if (o instanceof AccountDescriptor) {
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
                    R.layout.view_item_calendar_account, parent, false));
        } else if (viewType == VIEW_TYPE_CALENDAR) {
            return new CalendarViewHolder(inflater.inflate(
                    R.layout.view_item_calendar, parent, false));
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Object o = getItem(i);

        if (o instanceof CalendarDescriptor) {
            CalendarDescriptor descriptor = (CalendarDescriptor) o;
            CalendarViewHolder calViewHolder = (CalendarViewHolder) viewHolder;

            // Xor colour value with this value to make the colour solid as opposed to translucent
            calViewHolder.colorShape.setColor(descriptor.getColour() | 0xFF000000);
            calViewHolder.calendarNameView.setText(descriptor.getCalName());
            calViewHolder.activeSwitch.setChecked(descriptor.isActive());

        } else if (o instanceof AccountDescriptor) {
            AccountDescriptor account = (AccountDescriptor) o;
            AccountViewHolder accViewHolder = (AccountViewHolder) viewHolder;

            accViewHolder.accountNameView.setText(account.getName());
        }
    }

    private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Object>() {

        @Override
        public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (!oldItem.getClass().equals(newItem.getClass())) {
                return false;
            } else {
                // Calendar equality is just an ID check; account equality is just a name check
                return oldItem.equals(newItem);
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof CalendarDescriptor) {
                CalendarDescriptor oldCalendar = (CalendarDescriptor) oldItem;
                CalendarDescriptor newCalendar = (CalendarDescriptor) newItem;

                return oldCalendar.getCalName().equals(newCalendar.getCalName())
                        && oldCalendar.getColour() == newCalendar.getColour()
                        && oldCalendar.isActive() == newCalendar.isActive();

            } else {
                // In this case, both items will be accounts and their names are the same, so
                // there's nothing more to check here
                return true;
            }
        }

    };


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
            CalendarDescriptor calendar = (CalendarDescriptor) getItem(getBindingAdapterPosition());
            calendar.setActive(isChecked);
        }
    }
}
