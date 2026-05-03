package com.diploma.service.flights;

import org.springframework.stereotype.Service;
import com.diploma.enums.Season;
import com.diploma.handler.TaxiTimeCalculator;

import com.diploma.models.characteristics.TimeCharacteristics;
import com.diploma.models.flights.Flight;
import com.diploma.models.flights.FlightMetaInfo;

import com.diploma.models.xml.FullNavLog;
import com.diploma.models.xml.Row;

import static com.diploma.util.SeasonUtils.getSeason;

@Service
public class FlightMetaDataExtractor {
    public FlightMetaInfo extractMetaData(Flight flight, FullNavLog fullNavLog, TimeCharacteristics timeCharacteristics) {
        String flightNumber = flight.getNr();
        String date = timeCharacteristics.getDTVO().toString();
        String captain = extractCaptainName(flight);
        Integer tab = extractTabNo(flight);
        Season season = getSeason(timeCharacteristics.getDTVO().toString());
        String type_mod = flight.getType_mod();

        Integer taxiOutTime = TaxiTimeCalculator.calculateTaxiOutTime(fullNavLog, timeCharacteristics);
        Integer taxiInTime = TaxiTimeCalculator.calculateTaxiInTime(fullNavLog, timeCharacteristics);
        Integer totalTaxiTime = TaxiTimeCalculator.calculateTotalTaxiTime(taxiOutTime, taxiInTime);

        return new FlightMetaInfo(
                flightNumber, date, type_mod, captain, season, taxiOutTime, taxiInTime, totalTaxiTime, tab
        );

    }

    private String extractCaptainName(Flight flight) {
        if (flight.getCrew_lst() != null && flight.getCrew_lst().getCapitan() != null) {
            Row cap = flight.getCrew_lst().getCapitan();
            return cap.getFam() + " " + cap.getName() + " " + cap.getOtch();
        }
        return null;
    }

    private Integer extractTabNo(Flight flight) {
        if (flight.getCrew_lst() != null && flight.getCrew_lst().getCapitan() != null) {
            Row cap = flight.getCrew_lst().getCapitan();
            return cap.getTab_no();
        }
        return null;
    }
}
