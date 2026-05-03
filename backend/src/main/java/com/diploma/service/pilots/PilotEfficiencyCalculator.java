package com.diploma.service.pilots;

import org.springframework.stereotype.Service;
import com.diploma.dto.CalculateData;
import com.diploma.service.metrics.Calculator;

import java.util.*;

@Service
public class PilotEfficiencyCalculator {
    private final List<Calculator> calculators;

    public PilotEfficiencyCalculator(List<Calculator> calculators) {
        this.calculators = calculators;
    }

    public Map<String, Double> calculateAll(CalculateData data) {
        Map<String, Double> allResults = new LinkedHashMap<>();

        double total = 0.0;
        for (Calculator calculator : calculators) {
            Map<String, Double> calcResults = calculator.calculate(data);
            for (var entry : calcResults.entrySet()) {
                String key = entry.getKey();
                allResults.put(key, entry.getValue());
            }
        }

        Double fuelEff = data.getFuelEff() / 100;
        allResults.put("fuelEff", fuelEff);

        Double punctuality = allResults.get("punctuality");
        Double difficultyTotal = allResults.get("difficulty");

        allResults.put("effTotal", (fuelEff + punctuality + difficultyTotal) / 3);
        return allResults;
    }

}
