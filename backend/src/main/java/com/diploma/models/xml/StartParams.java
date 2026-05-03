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
public class StartParams {
    @JacksonXmlProperty(localName = "OFPTotalFuel")
    public Integer OFPTotalFuel;
    @JacksonXmlProperty(localName = "OFPTaxiFuel")
    public Integer OFPTaxiFuel;
    @JacksonXmlProperty(localName = "ActualTotalFuel")
    public Integer ActualTotalFuel;
    @JacksonXmlProperty(localName = "ActualTaxiFuel")
    public Integer ActualTaxiFuel;
    @JacksonXmlProperty(localName = "MassesConfirmed")
    public Integer MassesConfirmed;
    @JacksonXmlProperty(localName = "DepManoeuvreFuelCalculated")
    public Integer DepManoeuvreFuelCalculated;
    @JacksonXmlProperty(localName = "EngineStart")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime EngineStart;
    @JacksonXmlProperty(localName = "ATOT")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime ATOT;
    @JacksonXmlProperty(localName = "DepartureManoeuvreTime")
    public Integer DepartureManoeuvreTime;
    @JacksonXmlProperty(localName = "OFPTripTime")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime OFPTripTime;
}
