package com.diploma.repository;

import com.diploma.entities.PilotWeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PilotWeatherSnapshotRepository extends JpaRepository<PilotWeatherSnapshot, Long> {
    List<PilotWeatherSnapshot> findBySourceFlightId(Long sourceFlightId);
}
