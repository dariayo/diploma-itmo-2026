package com.diploma.service.metrics;

import com.diploma.dto.CalculateData;

import java.util.Map;

public interface Calculator {
    Map<String, Double> calculate(CalculateData data);
}
