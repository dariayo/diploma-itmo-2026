package com.diploma.models.xml;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import com.diploma.util.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

@Getter
@Setter
public class Estimated {
    public Integer dist;
    public Integer dtgo;
    public Integer OFPet;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime OFPtt;
    public Integer OFPFuel;
    public Integer OFPFuelRemain;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime EstimatedTT;
    public Integer EstimatedFuelRem;
}
