# 🤖 Federated Agents

This sub-project implements a **Decentralized Multi-Agent System** for Composable Commerce.

## 🏗️ Architecture

- **`orchestrator.py`**: The Strategist. Triages user input and routes to specialists.
- **`specialists/`**: Contains specialized agents (Analyst, Closer).
- **`shared/mcp_proxy.py`**: Interface to the stable commerce action layer (MCP tools).

## 🚀 Getting Started

1. Navigate to this directory: `cd federated-agents`
2. Install dependencies: `pip install -r requirements.txt`
3. Run the orchestrator: `python orchestrator.py`

## 🔗 How it Works

Agents communicate via the **MCP Proxy** to execute commerce actions. State is managed by the Orchestrator and passed to specialists during delegation, ensuring a seamless multi-agent experience without modifying the core MCP server.
