package com.sensordetection.service;

import com.sensordetection.model.SensorReading;
import com.sensordetection.model.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sensor Data Parser Service Tests")
class SensorDataParserServiceTest {

    private SensorDataParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new SensorDataParserService();
    }

    @Test
    @DisplayName("Should successfully parse valid standard CSV")
    void testParseValidCsv() {
        String csv = "timestamp,sensorType,value\n" +
                "2026-01-01T09:00:00,temperature,24.1\n" +
                "2026-01-01T09:05:00,pressure,101.3\n" +
                "2026-01-01T09:10:00,vibration,0.032\n";

        List<SensorReading> readings = parserService.parseCsvString(csv);
        assertEquals(3, readings.size());

        assertEquals("2026-01-01T09:00:00", readings.get(0).getTimestamp());
        assertEquals(SensorType.TEMPERATURE, readings.get(0).getSensorType());
        assertEquals(24.1, readings.get(0).getValue());

        assertEquals(SensorType.PRESSURE, readings.get(1).getSensorType());
        assertEquals(SensorType.VIBRATION, readings.get(2).getSensorType());
    }

    @Test
    @DisplayName("Should support case-insensitive header mapping")
    void testCaseInsensitiveHeaders() {
        String csv = "TIMESTAMP,SENSORTYPE,VALUE\n" +
                "2026-01-01T09:00:00,TEMPERATURE,25.4\n";

        List<SensorReading> readings = parserService.parseCsvString(csv);
        assertEquals(1, readings.size());
        assertEquals(25.4, readings.get(0).getValue());
    }

    @Test
    @DisplayName("Should reject malformed CSV missing required column")
    void testMissingRequiredColumn() {
        String csv = "timestamp,sensorType\n" +
                "2026-01-01T09:00:00,temperature\n";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                parserService.parseCsvString(csv)
        );
        assertTrue(ex.getMessage().contains("Missing required column 'value'"));
    }

    @Test
    @DisplayName("Should reject CSV with invalid numeric values")
    void testInvalidNumericValue() {
        String csv = "timestamp,sensorType,value\n" +
                "2026-01-01T09:00:00,temperature,INVALID_NUMBER\n";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                parserService.parseCsvString(csv)
        );
        assertTrue(ex.getMessage().contains("Invalid numeric value"));
    }

    @Test
    @DisplayName("Should reject empty CSV content")
    void testEmptyCsvContent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                parserService.parseCsvString("   \n  \n")
        );
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("Should reject CSV with missing timestamp")
    void testMissingTimestamp() {
        String csv = "timestamp,sensorType,value\n" +
                ",temperature,25.0\n";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                parserService.parseCsvString(csv)
        );
        assertTrue(ex.getMessage().contains("'timestamp' cannot be blank"));
    }
}
