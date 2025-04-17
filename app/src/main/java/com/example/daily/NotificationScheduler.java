package com.example.daily;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
public class NotificationScheduler {

    private static void scheduleNotification(Context context, String taskId, long triggerTime, String type) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("NotificationScheduler", "AlarmManager is null!");
            return;
        }

        // Start the NotificationService before scheduling notifications
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.startService(serviceIntent);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("com.example.daily.TASK_NOTIFICATION");  // 🔹 Add this line
        intent.putExtra("task_id", taskId);
        intent.putExtra("notification_type", type);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, (taskId + type).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        Log.d("NotificationScheduler", "Notification scheduled: " + type + " at " + triggerTime);
    }





    public static void scheduleTaskNotifications(Context context, Task task) {
        Log.d("NotificationScheduler", "Scheduling notifications for: " + task.getTitle());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("NotificationScheduler", "AlarmManager is null!");
            return;
        }

        // 🔹 Reminder Notification
        long reminderTime = TaskUtils.getMillisFromTime(task.getDate(), task.getStartTime()) - (15 * 60 * 1000);
        Log.d("NotificationScheduler", "Reminder scheduled for: " + reminderTime);
        scheduleNotification(context, task.getId(), reminderTime, "REMINDER");

        // 🔹 Incomplete Notification
        long incompleteTime = TaskUtils.getMillisFromTime(task.getDate(), task.getEndTime()) + (5 * 60 * 1000);
        Log.d("NotificationScheduler", "Incomplete notification scheduled for: " + incompleteTime);
        scheduleNotification(context, task.getId(), incompleteTime, "INCOMPLETE");
    }

}
