package com.diploma.models.characteristics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import com.diploma.entities.FlightXMLAllParams;
import com.diploma.enums.Season;

@AllArgsConstructor
@Builder()
public class WeightCharacteristics {
    private int vzr;
    private int rm;
    private int rb;
    private int gruz;
    private int pochta;
    private int bag;

    public int calculatePayload(Season season) {
        if (season == Season.SUMMER)
            return  80 * vzr + 30 * rb + 15 * rm + bag + pochta + gruz;
        else if (season == Season.WINTER)
            return 85 * vzr + 30 * rb + 15 * rm + bag + pochta + gruz;
        return 0;
    }

    public static WeightCharacteristics buildByFlightXMLAllParams(FlightXMLAllParams flightXML){
        return WeightCharacteristics.builder()
                .vzr(flightXML.getVzr()).rb(flightXML.getRb()).rm(flightXML.getRm())
                .bag(flightXML.getBag()).pochta(flightXML.getPochta()).gruz(flightXML.getGruz()).build();

    }
}

