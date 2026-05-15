package com.diploma.service.metrics;

import com.diploma.dto.CalculateData;
import com.diploma.models.characteristics.TimeCharacteristics;
import com.diploma.repository.MrshrRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PunctualityCalculatorTest {

    @Test
    void calculateReturnsAverageScoreForDepartureAndArrivalDelays() {
        MrshrRepository repository = mock(MrshrRepository.class);
        when(repository.findDelayTakeById(42L)).thenReturn("pilot");
        when(repository.findDelayArrById(42L)).thenReturn("pilot");

        CalculateData data = new CalculateData();
        data.setId_mrshr(42L);
        data.setTd(TimeCharacteristics.builder()
                .DTAD(LocalDateTime.of(2026, 1, 15, 10, 0))
                .DTAOBT(LocalDateTime.of(2026, 1, 15, 10, 20))
                .DTAA(LocalDateTime.of(2026, 1, 15, 12, 0))
                .DTATA(LocalDateTime.of(2026, 1, 15, 12, 10))
                .build());

        Map<String, Double> result = new PunctualityCalculator(repository).calculate(data);

        assertThat(result.get("punctuality")).isEqualTo(0.75);
    }

    @Test
    void calculateReturnsZeroWhenRequiredTimeDataIsMissing() {
        CalculateData data = new CalculateData();
        data.setTd(TimeCharacteristics.builder().build());

        Map<String, Double> result = new PunctualityCalculator(mock(MrshrRepository.class)).calculate(data);

        assertThat(result.get("punctuality")).isZero();
    }
}
