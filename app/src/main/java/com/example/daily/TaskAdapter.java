package com.example.daily;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;
    private SharedPreferences sharedPreferences;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("DailyPulseTasks", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Ensure task ID is valid
        if (task.getId() == null) {
            return;
        }

        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskDate.setText("Date: " + task.getDate());
        holder.taskTime.setText("Time: " + task.getStartTime() + " - " + task.getEndTime());

        if (task.getRepeatDuration() != null && !task.getRepeatDuration().isEmpty()) {
            holder.taskRepeatDuration.setVisibility(View.VISIBLE);
            holder.taskRepeatDuration.setText("Repeats: " + task.getRepeatDuration());
        } else {
            holder.taskRepeatDuration.setVisibility(View.GONE);
        }

        boolean isCompleted = sharedPreferences.getBoolean(task.getId() + "_completed", false);
        holder.checkComplete.setOnCheckedChangeListener(null); // Prevent multiple triggers
        holder.checkComplete.setChecked(isCompleted);

        holder.checkComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(task.getId() + "_completed", isChecked).apply();
            Toast.makeText(context, isChecked ? "Great! Keep up the good work!" : "Task marked as incomplete.", Toast.LENGTH_SHORT).show();
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTaskActivity.class);
            intent.putExtra("task_id", task.getId());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            TaskUtils.deleteTask(sharedPreferences, task.getId());
            taskList.remove(position);
            notifyDataSetChanged(); // Ensure full UI update
            Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show();
        });

        scheduleNotifications(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskDate, taskTime, taskRepeatDuration;
        Button editButton, deleteButton;
        CheckBox checkComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskRepeatDuration = itemView.findViewById(R.id.taskRepeatDuration);
            editButton = itemView.findViewById(R.id.btnEditTask);
            deleteButton = itemView.findViewById(R.id.btnDeleteTask);
            checkComplete = itemView.findViewById(R.id.checkComplete);
        }
    }

    private void scheduleNotifications(Task task) {
        String scheduledKey = task.getId() + "_notifs_scheduled";
        boolean alreadyScheduled = sharedPreferences.getBoolean(scheduledKey, false);

        if (alreadyScheduled) return; // Skip if already scheduled

        // Get the current time
        long currentTimeMillis = System.currentTimeMillis();

        // Convert the task start time and end time to milliseconds
        long startTimeMillis = TimeUtils.getTimeInMillis(task.getDate(), task.getStartTime());
        long endTimeMillis = TimeUtils.getTimeInMillis(task.getDate(), task.getEndTime());

        // Check if the task's start time is more than 15 minutes away
        if (startTimeMillis > currentTimeMillis + (15 * 60 * 1000)) {
            // Schedule a notification for 15 minutes before the start time
            long reminderBeforeStart = startTimeMillis - (15 * 60 * 1000);
            if (reminderBeforeStart > currentTimeMillis) {
                // Schedule the notification
                Handler handler = new Handler();
                handler.postDelayed(() -> NotificationUtils.sendReminder(context, task.getId(),
                                "Upcoming Task: " + task.getTitle(),
                                "Your task starts in 15 minutes! Stay on track."),
                        reminderBeforeStart - currentTimeMillis);
            }
        }

        // Schedule a notification for 5 minutes after the task end time if not completed
        long reminderAfterEnd = endTimeMillis + (5 * 60 * 1000);
        if (reminderAfterEnd > currentTimeMillis) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                boolean isCompleted = sharedPreferences.getBoolean(task.getId() + "_completed", false);
                if (!isCompleted) {
                    NotificationUtils.sendReminder(context, task.getId(),
                            "Missed Task: " + task.getTitle(),
                            "You didn't complete this task on time. Try to improve next time!");
                }
            }, reminderAfterEnd - currentTimeMillis);
        }

        // Mark as scheduled
        sharedPreferences.edit().putBoolean(scheduledKey, true).apply();
    }
}