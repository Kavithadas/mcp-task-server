package com.mcp.taskserver.controller;

import com.mcp.taskserver.dto.McpDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${mcp.server.version:2025-06-18}")
    private String mcpVersion;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<McpDto.McpResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).collect(Collectors.joining("; "));
        log.warn("[MCP] Validation error: {}", errors);
        return ResponseEntity.badRequest().body(
                McpDto.McpResponse.<Object>builder().status("error").mcpVersion(mcpVersion)
                        .error("Validation failed: " + errors).build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<McpDto.McpResponse<Object>> handleBadJson(HttpMessageNotReadableException ex) {
        log.warn("[MCP] Bad JSON: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                McpDto.McpResponse.<Object>builder().status("error").mcpVersion(mcpVersion)
                        .error("Invalid JSON payload. Body must be a JSON array of task objects.").build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<McpDto.McpResponse<Object>> handleGeneral(Exception ex) {
        log.error("[MCP] Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                McpDto.McpResponse.<Object>builder().status("error").mcpVersion(mcpVersion)
                        .error("Internal server error: " + ex.getMessage()).build());
    }
}
