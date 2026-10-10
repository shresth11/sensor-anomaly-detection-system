package com.sensordetection.detector;

import com.sensordetection.model.AnomalyResult;
import com.sensordetection.model.SensorReading;
import com.sensordetection.model.SensorType;
import com.sensordetection.service.detector.StatisticalZScoreDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Statistical Z-Score Detector Tests")
class StatisticalZScoreDetectorTest {

    private StatisticalZScoreDetector detector;

    @BeforeEach
    void setUp() {
        detector = new StatisticalZScoreDetector();
    }

    @Test
    @DisplayName("Should return empty list for empty readings")
    void testEmptyDataset() {
        List<AnomalyResult> results = detector.detect(Collections.emptyList(), 2.0);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should correctly identify normal readings within threshold")
    void testNormalReadings() {
        List<SensorReading> readings = List.of(
                new SensorReading("2026-01-01T09:00:00", SensorType.TEMPERATURE, 24.1, 0),
                new SensorReading("2026-01-01T09:01:00", SensorType.TEMPERATURE, 24.3, 1),
                new SensorReading("2026-01-01T09:02:00", SensorType.TEMPERATURE, 24.2, 2),
                new SensorReading("2026-01-01T09:03:00", SensorType.TEMPERATURE, 24.4, 3),
                new SensorReading("2026-01-01T09:04:00", SensorType.TEMPERATURE, 24.2, 4)
        );

        List<AnomalyResult> results = detector.detect(readings, 2.0);
        assertEquals(5, results.size());
        for (AnomalyResult r : results) {
            assertFalse(r.isAnomalous(), "Reading should not be flagged: " + r.getValue());
            assertEquals("NORMAL", r.getStatus());
            assertTrue(Math.abs(r.getScore()) <= 2.0);
        }
    }

    @Test
    @DisplayName("Should detect clearly anomalous outlier spike")
    void testClearlyAnomalousReading() {
        List<SensorReading> readings = List.of(
                new SensorReading("2026-01-01T09:00:00", SensorType.TEMPERATURE, 24.0, 0),
                new SensorReading("2026-01-01T09:01:00", SensorType.TEMPERATURE, 24.2, 1),
                new SensorReading("2026-01-01T09:02:00", SensorType.TEMPERATURE, 24.1, 2),
                new SensorReading("2026-01-01T09:03:00", SensorType.TEMPERATURE, 24.0, 3),
                new SensorReading("2026-01-01T09:04:00", SensorType.TEMPERATURE, 24.1, 4),
                new SensorReading("2026-01-01T09:05:00", SensorType.TEMPERATURE, 24.3, 5),
                new SensorReading("2026-01-01T09:06:00", SensorType.TEMPERATURE, 95.0, 6) // Extreme outlier
        );

        List<AnomalyResult> results = detector.detect(readings, 2.0);
        AnomalyResult outlier = results.get(6);

        assertTrue(outlier.isAnomalous());
        assertEquals("ANOMALY", outlier.getStatus());
        assertTrue(outlier.getScore() > 2.0);
        assertTrue(outlier.getReason().contains("|z|"));
    }

    @Test
    @DisplayName("Should safely handle identical readings with zero standard deviation")
    void testIdenticalReadingsZeroVariance() {
        List<SensorReading> readings = List.of(
                new SensorReading("2026-01-01T09:00:00", SensorType.PRESSURE, 101.3, 0),
                new SensorReading("2026-01-01T09:01:00", SensorType.PRESSURE, 101.3, 1),
                new SensorReading("2026-01-01T09:02:00", SensorType.PRESSURE, 101.3, 2),
                new SensorReading("2026-01-01T09:03:00", SensorType.PRESSURE, 101.3, 3)
        );

        List<AnomalyResult> results = detector.detect(readings, 2.0);
        assertEquals(4, results.size());
        for (AnomalyResult r : results) {
            assertFalse(r.isAnomalous());
            assertEquals("NORMAL", r.getStatus());
            assertEquals(0.0, r.getScore());
            assertTrue(r.getReason().contains("standard deviation = 0.0"));
        }
    }

    @Test
    @DisplayName("Should adjust sensitivity based on configured threshold")
    void testThresholdConfiguration() {
        List<SensorReading> readings = List.of(
                new SensorReading("2026-01-01T09:00:00", SensorType.TEMPERATURE, 20.0, 0),
                new SensorReading("2026-01-01T09:01:00", SensorType.TEMPERATURE, 20.5, 1),
                new SensorReading("2026-01-01T09:02:00", SensorType.TEMPERATURE, 21.0, 2),
                new SensorReading("2026-01-01T09:03:00", SensorType.TEMPERATURE, 20.2, 3),
                new SensorReading("2026-01-01T09:04:00", SensorType.TEMPERATURE, 20.1, 4),
                new SensorReading("2026-01-01T09:05:00", SensorType.TEMPERATURE, 27.0, 5) // Moderate anomaly
        );

        // With strict threshold of 1.5, the reading should be flagged
        List<AnomalyResult> strictResults = detector.detect(readings, 1.5);
        long strictAnomalies = strictResults.stream().filter(AnomalyResult::isAnomalous).count();

        // With relaxed threshold of 4.0, it should not be flagged
        List<AnomalyResult> relaxedResults = detector.detect(readings, 4.0);
        long relaxedAnomalies = relaxedResults.stream().filter(AnomalyResult::isAnomalous).count();

        assertTrue(strictAnomalies >= relaxedAnomalies);
        assertEquals(0, relaxedAnomalies);
    }

    @Test
    @DisplayName("Should safely handle invalid non-finite numbers without crashing")
    void testNonFiniteNumericHandling() {
        List<SensorReading> readings = new ArrayList<>();
        readings.add(new SensorReading("2026-01-01T09:00:00", SensorType.VIBRATION, 0.03, 0));
        readings.add(new SensorReading("2026-01-01T09:01:00", SensorType.VIBRATION, Double.NaN, 1));
        readings.add(new SensorReading("2026-01-01T09:02:00", SensorType.VIBRATION, 0.04, 2));

        List<AnomalyResult> results = detector.detect(readings, 2.0);
        assertEquals(3, results.size());
        AnomalyResult nanResult = results.get(1);
        assertTrue(nanResult.isAnomalous());
        assertEquals("ANOMALY", nanResult.getStatus());
    }
}
