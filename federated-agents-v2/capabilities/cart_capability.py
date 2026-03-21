from capabilities.base import BaseCapability
from agents.transaction_agent import TransactionAgent

class CartCapability(BaseCapability):
    def __init__(self, proxy):
        super().__init__(proxy)
        self.agent = TransactionAgent(proxy)

    async def add_async(self, product):
        sku = product.get("sku")
        if not sku:
            # Business logic: if SKU is missing, we can't add to cart
            raise ValueError("Cannot add to cart: Product missing SKU")
        
        # Invariants: check stock if not already checked
        # (Assuming here that we trust the 'in_stock' field if present)
        if product.get("in_stock") is False:
             raise ValueError(f"Product {sku} is out of stock")

        return await self.agent.add_to_cart_async(sku)
