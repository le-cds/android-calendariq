package net.hypotenubel.calendariq.ui.main.log;

import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;

/**
 * Adapts a {@link LogViewModel} for a recycler view.
 */
public class LogAdapter extends ListAdapter<BroadcastStatistics, LogViewHolder> {

    public LogAdapter() {
        super(new BroadcastDiff());
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return LogViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        BroadcastStatistics stat = getItem(position);
        holder.bind(stat);
    }

    static class BroadcastDiff extends DiffUtil.ItemCallback<BroadcastStatistics> {

        @Override
        public boolean areItemsTheSame(@NonNull BroadcastStatistics oldItem, @NonNull BroadcastStatistics newItem) {
            return oldItem.getUtcTimestampMillis() == newItem.getUtcTimestampMillis();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BroadcastStatistics oldItem, @NonNull BroadcastStatistics newItem) {
            return oldItem.getTotalApps() == newItem.getTotalApps()
                    && oldItem.getContactedApps() == newItem.getTotalApps()
                    && TextUtils.equals(oldItem.getMessage(), newItem.getMessage());
        }

    }
}
