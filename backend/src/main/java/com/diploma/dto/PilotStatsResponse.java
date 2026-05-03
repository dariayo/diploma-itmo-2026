package com.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.diploma.models.pilots.PilotStats;

import java.util.List;

@Data
@AllArgsConstructor
public class PilotStatsResponse {
    private List<PilotStats> pilots;
    private double averageEfficiency;
    private double totalFlightHours;
}
