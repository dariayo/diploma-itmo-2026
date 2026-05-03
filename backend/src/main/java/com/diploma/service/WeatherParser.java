package com.diploma.service;

import com.diploma.parser.TAFParser;

import java.util.*;
import java.util.regex.*;


public class WeatherParser {

    private static final Pattern BLOCK_PATTERN = Pattern.compile(
            "(TAF(?:\\s+(?:AMD|COR|CNL))*\\s+(\\w{4})\\b[\\s\\S]*?)(?=(?:\\r?\\n)*(?:METAR|TAF|SIGMET)|$)"
    );


    public static LinkedHashMap<String, String> parseAll(String text) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        Matcher matcher = BLOCK_PATTERN.matcher(text);

        while (matcher.find()) {
            String fullTafBlock = matcher.group(1).trim();
            String airport = matcher.group(2).trim();
            map.put(airport, fullTafBlock);
        }

        return map;
    }

    public static Map<String, Integer> calculateVisibilities(String fullText, String depICAO, String arrICAO) {
        LinkedHashMap<String, String> all = parseAll(fullText);

        String dep = all.get(depICAO);
        String arr = all.get(arrICAO);

        Integer takeoffVis = null;
        Integer landingVis = null;
        Integer worstEnRoute = null;

        if (dep != null) {
            takeoffVis = (Integer) TAFParser.parse(dep).get("visibility");
        }

        if (arr != null) {
            landingVis = (Integer) TAFParser.parse(arr).get("visibility");
        }

        List<Integer> enRouteVis = new ArrayList<>();
        for (var entry : all.entrySet()) {
            String airport = entry.getKey();
            if (airport.equals(depICAO) || airport.equals(arrICAO)) continue;

            String wd = entry.getValue();
            if (wd != null) {
                Integer vis = (Integer) TAFParser.parse(wd).get("visibility");
                enRouteVis.add(vis);
            }
        }

        if (!enRouteVis.isEmpty()) {
            worstEnRoute = Collections.min(enRouteVis);
        }

        Map<String, Integer> visMap = new LinkedHashMap<>();
        visMap.put("take", takeoffVis == null ? 9999 : takeoffVis);
        visMap.put("enroute", worstEnRoute == null ? 9999 : worstEnRoute);
        visMap.put("arr", landingVis == null ? 9999 : landingVis);
        return visMap;
    }
}
