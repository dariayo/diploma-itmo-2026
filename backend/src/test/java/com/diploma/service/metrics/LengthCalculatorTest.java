package com.diploma.service.metrics;

import com.diploma.dto.CalculateData;
import com.diploma.repository.DistanceTypeModeRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LengthCalculatorTest {

    @Test
    void calculateNormalizesRouteDistanceByAircraftRange() {
        DistanceTypeModeRepository repository = mock(DistanceTypeModeRepository.class);
        when(repository.distance("A320")).thenReturn(3000);

        CalculateData data = new CalculateData();
        data.setTypeMod("A320");
        data.setDistance(1500);

        Map<String, Double> result = new LengthCalculator(repository).calculate(data);

        assertThat(result.get("length")).isEqualTo(0.5);
    }
}
