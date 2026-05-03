package com.diploma.models.flights;

import com.diploma.enums.Season;

public record FlightMetaInfo(
        String flightNumber,
        String date,
        String type_mod,
        String captain,
        Season season,
        Integer taxiOutTime,
        Integer taxiInTime,
        Integer totalTaxiTime,
        Integer tab
) {
}
