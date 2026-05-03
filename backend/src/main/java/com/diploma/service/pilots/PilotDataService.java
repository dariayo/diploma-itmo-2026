package com.diploma.service.pilots;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.diploma.models.characteristics.TimeCharacteristics;
import com.diploma.models.flights.Flight;
import com.diploma.models.flights.FlightEff;
import com.diploma.parser.Parser;
import com.diploma.entities.*;
import com.diploma.models.*;
import com.diploma.repository.ApprovedTimesRepository;
import com.diploma.service.flights.FlightXMLAllParamsService;
import com.diploma.dto.CalculateData;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.diploma.util.SeasonUtils.getSeason;

@Service
@RequiredArgsConstructor
public class PilotDataService {
    private final FlightXMLAllParamsService flightXMLAllParamsService;
    private final Parser parser;
    private final PilotEfficiencyCalculator pilotEfficiencyCalculator;
    private final ApprovedTimesRepository approvedTimesRepository;

    public List<FlightEff> getPilotData(int tabNo, LocalDate startDate, LocalDate endDate) throws IOException {

        List<FlightXMLAllParams> flights = flightXMLAllParamsService.getByDateRangeAndTabNo(startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1), tabNo);
        List<FlightEff> result = new ArrayList<>();
        for (FlightXMLAllParams flight : flights) {
            if (flight != null) {
                Flight f = parser.parseFlightXML(flight.getFlightXml());

                TimeCharacteristics timeCharacteristics = TimeCharacteristics.buildByFlightXMLAllParams(flight);
                try {
                    LocalDateTime[] adt = approvedTimesRepository.getApprovedTimes(flight.getIdmrshr());
                    timeCharacteristics.setDTAD(adt[0]);
                    timeCharacteristics.setDTAA(adt[1]);
                } catch (RuntimeException e) {
                    timeCharacteristics.setDTAD(timeCharacteristics.getDTVO());
                    timeCharacteristics.setDTAA(timeCharacteristics.getDTVP());
                }

                CalculateData cd = getCalculateData(flight, timeCharacteristics, f);

                double flightHours = calculateFlightHours(f);
                FlightEff fe = FlightEff.builder().
                        tabNo(tabNo).
                        flightNumber(flight.getNr()).
                        route(flight.getRoute()).
                        typeMod(f.type_mod).
                        fuelEff(flight.getFuel_eff()).
                        season(getSeason(timeCharacteristics.getDTVO().toString())).
                        dateTime(flight.getDk()).
                        calculatedResults(pilotEfficiencyCalculator.calculateAll(cd)).
                        flightHours(flightHours).
                        build();
                result.add(fe);
            }
        }
        return result;
    }

    public CalculateData getCalculateData(FlightXMLAllParams flight, TimeCharacteristics timeCharacteristics, Flight f) {
        CalculateData cd = new CalculateData();
        cd.setId_mrshr(flight.getIdmrshr());
        cd.setTd(timeCharacteristics);
        cd.setWeatherData(flight.getMeteo());
        cd.setDateTime(flight.getDTVO());
        cd.setFrom(new Airport(f.iata1, f.icao1, Double.parseDouble(f.lat1), Double.parseDouble(f.lon1)));
//        cd.setFrom(new Airport(f.iata1, f.icao1, 0.0, 0.0));
        cd.setTo(new Airport(f.iata2, f.icao2, Double.parseDouble(f.lat2), Double.parseDouble(f.lon2)));
//        cd.setTo(new Airport(f.iata2, f.icao2, 0.0, 0.0));
        cd.setDistance(f.fullNavLog.fullNavLogCommon.getDistance());
        cd.setTypeMod(f.type_mod);
        cd.setFuelEff(flight.getFuel_eff());
        return cd;
    }

    public double calculateFlightHours(Flight flight) {
        LocalDateTime start = flight.getFullNavLog().getStartParams().getEngineStart();
        LocalDateTime end = flight.getFullNavLog().getEndParams().getEngineOff();

        if (start != null && end != null) {
            return (double) Duration.between(start, end).toMinutes() / 60;
        }
        return 0.0;
    }

}

