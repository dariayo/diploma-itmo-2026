package com.diploma.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.diploma.handler.GeneralInfoBuilder;
import com.diploma.models.characteristics.TimeCharacteristics;
import com.diploma.models.characteristics.WeightCharacteristics;
import com.diploma.models.flights.*;
import com.diploma.models.flights.Flight;
import com.diploma.models.flights.FlightFactor;
import com.diploma.models.flights.FlightMetaInfo;
import com.diploma.models.xml.*;
import com.diploma.models.xml.FullNavLog;
import com.diploma.service.analyzers.DeviationAnalyzer;
import com.diploma.service.analyzers.FlightStatsAnalyzer;
import com.diploma.service.analyzers.FuelEfficiencyCalculator;
import com.diploma.service.flights.FlightMetaDataExtractor;


import java.util.*;

import static com.diploma.handler.FuelTimeInfoBuilder.addFuelAndTimeInfo;
import static com.diploma.handler.NavLogProcessor.processNavLogItems;

@Service
@RequiredArgsConstructor
public class FlightDataProcessor {
    private final FlightMetaDataExtractor metaDataExtractor;
    private final FuelEfficiencyCalculator fuelEfficiencyCalculator;
    private final DeviationAnalyzer deviationAnalyzer;
    private final FlightStatsAnalyzer flightStatsAnalyzer;

    public List<FlightFactor> processFlightData(Flight flight, WeightCharacteristics weightCharacteristics, TimeCharacteristics timeCharacteristics, String Straightening, String StraighteningROV, Double fuel_eff) {
        FullNavLog fullNavLog = flight.getFullNavLog();
        if (fullNavLog == null) return Collections.emptyList();

        FlightMetaInfo meta = metaDataExtractor.extractMetaData(flight, fullNavLog, timeCharacteristics);
        List<FlightFactor> factors = new ArrayList<>();
        factors.add(GeneralInfoBuilder.build(flight, meta));
        List<FlightFactor> sourceFactors = new ArrayList<>();

        processSourceNavLog(fullNavLog, sourceFactors, meta);

        processActualNavLog(fullNavLog, factors, meta);

        String deviation = deviationAnalyzer.analyzeDeviation(sourceFactors, factors);
        fuelEfficiencyCalculator.calculateAndSetEfficiency(factors, weightCharacteristics, timeCharacteristics, flight);

        addFuelAndTimeInfo(flight, weightCharacteristics, timeCharacteristics, fullNavLog, factors, meta, deviation, Straightening, StraighteningROV, fuel_eff);

        return factors;
    }

    private void processActualNavLog(FullNavLog fullNavLog, List<FlightFactor> factors, FlightMetaInfo meta) {
        if (hasNavItems(fullNavLog.getActualNavLog())) {
            FlightStats stats = flightStatsAnalyzer.analyzeFlightData(fullNavLog.getActualNavLog().getNavLogItem());
            processNavLogItems(fullNavLog.ActualNavLog.getNavLogItem(), factors, true, meta);
            updateGeneralInfoStats(factors, stats);
        }
    }

    private void updateGeneralInfoStats(List<FlightFactor> factors, FlightStats stats) {
        if (!factors.isEmpty()) {
            FlightFactor generalInfo = factors.get(0);
            generalInfo.setTemperature(stats.getTemperatureSummary());
            generalInfo.setFlightLevel(stats.getMostCommonFlightLevel());
            generalInfo.setWind(stats.getAverageWind());
        }
    }

    private void processSourceNavLog(FullNavLog fullNavLog, List<FlightFactor> factors, FlightMetaInfo meta) {
        if (hasNavItems(fullNavLog.getSourceNavLog())) {
            processNavLogItems(fullNavLog.getSourceNavLog().getNavLogItem(), factors, false, meta);
        }
    }

    private boolean hasNavItems(SourceNavLog navLog) {
        return navLog != null && navLog.getNavLogItem() != null && !navLog.getNavLogItem().isEmpty();
    }

    private boolean hasNavItems(ActualNavLog navLog) {
        return navLog != null && navLog.getNavLogItem() != null && !navLog.getNavLogItem().isEmpty();
    }

}