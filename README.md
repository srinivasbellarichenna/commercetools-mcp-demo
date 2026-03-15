# KESTREL - Commercetools MCP Demo

Welcome to **KESTREL**, a demonstration of Commercetools microservices integrated with autonomous AI agents via the Model Context Protocol (MCP).

## 🏛️ Project Architecture

The project is organized into clear layers to ensure architectural clarity:

### 1. [Frontend Application](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/frontend)
The customer-facing storefront, providing a modern visual experience.

### 2. [Backend Foundation](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/commercetools-wrapper)
The core microservice ecosystem, featuring:
- **Product Service**: Inventory and discovery.
- **Cart Service**: Shopping cart and checkout management.
- **Order Service**: Finalized order records.

### 2. [Agency Layer](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/agency)
The orchestration bridges for AI integration:
- **MCP Server**: A custom Python bridge (SSE) providing e-commerce tools.
- **MCP Client**: An autonomous agent powered by local LLMs (Llama 3).

### 3. [Documentation & Guides](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/docs)
Essential blueprints for setup and integration:
- [🏺 Local Setup Guide](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/docs/LOCAL_SETUP_GUIDE.md) — **Start Here**
- [🗺️ Project Roadmap](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/docs/ROADMAP.md) — **Future Vision**
- [KESTREL MCP Integration](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/docs/KESTREL_MCP_INTEGRATION.md)
- [Official Commercetools MCP Setup](file:///Users/srinivasbellarichenna/Personal/dev/ai-projects/antigravity_workspace/commercetools-mcp-demo/docs/COMMERCETOOLS_MCP_SETUP.md)

### 4. [Development Skills](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/skills)
Domain-specific resources for AI agents:
- [commercetools-api](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/skills/commercetools-api.md)
- [commercetools-frontend](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/skills/commercetools-frontend.md)
- [commercetools-headless-commerce](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/skills/commercetools-headless-commerce.md)

## 🎭 Running the Autonomous Agent

To witness the AI agent placing an order:
```bash
cd commercetools-wrapper
docker compose up -d --build
# Monitor the client logs for success
docker logs -f commercetools-wrapper-mcp-client-1
```

---
*KESTREL - Commercetools MCP Integration Demo*
