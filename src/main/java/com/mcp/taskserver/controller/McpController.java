package com.mcp.taskserver.controller;

import com.mcp.taskserver.dto.McpDto;
import com.mcp.taskserver.service.McpTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mcp")
@Tag(name = "MCP Tools", description = "Model Context Protocol endpoints for AI agent interaction")
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);
    private final McpTaskService mcpTaskService;

    public McpController(McpTaskService mcpTaskService) {
        this.mcpTaskService = mcpTaskService;
    }

    @GetMapping("/schema/tasks")
    @Operation(summary = "mcp-schema-tasks", description = "Returns JSON Schema for the tasks table.")
    public ResponseEntity<McpDto.McpResponse<McpDto.SchemaResponse>> getTaskSchema() {
        return ResponseEntity.ok(mcpTaskService.getTaskSchema());
    }

    @PostMapping("/tasks")
    @Operation(summary = "mcp-tasks", description = "Bulk-inserts a JSON array of task objects into the database.")
    public ResponseEntity<McpDto.McpResponse<McpDto.BulkInsertResponse>> insertTasks(
            @RequestBody List<McpDto.TaskRequest> taskRequests) {
        if (taskRequests == null || taskRequests.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    McpDto.McpResponse.<McpDto.BulkInsertResponse>builder()
                            .tool("mcp-tasks").status("error")
                            .error("Request body must be a non-empty JSON array of task objects.").build());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(mcpTaskService.insertTasks(taskRequests));
    }

    @GetMapping("/tasks/summary")
    @Operation(summary = "mcp-tasks-summary", description = "Returns aggregate statistics about tasks in the database.")
    public ResponseEntity<McpDto.McpResponse<McpDto.TaskSummaryResponse>> getSummary() {
        return ResponseEntity.ok(mcpTaskService.getSummary());
    }

    @GetMapping("/help")
    @Operation(summary = "mcp-help", description = "Lists all available MCP tools with their endpoints and schemas.")
    public ResponseEntity<McpDto.McpResponse<McpDto.McpHelpResponse>> getHelp() {
        return ResponseEntity.ok(mcpTaskService.getHelp());
    }
}
