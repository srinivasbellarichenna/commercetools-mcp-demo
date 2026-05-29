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

---

## 🛡️ Production Hardening Recommendations

While this project functions as a local developer sandbox, transitioning it to a production-grade deployment would require implementing the following security and operational enhancements:

1. **API Gateway Authentication & Resource Authorization**:
   Downstream microservices (like `cart-service` and `customer-service`) currently process requests containing direct resource IDs (e.g., `cartId` or `customerId`) without validating ownership. In production, a security framework (such as Spring Security OAuth2 / Resource Server) should be added to the `api-gateway` to validate JWT bearer tokens or session cookies. The gateway should also inject user contexts into downstream headers to enforce resource ownership at the microservice level.

2. **Secrets & Credentials Management**:
   The current setup uses plain `.env` files and Docker Compose variables to store Commercetools client credentials and Stripe API keys. For production, these should be retrieved dynamically from a secure key-management service (such as HashiCorp Vault, AWS Secrets Manager, or Google Cloud Secret Manager).

3. **Rate Limiting & Abuse Prevention**:
   To protect endpoints from automated scraping or brute-force requests, rate-limiting rules (using Spring Cloud Gateway RateLimiter with Redis) should be introduced at the gateway boundary.

4. **Service Discovery & Dynamic Routing**:
   Dynamic host resolution (via tools like Consul, Netflix Eureka, or Kubernetes DNS services) should replace the static hostnames currently configured in the backend configuration.
