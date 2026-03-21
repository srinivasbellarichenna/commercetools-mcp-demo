# Foundational MCP Server Walkthrough: Core E-Commerce Tools

The Foundational MCP Server provides the foundational tools and resources that allow AI models (like Claude) to interact directly with the Commercetools-powered backend. It acts as a bridge between the high-level agentic logic and the low-level microservices.

## Overview

The server is built with Python using the `mcp[fastmcp]` framework. It exposes a wide range of tools that cover the entire e-commerce journey, from browsing products to finalizing a purchase.

## Available Tools

The server exposes the following categories of tools:

### Product Discovery

* **`get_collection`**: Fetches a paginated list of products from the catalog.
* **`get_piece_detail`**: Retrieves full specifications for a specific product by its ID.

### Cart & Checkout Management

* **`initialize_cart`**: Creates a new shopping cart session.
* **`add_to_cart`**: Adds a specific product (by SKU) to an active cart.
* **`get_cart`**: Returns the current state and contents of a cart.
* **`set_shipping_address`**: Attaches a delivery address to the cart.
* **`get_shipping_methods` / `set_shipping_method`**: Manages delivery options.

### Payment & Ordering

* **`create_stripe_checkout`**: Generates a secure URL for the customer to complete their payment via Stripe.
* **`create_payment` / `add_payment_to_cart`**: Manages the link between payment transactions and shopping carts.
* **`place_order`**: The final step that converts a paid cart into a Commercetools order.

### Customer Management

* **`get_customer_by_email`**: Finds a customer profile using their email address.
* **`get_customer_profile`**: Retrieves detailed profile information and order history.
* **`update_customer_profile`**: Allows updating customer contact details.

## Integration Details

* **FastMCP**: Used to define tools and manage the MCP lifecycle.
* **HTTPX**: Asynchronous HTTP client used to communicate with the Spring Boot backend microservices.
* **Environment Variables**: Uses `API_BASE_URL` to point to the API Gateway (e.g., `http://localhost:8085/api`).

## Execution

The server can be started in SSE mode (Server-Sent Events) for use in the agentic-agency architecture:

```bash
cd ../../foundational-mcp-server
# Install dependencies
pip install -r requirements.txt
# Run the SSE server
python run_sse.py
```

This enables real-time tool execution and status reporting via the SSE transport.
