# Foundation Layer: Backend Microservices

This directory contains the core Java Spring Boot microservice ecosystem for the Composable Agentic Commerce project. It acts as an abstraction and orchestration layer over the headless Commercetools APIs.

## 🏗️ Architecture

The backend is composed of five distinct domain services and a unified API Gateway:

- **`api-gateway`**: The entry point for all frontend and MCP requests. Handles routing to the underlying domain services.
- **`product-service`**: Manages catalog queries, search, and product details.
- **`cart-service`**: Handles cart initialization, line item management, and shipping logic.
- **`customer-service`**: Manages customer profiles, address books, and order history.
- **`payment-service`**: Integrates with Stripe to generate checkout sessions and bind payments to carts.
- **`order-service`**: Finalizes carts into strict order records.

## 🚀 Building and Running

### Prerequisites
- Java 17 to 22 (Java 17 Recommended)
- Maven
- Docker & Docker Compose

### Local Development

1. **Configure Environment**
   Copy the provided `.env.example` to `.env` and populate it with your Commercetools API credentials and Stripe keys:
   ```bash
   cp .env.example .env
   ```

2. **Compile Services**
   Use Maven to build all microservice JARs:
   ```bash
   mvn clean package -DskipTests
   ```

3. **Spin up Infrastructure**
   Use Docker Compose to containerize and start the entire backend (along with the frontend and MCP server):
   ```bash
   docker compose up -d --build
   ```

The API Gateway will be available at `http://localhost:8085`.
