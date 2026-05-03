package com.diploma.models.flights;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import com.diploma.models.characteristics.MassesAndFuel;
import com.diploma.models.xml.Crew_lst;
import com.diploma.models.xml.FullNavLog;
import com.diploma.models.xml.Reports;
import com.diploma.util.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {
    @JacksonXmlProperty(localName = "plan_id")
    public Integer plan_id;
    @JacksonXmlProperty(localName = "id_mrshr")
    public Integer id_mrshr;
    @JacksonXmlProperty(localName = "plan_name")
    public String plan_name;
    @JacksonXmlProperty(localName = "nr")
    public String nr;
    @JacksonXmlProperty(localName = "plan_start_utc")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime plan_start_utc;
    @JacksonXmlProperty(localName = "plan_end_utc")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime plan_end_utc;
    @JacksonXmlProperty(localName = "ap1")
    public String ap1;
    @JacksonXmlProperty(localName = "iata1")
    public String iata1;
    @JacksonXmlProperty(localName = "icao1")
    public String icao1;
    @JacksonXmlProperty(localName = "ricao1")
    public String ricao1;
    @JacksonXmlProperty(localName = "apnm1")
    public String apnm1;
    @JacksonXmlProperty(localName = "Latitude1")
    public String lat1;
    @JacksonXmlProperty(localName = "Longitude1")
    public String lon1;
    @JacksonXmlProperty(localName = "ap2")
    public String ap2;
    @JacksonXmlProperty(localName = "iata2")
    public String iata2;
    @JacksonXmlProperty(localName = "icao2")
    public String icao2;
    @JacksonXmlProperty(localName = "ricao2")
    public String ricao2;
    @JacksonXmlProperty(localName = "apnm2")
    public String apnm2;
    @JacksonXmlProperty(localName = "Latitude2")
    public String lat2;
    @JacksonXmlProperty(localName = "Longitude2")
    public String lon2;
    @JacksonXmlProperty(localName = "type_mod")
    public String type_mod;
    @JacksonXmlProperty(localName = "crew_lst")
    public Crew_lst crew_lst;
    @JacksonXmlProperty(localName = "MassesAndFuel")
    public MassesAndFuel massesAndFuel;
    @JacksonXmlProperty(localName = "FullNavLog")
    public FullNavLog fullNavLog;
    @JacksonXmlProperty(localName = "Reports")
    public Reports reports;
}