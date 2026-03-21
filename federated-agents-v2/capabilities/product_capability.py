from capabilities.base import BaseCapability
from agents.analyst_agent import AnalystAgent

class ProductCapability(BaseCapability):
    def __init__(self, proxy):
        super().__init__(proxy)
        self.agent = AnalystAgent(proxy)

    async def search_async(self, inputs):
        query = inputs.get("query", "")
        brand = inputs.get("brand")
        price_max = inputs.get("priceMax")
        
        results = await self.agent.search_products_async(query, brand, price_max)
        
        # Normalize results for the execution engine evaluation
        import logging
        logger = logging.getLogger("product-capability")
        logger.info(f"ProductCapability: search_async for '{query}' returned {len(results) if isinstance(results, list) else 'non-list'} items")
        if not isinstance(results, list):
            return []
        return results

    async def get_detail_async(self, product_id):
        return await self.agent.get_product_detail_async(product_id)
