package com.sensordetection.service;

import com.sensordetection.model.SensorReading;
import com.sensordetection.model.SensorType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for safe parsing and rigorous validation of CSV sensor telemetry data.
 *
 * <p>Security & Robustness Rules:</p>
 * <ul>
 *   <li>Never trust client-supplied filenames or arbitrary encodings (enforce UTF-8).</li>
 *   <li>Enforce mandatory column presence (case-insensitive header mapping).</li>
 *   <li>Verify numeric finiteness to prevent NaN/Infinity poisoning.</li>
 *   <li>Provide actionable error messages with precise line numbers for debugging.</li>
 * </ul>
 */
@Service
public class SensorDataParserService {

    private static final Logger log = LoggerFactory.getLogger(SensorDataParserService.class);
    private static final int MAX_ALLOWABLE_RECORDS = 50_000;

    /**
     * Parses CSV stream into structured SensorReading domain objects.
     *
     * @param inputStream CSV input byte stream.
     * @return Validated list of SensorReading models.
     */
    public List<SensorReading> parseCsv(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("CSV input stream cannot be null.");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return parseReader(reader);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse CSV stream: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to read CSV input stream: " + e.getMessage());
        }
    }

    /**
     * Parses CSV text string into structured SensorReading domain objects.
     */
    public List<SensorReading> parseCsvString(String csvContent) {
        if (csvContent == null || csvContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Uploaded CSV content is empty.");
        }
        try (StringReader reader = new StringReader(csvContent.trim())) {
            return parseReader(new BufferedReader(reader));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse CSV string: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to process CSV data: " + e.getMessage());
        }
    }

    private List<SensorReading> parseReader(BufferedReader reader) throws Exception {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();

        List<SensorReading> readings = new ArrayList<>();

        try (CSVParser parser = format.parse(reader)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();

            if (headerMap == null || headerMap.isEmpty()) {
                throw new IllegalArgumentException("CSV file must contain a header row: timestamp,sensorType,value");
            }

            // Verify mandatory columns exist
            String timestampCol = findColumn(headerMap, "timestamp");
            String sensorTypeCol = findColumn(headerMap, "sensortype");
            String valueCol = findColumn(headerMap, "value");

            if (timestampCol == null) {
                throw new IllegalArgumentException("Missing required column 'timestamp' in CSV header.");
            }
            if (sensorTypeCol == null) {
                throw new IllegalArgumentException("Missing required column 'sensorType' in CSV header.");
            }
            if (valueCol == null) {
                throw new IllegalArgumentException("Missing required column 'value' in CSV header.");
            }

            int index = 0;
            for (CSVRecord record : parser) {
                long lineNum = record.getRecordNumber() + 1; // 1-based line accounting for header

                if (index >= MAX_ALLOWABLE_RECORDS) {
                    throw new IllegalArgumentException("CSV exceeds maximum allowed limit of "
                            + MAX_ALLOWABLE_RECORDS + " records.");
                }

                String rawTimestamp = record.get(timestampCol);
                String rawType = record.get(sensorTypeCol);
                String rawValue = record.get(valueCol);

                if (rawTimestamp == null || rawTimestamp.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            String.format("Malformed CSV at line %d: 'timestamp' cannot be blank.", lineNum));
                }

                if (rawType == null || rawType.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            String.format("Malformed CSV at line %d: 'sensorType' cannot be blank.", lineNum));
                }

                if (rawValue == null || rawValue.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            String.format("Malformed CSV at line %d: 'value' cannot be blank.", lineNum));
                }

                double value;
                try {
                    value = Double.parseDouble(rawValue.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            String.format("Malformed CSV at line %d: Invalid numeric value '%s'.", lineNum, rawValue));
                }

                if (Double.isNaN(value) || !Double.isFinite(value)) {
                    throw new IllegalArgumentException(
                            String.format("Malformed CSV at line %d: Numeric value '%s' is not finite.", lineNum, rawValue));
                }

                SensorType sensorType = SensorType.fromString(rawType);
                readings.add(new SensorReading(rawTimestamp.trim(), sensorType, value, index));
                index++;
            }
        }

        if (readings.isEmpty()) {
            throw new IllegalArgumentException("CSV file contains no data records.");
        }

        return readings;
    }

    private String findColumn(Map<String, Integer> headerMap, String normalizedName) {
        for (String key : headerMap.keySet()) {
            if (key.equalsIgnoreCase(normalizedName)) {
                return key;
            }
        }
        return null;
    }
}
