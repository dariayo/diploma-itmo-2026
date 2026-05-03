package com.diploma.models.characteristics;

import lombok.Builder;
import lombok.Data;
import com.diploma.entities.FlightXMLAllParams;

import java.time.LocalDateTime;

@Data
@Builder
public class TimeCharacteristics {
    //планируемое время начала рейса
    private LocalDateTime DTVO;
    //планируемое время окончания рейса
    private LocalDateTime DTVP;
    //фактическое время начала рейса
    private LocalDateTime DTAOBT;
    //фактическое время окончания рейса
    private LocalDateTime DTATA;
    //взлёт (необходимо для вычисления руления)
    private LocalDateTime DTATOT;
    //посадка (необходимо для вычисления руления)
    private LocalDateTime DTATL;
    //согласованное время вылета (если отсутствует то == план)
    private LocalDateTime DTAD;
    //согласованное время прилета (если отсутствует то == план)
    private LocalDateTime DTAA;

    @Override
    public String toString() {
        return "TimeCharacteristics{" +
                "DTVO=" + DTVO +
                ", DTVP=" + DTVP +
                ", DTAOBT=" + DTAOBT +
                ", DTATA=" + DTATA +
                ", DTAD=" + DTAD +
                ", DTAA=" + DTAA +
                '}';
    }

    public static TimeCharacteristics buildByFlightXMLAllParams(FlightXMLAllParams flightXMLAllParams){
        return TimeCharacteristics.builder()
                .DTAOBT(flightXMLAllParams.getDTAOBT()).DTATOT(flightXMLAllParams.getDTATOT()).DTATL(flightXMLAllParams.getDTATL())
                .DTATA(flightXMLAllParams.getDTATA())
                .DTVO(flightXMLAllParams.getDTVO()).DTVP(flightXMLAllParams.getDTVP()).build();
    }
}
