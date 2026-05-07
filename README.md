# IoT Auto Watering System
This is an IoT project that uses the ESP32 and Google Firebase to enable remote communication. A watering circuit for the plant is controlled by the soil readings and the Android Studio app. 

## 📌Features
• Wifi Connectivity - ESP32 is used for internet access   
• Sensor Data Collection - reading in moisture levels from capacative moisture sensor.  
• Data Uploads to a RTDB - Pushes sensor readings to RTDB and handles Wi-Fi reconnections/Firebase disconnects  
• Data Security - Uses Firebase Authenticatin (API Key)  
• Data Retrieval - Java app pulls readings from firebase and displays on the app  
• Auto Watering - Submersible pump auto waters based on firebase boolan  
• CSV Downloads - last 100 readings can be exported to downlaods on phone  
• Character animation - Custom plant character expresses moisture level in expressions   
• User Interactivity - Plant character will give happy/sad plant puns based on moisture level  
• Data Visualistation - Activtiy dedicated to graphs to show data over time  
• Background Readings - WorkManager library used to achieve the app pull sensor readings in the background  
• Customisation - User can choose the moisture threshold for notifications, user can also enable/disable auto watering  

## 📌Tech Stack
### Device (ESP32)
• ESP32 microcontroller  
• Arduino framework (C/C++)  
• Firebase-ESP-Client library  
• Wi-Fi + sensor libraries  

### Cloud (Backend)
• Firebase Realtime Database (RTDB)  
• Firebase Authentication (optional)  

### Mobile App (Frontend)
• Android (Java)  
• Firebase Android SDK (RTDB, Auth)  

### Tools
• Arduino IDE  
• Android Studio  

## 📌Future Improvemnts
• Improve data visualisation where user can choose to view data over 1,5,102,20 days  
• Use a weather API to give user custom feedback on watering the plant based on weather  
• Use a temperature sensor to enhance user insight  
