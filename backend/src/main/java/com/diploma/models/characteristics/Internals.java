package com.diploma.models.characteristics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import com.diploma.util.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Internals {
    public String ItemType;
    public String IsOFP;
    public String ID;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime CreationTime;
    public String ArrivalType;
    public String IsInactive;
    public String IsChecked;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime CheckedTime;
}
