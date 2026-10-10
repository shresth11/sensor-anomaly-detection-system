package com.sensordetection.service;

import com.sensordetection.model.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service to load pre-configured educational sensor telemetry datasets.
 */
@Service
public class SampleDataService {

    private static final Logger log = LoggerFactory.getLogger(SampleDataService.class);
    private static final String SAMPLE_RESOURCE_PATH = "sample/sensor_sample_data.csv";

    private final SensorDataParserService parserService;

    public SampleDataService(SensorDataParserService parserService) {
        this.parserService = parserService;
    }

    /**
     * Retrieves the sample CSV content as raw UTF-8 string.
     */
    public String getSampleCsvContent() {
        try {
            ClassPathResource resource = new ClassPathResource(SAMPLE_RESOURCE_PATH);
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to load sample dataset resource: {}", e.getMessage(), e);
            throw new IllegalStateException("Sample dataset resource could not be loaded: " + e.getMessage());
        }
    }

    /**
     * Returns parsed sample sensor telemetry readings.
     */
    public List<SensorReading> getSampleReadings() {
        try {
            ClassPathResource resource = new ClassPathResource(SAMPLE_RESOURCE_PATH);
            try (InputStream is = resource.getInputStream()) {
                return parserService.parseCsv(is);
            }
        } catch (Exception e) {
            log.error("Failed to parse sample readings: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to parse sample telemetry: " + e.getMessage());
        }
    }
}
