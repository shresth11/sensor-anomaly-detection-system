package com.sensordetection.controller;

import com.sensordetection.dto.AnalysisResponseDto;
import com.sensordetection.dto.SampleDataResponseDto;
import com.sensordetection.dto.SensorReadingDto;
import com.sensordetection.model.DetectorType;
import com.sensordetection.model.SensorReading;
import com.sensordetection.service.AnalysisService;
import com.sensordetection.service.SampleDataService;
import com.sensordetection.service.SensorDataParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller handling telemetry upload, anomaly detection analysis,
 * sample data provisioning, and CSV report downloads.
 */
@RestController
@RequestMapping("/api")
public class SensorAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(SensorAnalysisController.class);

    private final SensorDataParserService parserService;
    private final AnalysisService analysisService;
    private final SampleDataService sampleDataService;

    public SensorAnalysisController(SensorDataParserService parserService,
                                    AnalysisService analysisService,
                                    SampleDataService sampleDataService) {
        this.parserService = parserService;
        this.analysisService = analysisService;
        this.sampleDataService = sampleDataService;
    }

    /**
     * GET /api/sample-data — Return the built-in sample dataset as JSON.
     */
    @GetMapping("/sample-data")
    public ResponseEntity<SampleDataResponseDto> getSampleData() {
        List<SensorReading> readings = sampleDataService.getSampleReadings();
        List<SensorReadingDto> dtos = readings.stream()
                .map(SensorReadingDto::fromDomain)
                .collect(Collectors.toList());

        SampleDataResponseDto response = new SampleDataResponseDto(
                "sensor_sample_data.csv",
                dtos.size(),
                dtos
        );
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/analyze/sample — Analyze the built-in sample dataset with specified parameters.
     */
    @GetMapping("/analyze/sample")
    public ResponseEntity<AnalysisResponseDto> analyzeSample(
            @RequestParam(defaultValue = "2.0") double threshold,
            @RequestParam(defaultValue = "STATISTICAL_ZSCORE") String detectorType) {

        log.info("Analyzing built-in sample dataset with threshold={}, detector={}", threshold, detectorType);
        List<SensorReading> readings = sampleDataService.getSampleReadings();
        DetectorType type = DetectorType.fromString(detectorType);

        AnalysisResponseDto response = analysisService.analyze(
                readings,
                "sensor_sample_data.csv",
                type,
                threshold
        );
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/analyze — Ingest user-uploaded CSV file and run anomaly analysis.
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResponseDto> analyzeUploadedCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "threshold", defaultValue = "2.0") double threshold,
            @RequestParam(value = "detectorType", defaultValue = "STATISTICAL_ZSCORE") String detectorType) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded. Please select a valid CSV file.");
        }

        // Sanitize uploaded filename (never trust raw client filename for security)
        String rawFilename = file.getOriginalFilename();
        String safeName = (rawFilename != null && !rawFilename.trim().isEmpty())
                ? rawFilename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "uploaded_telemetry.csv";

        log.info("Received CSV upload '{}' (size: {} bytes). Threshold={}, detector={}",
                safeName, file.getSize(), threshold, detectorType);

        List<SensorReading> readings;
        try (InputStream is = file.getInputStream()) {
            readings = parserService.parseCsv(is);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to read uploaded file: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to read uploaded file: " + e.getMessage());
        }

        DetectorType type = DetectorType.fromString(detectorType);
        AnalysisResponseDto response = analysisService.analyze(readings, safeName, type, threshold);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/report — Download the most recently generated analysis report as CSV.
     */
    @GetMapping(value = "/report", produces = "text/csv")
    public ResponseEntity<Resource> downloadReport() {
        AnalysisService.LatestReportHolder report = analysisService.getLatestReport();

        byte[] csvBytes = report.getCsvContent().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(csvBytes);

        String filename = "sensor_anomaly_report.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvBytes.length)
                .body(resource);
    }
}
