package com.example.daily;

import java.util.UUID;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnTaskActionListener {

    private static final int REQUEST_CODE_ADD_TASK = 1;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 123;
    private static final String TAG = "MainActivity";

    private RecyclerView trackedAppsRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private Button btnViewUsageStats, btnResetStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackedAppsRecyclerView = findViewById(R.id.trackedAppsRecyclerView);
        FloatingActionButton addTaskButton = findViewById(R.id.addTaskButton);
        Button manageAppsButton = findViewById(R.id.btnViewInstalledApps);
        btnViewUsageStats = findViewById(R.id.btnViewUsageStats);
        btnResetStats = findViewById(R.id.btnResetStats);

        // Set up notification testing with long-click on an existing button
        if (btnResetStats != null) {
            btnResetStats.setOnLongClickListener(v -> {
                sendTestNotification();
                Toast.makeText(MainActivity.this, "Long press detected - sending test notification", Toast.LENGTH_SHORT).show();
                return true; // Consume the long click
            });
            Log.d(TAG, "Long-click listener set up for notification testing");
        } else {
            Log.e(TAG, "Reset stats button not found");
        }

        sharedPreferences = getSharedPreferences("DailyPulseTasks", MODE_PRIVATE);
        loadTasks();

        if (!isAccessibilityEnabled()) {
            showEnableAccessibilityDialog();
        }

        // Request notification permissions for Android 13+
        requestNotificationPermission();

        // Request battery optimization exemption
        requestBatteryOptimizationExemption();

        manageAppsButton.setOnClickListener(v -> startActivity(new Intent(this, InstalledAppsActivity.class)));

        addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_TASK);
        });

        btnViewUsageStats.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UsageStatsActivity.class)));

        btnResetStats.setOnClickListener(v -> resetUsageStats());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting notification permission for Android 13+");
                ActivityCompat.requestPermissions(this,
                        new String[] { android.Manifest.permission.POST_NOTIFICATIONS },
                        REQUEST_NOTIFICATION_PERMISSION);
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        }
    }

    private void requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            String packageName = getPackageName();
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.d(TAG, "Requesting battery optimization exemption");
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            } else {
                Log.d(TAG, "Battery optimization already disabled for app");
            }
        }
    }

    private void sendTestNotification() {
        Log.d(TAG, "Sending test notification");
        Toast.makeText(this, "Sending test notification...", Toast.LENGTH_SHORT).show();

        // Method 1: Direct through NotificationService
        Intent serviceIntent = new Intent(this, NotificationService.class);
        serviceIntent.putExtra("notification_title", "Test Notification");
        serviceIntent.putExtra("notification_message", "This is a test notification from MainActivity");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Starting foreground service for notification");
            startForegroundService(serviceIntent);
        } else {
            Log.d(TAG, "Starting service for notification");
            startService(serviceIntent);
        }

        // Method 2: Through NotificationReceiver (simulating alarm trigger)
        Log.d(TAG, "Sending broadcast to NotificationReceiver");
        Intent receiverIntent = new Intent(this, NotificationReceiver.class);
        receiverIntent.setAction("com.example.daily.TASK_NOTIFICATION");
        receiverIntent.putExtra("task_id", "test_task");
        receiverIntent.putExtra("notification_type", "TEST");
        sendBroadcast(receiverIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Notification permission denied");
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == RESULT_OK) {
            if (data == null || !data.hasExtra("taskTitle") || !data.hasExtra("taskDate")) {
                Toast.makeText(this, "Error: Task data is missing!", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "onActivityResult: Missing task data!");
                return;
            }

            String title = data.getStringExtra("taskTitle");
            String description = data.getStringExtra("taskDescription") != null ? data.getStringExtra("taskDescription") : "";
            String date = data.getStringExtra("taskDate");
            String startTime = data.getStringExtra("taskStartTime") != null ? data.getStringExtra("taskStartTime") : "";
            String endTime = data.getStringExtra("taskEndTime") != null ? data.getStringExtra("taskEndTime") : "";
            String repeat = data.getStringExtra("taskRepeat") != null ? data.getStringExtra("taskRepeat") : "";

            if (title == null || title.isEmpty() || date == null || date.isEmpty()) {
                Toast.makeText(this, "Task title and date cannot be empty!", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "onActivityResult: Task title or date is empty!");
                return;
            }

            String taskId = UUID.randomUUID().toString();
            Task newTask = new Task(taskId, title, description, date, startTime, endTime, repeat, false);
            taskList.add(newTask);

            // Schedule notifications for the new task
            NotificationScheduler.scheduleTaskNotifications(this, newTask);

            saveTasks();
        }
    }

    private void loadTasks() {
        taskList = TaskUtils.getAllTasks(sharedPreferences);
        if (taskList == null) {
            taskList = new ArrayList<>();
        }

        Log.d("MainActivity", "Loaded tasks count: " + taskList.size());

        // ✅ Improved Date Sorting
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Collections.sort(taskList, (t1, t2) -> {
            try {
                Date date1 = sdf.parse(t1.getDate());
                Date date2 = sdf.parse(t2.getDate());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        taskAdapter = new TaskAdapter(taskList, this);
        trackedAppsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trackedAppsRecyclerView.setAdapter(taskAdapter);
    }

    private void saveTasks() {
        TaskUtils.saveAllTasks(sharedPreferences, taskList);
        Log.d("MainActivity", "Tasks saved successfully!");
        taskAdapter.notifyDataSetChanged(); // 🔥 Ensure UI updates after saving
    }

    @Override
    public void onTaskAction(Task task) {
        Toast.makeText(this, "Task selected: " + task.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private boolean isAccessibilityEnabled() {
        String id = getPackageName() + "/com.example.daily.AppBlockerService";
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> services = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo service : services) {
            if (service.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void showEnableAccessibilityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable App Blocker");
        builder.setMessage("To block apps, please enable the Accessibility Service.");

        builder.setPositiveButton("Go to Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void resetUsageStats() {
        SharedPreferences appLimits = getSharedPreferences("AppLimits", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = appLimits.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(this, "Usage stats reset!", Toast.LENGTH_SHORT).show();
    }
}