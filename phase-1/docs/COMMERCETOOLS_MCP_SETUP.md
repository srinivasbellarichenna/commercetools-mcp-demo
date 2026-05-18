# Commercetools MCP Integration Instructions

This document provides the necessary instructions to set up the commercetools Model Context Protocol (MCP) servers locally for your project.

commercetools provides two primary MCP servers depending on your needs:

## 1. Developer MCP
The Developer MCP provides a stateless service to access commercetools documentation, GraphQL schemata, and API reference content. No authentication is required.

**Endpoint:** `https://docs.commercetools.com/apis/mcp`

**Execution Command:**
```bash
npx -y mcp-remote --url https://docs.commercetools.com/apis/mcp
```

**Claude Desktop Configuration (`claude_desktop_config.json`):**
```json
"mcpServers": {
  "commercetools-dev-content": {
    "command": "npx",
    "args": ["-y", "mcp-remote", "--url", "https://docs.commercetools.com/apis/mcp"]
  }
}
```

**Gemini CLI Configuration:**
Run the following command to register the server with your local `gemini-cli` environment:
```bash
gemini mcp add commercetools-dev-content npx -- -y mcp-remote --url https://docs.commercetools.com/apis/mcp
```

## 2. Commerce MCP
The Commerce MCP (`@commercetools/commerce-mcp`) allows AI agents to interact directly with your Composable Commerce Project (e.g., managing products, carts, orders).

**Execution Command:**
```bash
npx -y @commercetools/commerce-mcp --tools=all
```

**Required Environment Variables:**
To use this server, you must provide your commercetools API credentials:
- `PROJECT_KEY`: Your commercetools Project key.
- `AUTH_URL`: Authorization service URL (e.g., `https://auth.europe-west1.gcp.commercetools.com`).
- `API_URL`: API service URL (e.g., `https://api.europe-west1.gcp.commercetools.com`).
- `AUTH_TYPE`: Must be set to `client_credentials`.
- `CLIENT_ID`: Your API Client ID.
- `CLIENT_SECRET`: Your API Client Secret.
- `TOOLS`: Set to `all` to enable all available tools.
- `IS_ADMIN`: Set to `true` if `TOOLS` is set to `all`.

**Claude Desktop Configuration (`claude_desktop_config.json`):**
```json
"mcpServers": {
  "commercetools-commerce": {
    "command": "npx",
    "args": ["-y", "@commercetools/commerce-mcp", "--tools=all"],
    "env": {
      "PROJECT_KEY": "mcp-demo",
      "AUTH_URL": "https://auth.eu-central-1.aws.commercetools.com",
      "API_URL": "https://api.eu-central-1.aws.commercetools.com",
      "AUTH_TYPE": "client_credentials",
      "CLIENT_ID": "OKZUKU1YHWR9sJGvIfENuBhl",
      "CLIENT_SECRET": "7PgSQV8vQhGC9hU2wEzJrwGsSVItaANR",
      "TOOLS": "all",
      "IS_ADMIN": "true"
    }
  }
}
```

**Gemini CLI Configuration:**
Run the following command to register the server with your local `gemini-cli` environment:
```bash
gemini mcp add commercetools-commerce npx -- -y @commercetools/commerce-mcp --tools=all -e PROJECT_KEY="mcp-demo" -e AUTH_URL="https://auth.eu-central-1.aws.commercetools.com" -e API_URL="https://api.eu-central-1.aws.commercetools.com" -e AUTH_TYPE="client_credentials" -e CLIENT_ID="OKZUKU1YHWR9sJGvIfENuBhl" -e CLIENT_SECRET="7PgSQV8vQhGC9hU2wEzJrwGsSVItaANR" -e TOOLS="all" -e IS_ADMIN="true"
```

## Next Steps
1. Determine which MCP server you need (documentation vs data operations).
2. For the Commerce MCP, generate API Client credentials in your commercetools Merchant Center.
3. Apply the JSON configuration snippets to your AI Agent or standard MCP client (e.g., Claude Desktop, Cursor, or your custom implementation).
