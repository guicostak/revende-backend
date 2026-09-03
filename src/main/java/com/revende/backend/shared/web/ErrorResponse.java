package com.revende.backend.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(Instant timestamp, int status, String message, Map<String, String> fields) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status, message, null);
    }

    public static ErrorResponse ofValidation(int status, String message, Map<String, String> fields) {
        return new ErrorResponse(Instant.now(), status, message, fields);
    }
}
