// Package declaration - this file belongs to com.example.example2
package com.example.example2;
// Creates a category for your notifications
import android.app.NotificationChannel;
// The system service that manages and displays notifications
import android.app.NotificationManager;
// Simple key-value storage for saving small bits of data
import android.content.SharedPreferences;
// Lets you check what Android version the device is running
import android.os.Build;
// A container for passing data between activities or saving state
import android.os.Bundle;

// An annotation that marks a parameter as never being null
import androidx.annotation.NonNull;
// App compact activity is the base class for activities
// Provides backwards compatible features
import androidx.appcompat.app.AppCompatActivity;

// Used to navigate between activities
import android.content.Intent;

import android.graphics.Color;
//The base class for all UI elements
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

// Builds notifications in a backwards compatible way
import androidx.core.app.NotificationCompat;
// Sends notifications in a backwards compatible way
import androidx.core.app.NotificationManagerCompat;

// Schedules a task to run repeatedly
import androidx.work.PeriodicWorkRequest;
// Manages and runs your background tasks reliably
import androidx.work.WorkManager;
// Controls what happens if you schedule a task that already exists
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

// A snapshot of data read from Firebase at a point in time
import com.google.firebase.database.DataSnapshot;
// Represents an error that occurred when reading Firebase
import com.google.firebase.database.DatabaseError;
// A reference/pointer to a specific location in your Firebase database
import com.google.firebase.database.DatabaseReference;
// The entry point for accessing Firebase Realtime Database
import com.google.firebase.database.FirebaseDatabase;
// The bottom navigation bar with tabs
import com.google.firebase.database.ValueEventListener;

// The bottom navigation bar with tabs
import com.google.android.material.bottomnavigation.BottomNavigationView;
// Lets you play frame-by-frame animations on an ImageView
import android.graphics.drawable.AnimationDrawable;

// Creates stylish Material Design popup dialogs for plant puns
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Random;

// Intent used to navigate through activities
public class MainActivity extends AppCompatActivity{
    // MainActivity is the main screen of our app
    // extends AppCompatActivity means we inherit the functionality

    // Members - accessible through the class
    private TextView tvSensorValue;
    private TextView tvSensorStatus;
    private Button btnUpdate;
    private TextView tvReadingCount;
    private ImageView ivPlantCharacter;
    private Button btnWater;

    private int currentMoisture = -1;
    // Stores the latest reading

    // Colour constants
    // Converting hex colour to integer
    private static final int COLOR_RED = Color.parseColor("#FF6B6B");
    private static final int COLOR_YELLOW = Color.parseColor("#FFD93D");
    private static final int COLOR_GREEN = Color.parseColor("#6BCB77");
    private static final int COLOR_BLUE = Color.parseColor("#4ECDC4");

    // Threshold Constants
    public static final int THRESHOLD_LOW = 30;
    public static final int THRESHOLD_HIGH = 65;
    public static final int THRESHOLD_TOO_HIGH = 85;

    // private boolean lastPumpState = false;
    private boolean autoTriggerActive = false;
    private boolean manualOverride = false;

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

        // Method to ensure appropriate value appears to reader once app opens
        startRealtimeUpdates();

        setUpBottomNavigation();
        setUpButtonListeners();
        setUpPlantPunListener();
        updateReadingCount();
        createNotificationChannel();
        scheduleHourlyMoistureCheck();

    }
    private void scheduleHourlyMoistureCheck() {
        // Control when worker method is called
        PeriodicWorkRequest moistureCheck = new PeriodicWorkRequest.Builder(
                // Determines how often moisturecheck is called
                MoistureCheckWorker.class,
                2, TimeUnit.HOURS)
                .build();
        // Method allows you to enqueue a uniquely-named PeriodicWorkRequest
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "moistureCheck",
                // If existing work, do nothing
                ExistingPeriodicWorkPolicy.KEEP,
                moistureCheck);
    }
    // Runs every time the app comes back to the foreground i.e when device wakes from sleep
    protected void onResume() {
        super.onResume();
        // Ensure always latest reading when resuming the app
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        int lastReading = prefs.getInt("lastReading", -1);
        // Only restore if real reading, avoids -1 being displayed on initial set up
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
        btnWater = findViewById(R.id.btnWater);
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
        // Manual Watering
        btnWater.setOnClickListener(v -> {
            manualOverride = true; // Prevents auto-logic from interfering

            // Turn ON
            database.child("pump").setValue(true).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Manual watering started 💧", Toast.LENGTH_SHORT).show();

                // Wait 2 seconds (longer than ESP32 check time)
                new android.os.Handler().postDelayed(() -> {
                    // Turn OFF
                    database.child("pump").setValue(false);

                    // Release override after a small buffer
                    new android.os.Handler().postDelayed(() -> {
                        manualOverride = false;
                        // 1 second buffer
                    }, 1000);
                    // 2 second water
                }, 2000);
            });
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
        // Go to firebase and find moisture node, read once. If succesful, store in snapshot
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
                    sendNotification(moistureInt);


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
                    currentMoisture = moistureInt;

                    tvSensorValue.setText(String.valueOf(moistureInt));
                    updateValueColour(moistureInt);

                    SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                    prefs.edit().putInt("lastReading", moistureInt).apply();

                    // AUTO WATERING when checking firebase reading
                    // Get preference for settings
                    boolean automationEnabled = prefs.getBoolean("automationEnabled", false);

                    if (automationEnabled && !manualOverride) {

                        boolean shouldWater = moistureInt < 20;

                        // auto trigger prevents multiple fires
                        if (shouldWater && !autoTriggerActive) {

                            autoTriggerActive = true;

                            database.child("pump").setValue(true);
                            Toast.makeText(MainActivity.this,
                                    "Auto watering 💧", Toast.LENGTH_SHORT).show();

                            new android.os.Handler().postDelayed(() -> {
                                database.child("pump").setValue(false);
                                autoTriggerActive = false;
                            }, 1000);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void setUpPlantPunListener(){

        String happyPuns[] = {
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

        String sadPuns[] = {
                "I'm feeling a bit dry today...🥀",
                "Is anyone there!? I'm parched 🌵",
                "Water you waiting for?💧",
                "So no water again? No it's cool i'm not upset🥺",
                "Aloeee, can you hear me?🥀",
                "I'm wilting away!🥺",
                "Don't worry I'm fine (I am NOT fine😭)",
        };

        ivPlantCharacter.setOnClickListener(v -> {
            Random random = new Random();
            String pun;

            if(currentMoisture < THRESHOLD_LOW){
                pun = sadPuns[random.nextInt(sadPuns.length)];
            }
            else{
                pun = happyPuns[random.nextInt(happyPuns.length)];
            }

            // Customise response based on the moisture level
            if(currentMoisture > THRESHOLD_LOW){
            new MaterialAlertDialogBuilder(this).
                    setTitle("🌷 Your Plant Says...")
                    .setMessage(pun)
                    .setPositiveButton("Thanks! 🌸", null)
                    .show(); }
            else{
                new MaterialAlertDialogBuilder(this).
                        setTitle("🥀 Your Plant Says...")
                        .setMessage(pun)
                        .setPositiveButton("Sorry! 🙈", null)
                        .show();
            }
        });

    }
}
