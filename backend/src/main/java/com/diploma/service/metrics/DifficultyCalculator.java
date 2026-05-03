package com.diploma.service.metrics;

import org.springframework.stereotype.Component;
import com.diploma.dto.CalculateData;

import java.util.*;

@Component
public class DifficultyCalculator implements Calculator {
    private final List<DifficultyCalc> metrics;
    private final int PARAMS = 3;

    public DifficultyCalculator(List<DifficultyCalc> m) {
        this.metrics = m;
    }

    public Map<String, Double> calculate(CalculateData data) {
        Map<String, Double> allResults = new LinkedHashMap<>();

        double total = 0.0;

        for (DifficultyCalc calc : metrics) {
            Map<String, Double> results = calc.calculate(data);

            for (var entry : results.entrySet()) {
                String key = entry.getKey();
                allResults.put(key, entry.getValue());
                total += entry.getValue();
            }
        }

        double overall = total / PARAMS;
        allResults.put("difficulty", overall);

        return allResults;
    }
}
