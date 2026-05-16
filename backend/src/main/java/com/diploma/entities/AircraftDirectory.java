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
@Table(name = "pilot_aircraft_directory", schema = "dbo")
@NoArgsConstructor
public class AircraftDirectory {
    @Id
    @Column(name = "aircraft_type")
    private String aircraftType;

    @Column(name = "aircraft_family")
    private String aircraftFamily;

    @Column(name = "max_range_nm")
    private Integer maxRangeNm;
}
