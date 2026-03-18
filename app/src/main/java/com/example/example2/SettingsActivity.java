package com.example.example2;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
// Needed to create a URI from the CSV file for sharing
import android.net.Uri;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
// FileProvider lets us safely share a file with other apps
import androidx.core.content.FileProvider;
// Needed to fetch readings directly from Firebase in this activity
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
// writes the CSV content to a temp file
import java.io.FileWriter;
// handles any file writing errors
import java.io.IOException;
// converts raw timestamps to readable date/time strings
import java.io.OutputStream;
import java.text.SimpleDateFormat;
// sed alongside SimpleDateFormat to format timestamps
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchAutomation;
    private SwitchMaterial switchNotifications;
    private Button btnSaveSettings;
    private TextView tvSettingStatus;
    private EditText etThreshold;
    private Button btnExportCSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialiseViews();
        setUpBottomNavigation();
        loadSettings();
        setupSaveButton();
        // Sets up the click listener for the export button
        setupExportButton();

    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(SettingsActivity.this, activityClass);
        startActivity(intent);
        finish();
    }

    private void setUpBottomNavigation() {
        // Find bottom navigation in layout
        // findViewById searches layout for ID
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        // Mark home tab as selected
        bottomNav.setSelectedItemId(R.id.nav_settings);
        // Set up listener for navigation clicks
        bottomNav.setOnItemSelectedListener(item -> {
            // LAMBDA expression, runs when tab is selected
            // item is the menu item that is clicked

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navigateToActivity(MainActivity.class);
                return true;
            } else if (itemId == R.id.nav_graphs) {
                // Navigate to graphs screen
                navigateToActivity(GraphsActivity.class);
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;

        });

    } // end of bottom nav method

    private void initialiseViews() {
        switchAutomation = findViewById(R.id.switchAutomation);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        tvSettingStatus = findViewById(R.id.tvSettingStatus);
        btnExportCSV = findViewById(R.id.btnExportCSV);
        etThreshold = findViewById(R.id.etThreshold);
    }

    private void setupSaveButton() {
        btnSaveSettings.setOnClickListener(v -> {
            boolean automationEnabled = switchAutomation.isChecked();
            boolean notificationsEnabled = switchNotifications.isChecked();

            // ← added threshold reading and saving
            String thresholdStr = etThreshold.getText().toString();
            int threshold = thresholdStr.isEmpty() ? 30 : Integer.parseInt(thresholdStr);

            SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("automationEnabled", automationEnabled)
                    .putBoolean("notificationsEnabled", notificationsEnabled)
                    .putInt("threshold", threshold)
                    .apply();

            tvSettingStatus.setText("Settings Saved!");
            android.util.Log.d("Settings", "Automation: " + automationEnabled +
                    ", Notifications: " + notificationsEnabled +
                    ", Threshold: " + threshold);
        });
    }

    private void loadSettings(){
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        switchAutomation.setChecked(prefs.getBoolean("automationEnabled", false));
        switchNotifications.setChecked(prefs.getBoolean("notificationsEnabled", false));
        int threshold = prefs.getInt("threshold", 30);
        etThreshold.setText(String.valueOf(threshold));
    }
    private void setupExportButton(){
        // Set-up listener for the button
        btnExportCSV.setOnClickListener(v -> {
            // Disable button if user taps twice
            btnExportCSV.setEnabled(false);
            btnExportCSV.setText("Exporting...");
            // Navigate to sensore/readings and orders by key (time) and gets one-time read
            FirebaseDatabase.getInstance().getReference("sensor/readings")
                    .orderByKey()
                    .get()
                    // If read is successful but no data, inform with toast
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            Toast.makeText(this, "No readings to export", Toast.LENGTH_SHORT).show();
                            resetExportButton();
                            return;
                        }

                        // String Builder puts together CSV
                        // Appending the header row
                        StringBuilder csv = new StringBuilder();
                        csv.append("Date/Time,Moisture (%)\n");
                        // Date formatter to change time into readable strings
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

                        // Loops through each value getting the timestamp, moisture
                        // Friendly time with sdf object
                        for (DataSnapshot child : snapshot.getChildren()) {
                            long timestamp = Long.parseLong(child.getKey());
                            int value = child.getValue(Integer.class);
                            String dateTime = sdf.format(new Date(timestamp));
                            csv.append(dateTime).append(",").append(value).append("\n");
                        }

                        // Generate filename with unique timestamp idnetifier
                        try {
                            String fileName = "moisture_readings_" +
                                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                            .format(new Date()) + ".csv";

                            // File description to device
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                            values.put(MediaStore.Downloads.IS_PENDING, 1); // Mark as pending while writing

                            // Insert a new entry into the Downloads collection
                            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                            Uri fileUri = getContentResolver().insert(collection, values);

                            // Write CSV content to the file in downloads
                            try (OutputStream os = getContentResolver().openOutputStream(fileUri)) {
                                os.write(csv.toString().getBytes());
                            }

                            // Mark as no longer pending (=0) so it appears in Downloads
                            values.clear();
                            values.put(MediaStore.Downloads.IS_PENDING, 0);
                            getContentResolver().update(fileUri, values, null, null);

                            // Success toast to inform user
                            Toast.makeText(this, "Saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                        // If error occurs inform user
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to save file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        resetExportButton();
                    })
                    // If Firebase fails, show user and re-enable the button
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch readings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        resetExportButton();
                    });
        });
    }

    private void resetExportButton() {
        // Method to re-enable the button, so user can press again
        btnExportCSV.setEnabled(true);
        btnExportCSV.setText("Export CSV");
    }
}