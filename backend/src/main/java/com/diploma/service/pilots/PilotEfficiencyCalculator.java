package com.diploma.service.pilots;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.diploma.dto.CalculateData;
import com.diploma.service.metrics.Calculator;

import java.util.*;

@Service
public class PilotEfficiencyCalculator {
    private final List<Calculator> calculators;
    private final double fuelWeight;
    private final double punctualityWeight;
    private final double difficultyWeight;

    public PilotEfficiencyCalculator(
            List<Calculator> calculators,
            @Value("${app.efficiency.weights.fuel:0.4}") double fuelWeight,
            @Value("${app.efficiency.weights.punctuality:0.3}") double punctualityWeight,
            @Value("${app.efficiency.weights.difficulty:0.3}") double difficultyWeight) {
        this.calculators = calculators;
        this.fuelWeight = fuelWeight;
        this.punctualityWeight = punctualityWeight;
        this.difficultyWeight = difficultyWeight;
    }

    public Map<String, Double> calculateAll(CalculateData data) {
        Map<String, Double> allResults = new LinkedHashMap<>();

        for (Calculator calculator : calculators) {
            Map<String, Double> calcResults = calculator.calculate(data);
            for (var entry : calcResults.entrySet()) {
                String key = entry.getKey();
                allResults.put(key, entry.getValue());
            }
        }

        Double fuelEff = data.getFuelEff() / 100;
        allResults.put("fuelEff", fuelEff);

        double punctuality = clamp(allResults.getOrDefault("punctuality", 0.0));
        double difficultyTotal = clamp(allResults.getOrDefault("difficulty", 0.0));
        double normalizedFuelEff = clamp(fuelEff);

        double weightSum = fuelWeight + punctualityWeight + difficultyWeight;
        double effTotal = weightSum > 0
                ? (fuelWeight * normalizedFuelEff + punctualityWeight * punctuality + difficultyWeight * difficultyTotal) / weightSum
                : (normalizedFuelEff + punctuality + difficultyTotal) / 3;

        allResults.put("effTotal", effTotal);
        return allResults;
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
