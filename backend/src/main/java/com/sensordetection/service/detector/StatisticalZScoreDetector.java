package com.sensordetection.service.detector;

import com.sensordetection.model.AnomalyResult;
import com.sensordetection.model.DetectorType;
import com.sensordetection.model.SensorReading;
import com.sensordetection.model.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Statistical Anomaly Detector using Population Standard Deviation and Z-Score.
 *
 * <p>Mathematical Formulation:</p>
 * <ul>
 *   <li>Mean: μ = (1 / N) * ∑ x_i</li>
 *   <li>Population Standard Deviation: σ = sqrt( (1 / N) * ∑ (x_i - μ)^2 )</li>
 *   <li>Z-Score: z = (x_i - μ) / σ</li>
 * </ul>
 *
 * <p>Telemetry Engineering Consideration:</p>
 * Sensor values are partitioned by {@link SensorType} because physical units
 * (temperature in °C, pressure in kPa, vibration in g) have completely disparate
 * operating scales. Calculating z-scores within each sensor type avoids cross-sensor distortion.
 *
 * <p>Statistical Limitation Note:</p>
 * This detector is a baseline statistical heuristic, not a trained AI model.
 * In the presence of extreme outliers, both the mean (μ) and standard deviation (σ)
 * can become inflated, potentially masking secondary outliers (the masking effect).
 */
@Component
public class StatisticalZScoreDetector implements AnomalyDetector {

    private static final Logger log = LoggerFactory.getLogger(StatisticalZScoreDetector.class);

    @Override
    public DetectorType getType() {
        return DetectorType.STATISTICAL_ZSCORE;
    }

    @Override
    public List<AnomalyResult> detect(List<SensorReading> readings, double threshold) {
        if (readings == null || readings.isEmpty()) {
            return Collections.emptyList();
        }

        // Validate threshold sanity
        double activeThreshold = threshold > 0 ? threshold : 2.0;

        // Group readings by SensorType to evaluate distributions independently
        Map<SensorType, List<SensorReading>> groupedByType = readings.stream()
                .collect(Collectors.groupingBy(SensorReading::getSensorType));

        Map<Integer, AnomalyResult> resultsByIndex = new HashMap<>();

        for (Map.Entry<SensorType, List<SensorReading>> entry : groupedByType.entrySet()) {
            SensorType sensorType = entry.getKey();
            List<SensorReading> groupReadings = entry.getValue();

            // 1. Filter valid finite numbers to compute baseline statistics
            List<Double> validValues = groupReadings.stream()
                    .map(SensorReading::getValue)
                    .filter(v -> !Double.isNaN(v) && Double.isFinite(v))
                    .collect(Collectors.toList());

            if (validValues.isEmpty()) {
                for (SensorReading r : groupReadings) {
                    resultsByIndex.put(r.getOriginalIndex(), new AnomalyResult(
                            r.getTimestamp(), r.getSensorType(), r.getValue(),
                            0.0, true, "ANOMALY", "Non-finite or missing numeric value", r.getOriginalIndex()
                    ));
                }
                continue;
            }

            int n = validValues.size();

            // 2. Calculate Mean (μ)
            double sum = 0.0;
            for (Double val : validValues) {
                sum += val;
            }
            double mean = sum / n;

            // 3. Calculate Population Standard Deviation (σ)
            double sumSquaredDiff = 0.0;
            for (Double val : validValues) {
                double diff = val - mean;
                sumSquaredDiff += diff * diff;
            }
            double populationStdDev = Math.sqrt(sumSquaredDiff / n);

            log.debug("SensorType: {}, Samples: {}, Mean: {}, PopStdDev: {}",
                    sensorType, n, mean, populationStdDev);

            // 4. Calculate Z-Scores for each reading in this group
            for (SensorReading r : groupReadings) {
                double val = r.getValue();

                if (Double.isNaN(val) || !Double.isFinite(val)) {
                    resultsByIndex.put(r.getOriginalIndex(), new AnomalyResult(
                            r.getTimestamp(), r.getSensorType(), val,
                            Double.NaN, true, "ANOMALY", "Invalid non-finite number", r.getOriginalIndex()
                    ));
                    continue;
                }

                // Handle edge case: Standard deviation is zero (all readings identical)
                if (populationStdDev == 0.0 || Double.compare(populationStdDev, 0.0) == 0) {
                    resultsByIndex.put(r.getOriginalIndex(), new AnomalyResult(
                            r.getTimestamp(), r.getSensorType(), val,
                            0.0, false, "NORMAL",
                            "Uniform data (standard deviation = 0.0; value matches uniform baseline of " + round(mean, 2) + ")",
                            r.getOriginalIndex()
                    ));
                    continue;
                }

                // Compute z-score
                double zScore = (val - mean) / populationStdDev;
                double absZ = Math.abs(zScore);
                boolean isAnomaly = absZ > activeThreshold;
                String status = isAnomaly ? "ANOMALY" : "NORMAL";

                String reason = isAnomaly
                        ? String.format("|z| = %.2f > threshold %.2f (Mean: %.2f, StdDev: %.2f)",
                        absZ, activeThreshold, mean, populationStdDev)
                        : String.format("|z| = %.2f ≤ threshold %.2f (Normal operating range)",
                        absZ, activeThreshold);

                resultsByIndex.put(r.getOriginalIndex(), new AnomalyResult(
                        r.getTimestamp(), r.getSensorType(), val,
                        round(zScore, 4), isAnomaly, status, reason, r.getOriginalIndex()
                ));
            }
        }

        // Reconstruct results strictly in original input order
        List<AnomalyResult> orderedResults = new ArrayList<>(readings.size());
        for (SensorReading reading : readings) {
            AnomalyResult res = resultsByIndex.get(reading.getOriginalIndex());
            if (res != null) {
                orderedResults.add(res);
            }
        }
        return orderedResults;
    }

    /**
     * Helper to round floating point numbers for clean display and output.
     */
    public static double round(double value, int places) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        return BigDecimal.valueOf(value)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
