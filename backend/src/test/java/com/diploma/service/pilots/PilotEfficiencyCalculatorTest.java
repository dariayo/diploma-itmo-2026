package com.diploma.service.pilots;

import com.diploma.dto.CalculateData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PilotEfficiencyCalculatorTest {

    @Test
    void calculateAllBuildsWeightedEfficiencyFromFuelPunctualityAndDifficulty() {
        CalculateData data = new CalculateData();
        data.setFuelEff(70.0);

        PilotEfficiencyCalculator calculator = new PilotEfficiencyCalculator(
                List.of(
                        ignored -> Map.of("punctuality", 0.80),
                        ignored -> Map.of("difficulty", 0.50)
                ),
                0.40,
                0.30,
                0.30
        );

        Map<String, Double> result = calculator.calculateAll(data);

        assertThat(result.get("fuelEff")).isEqualTo(0.70);
        assertThat(result.get("effTotal")).isCloseTo(0.67, org.assertj.core.data.Offset.offset(0.0001));
    }
}
