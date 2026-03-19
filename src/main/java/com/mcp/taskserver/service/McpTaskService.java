package com.mcp.taskserver.service;

import com.mcp.taskserver.dto.McpDto;
import com.mcp.taskserver.model.Task;
import com.mcp.taskserver.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class McpTaskService {

    private static final Logger log = LoggerFactory.getLogger(McpTaskService.class);

    private final TaskRepository taskRepository;

    @Value("${mcp.server.version:2025-06-18}")
    private String mcpVersion;

    @Value("${mcp.server.name:Task Management MCP Server}")
    private String serverName;

    public McpTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // ─── mcp-schema-tasks ────────────────────────────────────────────────────
    public McpDto.McpResponse<McpDto.SchemaResponse> getTaskSchema() {
        log.info("[MCP] mcp-schema-tasks called");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "integer", "description", "Auto-generated primary key. Do NOT include when inserting.", "readOnly", true));
        properties.put("title", Map.of("type", "string", "maxLength", 255, "description", "Short title for the task (required)"));
        properties.put("description", Map.of("type", "string", "description", "Detailed description (optional)"));
        properties.put("status", Map.of("type", "string", "enum", List.of("TODO", "IN_PROGRESS", "DONE", "CANCELLED", "BLOCKED"), "description", "Current status (required)"));
        properties.put("priority", Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH", "CRITICAL"), "description", "Priority level (optional, defaults to MEDIUM)"));
        properties.put("dueDate", Map.of("type", "string", "format", "date", "example", "2025-12-31", "description", "Due date yyyy-MM-dd (optional)"));
        properties.put("assignedTo", Map.of("type", "string", "maxLength", 100, "description", "Assignee name (optional)"));
        properties.put("category", Map.of("type", "string", "maxLength", 100, "description", "Category for grouping (optional)"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        schema.put("title", "Task");
        schema.put("type", "object");
        schema.put("required", List.of("title", "status"));
        schema.put("properties", properties);
        schema.put("additionalProperties", false);

        McpDto.SchemaResponse schemaResponse = new McpDto.SchemaResponse("mcp-schema-tasks", mcpVersion, schema);

        return McpDto.McpResponse.<McpDto.SchemaResponse>builder()
                .tool("mcp-schema-tasks").status("success").mcpVersion(mcpVersion).data(schemaResponse).build();
    }

    // ─── mcp-tasks ───────────────────────────────────────────────────────────
    @Transactional
    public McpDto.McpResponse<McpDto.BulkInsertResponse> insertTasks(List<McpDto.TaskRequest> taskRequests) {
        log.info("[MCP] mcp-tasks called — inserting {} tasks", taskRequests.size());

        int inserted = 0, failed = 0;
        List<Task> batch = new ArrayList<>();

        for (McpDto.TaskRequest req : taskRequests) {
            try {
                Task task = Task.builder()
                        .title(req.getTitle())
                        .description(req.getDescription())
                        .status(req.getStatus())
                        .priority(req.getPriority() != null ? req.getPriority() : Task.Priority.MEDIUM)
                        .dueDate(req.getDueDate())
                        .assignedTo(req.getAssignedTo())
                        .category(req.getCategory())
                        .build();
                batch.add(task);
                inserted++;
            } catch (Exception e) {
                log.warn("[MCP] Failed to build task: {}", e.getMessage());
                failed++;
            }
        }

        taskRepository.saveAll(batch);
        long total = taskRepository.count();
        log.info("[MCP] Batch insert complete — inserted={}, failed={}, totalInDb={}", inserted, failed, total);

        McpDto.BulkInsertResponse result = new McpDto.BulkInsertResponse(
                taskRequests.size(), inserted, failed,
                String.format("Successfully inserted %d tasks. %d failed.", inserted, failed), total);

        return McpDto.McpResponse.<McpDto.BulkInsertResponse>builder()
                .tool("mcp-tasks").status("success").mcpVersion(mcpVersion).data(result).build();
    }

    // ─── mcp-tasks-summary ───────────────────────────────────────────────────
    public McpDto.McpResponse<McpDto.TaskSummaryResponse> getSummary() {
        log.info("[MCP] mcp-tasks-summary called");

        long total = taskRepository.count();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : taskRepository.countByStatusGrouped())
            byStatus.put(row[0].toString(), (Long) row[1]);

        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Object[] row : taskRepository.countByPriorityGrouped())
            byPriority.put(row[0].toString(), (Long) row[1]);

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Object[] row : taskRepository.countByCategoryGrouped())
            byCategory.put(row[0] != null ? row[0].toString() : "Uncategorized", (Long) row[1]);

        Map<String, Long> byAssignee = new LinkedHashMap<>();
        taskRepository.countByAssigneeGrouped().stream().limit(10)
                .forEach(row -> byAssignee.put(row[0].toString(), (Long) row[1]));

        McpDto.TaskSummaryResponse summary = new McpDto.TaskSummaryResponse(total, byStatus, byPriority, byCategory, byAssignee);

        return McpDto.McpResponse.<McpDto.TaskSummaryResponse>builder()
                .tool("mcp-tasks-summary").status("success").mcpVersion(mcpVersion).data(summary).build();
    }

    // ─── mcp-help ─────────────────────────────────────────────────────────────
    public McpDto.McpResponse<McpDto.McpHelpResponse> getHelp() {
        log.info("[MCP] mcp-help called");

        List<McpDto.ToolInfo> tools = List.of(
                new McpDto.ToolInfo("mcp-schema-tasks", "/mcp/schema/tasks", "GET",
                        "Returns the JSON Schema for the tasks table. Call this first to understand required fields and valid enum values.",
                        "none", "JSON Schema (draft 2020-12) describing the Task object"),
                new McpDto.ToolInfo("mcp-tasks", "/mcp/tasks", "POST",
                        "Accepts a JSON array of Task objects and bulk-inserts them. Required: title, status. Optional: description, priority, dueDate, assignedTo, category.",
                        "Array<TaskRequest>", "BulkInsertResponse: {requested, inserted, failed, message, totalTasksInDb}"),
                new McpDto.ToolInfo("mcp-tasks-summary", "/mcp/tasks/summary", "GET",
                        "Returns aggregate statistics: total count, counts by status, priority, category, and top assignees.",
                        "none", "TaskSummaryResponse: {totalTasks, byStatus, byPriority, byCategory, byAssignee}"),
                new McpDto.ToolInfo("mcp-help", "/mcp/help", "GET",
                        "Returns this help document listing all available MCP tools.",
                        "none", "McpHelpResponse listing all tools")
        );

        McpDto.McpHelpResponse help = new McpDto.McpHelpResponse(serverName, mcpVersion,
                "MCP Server for AI-powered task data injection. Use mcp-schema-tasks to inspect the schema, " +
                "mcp-tasks to insert data, and mcp-tasks-summary to verify results.", tools);

        return McpDto.McpResponse.<McpDto.McpHelpResponse>builder()
                .tool("mcp-help").status("success").mcpVersion(mcpVersion).data(help).build();
    }
}
