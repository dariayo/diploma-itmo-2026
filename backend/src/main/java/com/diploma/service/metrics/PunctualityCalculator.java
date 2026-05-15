package com.diploma.service.metrics;

import org.springframework.stereotype.Component;
import com.diploma.dto.CalculateData;
import com.diploma.repository.MrshrRepository;
import com.diploma.models.characteristics.TimeCharacteristics;

import java.time.Duration;
import java.util.Map;

@Component
public class PunctualityCalculator implements Calculator {
    private final MrshrRepository mrshrRepository;

    public PunctualityCalculator(MrshrRepository mr) {
        this.mrshrRepository = mr;
    }

    @Override
    public Map<String, Double> calculate(CalculateData data) {
        Long id = data.getId_mrshr();

        TimeCharacteristics timeData = data.getTd();

        if (timeData.getDTAD() == null ||
                timeData.getDTAA() == null ||
                timeData.getDTAOBT() == null ||
                timeData.getDTATA() == null)
            return Map.of("punctuality", 0.0);

        double delayFee = 0;
        if (timeData.getDTAOBT().isAfter(timeData.getDTAD())) {
            String reason = mrshrRepository.findDelayTakeById(id);
            if (pilotFault(reason)) {
                long minutes = Duration.between(timeData.getDTAD(), timeData.getDTAOBT()).toMinutes();
                double fee = feeCalcTake(minutes);
                System.out.println("пунктуальность до: "+minutes+fee);
                delayFee += fee;
            }
        }
        if (timeData.getDTATA().isAfter(timeData.getDTAA())) {
            String reason = mrshrRepository.findDelayArrById(id);
            if (pilotFault(reason)) {
                long minutes = Duration.between(timeData.getDTAA(), timeData.getDTATA()).toMinutes();
                double fee = feeCalcArr(minutes);
                System.out.println("пунктуальность после: "+minutes+fee);
                delayFee += fee;
            }
        }
        return Map.of("punctuality", delayFee / 2);
    }

    private boolean pilotFault(String reason) {
        return true;
    }

    private double feeCalcTake(long minutes) {
        if (minutes <= 1) {
            return 1.0;
        } else if (minutes <= 30) {
            return 0.75;
        } else if (minutes <= 60) {
            return 0.50;
        } else if (minutes <= 90) {
            return 0.25;
        } else {
            return 0.0;
        }
    }

    private double feeCalcArr(long minutes) {
        if (minutes <= 1) {
            return 1.0;
        } else if (minutes <= 15) {
            return 0.75;
        } else if (minutes <= 30) {
            return 0.5;
        } else if (minutes <= 45) {
            return 0.25;
        } else {
            return 0;
        }
    }
}
