package com.diploma.service.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.diploma.dto.CalculateData;
import com.diploma.entities.PilotWeatherSnapshot;
import com.diploma.repository.PilotWeatherSnapshotRepository;
import com.diploma.service.WeatherParser;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class WeatherDifficulty implements DifficultyCalc {
    private PilotWeatherSnapshotRepository pilotWeatherSnapshotRepository;

    public WeatherDifficulty() {
    }

    @Autowired
    public WeatherDifficulty(PilotWeatherSnapshotRepository pilotWeatherSnapshotRepository) {
        this.pilotWeatherSnapshotRepository = pilotWeatherSnapshotRepository;
    }

    @Override
    public Map<String, Double> calculate(CalculateData data) {
        List<PilotWeatherSnapshot> snapshots = resolveWeatherSnapshots(data);
        if (snapshots != null && !snapshots.isEmpty()) {
            return Map.of("weatherDifficulty", calculateFromSnapshots(data, snapshots));
        }

        Map<String, Integer> visibilities = WeatherParser.calculateVisibilities(
                data.getWeatherData(), data.getFrom().icao(), data.getTo().icao());

        int enroute = visibilities.getOrDefault("enroute", 0);
        int take = visibilities.getOrDefault("take", 0);
        int arr = visibilities.getOrDefault("arr", 0);

        double enrouteScore = enroute > 3400 ? 0.0 : (3400.0 - enroute) / 3400;
        double takeScore = take > 5000 ? 0.0 : (5000.0 - take) / 5000;
        double arrScore = arr > 5000 ? 0.0 : (5000.0 - arr) / 5000;

        double result = (enrouteScore + takeScore + arrScore) / 3;

        return Map.of("weatherDifficulty", result);
    }

    private List<PilotWeatherSnapshot> resolveWeatherSnapshots(CalculateData data) {
        if (data.getWeatherSnapshots() != null && !data.getWeatherSnapshots().isEmpty()) {
            return data.getWeatherSnapshots();
        }
        if (pilotWeatherSnapshotRepository != null && data.getId_mrshr() != null) {
            return pilotWeatherSnapshotRepository.findBySourceFlightId(data.getId_mrshr());
        }
        return List.of();
    }

    private double calculateFromSnapshots(CalculateData data, List<PilotWeatherSnapshot> snapshots) {
        String fromIcao = data.getFrom() != null ? data.getFrom().icao() : null;
        String toIcao = data.getTo() != null ? data.getTo().icao() : null;

        int take = findVisibility(snapshots, fromIcao, 5000);
        int arr = findVisibility(snapshots, toIcao, 5000);
        int enroute = snapshots.stream()
                .map(PilotWeatherSnapshot::getVisibilityM)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(3400);

        double enrouteScore = enroute > 3400 ? 0.0 : (3400.0 - enroute) / 3400;
        double takeScore = take > 5000 ? 0.0 : (5000.0 - take) / 5000;
        double arrScore = arr > 5000 ? 0.0 : (5000.0 - arr) / 5000;

        return (enrouteScore + takeScore + arrScore) / 3;
    }

    private int findVisibility(List<PilotWeatherSnapshot> snapshots, String icaoCode, int defaultValue) {
        if (icaoCode == null) {
            return defaultValue;
        }
        return snapshots.stream()
                .filter(snapshot -> icaoCode.equalsIgnoreCase(snapshot.getIcaoCode()))
                .map(PilotWeatherSnapshot::getVisibilityM)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(defaultValue);
    }
}
