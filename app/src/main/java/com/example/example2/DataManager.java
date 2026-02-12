package com.example.example2;

import java.util.ArrayList;
import java.util.List;
public class DataManager {
    private static DataManager instance;
    private List<SensorReading> readings;

    private DataManager(){
        readings = new ArrayList<>();
    }

    public static DataManager getInstance(){
        if(instance == null){
            instance = new DataManager();
        }
        return instance;
    }
    public void addReading(int value){
        long timestamp = System.currentTimeMillis();

        SensorReading reading = new SensorReading(value, timestamp);
        readings.add(reading);
    }
    public List<SensorReading> getAllReadings(){
        return readings;
    }
    public int getReadingCount(){
        return readings.size();
    }
    public void clearReadings(){
       readings.clear();
    }
    public int countByStatus(String status){
        int count = 0;
        for(SensorReading reading : readings){
            if(reading.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

}
