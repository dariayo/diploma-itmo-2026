package com.diploma.models.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import com.diploma.util.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class EndParams {
    @JacksonXmlProperty(localName = "RemFuelAfterLand")
    public Integer RemFuelAfterLand;
    @JacksonXmlProperty(localName = "RemFuelAfterFlight")
    public Integer RemFuelAfterFlight;
    @JacksonXmlProperty(localName = "DestinationManoeuvreFuel")
    public Integer DestinationManoeuvreFuel;
    @JacksonXmlProperty(localName = "ATL")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime ATL;
    @JacksonXmlProperty(localName = "EngineOff")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime EngineOff;
    @JacksonXmlProperty(localName = "OFPdtvp")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime OFPdtvp;
    @JacksonXmlProperty(localName = "DestinationManoeuvreTime")
    public Integer DestinationManoeuvreTime;
    @JacksonXmlProperty(localName = "RW")
    public String RW;
    @JacksonXmlProperty(localName = "LandingCondition")
    public Integer LandingCondition;
    @JacksonXmlProperty(localName = "ArrivalType")
    public Integer ArrivalType;
}
