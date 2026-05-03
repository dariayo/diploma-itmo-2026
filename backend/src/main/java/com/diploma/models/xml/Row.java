package com.diploma.models.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Row {
    public Integer tab_no;
    public String crew_code;
    public String fam;
    public String name;
    public String otch;
    public Integer alg_id;
    public String meteo;
    public String phones;
}
