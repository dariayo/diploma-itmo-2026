package com.diploma.controller;

import com.diploma.entities.RouteDirectory;
import com.diploma.repository.FlightXMLGroupRepository;
import com.diploma.repository.RouteDirectoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightRoutesController {
    private final RouteDirectoryRepository routeDirectoryRepository;
    private final FlightXMLGroupRepository flightXMLGroupRepository;

    @GetMapping("/routes")
    public List<String> getRoutes() {
        Set<String> routes = new LinkedHashSet<>();

        routeDirectoryRepository.findAll().stream()
                .map(RouteDirectory::getRouteCode)
                .filter(route -> route != null && !route.isBlank())
                .forEach(routes::add);

        if (routes.isEmpty()) {
            flightXMLGroupRepository.findDistinctRoutes().stream()
                    .filter(route -> route != null && !route.isBlank())
                    .forEach(routes::add);
        }

        return new ArrayList<>(routes);
    }
}
