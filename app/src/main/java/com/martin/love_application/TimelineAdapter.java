package com.martin.love_application;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private Context context;
    private List<TimelineEvent> timelineEvents;
    private LayoutInflater inflater;

    public TimelineAdapter(Context context, List<TimelineEvent> timelineEvents) {
        this.context = context;
        this.timelineEvents = timelineEvents;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_timeline_event, parent, false);
        return new TimelineViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TimelineEvent event = timelineEvents.get(position);

        // Set title and description
        holder.titleText.setText(event.getTitle());
        holder.descriptionText.setText(event.getDescription());

        // Format and set date
        String formattedDate = formatDate(event.getDate());
        holder.dateText.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return timelineEvents.size();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("d. MMMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString; // Return original if parsing fails
        }
    }

    public void updateEvents(List<TimelineEvent> newEvents) {
        this.timelineEvents = newEvents;
        android.util.Log.d("TimelineAdapter", "Updating events: " + (newEvents != null ? newEvents.size() : 0) + " events");
        notifyDataSetChanged();
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView titleText;
        TextView dateText;
        TextView descriptionText;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.timeline_card);
            titleText = itemView.findViewById(R.id.event_title);
            dateText = itemView.findViewById(R.id.event_date);
            descriptionText = itemView.findViewById(R.id.event_description);
        }
    }
}