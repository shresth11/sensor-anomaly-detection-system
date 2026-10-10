package com.sensordetection.detector;

import com.sensordetection.model.AnomalyResult;
import com.sensordetection.model.SensorReading;
import com.sensordetection.model.SensorType;
import com.sensordetection.service.detector.IsolationForestDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Isolation Forest Machine Learning Detector Tests")
class IsolationForestDetectorTest {

    private IsolationForestDetector detector;

    @BeforeEach
    void setUp() {
        detector = new IsolationForestDetector();
    }

    @Test
    @DisplayName("Should return empty list for empty readings")
    void testEmptyDataset() {
        List<AnomalyResult> results = detector.detect(Collections.emptyList(), 0.60);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should detect prominent outlier using Isolation Forest ensemble")
    void testDetectOutlier() {
        List<SensorReading> readings = List.of(
                new SensorReading("2026-01-01T09:00:00", SensorType.TEMPERATURE, 24.1, 0),
                new SensorReading("2026-01-01T09:01:00", SensorType.TEMPERATURE, 24.2, 1),
                new SensorReading("2026-01-01T09:02:00", SensorType.TEMPERATURE, 24.3, 2),
                new SensorReading("2026-01-01T09:03:00", SensorType.TEMPERATURE, 24.1, 3),
                new SensorReading("2026-01-01T09:04:00", SensorType.TEMPERATURE, 24.2, 4),
                new SensorReading("2026-01-01T09:05:00", SensorType.TEMPERATURE, 24.4, 5),
                new SensorReading("2026-01-01T09:06:00", SensorType.TEMPERATURE, 24.2, 6),
                new SensorReading("2026-01-01T09:07:00", SensorType.TEMPERATURE, 24.3, 7),
                new SensorReading("2026-01-01T09:08:00", SensorType.TEMPERATURE, 24.1, 8),
                new SensorReading("2026-01-01T09:09:00", SensorType.TEMPERATURE, 98.5, 9) // Massive outlier
        );

        List<AnomalyResult> results = detector.detect(readings, 0.60);
        assertEquals(10, results.size());

        AnomalyResult outlier = results.get(9);
        assertTrue(outlier.isAnomalous(), "Outlier should be flagged by Isolation Forest");
        assertTrue(outlier.getScore() >= 0.60, "Outlier score should exceed 0.60");
        assertTrue(outlier.getReason().contains("iForest score"));
    }

    @Test
    @DisplayName("Should safely handle uniform values in dataset")
    void testUniformValues() {
        List<SensorReading> readings = List.of(
                new SensorReading("2026-01-01T09:00:00", SensorType.PRESSURE, 101.3, 0),
                new SensorReading("2026-01-01T09:01:00", SensorType.PRESSURE, 101.3, 1),
                new SensorReading("2026-01-01T09:02:00", SensorType.PRESSURE, 101.3, 2)
        );

        List<AnomalyResult> results = detector.detect(readings, 0.60);
        assertEquals(3, results.size());
        for (AnomalyResult r : results) {
            assertFalse(r.isAnomalous());
            assertEquals("NORMAL", r.getStatus());
        }
    }
}
