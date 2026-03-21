from capabilities.base import BaseCapability
from agents.transaction_agent import TransactionAgent

class CheckoutCapability(BaseCapability):
    def __init__(self, proxy):
        super().__init__(proxy)
        self.agent = TransactionAgent(proxy)

    async def process_async(self, cart_id):
        return await self.agent.checkout_async(cart_id)
