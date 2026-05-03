package com.diploma.service.analyzers;

import org.springframework.stereotype.Service;


import com.diploma.models.flights.FlightStats;
import com.diploma.models.xml.Common;

import com.diploma.models.xml.NavLogItem;

import java.util.List;

@Service
public class FlightStatsAnalyzer {
    public FlightStats analyzeFlightData(List<NavLogItem> navLogItems) {
        FlightStats stats = new FlightStats();

        for (NavLogItem item : navLogItems) {
            if (item.getCommon() != null) {
                analyzeCommonData(item.getCommon(), stats);
            }
        }

        return stats;
    }

    private void analyzeCommonData(Common common, FlightStats stats) {
        // Анализ температур
        if (common.getTemp() != null) {
            stats.addTemperature(common.getTemp());
        }

        // Анализ эшелонов
        if (common.getFl() != null && !common.getFl().isEmpty() && !common.getFl().equals("DSC") && !common.getFl().equals("DES") && !common.getFl().equals("CLB")) {
            stats.addFlightLevel(common.getFl());
        }

        if (!common.getWindAngle().isEmpty() && !common.getWindSpeed().isEmpty()) {
            String windStr = common.getWindAngle() + "/" + common.getWindSpeed();
            stats.addWind(windStr);
        }

    }
}
