import logging
from typing import Dict, Any, Optional
from shared.mcp_proxy import MCPProxy

logger = logging.getLogger("transaction-agent")

class TransactionAgent:
    def __init__(self, proxy: MCPProxy):
        self.proxy = proxy

    async def add_to_cart_async(self, sku: str, quantity: int = 1) -> Dict[str, Any]:
        """
        Adds a product to the cart via MCP.
        """
        logger.info(f"Closer: Adding SKU '{sku}' to cart")
        result = await self.proxy.call_tool("add_to_cart", {"sku": sku, "quantity": quantity})
        return result if isinstance(result, dict) else {"error": "Invalid response"}

    async def checkout_async(self, cart_id: str) -> Dict[str, Any]:
        """
        Processes checkout via MCP.
        """
        logger.info(f"Closer: Initiating checkout for cart '{cart_id}'")
        # In this demo, we might need multiple steps or a specialized tool
        result = await self.proxy.call_tool("place_order", {"cart_id": cart_id})
        return result if isinstance(result, dict) else {"status": "SUCCESS", "order": result}
