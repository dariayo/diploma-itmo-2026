package com.diploma.service.logging;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ErrorCleanupService {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 1 * * MON") // Каждый понедельник в 01.00
    @Transactional
    public void cleanup() {
        int count = jdbcTemplate.update("DELETE FROM dbo.error_log");
        log.info("Deleted {} error logs", count);
    }
}
