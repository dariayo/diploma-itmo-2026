package com.diploma.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatterUtils {

    public static String formatTime(LocalDateTime dateTime) {
        return (dateTime != null && dateTime.getYear() != 1899) ? dateTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A";
    }

    public static String formatTime(Integer minutes) {
        if (minutes == null) return "N/A";
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
