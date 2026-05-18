# Backend Walkthrough: Commercetools Wrapper

The backend of this project is a collection of Spring Boot microservices designed to provide a clean, simplified API over the Commercetools platform. This architecture allows for better separation of concerns and easier integration with the agentic frontend and MCP servers.

## Architecture Overview

The backend follows a microservices pattern, with each service responsible for a specific domain. All services are containerized using Docker and orchestrated via Docker Compose.

## Microservices

### 1. [API Gateway](../../commercetools-wrapper/api-gateway)

* **Role**: Entry point for all external requests from the frontend and MCP clients.
* **Functionality**: Routes requests to the appropriate downstream microservice based on the URL path. It also handles common concerns like cross-origin resource sharing (CORS).
* **API Exposure**: All microservice endpoints listed below are accessible via the gateway at the same relative paths (e.g., `GATEWAY_URL/api/products`).

### 2. [Customer Service](../../commercetools-wrapper/customer-service)

* **Role**: Manages customer profiles and authentication.
* **Functionality**: Interfaces with Commercetools to create, update, and retrieve customer information.
* **Key APIs**:
  * `POST /api/customers/register`: Creates a new customer profile.
  * `POST /api/customers/login`: Authenticates a customer and returns profile data.
  * `GET /api/customers/{id}`: Retrieves a specific customer's profile and addresses.
  * `GET /api/customers/search?email=...`: Finds a customer by their email address.
  * `PATCH /api/customers/{id}/profile`: Updates basic profile info (name, email).
  * `POST /api/customers/{id}/addresses`: Adds a new shipping/billing address to the profile.

### 3. [Product Service](../../commercetools-wrapper/product-service)

* **Role**: Handles all product-related data.
* **Functionality**: Wraps the Commercetools product catalog for efficient retrieval.
* **Key APIs**:
  * `GET /api/products`: Returns a paginated list of all published products.
  * `GET /api/products/search?keyword=...`: Performs a full-text search across the product catalog.
  * `GET /api/products/{id}`: Fetches detailed projection data for a single product.

### 4. [Cart Service](../../commercetools-wrapper/cart-service)

* **Role**: Manages shopping carts and line items.
* **Functionality**: Manages the lifecycle of a shopping session.
* **Key APIs**:
  * `POST /api/carts`: Creates a new anonymous or customer-linked cart.
  * `GET /api/carts/{id}`: Retrieves the current state of a cart, including totals and line items.
  * `POST /api/carts/{id}/items`: Adds a product to the cart via its SKU.
  * `DELETE /api/carts/{id}/items/{itemId}`: Removes a line item from the cart.
  * `POST /api/carts/{id}/shipping-address`: Sets the delivery destination for tax and shipping calculations.
  * `POST /api/carts/{id}/payments`: Associates a payment object with the cart.

### 5. [Order Service](../../commercetools-wrapper/order-service)

* **Role**: Orchestrates the order placement process.
* **Functionality**: Responsible for the final conversion of a cart to a permanent order record.
* **Key APIs**:
  * `POST /api/orders/from-cart`: Converts a valid, paid cart into a final order.
  * `GET /api/orders/{id}`: Retrieves details of a placed order.
  * `GET /api/orders/customer/{customerId}`: Lists all orders for a specific customer.
  * `GET /api/orders/cart/{cartId}`: Finds the order associated with a specific cart ID (useful for idempotency checks).

### 6. [Payment Service](../../commercetools-wrapper/payment-service)

* **Role**: Orchestrates fiscal transactions and integrates with external payment processors.
* **Port**: `8084`
* **Key APIs**:
  * `POST /api/payments/checkout`: Initiates a Stripe Checkout session.
    * **Parameters**: `cartId` (String), `successUrl` (String), `cancelUrl` (String).
    * **Logic**: Calculates the total cent amount from the Cart, initializes the Stripe SDK, and generates a hosted checkout URL. It appends critical metadata (`cartId` and `cartVersion`) to the success URL for post-payment processing.
  * `POST /api/payments`: Records a payment transaction in Commercetools.
  * `GET /api/payments/session/{sessionId}`: Retrieves raw session data from Stripe for verification.

**Stripe Integration Details**:

The service uses the Stripe Java SDK. It requires a `STRIPE_SECRET_KEY` environment variable to be set. During the checkout session creation, it automatically configures:

* **Success URL**: Includes `{CHECKOUT_SESSION_ID}` placeholder which Stripe replaces with the actual ID.
* **Line Items**: Dynamically generated based on the Commercetools Cart totals.

## Environment Configuration

To run the backend services, the following environment variables must be configured (typically in a `.env` file):

| Variable | Description |
| :--- | :--- |
| `CTP_PROJECT_KEY` | Your Commercetools project identifier. |
| `CTP_CLIENT_ID` | API Client ID with appropriate scopes. |
| `CTP_CLIENT_SECRET` | API Client Secret. |
| `STRIPE_SECRET_KEY` | Your Stripe Secret API Key (starts with `sk_test_` or `sk_live_`). |

## Core Technologies

* **Java 17+**: The primary programming language.
* **Spring Boot 3.x**: The framework used for building the microservices.
* **Spring Cloud Gateway**: Powers the API Gateway.
* **Commercetools Java SDK (v2)**: Used for all interactions with the Commercetools platform.
* **Lombok**: Used to reduce boilerplate code (getters, setters, logging).
* **Docker & Docker Compose**: For containerization and local development environment setup.

## Running the Backend

### Using Docker Compose (Recommended)

The most efficient way to start the entire backend stack is using Docker Compose from the root directory:

```bash
docker-compose up --build
```

### Running Individually

Each service is a Spring Boot application and can be run independently using Maven:

```bash
mvn spring-boot:run
```

Ensure all services are registered with the API Gateway for correct routing.
Each service requires Commercetools credentials (Project Key, Client ID, Client Secret, etc.) to be provided via environment variables, typically managed in a `.env` file.
