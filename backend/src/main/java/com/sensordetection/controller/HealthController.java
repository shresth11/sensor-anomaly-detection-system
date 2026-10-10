package com.sensordetection.controller;

import com.sensordetection.model.DetectorType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Health check endpoint for system readiness and diagnostics.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final LocalDateTime startedAt = LocalDateTime.now();

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Sensor Anomaly Detection System");
        health.put("version", "1.0.0");
        health.put("uptimeSince", startedAt.toString());
        health.put("supportedDetectors", Arrays.stream(DetectorType.values())
                .map(Enum::name)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(health);
    }
}
