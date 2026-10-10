package com.sensordetection.model;

/**
 * Result of evaluating a single sensor reading through an anomaly detector.
 */
public class AnomalyResult {
    private final String timestamp;
    private final SensorType sensorType;
    private final double value;
    private final double score;
    private final boolean anomalous;
    private final String status;
    private final String reason;
    private final int originalIndex;

    public AnomalyResult(String timestamp,
                         SensorType sensorType,
                         double value,
                         double score,
                         boolean anomalous,
                         String status,
                         String reason,
                         int originalIndex) {
        this.timestamp = timestamp;
        this.sensorType = sensorType;
        this.value = value;
        this.score = score;
        this.anomalous = anomalous;
        this.status = status;
        this.reason = reason;
        this.originalIndex = originalIndex;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public double getValue() {
        return value;
    }

    public double getScore() {
        return score;
    }

    public boolean isAnomalous() {
        return anomalous;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }
}
