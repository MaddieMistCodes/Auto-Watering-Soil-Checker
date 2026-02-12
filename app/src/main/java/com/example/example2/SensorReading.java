package com.example.example2;

public class SensorReading {
    private int value;
    private long timestamp;

    public SensorReading(int value, long timestamp){
        this.value = value;
        this.timestamp = timestamp;
    }
    public int getValue(){
        return value;
    }
    public long getTimestamp(){
        return timestamp;
    }
    public String getStatus(){
        if (value < 30){
            return "Low";
        }
        else if (value <= 65){
            return "Medium";
        }
        else{
            return "High";
        }
    }
}
