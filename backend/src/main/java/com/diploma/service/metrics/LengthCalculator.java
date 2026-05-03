package com.diploma.service.metrics;

import org.springframework.stereotype.Component;
import com.diploma.dto.CalculateData;
import com.diploma.repository.DistanceTypeModeRepository;

import java.util.Map;

@Component
public class LengthCalculator implements DifficultyCalc {
    private final DistanceTypeModeRepository distanceTypeModeRepository;

    public LengthCalculator(DistanceTypeModeRepository distanceTypeModeRepository) {
        this.distanceTypeModeRepository = distanceTypeModeRepository;
    }

    @Override
    public Map<String, Double> calculate(CalculateData data) {
        String typeMod = data.getTypeMod();
        double distanceByType = distanceTypeModeRepository.distance(typeMod);
        double distance = data.getDistance();

        double result = distance / distanceByType;

        return Map.of("length", result);
    }

}
