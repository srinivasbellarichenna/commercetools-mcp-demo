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

## 5. Detailed Microservice APIs

Below is a reference for the domain-specific APIs exposed through the Gateway that the frontend and MCP servers rely on.

### Customer Service
- `POST /api/customers/register`: Creates a new customer profile.
- `POST /api/customers/login`: Authenticates a customer and returns profile data.
- `GET /api/customers/{id}`: Retrieves a specific customer's profile and addresses.
- `GET /api/customers/search?email=...`: Finds a customer by their email address.
- `PATCH /api/customers/{id}/profile`: Updates basic profile info (name, email).
- `POST /api/customers/{id}/addresses`: Adds a new shipping/billing address.

### Product Service
- `GET /api/products`: Returns a paginated list of all published products.
- `GET /api/products/search?keyword=...`: Performs a full-text search across the catalog.
- `GET /api/products/{id}`: Fetches detailed projection data for a single product.

### Cart Service
- `POST /api/carts`: Creates a new anonymous or customer-linked cart.
- `GET /api/carts/{id}`: Retrieves the current state of a cart (totals and items).
- `POST /api/carts/{id}/items`: Adds a product to the cart via its SKU.
- `DELETE /api/carts/{id}/items/{itemId}`: Removes a line item.
- `POST /api/carts/{id}/shipping-address`: Sets the delivery destination.
- `GET /api/carts/{id}/shipping-methods`: Retrieves valid shipping options.
- `POST /api/carts/{id}/shipping-method`: Saves the selected delivery method.

### Order Service
- `POST /api/orders/from-cart`: Converts a valid, paid cart into a final order.
- `GET /api/orders/{id}`: Retrieves details of a placed order.
- `GET /api/orders/customer/{customerId}`: Lists all orders for a specific customer.
- `GET /api/orders/cart/{cartId}`: Finds the order associated with a specific cart ID (critical for idempotency checks in the MCP server).

### Payment Service
- `POST /api/payments/checkout`: Initiates a Stripe Checkout session. It calculates the total from the Cart and generates a hosted checkout URL.
- `POST /api/payments`: Records a payment transaction in Commercetools.
- `POST /api/carts/{id}/payments`: Associates a payment object with the cart.
