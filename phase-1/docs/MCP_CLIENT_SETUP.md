# Foundational MCP Server Client Setup Guide

This guide walks you through connecting your preferred AI assistant (Claude Desktop, Gemini CLI, etc.) directly to the **Foundational MCP Server** provided in this repository.

By connecting your AI agent to this custom server, the agent gains access to our specific Composable Agentic Commerce tools—including catalog search, cart management, and secure Stripe checkout generation—all orchestrated through our Java API Gateway.

---

## 🛠️ Prerequisites

Before connecting your AI client, you must ensure the foundational MCP server is set up locally:

1. **Start the Java API Gateway**: Ensure your Backend Microservices are running (usually via Docker Compose as described in the main README) on `http://localhost:8085`.
2. **Prepare the Python Environment**: 
   ```bash
   cd phase-1/foundational-mcp-server
   python3 -m venv venv
   source venv/bin/activate
   pip install -r requirements.txt
   ```
   *(Note the absolute path to this `venv` directory on your machine, as you will need it for the configurations below!)*

---

## 🤖 1. Gemini CLI Integration

To register this MCP server globally with the Gemini CLI, open a terminal and run the following command. 

> **Important**: Replace `/ABSOLUTE/PATH/TO/...` with the actual path to your repository clone!

```bash
gemini mcp add foundational-mcp \
  /ABSOLUTE/PATH/TO/commercetools-mcp-demo/phase-1/foundational-mcp-server/venv/bin/python \
  -- /ABSOLUTE/PATH/TO/commercetools-mcp-demo/phase-1/foundational-mcp-server/main.py \
  -e API_BASE_URL="http://localhost:8085/api"
```

Once registered, Gemini will now have access to tools like `search_products`, `initialize_cart`, and `create_stripe_checkout`.

---

## 🦅 2. Claude Desktop Integration

To configure Claude Desktop to use the foundational server, you must edit its global configuration file.

- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

Add the `foundational-mcp` definition to your `mcpServers` block. 

> **Important**: Replace `/ABSOLUTE/PATH/TO/...` with the actual path to your repository clone!

```json
{
  "mcpServers": {
    "foundational-mcp": {
      "command": "/ABSOLUTE/PATH/TO/commercetools-mcp-demo/phase-1/foundational-mcp-server/venv/bin/python",
      "args": [
        "/ABSOLUTE/PATH/TO/commercetools-mcp-demo/phase-1/foundational-mcp-server/main.py"
      ],
      "env": {
        "API_BASE_URL": "http://localhost:8085/api"
      }
    }
  }
}
```

After saving the file, restart the Claude Desktop application. The new tools will appear as available integrations.

---

## 🎯 Example Prompts to Try

Once connected, try asking your AI assistant:
- *"What minimalist chairs do we have in the catalog?"*
- *"Can you add the pink chair to a new cart and generate a checkout link for me?"*
- *"Look up the customer profile for test@example.com."*
