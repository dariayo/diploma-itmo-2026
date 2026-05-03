package com.diploma.handler;

import com.diploma.models.*;
import com.diploma.models.characteristics.MassesAndFuel;
import com.diploma.models.characteristics.TimeCharacteristics;
import com.diploma.models.characteristics.WeightCharacteristics;
import com.diploma.models.flights.*;
import com.diploma.models.xml.EndParams;
import com.diploma.models.xml.FullNavLog;
import com.diploma.models.xml.StartParams;

import java.time.Duration;
import java.util.List;

import static com.diploma.util.TimeFormatterUtils.formatTime;

public class FuelTimeInfoBuilder {

    public static void addFuelAndTimeInfo(Flight flight, WeightCharacteristics weightCharacteristics, TimeCharacteristics timeCharacteristics, FullNavLog fullNavLog, List<FlightFactor> factors, FlightMetaInfo metaInfo, String deviation, String Straightening, String StraighteningROV, Double fuel_eff) {
        addStartFuelInfo(fullNavLog, factors, metaInfo);
        addEndFuelInfo(flight, fullNavLog, weightCharacteristics, timeCharacteristics, factors, metaInfo, deviation, Straightening, StraighteningROV, fuel_eff);
        addFuelSummary(flight, factors, metaInfo);
        addMassInfo(flight, weightCharacteristics, factors, metaInfo);
    }

    private static void addStartFuelInfo(FullNavLog fullNavLog, List<FlightFactor> factors, FlightMetaInfo metaInfo) {
        StartParams start = fullNavLog.getStartParams();
        if (start == null) return;

        FlightFactor startFactor = new FlightFactor();
        startFactor.setFlightNumber(metaInfo.flightNumber());
        startFactor.setDate(metaInfo.date());
        startFactor.setType_mod(metaInfo.type_mod());
        startFactor.setCaptain(metaInfo.captain());
        startFactor.setTab(metaInfo.tab());
        startFactor.setSeason(String.valueOf(metaInfo.season()));
        startFactor.setPoint("START");
        startFactor.setActualTime(formatTime(start.getATOT()));
        startFactor.setActualFuel(start.getActualTotalFuel() != null ? start.getActualTotalFuel() + " kg" : "N/A");
        startFactor.setEstimatedFuel(start.getOFPTotalFuel() != null ? start.getOFPTotalFuel() + " kg" : "N/A");
        startFactor.setPlannedFuel(start.getOFPTaxiFuel() != null ? start.getOFPTaxiFuel() + " kg (taxi)" : "N/A");

        StringBuilder startNotes = new StringBuilder("Engine start: " + formatTime(start.getEngineStart()));
        if (metaInfo.taxiOutTime() != null) {
            startFactor.setTaxiTime(formatTime(metaInfo.taxiOutTime()));
            startNotes.append(", Taxi-out time: ").append(formatTime(metaInfo.taxiOutTime()));
        }
        startFactor.setNotes(startNotes.toString());

        factors.add(startFactor);
    }

    private static void addEndFuelInfo(Flight flight, FullNavLog fullNavLog, WeightCharacteristics weightCharacteristics, TimeCharacteristics characteristics, List<FlightFactor> factors, FlightMetaInfo metaInfo, String deviation, String Straightening, String StraighteningROV, Double fuel_eff) {
        EndParams end = fullNavLog.getEndParams();
        StartParams start = fullNavLog.getStartParams();
        if (end == null || start == null) return;

        FlightFactor endFactor = new FlightFactor();
        endFactor.setFlightNumber(metaInfo.flightNumber());
        endFactor.setDate(metaInfo.date());
        endFactor.setCaptain(metaInfo.captain());
        endFactor.setTab(metaInfo.tab());
        endFactor.setPoint("END");
        endFactor.setSeason(String.valueOf(metaInfo.season()));
        endFactor.setActualTime(formatTime(end.getATL()));
        endFactor.setActualFuel(end.getRemFuelAfterLand() != null ? end.getRemFuelAfterLand() + " kg" : "N/A");

        StringBuilder endNotes = new StringBuilder();
        if (end.getEngineOff() != null) {
            endNotes.append("Engine off: ").append(formatTime(end.getEngineOff()));
        }
        if (end.getRW() != null) {
            endNotes.append(!endNotes.isEmpty() ? ", " : "").append("RW: ").append(end.getRW());
        }
        if (metaInfo.totalTaxiTime() != null) {
            endFactor.setTaxiTime(formatTime(metaInfo.totalTaxiTime()));
            endNotes.append(!endNotes.isEmpty() ? ", " : "").append("Taxi-in time: ").append(formatTime(metaInfo.taxiInTime()));
        }
        endFactor.setNotes(endNotes.toString());

        Integer plannedFuelTotal = start.getOFPTotalFuel();
        Integer actualFuelTotal = start.getActualTotalFuel();
        if (plannedFuelTotal != null && actualFuelTotal != null && end.getRemFuelAfterLand() != null) {
            int actualConsumption = actualFuelTotal - end.getRemFuelAfterLand();
            endFactor.setFuelConsumption(actualConsumption + " kg");

            if (plannedFuelTotal != 0) {
                //int plannedConsumption = flight.getMassesAndFuel().getTripFuel();
                //int massFact = weightCharacteristics.calculatePayload(metaInfo.season());
                double efficiency = fuel_eff;
                endFactor.setFuelEfficiency(String.format("%.2f%%", efficiency));
            }
        }
        endFactor.setDeviation(deviation);
        endFactor.setStraightening(Straightening);
        endFactor.setStraighteningROV(StraighteningROV);
        factors.add(endFactor);
    }

