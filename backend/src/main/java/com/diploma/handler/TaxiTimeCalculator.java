package com.diploma.handler;

import com.diploma.models.xml.FullNavLog;
import com.diploma.models.characteristics.TimeCharacteristics;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaxiTimeCalculator {

    // время руления до взлета (время от запуски двигателя до взлета)
    public static Integer calculateTaxiOutTime(FullNavLog fullNavLog, TimeCharacteristics timeCharacteristics) {
        if (fullNavLog.getStartParams() == null) return null;
        return calculateTimeDifference(timeCharacteristics.getDTAOBT(), timeCharacteristics.getDTATOT());
    }



    // время руления после посадки (время от посадки до выключения двигателя)
    public static Integer calculateTaxiInTime(FullNavLog fullNavLog, TimeCharacteristics timeCharacteristics) {
        if (fullNavLog.getEndParams() == null) return null;
        //System.out.println("Old "  + calculateTimeDifference(fullNavLog.getEndParams().getATL(),
                //fullNavLog.getEndParams().getEngineOff()));
        //System.out.println("New " + calculateTimeDifference(timeCharacteristics.getDTATL(), timeCharacteristics.getDTATA()));
        return calculateTimeDifference(timeCharacteristics.getDTATL(), timeCharacteristics.getDTATA());
    }



    public static Integer calculateTimeDifference(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return (int) Duration.between(start, end).toMinutes();
    }

    public static Integer calculateTotalTaxiTime(Integer taxiOutTime, Integer taxiInTime) {
        if (taxiOutTime == null || taxiInTime == null) return null;
        return taxiOutTime + taxiInTime;
    }
}
