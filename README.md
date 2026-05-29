# Composable Agentic Commerce - Commercetools MCP Demo

[![CI Build](https://github.com/srinivasbellarichenna/commercetools-mcp-demo/actions/workflows/build.yml/badge.svg)](https://github.com/srinivasbellarichenna/commercetools-mcp-demo/actions/workflows/build.yml)

Welcome to **Composable Agentic Commerce**, a demonstration of Commercetools microservices integrated with autonomous AI agents via the Model Context Protocol (MCP).

> [!WARNING]
> **Demo Sandbox Disclaimer**: This project is built solely as a local developer demonstration and sandbox environment. While it showcases secure patterns like Stripe Checkout payment redirection to keep payment credentials out of the LLM agent's context, the backend microservice APIs (Customer, Cart, Order, Payment services) do not implement application-level user authentication or authorization. This codebase is **not** a production-ready commerce backend.

This repository branch contains the **Foundation Layer** (Phase 1) of the architecture, which establishes the robust, scalable local microservice structure necessary to run demo e-commerce operations. It serves as the bedrock upon which the advanced AI orchestration (Phase 2) is built.

---

## 🎯 Phase 1: Key Capabilities

- **Headless Commerce Engine**: Full integration with Commercetools APIs for catalog discovery, cart management, and order finalization.
- **Microservice Architecture**: Decoupled Java Spring Boot microservices behind a unified API Gateway.
- **Stripe Checkout Payment Handoff**: Stripe Checkout integration that keeps sensitive PCI-compliant data completely out of the AI's scope (local sandboxed flow).
- **Model Context Protocol (MCP)**: A foundational Python FastMCP server that exposes deterministic e-commerce actions (tools) directly to AI models like Claude or Gemini.

---

## 🏛️ System Architecture & Deep Dives

The Foundation Layer is divided into three primary domains. For an in-depth understanding of how each component is built and architected, refer to their specific documentations:

### 📸 Visual Proof

<div align="center">
  <a href="https://youtu.be/I9nLHVyY9bQ">
    <img src="https://img.shields.io/badge/▶_Click_to_Watch_Demo_Video-FF0000?style=for-the-badge&logo=youtube&logoColor=white" alt="Watch Video" />
    <br/><br/>
    <img src="https://img.youtube.com/vi/I9nLHVyY9bQ/maxresdefault.jpg?v=2" alt="Ecommerce Demo Video" style="max-width:100%; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);" />
  </a>
</div>

### 1. [Backend Microservices](./phase-1/backend)
The core logic layer, built with Java and Spring Boot.
- 📖 **[Backend README](./phase-1/backend/README.md)**
- 🧠 **[Architectural Walkthrough](./phase-1/backend/walkthrough.md)**

### 2. [Frontend Storefront](./phase-1/frontend)
The customer-facing React application, built with Vite and styled with Vanilla CSS.
- 📖 **[Frontend README](./phase-1/frontend/README.md)**
- 🧠 **[Architectural Walkthrough](./phase-1/frontend/walkthrough.md)**

### 3. [Foundational MCP Server](./phase-1/foundational-mcp-server)
The Python server bridging the AI interfaces and the Java backend APIs.
- 📖 **[MCP Server README](./phase-1/foundational-mcp-server/README.md)**
- 🧠 **[Architectural Walkthrough](./phase-1/foundational-mcp-server/walkthrough.md)**

---

## 📚 AI Client Integration (MCP)

This project exposes its e-commerce capabilities via the Model Context Protocol (MCP). You can connect our foundational server directly to popular AI assistants, allowing them to act as autonomous shopping agents on your behalf.

- **Gemini CLI**: Register the server globally using the `gemini mcp add` command to give Gemini full interactive access to your storefront from the terminal.
- **Claude Desktop**: Configure your `claude_desktop_config.json` to attach the foundational MCP server, granting the Claude Desktop app native e-commerce capabilities.

For step-by-step instructions on wiring these up, please read our detailed setup guide:
- 🤖 **[Foundational MCP Setup Guide](./phase-1/docs/MCP_CLIENT_SETUP.md)**

---

## 🚀 Getting Started

Follow these steps to spin up the entire foundation layer locally.

### 0. Prerequisites
Before attempting to run this project, ensure you have the following third-party services configured:

**Commercetools Setup:**
1. Sign up for a [Commercetools 60-day free trial](https://commercetools.com/free-trial). *(Note: You can use a personal email address like `@gmail.com` in the "Business Email" field if you don't have a corporate one).*
2. Create a Commercetools project. **Crucially**, during the project creation flow, ensure you opt-in to load the sample demo data offered by Commercetools.
3. Ensure you have an **API Client** created with appropriate scopes (or Admin) to obtain your credentials.
4. In the Merchant Center, ensure products are **indexed** and that **Product Search** is fully activated so the backend can execute catalog queries.

**Stripe Setup:**
1. Create a Stripe developer account.
2. Obtain your **Stripe Secret Key** (`STRIPE_SECRET_KEY` starting with `sk_test_...`) to enable secure checkout flow generation.

### 1. Environment Configuration
Navigate into the backend directory and set up your environment variables:
```bash
cd phase-1/backend
cp .env.example .env
```
Open the newly created `.env` file and securely input your Commercetools API and Stripe credentials.

### 2. Compile and Start Services
First, compile the Java microservices (requires Java 17 to 22, with Java 17 recommended). Then, use Docker Compose to containerize and spin up the Backend Microservices, Frontend React App, and the Foundational MCP Server all at once.
```bash
# Ensure you are inside the 'phase-1/backend' directory
mvn clean package -DskipTests
docker compose up -d --build
```

### 3. Verify Deployment
Once Docker finishes booting, the services will be available locally at:
- 🛒 **Frontend Storefront**: `http://localhost:3001`
- 🌐 **API Gateway (Backend)**: `http://localhost:8085`
- 🔌 **MCP Server (SSE Endpoint)**: `http://localhost:8087`

---

## 📚 Global Resources
Resources that apply across the entire project:
- **[Development Skills](./skills)**: Domain-specific resources for AI agents.

## 🚀 What's Next? (Phase 2 Teaser)
The **Foundation Layer** you see here is just the beginning. 

In **Phase 2**, we will be introducing **Federated Agent Swarms** (powered by LangGraph). Instead of a single LLM trying to do everything, you will see a network of specialized agents—a Product Specialist, a Returns Manager, and a Checkout Coordinator—collaborating autonomously to serve the customer. 

Star and Watch the repository to stay updated on the Phase 2 launch!

---
*Composable Agentic Commerce - Established 2026*
