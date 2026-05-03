package com.diploma.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.diploma.models.Airport;
import com.diploma.models.characteristics.TimeCharacteristics;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Setter
public class CalculateData {
    private LocalDateTime dateTime;
    private Long id_mrshr;
    private String typeMod;
    private double distance;
    private TimeCharacteristics td;
    private String weatherData;
    private double fuelEff;
    private Airport from;
    private Airport to;

    @Override
    public String toString() {
        return "CalculateData{" +
                "id_mrshr=" + id_mrshr +
                ", typeMod='" + typeMod + '\'' +
                ", distance=" + distance +
                ", fuelEff=" + fuelEff +
                ", td=" + td +
                '}';
    }
}

