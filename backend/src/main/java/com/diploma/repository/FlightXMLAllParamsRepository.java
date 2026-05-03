package com.diploma.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.diploma.entities.FlightXMLAllParams;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightXMLAllParamsRepository extends JpaRepository<FlightXMLAllParams, Integer> {

    @Transactional
    List<FlightXMLAllParams> findByDTVOBetweenAndTabNo(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Integer tabNo
    );

    @Query(
            value = "SELECT tr FROM FlightXMLAllParams tr WHERE tr.DTVO BETWEEN :start AND :end and tr.DTAOBT IS NOT NULL and tr.DTATA IS NOT NULL and tr.DTATL IS NOT NULL and tr.DTATOT IS NOT NULL"
    )
    List<FlightXMLAllParams> findByDTVOBetweenNotNull(LocalDateTime start, LocalDateTime end);

    long count();
}


