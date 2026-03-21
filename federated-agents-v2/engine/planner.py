import os
import json
import logging
import uuid
from typing import Optional, List
from openai import AsyncOpenAI
from models.plan import Plan
from models.step import Step

import logging

logger = logging.getLogger("planner")

ALLOWED_ACTIONS = {
    "search_products",
    "select_product",
    "add_to_cart",
    "checkout",
    "present_options"
}

class Planner:
    def __init__(self):
        self.client = AsyncOpenAI(
            base_url=os.getenv("LM_STUDIO_URL", "http://host.docker.internal:1234/v1"),
            api_key="lm-studio"
        )
        self.model = os.getenv("MODEL_NAME", "qwen3-4b-instruct-2507-mlx")

    async def create_plan(self, user_input: str, state: dict = None) -> Plan:
        """
        Calls the LLM to generate a structured JSON plan based on user input.
        """
        logger.info(f"Generating plan for: {user_input}")
        
        # Trimmed production prompt for low latency
        system_prompt = (
            "You are a planner. Return ONLY JSON.\n\n"
            "FORMAT:\n"
            "{\n"
            "  \"goal\": \"string\",\n"
            "  \"steps\": [\n"
            "    {\"id\": \"1\", \"action\": \"search_products\", \"inputs\": {\"query\": \"real search term\"}}\n"
            "  ]\n"
            "}\n\n"
            "ACTIONS: search_products, select_product, add_to_cart, checkout.\n"
            "RULES: Always return at least 1 step. No extra text."
        )

        try:
            import asyncio
            response_obj = await asyncio.wait_for(
                self.client.chat.completions.create(
                    model=self.model,
                    messages=[
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": f"Request: {user_input}\nReturn JSON plan."}
                    ],
                    response_format={"type": "text"},
                    temperature=0.0
                ),
                timeout=60 # Increased timeout for local inference
            )
            
            content = response_obj.choices[0].message.content
            
            # --- GUARD 4.1: Empty Output Guard ---
            if not content or not content.strip():
                logger.error("Planner returned empty response. Triggering fallback.")
                return self.fallback_plan(user_input)

            logger.debug(f"Planner raw response: {content}")
            
            # Clean up content if LLM wrapped it in markdown
            if "```json" in content:
                content = content.split("```json")[1].split("```")[0].strip()
            elif "```" in content:
                content = content.split("```")[1].split("```")[0].strip()
            
            # --- GUARD 4.2: JSON Parse Guard ---
            try:
                raw_data = json.loads(content)
            except Exception as e:
                logger.error(f"Failed to parse planner JSON: {e}. Content: {content}")
                return self.fallback_plan(user_input)
            
            # --- GUARD 4.3: Structure Validation ---
            if not isinstance(raw_data, dict) or "steps" not in raw_data or not raw_data["steps"]:
                logger.error(f"Invalid plan structure: {raw_data}")
                return self.fallback_plan(user_input)

            valid_steps = []
            for s in raw_data.get("steps", []):
                action = s.get("action")
                if action not in {"search_products", "select_product", "add_to_cart", "checkout"}:
                    logger.warning(f"Planner emitted blocked action: {action}. Skipping.")
                    continue
                
                # Use a dummy ID if missing
                if "id" not in s:
                    s["id"] = str(uuid.uuid4())[:8]
                
                valid_steps.append(Step(**s))
            
            if not valid_steps:
                 return self.fallback_plan(user_input)

            plan = Plan(goal=raw_data.get("goal", "unknown"), steps=valid_steps)
            return plan

        except Exception as e:
            logger.error(f"Planner Hard Failure or Timeout: {e}. Triggering fallback.")
            return self.fallback_plan(user_input)

    def fallback_plan(self, user_input: str) -> Plan:
        """
        Returns a minimal viable plan when the LLM fails.
        """
        return Plan(
            goal="fallback_search",
            steps=[
                Step(id="1", action="search_products", inputs={"query": user_input})
            ]
        )
