package com.example.daily;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String appName;
    private String packageName;
    private Drawable icon;
    private long usageTime; // Add usageTime variable

    public AppInfo(String appName, String packageName, Drawable icon, long usageTime) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.usageTime = usageTime; // Initialize usage time
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public long getUsageTime() { // Add this method
        return usageTime;
    }
}
