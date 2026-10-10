package com.sensordetection.exception;

import com.sensordetection.dto.ApiErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Centralized exception handler providing consistent and helpful JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        ApiErrorResponseDto error = new ApiErrorResponseDto("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponseDto> handleIllegalState(IllegalStateException ex) {
        log.warn("State conflict: {}", ex.getMessage());
        ApiErrorResponseDto error = new ApiErrorResponseDto("CONFLICT", ex.getMessage(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Uploaded file exceeds allowable limit: {}", ex.getMessage());
        ApiErrorResponseDto error = new ApiErrorResponseDto(
                "PAYLOAD_TOO_LARGE",
                "Uploaded CSV exceeds maximum allowed file size of 10MB.",
                HttpStatus.PAYLOAD_TOO_LARGE.value()
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleGeneralException(Exception ex) {
        log.error("Unhandled internal server error: {}", ex.getMessage(), ex);
        ApiErrorResponseDto error = new ApiErrorResponseDto(
                "INTERNAL_SERVER_ERROR",
                "An unexpected server error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
