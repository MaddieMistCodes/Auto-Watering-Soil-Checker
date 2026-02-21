package com.example.example2;
// Package declaration - this file belongs to com.example.example2
import android.os.Bundle;
// Bundle is used to save/restore activity state
import androidx.appcompat.app.AppCompatActivity;
// App compact activity is the base class for activities
// Provides backwards compatible features
import android.content.Intent;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

// Handler and Work imports
import android.os.Handler;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// Intent used to navigate through activities
public class MainActivity extends AppCompatActivity{
    // MainActivity is the main screen of our app
    // extends AppCompatActivity means we inherit the functionality

    // Members - accessible through the class
    private TextView tvSensorValue;
    private TextView tvSensorStatus;
    private EditText etTestValue;
    private Button btnUpdate;
    private TextView tvReadingCount;

    // Colour constants
    // Converting hex colour to integer
    private static final int COLOR_RED = Color.parseColor("#E743C3");
    private static final int COLOR_YELLOW = Color.parseColor("#F39C12");
    private static final int COLOR_GREEN = Color.parseColor("#27AE60");

    // Threshold Constants
    private static final int THRESHOLD_LOW = 30;
    private static final int THRESHOLD_HIGH = 65;

    // Firebase reference
    private DatabaseReference database;
    //private static boolean initialReadDone = false;

    @Override
    // @Override tells Java we are replacing a method from parent class
    protected void onCreate(Bundle savedInstanceState){
        // onCreate is called when the activity is first created
        // This is where we set up the UI
        super.onCreate(savedInstanceState);
        // Always call parents onCreate first
        setContentView(R.layout.activity_main);
        // Connect this activity to its layout file
        // R.layout.activity_main refers to res/layout/activity_main.xml
        initialiseViews();
        database = FirebaseDatabase.getInstance().getReference("sensor");
        setUpBottomNavigation();
        setUpButtonListeners();
        updateReadingCount();

        // NEW FUNCTIONALITY
        // Handler allows new value read every time user opens app
        // Work Manager allows background check of values to allow constant readings on graphs

        Handler handler = new Handler();
        Runnable hourlyCheck = new Runnable() {
            @Override
            public void run() {
                readFirebaseData();
                handler.postDelayed(this, 60 * 60 * 1000);
            }
        };
        handler.post(hourlyCheck);

        // WorkManager - runs in background when app is closed
        PeriodicWorkRequest moistureCheck = new PeriodicWorkRequest.Builder(
                MoistureCheckWorker.class,
                1, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "moistureCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                moistureCheck);
    }
    protected void onResume(){
        super.onResume();
        updateReadingCount();
    }
    private void setUpBottomNavigation(){
        // Find bottom navigation in layout
        // findViewById searches layout for ID
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        // Mark home tab as selected
        bottomNav.setSelectedItemId(R.id.nav_home);
        // Set up listener for navigation clicks
        bottomNav.setOnItemSelectedListener(item -> {
            // LAMBDA expression, runs when tab is selected
            // item is the menu item that is clicked

            int itemId = item.getItemId();
            if(itemId == R.id.nav_home){
                // already on home - do nothing
                return true;
            }
            else if(itemId == R.id.nav_graphs){
                // Navigate to graphs screen
                navigateToActivity(GraphsActivity.class);
                return true;
            }
            else if(itemId == R.id.nav_settings){
                // Navigate to settings screen
                navigateToActivity(SettingsActivity.class);
                return true;
            }
            return false;

        });

    } // end of bottom nav method

    // Custom method to use intent to navigate activities
    private void navigateToActivity(Class<?> activityClass){
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
        finish();
    }
    // Custom method to find views in layout
    private void initialiseViews(){
        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        tvSensorValue = findViewById(R.id.tvSensorValue);
        etTestValue =findViewById(R.id.etTestValue);
        btnUpdate = findViewById(R.id.btnUpdate);
        tvReadingCount = findViewById(R.id.tvReadingCount);
    }
    // Set up button click listeners
    private void setUpButtonListeners(){
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFirebaseData();
            }
        });
    }
    private void updateSensorValue(){
        String inputText = etTestValue.getText().toString().trim();
        if(inputText.isEmpty()){
            Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            int value = Integer.parseInt(inputText);

            if(value < 0 || value > 100){
                Toast.makeText(this, "Please enter a value between 0 and 100", Toast.LENGTH_SHORT).show();
                return;
            }
            DataManager.getInstance().addReading(value);
            tvSensorValue.setText(String.valueOf(value));
            updateValueColour(value);
            updateReadingCount();
            etTestValue.setText("");

            int count = DataManager.getInstance().getReadingCount();
            Toast.makeText(this, "Saved! Total readings: " + count,Toast.LENGTH_SHORT).show();


        }
        catch(NumberFormatException e){
            // Runs if parse int fails
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateValueColour(int value){
        if(value < THRESHOLD_LOW){
            tvSensorValue.setTextColor(COLOR_RED);
            tvSensorValue.setText("Low");
            tvSensorValue.setTextColor(COLOR_RED);
        }
        else if(value <= THRESHOLD_HIGH){
            tvSensorValue.setTextColor(COLOR_YELLOW);
            tvSensorValue.setText("Medium");
            tvSensorValue.setTextColor(COLOR_YELLOW);
        }
        else{
            tvSensorValue.setTextColor(COLOR_GREEN);
            tvSensorValue.setText("High");
            tvSensorValue.setTextColor(COLOR_GREEN);
        }
    }

    private void updateReadingCount(){
        int count = DataManager.getInstance().getReadingCount();
        if(tvReadingCount != null){
            tvReadingCount.setText("Readings saved: " + count);
        }
    }
    private void readFirebaseData() {
        database.child("moisture").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                float moisture = Float.parseFloat(snapshot.getValue().toString());
                int moistureInt = (int) moisture;

                tvSensorValue.setText(String.valueOf(moistureInt));
                DataManager.getInstance().addReading(moistureInt);
                updateReadingCount();

                // Save reading with timestamp to Firebase
                long timestamp = System.currentTimeMillis();
                database.child("readings").child(String.valueOf(timestamp)).setValue(moistureInt);

                Toast.makeText(this, "Moisture read: " + moistureInt + "%", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to read data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
