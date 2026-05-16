package com.diploma.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pilot_route_directory", schema = "dbo")
@NoArgsConstructor
public class RouteDirectory {
    @Id
    @Column(name = "route_code")
    private String routeCode;

    @Column(name = "departure_airport")
    private String departureAirport;

    @Column(name = "arrival_airport")
    private String arrivalAirport;

    @Column(name = "distance_nm")
    private Integer distanceNm;
}
