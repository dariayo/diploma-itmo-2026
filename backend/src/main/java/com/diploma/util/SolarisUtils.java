package com.diploma.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static java.lang.Math.*;

@Component
@SuppressWarnings("PMD")
public class SolarisUtils {
    private static final double DEGREES_IN_RADIAN = 57.29578;
    private static final double FULL_CIRCLE = 6.2831852;
    private static final double EARTH_ORBIT_STEP = 0.01720279;
    private static final double MEAN_ANOMALY_SHIFT = 0.065674294;
    private static final double ORBIT_ECCENTRICITY = 0.016718;
    private static final double SOLAR_LONGITUDE_SHIFT = 4.9322375;
    private static final double EARTH_AXIS_SIN = 0.39781868;
    private static final double SUNRISE_ZENITH_COS = -0.014834754;
    private static final double TWILIGHT_ZENITH_COS = -0.10452845;
    private static final double HOURS_PER_DAY = 24.0;
    private static final double DEGREES_PER_HOUR = 15.0;
    private static final int YEAR_BASE = 1980;

    private static final Set<Integer> COLD_SEASON_MONTHS = Set.of(1, 2, 3, 10, 11, 12);
    private static final Set<Integer> WARM_SEASON_MONTHS = Set.of(4, 5, 6, 7, 8, 9);

    /**
     * Рассчитывает ключевые моменты естественного освещения для заданной даты и координат.
     *
     * @param baseDate дата в UTC, временная часть используется только для определения суток
     * @param latitude широта точки
     * @param longitude долгота точки
     * @return моменты рассвета, восхода, заката и наступления темноты
     */
    @Contract("_, _, _ -> new")
    public @NotNull SolarisMoments calculate(Instant baseDate, double latitude, double longitude) {
        LocalDate date = LocalDate.ofInstant(baseDate, ZoneOffset.UTC);
        double latitudeRad = toRadiansLegacy(latitude);
        double longitudeRad = toRadiansLegacy(longitude);

        int dayNumber = calculateDayNumber(date);
        double solarDay = dayNumber - longitudeRad / FULL_CIRCLE + 0.5;
        SolarPosition position = calculateSolarPosition(solarDay);

        LightCalculation calculation = calculateLightMoments(latitudeRad, longitudeRad, position);
        SolarisMoments moments = toInstants(baseDate, calculation);
        return normalizeForLongitude(moments, longitudeRad, date.getMonthValue());
    }

    private int calculateDayNumber(LocalDate date) {
        int yearsFromBase = date.getYear() - YEAR_BASE;
        int leapCycles = yearsFromBase / 4;
        int dayNumber = yearsFromBase * 365 + leapCycles;

        if (leapCycles * 4 == yearsFromBase) {
            if (date.getMonthValue() > 2) {
                dayNumber++;
            }
        } else {
            dayNumber++;
        }

        return dayNumber + daysBeforeMonth(date.getMonthValue()) + date.getDayOfMonth();
    }

    private int daysBeforeMonth(int month) {
        return switch (month) {
            case 2 -> 31;
            case 3 -> 59;
            case 4 -> 90;
            case 5 -> 120;
            case 6 -> 151;
            case 7 -> 181;
            case 8 -> 212;
            case 9 -> 242;
            case 10 -> 273;
            case 11 -> 304;
            case 12 -> 334;
            default -> 0;
        };
    }

    private SolarPosition calculateSolarPosition(double solarDay) {
        double meanAnomaly = normalizeAngle(EARTH_ORBIT_STEP * solarDay - MEAN_ANOMALY_SHIFT);
        double eccentricAnomaly = solveEccentricAnomaly(meanAnomaly);
        double trueAnomaly = 2 * atan(1.01686 * tan(eccentricAnomaly / 2));
        double solarLongitude = trueAnomaly + SOLAR_LONGITUDE_SHIFT;
        double declination = asin(EARTH_AXIS_SIN * sin(solarLongitude));
        double equationOfTime = 0.12833333 * sin(solarLongitude + 4.5204026)
                + 0.165 * sin(2 * solarLongitude);

        return new SolarPosition(declination, equationOfTime);
    }

