package com.sensordetection.dto;

import java.util.List;

public class SampleDataResponseDto {
    private String filename;
    private int totalCount;
    private List<SensorReadingDto> readings;

    public SampleDataResponseDto() {
    }

    public SampleDataResponseDto(String filename, int totalCount, List<SensorReadingDto> readings) {
        this.filename = filename;
        this.totalCount = totalCount;
        this.readings = readings;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<SensorReadingDto> getReadings() {
        return readings;
    }

    public void setReadings(List<SensorReadingDto> readings) {
        this.readings = readings;
    }
}
