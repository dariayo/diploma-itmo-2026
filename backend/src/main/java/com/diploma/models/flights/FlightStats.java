package com.diploma.models.flights;

import java.util.*;

public class FlightStats {
    private final List<Integer> temperatures = new ArrayList<>();
    private final Map<String, Integer> flightLevels = new HashMap<>();
    private final List<Double> windSpeed = new ArrayList<>();
    private final List<Double> windAngle = new ArrayList<>();

    public void addTemperature(Integer temp) {
        if (temp != null) {
            temperatures.add(temp);
        }
    }

    public void addFlightLevel(String fl) {
        if (fl != null) {
            flightLevels.put(fl, flightLevels.getOrDefault(fl, 0) + 1);
        }
    }

    public String getTemperatureSummary() {
        if (temperatures.isEmpty()) return "N/A";

        int min = Collections.min(temperatures);
        int max = Collections.max(temperatures);

        if (min == max) {
            return min + "°C";
        } else {
            return min + "°C / " + max + "°C";
        }
    }

    public String getMostCommonFlightLevel() {
        if (flightLevels.isEmpty()) return "N/A";

        return flightLevels.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    public void addWind(String wind) {
        if (wind != null && !wind.equals("N/A")) {
            try {
                String[] split = wind.split("/");
                if (split.length == 2) {
                    windSpeed.add(Double.parseDouble(split[1]));
                    windAngle.add(Double.parseDouble(split[0]));
                }
            } catch (NumberFormatException e) {
                System.err.println("Ошибка парсинга ветра: " + wind);
            }
        }

    }

    public String getAverageWind() {
        if (windAngle.isEmpty() || windSpeed.isEmpty()) return "N/A";
        double avgAngle = windAngle.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double avgSpeed = windSpeed.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        return String.format("%.0f/%.0f", avgSpeed, avgAngle);
    }


}