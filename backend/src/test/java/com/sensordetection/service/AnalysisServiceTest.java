package com.sensordetection.service;

import com.sensordetection.dto.AnalysisResponseDto;
import com.sensordetection.model.DetectorType;
import com.sensordetection.model.SensorReading;
import com.sensordetection.model.SensorType;
import com.sensordetection.service.detector.IsolationForestDetector;
import com.sensordetection.service.detector.StatisticalZScoreDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Analysis Service Tests")
class AnalysisServiceTest {

    private AnalysisService analysisService;
    private ReportExportService reportExportService;

    @BeforeEach
    void setUp() {
        StatisticalZScoreDetector zScore = new StatisticalZScoreDetector();
        IsolationForestDetector iForest = new IsolationForestDetector();
        reportExportService = new ReportExportService();
        analysisService = new AnalysisService(zScore, iForest, reportExportService);
    }

    @Test
    @DisplayName("Should successfully analyze telemetry and compute statistics")
    void testSuccessfulAnalysis() {
        List<SensorReading> readings = List.of(
                new SensorReading("2026-01-01T09:00:00", SensorType.TEMPERATURE, 20.0, 0),
                new SensorReading("2026-01-01T09:01:00", SensorType.TEMPERATURE, 20.0, 1),
                new SensorReading("2026-01-01T09:02:00", SensorType.TEMPERATURE, 20.0, 2),
                new SensorReading("2026-01-01T09:03:00", SensorType.TEMPERATURE, 80.0, 3)
        );

        AnalysisResponseDto response = analysisService.analyze(
                readings,
                "test_dataset.csv",
                DetectorType.STATISTICAL_ZSCORE,
                1.5
        );

        assertTrue(response.isSuccess());
        assertNotNull(response.getStats());
        assertEquals(4, response.getStats().getTotalReadings());
        assertEquals(1, response.getStats().getAnomalyCount());
        assertEquals(3, response.getStats().getNormalCount());
        assertEquals(25.0, response.getStats().getAnomalyPercentage());
        assertEquals(35.0, response.getStats().getAverageValue());
        assertEquals(20.0, response.getStats().getMinValue());
        assertEquals(80.0, response.getStats().getMaxValue());

        // Verify report is cached and retrievable
        AnalysisService.LatestReportHolder cachedReport = analysisService.getLatestReport();
        assertNotNull(cachedReport);
        assertTrue(cachedReport.getCsvContent().contains("timestamp,sensorType,value,score,status"));
    }

    @Test
    @DisplayName("Should reject empty dataset")
    void testEmptyDatasetRejection() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                analysisService.analyze(Collections.emptyList(), "empty.csv", DetectorType.STATISTICAL_ZSCORE, 2.0)
        );
        assertTrue(ex.getMessage().contains("Cannot analyze an empty dataset"));
    }
}
