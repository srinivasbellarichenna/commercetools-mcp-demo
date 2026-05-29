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

To register this MCP server globally with the Gemini CLI, open a terminal and choose one of the following methods depending on how you are running the server.

### Option A: Connecting to the Local Python Environment

Use this if you manually installed the Python virtual environment.
> **Important**: Replace `/ABSOLUTE/PATH/TO/...` with the actual path to your repository clone!

```bash
gemini mcp add foundational-mcp \
  /ABSOLUTE/PATH/TO/commercetools-mcp-demo/phase-1/foundational-mcp-server/venv/bin/python \
  -- /ABSOLUTE/PATH/TO/commercetools-mcp-demo/phase-1/foundational-mcp-server/main.py \
  -e API_BASE_URL="http://localhost:8085/api"
```

### Option B: Connecting to the Docker Container (Recommended)

If you have spun up the entire foundation layer using `docker compose up -d`, you can attach Gemini directly to the running container without needing to configure Python on your host machine!

```bash
gemini mcp add foundational-mcp docker -- exec -i foundational-mcp-server python main.py
```

Once registered, Gemini will now have access to tools like `search_products`, `initialize_cart`, and `create_stripe_checkout`.

---

## 🦅 2. Claude Desktop Integration

To configure Claude Desktop to use the foundational server, you must edit its global configuration file.

- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

Add the `foundational-mcp` definition to your `mcpServers` block. 

### Option A: Connecting to the Local Python Environment

Use this if you manually installed the Python virtual environment.
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

### Option B: Connecting to the Docker Container (Recommended)

If you have already spun up the entire foundation layer using `docker compose up -d`, the MCP server is actively running inside a container. You can attach Claude Desktop directly to this running container without needing to configure Python on your host machine!

```json
{
  "mcpServers": {
    "foundational-mcp": {
      "command": "docker",
      "args": [
        "exec",
        "-i",
        "foundational-mcp-server",
        "python",
        "main.py"
      ]
    }
  }
}
```

After saving the file, restart the Claude Desktop application. The new tools will appear as available integrations.

---

## 🎯 Example Prompts to Try

Once connected, your AI assistant essentially has the keys to your e-commerce storefront. Try asking it to perform multi-step agentic workflows:

**Catalog & Discovery:**
- *"What minimalist chairs do we have in the catalog? Give me their specific dimensions and prices."*
- *"Can you search for any products matching the keyword 'pink'?"*

**Cart & Checkout Flow:**
- *"I'd like to buy the pink chair. Can you initialize a new cart, add it, set the shipping method to standard delivery, and generate a secure Stripe checkout link for me?"*
- *"What is the current subtotal and grand total of my active cart?"*

**Customer Management:**
- *"Look up the customer profile for test@example.com and tell me if they have any past orders."*
- *"Update the shipping address on my active cart to 123 Alexanderplatz, Berlin, 10178, DE."*
