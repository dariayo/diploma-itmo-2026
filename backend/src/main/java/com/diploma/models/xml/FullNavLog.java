package com.diploma.models.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FullNavLog {
    @JacksonXmlProperty(localName = "SourceNavLog")
    public SourceNavLog SourceNavLog;
    
    @JacksonXmlProperty(localName = "Common")
    public FullNavLogCommon fullNavLogCommon;

    @JacksonXmlProperty(localName = "ActualNavLog")
    public ActualNavLog ActualNavLog;

    @JacksonXmlProperty(localName = "StartParams")
    public StartParams startParams;

    @JacksonXmlProperty(localName = "EndParams")
    public EndParams endParams;
}

