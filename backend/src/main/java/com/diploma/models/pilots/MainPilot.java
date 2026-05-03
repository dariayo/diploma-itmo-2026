package com.diploma.models.pilots;

import lombok.Builder;
import lombok.Data;
import com.diploma.models.characteristics.FamilyEfficiency;


import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MainPilot {
    private LocalDateTime dt;
    private Integer totalfuel;
    private Double sumefficiency;
    private Integer countreis;
    private List<FamilyEfficiency> familyefficiencies;
    private List<PilotStats> pilots;
    private Double totalhours;
    private Integer countpilot;
}
