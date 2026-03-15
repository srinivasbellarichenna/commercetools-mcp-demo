import logging
import os
import json
from openai import OpenAI
from shared.mcp_proxy import MCPProxy

logger = logging.getLogger("analyst")

class Analyst:
    """
    Discovery Specialist: Handles product search, detail retrieval, and discovery logic.
    """
    def __init__(self, proxy: MCPProxy):
        self.proxy = proxy
        self.client = OpenAI(
            base_url=os.getenv("LM_STUDIO_URL", "http://host.docker.internal:1234/v1"),
            api_key="lm-studio"
        )
        self.model = os.getenv("MODEL_NAME", "meta-llama-3-8b-instruct")

    async def handle_discovery(self, prompt: str, customer_id: str = None):
        """
        Main reasoning loop for discovery requests.
        """
        logger.info(f"Analyst Reasoning started for: {prompt}")
        
        # Step 1: Initial tool call to get a broad set of products
        # In a more advanced agent, the LLM would decide which tool to call.
        # Here we do a foundational hop to get context.
        try:
            raw_products = await self.proxy.call_tool("get_collection", {"limit": 20})
            
            # Step 2: Use LLM to 'curate' the results based on the prompt
            system_prompt = f"""You are the AGENTIC Analyst. 
Your specialty is product discovery and curation.
The user wants: {prompt}
Available products: {json.dumps(raw_products.get('results', []), indent=2)}

Task: Identify the products that BEST match the user's intent. 
Provide a professional, persuasive response with SKU details for the recommended items.
If no matches, suggest alternatives from the available collection."""

            response = self.client.chat.completions.create(
                model=self.model,
                messages=[{"role": "system", "content": system_prompt}],
                temperature=0.7
            )
            
            return {
                "specialist": "Analyst",
                "response": response.choices[0].message.content,
                "data_context": raw_products
            }
        except Exception as e:
            logger.error(f"Analyst Error: {str(e)}")
            return {"error": f"Analyst failed to process discovery: {str(e)}"}
