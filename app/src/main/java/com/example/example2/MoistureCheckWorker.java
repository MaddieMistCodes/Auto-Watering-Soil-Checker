
package com.example.example2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MoistureCheckWorker extends Worker {

    public MoistureCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Create notification channel
        createNotificationChannel();
        // Allows one or more threads to wait until a set of operations being performed in other threads completes.
        CountDownLatch latch = new CountDownLatch(1);

        // Get firebase reading in background
        FirebaseDatabase.getInstance().getReference("sensor")
                .child("moisture").get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        float moisture = Float.parseFloat(snapshot.getValue().toString());
                        int moistureInt = (int) moisture;

                        // Save reading with current timestamp
                        long timestamp = System.currentTimeMillis();
                        FirebaseDatabase.getInstance().getReference("sensor")
                                .child("readings")
                                .child(String.valueOf(timestamp))
                                .setValue(moistureInt);

                        /*
                        // Keep only 100 readings, delete oldest if over
                        FirebaseDatabase.getInstance().getReference("sensor/readings")
                                .get().addOnSuccessListener(readingsSnapshot -> {
                                    long count = readingsSnapshot.getChildrenCount();
                                    for (DataSnapshot child : readingsSnapshot.getChildren()) {
                                        if (count <= 100) break;
                                        child.getRef().removeValue();
                                        count--;
                                    }
                                    latch.countDown();
                                }).addOnFailureListener(e -> latch.countDown()); */

                        // Check SharedPreferences for notification settings - has no access to the UI
                        // So must know if can send notifications
                        SharedPreferences prefs = getApplicationContext()
                                .getSharedPreferences("MyApp", Context.MODE_PRIVATE);
                        boolean notificationsEnabled = prefs.getBoolean("notificationsEnabled", false);
                        int threshold = prefs.getInt("threshold", 30);

                        // Send notification if enabled and moisture is below threshold
                        if (notificationsEnabled && moistureInt < threshold) {
                            // Send notification
                            sendNotification(moistureInt);
                        }

                    } else {
                        latch.countDown(); // No data found, signal done
                    }
                }).addOnFailureListener(e -> latch.countDown()); // Failure, signal done

        try {
            // Wait up to 10 seconds for Firebase to respond
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "moisture_channel",
                    "Moisture_Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alerts when moisture drops below a certain threshold");
            NotificationManager manager = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(int moisture) {
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getApplicationContext().checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return; // Permission not granted, skip notification
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), "moisture_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Low Moisture Alert")
                .setContentText("Moisture is at " + moisture + "%, water the plant!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(1, builder.build());
    }
}
