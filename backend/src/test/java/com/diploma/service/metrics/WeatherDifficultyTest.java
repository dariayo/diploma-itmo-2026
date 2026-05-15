package com.diploma.service.metrics;

import com.diploma.dto.CalculateData;
import com.diploma.models.Airport;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherDifficultyTest {

    @Test
    void calculateUsesDepartureArrivalAndWorstEnrouteVisibility() {
        CalculateData data = new CalculateData();
        data.setFrom(new Airport("LED", "ULLI", 59.8, 30.2));
        data.setTo(new Airport("SVO", "UUEE", 55.9, 37.4));
        data.setWeatherData("""
                TAF ULLI 150500Z 1506/1606 2000 BR
                TAF UUEE 150500Z 1506/1606 6000 NSW
                TAF ULLL 150500Z 1506/1606 1000 SN
                """);

        Map<String, Double> result = new WeatherDifficulty().calculate(data);

        assertThat(result.get("weatherDifficulty")).isCloseTo(0.4353, org.assertj.core.data.Offset.offset(0.0001));
    }
}
