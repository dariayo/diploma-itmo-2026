package com.diploma.models.characteristics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class MassesAndFuel {
    @JacksonXmlProperty(localName = "fuel_rest")
    public Integer fuel_rest;
    @JacksonXmlProperty(localName = "TripFuel")
    public Integer TripFuel;
    @JacksonXmlProperty(localName = "OFPTotalFuel")
    public Integer OFPTotalFuel;
    @JacksonXmlProperty(localName = "OFPRequiredFuel")
    public Integer OFPRequiredFuel;
    @JacksonXmlProperty(localName = "OFPMinRequiredFuel")
    public Integer OFPMinRequiredFuel;
    @JacksonXmlProperty(localName = "OFPCNTGFuel")
    public Integer OFPCNTGFuel;
    @JacksonXmlProperty(localName = "OFPTaxiFuel")
    public Integer OFPTaxiFuel;
    @JacksonXmlProperty(localName = "FuelAdjust")
    public Integer FuelAdjust;
    @JacksonXmlProperty(localName = "FuelRemAfterLand")
    public Integer FuelRemAfterLand;
    @JacksonXmlProperty(localName = "FuelRemAfterFlight")
    public Integer FuelRemAfterFlight;
    @JacksonXmlProperty(localName = "FinalFuel")
    public Integer FinalFuel;
    @JacksonXmlProperty(localName = "cr_EPLD")
    public Integer MassOfp;
    @JacksonXmlProperty(localName = "ActualPLD")
    public Integer MassFact;
}
