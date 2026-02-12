package com.example.example2;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialiseViews();
        setUpBottomNavigation();
        setupSaveButton();

    }
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(SettingsActivity.this, activityClass);
        startActivity(intent);
        finish();
    }

    private void setUpBottomNavigation(){
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
            if(itemId == R.id.nav_home){
                navigateToActivity(MainActivity.class);
                return true;
            }
            else if(itemId == R.id.nav_graphs){
                // Navigate to graphs screen
                navigateToActivity(GraphsActivity.class);
                return true;
            }
            else if(itemId == R.id.nav_settings){
                return true;
            }
            return false;

        });

    } // end of bottom nav method

    private void initialiseViews(){
        switchAutomation = findViewById(R.id.switchAutomation);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnSaveSettings =findViewById(R.id.btnSaveSettings);
        tvSettingStatus = findViewById(R.id.tvSettingStatus);
    }

    private void setupSaveButton(){
        btnSaveSettings.setOnClickListener(v -> {
            // Get switch states
            boolean automationEnabled = switchAutomation.isChecked();
            // isChecked returns true or false

            boolean notificationsEnabled = switchNotifications.isChecked();

            // In real app, this is saved to firebase
            // Showing confirmation for now
            tvSettingStatus.setText("Settings Saved!");

            // Log Values
            android.util.Log.d("Settings", "Automation: " + automationEnabled + ", Notifications: " + notificationsEnabled);
        });
    }
}