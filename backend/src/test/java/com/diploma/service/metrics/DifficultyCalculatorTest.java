package com.diploma.service.metrics;

import com.diploma.dto.CalculateData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DifficultyCalculatorTest {

    @Test
    void calculateCombinesLengthNightAndWeatherWithWeights() {
        DifficultyCalculator calculator = new DifficultyCalculator(
                List.of(
                        data -> Map.of("length", 0.75),
                        data -> Map.of("night", 0.30),
                        data -> Map.of("weatherDifficulty", 0.60)
                ),
                0.34,
                0.33,
                0.33
        );

        Map<String, Double> result = calculator.calculate(new CalculateData());

        assertThat(result)
                .containsEntry("length", 0.75)
                .containsEntry("night", 0.30)
                .containsEntry("weatherDifficulty", 0.60);
        assertThat(result.get("difficulty")).isCloseTo(0.552, withinTolerance());
    }

    @Test
    void calculateClampsIncorrectMetricValuesBeforeOverallDifficulty() {
        DifficultyCalculator calculator = new DifficultyCalculator(
                List.of(
                        data -> Map.of("length", 1.50),
                        data -> Map.of("night", Double.NaN),
                        data -> Map.of("weatherDifficulty", -0.40)
                ),
                1.0,
                1.0,
                1.0
        );

        Map<String, Double> result = calculator.calculate(new CalculateData());

        assertThat(result.get("difficulty")).isCloseTo(1.0 / 3.0, withinTolerance());
    }

    private org.assertj.core.data.Offset<Double> withinTolerance() {
        return org.assertj.core.data.Offset.offset(0.0001);
    }
}
