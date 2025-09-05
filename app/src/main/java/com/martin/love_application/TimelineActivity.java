package com.martin.love_application;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimelineActivity extends AppCompatActivity {

    private RecyclerView timelineRecyclerView;
    private TimelineAdapter timelineAdapter;
    private MaterialToolbar toolbar;
    private MaterialButton syncButton;
    private FloatingActionButton fabAddEvent;
    private TextView totalMemories;
    private TextView daysTogether;

    private DatabaseHelper dbHelper;
    private FirebaseDataManager firebaseManager;
    private List<TimelineEvent> timelineEvents;
    
    // Your relationship start date - should match MainActivity
    private static final String RELATIONSHIP_START = "2024-11-01";



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
        // Initialize views
        timelineRecyclerView = findViewById(R.id.timeline_recycler_view);
        toolbar = findViewById(R.id.toolbar);
        syncButton = findViewById(R.id.sync_button);
        fabAddEvent = findViewById(R.id.fab_add_event);
        totalMemories = findViewById(R.id.total_memories);
        daysTogether = findViewById(R.id.days_together);

        // Setup toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Set click listeners
        syncButton.setOnClickListener(v -> syncTimeline());
        fabAddEvent.setOnClickListener(v -> openAddEventActivity());
        
        // Update header stats
        updateHeaderStats();
    }

    private void setupDatabase() {
        dbHelper = DatabaseHelper.getInstance(this);
        firebaseManager = new FirebaseDataManager(this);
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
        
        firebaseManager.fetchAndUpdateTimeline(new FirebaseDataManager.DataUpdateCallback() {
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
                    // Update header stats
                    updateHeaderStats();
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
        });
    }

    private void updateHeaderStats() {
        // Update total memories count
        if (timelineEvents != null && totalMemories != null) {
            totalMemories.setText(String.valueOf(timelineEvents.size()));
        }
        
        // Calculate and update days together
        if (daysTogether != null) {
            int days = calculateDaysTogether();
            daysTogether.setText(String.valueOf(days));
        }
    }
    
    private int calculateDaysTogether() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(RELATIONSHIP_START);
            Date currentDate = new Date();
            
            if (startDate != null) {
                long timeDiff = currentDate.getTime() - startDate.getTime();
                return (int) (timeDiff / (1000 * 60 * 60 * 24));
            }
        } catch (Exception e) {
            Log.e("TimelineActivity", "Error calculating days together: " + e.getMessage());
        }
        return 0;
    }
    
    private void openAddEventActivity() {
        Intent intent = new Intent(this, AddTimelineEventActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh timeline in case new events were added
        loadTimelineEvents();
        if (timelineAdapter != null) {
            timelineAdapter.updateEvents(timelineEvents);
        }
        // Update header stats
        updateHeaderStats();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseManager != null) {
            firebaseManager.shutdown();
        }
    }
}