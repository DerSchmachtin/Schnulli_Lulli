package com.martin.love_application;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.card.MaterialCardView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
        
        // Set time ago text
        String timeAgo = getTimeAgo(event.getDate());
        holder.timeAgoText.setText(timeAgo);

        // Set icon and chip based on event type
        setupEventTypeUI(holder, event.getType());

        // Set up click listeners for action buttons
        holder.shareButton.setOnClickListener(v -> shareEvent(event));
        holder.favoriteButton.setOnClickListener(v -> toggleFavorite(event));
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

    private void setupEventTypeUI(TimelineViewHolder holder, String eventType) {
        // Set icon
        int iconResource;
        String chipText;
        int chipColor;
        
        switch (eventType) {
            case "milestone":
                iconResource = R.drawable.ic_heart;
                chipText = "Meilenstein";
                chipColor = R.color.chip_milestone_color;
                break;
            case "holiday":
                iconResource = R.drawable.ic_star;
                chipText = "Feiertag";
                chipColor = R.color.chip_holiday_color;
                break;
            case "adventure":
                iconResource = R.drawable.ic_rocket;
                chipText = "Abenteuer";
                chipColor = R.color.chip_adventure_color;
                break;
            case "special":
                iconResource = R.drawable.ic_star;
                chipText = "Besonders";
                chipColor = R.color.chip_special_color;
                break;
            case "funny":
                iconResource = R.drawable.ic_smile;
                chipText = "Lustig";
                chipColor = R.color.chip_default_color;
                break;
            case "memory":
                iconResource = R.drawable.ic_cloud;
                chipText = "Erinnerung";
                chipColor = R.color.chip_memory_color;
                break;
            default:
                iconResource = R.drawable.ic_heart;
                chipText = "Erinnerung";
                chipColor = R.color.chip_default_color;
                break;
        }

        try {
            holder.iconImage.setImageResource(iconResource);
            holder.typeChip.setText(chipText);
            holder.typeChip.setChipBackgroundColor(ContextCompat.getColorStateList(context, chipColor));
        } catch (Exception e) {
            // Fallback to defaults
            holder.iconImage.setImageResource(android.R.drawable.ic_dialog_info);
            holder.typeChip.setText("Event");
        }
    }
    
    private String getTimeAgo(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date eventDate = inputFormat.parse(dateString);
            Date currentDate = new Date();
            
            long diffInMillis = currentDate.getTime() - eventDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            
            if (diffInDays == 0) {
                return "Heute";
            } else if (diffInDays == 1) {
                return "Gestern";
            } else if (diffInDays < 7) {
                return "vor " + diffInDays + " Tagen";
            } else if (diffInDays < 30) {
                long weeks = diffInDays / 7;
                return "vor " + weeks + " Woche" + (weeks > 1 ? "n" : "");
            } else if (diffInDays < 365) {
                long months = diffInDays / 30;
                return "vor " + months + " Monat" + (months > 1 ? "en" : "");
            } else {
                long years = diffInDays / 365;
                return "vor " + years + " Jahr" + (years > 1 ? "en" : "");
            }
        } catch (ParseException e) {
            return "vor einiger Zeit";
        }
    }
    
    private void shareEvent(TimelineEvent event) {
        // TODO: Implement sharing functionality
        // For now, just show a toast
        android.widget.Toast.makeText(context, "Event teilen: " + event.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private void toggleFavorite(TimelineEvent event) {
        // TODO: Implement favorite functionality
        // For now, just show a toast
        android.widget.Toast.makeText(context, "Favorit: " + event.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
    }

    public void updateEvents(List<TimelineEvent> newEvents) {
        this.timelineEvents = newEvents;
        android.util.Log.d("TimelineAdapter", "Updating events: " + (newEvents != null ? newEvents.size() : 0) + " events");
        notifyDataSetChanged();
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView iconImage;
        TextView titleText;
        TextView dateText;
        TextView timeAgoText;
        TextView descriptionText;
        Chip typeChip;
        MaterialButton shareButton;
        MaterialButton favoriteButton;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.timeline_card);
            iconImage = itemView.findViewById(R.id.event_icon);
            titleText = itemView.findViewById(R.id.event_title);
            dateText = itemView.findViewById(R.id.event_date);
            timeAgoText = itemView.findViewById(R.id.event_time_ago);
            descriptionText = itemView.findViewById(R.id.event_description);
            typeChip = itemView.findViewById(R.id.event_type_chip);
            shareButton = itemView.findViewById(R.id.button_share);
            favoriteButton = itemView.findViewById(R.id.button_favorite);
        }
    }
}