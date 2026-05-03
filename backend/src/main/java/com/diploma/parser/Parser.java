package com.diploma.parser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.diploma.models.flights.Flight;

import java.io.IOException;

@Component
@NoArgsConstructor
public class Parser {
    private final XmlMapper xmlMapper = new XmlMapper();
    private String xmlDirectory;

    public Parser(@Value("${flight.xml.directory}") String xmlDirectory) {
        this.xmlDirectory = xmlDirectory;
    }


    public Flight parseFlightXML(String flightXML) throws IOException {
        return xmlMapper.readValue(flightXML, Flight.class);

    }
}


