# KESTREL MCP Integration Guide

This document provides instructions for integrating the KESTREL Model Context Protocol (MCP) server into your AI orchestration environment.

## 1. Architectural Overview
The KESTREL MCP Server provides a bridge between AI agents and the Commercetools microservice ecosystem. It utilizes the **SSE (Server-Sent Events)** transport to maintain a stable connection within the Docker environment.

- **Status**: Operational
- **Transport**: SSE
- **Endpoint**: `http://localhost:8087/sse`
- **Internal Network**: `mcp-server:8087`

## 2. Integration with LM Studio

LM Studio (version 0.3.17+) acts as an MCP Host. To establish the connection:

1.  **Open Configuration**: In LM Studio, navigate to the **Program** tab (right sidebar) -> **Install** -> **Edit mcp.json**.
2.  **Add Connectivity**: Append the following entry to the `mcpServers` object:
    ```json
    "kestrel-mcp": {
      "url": "http://localhost:8087/sse"
    }
    ```
3.  **Deploy Model**: Load a model with tool-calling capabilities (e.g., **Llama 3.1**).
4.  **Activate Tools**: Ensure the `kestrel-mcp` toggle is enabled in the right-hand menu.

> [!TIP]
> **Llama 3.1 Optimization**: For the best performance with Llama 3.1 Instruct, ensure your system prompt explicitly forbids ID hallucination and enforces data discovery via `get_collection`.

## 3. Integration with Claude Desktop

To empower your Claude Desktop experience with AI agency:

1.  **Locate Config**: Open `~/Library/Application Support/Claude/claude_desktop_config.json`.
2.  **Register Server**: Add the KESTREL bridge to the `mcpServers` section:
    ```json
    "mcpServers": {
      "kestrel-mcp": {
        "url": "http://localhost:8087/sse"
      }
    }
    ```

## 4. Available E-commerce Tools

Once connected, your AI agent gains access to the following product and customer management tools:

- `get_collection`: List the products currently in the catalog.
- `get_piece_detail`: View the detailed specifications of a specific product.
- `initialize_bag`: Create a new shopping cart for a specific territory.
- `commit_to_bag`: Add a product to the shopping cart.
- `get_patron_registry`: Retrieve records and history for a specific customer.
- `refine_registry_record`: Update a customer's details in the system.

## 5. Deployment Management

The server is managed via Docker. To restart or rebuild the bridge:

```bash
cd commercetools-wrapper
docker compose up -d --build mcp-server
```

## 6. Cloud Connectivity & Tunneling

While local hosts (LM Studio, Claude Desktop) connect via `localhost`, you can expose the KESTREL bridge to remote cloud LLMs using a secure tunnel like **ngrok**:

1.  **Expose the Port**:
    ```bash
    ngrok http 8087
    ```
2.  **Retrieve Public URL**: Copy the resulting `https://...` URL provided by ngrok.
3.  **Configure Cloud Client**: Use the public URL as your SSE endpoint:
    ```json
    "kestrel-mcp": {
      "url": "https://your-ngrok-url.ngrok-free.app/sse"
    }
    ```

> [!CAUTION]
> Exposing your local registry to the public internet involves security risks. Ensure you use ngrok's authentication features (`--auth`) or a VPN when operating in production environments.
