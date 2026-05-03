package com.diploma.models.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import com.diploma.models.characteristics.FuelCheck;
import com.diploma.models.characteristics.Internals;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NavLogItem {
    @JacksonXmlProperty(localName = "Common")
    public Common common;

    @JacksonXmlProperty(localName = "Estimated")
    public Estimated estimated;

    @JacksonXmlProperty(localName = "Actual")
    public Actual actual;

    @JacksonXmlProperty(localName = "Internals")
    public Internals internals;

    @JacksonXmlProperty(localName = "FuelCheck")
    public FuelCheck fuelCheck;
}
