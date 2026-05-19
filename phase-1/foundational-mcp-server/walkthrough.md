# Foundational MCP Server: Architectural Walkthrough

This document breaks down the design decisions and implementation details of the `foundational-mcp-server` located in `phase-1/foundational-mcp-server/main.py`.

## 1. The FastMCP Bridge

The server utilizes the `FastMCP` framework (`mcp.server.fastmcp`) to expose Python async functions directly to the Model Context Protocol.

```python
mcp = FastMCP(
    "foundational-mcp-server",
    transport_security=TransportSecuritySettings(enable_dns_rebinding_protection=False)
)
```
This configuration allows the server to act as a bridge. It translates the abstract, high-level intent of an LLM (e.g., "Add the pink chair to the cart") into the strict, payload-heavy HTTP requests required by the Java backend (`api-gateway`).

## 2. LLM-Optimized API Responses

Raw JSON responses from enterprise e-commerce platforms like Commercetools are often heavily nested, containing complex metadata that can confuse or consume the token window of an LLM.

To combat this, the MCP server acts as an **Anti-Corruption Layer**:
- **SKU Extraction**: In `get_collection` and `search_products`, the server aggressively hunts for the product's SKU across different API version schemas (`masterVariant.sku` vs `masterData.current.masterVariant.sku`) and hoists it to the top level as `suggested_sku`. 
- This guarantees that when the LLM decides to add an item to the cart, it doesn't hallucinate an ID, but uses the exact SKU string requested by the `add_to_cart` tool.

## 3. Asynchronous HTTP Orchestration

All upstream calls to the Java API Gateway are made using `httpx.AsyncClient`. This ensures that the MCP server remains highly concurrent and doesn't block while waiting for backend operations (like creating a Stripe checkout session) to resolve.

## 4. State Management and Fallbacks

E-commerce state machines are rigid (e.g., you cannot add a payment to a cart that has already been converted into an order). LLMs, however, often retry failed tool calls or act non-linearly.

The MCP server implements robust safety checks and fallbacks to handle AI behavior:
```python
async def _check_for_existing_order(cart_id: str) -> Optional[dict]:
    # ...
```
In tools like `place_order` and `add_payment_to_cart`, if the Java backend returns an HTTP 409 Conflict or 400 Bad Request, the Python script intercepts the error. It checks if the cart was *already* converted to an order. If it was, instead of throwing an error back to the LLM (which might cause the AI to panic or loop), it gracefully intercepts and returns the existing order details as success.

## 5. Security & Payment Delegation

The MCP server explicitly **does not** handle raw credit card details. 
When an AI wants to execute a payment, it uses the `create_stripe_checkout` tool. This tool delegates the PCI-compliant tokenization to Stripe via the Java backend, returning a secure checkout URL. The AI is instructed via its docstring to present this URL to the user, ensuring the agent remains out of scope for sensitive financial data processing.
