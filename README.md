# MCP Task Server

A Spring Boot REST API backed by PostgreSQL, containerized with Podman — with a Python bulk-load agent, live browser dashboard, and interactive Swagger docs.

---

## Overview

**MCP Task Server** exposes four REST endpoints for task management and ships with everything you need to run it locally: a Podman-based container setup, a Python agent that bulk-inserts 1000 tasks, a browser-based live dashboard, and Swagger UI for interactive API exploration.

---

## Prerequisites

- Podman installed and configured
- Python 3 with `pip` available
- A terminal with `curl`
- Ports **8081** and **5432** free on localhost

---

## Quick-Start Steps

### Step 1 — Enter the Project

```bash
cd mcp-task-server
```

---

### Step 2 — Start Podman Machine

```bash
podman machine start
```

Skip if already running. Check with `podman machine list`.

---

### Step 3 — Create the Pod & Start PostgreSQL

```bash
podman pod create --name mcp-pod -p 8081:8081 -p 5432:5432

podman run -d --pod mcp-pod --name mcp-postgres \
  -e POSTGRES_DB=taskdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:15-alpine

# Wait for postgres to initialise
sleep 10
```

---

### Step 4 — Build & Start the MCP Server

```bash
podman build -t mcp-task-server .

podman run -d --pod mcp-pod --name mcp-app \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/taskdb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  mcp-task-server
```

---

### Step 5 — Verify the Server is Running

```bash
podman logs mcp-app | grep "Started"
```

Expected output:

```
Started McpTaskServerApplication in 4.3 seconds
```

---

### Step 6 — Quick API Test

```bash
curl http://localhost:8081/mcp/help
```

You should receive a JSON response containing `"status": "success"`.

---

### Step 7 — Run the Python Agent *(inserts 1 000 tasks)*

```bash
# First time only
python3 -m venv venv
source venv/bin/activate
pip install requests

# Run the agent
python3 agent_test.py
```
![alt text](image.png)

You should see 1 000 tasks inserted with `✓ SUCCESS` at the end.

---

### Step 8 — Open the Browser UI

```bash
python3 -m http.server 3000
```

Open → [http://localhost:3000/mcp-agent.html](http://localhost:3000/mcp-agent.html)

Click **▶ Run AI Agent** to watch tasks insert live with charts.

---

### Step 9 — Open Swagger UI

Open → [http://localhost:8081/swagger-ui](http://localhost:8081/swagger-ui)

Test all 4 endpoints interactively in the browser.

---

### Step 10 — Verify Data in the Database

```bash
podman exec -it mcp-postgres psql -U postgres -d taskdb \
  -c "SELECT status, COUNT(*) FROM tasks GROUP BY status;"
```

You should see all 5 statuses with counts adding up to 1 000 or more.

---

## API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/mcp/tasks` | Create a new task |
| `GET` | `/mcp/tasks` | List all tasks (with filters) |
| `PUT` | `/mcp/tasks/{id}` | Update task status |
| `DELETE` | `/mcp/tasks/{id}` | Remove a task |

---

## Verification Checklist

| # | Command | Expected Result |
|---|---------|-----------------|
| 1 | `podman ps` | 2 containers running |
| 2 | `curl http://localhost:8081/mcp/help` | `status: success` |
| 3 | `python3 agent_test.py` | 1 000 tasks inserted |
| 4 | `http://localhost:8081/swagger-ui` | 4 endpoints visible |
| 5 | `psql COUNT(*)` | All 5 statuses summing to 1 000+ |

---

## Troubleshooting

### Pod or Container Fails to Start

- Run `podman pod rm -f mcp-pod` to tear down, then repeat Steps 3–4.
- Confirm ports 8081 and 5432 are not already in use.

### Spring Boot Fails to Connect to PostgreSQL

- Ensure the `sleep 10` in Step 3 was honoured before running Step 4.
- `podman logs mcp-postgres` should show: `database system is ready to accept connections`.

### Python Agent Errors

- Verify the virtual environment is active (`source venv/bin/activate`) and `requests` is installed.
- Confirm the server passed Step 6 before running the agent.

---

## Architecture

The system runs as a single Podman pod exposing two ports:

- **Port 8081** — Spring Boot REST API (`mcp-app` container)
- **Port 5432** — PostgreSQL 15 (`mcp-postgres` container)

Both containers share the pod's network namespace, so the application connects to PostgreSQL via `localhost` inside the pod. The Python agent and browser UI run on the host and communicate with the API over port 8081.

  
```

---

*MCP Task Server — Setup & Operations Guide*
