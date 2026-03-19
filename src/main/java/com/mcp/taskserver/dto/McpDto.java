package com.mcp.taskserver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mcp.taskserver.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class McpDto {

    public static class TaskRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
        @NotNull(message = "Status is required")
        private Task.TaskStatus status;
        private Task.Priority priority;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dueDate;
        private String assignedTo;
        private String category;

        public TaskRequest() {}
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String d) { this.description = d; }
        public Task.TaskStatus getStatus() { return status; }
        public void setStatus(Task.TaskStatus s) { this.status = s; }
        public Task.Priority getPriority() { return priority; }
        public void setPriority(Task.Priority p) { this.priority = p; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate d) { this.dueDate = d; }
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String a) { this.assignedTo = a; }
        public String getCategory() { return category; }
        public void setCategory(String c) { this.category = c; }
    }

    public static class BulkInsertResponse {
        private int requested, inserted, failed;
        private String message;
        private long totalTasksInDb;

        public BulkInsertResponse() {}
        public BulkInsertResponse(int requested, int inserted, int failed, String message, long totalTasksInDb) {
            this.requested = requested; this.inserted = inserted; this.failed = failed;
            this.message = message; this.totalTasksInDb = totalTasksInDb;
        }
        public int getRequested() { return requested; }
        public int getInserted() { return inserted; }
        public int getFailed() { return failed; }
        public String getMessage() { return message; }
        public long getTotalTasksInDb() { return totalTasksInDb; }
    }

    public static class TaskSummaryResponse {
        private long totalTasks;
        private Map<String, Long> byStatus, byPriority, byCategory, byAssignee;

        public TaskSummaryResponse() {}
        public TaskSummaryResponse(long totalTasks, Map<String, Long> byStatus,
                Map<String, Long> byPriority, Map<String, Long> byCategory, Map<String, Long> byAssignee) {
            this.totalTasks = totalTasks; this.byStatus = byStatus;
            this.byPriority = byPriority; this.byCategory = byCategory; this.byAssignee = byAssignee;
        }
        public long getTotalTasks() { return totalTasks; }
        public Map<String, Long> getByStatus() { return byStatus; }
        public Map<String, Long> getByPriority() { return byPriority; }
        public Map<String, Long> getByCategory() { return byCategory; }
        public Map<String, Long> getByAssignee() { return byAssignee; }
    }

    public static class ToolInfo {
        private String name, endpoint, method, description, inputSchema, outputSchema;

        public ToolInfo() {}
        public ToolInfo(String name, String endpoint, String method,
                String description, String inputSchema, String outputSchema) {
            this.name = name; this.endpoint = endpoint; this.method = method;
            this.description = description; this.inputSchema = inputSchema; this.outputSchema = outputSchema;
        }
        public String getName() { return name; }
        public String getEndpoint() { return endpoint; }
        public String getMethod() { return method; }
        public String getDescription() { return description; }
        public String getInputSchema() { return inputSchema; }
        public String getOutputSchema() { return outputSchema; }
    }

    public static class McpHelpResponse {
        private String serverName, version, description;
        private List<ToolInfo> tools;

        public McpHelpResponse() {}
        public McpHelpResponse(String serverName, String version, String description, List<ToolInfo> tools) {
            this.serverName = serverName; this.version = version;
            this.description = description; this.tools = tools;
        }
        public String getServerName() { return serverName; }
        public String getVersion() { return version; }
        public String getDescription() { return description; }
        public List<ToolInfo> getTools() { return tools; }
    }

    public static class SchemaResponse {
        private String tool, version;
        private Object schema;

        public SchemaResponse() {}
        public SchemaResponse(String tool, String version, Object schema) {
            this.tool = tool; this.version = version; this.schema = schema;
        }
        public String getTool() { return tool; }
        public String getVersion() { return version; }
        public Object getSchema() { return schema; }
    }

    public static class McpResponse<T> {
        private String tool, status, mcpVersion, error;
        private T data;

        public McpResponse() {}
        public McpResponse(String tool, String status, String mcpVersion, T data, String error) {
            this.tool = tool; this.status = status; this.mcpVersion = mcpVersion;
            this.data = data; this.error = error;
        }
        public static <T> Builder<T> builder() { return new Builder<>(); }
        public String getTool() { return tool; }
        public String getStatus() { return status; }
        public String getMcpVersion() { return mcpVersion; }
        public T getData() { return data; }
        public String getError() { return error; }

        public static class Builder<T> {
            private String tool, status, mcpVersion, error;
            private T data;
            public Builder<T> tool(String v) { this.tool = v; return this; }
            public Builder<T> status(String v) { this.status = v; return this; }
            public Builder<T> mcpVersion(String v) { this.mcpVersion = v; return this; }
            public Builder<T> data(T v) { this.data = v; return this; }
            public Builder<T> error(String v) { this.error = v; return this; }
            public McpResponse<T> build() { return new McpResponse<>(tool, status, mcpVersion, data, error); }
        }
    }
}
