package com.example.example2;
// Package declaration - this file belongs to com.example.example2
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
// Bundle is used to save/restore activity state
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
// App compact activity is the base class for activities
// Provides backwards compatible features
import android.content.Intent;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ValueEventListener;

import android.graphics.drawable.AnimationDrawable;

// Imports for plant puns
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Random;

// Intent used to navigate through activities
public class MainActivity extends AppCompatActivity{
    // MainActivity is the main screen of our app
    // extends AppCompatActivity means we inherit the functionality

    // Members - accessible through the class
    private TextView tvSensorValue;
    private TextView tvSensorStatus;
    //private EditText etTestValue;
    private Button btnUpdate;
    private TextView tvReadingCount;
    private ImageView ivPlantCharacter;

    // Colour constants
    // Converting hex colour to integer
    private static final int COLOR_RED = Color.parseColor("#FF6B6B");
    private static final int COLOR_YELLOW = Color.parseColor("#FFD93D");
    private static final int COLOR_GREEN = Color.parseColor("#6BCB77");
    private static final int COLOR_BLUE = Color.parseColor("#4ECDC4");

    // Threshold Constants
    private static final int THRESHOLD_LOW = 30;
    private static final int THRESHOLD_HIGH = 65;
    private static final int THRESHOLD_TOO_HIGH = 85;

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

        // Testing method to ensure appropriate value appears to reader once app opens
        startRealtimeUpdates();

