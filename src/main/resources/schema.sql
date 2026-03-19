-- ============================================================
-- MCP Task Server — Database Initialization Script
-- Run this once against your PostgreSQL instance
-- ============================================================

-- Create database (run as superuser if needed)
-- CREATE DATABASE taskdb;

-- Connect to taskdb, then run the rest:

CREATE TABLE IF NOT EXISTS tasks (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255)    NOT NULL,
    description TEXT,
    status      VARCHAR(50)     NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(50)              DEFAULT 'MEDIUM',
    due_date    DATE,
    assigned_to VARCHAR(100),
    category    VARCHAR(100),
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP                DEFAULT NOW()
);

-- Index for common query patterns
CREATE INDEX IF NOT EXISTS idx_tasks_status   ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks (priority);
CREATE INDEX IF NOT EXISTS idx_tasks_category ON tasks (category);

-- Verify
SELECT 'tasks table ready' AS message;
