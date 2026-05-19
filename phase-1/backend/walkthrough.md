# Backend Microservices: Architectural Walkthrough

This document outlines the design patterns and technical decisions implemented within the Java backend.

## 1. Microservice Domain Separation
We embraced a strict microservice architecture to isolate domains. Unlike traditional monoliths where cart logic and order logic might become entangled, separating these into `cart-service` and `order-service` ensures that:
- **Resilience**: If the `payment-service` goes down, the `product-service` remains unaffected, allowing customers to continue browsing the catalog.
- **Scalability**: High-traffic services like `product-service` can be horizontally scaled independently of the `order-service`.

## 2. API Gateway Pattern
Instead of exposing six different ports to the frontend and the MCP server, we utilize an **API Gateway** (`api-gateway`). 
- It provides a single unified entry point (`http://localhost:8085`).
- It abstracts the internal microservice topology from consumers.

## 3. Commercetools SDK Integration
The project leverages the official `commercetools-sdk-java-api` (v18.2.0). 
Each microservice independently maintains its own API client instance, initialized using the environment variables (`CTP_PROJECT_KEY`, `CTP_CLIENT_ID`, etc.) injected via Docker Compose.

## 4. Payment Orchestration
The `payment-service` acts as a crucial middleware between Commercetools and Stripe. 
When an AI agent (or the frontend) requests a payment via `create_stripe_checkout`:
1. The service generates a secure Stripe Session URL.
2. It returns this URL so the consumer can securely input credit card details out-of-scope of the AI.
3. Once completed, a Payment object is persisted in Commercetools and associated with the active Cart.
