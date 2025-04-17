//package com.example.daily;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.app.usage.UsageStats;
//import android.app.usage.UsageStatsManager;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Handler;
//import android.util.Log;
//import java.util.Calendar;
//import java.util.List;
//
//public class AppUsageService {
//    private Context context;
//    private Handler handler = new Handler();
//    private SharedPreferences preferences;
//
//    public AppUsageService(Context context) {
//        this.context = context;
//        this.preferences = context.getSharedPreferences("AppLimits", Context.MODE_PRIVATE);
//        scheduleDailyReset();
//    }
//
//    public void startMonitoring() {
//        handler.post(checkAppUsage);
//    }
//
//    private Runnable checkAppUsage = new Runnable() {
//        @Override
//        public void run() {
//            checkUsageStats();
//            handler.postDelayed(this, 10000); // Check every 10 seconds
//        }
//    };
//
//    private void checkUsageStats() {
//        new Thread(() -> {
//            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//            if (usageStatsManager != null) {
//                long endTime = System.currentTimeMillis();
//                long startTime = endTime - (1000 * 60 * 60 * 24); // Last 24 hours
//
//                List<UsageStats> stats = usageStatsManager.queryUsageStats(
//                        UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
//
//                for (UsageStats stat : stats) {
//                    String packageName = stat.getPackageName();
//                    long usageTime = stat.getTotalTimeInForeground() / 60000; // Convert to minutes
//                    int limit = preferences.getInt(packageName, 0);
//
//                    if (limit > 0 && usageTime >= limit) {
//                        Log.d("AppUsageService", "Blocking app: " + packageName);
//                        AppBlockerService.blockedApps.add(packageName);
//                    }
//                }
//            }
//        }).start();
//    }
//
//    // Reset limits at midnight
//    private void scheduleDailyReset() {
//        Intent intent = new Intent(context, ResetLimitsReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
//        );
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//
//        if (alarmManager != null) {
//            alarmManager.setRepeating(
//                    AlarmManager.RTC_WAKEUP,
//                    calendar.getTimeInMillis(),
//                    AlarmManager.INTERVAL_DAY,
//                    pendingIntent
//            );
//        }
//    }
//}

package com.example.daily;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppUsageService {
    private Context context;
    private Handler handler = new Handler();
    private SharedPreferences preferences;
    private static final String BLOCKED_APPS_KEY = "BlockedApps";

    public AppUsageService(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("AppLimits", Context.MODE_PRIVATE);
        scheduleDailyReset();
    }

    public void startMonitoring() {
        handler.post(checkAppUsage);
    }

    private Runnable checkAppUsage = new Runnable() {
        @Override
        public void run() {
            checkUsageStats();
            handler.postDelayed(this, 60000); // Check every 60 seconds instead of 10s
        }
    };

    private void checkUsageStats() {
        new Thread(() -> {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager != null) {
                long endTime = System.currentTimeMillis();
                long startTime = endTime - (1000 * 60 * 60 * 24); // Last 24 hours

                List<UsageStats> stats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

                SharedPreferences.Editor editor = preferences.edit();
                Set<String> blockedApps = preferences.getStringSet(BLOCKED_APPS_KEY, new HashSet<>());

                for (UsageStats stat : stats) {
                    String packageName = stat.getPackageName();
                    long usageTime = stat.getTotalTimeInForeground() / 60000; // Convert to minutes
                    int limit = preferences.getInt(packageName, 0);

                    if (limit > 0 && usageTime >= limit) {
                        Log.d("AppUsageService", "Blocking app: " + packageName);
                        blockedApps.add(packageName);
                    }
                }

                editor.putStringSet(BLOCKED_APPS_KEY, blockedApps);
                editor.apply();
            }
        }).start();
    }

    public boolean isAppBlocked(String packageName) {
        Set<String> blockedApps = preferences.getStringSet(BLOCKED_APPS_KEY, new HashSet<>());
        return blockedApps.contains(packageName);
    }

    // Reset limits at midnight
    private void scheduleDailyReset() {
        Intent intent = new Intent(context, ResetLimitsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }
}
