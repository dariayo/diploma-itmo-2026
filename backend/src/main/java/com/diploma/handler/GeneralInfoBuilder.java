package com.diploma.handler;


import com.diploma.models.flights.Flight;
import com.diploma.models.flights.FlightFactor;
import com.diploma.models.flights.FlightMetaInfo;

import static com.diploma.util.TimeFormatterUtils.formatTime;

public class GeneralInfoBuilder {
    public static FlightFactor build(Flight flight, FlightMetaInfo metaInfo) {
        FlightFactor factor = new FlightFactor();
        factor.setType_mod(metaInfo.type_mod());
        factor.setFlightNumber(metaInfo.flightNumber());
        factor.setDate(metaInfo.date());
        factor.setCaptain(metaInfo.captain());
        factor.setTab(metaInfo.tab());
        factor.setPoint(flight.getIata1() + " → " + flight.getIata2());
        factor.setSeason(String.valueOf(metaInfo.season()));

        StringBuilder generalNotes = new StringBuilder("Flight from " + flight.getApnm1() + " to " + flight.getApnm2());
        if (metaInfo.totalTaxiTime() != null) {
            generalNotes.append(", Total taxi time: ").append(formatTime(metaInfo.totalTaxiTime()));
            factor.setTaxiTime(formatTime(metaInfo.totalTaxiTime()));
        }
        factor.setNotes(generalNotes.toString());
        return factor;
    }
}
