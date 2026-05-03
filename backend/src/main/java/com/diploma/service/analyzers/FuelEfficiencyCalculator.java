package com.diploma.service.analyzers;

import org.springframework.stereotype.Service;
import com.diploma.enums.Season;
import com.diploma.handler.TaxiTimeCalculator;
import com.diploma.models.characteristics.TimeCharacteristics;
import com.diploma.models.characteristics.WeightCharacteristics;
import com.diploma.models.flights.Flight;
import com.diploma.models.flights.FlightFactor;

import com.diploma.models.xml.FullNavLog;
import com.diploma.models.xml.StartParams;

import java.time.Duration;
import java.util.List;

@Service
public class FuelEfficiencyCalculator {

    public void calculateAndSetEfficiency(List<FlightFactor> factors, WeightCharacteristics weightCharacteristics, TimeCharacteristics timeCharacteristics, Flight flight) {
        FullNavLog fullNavLog = flight.fullNavLog;

        EfficiencyCalculatorContext context = new EfficiencyCalculatorContext();
        context.fuel=fullNavLog.startParams.ActualTotalFuel;
        Integer massOFP = flight.getMassesAndFuel().getMassOfp();
        Integer massFact = weightCharacteristics.calculatePayload(Season.valueOf(factors.get(0).getSeason()));
        double effectStart = calculateStartEffect(fullNavLog, timeCharacteristics, massOFP, massFact);
        for (int i = 0; i < factors.size(); i++) {
            if (i + 1 < factors.size()) {
                handleNonTerminalPoints(factors, fullNavLog, i, context, effectStart, massOFP, massFact);
            } else {
                handleTerminalPoints(factors, fullNavLog, i, context, massOFP, massFact);
            }
            if (i > 1) {
                handleClimbPhase(factors, fullNavLog, context, i, massOFP, massFact);
            }
        }
    }

    private static class EfficiencyCalculatorContext {
        int clbIndex = 0;
        int dscIndex = 0;
        int fuel = 0;
    }

    private void handleTerminalPoints(List<FlightFactor> factors, FullNavLog fullNavLog, int i, EfficiencyCalculatorContext context, int massOFP, int massFact) {
        if (context.dscIndex == 0) return;
        int lastValidIndex = i;
        while (factors.get(lastValidIndex).getActualFuelInt() == null) {
            lastValidIndex--;
        }

        double effectDsc = calculateDescentEffect(factors, context.dscIndex, lastValidIndex, context.clbIndex, massOFP, massFact, context);
        factors.get(lastValidIndex).setFuelEfficiency(formatEfficiencyPercentage(effectDsc));
        factors.set(lastValidIndex, factors.get(lastValidIndex));
    }

    private double calculateDescentEffect(List<FlightFactor> factors, int dscIndex, int lastValidIndex, int clbIndex, int massOFP, int massFact, EfficiencyCalculatorContext context) {
        int actualFuelDiff = factors.get(dscIndex).getActualFuelInt() - factors.get(lastValidIndex).getActualFuelInt();
        int plannedFuelDiff = factors.get(clbIndex).getPlannedFuelInt() - factors.get(lastValidIndex).getPlannedFuelInt();
        try {
            double Pl = plannedFuelDiff - 32.67 * (massOFP - massFact) / 1000 * Duration.between(factors.get(dscIndex).getActualLocalDateTime(),factors.get(lastValidIndex).getActualLocalDateTime()).getSeconds()/3600;
            return (2 - (double) (actualFuelDiff) / Pl);
        } catch (Exception e) {
            return 2 - (double) actualFuelDiff / plannedFuelDiff * massOFP / massFact;
        }
    }

    private void handleNonTerminalPoints(List<FlightFactor> factors, FullNavLog fullNavLog, int i, EfficiencyCalculatorContext context, double effectStart, int massOFP, int massFact) {
        FlightFactor currFactor = factors.get(i);
        FlightFactor nextFactor = factors.get(i + 1);

        if (!currFactor.getFlightLevel().equals("CLB") && nextFactor.getFlightLevel().equals("CLB")) {
            currFactor.setFuelEfficiency(formatEfficiencyPercentage(effectStart));
            factors.set(i, currFactor);
        }

        if (!currFactor.getFlightLevel().equals("CLB") && !currFactor.getFlightLevel().equals("DSC") && !currFactor.getFlightLevel().equals("DES") && (nextFactor.getFlightLevel().equals("DSC") || nextFactor.getFlightLevel().equals("DES"))) {
            if (context.clbIndex == 0) {
                return;
            }
            double effectFlight = calculateFlightEffect(factors, context.clbIndex, i, massOFP, massFact, context);
            context.dscIndex = i;
            currFactor.setFuelEfficiency(formatEfficiencyPercentage(effectFlight));
            factors.set(i, currFactor);
        }

    }

