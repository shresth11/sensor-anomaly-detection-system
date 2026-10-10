package com.sensordetection.service;

import com.sensordetection.model.AnomalyResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;

/**
 * Service to export anomaly detection results into CSV reports.
 *
 * <p>CSV Format Output:</p>
 * {@code timestamp,sensorType,value,score,status}
 */
@Service
public class ReportExportService {

    private static final Logger log = LoggerFactory.getLogger(ReportExportService.class);

    /**
     * Generates standard CSV report string from detection results.
     *
     * @param results Anomaly detection evaluation records.
     * @return Formatted CSV report text.
     */
    public String generateCsvReport(List<AnomalyResult> results) {
        if (results == null || results.isEmpty()) {
            return "timestamp,sensorType,value,score,status\n";
        }

        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("timestamp", "sensorType", "value", "score", "status")
                .build();

        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            for (AnomalyResult r : results) {
                printer.printRecord(
                        r.getTimestamp(),
                        r.getSensorType().name().toLowerCase(),
                        r.getValue(),
                        r.getScore(),
                        r.getStatus()
                );
            }
            printer.flush();
        } catch (Exception e) {
            log.error("Failed to generate CSV report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate CSV report: " + e.getMessage(), e);
        }

        return sw.toString();
    }
}
