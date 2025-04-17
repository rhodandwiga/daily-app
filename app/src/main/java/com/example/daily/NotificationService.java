package com.example.daily;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "AppBlockerNotifications";
    private static final int FOREGROUND_ID = 1001;
    private static int notificationId = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NotificationService created");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "NotificationService started with intent: " + (intent != null ? "valid" : "null"));

        // For Android 8.0+, we need to start as a foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Task Manager")
                    .setContentText("Managing your tasks")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            Log.d(TAG, "Starting as foreground service");
            startForeground(FOREGROUND_ID, notification);
        }

        // Check if we have notification data in the intent
        if (intent != null) {
            String title = intent.getStringExtra("notification_title");
            String message = intent.getStringExtra("notification_message");

            if (title != null && message != null) {
                Log.d(TAG, "Sending notification: " + title);
                sendNotification(title, message);
            } else {
                Log.e(TAG, "Missing notification data in intent");
            }
        }

        return START_STICKY;
    }

    public void sendNotification(String title, String message) {
        Log.d(TAG, "Building notification: " + title);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] { 0, 250, 250, 250 })
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // Use incrementing ID to ensure multiple notifications show up
            manager.notify(notificationId++, builder.build());
            Log.d(TAG, "Notification sent with ID: " + (notificationId-1));
        } else {
            Log.e(TAG, "NotificationManager is null");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Blocker Alerts",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] { 0, 250, 250, 250 });

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            } else {
                Log.e(TAG, "NotificationManager is null when creating channel");
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}