    private static double getEfficiency(Flight flight, int massFact, TimeCharacteristics timeCharacteristics, int actualConsumption, int plannedConsumption) {
        double efficiency;
        Integer massOFP = flight.getMassesAndFuel().getMassOfp();

        if (massOFP != 0 & massFact != 0) {
            double Pl = plannedConsumption - 32.67 * (massOFP - massFact) / 1000 * Duration.between(timeCharacteristics.getDTAOBT(),timeCharacteristics.getDTATA()).getSeconds()/3600;
            efficiency = (2 - (double) (actualConsumption) / Pl) * 100;
        } else {
            efficiency = ((double) (plannedConsumption - actualConsumption) / plannedConsumption + 1) * 100;
        }
        return efficiency;
    }

    private static void addFuelSummary(Flight flight, List<FlightFactor> factors, FlightMetaInfo metaInfo) {
        MassesAndFuel masses = flight.getMassesAndFuel();
        if (masses == null) return;

        FlightFactor fuelFactor = new FlightFactor();
        fuelFactor.setFlightNumber(metaInfo.flightNumber());
        fuelFactor.setDate(metaInfo.date());
        fuelFactor.setCaptain(metaInfo.captain());
        fuelFactor.setTab(metaInfo.tab());
        fuelFactor.setPoint("FUEL SUMMARY");

        StringBuilder fuelInfo = new StringBuilder();
        if (masses.getOFPTotalFuel() != null) {
            fuelInfo.append("Planned: ").append(masses.getOFPTotalFuel()).append(" kg");
        }
        if (masses.getTripFuel() != null) {
            fuelInfo.append(!fuelInfo.isEmpty() ? ", " : "").append("Trip: ").append(masses.getTripFuel()).append(" kg");
        }
        if (masses.getFuelRemAfterLand() != null) {
            fuelInfo.append(!fuelInfo.isEmpty() ? ", " : "").append("Remaining: ").append(masses.getFuelRemAfterLand()).append(" kg");
        }

        fuelFactor.setNotes(fuelInfo.toString());
        factors.add(fuelFactor);
    }

    private static void addMassInfo(Flight flight, WeightCharacteristics weightCharacteristics, List<FlightFactor> factors, FlightMetaInfo metaInfo) {
        MassesAndFuel masses = flight.getMassesAndFuel();
        if (masses == null) return;
        FlightFactor massFactor = new FlightFactor();
        massFactor.setPoint("PYLD");
        massFactor.setFlightNumber(metaInfo.flightNumber());
        massFactor.setDate(metaInfo.date());
        massFactor.setCaptain(metaInfo.captain());
        massFactor.setTab(metaInfo.tab());
        StringBuilder massInfo = new StringBuilder();
        if (masses.getMassOfp() != null) {
            massInfo.append("Max_PYLD: ").append(masses.getMassOfp()).append(" kg ");
        }
        if (masses.getMassFact() != null) {
            massInfo.append("Actual_PYLD: ").append(weightCharacteristics.calculatePayload(metaInfo.season())).append(" kg");
        }
        massFactor.setNotes(massInfo.toString());
        factors.add(massFactor);
    }
}