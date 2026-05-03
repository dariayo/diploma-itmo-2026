package com.diploma.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MrshrRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String findDelayTakeById(Long id) {
        List<String> results = jdbcTemplate.query(
                "SELECT \"DelayCategTake\" FROM dbo.spp_mrshr WHERE id = ?",
                (rs, rowNum) -> rs.getString("DelayCategTake"),
                id
        );
        return results.isEmpty() ? null : results.get(0);
    }

    public String findDelayArrById(Long id) {
        List<String> results = jdbcTemplate.query(
                "SELECT \"DelayCategArr\" FROM dbo.spp_mrshr WHERE id = ?",
                (rs, rowNum) -> rs.getString("DelayCategArr"),
                id
        );
        return results.isEmpty() ? null : results.get(0);
    }

}
