package com.diploma.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;


import com.diploma.models.characteristics.FamilyEfficiency;
import com.diploma.models.pilots.MainPilot;
import com.diploma.models.pilots.PilotStats;
import com.diploma.util.JsonbConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "pilot_period_aggregates", schema = "dbo")
@NoArgsConstructor
public class MainPilotGroupDt {
    @Id
    private LocalDateTime dt;
    private Integer totalfuel;
    private Double sumefficiency;
    private Integer countreis;
    @Convert(converter = JsonbConverter.class)
    @Column(name = "familyefficiency", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String familyefficiency;
    @Convert(converter = JsonbConverter.class)
    @Column(name = "pilots", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String pilots;
    private Double totalhours;
    private Integer countpilot;

    public MainPilotGroupDt(LocalDateTime dt) throws JsonProcessingException {
        this.dt = dt;
        this.totalfuel = 0;
        this.sumefficiency = 0.0;
        this.countreis = 0;
        List<FamilyEfficiency> familyEfficiencies = new ArrayList<>();
        familyEfficiencies.add(new FamilyEfficiency("A320",0.0,0.0,0.0));
        familyEfficiencies.add(new FamilyEfficiency("A319",0.0,0.0,0.0));
        familyEfficiencies.add(new FamilyEfficiency("B738",0.0,0.0,0.0));
        familyEfficiencies.add(new FamilyEfficiency("B739",0.0,0.0,0.0));
        familyEfficiencies.add(new FamilyEfficiency("B747",0.0,0.0,0.0));
        familyEfficiencies.add(new FamilyEfficiency("B777",0.0,0.0,0.0));
        familyEfficiencies.add(new FamilyEfficiency("B773",0.0,0.0,0.0));
        familyEfficiencies.add(new FamilyEfficiency("SSJ-100",0.0,0.0,0.0));
        familyEfficiencies.add(new FamilyEfficiency("SJ-100",0.0,0.0,0.0));
        ObjectMapper objectMapper = new ObjectMapper();
        this.familyefficiency = objectMapper.writeValueAsString(familyEfficiencies);
        List<PilotStats> pilotStats = new ArrayList<>();
        this.pilots = objectMapper.writeValueAsString(pilotStats);
        this.totalhours = 0.0;
        this.countpilot = 0;
    }

    public void MainPilotGroupDtFromMainPilot(MainPilot mainPilot) throws JsonProcessingException {
        this.totalfuel = mainPilot.getTotalfuel();
        this.sumefficiency = mainPilot.getSumefficiency();
        this.countreis = mainPilot.getCountreis();
        ObjectMapper objectMapper = new ObjectMapper();
        this.familyefficiency = objectMapper.writeValueAsString(mainPilot.getFamilyefficiencies());
        this.pilots = objectMapper.writeValueAsString(mainPilot.getPilots());
        this.totalhours = mainPilot.getTotalhours();
        this.countpilot = mainPilot.getCountpilot();
    }

}
