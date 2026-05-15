package com.diploma.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.diploma.dto.PilotStatsResponse;
import com.diploma.service.pilots.PilotStatsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pilots")
@RequiredArgsConstructor
public class PilotStatsController {
    private final PilotStatsService pilotStatsService;

    @GetMapping("/stats")
    public ResponseEntity<PilotStatsResponse> getPilotStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Убрали параметр limit

        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(null);
        }

        PilotStatsResponse stats = pilotStatsService.getPilotStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statsNew")
    public ResponseEntity<PilotStatsResponse> getPilotStatsNew(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws JsonProcessingException {
        // Убрали параметр limit

        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(null);
        }

        PilotStatsResponse stats = pilotStatsService.getPilotStatsFromGroupDt(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statsByRoutes")
    public ResponseEntity<PilotStatsResponse> getPilotStatsByRoutes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<String> routes) throws JsonProcessingException {
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(null);
        }

        PilotStatsResponse stats = pilotStatsService.getPilotStatsFromGroupDtByRoutes(startDate, endDate, routes);
        return ResponseEntity.ok(stats);
    }

}