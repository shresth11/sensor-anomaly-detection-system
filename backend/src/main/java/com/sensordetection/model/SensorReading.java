package com.sensordetection.model;

import java.util.Objects;

/**
 * Immutable representation of a single telemetry reading parsed from CSV or API input.
 */
public class SensorReading {
    private final String timestamp;
    private final SensorType sensorType;
    private final double value;
    private final int originalIndex;

    public SensorReading(String timestamp, SensorType sensorType, double value, int originalIndex) {
        this.timestamp = timestamp;
        this.sensorType = sensorType;
        this.value = value;
        this.originalIndex = originalIndex;
    }

    public SensorReading(String timestamp, SensorType sensorType, double value) {
        this(timestamp, sensorType, value, 0);
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

    public int getOriginalIndex() {
        return originalIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorReading that = (SensorReading) o;
        return Double.compare(that.value, value) == 0 &&
                originalIndex == that.originalIndex &&
                Objects.equals(timestamp, that.timestamp) &&
                sensorType == that.sensorType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, sensorType, value, originalIndex);
    }

    @Override
    public String toString() {
        return "SensorReading{" +
                "timestamp='" + timestamp + '\'' +
                ", sensorType=" + sensorType +
                ", value=" + value +
                ", originalIndex=" + originalIndex +
                '}';
    }
}
