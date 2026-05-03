package com.diploma.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class aircrafts {
    private Integer max_range_nm;
    private Integer taxi_fuel_kg_per_min;
    private String type;

}
