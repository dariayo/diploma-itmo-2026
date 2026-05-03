package com.diploma.parser;

import java.util.*;
import java.util.regex.*;

public class TAFParser {
    private static final Pattern WIND_PATTERN = Pattern.compile("\\b(VRB|\\d{3})(\\d{2,3})(G(\\d{2,3}))?(KT|MPS)\\b");

    public static Map<String, Object> parse(String taf) {
        Map<String, Object> result = new HashMap<>();

        String[] lines = taf.split("\\R");
        String line0 = lines[0].trim();
        String[] parts = line0.split("\\s+");
        String visibility = null;

        for (String p : parts) {
            if (p.startsWith("CAVOK")) {
                visibility = "CAVOK";
                break;
            }

            if (p.length() == 4 && p.chars().allMatch(Character::isDigit)) {
                visibility = p;
                break;
            }
        }

        if (visibility != null) {
            if ("CAVOK".equalsIgnoreCase(visibility)) {
                result.put("visibility", 9999);
            } else {
                result.put("visibility", Integer.parseInt(visibility));
            }
        }

        Matcher mWind = WIND_PATTERN.matcher(lines[0]);
        if (mWind.find()) {
            Map<String,Object> wind = new HashMap<>();
            wind.put("dir", mWind.group(1));
            wind.put("speed", Integer.parseInt(mWind.group(2)));
            wind.put("gust", mWind.group(4) != null ? Integer.parseInt(mWind.group(4)) : null);
            wind.put("unit", mWind.group(5));
            result.put("wind", wind);
        }
        return result;
    }

}
