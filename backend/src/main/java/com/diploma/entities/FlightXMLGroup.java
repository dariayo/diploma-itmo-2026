package com.diploma.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import com.diploma.util.JsonbConverter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Entity
@Table(name = "flight_description", schema = "dbo")
@NoArgsConstructor
public class FlightXMLGroup {
    @Id
    private Long idmrshr;
    private String flight_number;
    private String route;
    private String ts;
    private String captain;
    private LocalDateTime date;
    private String flight_level;
    private String temperature;
    private String trip_fuel;
    @Column(name = "payload_ofp")
    private String payloadOFP;
    @Column(name = "payload_fact")
    private String payloadFact;
    private String fuel_consumption;
    private String fuel_estimated;
    private Double fuel_efficiency;
    private String deviation;
    @Column(name = "is_straightening")
    private String isStraightening;
    @Column(name = "is_straighteningrov")
    private String isStraighteningROV;
    @Column(name = "percent_pyld")
    private Double percentPYLD;
    @Column(name = "tab_no")
    private Integer tabNo;
    @Column(name = "planned_departure_taxi_time")
    private Integer plannedDepartureTaxiTime;
    @Column(name = "planned_arrival_taxi_time")
    private Integer plannedArrivalTaxiTime;
    @Column(name = "actual_departure_taxi_time")
    private Integer actualDepartureTaxiTime;
    @Column(name = "actual_arrival_taxi_time")
    private Integer actualArrivalTaxiTime;
    private Integer distance;
    private Double commercial_efficiency;
    private String hold_zone;
    private String hold_time;
    @Column(name = "taxi_fuel_start_actual")
    private Integer taxiFuelStartActual;
    @Column(name = "taxi_fuel_end_actual")
    private Integer taxiFuelEndActual;
    private String actual_fuel;
    @Column(name = "planned_fuel_departure")
    private Integer plannedFuelDeparture;
    @Column(name = "fuel_deviation_departure")
    private Integer fuelDeviationDeparture;
    @Column(name = "planned_fuel_arrival")
    private Integer plannedFuelArrival;
    @Column(name = "fuel_deviation_arrival")
    private Integer fuelDeviationArrival;
    @Convert(converter = JsonbConverter.class)
    @Column(name = "calculated_results", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String calculatedResults;
    @Column(name = "flight_hours")
    private double flightHours;
    @Column(name = "kover_time")
    private Integer koverTime;
    @Column(name = "fuel_price_rub")
    private Double fuelPriceRub;
}
