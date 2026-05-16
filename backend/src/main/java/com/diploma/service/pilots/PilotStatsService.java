package com.diploma.service.pilots;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.diploma.processor.FlightDataProcessor;
import com.diploma.models.flights.FlightFactor;

import com.diploma.parser.Parser;
import com.diploma.entities.FlightXMLAllParams;
import com.diploma.entities.MainPilotGroupDt;

import com.diploma.models.characteristics.TimeCharacteristics;
import com.diploma.models.characteristics.WeightCharacteristics;
import com.diploma.models.pilots.PilotStats;
import com.diploma.dto.PilotStatsResponse;
import com.diploma.models.flights.Flight;

import com.diploma.repository.FlightXMLGroupRepository;
import com.diploma.service.flights.FlightXMLAllParamsService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PilotStatsService {
    private final FlightXMLAllParamsService flightService;
    private final FlightDataProcessor flightDataProcessor;
    private final MainPilotGroupDtService mainPilotGroupDtService;
    private final Parser parser;
    private final FlightXMLGroupRepository flightXMLGroupRepository;

    @Cacheable(value = "pilotStats", key = "#startDate.toString() + '-' + #endDate.toString()")
    @Transactional(readOnly = true)
    public PilotStatsResponse getPilotStats(LocalDate startDate, LocalDate endDate) {

        List<FlightXMLAllParams> flights = flightService.getByRangeWithoutNulls(startDate, endDate);
        Map<Integer, PilotStats> pilotStatsMap = new HashMap<>();
        double totalEfficiency = 0;
        int totalFlight = 0;
        double totalFlightHours = 0;

        for (FlightXMLAllParams flightXML : flights) {
            try {
                Flight flight = parser.parseFlightXML(flightXML.getFlightXml());
                if (flight != null) {
                    String Straightening = (flightXML.getIsStraightening() == null || flightXML.getIsStraightening().equals(false)) ? "0" : "1";
                    String StraighteningROV = (flightXML.getIsStraighteningROV() == null || flightXML.getIsStraighteningROV().equals(false)) ? "0" : "1";
                    TimeCharacteristics timeCharacteristics = TimeCharacteristics.buildByFlightXMLAllParams(flightXML);
                    WeightCharacteristics weightCharacteristics = WeightCharacteristics.buildByFlightXMLAllParams(flightXML);

                    Double fuel_eff = flightXML.getFuel_eff();
                    List<FlightFactor> factors = flightDataProcessor.processFlightData(
                            flight, weightCharacteristics, timeCharacteristics,
                            Straightening,
                            StraighteningROV,
                            fuel_eff
                    );

                    if (!factors.isEmpty()) {
                        FlightFactor general = factors.get(0);
                        Integer tabNo = general.getTab();
                        if (tabNo != null) {
                            PilotStats stats = pilotStatsMap.getOrDefault(tabNo, new PilotStats(tabNo, resolveCaptainName(flightXML, general.getCaptain()), 0, 0.0, 0));
                            String efficiencyStr = factors.get(factors.size() - 3).getFuelEfficiency();
                            if (efficiencyStr != null) {
                                double efficiency = parseEfficiency(efficiencyStr);
                                stats.addFlight(efficiency);
                                totalEfficiency += efficiency;
                                totalFlight++;
                            }

                            double flightHours = calculateFlightHours(flight);
                            stats.addFlightHours(flightHours);
                            totalFlightHours += flightHours;

                            pilotStatsMap.put(tabNo, stats);
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Error processing flight: " + e.getMessage());
            }

        }

        // Убрали .limit(limit) - теперь возвращаем всех пилотов
        List<PilotStats> rankedPilots = pilotStatsMap.values().stream()
                .sorted((a, b) -> Double.compare(b.getAverageEfficiency(), a.getAverageEfficiency()))
                .toList();
        List<PilotStats> longRankedPilots = rankedPilots.stream()
                .peek(a -> a.setFlightHours(Math.round(a.getFlightHours())))
                .toList();
        double avgEfficiency = totalFlight > 0 ? totalEfficiency / totalFlight : 0;

        return new PilotStatsResponse(longRankedPilots, avgEfficiency, totalFlightHours);
    }

    private String resolveCaptainName(FlightXMLAllParams flightXML, String fallbackCaptainName) {
        if (flightXML.getPilot() != null && flightXML.getPilot().getFullName() != null && !flightXML.getPilot().getFullName().isBlank()) {
            return flightXML.getPilot().getFullName();
        }
        return fallbackCaptainName;
    }

    public PilotStatsResponse getPilotStatsFromGroupDt(LocalDate startDate, LocalDate endDate) throws JsonProcessingException {
        long s = System.currentTimeMillis();
        List<MainPilotGroupDt> days = mainPilotGroupDtService.getByDataRange(startDate.atStartOfDay(), endDate.atStartOfDay());

        Map<Integer, PilotStats> pilotStatsMap = new HashMap<>();
        double totalEfficiency = 0;
        int totalFlight = 0;
        double totalFlightHours = 0;

        for (MainPilotGroupDt group : days) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<PilotStats> pilotStats = mapper.readValue(group.getPilots(), new TypeReference<List<PilotStats>>() {
                });
                for (PilotStats pilot : pilotStats) {
                    double rating = calculatePilotRating(pilot);
                    if (pilot.getPilotRating() == null) {
                        pilot.setPilotRating(rating);
                    } else {
                        pilot.setPilotRating(pilot.getPilotRating() + rating);
                    }
                    if (!pilotStatsMap.containsKey(pilot.getTabNumber())) {
                        pilotStatsMap.put(pilot.getTabNumber(), pilot);
                    } else {
                        PilotStats consistsPilot = pilotStatsMap.get(pilot.getTabNumber());
                        consistsPilot.updateData(pilot);
                    }

                    if (pilot.getDifficultFlight() != null && pilot.getFlightCount() > 0) {
                        double avgDifficulty = pilot.getDifficultFlight();
                        pilot.setAverageDifficulty(avgDifficulty);
                    } else {
                        pilot.setAverageDifficulty(0.0);
                    }
                }
                totalEfficiency += group.getSumefficiency();
                totalFlightHours += group.getTotalhours();
                totalFlight += group.getCountreis();

            } catch (Exception e) {
                System.err.println("Error processing flight: " + e.getMessage());
            }
        }

        List<PilotStats> rankedPilots = pilotStatsMap.values().stream()
                .sorted((p2, p1) -> {
                    double rating1 = (p1.getPilotRating() != null ? p1.getPilotRating() : 0.0);
                    double rating2 = (p2.getPilotRating() != null ? p2.getPilotRating() : 0.0);
                    return Double.compare(rating1, rating2);
                })
                .toList();

        List<PilotStats> longRankedPilots = rankedPilots.stream()
                .peek(a -> a.setFlightHours(Math.round(a.getFlightHours())))
                .toList();

        double avgEfficiency = totalFlight > 0 ? totalEfficiency / totalFlight : 0;

        return new PilotStatsResponse(longRankedPilots, avgEfficiency, totalFlightHours);
    }

    private double calculatePilotRating(PilotStats pilot) {
        if (pilot.getFlightCount() <= 0) {
            return 0.0;
        }
        double averageEfficiency = pilot.getTotalEfficiency() / pilot.getFlightCount();
        double difficulty = pilot.getDifficultFlight() != null ? pilot.getDifficultFlight() : 1.0;
        double flightHours = pilot.getFlightHours();
        double workloadCoefficient = difficulty * flightHours;

        return averageEfficiency * workloadCoefficient;
    }

    public PilotStatsResponse getPilotStatsFromGroupDtByRoutes(LocalDate startDate, LocalDate endDate, List<String> routes) throws JsonProcessingException {
        PilotStatsResponse fullStats = getPilotStatsFromGroupDt(startDate, endDate);
        if (routes == null || routes.isEmpty()) {
            return fullStats;
        }

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atStartOfDay();

        List<Integer> tabNos = new ArrayList<>();
        try {
            tabNos = flightXMLGroupRepository.findTabNosByDtAndRoutes(routes, startDt, endDt);
        } catch (Exception e) {
            System.err.println("Error querying FlightXMLGroupRepository for tab numbers: " + e.getMessage());
        }

        Set<Integer> tabNoSet = new HashSet<>(tabNos);

        List<PilotStats> filteredPilots = new ArrayList<>();
        for (PilotStats pilot : fullStats.getPilots()) {
            if (pilot.getTabNumber() != null && tabNoSet.contains(pilot.getTabNumber())) {
                filteredPilots.add(pilot);
            }
        }

        return new PilotStatsResponse(filteredPilots, fullStats.getAverageEfficiency(), fullStats.getTotalFlightHours());
    }

    @Cacheable(value = "efficiencyTrend", key = "#months")
    public Map<String, List<Object>> getEfficiencyTrend(int months, int limit) {
        Map<String, List<Object>> efficiencyTrend = new HashMap<>();
        List<Double> monthlyAverages = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate start = now.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            PilotStatsResponse monthlyStats = getPilotStats(start, end);
            monthlyAverages.add(monthlyStats.getAverageEfficiency());
            labels.add(start.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
        }
        efficiencyTrend.put("data", Collections.singletonList(monthlyAverages));
        efficiencyTrend.put("labels", Collections.singletonList(labels));
        return efficiencyTrend;
    }

    public double parseEfficiency(String efficiencyStr) {
        try {
            String eff = efficiencyStr.replace("%", "").trim();
            return Double.parseDouble(eff.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double calculateFlightHours(Flight flight) {
        if (flight != null) {
            LocalDateTime start = flight.getFullNavLog().getStartParams().getEngineStart();
            LocalDateTime end = flight.getFullNavLog().getEndParams().getEngineOff();

            if (start != null && end != null) {
                return (double) Duration.between(start, end).toMinutes() / 60;
            }
        }
        return 0.0;
    }
}
