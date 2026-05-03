package com.diploma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.diploma.models.flights.FlightEff;
import com.diploma.service.flights.FlightDescriptionService;
import com.diploma.service.pilots.PilotDataService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pilotStatistics")
@RequiredArgsConstructor
public class PilotDataController {
    private final PilotDataService pilotDataService;
    private final FlightDescriptionService flightDescriptionService;

    @GetMapping("/getDataOld/{tabNo}")
    public List<FlightEff> getPilotDataOld(@PathVariable int tabNo,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        return pilotDataService.getPilotData(tabNo, startDate, endDate);
    }

    @GetMapping("/getPilotsDataOld")
    public Map<Integer, List<FlightEff>> getPilotsDataOld(@RequestParam List<Integer> tabs,
                                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        Map<Integer, List<FlightEff>> result = new java.util.LinkedHashMap<>();
        for (Integer tab : tabs) {
            if (tab == null) continue;
            List<FlightEff> data = pilotDataService.getPilotData(tab, startDate, endDate);
            result.put(tab, data);
        }

        return result;
    }

    @GetMapping("/getData/{tabNo}")
    public List<FlightEff> getPilotData(@PathVariable int tabNo,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        return flightDescriptionService.getPilotData(tabNo, startDate, endDate);
    }

    @GetMapping("/getPilotsData")
    public Map<Integer, List<FlightEff>> getPilotsData(@RequestParam List<Integer> tabs,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        Map<Integer, List<FlightEff>> result = new java.util.LinkedHashMap<>();
        for (Integer tab : tabs) {
            if (tab == null) continue;
            List<FlightEff> data = flightDescriptionService.getPilotData(tab, startDate, endDate);
            result.put(tab, data);
        }

        return result;
    }
}
