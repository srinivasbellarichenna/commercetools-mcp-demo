# Federated Agents V2 Walkthrough

The Federated Agents V2 is a second-generation autonomous execution engine designed for reliable, multi-step task completion with real-time feedback. It moves away from the specialist-delegation model towards a structured **Plan-Execute-Evaluate** loop.

## Core Architecture

V2 is built as a state-aware execution engine that converts user intent into a sequence of trackable steps.

### 1. Planner (`engine/planner.py`)
The **Planner** uses an LLM to convert the user's request into a structured `Plan`. This plan contains:
- A high-level **Goal**.
- A sequence of **Steps**, each with an action, target capability, and optional conditions.

### 2. Execution Engine (`engine/execution_engine.py`)
The **Brain** of the system. It:
- Iterates through the plan steps.
- Coordinates with the **StepExecutor**.
- Emits real-time SSE events (`STEP_STARTED`, `STEP_COMPLETED`, `ERROR`, etc.).
- Manages session-based state and error recovery.

### 3. Capabilities (`capabilities/`)
Domain-specific tool wrappers that provide a clean interface for the engine:
- **ProductCapability**: Search and retrieval.
- **CartCapability**: Management of line items.
- **CheckoutCapability**: Finalizing transactions.

### 4. Agents (`agents/`)
Specialized logic for complex reasoning within steps:
- **AnalystAgent**: Handles product ranking and preference matching.
- **TransactionAgent**: Manages cart operations and order validation.

## Key Evolution from V1

| Feature | Federated Agents V1 | Federated Agents V2 |
| --- | --- | --- |
| **Model** | LLM Router + Specialist Specialists | Structured Plan + Async Engine |
| **Feedback** | Single tool response | Real-time SSE Event Stream |
| **State** | Stateless / specialist-managed | Centralized `StateManager` |
| **Resilience** | Re-run entire request | Step-level retries and error handling |

## Running the V2 Engine

### Standalone SSE Server
To run the V2 server with streaming support:

```bash
cd federated-agents-v2
# Run using the local venv
PYTHONPATH=. venv/bin/python3 run_v2_sse.py
```

### CLI Mode (Dry Run)
For testing orchestration without SSE:

```bash
cd federated-agents-v2
PYTHONPATH=. venv/bin/python3 main.py "purchase a pair of Nike shoes"
```

## Hardening & Production-Readiness

The V2 engine has been hardened for production environments:

- **Latency-Optimized Planner**: Dramatically reduced system prompt size to lower time-to-first-token, specifically tuned for local models like `qwen-4B`.
- **Extended Timeouts**: Increased LLM generation timeout to **60 seconds** to accommodate local inference latency and prevent early connection termination.
- **Data Path Hardening**: Fixed critical method mismatches in the `ExecutionEngine` and implemented **structured history tracking** (including `timestamp` and `result_count`) to ensure results are correctly written to the session state without data loss.
- **Semantic Evaluator Validation**: Transformed validation logic from a brittle structural check to an intelligent semantic assertion system. The `Evaluator` now reads raw domain properties comprehensively (e.g., checks if product `id` and `name` are missing) and autonomously invokes `RETRY` (for invalid data) or `REPLAN` (for logical failures like empty lists), ensuring pure, resilient data propagation without hard crashing.
- **State-Aware Decision Engine**: The Evaluator actively pulls context variables `retry_count`, `replan_count`, and `intent.strict_query` directly from persistent `State`. This mathematically breaks infinite feedback routing loops and guarantees finite operation.
- **Payload & Memory Protection**: Upgraded tracking to maintain distinct memory layers: `ExecutionHistory` serializes only compressed *audit traces* (small metadata summaries), while critical operations like searches use intelligent `rank_and_trim()` implementations mapping purely to distinct *working memory*, safely preserving the top semantic features securely from arbitrary truncation.
- **Precision Tracing**: Added granular logs across `StepExecutor`, `ProductCapability`, `AnalystAgent`, and `MCPProxy` to trace every search result from the MCP tool call to the state update.
- **MCP Contract Mapping Layer**: A dedicated translation boundary (`shared/mcp_contract.py`) rigidly maps orchestration-agnostic commands (e.g., `query`) to the specific backend requirements matching the MCP spec (e.g., `keyword`). The Proxy enforces these types via explicit schema validations securely preventing silent query loss.
- **MCP Input Echo Logging**: Real-time logging at the absolute MCP boundary capturing exactly what was mapped and executed. 
- **Suspicious Results Guards**: Evaluates edge cases where a structurally sound empty array is returned despite full input payloads, proactively intercepting silent API mismatch failures (`StepExecutor` flagging suspect arrays).
- **Terminal State Guarantee**: The execution engine now uses a `finally` block to ensure a **system-terminal `DONE` event** is always emitted, preventing MCP client timeouts even on fatal errors.
- **Autonomous Decision Loop**: Each step outcome is evaluated by a dedicated **Decision Engine** (CONTINUE, RETRY, REPLAN, SKIP, ABORT).
- **Heuristic Replanning**: The engine includes a recovery layer that can autonomously generate alternative plans (e.g., relaxing search filters) without immediate user intervention.
- **Distributed Tracing**: Implementation of **Trace IDs** and **Span IDs** across the autonomous loop, allowing for end-to-end observability of complex retry/replan cycles.
- **Agent Metrics**: Real-time tracking of `retry_count` and `replan_count` in the session state.
- **Strict Contracts (Pydantic & MCP Proxying)**: All core models (`Plan`, `Step`, `State`) are strictly enforced via schema validation alongside strict Backend Parameter validation before SSE transit.
- **Security**: Added **API Key Authentication** middleware to the SSE server.
- **Observability**: Implemented **Structured JSON Logging** with full tracing context.

## Verification Status

- [x] **Unit Tests**: Tracing and metric propagation verified with `unittest`.
- [x] **Decision Loop**: Autonomous recovery from search failure verified.
- [x] **Observation**: JSON logs verified for `trace_id` and `span_id` presence.

## Internal Directory Structure

```text
federated-agents-v2/
├── agents/             # Reasoning specialists (Analyst, Transaction)
├── capabilities/       # High-level tool wrappers (Business Validation)
├── engine/             # Plan execution, Evaluator, and Replanner
├── models/             # Pydantic data contracts (Decision, Plan, Step, State)
├── shared/             # Logging and Tracing utilities
├── tests/              # Automated verification (Failure Injection Scenarios)
├── main.py             # CLI entry point
└── run_v2_sse.py       # Authenticated SSE server providing the event stream
```
