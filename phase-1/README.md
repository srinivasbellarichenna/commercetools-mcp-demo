# Phase 1: Foundation Layer

Welcome to the **Foundation Layer** of the Composable Agentic Commerce project. This phase focuses on the core infrastructure necessary to run the e-commerce operations.

## 🏛️ Architecture

- **[Backend Foundation](./backend)**: The core microservice ecosystem (Product, Cart, Order, and API Gateway).
- **[Frontend Application](./frontend)**: The customer-facing storefront, providing a modern visual experience.
- **[Foundational MCP Server](./foundational-mcp-server)**: The base Model Context Protocol server that exposes primitive e-commerce tools to models.

## 📚 Documentation
- **[Setup Guides](./docs)**: Setup guides for the foundation, including Docker and Commercetools configuration.

## 🚀 Getting Started

### 1. Environment Configuration
Navigate into the backend directory and set up your environment variables:
```bash
cd backend
cp .env.example .env
```
Open the `.env` file and fill in your Commercetools API and Stripe credentials.

### 2. Compile and Start Services
Compile the Java microservices and spin up the entire foundation layer (Backend Microservices, Frontend React App, and the Foundational MCP Server):
```bash
# From inside the backend directory
mvn clean package -DskipTests
docker compose up -d --build
```
This single Docker Compose command will containerize and start all Phase 1 components.
