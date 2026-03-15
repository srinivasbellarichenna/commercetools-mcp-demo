import os
import json
import httpx
import logging
from typing import Dict, Any, List, Optional
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("mcp-proxy")

class MCPProxy:
    """
    Acts as a bridge between Federated Agents and the stable MCP Action Layer.
    """
    def __init__(self, base_url: str = os.getenv("API_BASE_URL", "http://localhost:8085/api")):
        self.base_url = base_url

    async def call_tool(self, tool_name: str, arguments: Dict[str, Any]) -> Dict[str, Any]:
        """
        Translates agent tool requests into direct backend API calls (simulating MCP tool execution).
        In a full implementation, this could interface with the MCP server via JSON-RPC.
        """
        # Mapping MCP tool names to internal API endpoints for the proxy
        endpoint_map = {
            "get_customer_by_email": ("/customers/search", "GET"),
            "get_cart": ("/carts/{cart_id}", "GET"),
            "add_to_cart": ("/carts/{cart_id}/items", "POST"),
            "set_shipping_address": ("/carts/{cart_id}/shipping-address", "POST"),
            "create_stripe_checkout": ("/payments/checkout", "POST"),
            "place_order": ("/orders/from-cart", "POST"),
        }

        if tool_name not in endpoint_map:
            raise ValueError(f"Unknown tool: {tool_name}")

        path_template, method = endpoint_map[tool_name]
        
        # Handle path parameters if any (e.g., {cart_id})
        path = path_template.format(**arguments)
        url = f"{self.base_url}{path}"
        
        # Remove path params from arguments for query/body
        clean_args = {k: v for k, v in arguments.items() if f"{{{k}}}" not in path_template}

        async with httpx.AsyncClient(timeout=30.0) as client:
            logger.info(f"A2A Proxy: Calling {method} {url} with {clean_args}")
            if method == "GET":
                response = await client.get(url, params=clean_args)
            else:
                # Some POST endpoints in this demo use query params, others use JSON
                if tool_name in ["add_to_cart", "create_stripe_checkout", "place_order"]:
                    response = await client.post(url, params=clean_args)
                else:
                    response = await client.post(url, json=clean_args)
            
            response.raise_for_status()
            return response.json()
