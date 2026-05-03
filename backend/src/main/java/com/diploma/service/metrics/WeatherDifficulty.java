package com.diploma.service.metrics;

import org.springframework.stereotype.Component;
import com.diploma.dto.CalculateData;
import com.diploma.service.WeatherParser;

import java.util.Map;

@Component
public class WeatherDifficulty implements DifficultyCalc {
    @Override
    public Map<String, Double> calculate(CalculateData data) {
        Map<String, Integer> visibilities = WeatherParser.calculateVisibilities(
                data.getWeatherData(), data.getFrom().icao(), data.getTo().icao());

        int enroute = visibilities.getOrDefault("enroute", 0);
        int take = visibilities.getOrDefault("take", 0);
        int arr = visibilities.getOrDefault("arr", 0);

        double enrouteScore = enroute > 3400 ? 0.0 : (3400.0 - enroute) / 3400;
        double takeScore = take > 5000 ? 0.0 : (5000.0 - take) / 5000;
        double arrScore = arr > 5000 ? 0.0 : (5000.0 - arr) / 5000;

        double result = (enrouteScore + takeScore + arrScore) / 3;

        return Map.of("weatherDifficulty", result);
    }

}
