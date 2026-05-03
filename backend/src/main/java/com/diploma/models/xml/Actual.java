package com.diploma.models.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import com.diploma.util.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Actual {
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime ActualTT;
    public Integer ActualFuelRem;
    public String note;
}
