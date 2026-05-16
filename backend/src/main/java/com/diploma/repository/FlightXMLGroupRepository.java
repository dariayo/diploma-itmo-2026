package com.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.diploma.entities.FlightXMLGroup;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightXMLGroupRepository extends JpaRepository<FlightXMLGroup, Long> {

    List<FlightXMLGroup> findByDateBetweenAndTabNo(LocalDateTime startDt, LocalDateTime endDt, @Param("tab_no") Integer tab_no);
    @Query("SELECT distinct f.tabNo FROM FlightXMLGroup f where f.date >= :startDt and f.date <= :endDt and (:routes is null or f.route IN :routes)")
    List<Integer> findTabNosByDtAndRoutes(@Param("routes") List<String> routes, @Param("startDt") LocalDateTime startDt, @Param("endDt") LocalDateTime endDt);

    @Query("SELECT distinct f.route FROM FlightXMLGroup f WHERE f.route IS NOT NULL ORDER BY f.route")
    List<String> findDistinctRoutes();

    long count();
}
