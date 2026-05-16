package com.diploma.service.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.diploma.dto.CalculateData;
import com.diploma.entities.AircraftDirectory;
import com.diploma.repository.AircraftDirectoryRepository;
import com.diploma.repository.DistanceTypeModeRepository;

import java.util.Map;
import java.util.Optional;

@Component
public class LengthCalculator implements DifficultyCalc {
    private final DistanceTypeModeRepository distanceTypeModeRepository;
    private final AircraftDirectoryRepository aircraftDirectoryRepository;

    @Autowired
    public LengthCalculator(DistanceTypeModeRepository distanceTypeModeRepository,
                            AircraftDirectoryRepository aircraftDirectoryRepository) {
        this.distanceTypeModeRepository = distanceTypeModeRepository;
        this.aircraftDirectoryRepository = aircraftDirectoryRepository;
    }

    public LengthCalculator(DistanceTypeModeRepository distanceTypeModeRepository) {
        this.distanceTypeModeRepository = distanceTypeModeRepository;
        this.aircraftDirectoryRepository = null;
    }

    @Override
    public Map<String, Double> calculate(CalculateData data) {
        String typeMod = data.getTypeMod();
        double distanceByType = resolveMaxRange(typeMod);
        double distance = data.getDistance();

        if (distanceByType <= 0 || distance <= 0) {
            return Map.of("length", 0.0);
        }

        double result = distance / distanceByType;

        return Map.of("length", result);
    }

    private double resolveMaxRange(String typeMod) {
        if (typeMod == null || typeMod.isBlank()) {
            return 0.0;
        }
        if (aircraftDirectoryRepository != null) {
            Optional<AircraftDirectory> aircraft = aircraftDirectoryRepository.findById(typeMod);
            if (aircraft.isPresent() && aircraft.get().getMaxRangeNm() != null) {
                return aircraft.get().getMaxRangeNm();
            }
        }
        return distanceTypeModeRepository.distance(typeMod);
    }
}
