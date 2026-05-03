package com.diploma.util;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.Math.*;

@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD")
public class SolarisUtils {
    /**
     * Метод расчета всех моментов естественного освещения на указанную дату. Обычно не нужен напрямую.
     * @param baseDate дата (UTC) для которой должен быть произведен расчет. ВременнАя часть отсекается.
     * @param lat - широта точки, для которой должен быть произведен расчет
     * @param lon - долгота точки, для которой должен быть произведен расчет.
     * @return моменты естественного освещения (рассвет-восход-закат-темнота) для указанных координат в указанных сутках.
     */
    @Contract("_, _, _ -> new")
    public @NotNull SolarisMoments calculate(Instant baseDate, double lat, double lon) {

        lat = lat / 57.29578;
        lon = lon / 57.29578;

        LocalDate dt = LocalDate.ofInstant(baseDate, ZoneOffset.UTC);

        int yy = dt.getYear();
        int mm = dt.getMonthValue();
        int dd = dt.getDayOfMonth();

        int ndel = yy - 1980;
        int kvis = ndel / 4; // целочисленное деление
        int nday = ndel * 365 + kvis;
        int kcel = kvis * 4;

        if (kcel == ndel) {
            if (mm > 2)
                nday++;
        } else
            nday++;

        switch (mm) {
            case 2 -> nday = nday + 31;
            case 3 -> nday = nday + 59;
            case 4 -> nday = nday + 90;
            case 5 -> nday = nday + 120;
            case 6 -> nday = nday + 151;
            case 7 -> nday = nday + 181;
            case 8 -> nday = nday + 212;
            case 9 -> nday = nday + 242;
            case 10 -> nday = nday + 273;
            case 11 -> nday = nday + 304;
            case 12 -> nday = nday + 334;
        }
        nday = nday + dd;

        double day = nday - (lon / 6.2831852) + 0.5;

        // Средняя аномалия
        double anom = 0.01720279 * day - 0.065674294;

        while ((anom - 6.2831852) >= 0)
            anom = anom - 6.2831852;

        double e = anom;
        boolean _ok = false;
        double del = 0;
        int i = 0;
        while (true) {
            if (_ok) {
                double de = del / (1 - 0.016718 * cos(e));
                e = e - de;
            }
            del = e - 0.016718 * sin(e) - anom;
            _ok = true;
            if (((Math.abs(del) - 1E-6) > 0) || (del < 9E-23))
                break;

            i++;
            if (i > 10000)
                break;
        }

        // Истиная аномалия
        double anomi = 2 * atan(1.01686 * tan(e / 2));
        double dsol = anomi + 4.9322375;

        // Склонение солнца, рад
        double skl = asin(0.39781868 * sin(dsol));

        double tv = 0;
        double tz = 0;
        double tkt = 0;
        double tnt = 0;
        double x = sin(lat) * sin(skl);
        double y = cos(lat) * cos(skl);

        //Косинусы числовых углов
        double ct1 = (-0.014834754 - x) / y;
        double ct2 = (-0.10452845 - x) / y;

        double dg = 0;
        double uv = 0;
        double ntv;
        double ntz;
        double ntkt;
        double ntnt;

        if ((ct1 + 1) >= 0) {
            if ((ct1 - 1) <= 0) {
                double t1g = acos(ct1) * 57.29578;
                dg = lon * 57.29578;
                // Время захода/восхода ист, час
                double tvich = 12 - (t1g + dg) / 15.0;
                double tzich = 12 + (t1g - dg) / 15.0;

                //Уравнение времени, час
                uv = 0.12833333 * sin(dsol + 4.5204026) + 0.165 * sin(2 * dsol);

                //Время восх/зах среднее, час
                double tvsch = tvich - uv;
                double tzsch = tzich - uv;

                if (tvsch < 0)
                    tvsch = tvsch + 24;
                if (tvsch > 24)
                    tvsch = tvsch - 24;
                if (tzsch < 0)
                    tzsch = tzsch + 24;
                if (tzsch > 24)
                    tzsch = tzsch - 24;

                ntv = Math.floor(tvsch);
                tv = (tvsch - ntv) * 60d;
                ntz = Math.floor(tzsch);
                tz = (tzsch - ntz) * 60;
            } else {
                ntv = 70;
                ntz = 90;
            }

            if ((ct2 + 1) >= 0) {
                if ((ct2 - 1) <= 0) {
                    double t2g = acos(ct2) * 57.29578;
                    // Время конца/нач темноты, час
                    double tktich = 12 - (t2g + dg) / 15.0;
                    double tntich = 12 + (t2g - dg) / 15.0;

                    double tktsch = tktich - uv;
                    double tntsch = tntich - uv;

                    if (tktsch < 0)
                        tktsch = tktsch + 24;
                    if (tktsch > 24)
                        tktsch = tktsch - 24;

                    if (tntsch < 0)
                        tntsch = tntsch + 24;
                    if (tntsch > 24)
                        tntsch = tntsch - 24;

                    ntkt = Math.floor(tktsch);
                    tkt = (tktsch - ntkt) * 60;

                    ntnt = Math.floor(tntsch);
                    tnt = (tntsch - ntnt) * 60;
                } else {
                    ntkt = 60;
                    ntnt = 60;
                }
            } else {
                ntkt = 60;
                ntnt = 60;
            }
        } else {
            ntz = 80;
            ntv = 90;
            ntkt = 90;
            ntnt = 90;
        }

        if (tv == 60)
            tv = 0;
        if (tz == 60)
            tz = 0;
        if (tkt == 60)
            tkt = 0;
        if (tnt == 60)
            tnt = 0;

        Instant dawn = baseDate.truncatedTo(ChronoUnit.DAYS)
                .plus(Math.round(ntkt * 60 * 60), ChronoUnit.SECONDS)
                .plus(Math.round(tkt * 60), ChronoUnit.SECONDS);
        Instant sunrise = baseDate.truncatedTo(ChronoUnit.DAYS)
                .plus(Math.round(ntv * 60 * 60), ChronoUnit.SECONDS)
                .plus(Math.round(tv * 60), ChronoUnit.SECONDS);
        Instant sunset = baseDate.truncatedTo(ChronoUnit.DAYS)
                .plus(Math.round(ntz * 60 * 60), ChronoUnit.SECONDS)
                .plus(Math.round(tz * 60), ChronoUnit.SECONDS);
        Instant dark = baseDate.truncatedTo(ChronoUnit.DAYS)
                .plus(Math.round(ntnt * 60 * 60), ChronoUnit.SECONDS)
                .plus(Math.round(tnt * 60), ChronoUnit.SECONDS);

        if (lon > 0) {
            if (dawn.compareTo(dark) != 0) {
                if (dawn.isAfter(dark))
                    dawn = dawn.minus(1, ChronoUnit.DAYS);
                if (sunrise.isAfter(dark))
                    sunrise = sunrise.minus(1, ChronoUnit.DAYS);
                if (sunset.isAfter(dark))
                    sunset = sunset.minus(1, ChronoUnit.DAYS);
            } else if (sunrise.isAfter(sunset) && (sunrise.compareTo(dawn) != 0))
                sunrise = sunrise.minus(1, ChronoUnit.DAYS);
        }

        int sun = 0;
        int light = 0;
        if (sunset.isAfter(dark))
            sun = -1;
        else if (sunrise.compareTo(dawn) == 0)
            sun = 1;

        if ((dawn.compareTo(dark) == 0) && Set.of(1, 2, 3, 10, 11, 12).contains(mm))
            light = -1;
        else if ((dawn.compareTo(dark) == 0) && Set.of(4, 5, 6, 7, 8, 9).contains(mm))
            light = 1;

        return new SolarisMoments(
                dawn, sunrise, sunset, dark, light, sun
        );
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
        Instant getDt(){
            if (isLightTimeChanged())
                return dtX;
            else
                return Instant.EPOCH;
        }

        boolean isLightTimeChanged() {
            return switch (lightType) {
                case DAWN, DARK -> light == 0;
                case SUNRISE, SUNSET -> sun == 0;
            };
        }
    }

}
