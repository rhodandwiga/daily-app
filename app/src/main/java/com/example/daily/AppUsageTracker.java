//package com.example.daily;
//
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.util.Log;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Map;
//
//public class AppUsageTracker {
//    private Context context;
//    private SharedPreferences sharedPreferences;
//    private static final String PREFS_NAME = "AppLimits";
//    private static final String LAST_RESET_KEY = "LastResetTime";
//
//    public AppUsageTracker(Context context) {
//        this.context = context;
//        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//    }
//
//    public void checkAppUsage() {
//        resetUsageIfNeeded(); // Ensure reset happens if 24 hours have passed
//
//        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//        Calendar calendar = Calendar.getInstance();
//        long endTime = calendar.getTimeInMillis();
//        calendar.add(Calendar.DAY_OF_MONTH, -1);
//        long startTime = calendar.getTimeInMillis();
//
//        Map<String, ?> limits = sharedPreferences.getAll();
//
//        if (usageStatsManager != null) {
//            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
//            for (UsageStats usageStats : stats) {
//                String packageName = usageStats.getPackageName();
//                long usageTimeInMillis = usageStats.getTotalTimeInForeground();
//
//                if (limits.containsKey(packageName)) {
//                    int maxAllowedTime = Integer.parseInt(limits.get(packageName).toString()) * 60 * 1000;
//
//                    if (usageTimeInMillis >= maxAllowedTime) {
//                        Log.d("AppUsageTracker", "Blocking app: " + packageName);
//                        AppBlockerService.blockedApps.add(packageName);
//                    }
//                }
//            }
//        }
//    }
//
//    private void resetUsageIfNeeded() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        long lastResetTime = sharedPreferences.getLong(LAST_RESET_KEY, 0);
//        long currentTime = System.currentTimeMillis();
//
//        // Check if 24 hours have passed since the last reset
//        if (currentTime - lastResetTime >= 24 * 60 * 60 * 1000) {
//            Log.d("AppUsageTracker", "Resetting app usage tracking...");
//
//            // Clear all saved limits and reset timestamp
//            editor.clear();
//            editor.putLong(LAST_RESET_KEY, currentTime);
//            editor.apply();
//        }
//    }
//
//    public void setAppLimit(String packageName, int minutes) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(packageName, minutes);
//        editor.apply();
//    }
//
//    public int getAppLimit(String packageName) {
//        return sharedPreferences.getInt(packageName, 0);
//    }
//
//    public long getAppUsageTime(String packageName) {
//        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//        long currentTime = System.currentTimeMillis();
//        long startTime = currentTime - (24 * 60 * 60 * 1000);
//
//        if (usageStatsManager != null) {
//            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);
//            for (UsageStats usageStats : stats) {
//                if (usageStats.getPackageName().equals(packageName)) {
//                    return usageStats.getTotalTimeInForeground();
//                }
//            }
//        }
//        return 0;
//    }
//}


package com.example.daily;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AppUsageTracker {
    private Context context;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppLimits";
    private static final String LAST_RESET_KEY = "LastResetTime";

    public AppUsageTracker(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void checkAppUsage() {
        resetUsageIfNeeded(); // Ensure reset happens if 24 hours have passed

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - (24 * 60 * 60 * 1000); // Last 24 hours

        Map<String, ?> limits = sharedPreferences.getAll();

        if (usageStatsManager != null) {
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);
            for (UsageStats usageStats : stats) {
                String packageName = usageStats.getPackageName();
                long usageTimeInMillis = usageStats.getTotalTimeInForeground();

                if (limits.containsKey(packageName)) {
                    int maxAllowedTime = Integer.parseInt(limits.get(packageName).toString()) * 60 * 1000;

                    if (usageTimeInMillis >= maxAllowedTime) {
                        Log.d("AppUsageTracker", "Blocking app: " + packageName);
                        blockApp(packageName);
                    }
                }
            }
        }
    }

    private void resetUsageIfNeeded() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        long lastResetTime = sharedPreferences.getLong(LAST_RESET_KEY, 0);
        long currentTime = System.currentTimeMillis();

        // Reset only the last reset timestamp, not the app limits
        if (currentTime - lastResetTime >= 24 * 60 * 60 * 1000) {
            Log.d("AppUsageTracker", "Resetting app usage tracking...");
            editor.putLong(LAST_RESET_KEY, currentTime);
            editor.apply();
        }
    }

    private void blockApp(String packageName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("blocked_" + packageName, true);
        editor.apply();
    }

    public boolean isAppBlocked(String packageName) {
        return sharedPreferences.getBoolean("blocked_" + packageName, false);
    }

    public void setAppLimit(String packageName, int minutes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(packageName, minutes);
        editor.apply();
    }

    public int getAppLimit(String packageName) {
        return sharedPreferences.getInt(packageName, 0);
    }

    public long getAppUsageTime(String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - (24 * 60 * 60 * 1000);

        if (usageStatsManager != null) {
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);
            for (UsageStats usageStats : stats) {
                if (usageStats.getPackageName().equals(packageName)) {
                    return usageStats.getTotalTimeInForeground();
                }
            }
        }
        return 0;
    }
}
