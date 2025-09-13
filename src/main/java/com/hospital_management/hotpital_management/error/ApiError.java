package com.hospital_management.hotpital_management.error;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record ApiError(LocalDateTime timestamp, String message, int status, String error, String path) {
    public static ApiError of(String message, HttpStatus status, String path) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .path(path)
                .build();
    }
}
