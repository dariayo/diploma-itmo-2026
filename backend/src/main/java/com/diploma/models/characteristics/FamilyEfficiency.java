package com.diploma.models.characteristics;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class FamilyEfficiency {
    private String family;
    private Double fuelEfficiency;
    private Double sumefficiency;
    private Double count;

    public FamilyEfficiency(String family, Double sumefficiency, Double count, Double fuelEfficiency){
        this.family = family;
        this.sumefficiency = 0.0;
        this.count = 0.0;
        this.fuelEfficiency = 0.0;
    }
}
