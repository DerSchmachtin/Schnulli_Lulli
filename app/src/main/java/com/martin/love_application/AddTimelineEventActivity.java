package com.martin.love_application;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTimelineEventActivity extends AppCompatActivity {

    private TextInputEditText titleInput, dateInput, descriptionInput;
    private MaterialButton saveButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timeline_event);
//
       dbHelper = DatabaseHelper.getInstance(this);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        // Find the TextInputLayouts
        TextInputLayout titleLayout = findViewById(R.id.text_input_layout_title);
        TextInputLayout dateLayout = findViewById(R.id.text_input_layout_date);
        TextInputLayout descriptionLayout = findViewById(R.id.text_input_layout_description);

        // Find the TextInputEditTexts within the layouts
        titleInput = (TextInputEditText) titleLayout.getEditText();
        dateInput = (TextInputEditText) dateLayout.getEditText();
        descriptionInput = (TextInputEditText) descriptionLayout.getEditText();

        // Correctly find the MaterialButton
        saveButton = findViewById(R.id.button_save_event);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        String title = titleInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Title and Date are required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDateValid(date, "yyyy-MM-dd")) {
            Toast.makeText(this, "Please enter the date in YYYY-MM-DD format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable local event creation - only Firebase data should be used
        Toast.makeText(this, "❌ Local event creation disabled. Events are managed via Firebase only.", Toast.LENGTH_LONG).show();
    }

    private boolean isDateValid(String dateString, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
}