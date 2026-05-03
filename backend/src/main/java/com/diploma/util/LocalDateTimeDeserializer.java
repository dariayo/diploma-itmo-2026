package com.diploma.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter[] formatters = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String date = p.getText();

        if ("1899-12-30".equals(date)) {
            return null;
        }

        for (DateTimeFormatter formatter : formatters) {
            try {
                TemporalAccessor ta = formatter.parse(date);

                LocalDate ld = LocalDate.from(ta);

                LocalTime lt = ta.query(TemporalQueries.localTime());
                if (lt == null) {
                    return ld.atStartOfDay();
                } else {
                    return LocalDateTime.of(ld, lt);
                }

            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IOException("Unable to parse date: " + date);
    }

}