    private double solveEccentricAnomaly(double meanAnomaly) {
        double eccentricAnomaly = meanAnomaly;
        double delta = 0.0;
        boolean hasPreviousDelta = false;

        for (int iteration = 0; iteration <= 10_000; iteration++) {
            if (hasPreviousDelta) {
                double correction = delta / (1 - ORBIT_ECCENTRICITY * cos(eccentricAnomaly));
                eccentricAnomaly -= correction;
            }

            delta = eccentricAnomaly - ORBIT_ECCENTRICITY * sin(eccentricAnomaly) - meanAnomaly;
            hasPreviousDelta = true;

            if (abs(delta) > 1E-6 || delta < 9E-23) {
                break;
            }
        }

        return eccentricAnomaly;
    }

    private LightCalculation calculateLightMoments(
            double latitudeRad,
            double longitudeRad,
            SolarPosition position
    ) {
        double sunProjection = sin(latitudeRad) * sin(position.declination());
        double horizonProjection = cos(latitudeRad) * cos(position.declination());
        double sunriseCos = (SUNRISE_ZENITH_COS - sunProjection) / horizonProjection;
        double twilightCos = (TWILIGHT_ZENITH_COS - sunProjection) / horizonProjection;

        if (sunriseCos < -1) {
            return new LightCalculation(
                    MinuteOfDay.special(90),
                    MinuteOfDay.special(90),
                    MinuteOfDay.special(80),
                    MinuteOfDay.special(90)
            );
        }

        if (sunriseCos > 1) {
            DayPeriod twilightPeriod = inCosineRange(twilightCos)
                    ? calculatePeriod(twilightCos, 0, 0)
                    : DayPeriod.special(60);

            return new LightCalculation(
                    twilightPeriod.start(),
                    MinuteOfDay.special(70),
                    MinuteOfDay.special(90),
                    twilightPeriod.end()
            );
        }

        double longitudeDeg = longitudeRad * DEGREES_IN_RADIAN;
        DayPeriod sunPeriod = calculatePeriod(sunriseCos, longitudeDeg, position.equationOfTime());
        DayPeriod twilightPeriod = inCosineRange(twilightCos)
                ? calculatePeriod(twilightCos, longitudeDeg, position.equationOfTime())
                : DayPeriod.special(60);

        return new LightCalculation(
                twilightPeriod.start(),
                sunPeriod.start(),
                sunPeriod.end(),
                twilightPeriod.end()
        );
    }

    private DayPeriod calculatePeriod(double angleCos, double longitudeDeg, double equationOfTime) {
        double angleDeg = acos(angleCos) * DEGREES_IN_RADIAN;

        double startTrueTime = 12 - (angleDeg + longitudeDeg) / DEGREES_PER_HOUR;
        double endTrueTime = 12 + (angleDeg - longitudeDeg) / DEGREES_PER_HOUR;

        return new DayPeriod(
                MinuteOfDay.fromHours(startTrueTime - equationOfTime),
                MinuteOfDay.fromHours(endTrueTime - equationOfTime)
        );
    }

    private SolarisMoments toInstants(Instant baseDate, LightCalculation calculation) {
        Instant dayStart = baseDate.truncatedTo(ChronoUnit.DAYS);

        return new SolarisMoments(
                calculation.dawn().toInstant(dayStart),
                calculation.sunrise().toInstant(dayStart),
                calculation.sunset().toInstant(dayStart),
                calculation.dark().toInstant(dayStart),
                0,
                0
        );
    }

