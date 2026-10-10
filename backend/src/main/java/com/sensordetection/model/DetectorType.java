package com.sensordetection.model;

/**
 * Anomaly detection algorithms supported by the backend engine.
 */
public enum DetectorType {
    /**
     * Parametric baseline using Mean, Standard Deviation, and Z-Score.
     */
    STATISTICAL_ZSCORE,

    /**
     * Unsupervised machine learning ensemble isolating data points using random partition trees.
     */
    ISOLATION_FOREST;

    public static DetectorType fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return STATISTICAL_ZSCORE;
        }
        try {
            return DetectorType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STATISTICAL_ZSCORE;
        }
    }
}
