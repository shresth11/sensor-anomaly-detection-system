package com.sensordetection.dto;

import java.time.LocalDateTime;

public class ApiErrorResponseDto {
    private boolean success;
    private String error;
    private String message;
    private int status;
    private String timestamp;

    public ApiErrorResponseDto() {
    }

    public ApiErrorResponseDto(String error, String message, int status) {
        this.success = false;
        this.error = error;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now().toString();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