        setUpBottomNavigation();
        setUpButtonListeners();
        setUpPlantPunListener();
        updateReadingCount();
        createNotificationChannel();
        scheduleHourlyMoistureCheck();

    }
    private void scheduleHourlyMoistureCheck() {
        PeriodicWorkRequest moistureCheck = new PeriodicWorkRequest.Builder(
                // Determines how often moisturecheck is called
                MoistureCheckWorker.class,
                2, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "moistureCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                moistureCheck);
    }
    protected void onResume() {
        super.onResume();
        //updateReadingCount();
        //readFirebaseData();
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        int lastReading = prefs.getInt("lastReading", -1);
        // only restore if real reading, avoids -1 being displayed on initial set up
        if (lastReading != -1) {
            tvSensorValue.setText(String.valueOf(lastReading));
            updateValueColour(lastReading);
        }
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
        // Intent parameters: where your coming from, where your going to
        Intent intent = new Intent(MainActivity.this, activityClass);
        // Organise the activities stacking up - so bottom nav does not get muddled
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // Execute the navigation, to the new screen
        startActivity(intent);
        // Destroys the actvity we are moving from
        //finish();
    }
    // Custom method to find views in layout
    private void initialiseViews(){
        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        tvSensorValue = findViewById(R.id.tvSensorValue);
        btnUpdate = findViewById(R.id.btnUpdate);
        tvReadingCount = findViewById(R.id.tvReadingCount);
        ivPlantCharacter = findViewById(R.id.ivPlantCharacter);
    }
    private void setUpButtonListeners(){
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            // When button clicked for "update", it will fun firebase to get the reading
            @Override
            public void onClick(View v) {
                readFirebaseData();
            }
        });
    }
    private void updateValueColour(int value){
        int animRes;

        if(value < THRESHOLD_LOW){
            tvSensorValue.setTextColor(COLOR_RED);
            tvSensorStatus.setText("Status: Dry 🥀");
            tvSensorStatus.setTextColor(COLOR_RED);
            animRes = R.drawable.anim_plant_dehydrated;
        }
        else if(value <= THRESHOLD_HIGH){
            tvSensorValue.setTextColor(COLOR_YELLOW);
            tvSensorStatus.setText("Status: Okay");
            tvSensorStatus.setTextColor(COLOR_YELLOW);
            animRes = R.drawable.anim_plant_ok;
        }
        else if(value <= THRESHOLD_TOO_HIGH){
            tvSensorValue.setTextColor(COLOR_GREEN);
            tvSensorStatus.setText("Status: Perfect 🌸");
            tvSensorStatus.setTextColor(COLOR_GREEN);
            animRes = R.drawable.anim_plant_happy;
        }
        else{
            tvSensorValue.setTextColor(COLOR_BLUE);
            tvSensorStatus.setText("Status: Very Wet");
            tvSensorStatus.setTextColor(COLOR_BLUE);
            animRes = R.drawable.anim_plant_uncomfortable;
        }
        // Load the appropriate xml
        ivPlantCharacter.setImageResource(animRes);
        // Grab the loaded drawable and cast it into an animation object to access methods
        AnimationDrawable anim = (AnimationDrawable) ivPlantCharacter.getDrawable();
        // Tells animation to cycle between frames in xml
        anim.start();
    }
    // Redundant with firebase data
    private void updateReadingCount(){
        // Update reading count based on firebase readings
        database.child("readings").get().addOnSuccessListener(snapshot -> {
            int count = (int) snapshot.getChildrenCount();
            if(tvReadingCount != null){
                tvReadingCount.setText("Readings saved: " + count);
            }
        });
    }
    private void readFirebaseData() {
        // Go to firebase and fine moisture node, read once. If succesful, store in snapshot
        database.child("moisture").get().addOnSuccessListener(snapshot -> {
            // If data is there
            if (snapshot.exists()) {
                // Get value of snapshot (object), parse to a String and then a float
                float moisture = Float.parseFloat(snapshot.getValue().toString());
                // Converts to int for clean percentage
                int moistureInt = (int) moisture;
                // Display in tv box and update colour

                // Basically jots down a note of the latest reading
                SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                prefs.edit().putInt("lastReading", moistureInt).apply();

                tvSensorValue.setText(String.valueOf(moistureInt));
                updateValueColour(moistureInt);
                //DataManager.getInstance().addReading(moistureInt);
                //updateReadingCount();

                // added notification check
                //int threshold = prefs.getInt("threshold", 30);
                //if(moistureInt < threshold){
                    sendNotification(moistureInt);
                //}

                // Save reading with timestamp to Firebase
                long timestamp = System.currentTimeMillis();
                database.child("readings").child(String.valueOf(timestamp)).setValue(moistureInt);

                // Read readings from firebase
                database.child("readings").get().addOnSuccessListener(readingsSnapshot -> {
                    // Count how many children in snapshot
                    long count = readingsSnapshot.getChildrenCount();
                    // run through each child
                    for (DataSnapshot child : readingsSnapshot.getChildren()) {
                        if (count <= 100) break;  // Changed from 10 to 100 saved on FB
                        child.getRef().removeValue();
                        count--;
                    }
                });

                Toast.makeText(this, "Moisture read: " + moistureInt + "%", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to read data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    "moisture_channel",
                    "Moisture_Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alerts when moisture drops below a certain threshold");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    private void sendNotification(int moisture){
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notificationsEnabled", false);

        if(!notificationsEnabled) return;

        // Check permission for Android 13+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED){
                // Permission not granted, request it
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
                return;
            }
        }
        if(moisture <= THRESHOLD_HIGH && moisture > THRESHOLD_LOW){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "moisture_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Low Moisture Alert")
                .setContentText("Moisture is at " + moisture + "%, water the plant soon.")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(1, builder.build());}

        if(moisture < THRESHOLD_LOW){
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "moisture_channel")
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Low Moisture Alert")
                    .setContentText("Moisture is at " + moisture + "%, water the plant!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            // Different id's, so both can appear
            manager.notify(2, builder.build());
        }

    }
    // Real time updates on tv Sensor value
    private void startRealtimeUpdates() {
        database.child("moisture").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int moistureInt = Integer.parseInt(snapshot.getValue().toString());

                    // Update the UI immediately when Firebase changes
                    tvSensorValue.setText(String.valueOf(moistureInt));
                    // Update according to parameters
                    updateValueColour(moistureInt);

                    // Save to prefs so it's there on next app launch
                    getSharedPreferences("MyApp", MODE_PRIVATE)
                            .edit().putInt("lastReading", moistureInt).apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void setUpPlantPunListener(){
        String puns[] = {
                "I'm rooting for you! 🌱",
                "You grow girl! 🌿",
                "I'm so frond of you! 🌿",
                "Aloe you vera much! 🪴",
                "I be-leaf in you! 🍃",
                "Thistle be a great day! 🌸",
                "I'm so glad we grew together! 🌱",
                "Soil mate! 🌍",
                "You have lots of thyme!✨",
                "You look radishing! 🌸",
                "Nothing to seed here 🙈, you grow girl!"
        };
        ivPlantCharacter.setOnClickListener(v -> {
            Random random = new Random();
            String pun = puns[random.nextInt(puns.length)];

            new MaterialAlertDialogBuilder(this).
                    setTitle("🌷 Your Plant Says...")
                    .setMessage(pun)
                    .setPositiveButton("Thanks! 🌸", null)
                    .show();
        });
    }
}
