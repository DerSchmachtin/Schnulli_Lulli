package com.martin.love_application;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TimelineActivity extends AppCompatActivity {

    private RecyclerView timelineRecyclerView;
    private TimelineAdapter timelineAdapter;
    private Button backButton;

    private DatabaseHelper dbHelper;
    private List<TimelineEvent> timelineEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        initializeViews();
        setupDatabase();
        loadTimelineEvents();
        setupRecyclerView();
    }

    private void initializeViews() {
        timelineRecyclerView = findViewById(R.id.timeline_recycler_view);
        backButton = findViewById(R.id.back_button);

        // Set click listener
        backButton.setOnClickListener(v -> finish());
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
    }

    private void loadTimelineEvents() {
        timelineEvents = dbHelper.getAllTimelineEvents();
    }

    private void setupRecyclerView() {
        timelineAdapter = new TimelineAdapter(this, timelineEvents);
        timelineRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        timelineRecyclerView.setAdapter(timelineAdapter);

        // Add some spacing between items
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.timeline_item_spacing);
        timelineRecyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh timeline in case new events were added
        loadTimelineEvents();
        if (timelineAdapter != null) {
            timelineAdapter.updateEvents(timelineEvents);
        }
    }
}