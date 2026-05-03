package com.diploma.handler;

import com.diploma.models.characteristics.FuelCheck;
import com.diploma.models.characteristics.Internals;
import com.diploma.models.flights.FlightFactor;
import com.diploma.models.flights.FlightMetaInfo;
import com.diploma.models.xml.*;

import java.util.List;

import static com.diploma.util.TimeFormatterUtils.formatTime;

public class NavLogProcessor {
    public static void processNavLogItems(List<NavLogItem> items, List<FlightFactor> factors,
                                          boolean isActual, FlightMetaInfo metaInfo) {
        for (NavLogItem item : items) {
            FlightFactor factor = new FlightFactor();

            factor.setFlightNumber(metaInfo.flightNumber());
            factor.setDate(metaInfo.date());
            factor.setType_mod(metaInfo.type_mod());
            factor.setCaptain(metaInfo.captain());
            factor.setTab(metaInfo.tab());
            factor.setSeason(String.valueOf(metaInfo.season()));
            if (metaInfo.totalTaxiTime() != null) {
                factor.setTaxiTime(formatTime(metaInfo.totalTaxiTime()));
            }

            if (item.getCommon() != null) {
                Common common = item.getCommon();
                factor.setPoint(common.getTo() != null ? common.getTo() : "UNKNOWN");
                factor.setFlightLevel(common.getFl() != null ? common.getFl() : "N/A");
                factor.setRouteType(common.getPointType() != null ? common.getPointType() : "N/A");

                String windStr = "";
                if (!common.getWindSpeed().isEmpty() && !common.getWindAngle().isEmpty()) {
                    windStr = common.getWindSpeed() + "/" + common.getWindAngle();
                } else if (!common.getWindSpeed().isEmpty()) {
                    windStr = common.getWindSpeed()+"/";
                } else if (!common.getWindAngle().isEmpty()) {
                    windStr =  "/" + common.getWindSpeed();
                }
                factor.setWind(windStr);

                factor.setTemperature(common.getTemp() != null ? common.getTemp() + "°C" : "N/A");
            }

            if (item.getEstimated() != null && !isActual) {
                Estimated estimated = item.getEstimated();
                factor.setEstimatedFuel(estimated.getOFPFuel() != null ? estimated.getOFPFuel() + " kg" : "N/A");
                factor.setEstimatedTime(formatTime(estimated.getOFPtt()));
                factor.setPlannedFuel(estimated.getOFPFuelRemain() != null ? estimated.getOFPFuelRemain() + " kg" : "N/A");
            }

            if (item.getActual() != null && isActual) {
                Actual actual = item.getActual();
                factor.setActualFuel(actual.getActualFuelRem() != null ? actual.getActualFuelRem() + " kg" : "N/A");
                factor.setActualTime(formatTime(actual.getActualTT()));
                if (actual.getNote() != null) {
                    factor.setNotes(actual.getNote());
                }
                Estimated estimated = item.getEstimated();
                //Удалён из-за значения N/A
                //factor.setEstimatedTime(formatTime(estimated.getOFPtt()));
                factor.setPlannedFuel(estimated.getOFPFuelRemain() != null ? estimated.getOFPFuelRemain() + " kg" : "N/A");
            }

            if (item.getInternals() != null) {
                Internals internals = item.getInternals();
//                factor.setOFPCompliant(internals.getIsOFP());
                factor.setCreationTime(formatTime(internals.getCreationTime()));
            }

            if (item.getFuelCheck() != null) {
                FuelCheck fuelCheck = item.getFuelCheck();
                String fuelCheckInfo = "Fuel check: " +
                        (fuelCheck.getFuelUsed1() != null ? fuelCheck.getFuelUsed1() : "N/A") + "/" +
                        (fuelCheck.getFuelUsed2() != null ? fuelCheck.getFuelUsed2() : "N/A");
                //System.out.println(factor.getNotes());
                factor.setNotes((factor.getNotes() != null && !factor.getNotes().isEmpty())? factor.getNotes() + ", " + fuelCheckInfo : fuelCheckInfo);
            }

            factor.setActual(isActual);
            factors.add(factor);
        }
    }
}
