package com.example.daily;

import java.io.Serializable;

public class Task implements Serializable {  // ✅ Implements Serializable for easy passing between activities
    private String id;
    private String title;
    private String description;
    private String date; // Task date
    private String startTime;
    private String endTime;
    private String repeatDuration; // Daily, Weekly, Monthly, or Custom
    private boolean isCompleted; // Tracks task completion

    public Task(String id, String title, String description, String date, String startTime, String endTime, String repeatDuration, boolean isCompleted) {
        this.id = id != null ? id : ""; // ✅ Prevents null ID
        this.title = title != null ? title : "Untitled Task"; // ✅ Default title if null
        this.description = description != null ? description : ""; // ✅ Prevents null description
        this.date = date != null ? date : "Unknown Date"; // ✅ Default date if null
        this.startTime = startTime != null ? startTime : "00:00"; // ✅ Default time if null
        this.endTime = endTime != null ? endTime : "00:00";
        this.repeatDuration = repeatDuration != null ? repeatDuration : "None"; // ✅ Default repeat setting
        this.isCompleted = isCompleted;
    }

    // ✅ Getter and setter for ID
    public String getId() {
        return id;
    }

    // ✅ Getter and setter for Title
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title != null ? title : "Untitled Task";
    }

    // ✅ Getter and setter for Description
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    // ✅ Getter and setter for Date
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date != null ? date : "Unknown Date";
    }

    // ✅ Getter and setter for Start Time
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime != null ? startTime : "00:00";
    }

    // ✅ Getter and setter for End Time
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime != null ? endTime : "00:00";
    }

    // ✅ Getter and setter for Repeat Duration
    public String getRepeatDuration() {
        return repeatDuration;
    }
    public void setRepeatDuration(String repeatDuration) {
        this.repeatDuration = repeatDuration != null ? repeatDuration : "None";
    }

    // ✅ Getter and setter for Completion Status
    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    // ✅ Override toString() for debugging purposes
    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", repeatDuration='" + repeatDuration + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
