package com.diploma.models.flights;

import lombok.Builder;
import lombok.Data;
import com.diploma.enums.Season;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class FlightEff {
    private int tabNo;
    private String flightNumber;
    private String route;
    private String typeMod;
    private double fuelEff;
    private Season season;
    private LocalDateTime dateTime;
    private Map<String, Double> calculatedResults;
    private double flightHours;
}
