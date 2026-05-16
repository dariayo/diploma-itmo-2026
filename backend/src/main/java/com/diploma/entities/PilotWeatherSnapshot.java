package com.diploma.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "pilot_weather_snapshots", schema = "dbo")
@NoArgsConstructor
public class PilotWeatherSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_flight_id", nullable = false)
    private Long sourceFlightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_flight_id", referencedColumnName = "idmrshr", insertable = false, updatable = false)
    private FlightXMLAllParams sourceFlight;

    @Column(name = "icao_code", nullable = false)
    private String icaoCode;

    @Column(name = "visibility_m")
    private Integer visibilityM;

    @Column(name = "wind_direction")
    private String windDirection;

    @Column(name = "wind_speed")
    private Integer windSpeed;

    @Column(name = "weather_phenomena")
    private String weatherPhenomena;

    @Column(name = "raw_report", columnDefinition = "text")
    private String rawReport;

    @Column(name = "observed_at")
    private LocalDateTime observedAt;
}
