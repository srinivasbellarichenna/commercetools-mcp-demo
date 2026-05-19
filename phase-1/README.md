# Phase 1: Foundation Layer 🧱

Welcome to the **Foundation Layer** of the Composable Agentic Commerce project. This phase establishes the robust, scalable, and secure infrastructure necessary to run modern e-commerce operations powered by Commercetools and Stripe. It serves as the bedrock upon which the advanced AI orchestration (Phase 2) is built.

---

## 🎯 Key Capabilities

- **Headless Commerce Engine**: Full integration with Commercetools APIs for catalog discovery, cart management, and order finalization.
- **Microservice Architecture**: Decoupled Java Spring Boot microservices behind a unified API Gateway.
- **Secure Payment Processing**: Seamless Stripe Checkout integration that keeps sensitive PCI-compliant data completely out of the AI's scope.
- **Model Context Protocol (MCP)**: A foundational Python FastMCP server that exposes deterministic e-commerce actions (tools) directly to AI models like Claude or Gemini.

---

## 🏛️ System Architecture & Deep Dives

The Foundation Layer is divided into three primary domains. For an in-depth understanding of how each component is built and architected, refer to their specific documentations:

### 1. [Backend Microservices](./backend)
The core logic layer, built with Java and Spring Boot.
- 📖 **[Backend README](./backend/README.md)**
- 🧠 **[Architectural Walkthrough](./backend/walkthrough.md)**

### 2. [Frontend Storefront](./frontend)
The customer-facing React application, built with Vite and styled with Vanilla CSS.
- 📖 **[Frontend README](./frontend/README.md)**
- 🧠 **[Architectural Walkthrough](./frontend/walkthrough.md)**

### 3. [Foundational MCP Server](./foundational-mcp-server)
The Python server bridging the AI interfaces and the Java backend APIs.
- 📖 **[MCP Server README](./foundational-mcp-server/README.md)**
- 🧠 **[Architectural Walkthrough](./foundational-mcp-server/walkthrough.md)**

---

## 📚 MCP Configuration Guides

To securely connect this foundation layer to your AI agents (like Claude Desktop or Gemini CLI), please read our setup guides:
- 🤖 **[Commercetools MCP Setup Guide](./docs/COMMERCETOOLS_MCP_SETUP.md)**

---

## 🚀 Getting Started

Follow these steps to spin up the entire foundation layer locally.

### 1. Environment Configuration
Navigate into the backend directory and set up your environment variables:
```bash
cd backend
cp .env.example .env
```
Open the newly created `.env` file and securely input your Commercetools API and Stripe credentials.

### 2. Compile and Start Services
First, compile the Java microservices. Then, use Docker Compose to containerize and spin up the Backend Microservices, Frontend React App, and the Foundational MCP Server all at once.
```bash
# Ensure you are inside the 'backend' directory
mvn clean package -DskipTests
docker compose up -d --build
```

### 3. Verify Deployment
Once Docker finishes booting, the services will be available locally at:
- 🛒 **Frontend Storefront**: `http://localhost:3001`
- 🌐 **API Gateway (Backend)**: `http://localhost:8085`
- 🔌 **MCP Server (SSE Endpoint)**: `http://localhost:8087`
