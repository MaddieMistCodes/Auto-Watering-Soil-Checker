# IoT Auto Watering System
This is an IoT project that uses the ESP32 and Google Firebase to enable remote communication. A watering circuit for the plant is controlled by the soil readings and the Android Studio app. 

## 🌼Features
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

## 🌼Tech Stack
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

## 🌼App Features
### Visualisation
On the second activity there are three graphs. A line chart, bar chart and pie chart. The moisture reading is sensed roughly every two hours depending on phone settings. This will automatically show the last ten readings. 

<img width="250" height="600" alt="Screenshot_20260307-143505" src="https://github.com/user-attachments/assets/8a223b4f-59f4-41c2-a6ba-569f3b4c25d2" />

### The Plant
The self-designed plant character is designed to enhance user expperience. It runs on an animation cycle to give the plant life. Depending on the soil moisture, the plant will be unhappy, okay, happy and over-watered. To further enhance the experience, if the plant is tocuhed when happy - it will tell the user a plant pun!

<img width="250" height="600" alt="Screenshot_20260325-123054" src="https://github.com/user-attachments/assets/7e1acf5d-ff56-42d9-ac9b-d24e8d86e270" />

Similarily, if the plant is unhappy - it will inform you with unhappy plant puns/comments.

<img width="250" height="600" alt="Screenshot_20260327-114310 (1)" src="https://github.com/user-attachments/assets/9b776b84-92ea-4966-ab30-7718dd9ab168" />

## 🌼The Circuit
The circuit was set up using the following components:
• ESP32  
• Submersible Water Pump  
• Capacitive Soil Moisture Sensor  
• 5V Power Supply  
• 4 AA Batteries (for Pump)  
• 5V Relay  
A relay is necessary for this project. The pump draws too much current to be powered by the ESP32 alone, so a relay was used to ensure circuit seperation and was powered with 4 AA bateries. The below was the home setup.

<img width="1000" height="750" alt="PXL_20260410_161819610" src="https://github.com/user-attachments/assets/dbce56b9-e9be-41a3-809a-ebbb146cac28" />

## 🌼Future Improvemnts
• Improve data visualisation where user can choose to view data over 1,5,102,20 days  
• Use a weather API to give user custom feedback on watering the plant based on weather  
• Use a temperature sensor to enhance user insight  
• Create 3D parts to make the physical set-up neater/more usable
