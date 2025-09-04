package com.martin.love_application;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TimelineActivity extends AppCompatActivity {

    private RecyclerView timelineRecyclerView;
    private TimelineAdapter timelineAdapter;
    private MaterialButton backButton;
    private MaterialButton syncButton;

    private DatabaseHelper dbHelper;
    private NetworkDataManager networkManager;
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
        syncButton = findViewById(R.id.sync_button);

        // Set click listeners
        backButton.setOnClickListener(v -> finish());
        syncButton.setOnClickListener(v -> syncTimeline());
    }

    private void setupDatabase() {
        dbHelper = DatabaseHelper.getInstance(this);
        networkManager = new NetworkDataManager(this);
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

    private void syncTimeline() {
        syncButton.setEnabled(false);
        syncButton.setText("🔄 Syncing...");
        
        Toast.makeText(this, "Synchronisiere Timeline...", Toast.LENGTH_SHORT).show();
        
        networkManager.fetchAndUpdateTimeline(new NetworkDataManager.DataUpdateCallback() {
            @Override
            public void onSuccess(int newEventsCount) {
                syncButton.setEnabled(true);
                syncButton.setText("🔄 Sync");
                
                if (newEventsCount > 0) {
                    Toast.makeText(TimelineActivity.this, 
                        "❤️ " + newEventsCount + " neue Events hinzugefügt!", 
                        Toast.LENGTH_LONG).show();
                    // Refresh the timeline
                    loadTimelineEvents();
                    if (timelineAdapter != null) {
                        timelineAdapter.updateEvents(timelineEvents);
                    }
                } else {
                    Toast.makeText(TimelineActivity.this, 
                        "✅ Timeline ist bereits aktuell", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                syncButton.setEnabled(true);
                syncButton.setText("🔄 Sync");
                
                Toast.makeText(TimelineActivity.this, 
                    "❌ Fehler: " + error, 
                    Toast.LENGTH_LONG).show();
                
                Log.e("TimelineActivity", "Sync error: " + error);
            }

            @Override
            public void onStarted() {
                // Already handled above
            }
        });
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkManager != null) {
            networkManager.shutdown();
        }
    }
}