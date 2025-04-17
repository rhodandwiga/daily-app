package com.example.daily;

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TaskUtils {
    private static final String TASKS_KEY = "tasks";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ✅ Retrieve all tasks from SharedPreferences safely
    public static List<Task> getAllTasks(SharedPreferences sharedPreferences) {
        if (sharedPreferences == null) {
            return new ArrayList<>(); // Prevents crash if SharedPreferences is null
        }
        String json = sharedPreferences.getString(TASKS_KEY, null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        return json != null ? gson.fromJson(json, type) : new ArrayList<>();
    }

    // ✅ Retrieve a task by ID efficiently using Java Streams
    public static Task getTaskById(SharedPreferences sharedPreferences, String taskId) {
        return getAllTasks(sharedPreferences).stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .orElse(null);
    }

    // ✅ Save the task list back to SharedPreferences safely
    private static void saveTaskList(SharedPreferences sharedPreferences, List<Task> taskList) {
        if (sharedPreferences == null || taskList == null) return; // Prevents null errors
        sharedPreferences.edit().putString(TASKS_KEY, gson.toJson(taskList)).apply();
    }

    // ✅ Public method to save all tasks
    public static void saveAllTasks(SharedPreferences sharedPreferences, List<Task> taskList) {
        saveTaskList(sharedPreferences, taskList);
    }

    // ✅ Add a new task safely with full details
    public static void addNewTask(SharedPreferences sharedPreferences, String title, String description,
                                  String date, String startTime, String endTime, String repeatDuration) {
        if (title == null || title.trim().isEmpty() || date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Title and Date cannot be empty!");
        }

        List<Task> taskList = getAllTasks(sharedPreferences);
        Task newTask = new Task(UUID.randomUUID().toString(), title, description, date, startTime, endTime, repeatDuration, false);
        taskList.add(newTask);
        saveTaskList(sharedPreferences, taskList);
    }

    // ✅ Update an existing task
    public static void updateTask(SharedPreferences sharedPreferences, Task updatedTask) {
        List<Task> taskList = getAllTasks(sharedPreferences);
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(updatedTask.getId())) {
                taskList.set(i, updatedTask);
                saveTaskList(sharedPreferences, taskList);
                return;
            }
        }
    }

    // ✅ Delete a task by ID safely
    public static void deleteTask(SharedPreferences sharedPreferences, String taskId) {
        List<Task> taskList = getAllTasks(sharedPreferences);
        if (taskList.removeIf(task -> task.getId().equals(taskId))) {
            saveTaskList(sharedPreferences, taskList);
        }
    }

    // ✅ Convert Date + Time string into milliseconds safely
    public static long getMillisFromTime(String date, String time) {
        try {
            // Update the SimpleDateFormat to match the expected format
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.parse(date + " " + time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return System.currentTimeMillis(); // Returns current time in case of a parse error
        }
    }
}
