package com.example.daily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Device rebooted, restarting AppUsageService...");

            // Get SharedPreferences from the context
            SharedPreferences sharedPreferences = context.getSharedPreferences("DailyPulseTasks", Context.MODE_PRIVATE);

            // Now you can pass SharedPreferences to TaskUtils
            for (Task task : TaskUtils.getAllTasks(sharedPreferences)) {
                // Your code to handle tasks
                Log.d("BootReceiver", "Task: " + task.getTitle());
            }

            // Starting the AppUsageService as intended
            Intent serviceIntent = new Intent(context, AppUsageService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
