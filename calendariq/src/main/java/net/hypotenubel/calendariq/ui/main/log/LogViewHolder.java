package net.hypotenubel.calendariq.ui.main.log;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.data.model.stats.BroadcastStatistics;

/**
 * Thing that displays a log item in a {@link RecyclerView}.
 */
public class LogViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final GradientDrawable syncResultShape;
    private final TextView syncTime;
    private final TextView syncDetails;

    private LogViewHolder(@NonNull View itemView) {
        super(itemView);

        context = itemView.getContext();

        // Extract relevant views
        syncTime = itemView.findViewById(R.id.logViewItem_syncTime);
        syncDetails = itemView.findViewById(R.id.logViewItem_syncDetails);

        // The shape is a bit more complicated
        ImageView shapeView = itemView.findViewById(R.id.logViewItem_syncResultIcon);
        syncResultShape = (GradientDrawable) shapeView.getDrawable();
    }

    static LogViewHolder create(ViewGroup parent) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.log_view_item, parent, false);
        return new LogViewHolder(view);
    }

    /**
     * Display the given objects properties in the view managed by this view holder.
     */
    public void bind(BroadcastStatistics stats) {
        Resources res = context.getResources();

        int resultColourResID = stats.getMessage() == null
                ? R.color.calendar_colorSuccess
                : R.color.calendar_colorFailure;
        syncResultShape.setColor(res.getColor(resultColourResID, null));

        syncTime.setText(res.getString(
                R.string.logFragment_logViewItem_syncTime,
                stats.getUtcTimestampMillis()));

        if (stats.getMessage() != null) {
            syncDetails.setText(stats.getMessage());
        } else {
            syncDetails.setText(res.getQuantityString(
                    R.plurals.logFragment_logViewItem_syncDetails_success,
                    stats.getContactedApps(),
                    stats.getContactedApps()));
        }
    }

}
