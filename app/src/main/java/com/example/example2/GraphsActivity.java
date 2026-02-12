package com.example.example2;

import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.widget.TextView;

import com.github.mikephil.charting.*;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GraphsActivity extends AppCompatActivity {

    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;
    private TextView tvDataInfo;

    private static final int COLOR_RED = Color.parseColor("#E74C3C");
    private static final int COLOR_YELLOW = Color.parseColor("#F39C12");
    private static final int COLOR_GREEN = Color.parseColor("#27AE60");
    private static final int COLOR_BLUE = Color.parseColor("#3498DB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);
        initializeViews();
        setUpBottomNavigation();
        loadCharts();

    }
    @Override
    // Refresh charts when returning to screen
    protected  void onResume(){
        super.onResume();
        loadCharts();
    }
    private void loadCharts(){
        List<SensorReading> readings = DataManager.getInstance().getAllReadings();
        if(readings.isEmpty()){
            tvDataInfo.setText("No data yet - add readings");
        }
        else{
            tvDataInfo.setText("Showing " + readings.size() + " readings");
        }
        setupLineChart(readings);
        setupBarChart(readings);
        setupPieChart(readings);
    }
    private void  setupLineChart(List<SensorReading> readings){
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        for(int i = 0;i < readings.size();i++){
            SensorReading reading = readings.get(i);
            entries.add(new Entry(i, reading.getValue()));
            // Create time label for this point
            labels.add(sdf.format(new Date(reading.getTimestamp())));
        }
        if(entries.isEmpty()){
            lineChart.clear();
            lineChart.setNoDataText("No data availible");
            // Refresh the chart
            lineChart.invalidate();
            return;
        }
        // Create dataset from entries
        LineDataSet dataset = new LineDataSet(entries, "Sensor Values");

        // Customise appearance
        dataset.setColor(COLOR_BLUE); // Line colour
        dataset.setCircleColor(COLOR_BLUE); // Point colour
        dataset.setLineWidth(2f); // Line thickness
        dataset.setCircleRadius(4f); // Point size
        dataset.setDrawValues(true); // Show values on points
        dataset.setValueTextSize(10f); // Value text size
        dataset.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curve

        LineData lineData = new LineData(dataset);
        // Configure x Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set labels at bottom
        xAxis.setGranularity(1f); // Min interval
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Uses label array for x axis
        xAxis.setLabelRotationAngle(-45f); // Rotate labels -> readability

        //Configure Chart
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false); // Hide description
        lineChart.getLegend().setEnabled(true); // Show legend
        lineChart.setTouchEnabled(true); // Allow touch
        lineChart.setDragEnabled(true); // Allow drag
        lineChart.setScaleEnabled(true); // Allow zoom

        // Resfresh to show changes
        lineChart.invalidate();
    }
    private void  setupBarChart(List<SensorReading> readings){
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        int startIndex = Math.max(0, readings.size()-10);
        int barIndex = 0;

        for(int i = startIndex;i < readings.size();i++){
            SensorReading reading = readings.get(i);
            entries.add(new BarEntry(barIndex, reading.getValue()));
            // Create time label for this point
            labels.add(sdf.format(new Date(reading.getTimestamp())));

            if(reading.getValue()<30){
                colors.add(COLOR_RED);
            }
            else if(reading.getValue()<=65){
                colors.add(COLOR_YELLOW);
            }
            else{
                colors.add(COLOR_GREEN);
            }
            barIndex++;
        }

        if(entries.isEmpty()){
            barChart.clear();
            barChart.setNoDataText("No data availible");
            // Refresh the chart
            barChart.invalidate();
            return;
        }

        // Create dataset from entries
        BarDataSet dataset = new BarDataSet(entries, " Recent values");

        // Customise appearance
        dataset.setColors(colors); // Line colour
        dataset.setValueTextSize(10f);

        BarData barData = new BarData(dataset);
        barData.setBarWidth(0.8f);

        // Configure x Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set labels at bottom
        xAxis.setGranularity(1f); // Min interval
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Uses label array for x axis
        xAxis.setLabelRotationAngle(-45f); // Rotate labels -> readability

        //Configure Chart
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false); // Hide description
        barChart.getLegend().setEnabled(false); // Show legend
        barChart.setFitBars(true);

        // Resfresh to show changes
        barChart.invalidate();
    }
    private void setupPieChart(List<SensorReading> readings){
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        int lowCount = DataManager.getInstance().countByStatus("Low");
        int mediumCount = DataManager.getInstance().countByStatus("Medium");
        int highCount = DataManager.getInstance().countByStatus("High");

        if(lowCount>0){
            entries.add(new PieEntry(lowCount, "Low"));
            colors.add(COLOR_RED);
        }
        if(mediumCount>0){
            entries.add(new PieEntry(mediumCount, "Medium"));
            colors.add(COLOR_YELLOW);
        }
        if(highCount>0){
            entries.add(new PieEntry(mediumCount, "High"));
            colors.add(COLOR_GREEN);
        }
        if(entries.isEmpty()){
            pieChart.clear();
            pieChart.setNoDataText("No data available");
            pieChart.invalidate();
            return;
        }
        PieDataSet dataSet = new PieDataSet(entries, "Distribution");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f); // Space between slices
        // Create Pie Data
        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setCenterText("Status\nDistribution");

        pieChart.setCenterTextSize(12f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.getLegend().setEnabled(true);

        pieChart.invalidate();

    }
    private void initializeViews(){
        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        tvDataInfo = findViewById(R.id.tvDataInfo);
    }
    private void setUpBottomNavigation() {
        // Find bottom navigation in layout
        // findViewById searches layout for ID
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        // Mark home tab as selected
        bottomNav.setSelectedItemId(R.id.nav_graphs);
        // Set up listener for navigation clicks
        bottomNav.setOnItemSelectedListener(item -> {
            // LAMBDA expression, runs when tab is selected
            // item is the menu item that is clicked

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navigateToActivity(MainActivity.class);
                return true;
            } else if (itemId == R.id.nav_graphs) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Navigate to settings screen
                navigateToActivity(SettingsActivity.class);
                return true;
            }
            return false;

        });
    }
    private void navigateToActivity(Class<?> activityClass){
        Intent intent = new Intent(GraphsActivity.this, activityClass);
        startActivity(intent);
        finish();
    }

}