package com.diploma.service.flights;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import com.diploma.entities.FlightXMLAllParams;
import com.diploma.repository.FlightXMLAllParamsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightXMLAllParamsService {
    @Autowired
    private FlightXMLAllParamsRepository repository;

    public List<FlightXMLAllParams> getByRangeWithoutNulls(LocalDate dateFrom, LocalDate dateTo) {
        return repository.findByDTVOBetweenNotNull(dateFrom.atStartOfDay(), dateTo.atStartOfDay());
    }

    public List<FlightXMLAllParams> getByDateRangeAndTabNo(
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Integer tabNo) {
        return repository.findByDTVOBetweenAndTabNo(dateFrom, dateTo, tabNo);
    }
}
