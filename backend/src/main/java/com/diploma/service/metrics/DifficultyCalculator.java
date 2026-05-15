package com.diploma.service.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.diploma.dto.CalculateData;

import java.util.*;

@Component
public class DifficultyCalculator implements Calculator {
    private final List<DifficultyCalc> metrics;
    private final double lengthWeight;
    private final double nightWeight;
    private final double weatherWeight;

    public DifficultyCalculator(
            List<DifficultyCalc> m,
            @Value("${app.difficulty.weights.length:0.34}") double lengthWeight,
            @Value("${app.difficulty.weights.night:0.33}") double nightWeight,
            @Value("${app.difficulty.weights.weather:0.33}") double weatherWeight) {
        this.metrics = m;
        this.lengthWeight = lengthWeight;
        this.nightWeight = nightWeight;
        this.weatherWeight = weatherWeight;
    }

    public Map<String, Double> calculate(CalculateData data) {
        Map<String, Double> allResults = new LinkedHashMap<>();

        for (DifficultyCalc calc : metrics) {
            Map<String, Double> results = calc.calculate(data);

            for (var entry : results.entrySet()) {
                String key = entry.getKey();
                allResults.put(key, entry.getValue());
            }
        }

        double length = clamp(allResults.getOrDefault("length", 0.0));
        double night = clamp(allResults.getOrDefault("night", 0.0));
        double weather = clamp(allResults.getOrDefault("weatherDifficulty", 0.0));
        double weightSum = lengthWeight + nightWeight + weatherWeight;

        double overall = weightSum > 0
                ? (lengthWeight * length + nightWeight * night + weatherWeight * weather) / weightSum
                : (length + night + weather) / 3;

        allResults.put("difficulty", overall);

        return allResults;
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
