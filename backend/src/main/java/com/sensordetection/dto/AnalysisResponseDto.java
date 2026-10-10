package com.sensordetection.dto;

import com.sensordetection.model.AnomalyResult;
import java.util.List;

/**
 * Full response payload returned to the frontend after analysis completes.
 */
public class AnalysisResponseDto {
    private boolean success;
    private String message;
    private DashboardStatsDto stats;
    private List<AnomalyResult> results;
    private String detectorType;
    private double threshold;
    private String analyzedAt;

    public AnalysisResponseDto() {
    }

    public AnalysisResponseDto(boolean success,
                               String message,
                               DashboardStatsDto stats,
                               List<AnomalyResult> results,
                               String detectorType,
                               double threshold,
                               String analyzedAt) {
        this.success = success;
        this.message = message;
        this.stats = stats;
        this.results = results;
        this.detectorType = detectorType;
        this.threshold = threshold;
        this.analyzedAt = analyzedAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DashboardStatsDto getStats() {
        return stats;
    }

    public void setStats(DashboardStatsDto stats) {
        this.stats = stats;
    }

    public List<AnomalyResult> getResults() {
        return results;
    }

    public void setResults(List<AnomalyResult> results) {
        this.results = results;
    }

    public String getDetectorType() {
        return detectorType;
    }

    public void setDetectorType(String detectorType) {
        this.detectorType = detectorType;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(String analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
