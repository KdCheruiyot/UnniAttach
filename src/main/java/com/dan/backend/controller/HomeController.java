package com.dan.backend.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "name", "UnniAttach API",
                "status", "running",
                "docs", "/swagger-ui.html",
                "health", "/actuator/health");
    }
}
