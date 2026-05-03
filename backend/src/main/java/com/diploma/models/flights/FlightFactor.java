package com.diploma.models.flights;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class FlightFactor {
    private String flightNumber;
    private String captain;
    private String type_mod;
    private String date;
    private String point;
    private String flightLevel;
    private String routeType;
    private String wind;
    private String temperature;
    private String plannedFuel;
    private String actualFuel;
    private String fuelConsumption;
    private String fuelEfficiency;
    private String estimatedFuel;
    private String estimatedTime;
    private String actualTime;
    private String notes;
//    private String OFPCompliant;
    private String creationTime;
    private boolean isActual;
    private String aircraftLoad;
    private String taxiTime;
    private String gearExtension;
    private String season;
    private String deviation;
    private String straightening;
    private String straighteningROV;
    private Integer tab;
    private Integer taxiFuelStart;

    public Integer getActualFuelInt(){
        try{
            String numberString = this.actualFuel.replaceAll("[^0-9]", "");
            return Integer.parseInt(numberString);
        } catch (Exception e){
            return null;
        }
    }

    public Integer getPlannedFuelInt(){
        try{
            String numberString = this.plannedFuel.replaceAll("[^0-9]", "");
            return Integer.parseInt(numberString);
        } catch (Exception e){
            return null;
        }
    }
    public LocalTime getActualLocalDateTime(){
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(this.actualTime, formatter);
        } catch (Exception e){
            return null;
        }
    }
}