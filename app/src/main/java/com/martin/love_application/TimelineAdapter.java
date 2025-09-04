package com.martin.love_application;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
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

        // Set title
        holder.titleText.setText(event.getTitle());
        // Set description
        holder.descriptionText.setText(event.getDescription());

        // Format and set date
        String formattedDate = formatDate(event.getDate());
        holder.dateText.setText(formattedDate);

        // Set icon based on event type
        setEventIcon(holder.iconImage, event.getType());

        // Set card background color based on type
        setCardColor(holder.cardView, event.getType());
        
        // Set text colors for good contrast
        setTextColors(holder, event.getType());

        // Handle click to expand/collapse description
        holder.cardView.setOnClickListener(v -> {
            if (holder.descriptionText.getVisibility() == View.GONE) {
                holder.descriptionText.setVisibility(View.VISIBLE);
                holder.expandIcon.setText("▲");
            } else {
                holder.descriptionText.setVisibility(View.GONE);
                holder.expandIcon.setText("▼");
            }
        });

        // Initially hide description if it's long
        if (event.getDescription().length() > 50) {
            holder.descriptionText.setVisibility(View.GONE);
            holder.expandIcon.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionText.setVisibility(View.VISIBLE);
            holder.expandIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return timelineEvents.size();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString; // Return original if parsing fails
        }
    }

    private void setEventIcon(ImageView iconImage, String eventType) {
        int iconResource;
        switch (eventType) {
            case "milestone":
                iconResource = R.drawable.ic_heart;
                break;
            case "holiday":
                iconResource = R.drawable.ic_star;
                break;
            case "adventure":
                iconResource = R.drawable.ic_camera;
                break;
            case "special":
                iconResource = R.drawable.ic_gift;
                break;
            case "funny":
                iconResource = R.drawable.ic_smile;
                break;
            case "memory":
                iconResource = R.drawable.ic_cloud;
                break;
            default:
                iconResource = R.drawable.ic_heart;
                break;
        }

        try {
            iconImage.setImageResource(iconResource);
        } catch (Exception e) {
            // Fallback to default Android icon
            iconImage.setImageResource(android.R.drawable.ic_dialog_info);
        }
    }

    private void setCardColor(CardView cardView, String eventType) {
        int colorResource;
        switch (eventType) {
            case "milestone":
                colorResource = R.color.milestone_color;
                break;
            case "holiday":
                colorResource = R.color.holiday_color;
                break;
            case "adventure":
                colorResource = R.color.adventure_color;
                break;
            case "special":
                colorResource = R.color.special_color;
                break;
            case "funny":
                colorResource = R.color.funny_color;
                break;
            case "memory":
                colorResource = R.color.adventure_color; // Reuse green for memory
                break;
            default:
                colorResource = R.color.default_card_color;
                break;
        }

        try {
            int color = context.getResources().getColor(colorResource);
            cardView.setCardBackgroundColor(color);
        } catch (Exception e) {
            // Fallback to default color
            cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
        }
    }

    private void setTextColors(TimelineViewHolder holder, String eventType) {
        int textColor;
        
        // Determine if we need dark or light text based on background color
        switch (eventType) {
            case "milestone":
                // Pink background - use dark text
                textColor = context.getResources().getColor(android.R.color.black);
                break;
            case "holiday":
                // Yellow background - use dark text
                textColor = context.getResources().getColor(android.R.color.black);
                break;
            case "adventure":
            case "memory":
                // Green background - use white text
                textColor = context.getResources().getColor(android.R.color.white);
                break;
            case "special":
                // Orange background - use dark text
                textColor = context.getResources().getColor(android.R.color.black);
                break;
            case "funny":
                // Blue background - use white text
                textColor = context.getResources().getColor(android.R.color.white);
                break;
            default:
                // Default (purple) background - use white text
                textColor = context.getResources().getColor(android.R.color.white);
                break;
        }

        // Apply text color to all text views
        holder.titleText.setTextColor(textColor);
        holder.descriptionText.setTextColor(textColor);
        holder.dateText.setTextColor(textColor);
        holder.expandIcon.setTextColor(textColor);
    }

    // Method to update the timeline events (useful for refreshing data)
    public void updateEvents(List<TimelineEvent> newEvents) {
        this.timelineEvents = newEvents;
        notifyDataSetChanged();
    }

    // ViewHolder class
    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView iconImage;
        TextView titleText;
        TextView descriptionText;
        TextView dateText;
        TextView expandIcon;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.timeline_card);
            iconImage = itemView.findViewById(R.id.event_icon);
            titleText = itemView.findViewById(R.id.event_title);
            descriptionText = itemView.findViewById(R.id.event_description);
            dateText = itemView.findViewById(R.id.event_date);
            expandIcon = itemView.findViewById(R.id.expand_icon);
        }
    }
}