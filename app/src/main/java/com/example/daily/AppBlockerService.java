package com.example.daily;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class AppBlockerService extends AccessibilityService {
    private static final String TAG = "AppBlockerService";
    private static final String CHANNEL_ID = "AppBlockerNotifications";
    public static HashSet<String> blockedApps = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleDailyReset();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();

            if (shouldBlockApp(packageName)) {
                Log.d(TAG, "Blocking app: " + packageName);
                sendBlockNotification(packageName);
                closeApp(packageName);
            }
        }
    }

    private boolean shouldBlockApp(String packageName) {
        SharedPreferences preferences = getSharedPreferences("AppLimits", Context.MODE_PRIVATE);
        int limit = preferences.getInt(packageName, 0);

        if (limit > 0) {
            long usageTime = getAppUsageTime(packageName);
            Log.d(TAG, "App: " + packageName + " | Limit: " + limit + " min | Used: " + usageTime + " min");

            if (usageTime >= limit) {
                blockedApps.add(packageName);
                return true;
            }
        }
        return blockedApps.contains(packageName);
    }

    private long getAppUsageTime(String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 1000 * 60 * 60 * 24;

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);

        if (stats != null) {
            for (UsageStats usageStats : stats) {
                if (usageStats.getPackageName().equals(packageName)) {
                    return usageStats.getTotalTimeInForeground() / 60000;
                }
            }
        }
        return 0;
    }

    private void closeApp(String packageName) {
        new Thread(() -> {
            while (shouldBlockApp(packageName)) {
                performGlobalAction(GLOBAL_ACTION_HOME);
                try {
                    Thread.sleep(1000); // Check every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendBlockNotification(String packageName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("App Blocked")
                .setContentText(packageName + " has reached its daily limit.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(2, builder.build());
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted");
    }

    private void scheduleDailyReset() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ResetLimitsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
}
