package com.sensordetection.dto;

import java.util.Map;

/**
 * Summary metrics displayed on the dashboard cards.
 */
public class DashboardStatsDto {
    private long totalReadings;
    private long anomalyCount;
    private long normalCount;
    private double anomalyPercentage;
    private double averageValue;
    private double minValue;
    private double maxValue;
    private double standardDeviation;
    private String datasetName;
    private String detectorUsed;
    private double thresholdUsed;
    private Map<String, Long> anomaliesBySensorType;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalReadings,
                             long anomalyCount,
                             long normalCount,
                             double anomalyPercentage,
                             double averageValue,
                             double minValue,
                             double maxValue,
                             double standardDeviation,
                             String datasetName,
                             String detectorUsed,
                             double thresholdUsed,
                             Map<String, Long> anomaliesBySensorType) {
        this.totalReadings = totalReadings;
        this.anomalyCount = anomalyCount;
        this.normalCount = normalCount;
        this.anomalyPercentage = anomalyPercentage;
        this.averageValue = averageValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.standardDeviation = standardDeviation;
        this.datasetName = datasetName;
        this.detectorUsed = detectorUsed;
        this.thresholdUsed = thresholdUsed;
        this.anomaliesBySensorType = anomaliesBySensorType;
    }

    public long getTotalReadings() {
        return totalReadings;
    }

    public void setTotalReadings(long totalReadings) {
        this.totalReadings = totalReadings;
    }

    public long getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(long anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(long normalCount) {
        this.normalCount = normalCount;
    }

    public double getAnomalyPercentage() {
        return anomalyPercentage;
    }

    public void setAnomalyPercentage(double anomalyPercentage) {
        this.anomalyPercentage = anomalyPercentage;
    }

    public double getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(double averageValue) {
        this.averageValue = averageValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDetectorUsed() {
        return detectorUsed;
    }

    public void setDetectorUsed(String detectorUsed) {
        this.detectorUsed = detectorUsed;
    }

    public double getThresholdUsed() {
        return thresholdUsed;
    }

    public void setThresholdUsed(double thresholdUsed) {
        this.thresholdUsed = thresholdUsed;
    }

    public Map<String, Long> getAnomaliesBySensorType() {
        return anomaliesBySensorType;
    }

    public void setAnomaliesBySensorType(Map<String, Long> anomaliesBySensorType) {
        this.anomaliesBySensorType = anomaliesBySensorType;
    }
}
