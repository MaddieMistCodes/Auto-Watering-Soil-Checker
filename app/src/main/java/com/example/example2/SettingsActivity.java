package com.example.example2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchAutomation;
    private SwitchMaterial switchNotifications;
    private Button btnSaveSettings;
    private TextView tvSettingStatus;
    private EditText etThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialiseViews();
        setUpBottomNavigation();
        loadSettings();
        setupSaveButton();

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
}