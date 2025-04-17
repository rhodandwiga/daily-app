package com.example.daily;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private Context context;
    private List<AppInfo> appList;
    private SharedPreferences preferences;

    public AppListAdapter(Context context, List<AppInfo> appList) {
        this.context = context;
        this.appList = appList;
        this.preferences = context.getSharedPreferences("AppLimits", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo appInfo = appList.get(position);
        String packageName = appInfo.getPackageName();
        long usageTime = appInfo.getUsageTime();
        int limit = preferences.getInt(packageName, 0);

        Log.d("AppListAdapter", "App: " + appInfo.getAppName() + " | Usage: " + usageTime + " min | Limit: " + limit + " min");

        holder.appName.setText(appInfo.getAppName());
        holder.appIcon.setImageDrawable(appInfo.getIcon());
        holder.appUsage.setText("Usage: " + usageTime + " min");
        holder.appLimit.setText(limit > 0 ? "Limit: " + limit + " min" : "Limit: No Limit");

        if (limit > 0 && usageTime >= limit) {
            holder.appBlockedStatus.setVisibility(View.VISIBLE);
            holder.appBlockedStatus.setText("Blocked. Try Tomorrow");
            holder.setLimitButton.setEnabled(false);

            // Show Toast message
            Toast.makeText(context, "You exceeded the app limit for " + appInfo.getAppName(), Toast.LENGTH_LONG).show();
        } else {
            holder.appBlockedStatus.setVisibility(View.GONE);
            holder.setLimitButton.setEnabled(true);
        }

        // ✅ Set Limit Button Click Event
        holder.setLimitButton.setOnClickListener(v -> showTimeLimitDialog(packageName));
    }



    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName, appUsage, appLimit, appBlockedStatus;
        ImageView appIcon;
        Button setLimitButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            appIcon = itemView.findViewById(R.id.app_icon);
            appUsage = itemView.findViewById(R.id.app_usage);
            appLimit = itemView.findViewById(R.id.app_limit);
            setLimitButton = itemView.findViewById(R.id.set_limit_button);
            appBlockedStatus = itemView.findViewById(R.id.app_blocked_status);
        }
    }

    private void showTimeLimitDialog(String packageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Time Limit (minutes)");

        final EditText input = new EditText(context);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Set", (dialog, which) -> {
            String value = input.getText().toString();
            if (!value.isEmpty()) {
                int minutes = Integer.parseInt(value);
                saveAppLimit(packageName, minutes);
                Toast.makeText(context, "Time limit set for " + minutes + " minutes", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged(); // Refresh UI
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveAppLimit(String packageName, int minutes) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(packageName, minutes);
        editor.apply();
    }
}
