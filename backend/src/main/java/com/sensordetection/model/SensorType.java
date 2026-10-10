package com.sensordetection.model;

/**
 * Supported sensor physical measurement categories.
 * Educational note: In engineering telemetry, each sensor type operates
 * on completely different physical units (e.g., Celsius, kPa, g-force).
 */
public enum SensorType {
    TEMPERATURE,
    PRESSURE,
    VIBRATION,
    OTHER;

    /**
     * Safely parse a sensor type string from CSV input (case-insensitive).
     *
     * @param rawType The string representation from CSV.
     * @return Corresponding SensorType or OTHER if unrecognized.
     */
    public static SensorType fromString(String rawType) {
        if (rawType == null || rawType.trim().isEmpty()) {
            return OTHER;
        }
        String normalized = rawType.trim().toUpperCase();
        try {
            return SensorType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
