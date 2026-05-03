package com.diploma.entities;

import jakarta.persistence.*;
import lombok.*;

import javax.annotation.concurrent.Immutable;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Immutable
@Entity
@Table(name = "flightxml_allparams", schema = "dbo")
@AllArgsConstructor
@NoArgsConstructor
public class FlightXMLAllParams {

    private Long idflightxml;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idmrshr;

    private Long idflight;

    private Long idpassport;

    private Long idmeteo;

    @Column(nullable = false)
    private LocalDateTime dk;

    @Column(name = "flight", columnDefinition = "XML")
    private String flightXml;

    private Integer idreis;

    @Column(name = "isstraightening")
    private Boolean IsStraightening;

    @Column(name = "isstraighteningrov")
    private Boolean IsStraighteningROV;

    @Column(name="dtvu_s")
    private LocalDateTime DTVUS;
    //тип самолёта
    private String ts;

    //номер рейса
    private String nr;

    //планируемое время начала рейса

    private String route;

    @Column(name="dtvo")
    private LocalDateTime DTVO;

    //планируемое время окончания рейса
    @Column(name="dtvp")
    private LocalDateTime DTVP;

    //фактическое время начала рейса
    @Column(name="dtaodt")
    private LocalDateTime DTAOBT;

    //фактическое время окончания рейса
    @Column(name="dtata")
    private LocalDateTime DTATA;

    //взлёт (необходимо для вычисления руления)
    @Column(name="dtatot")
    private LocalDateTime DTATOT;

    //посадка (необходимо для вычисления руления)
    @Column(name="dtatl")
    private LocalDateTime DTATL;


    @Column(name = "taxi_out")
    private Integer taxi_out;

    @Column(name = "taxi_in")
    private Integer taxi_in;

    private Integer tabNo;

    @Column()
    private int vzr;

    @Column()
    private int rm;

    @Column()
    private int rb;

    @Column()
    private int gruz;

    @Column()
    private int pochta;

    @Column()
    private Integer bag;

    @Column
    private double fuel_eff;
    @Column(name = "meteo_data", columnDefinition = "text")
    private String meteo;

    private Integer kover_time;

    private Double fuel_price_rub;
}



