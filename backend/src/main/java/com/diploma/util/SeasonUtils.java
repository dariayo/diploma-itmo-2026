package com.diploma.util;

import com.diploma.enums.Season;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SeasonUtils {

    public static Season getSeason(String date) {
        if (date == null) return Season.UNKNOWN;
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date);
            int month = dateTime.getMonthValue();
            int day = dateTime.getDayOfMonth();
            int dayW = lastSunday(dateTime.getYear(), 10);
            int dayS = lastSunday(dateTime.getYear(), 3);
            if ((month == 10 && day >= dayW) ||
                    (month == 11 || month == 12 || month == 1 || month == 2) ||
                    (month == 3 && day <= dayS)) {
                return Season.WINTER;
            } else {
                return Season.SUMMER;
            }
        } catch (Exception e) {
            return Season.UNKNOWN;
        }
    }
    public static int lastSunday(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 31);
        int day = date.getDayOfWeek().getValue();
        return date.minusDays(day).getDayOfMonth();
    }
}
