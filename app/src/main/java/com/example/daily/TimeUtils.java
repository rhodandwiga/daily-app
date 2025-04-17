package com.example.daily;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeUtils {
    public static long getTimeInMillis(String date, String time) {
        // Update the format to match "dd/MM/yyyy" for the date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            return sdf.parse(date + " " + time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
