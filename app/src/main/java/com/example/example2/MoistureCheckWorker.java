
package com.example.example2;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

public class MoistureCheckWorker extends Worker {

    public MoistureCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseDatabase.getInstance().getReference("sensor")
                .child("moisture").get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        float moisture = Float.parseFloat(snapshot.getValue().toString());
                        int moistureInt = (int) moisture;

                        long timestamp = System.currentTimeMillis();
                        FirebaseDatabase.getInstance().getReference("sensor")
                                .child("readings")
                                .child(String.valueOf(timestamp))
                                .setValue(moistureInt);

                        // Check count and remove oldest if over 50
                        FirebaseDatabase.getInstance().getReference("sensor/readings")
                                .get().addOnSuccessListener(readingsSnapshot -> {
                                    if (readingsSnapshot.getChildrenCount() > 50) {
                                        // First child is oldest since keys are timestamps
                                        DataSnapshot oldest = readingsSnapshot.getChildren().iterator().next();
                                        oldest.getRef().removeValue();
                                    }
                                });
                    }
                });
        return Result.success();
    }
}
