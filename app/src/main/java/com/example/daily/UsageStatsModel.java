package com.example.daily;

public class UsageStatsModel {
    private String packageName;
    private long usageTime;

    public UsageStatsModel(String packageName, long usageTime) {
        this.packageName = packageName;
        this.usageTime = usageTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getUsageTime() {
        return usageTime;
    }
}
