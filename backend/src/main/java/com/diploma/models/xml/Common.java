package com.diploma.models.xml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import com.diploma.util.CommonDeserializer;

@Getter
@Setter
@JsonDeserialize(using = CommonDeserializer.class)
public class Common {
    public String to;
    public String freq;
    public String fir;
    public String awy;
    public Integer stb;
    public Integer gmora;
    public String fl;
    @JsonProperty("windSpeed")
    public String windSpeed;
    @JsonProperty("windAngle")
    public String windAngle;
    public String tas;
    public String mac;
    public Integer gs;
    public String sr;
    public Integer imt;
    public String trp;
    @JsonProperty("temp")
    public Integer temp;
    public String pointType;
}

