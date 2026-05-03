package com.diploma.models.pilots;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PilotStats {
    private Integer tabNumber;
    private String captainName;
    private int flightCount;
    private double totalEfficiency;
    private double flightHours;
    private double averageEfficiency;
    private int delayedStart;
    private int delayedFinish;
    private Double difficultFlight;
    private Double averageDifficulty;
    private Double pilotRating ;
    private Integer count = 1;

    public PilotStats(Integer tabNumber, String captainName, int flightCount, double totalEfficiency, double flightHours) {
        this.tabNumber = tabNumber;
        this.captainName = captainName;
        this.flightCount = flightCount;
        this.totalEfficiency = totalEfficiency;
        this.flightHours = flightHours;
        this.averageEfficiency = flightCount > 0 ? totalEfficiency / flightCount : 0;
        this.difficultFlight = 0.0;
    }

    public void addFlight(double efficiency) {
        this.flightCount++;
        this.totalEfficiency += efficiency;
        this.averageEfficiency = totalEfficiency / this.flightCount;
    }

    public void updateData(PilotStats pilot) {
        this.totalEfficiency += pilot.totalEfficiency;
        this.flightCount += pilot.flightCount;
        this.flightHours += pilot.flightHours;
        this.averageEfficiency = totalEfficiency / this.flightCount;
        if (pilot.pilotRating != null) {
            if (this.pilotRating == null) {
                this.pilotRating = pilot.pilotRating;
            } else {
                this.pilotRating += pilot.pilotRating;
            }
        }
    }

    public void addFlightHours(double hours) {
        this.flightHours += hours;
    }
}
