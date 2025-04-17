package com.example.daily;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UsageStatsAdapter extends RecyclerView.Adapter<UsageStatsAdapter.ViewHolder> {
    private List<UsageStatsModel> usageStatsList;
    private Context context;
    private PackageManager packageManager;

    public UsageStatsAdapter(Context context, List<UsageStatsModel> usageStatsList) {
        this.context = context;
        this.usageStatsList = usageStatsList;
        this.packageManager = context.getPackageManager();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.usage_stats_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsageStatsModel usageStats = usageStatsList.get(position);
        String packageName = usageStats.getPackageName();

        // Get the app's actual name instead of package name
        String appName;
        Drawable appIcon;
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            appName = (String) packageManager.getApplicationLabel(appInfo);
            appIcon = packageManager.getApplicationIcon(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            appName = packageName; // Show package name if app name is not found
            appIcon = context.getDrawable(R.mipmap.ic_launcher); // Default icon
        }

        holder.appName.setText(appName);
        holder.appIcon.setImageDrawable(appIcon);

        // Convert milliseconds to hours and minutes
        long timeInMillis = usageStats.getUsageTime();
        long minutes = (timeInMillis / 1000) / 60;
        long hours = minutes / 60;
        minutes = minutes % 60;

        holder.usageTime.setText(hours + "h " + minutes + "m");
    }

    @Override
    public int getItemCount() {
        return usageStatsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName, usageTime;
        ImageView appIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            usageTime = itemView.findViewById(R.id.usageTime);
            appIcon = itemView.findViewById(R.id.appIcon);
        }
    }
}
