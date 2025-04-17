package com.example.daily;

import android.widget.Spinner;
import android.widget.AdapterView;
import android.view.View;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class UsageStatsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UsageStatsAdapter adapter;
    private List<UsageStatsModel> usageList = new ArrayList<>();
    private BarChart usageBarChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats);

        recyclerView = findViewById(R.id.usageStatsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usageBarChart = findViewById(R.id.usageBarChart);
        setupChart(); // Configure the graph

        Spinner intervalSpinner = findViewById(R.id.intervalSpinner);
        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int intervalType;
                switch (position) {
                    case 0: intervalType = UsageStatsManager.INTERVAL_DAILY; break;
                    case 1: intervalType = UsageStatsManager.INTERVAL_WEEKLY; break;
                    case 2: intervalType = UsageStatsManager.INTERVAL_MONTHLY; break;
                    default: intervalType = UsageStatsManager.INTERVAL_DAILY; break;
                }
                fetchUsageStats(intervalType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        } else {
            fetchUsageStats(UsageStatsManager.INTERVAL_DAILY); // Default to Daily
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Toast.makeText(this, "Please grant usage access permission", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private void fetchUsageStats(int intervalType) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            Toast.makeText(this, "Usage Stats Not Available", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        // Set start time based on interval type
        switch (intervalType) {
            case UsageStatsManager.INTERVAL_DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case UsageStatsManager.INTERVAL_WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case UsageStatsManager.INTERVAL_MONTHLY:
                calendar.add(Calendar.MONTH, -1);
                break;
            default:
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
        }
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(intervalType, startTime, endTime);
        if (stats == null || stats.isEmpty()) {
            Toast.makeText(this, "No usage data available. Enable Usage Access.", Toast.LENGTH_LONG).show();
            return;
        }

        usageList.clear();
        HashMap<String, Long> usageMap = new HashMap<>();

        // Aggregate total time for each unique package name
        for (UsageStats stat : stats) {
            if (stat.getTotalTimeInForeground() > 0) {
                String packageName = stat.getPackageName();
                long usageTime = stat.getTotalTimeInForeground();

                if (usageMap.containsKey(packageName)) {
                    usageMap.put(packageName, usageMap.get(packageName) + usageTime);
                } else {
                    usageMap.put(packageName, usageTime);
                }
            }
        }

        // Convert aggregated data to list
        for (String packageName : usageMap.keySet()) {
            usageList.add(new UsageStatsModel(packageName, usageMap.get(packageName)));
        }

        // Sort apps by usage time (descending)
        Collections.sort(usageList, (a, b) -> Long.compare(b.getUsageTime(), a.getUsageTime()));

        // Keep only the top 5 apps
        if (usageList.size() > 5) {
            usageList = usageList.subList(0, 5);
        }

        adapter = new UsageStatsAdapter(this, usageList);
        recyclerView.setAdapter(adapter);

        updateChart();
    }

    private void setupChart() {
        usageBarChart.getDescription().setEnabled(false);
        usageBarChart.setDrawGridBackground(false);
        XAxis xAxis = usageBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = usageBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        usageBarChart.getAxisRight().setEnabled(false);
    }

    private void updateChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < usageList.size(); i++) {
            entries.add(new BarEntry(i, usageList.get(i).getUsageTime() / (1000 * 60))); // Convert to minutes
            labels.add(getShortAppName(usageList.get(i).getPackageName())); // Shorten app name
        }

        BarDataSet dataSet = new BarDataSet(entries, "App Usage (Minutes)");
        BarData barData = new BarData(dataSet);
        usageBarChart.setData(barData);

        XAxis xAxis = usageBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(0); // Prevents overlap

        usageBarChart.invalidate(); // Refresh chart
    }

    // Function to extract first 3 letters of app name for labels
    private String getShortAppName(String packageName) {
        String[] parts = packageName.split("\\.");
        if (parts.length > 1) {
            String name = parts[parts.length - 1]; // Get last part of package
            return name.length() > 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase();
        }
        return packageName;
    }
}
