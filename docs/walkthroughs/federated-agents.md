# Federated Agents V1 Walkthrough

The Federated Agents V1 is the original specialist-based implementation. It uses a central orchestrator to delegate tasks to specialist agents (`Analyst` and `Closer`).

> [!NOTE]
> For the new planned autonomous execution engine, see [Federated Agents V2](./federated-agents-v2.md).

## Architecture & Components

The agency is built using Python and the `mcp[fastmcp]` library. It can be run either via standard input (stdio) for local development with Claude Desktop, or via Server-Sent Events (SSE) for web-based integration.

### Orchestrator (`federated-agents/orchestrator.py`)

The [Orchestrator](../../federated-agents/orchestrator.py) is the primary contact point. It exposes the `commerce_agent_request_v2` tool.

* **Triage**: Analyzes the user's prompt to determine if they are in "discovery" mode or "transaction" mode.
* **Delegation**: Forwards the request to the appropriate specialist agent.

### Specialists (`federated-agents/specialists/`)

Specialized agents that embody specific domain expertise:

1. **[Analyst](../../federated-agents/specialists/analyst.py)**: Focuses on product discovery, searching the catalog, and recommending items based on user preferences.
2. **[Closer](../../federated-agents/specialists/closer.py)**: Handles the bottom-of-the-funnel activities, such as managing the cart, applying payments, and finalizing orders.

### Shared Logic & Connectivity

* **[MCP Proxy](../../federated-agents/shared/mcp_proxy.py)**: A utility that allows the Python specialists to call tools on other MCP servers (like the `foundational-mcp-server`), enabling a truly federated system.
* **[SSE Runner](../../federated-agents/run_sse.py)**: Wraps the FastMCP app in a FastAPI server, providing endpoint for real-time communication via Server-Sent Events.

## Key Features

* **Federated Intelligence**: By splitting logic into specialists, the system can be more robust and easier to maintain.
* **Multi-Modal Support**: Designed to work with both direct LLM interactions and through a custom frontend UI.
* **Real-Time Feedback**: Through SSE, the agency can provide immediate status updates to the user as agents perform their work.

## Running the Agency

To start the agency in SSE mode (standard for this project's Docker setup):

```bash
cd federated-agents
# Ensure dependencies are installed
pip install -r requirements.txt
# Run the SSE server
python run_sse.py
```

By default, the SSE server runs on port 8000 and the MCP endpoint is at `/mcp/`.
