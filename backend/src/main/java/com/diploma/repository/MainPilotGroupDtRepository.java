package com.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.diploma.entities.MainPilotGroupDt;

import java.time.LocalDateTime;
import java.util.List;

public interface MainPilotGroupDtRepository extends JpaRepository<MainPilotGroupDt, LocalDateTime> {
    @Query(
            value = "SELECT tr FROM MainPilotGroupDt tr WHERE tr.dt BETWEEN :start and :end"
    )
    List<MainPilotGroupDt> findByDtBetween(LocalDateTime start, LocalDateTime end);
    List<MainPilotGroupDt> findAll();
    long count();

}