    private SolarisMoments normalizeForLongitude(SolarisMoments moments, double longitudeRad, int month) {
        Instant dawn = moments.dawn();
        Instant sunrise = moments.sunrise();
        Instant sunset = moments.sunset();
        Instant dark = moments.dark();

        if (longitudeRad > 0) {
            if (!dawn.equals(dark)) {
                if (dawn.isAfter(dark)) {
                    dawn = dawn.minus(1, ChronoUnit.DAYS);
                }
                if (sunrise.isAfter(dark)) {
                    sunrise = sunrise.minus(1, ChronoUnit.DAYS);
                }
                if (sunset.isAfter(dark)) {
                    sunset = sunset.minus(1, ChronoUnit.DAYS);
                }
            } else if (sunrise.isAfter(sunset) && !sunrise.equals(dawn)) {
                sunrise = sunrise.minus(1, ChronoUnit.DAYS);
            }
        }

        int sun = defineSunState(dawn, sunrise, sunset, dark);
        int light = defineLightState(dawn, dark, month);

        return new SolarisMoments(dawn, sunrise, sunset, dark, light, sun);
    }

    private int defineSunState(Instant dawn, Instant sunrise, Instant sunset, Instant dark) {
        if (sunset.isAfter(dark)) {
            return -1;
        }
        if (sunrise.equals(dawn)) {
            return 1;
        }
        return 0;
    }

    private int defineLightState(Instant dawn, Instant dark, int month) {
        if (!dawn.equals(dark)) {
            return 0;
        }
        if (COLD_SEASON_MONTHS.contains(month)) {
            return -1;
        }
        if (WARM_SEASON_MONTHS.contains(month)) {
            return 1;
        }
        return 0;
    }

    private boolean inCosineRange(double value) {
        return value >= -1 && value <= 1;
    }

    private double normalizeAngle(double angle) {
        double normalized = angle;
        while (normalized >= FULL_CIRCLE) {
            normalized -= FULL_CIRCLE;
        }
        return normalized;
    }

    private double toRadiansLegacy(double degrees) {
        return degrees / DEGREES_IN_RADIAN;
    }

    private record SolarPosition(double declination, double equationOfTime) {
    }

    private record LightCalculation(
            MinuteOfDay dawn,
            MinuteOfDay sunrise,
            MinuteOfDay sunset,
            MinuteOfDay dark
    ) {
    }

    private record DayPeriod(MinuteOfDay start, MinuteOfDay end) {
        private static DayPeriod special(int hour) {
            return new DayPeriod(MinuteOfDay.special(hour), MinuteOfDay.special(hour));
        }
    }

    private record MinuteOfDay(double hour, double minute) {
        private static MinuteOfDay fromHours(double hours) {
            double normalizedHours = normalizeHours(hours);
            double wholeHours = floor(normalizedHours);
            double minutes = (normalizedHours - wholeHours) * 60.0;
            return new MinuteOfDay(wholeHours, normalizeMinute(minutes));
        }

        private static MinuteOfDay special(double hour) {
            return new MinuteOfDay(hour, 0);
        }

        private static MinuteOfDay zeroAt(double hour) {
            return new MinuteOfDay(hour, 0);
        }

        private Instant toInstant(Instant dayStart) {
            return dayStart
                    .plus(Math.round(hour * 60 * 60), ChronoUnit.SECONDS)
                    .plus(Math.round(minute * 60), ChronoUnit.SECONDS);
        }

        private static double normalizeHours(double hours) {
            if (hours < 0) {
                return hours + HOURS_PER_DAY;
            }
            if (hours > HOURS_PER_DAY) {
                return hours - HOURS_PER_DAY;
            }
            return hours;
        }

        private static double normalizeMinute(double minute) {
            return minute == 60 ? 0 : minute;
        }
    }

    public enum LightTimeType {
        DAWN, SUNRISE, SUNSET, DARK
    }

    public record SolarisMoments(
            Instant dawn,
            Instant sunrise,
            Instant sunset,
            Instant dark,
            int light,
            int sun
    ) {
    }

    public record TypedLightTime(
            Instant dtX,
            LightTimeType lightType,
            boolean isATOT,
            boolean isATL,
            int light,
            int sun
    ) {
        Instant getDt() {
            return isLightTimeChanged() ? dtX : Instant.EPOCH;
        }

        boolean isLightTimeChanged() {
            return switch (lightType) {
                case DAWN, DARK -> light == 0;
                case SUNRISE, SUNSET -> sun == 0;
            };
        }
    }
}
