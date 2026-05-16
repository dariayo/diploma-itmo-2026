package com.diploma.repository;

import com.diploma.entities.AircraftDirectory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AircraftDirectoryRepository extends JpaRepository<AircraftDirectory, String> {
}
