package com.example.daily;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InstalledAppsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppListAdapter appListAdapter;
    private List<AppInfo> appList;
    private UsageStatsManager usageStatsManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed_apps);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);

        // Request permission if not granted
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        }

        loadInstalledApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload app list when returning from settings
        if (hasUsageStatsPermission()) {
            loadInstalledApps();
        }
    }

    // Function to get and display installed apps
    private void loadInstalledApps() {
        appList = getInstalledApps();

        if (appList.isEmpty()) {
            Toast.makeText(this, "No installed apps found!", Toast.LENGTH_SHORT).show();
        } else {
            appListAdapter = new AppListAdapter(this, appList);
            recyclerView.setAdapter(appListAdapter);
        }
    }

    // Function to get the list of installed apps with usage time
    private List<AppInfo> getInstalledApps() {
        List<AppInfo> apps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : packages) {
            // Check if the app is NOT a system app
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String appName = pm.getApplicationLabel(appInfo).toString();
                String packageName = appInfo.packageName;
                Drawable icon = pm.getApplicationIcon(appInfo);
                long usageTime = getAppUsageTime(packageName);

                // Add only user-installed apps
                apps.add(new AppInfo(appName, packageName, icon, usageTime));
            }
        }
        return apps;
    }


    // Function to get app usage time in minutes
    private long getAppUsageTime(String packageName) {
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - (1000 * 60 * 60 * 24); // Last 24 hours

        Map<String, UsageStats> statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, currentTime);

        if (statsMap.containsKey(packageName)) {
            return statsMap.get(packageName).getTotalTimeInForeground() / 60000; // Convert ms to minutes
        }
        return 0;
    }

    // Check if usage stats permission is granted
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(), getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // Request user to grant usage access permission
    private void requestUsageStatsPermission() {
        Toast.makeText(this, "Please enable Usage Access Permission", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }
}
