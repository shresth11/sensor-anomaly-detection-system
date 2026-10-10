package com.sensordetection.service;

import com.sensordetection.dto.AnalysisResponseDto;
import com.sensordetection.dto.DashboardStatsDto;
import com.sensordetection.model.AnomalyResult;
import com.sensordetection.model.DetectorType;
import com.sensordetection.model.SensorReading;
import com.sensordetection.model.SensorType;
import com.sensordetection.service.detector.AnomalyDetector;
import com.sensordetection.service.detector.IsolationForestDetector;
import com.sensordetection.service.detector.StatisticalZScoreDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Core orchestrator service connecting CSV ingestion, detector strategy execution,
 * statistical aggregation, and report caching.
 */
@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final StatisticalZScoreDetector zScoreDetector;
    private final IsolationForestDetector isolationForestDetector;
    private final ReportExportService reportExportService;

    // Thread-safe in-memory cache for the most recently analyzed report
    private final AtomicReference<LatestReportHolder> latestReportCache = new AtomicReference<>();

    public AnalysisService(StatisticalZScoreDetector zScoreDetector,
                           IsolationForestDetector isolationForestDetector,
                           ReportExportService reportExportService) {
        this.zScoreDetector = zScoreDetector;
        this.isolationForestDetector = isolationForestDetector;
        this.reportExportService = reportExportService;
    }

    /**
     * Executes telemetry analysis for the given readings with selected detector and threshold.
     */
    public AnalysisResponseDto analyze(List<SensorReading> readings,
                                       String datasetName,
                                       DetectorType detectorType,
                                       double threshold) {
        if (readings == null || readings.isEmpty()) {
            throw new IllegalArgumentException("Cannot analyze an empty dataset.");
        }

        AnomalyDetector detector = resolveDetector(detectorType);
        log.info("Executing analysis on {} readings using {} with threshold {}",
                readings.size(), detector.getType(), threshold);

        List<AnomalyResult> results = detector.detect(readings, threshold);
        DashboardStatsDto stats = computeStats(readings, results, datasetName, detector.getType().name(), threshold);

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String csvReport = reportExportService.generateCsvReport(results);

        // Cache the report safely in memory for GET /api/report download
        latestReportCache.set(new LatestReportHolder(csvReport, datasetName, now));

        return new AnalysisResponseDto(
                true,
                "Analysis completed successfully using " + detector.getType(),
                stats,
                results,
                detector.getType().name(),
                threshold,
                now
        );
    }

    /**
     * Returns the cached latest report CSV content.
     */
    public LatestReportHolder getLatestReport() {
        LatestReportHolder holder = latestReportCache.get();
        if (holder == null) {
            throw new IllegalStateException("No analysis report has been generated yet. Please run an analysis first.");
        }
        return holder;
    }

    private AnomalyDetector resolveDetector(DetectorType detectorType) {
        if (detectorType == DetectorType.ISOLATION_FOREST) {
            return isolationForestDetector;
        }
        return zScoreDetector;
    }

    private DashboardStatsDto computeStats(List<SensorReading> readings,
                                           List<AnomalyResult> results,
                                           String datasetName,
                                           String detectorUsed,
                                           double thresholdUsed) {
        long total = results.size();
        long anomalyCount = results.stream().filter(AnomalyResult::isAnomalous).count();
        long normalCount = total - anomalyCount;
        double anomalyPct = total > 0 ? ((double) anomalyCount / total) * 100.0 : 0.0;

        List<Double> validValues = readings.stream()
                .map(SensorReading::getValue)
                .filter(v -> !Double.isNaN(v) && Double.isFinite(v))
                .collect(Collectors.toList());

        double avg = 0.0;
        double min = 0.0;
        double max = 0.0;
        double stdDev = 0.0;

        if (!validValues.isEmpty()) {
            double sum = 0.0;
            min = validValues.get(0);
            max = validValues.get(0);

            for (Double v : validValues) {
                sum += v;
                if (v < min) min = v;
                if (v > max) max = v;
            }
            avg = sum / validValues.size();

            double sumSq = 0.0;
            for (Double v : validValues) {
                double diff = v - avg;
                sumSq += diff * diff;
            }
            stdDev = Math.sqrt(sumSq / validValues.size());
        }

        Map<String, Long> anomaliesBySensorType = results.stream()
                .filter(AnomalyResult::isAnomalous)
                .collect(Collectors.groupingBy(
                        r -> r.getSensorType().name().toLowerCase(),
                        Collectors.counting()
                ));

        return new DashboardStatsDto(
                total,
                anomalyCount,
                normalCount,
                StatisticalZScoreDetector.round(anomalyPct, 2),
                StatisticalZScoreDetector.round(avg, 2),
                StatisticalZScoreDetector.round(min, 2),
                StatisticalZScoreDetector.round(max, 2),
                StatisticalZScoreDetector.round(stdDev, 2),
                datasetName,
                detectorUsed,
                thresholdUsed,
                anomaliesBySensorType
        );
    }

    public static class LatestReportHolder {
        private final String csvContent;
        private final String datasetName;
        private final String generatedAt;

        public LatestReportHolder(String csvContent, String datasetName, String generatedAt) {
            this.csvContent = csvContent;
            this.datasetName = datasetName;
            this.generatedAt = generatedAt;
        }

        public String getCsvContent() {
            return csvContent;
        }

        public String getDatasetName() {
            return datasetName;
        }

        public String getGeneratedAt() {
            return generatedAt;
        }
    }
}
