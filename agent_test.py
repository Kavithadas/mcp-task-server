#!/usr/bin/env python3
"""
MCP Task Server — AI Agent Simulation Script
Simulates what an AI agent (Claude / GPT-4o) does:
  1. Call /mcp/help to discover tools
  2. Call /mcp/schema/tasks to inspect the schema
  3. Generate 1000 diverse task objects
  4. POST them to /mcp/tasks
  5. Validate with /mcp/tasks/summary
"""

import json
import random
import requests
from datetime import date, timedelta

BASE_URL = "http://localhost:8081"

# ── Sample data pools ─────────────────────────────────────────────────────────

TITLES = [
    "Implement login page", "Fix null pointer exception", "Write unit tests",
    "Update API documentation", "Deploy to staging", "Review pull request",
    "Refactor database queries", "Add pagination support", "Set up CI/CD pipeline",
    "Optimize image loading", "Create onboarding flow", "Fix broken links",
    "Migrate to PostgreSQL", "Add dark mode", "Write integration tests",
    "Update dependencies", "Code review for sprint", "Fix memory leak",
    "Implement caching layer", "Add search functionality", "Create dashboard widget",
    "Resolve merge conflicts", "Setup monitoring alerts", "Write release notes",
    "Improve error messages", "Add input validation", "Create backup strategy",
    "Perform security audit", "Implement OAuth2", "Add rate limiting",
    "Setup log aggregation", "Create API gateway", "Implement retry logic",
    "Design database schema", "Add feature flags", "Update test coverage",
    "Fix CORS issues", "Implement file upload", "Create REST endpoint",
    "Add audit logging", "Setup load balancer", "Performance profiling",
]

DESCRIPTIONS = [
    "This task requires careful implementation following existing patterns.",
    "High priority item from the last sprint retrospective.",
    "Blocked by the DevOps team — needs infrastructure change first.",
    "Related to customer feedback from Q3 survey.",
    "Part of the tech debt reduction initiative.",
    "Required for the upcoming product launch.",
    "Dependency for three other open tasks.",
    "Estimated effort: 3 story points.",
    "Needs UX review before implementation starts.",
    "Regression found in latest build.",
    None,  # Some tasks have no description
    None,
    None,
]

CATEGORIES = [
    "Backend", "Frontend", "DevOps", "QA", "Security",
    "Database", "Documentation", "Design", "Infrastructure", "Bugfix",
]

ASSIGNEES = [
    "alice", "bob", "carol", "dave", "eve",
    "frank", "grace", "henry", "iris", "jack",
    None,  # Some tasks are unassigned
    None,
]

STATUSES = ["TODO", "IN_PROGRESS", "DONE", "CANCELLED", "BLOCKED"]
STATUS_WEIGHTS = [0.35, 0.25, 0.25, 0.08, 0.07]

PRIORITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
PRIORITY_WEIGHTS = [0.20, 0.40, 0.30, 0.10]


def random_due_date():
    """Return a random ISO date in the next 0–180 days, or None."""
    if random.random() < 0.15:
        return None
    delta = random.randint(0, 180)
    d = date.today() + timedelta(days=delta)
    return d.isoformat()


def generate_task(index: int) -> dict:
    title_base = random.choice(TITLES)
    task = {
        "title": f"{title_base} #{index}",
        "description": random.choice(DESCRIPTIONS),
        "status": random.choices(STATUSES, STATUS_WEIGHTS)[0],
        "priority": random.choices(PRIORITIES, PRIORITY_WEIGHTS)[0],
        "dueDate": random_due_date(),
        "assignedTo": random.choice(ASSIGNEES),
        "category": random.choice(CATEGORIES),
    }
    # Remove None values (they're optional; server defaults them)
    return {k: v for k, v in task.items() if v is not None}


# ── Step helpers ──────────────────────────────────────────────────────────────

def print_section(title: str):
    print(f"\n{'='*60}")
    print(f"  {title}")
    print('='*60)


def step1_discover_tools():
    print_section("STEP 1 — Discover tools via /mcp/help")
    r = requests.get(f"{BASE_URL}/mcp/help")
    r.raise_for_status()
    data = r.json()
    tools = data["data"]["tools"]
    print(f"Server : {data['data']['serverName']}")
    print(f"Version: {data['data']['version']}")
    print(f"Tools  : {[t['name'] for t in tools]}")


def step2_inspect_schema():
    print_section("STEP 2 — Inspect schema via /mcp/schema/tasks")
    r = requests.get(f"{BASE_URL}/mcp/schema/tasks")
    r.raise_for_status()
    schema = r.json()["data"]["schema"]
    required = schema.get("required", [])
    print(f"Required fields : {required}")
    props = list(schema["properties"].keys())
    print(f"All fields      : {props}")
    for field in ["status", "priority"]:
        enums = schema["properties"][field].get("enum", [])
        print(f"  {field} enum    : {enums}")


def step3_generate_and_insert():
    print_section("STEP 3 — Generate and insert 1000 tasks via /mcp/tasks")
    tasks = [generate_task(i + 1) for i in range(1000)]

    # Show sample
    print(f"Generated {len(tasks)} tasks. Sample (first 2):")
    for t in tasks[:2]:
        print(f"  {json.dumps(t)}")

    print("\nPosting to /mcp/tasks …")
    r = requests.post(
        f"{BASE_URL}/mcp/tasks",
        json=tasks,
        headers={"Content-Type": "application/json"},
        timeout=60,
    )
    r.raise_for_status()
    result = r.json()["data"]
    print(f"  Requested      : {result['requested']}")
    print(f"  Inserted       : {result['inserted']}")
    print(f"  Failed         : {result['failed']}")
    print(f"  Total in DB    : {result['totalTasksInDb']}")
    print(f"  Message        : {result['message']}")
    return result["inserted"]


def step4_validate():
    print_section("STEP 4 — Validate via /mcp/tasks/summary")
    r = requests.get(f"{BASE_URL}/mcp/tasks/summary")
    r.raise_for_status()
    summary = r.json()["data"]

    print(f"Total tasks in DB : {summary['totalTasks']}")

    print("\nBy status:")
    for status, count in summary["byStatus"].items():
        bar = "█" * (count // 10)
        print(f"  {status:<12} {count:>5}  {bar}")

    print("\nBy priority:")
    for priority, count in summary["byPriority"].items():
        bar = "█" * (count // 10)
        print(f"  {priority:<10} {count:>5}  {bar}")

    print("\nBy category (top 5):")
    for i, (cat, count) in enumerate(summary["byCategory"].items()):
        if i >= 5:
            break
        print(f"  {cat:<15} {count:>5}")

    print("\nBy assignee (top 5):")
    for i, (person, count) in enumerate(summary["byAssignee"].items()):
        if i >= 5:
            break
        print(f"  {person:<12} {count:>5}")


# ── Main ──────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    print("MCP AI Agent Simulation — inserting 1000 tasks")
    print(f"Target server: {BASE_URL}")

    try:
        step1_discover_tools()
        step2_inspect_schema()
        inserted = step3_generate_and_insert()
        step4_validate()

        print("\n" + "="*60)
        print(f"  ✓ SUCCESS — {inserted} tasks inserted and verified")
        print("="*60 + "\n")

    except requests.exceptions.ConnectionError:
        print(f"\n✗ Cannot connect to {BASE_URL}. Is the MCP server running?")
    except requests.exceptions.HTTPError as e:
        print(f"\n✗ HTTP error: {e.response.status_code} — {e.response.text}")
    except Exception as e:
        print(f"\n✗ Unexpected error: {e}")
