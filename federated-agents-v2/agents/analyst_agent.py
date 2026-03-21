import logging
from typing import List, Dict, Any, Optional
from shared.mcp_proxy import MCPProxy

logger = logging.getLogger("analyst-agent")

class AnalystAgent:
    def __init__(self, proxy: MCPProxy):
        self.proxy = proxy

    async def search_products_async(self, query: str, brand: Optional[str] = None, price_max: Optional[float] = None) -> List[Dict[str, Any]]:
        """
        Executes a product search via MCP.
        """
        logger.info(f"Analyst: Searching for '{query}' (Brand: {brand}, Max Price: {price_max})")
        arguments = {"query": query}
        if brand: arguments["brand"] = brand
        if price_max: arguments["priceMax"] = price_max
        
        result = await self.proxy.call_tool("search_products", arguments)
        logger.info(f"AnalystAgent: MCP Tool 'search_products' response type: {type(result)}")
        
        # Ensure we return a list of products
        products = []
        if isinstance(result, list):
            products = result
        elif isinstance(result, dict) and "products" in result:
            products = result["products"]
        
        logger.info(f"AnalystAgent: Returning {len(products)} products from agent")
        return products

    async def get_product_detail_async(self, product_id: str) -> Dict[str, Any]:
        """
        Retrieves product details via MCP.
        """
        logger.info(f"Analyst: Getting details for PIECE '{product_id}'")
        result = await self.proxy.call_tool("get_piece_detail", {"piece_id": product_id})
        return result if isinstance(result, dict) else {"error": "Invalid response"}
