package com.diploma.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.diploma.dto.DateRange;

import java.time.LocalDate;

@Configuration
public class AppConfig {
    @Value("${flight.xml.directory}")
    private String xmlDirectory;
    @Value("${flight.year}")
    private Integer year;
    @Value("${flight.month}")
    private Integer month;

    @Bean
    public String xmlDirectory() {
        return xmlDirectory;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public DateRange BASE_DATE_RANGE;

    @PostConstruct
    public void init() {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusDays(30);
        BASE_DATE_RANGE = new DateRange(start, end);
    }
}
