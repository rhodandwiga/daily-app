package com.example.daily;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class EditTaskActivity extends AppCompatActivity {
    private EditText etTaskTitle, etTaskDescription;
    private Button btnSelectDate, btnStartTime, btnEndTime, btnSaveTask;
    private Spinner spinnerRepeat;
    private String selectedRepeatOption = "None";
    private String taskId; // Used for editing existing tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // Initialize UI elements
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        spinnerRepeat = findViewById(R.id.spinnerRepeat);

        // Populate Spinner with repeat options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repeat_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(adapter);

        spinnerRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRepeatOption = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRepeatOption = "None";
            }
        });

        // Set Date Picker
        btnSelectDate.setOnClickListener(v -> showDatePicker());

        // Set Time Pickers
        btnStartTime.setOnClickListener(v -> showTimePicker(btnStartTime));
        btnEndTime.setOnClickListener(v -> showTimePicker(btnEndTime));

        // Save Task Button
        btnSaveTask.setOnClickListener(v -> saveTask());

        // Check if editing an existing task
        loadTaskData();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    btnSelectDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker(Button button) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = selectedHour + ":" + String.format("%02d", selectedMinute);
                    button.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        String date = btnSelectDate.getText().toString();
        String startTime = btnStartTime.getText().toString();
        String endTime = btnEndTime.getText().toString();

        if (title.isEmpty() || description.isEmpty() || date.equals("Select Date") ||
                startTime.equals("Start Time") || endTime.equals("End Time")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse date and time to create calendar event
        Calendar calendar = Calendar.getInstance();
        String[] dateParts = date.split("/");
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");

        int year = Integer.parseInt(dateParts[2]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based in Calendar
        int day = Integer.parseInt(dateParts[0]);
        int startHour = Integer.parseInt(startParts[0]);
        int startMinute = Integer.parseInt(startParts[1]);
        int endHour = Integer.parseInt(endParts[0]);
        int endMinute = Integer.parseInt(endParts[1]);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(year, month, day, startHour, startMinute);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(year, month, day, endHour, endMinute);

        // Create Intent to add event to Google Calendar
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
        calendarIntent.setType("vnd.android.cursor.item/event");
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCalendar.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCalendar.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.Events.TITLE, title);
        calendarIntent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        calendarIntent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        // Add repeat option if selected
        if (!selectedRepeatOption.equals("None")) {
            calendarIntent.putExtra(CalendarContract.Events.RRULE, getRepeatRule(selectedRepeatOption));
        }

        // Start Calendar app
        if (calendarIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(calendarIntent);
        } else {
            Toast.makeText(this, "No Calendar app found!", Toast.LENGTH_SHORT).show();
        }

        // Send result back to main activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("taskId", taskId);
        resultIntent.putExtra("taskTitle", title);
        resultIntent.putExtra("taskDescription", description);
        resultIntent.putExtra("taskDate", date);
        resultIntent.putExtra("taskStartTime", startTime);
        resultIntent.putExtra("taskEndTime", endTime);
        resultIntent.putExtra("taskRepeat", selectedRepeatOption);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String getRepeatRule(String repeatOption) {
        switch (repeatOption) {
            case "Daily":
                return "FREQ=DAILY";
            case "Weekly":
                return "FREQ=WEEKLY";
            case "Monthly":
                return "FREQ=MONTHLY";
            case "Yearly":
                return "FREQ=YEARLY";
            default:
                return null;
        }
    }

    private void loadTaskData() {
        Intent intent = getIntent();
        if (intent.hasExtra("taskId")) {
            taskId = intent.getStringExtra("taskId");
            etTaskTitle.setText(intent.getStringExtra("taskTitle"));
            etTaskDescription.setText(intent.getStringExtra("taskDescription"));
            btnSelectDate.setText(intent.getStringExtra("taskDate"));
            btnStartTime.setText(intent.getStringExtra("taskStartTime"));
            btnEndTime.setText(intent.getStringExtra("taskEndTime"));
            selectedRepeatOption = intent.getStringExtra("taskRepeat");

            // Set spinner selection
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerRepeat.getAdapter();
            int position = adapter.getPosition(selectedRepeatOption);
            spinnerRepeat.setSelection(position);
        }
    }
}
