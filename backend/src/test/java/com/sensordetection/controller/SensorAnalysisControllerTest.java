package com.sensordetection.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Sensor Analysis Controller End-to-End API Tests")
class SensorAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/health should return UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.supportedDetectors").isArray())
                .andExpect(jsonPath("$.supportedDetectors", hasItem("STATISTICAL_ZSCORE")))
                .andExpect(jsonPath("$.supportedDetectors", hasItem("ISOLATION_FOREST")));
    }

    @Test
    @DisplayName("GET /api/sample-data should return sample telemetry records")
    void testSampleDataEndpoint() throws Exception {
        mockMvc.perform(get("/api/sample-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("sensor_sample_data.csv"))
                .andExpect(jsonPath("$.totalCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.readings").isArray());
    }

    @Test
    @DisplayName("GET /api/analyze/sample should successfully analyze built-in sample data")
    void testAnalyzeSampleEndpoint() throws Exception {
        mockMvc.perform(get("/api/analyze/sample")
                        .param("threshold", "2.0")
                        .param("detectorType", "STATISTICAL_ZSCORE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats.totalReadings").value(greaterThan(0)))
                .andExpect(jsonPath("$.stats.anomalyCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("POST /api/analyze should process uploaded CSV multipart file")
    void testAnalyzeUploadValidFile() throws Exception {
        String csvContent = "timestamp,sensorType,value\n" +
                "2026-01-01T09:00:00,temperature,24.1\n" +
                "2026-01-01T09:05:00,temperature,24.3\n" +
                "2026-01-01T09:10:00,temperature,24.2\n" +
                "2026-01-01T09:15:00,temperature,75.0\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "custom_sensor.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/analyze")
                        .file(file)
                        .param("threshold", "1.5")
                        .param("detectorType", "STATISTICAL_ZSCORE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats.totalReadings").value(4))
                .andExpect(jsonPath("$.stats.anomalyCount").value(1));
    }

    @Test
    @DisplayName("POST /api/analyze should return 400 Bad Request on empty file")
    void testAnalyzeUploadEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/analyze").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("GET /api/report should download CSV report after analysis")
    void testReportDownload() throws Exception {
        // Run sample analysis first to populate report cache
        mockMvc.perform(get("/api/analyze/sample"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/report"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"sensor_anomaly_report.csv\"")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv")))
                .andExpect(content().string(containsString("timestamp,sensorType,value,score,status")));
    }
}
