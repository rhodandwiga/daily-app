package com.example.daily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification broadcast received: " + intent.getAction());

        if (intent.getAction() != null && intent.getAction().equals("com.example.daily.TASK_NOTIFICATION")) {
            String taskId = intent.getStringExtra("task_id");
            String type = intent.getStringExtra("notification_type");

            Log.d(TAG, "Processing notification for task: " + taskId + ", type: " + type);

            // Create notification content based on type
            String title = "Task Notification";
            String message = "You have a task to complete";

            if ("REMINDER".equals(type)) {
                title = "Task Starting Soon";
                message = "Your scheduled task will begin in 15 minutes";
            } else if ("INCOMPLETE".equals(type)) {
                title = "Task Incomplete";
                message = "Did you complete your scheduled task?";
            }

            // Start the service and send the notification
            Intent serviceIntent = new Intent(context, NotificationService.class);
            serviceIntent.putExtra("notification_title", title);
            serviceIntent.putExtra("notification_message", message);

            Log.d(TAG, "Starting NotificationService with title: " + title);

            // For Android 8.0+, we should use startForegroundService
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}