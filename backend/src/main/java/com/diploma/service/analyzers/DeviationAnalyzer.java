package com.diploma.service.analyzers;

import org.springframework.stereotype.Service;
import com.diploma.models.flights.FlightFactor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Service
public class DeviationAnalyzer {
    public String analyzeDeviation(List<FlightFactor> sourceFactors, List<FlightFactor> factors) {
        Deque<String> sourcePoints = extractPoints(sourceFactors);
        Deque<String> activePoints = extractPoints(factors);

        if (!sourcePoints.isEmpty() && !activePoints.isEmpty()) {
            activePoints.removeFirst();
            activePoints.removeLast();
            sourcePoints.removeLast();
            if (!sourcePoints.getLast().equals(activePoints.getLast())) {
                return "Сел на запасной аэродром " + activePoints.getLast();
            }
            return "соответствует";
        }
        return "соответствует";
    }

    private Deque<String> extractPoints(List<FlightFactor> sourceFactors) {
        Deque<String> points = new ArrayDeque<>();
        for (var factor : sourceFactors) {
            points.add(factor.getPoint());
        }
        return points;
    }

}
