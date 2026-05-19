# Composable Agentic Commerce - Commercetools MCP Demo

[![CI Build](https://github.com/srinivasbellarichenna/commercetools-mcp-demo/actions/workflows/build.yml/badge.svg)](https://github.com/srinivasbellarichenna/commercetools-mcp-demo/actions/workflows/build.yml)

Welcome to **Composable Agentic Commerce**, a demonstration of Commercetools microservices integrated with autonomous AI agents via the Model Context Protocol (MCP).

This repository branch contains the **Foundation Layer** (Phase 1) of the architecture, which establishes the robust, scalable, and secure infrastructure necessary to run modern e-commerce operations. It serves as the bedrock upon which the advanced AI orchestration (Phase 2) is built.

---

## ✨ Masterclass Enhancements (Phase 1)

This project has been meticulously engineered across the entire stack, propelling it from a basic foundation into a premium, enterprise-grade architecture:

### ⚡ 1. Architectural Revolution: Blocking to Reactive
We propelled the backend into the future by replacing the traditional synchronous model with a fully asynchronous, reactive architecture.
- **Spring WebFlux Transition**: Replaced `spring-boot-starter-web` with WebFlux across all 5 microservices. They now run on the high-concurrency Netty server instead of Tomcat.
- **Non-Blocking SDK Integration**: Eradicated all `executeBlocking()` calls. We now bridge Commercetools `CompletableFuture`s into Project Reactor `Mono` streams, allowing the CPU to handle thousands more concurrent requests with the same resources.
- **Reactive Controllers**: Every endpoint now returns `Mono<T>`, ensuring that threads are released back to the pool instantly while waiting for I/O, drastically increasing system throughput.

### 🎨 2. Frontend "Artisan Heritage" Aesthetic Polish
We moved beyond functional UI to a "Premium Boutique" feel using advanced CSS and interaction design.
- **Skeleton Loading System**: Replaced jarring loading spinners with shimmering skeleton cards. This reduces layout shift and makes the "Reactive" backend feel even faster by providing immediate visual structure.
- **Staggered Entrance Animations**: Implemented a "cascade" effect using Framer Motion where products glide in with incremental delays (100ms), creating a high-end, orchestrated feel.
- **Slide-out Cart Drawer**: Implemented a global Reactive Cart Drawer with backdrop blurring and spring-physics animations. It provides immediate, non-disruptive feedback when items are added to the bag.

### 🤖 3. Agentic Intelligence & Safety
The project is uniquely optimized for AI agents via the Model Context Protocol (MCP).
- **Anti-Corruption Layer**: The MCP server acts as a bridge, stripping complex enterprise JSON into LLM-friendly "hoisted" SKUs, which prevents AI hallucination during shopping tasks.
- **Idempotency & Safety Guards**: Added smart fallback checks (like `_check_for_existing_order`) that intercept 400/409 errors. This prevents AI agents from getting stuck in "infinite retry loops" if they attempt to modify an already finalized order.
- **PCI-Compliant Delegation**: Structured the payment flow to delegate sensitive Stripe data to secure, hosted URLs, keeping the AI agent completely out of scope for financial risks.

### 🏗️ 4. Enterprise-Grade Engineering
- **DTO-Driven Validation**: Introduced `AddressRequestDTO` with `jakarta.validation` to ensure the reactive pipelines only process high-quality, sanitized data.
- **Global State Orchestration**: Refactored `CartContext` to handle not just data, but UI state (Drawer toggles) across the entire application.
- **Production-Ready DevOps**: Optimized the multi-stage Docker builds to ensure minimal image sizes and rapid deployment cycles.

---

## 🏛️ System Architecture & Deep Dives

The Foundation Layer is divided into three primary domains. For an in-depth understanding of how each component is built and architected, refer to their specific documentations:

### 📸 Visual Proof
*(Replace these placeholders with actual screenshots before publishing)*
<div align="center">
  <img src="./assets/storefront.png" alt="React Storefront" width="45%" />
  <img src="./assets/claude-demo.png" alt="Claude MCP Action" width="45%" />
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
First, compile the Java microservices. Then, use Docker Compose to containerize and spin up the Backend Microservices, Frontend React App, and the Foundational MCP Server all at once.
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
