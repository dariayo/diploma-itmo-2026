package com.diploma.service.flights;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.diploma.entities.FlightXMLGroup;
import com.diploma.models.flights.*;
import com.diploma.repository.FlightXMLGroupRepository;

import java.time.LocalDate;
import java.util.*;

import static com.diploma.util.SeasonUtils.getSeason;

@Service
@AllArgsConstructor
public class FlightDescriptionService {
    private final FlightXMLGroupRepository flightXMLGroupRepository;

    public List<FlightEff> getPilotData(int tabNo, LocalDate startDate, LocalDate endDate) throws JsonProcessingException {
        List<FlightXMLGroup> flightXMLGroups = flightXMLGroupRepository.findByDateBetweenAndTabNo(startDate.atStartOfDay(), endDate.atStartOfDay(), tabNo);
        List<FlightEff> flightEffs = new ArrayList<>();
        for (FlightXMLGroup flightXMLGroup : flightXMLGroups) {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Map<String, Double>> typeRef = new TypeReference<Map<String, Double>>() {
            };
            Map<String, Double> calculatedResults = objectMapper.readValue(flightXMLGroup.getCalculatedResults(), typeRef);
            FlightEff fe = FlightEff.builder().
                    tabNo(tabNo).
                    flightNumber(flightXMLGroup.getFlight_number()).
                    route(flightXMLGroup.getRoute()).
                    typeMod(flightXMLGroup.getTs()).
                    fuelEff(flightXMLGroup.getFuel_efficiency()).
                    season(getSeason(flightXMLGroup.getDate().toString())).
                    dateTime(flightXMLGroup.getDate()).
                    calculatedResults(calculatedResults).
                    flightHours(flightXMLGroup.getFlightHours()).
                    build();
            flightEffs.add(fe);
        }
        return flightEffs;
    }

}