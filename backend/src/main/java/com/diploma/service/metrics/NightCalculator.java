package com.diploma.service.metrics;

import org.springframework.stereotype.Component;
import com.diploma.dto.CalculateData;
import com.diploma.models.Airport;
import com.diploma.util.SolarisUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

@Component
public class NightCalculator implements DifficultyCalc {
    SolarisUtils utils = new SolarisUtils();

    @Override
    public Map<String, Double> calculate(CalculateData data) {
        var td = data.getTd();

        Instant dtaTot = td.getDTATOT().atZone(ZoneId.of("UTC")).toInstant();
        Instant dtaTl = td.getDTATL().atZone(ZoneId.of("UTC")).toInstant();
        Instant date = data.getDateTime().atZone(ZoneId.of("UTC")).toInstant();

        Airport from = data.getFrom();
        Airport to = data.getTo();

        double fromLat = from.lat();
        double fromLon = from.lon();

        double toLat = to.lat();
        double toLon = to.lon();
        SolarisUtils.SolarisMoments smFrom = utils.calculate(date, fromLat, fromLon);

        Instant darkFrom = smFrom.dark();

        SolarisUtils.SolarisMoments smTo = utils.calculate(date, toLat, toLon);

        Instant sunriseTo = smTo.sunrise();
        Instant darkTo = smTo.dark();

        double takeoffAtNight = dtaTot.isAfter(darkFrom) ? 1.0 : 0.0;

        double landingAtNight = dtaTl.isBefore(sunriseTo) ? 1.0 : 0.0;

        double flightAtNight = (dtaTot.isAfter(darkFrom) || dtaTl.isBefore(darkTo)) ? 1.0 : 0.0;

        double nightPercent = (takeoffAtNight + landingAtNight + flightAtNight) / 3;

        return Map.of("night", nightPercent);
    }

}
