package com.diploma.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.diploma.models.aircrafts;

import java.util.List;

@Repository
public class DistanceTypeModeRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<aircrafts> aircrafts;

    public String getFamily(String type) {
        if (type.startsWith("A3")) return "A320";
        if (type.startsWith("B73")) return "B738";
        if (type.startsWith("B74")) return "B744";
        if (type.startsWith("B777")) return "B777";
        if (type.startsWith("S")) return "SSJ-100";
        if (type.startsWith("B773")) return "B773";
        return "";
    }

    public int distance(String type) {
        return findDistanceByTypeMod(getFamily(type));
    }

    public int findDistanceByTypeMod(String type) {
        if(aircrafts==null){
            aircrafts = jdbcTemplate.query(
                    "SELECT max_range_nm, taxi_fuel_kg_per_min, type FROM dbo.aircrafts",
                    new BeanPropertyRowMapper<>(aircrafts.class)
            );
        }
        for(aircrafts aircraft: aircrafts){
            if(aircraft.getType().equals(type)){
                return aircraft.getMax_range_nm();
            }
        }
        return 0;
    }
}
