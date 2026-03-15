import logging
import os
import json
from typing import Optional
from openai import OpenAI
from shared.mcp_proxy import MCPProxy

logger = logging.getLogger("closer")

class Closer:
    """
    Transaction Specialist: Handles secure checkout, address setting, and order placement.
    """
    def __init__(self, proxy: MCPProxy):
        self.proxy = proxy
        self.client = OpenAI(
            base_url=os.getenv("LM_STUDIO_URL", "http://host.docker.internal:1234/v1"),
            api_key="lm-studio"
        )
        self.model = os.getenv("MODEL_NAME", "meta-llama-3-8b-instruct")

    async def handle_transaction(self, prompt: str, customer_id: Optional[str] = None):
        """
        Reasoning loop for transaction requests.
        """
        logger.info(f"Closer Reasoning started for: {prompt}")
        
        try:
            # Step 1: Identify what the user wants to do with their cart
            # Strategy: Get the latest cart for the customer if ID is provided
            cart_info = None
            if customer_id:
                # We'd ideally find the active cart. For this demo, we assume prompt might contain it
                # or we use a fallback.
                logger.info(f"Closer: Fetching context for customer {customer_id}")
            
            system_prompt = f"""You are the AGENTIC Closer. 
Your specialty is secure checkout and order finalization.
User Intent: {prompt}
Customer ID: {customer_id}

Rules:
1. If the user wants to 'checkout' or 'buy', inform them of the steps: Address -> Method -> Payment.
2. If they provide a cart ID, offer to generate a Stripe Checkout URL using `create_stripe_checkout`.
3. If they want to 'place order', warn them that payment MUST be confirmed first.

Task: Provide a clear, actionable transactional response. Recommend the next tool-based action."""

            response = self.client.chat.completions.create(
                model=self.model,
                messages=[{"role": "system", "content": system_prompt}],
                temperature=0.3
            )
            
            return {
                "specialist": "Closer",
                "response": response.choices[0].message.content,
                "status": "ready_for_action"
            }
        except Exception as e:
            logger.error(f"Closer Error: {str(e)}")
            return {"error": f"Closer failed to process transaction: {str(e)}"}
