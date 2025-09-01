package com.satwik.splitora.persistence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error API response")
public class ErrorResponseModel {

    @Schema(description = "HTTP status", example = "BAD_REQUEST")
    private String status;

    @Schema(description = "Error message", example = "Invalid input")
    private String message;

    @Schema(description = "Field-specific error messages", example = "{\"username\": \"Username is required\", \"email\": \"Invalid email format\"}")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> fieldErrors;

    @Schema(description = "Time at which the error occurred", example = "2025-04-20T12:34:56")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Error details")
    private ErrorDetails error;

    public ErrorResponseModel(String status, String message, LocalDateTime timestamp, ErrorDetails error) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.error = error;
    }
}
