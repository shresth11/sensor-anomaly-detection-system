package com.sensordetection.dto;

import com.sensordetection.model.SensorReading;

public class SensorReadingDto {
    private String timestamp;
    private String sensorType;
    private double value;

    public SensorReadingDto() {
    }

    public SensorReadingDto(String timestamp, String sensorType, double value) {
        this.timestamp = timestamp;
        this.sensorType = sensorType;
        this.value = value;
    }

    public static SensorReadingDto fromDomain(SensorReading reading) {
        return new SensorReadingDto(
                reading.getTimestamp(),
                reading.getSensorType().name().toLowerCase(),
                reading.getValue()
        );
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
