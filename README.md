# KESTREL Atelier - Commercetools MCP Demo

Welcome to the **KESTREL Atelier**, a prestigious demonstration of Commercetools microservices integrated with autonomous AI agency via the Model Context Protocol (MCP).

## 🏛️ Tiered Architecture

The project is organized into curated layers to ensure architectural harmony:

### 1. [Frontend Atelier](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/frontend)
The customer-facing visual experience, elevated to its own professional workspace.

### 2. [Heritage Foundation (Backend Services)](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/commercetools-wrapper)
The core microservice ecosystem, featuring:

### 2. [Agency Layer](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/agency)
The orchestral bridges for AI integration:
- **MCP Server**: A custom Python bridge (SSE) providing curatorial tools.
- **MCP Client**: An autonomous orchestrator powered by local LLMs (Llama 3, Qwen 2.5).

### 3. [Curatorial Guidelines](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/docs)
Essential blueprints for setup and integration:
- [KESTREL MCP Integration](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/docs/KESTREL_MCP_INTEGRATION.md)
- [Official Commercetools MCP Setup](file:///Users/srinivasbellarichenna/Personal/dev/ai-projects/antigravity_workspace/commercetools-mcp-demo/docs/COMMERCETOOLS_MCP_SETUP.md)

### 4. [Heritage Skills](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/skills)
Domain-specific manuscripts for specialized agency:
- [commercetools-api](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/skills/commercetools-api.md)
- [commercetools-frontend](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/skills/commercetools-frontend.md)
- [commercetools-headless-commerce](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/skills/commercetools-headless-commerce.md)

## 🎭 The Agentic Journey

To witness the AI curator placing an artisanal order:
```bash
cd commercetools-wrapper
docker compose up -d --build
# Monitor the client logs for victory
docker logs -f commercetools-wrapper-mcp-client-1
```

---
*KESTREL Atelier - Established 2026*