    private void handleClimbPhase(List<FlightFactor> factors, FullNavLog fullNavLog, EfficiencyCalculatorContext context, int i, int massOFP, int massFact) {
        if (!factors.get(i).getFlightLevel().equals("CLB") && factors.get(i - 1).getFlightLevel().equals("CLB")) {
            int s = 1;
            while (factors.get(s).getActualFuelInt() == null) {
                s++;
            }
            int j = i;
            while (factors.get(j).getActualFuelInt() == null) {
                j++;
            }

            double effectClb = calculateClimbEffect(factors, fullNavLog.startParams, s, j, massOFP, massFact, context);

            context.clbIndex = j;
            factors.get(i - 1).setFuelEfficiency(formatEfficiencyPercentage(effectClb));
        }
    }

    private double calculateClimbEffect(List<FlightFactor> factors, StartParams startParams, int startIndex, int endIndex, int massOFP, int massFact, EfficiencyCalculatorContext context) {
        int actualFuelDiff = factors.get(startIndex).getActualFuelInt() - factors.get(endIndex).getActualFuelInt() - startParams.ActualTaxiFuel;
        int plannedFuelDiff = factors.get(startIndex).getPlannedFuelInt() - factors.get(endIndex).getPlannedFuelInt() - startParams.OFPTaxiFuel;
        try {
            double Pl = plannedFuelDiff - 32.67 * (massOFP - massFact) / 1000 * Duration.between(factors.get(startIndex).getActualLocalDateTime(),factors.get(endIndex).getActualLocalDateTime()).getSeconds()/3600;
            return (2 - (double) (actualFuelDiff) / Pl);
        } catch(Exception e) {
            return 2 - (double) actualFuelDiff / plannedFuelDiff * massOFP / massFact;
        }
    }

    private double calculateFlightEffect(List<FlightFactor> factors, int clbIndex, int i, int massOFP, int massFact, EfficiencyCalculatorContext context) {
        try {
            int actualFuelDiff = factors.get(clbIndex).getActualFuelInt() - factors.get(i).getActualFuelInt();
            int plannedFuelDiff = factors.get(clbIndex).getPlannedFuelInt() - factors.get(i).getPlannedFuelInt();
            double Pl = plannedFuelDiff - 32.67 * (massOFP - massFact) / 1000 * Duration.between(factors.get(clbIndex).getActualLocalDateTime(),factors.get(i).getActualLocalDateTime()).getSeconds()/3600;
            return (2 - (double) (actualFuelDiff) / Pl);
        } catch (Exception e){
            return 2 - (double) (factors.get(clbIndex).getActualFuelInt()
                    - factors.get(i).getActualFuelInt()) /
                    (factors.get(clbIndex).getPlannedFuelInt()
                            - factors.get(i).getPlannedFuelInt()) * massOFP / massFact;
        }
    }

    private double calculateStartEffect(FullNavLog fullNavLog, TimeCharacteristics timeCharacteristics, int massOFP, int massFact) {
        int actualFuelDiff = fullNavLog.startParams.ActualTaxiFuel;
        int plannedFuelDiff = fullNavLog.startParams.OFPTaxiFuel;
        var taxi = TaxiTimeCalculator.calculateTaxiOutTime(fullNavLog, timeCharacteristics);
        if(taxi!=null) {
            double Pl = plannedFuelDiff - 32.67 * (massOFP - massFact) / 1000 * (double) taxi / 60;
            return (2 - (double) (actualFuelDiff) / Pl);
        } else {
            return 2 - (double) fullNavLog.startParams.ActualTaxiFuel / fullNavLog.startParams.OFPTaxiFuel * massOFP / massFact;
        }
    }

    private String formatEfficiencyPercentage(double value) {
        return String.format("%.2f%%", value * 100);
    }
}
