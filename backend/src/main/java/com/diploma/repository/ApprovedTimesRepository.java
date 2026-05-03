package com.diploma.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class ApprovedTimesRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public LocalDateTime[] getApprovedTimes(Long id) {
        return jdbcTemplate.queryForObject(
                """
                    SELECT dtapproveddep, dtapprovedarr
                    FROM dbo.approved_times
                    WHERE id_mrshr = ?
                """,
                (rs, rowNum) -> new LocalDateTime[] {
                        rs.getTimestamp("dtapproveddep").toLocalDateTime(),
                        rs.getTimestamp("dtapprovedarr").toLocalDateTime()
                },
                id);
    }
}
