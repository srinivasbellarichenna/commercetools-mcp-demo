# Foundational MCP Server

This project is a Model Context Protocol (MCP) server built with Python and [FastMCP](https://github.com/jlowin/fastmcp). It acts as the intelligent bridge between autonomous AI agents and our Composable Commerce backend.

Instead of exposing raw REST APIs or GraphQL endpoints to the AI, this server provides a curated, domain-specific set of tools (functions) that represent discrete e-commerce actions.

## 🏗️ Architecture

- **Framework**: `mcp.server.fastmcp.FastMCP`
- **Transport**: Supports both `stdio` (for local Claude Desktop integration) and `SSE` (Server-Sent Events, for federated remote agents).
- **Upstream**: Communicates directly with the `api-gateway` Java microservice.

## 🛠️ Exposed Tools

The server exposes the following capabilities to the AI:

### Catalog & Discovery
- `get_collection(limit, offset)`: List products available in the storefront.
- `search_products(keyword, limit, offset)`: Search for specific products.
- `get_piece_detail(piece_id)`: Retrieve deep specifications of a product.

### Cart & Checkout
- `initialize_cart(currency, country)`: Create a new shopping cart.
- `add_to_cart(cart_id, sku, quantity)`: Add items to the cart.
- `get_cart(cart_id)`: Retrieve the current state of a cart.
- `set_shipping_address(...)`: Apply a shipping destination.
- `get_shipping_methods(cart_id)`: Look up available delivery options.
- `set_shipping_method(cart_id, shipping_method_id)`: Select a delivery method.

### Payments & Orders
- `create_payment(cart_id, amount, currency, payment_method)`: Initialize a payment object.
- `add_payment_to_cart(cart_id, payment_id)`: Bind a payment to the cart.
- `create_stripe_checkout(cart_id)`: Generate a secure payment URL.
- `place_order(cart_id, version)`: Finalize the checkout.

### Customer Management
- `get_customer_by_email(email)`: Look up a customer profile.
- `get_customer_profile(customer_id)`: Retrieve order history and details.
- `update_customer_profile(...)`: Modify customer details.

## 🚀 Running the Server

### Docker (Recommended)
This server is automatically built and orchestrated alongside the Java backend using `docker compose`. It exposes its SSE endpoint on port `8087`.

### Local Execution
To run locally without Docker (e.g., for direct `stdio` attachment to Claude Desktop):

```bash
# 1. Create a virtual environment
python3 -m venv venv
source venv/bin/activate

# 2. Install dependencies
pip install -r requirements.txt

# 3. Configure the upstream API
export API_BASE_URL="http://localhost:8085/api"

# 4. Start the server (SSE Mode)
python run_sse.py
```
