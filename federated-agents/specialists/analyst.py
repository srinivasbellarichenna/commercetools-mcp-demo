import logging
import os
import json
from openai import AsyncOpenAI
from shared.mcp_proxy import MCPProxy

logger = logging.getLogger("analyst")

class Analyst:
    """
    Discovery Specialist: Handles product search, detail retrieval, and discovery logic.
    """
    def __init__(self, proxy: MCPProxy):
        self.proxy = proxy
        self.client = AsyncOpenAI(
            base_url=os.getenv("LM_STUDIO_URL", "http://host.docker.internal:1234/v1"),
            api_key="lm-studio"
        )
        self.model = os.getenv("MODEL_NAME", "qwen3-4b-instruct-2507-mlx")

    async def handle_discovery(self, prompt: str, customer_id: str = None):
        """
        Native OpenAI tool-calling reasoning loop for discovery with dynamic tool registry.
        """
        logger.info(f"Analyst: Processing discovery request: {prompt}")
        
        # 1. Fetch dynamic tool definitions (they will be warm from the persistent proxy)
        tools = await self.proxy.get_tools()
        if not tools:
            logger.warning("Analyst: Tools not warm, attempting immediate initialization...")
            await self.proxy.initialize()
            tools = await self.proxy.get_tools()

        # 2. Intelligent Routing Check
        forced_tool = None
        is_discovery = any(k in prompt.lower() for k in ["find", "show", "search", "list", "collection", "get", "product", "piece"])
        
        if is_discovery and tools:
            if any(x in prompt.lower() for x in ["piece_", "sku", "id:"]):
                forced_tool = "get_piece_detail"
            elif "@" in prompt:
                forced_tool = "get_customer_by_email"
            elif any(t["function"]["name"] == "search_products" for t in tools):
                # Prefer search_products for discovery if available
                forced_tool = "search_products"

        # DEEP DEBUG: Log exactly what's in our registry
        registered_names = [t["function"]["name"] for t in tools]
        logger.info(f"Analyst: Registered tools in this turn: {registered_names}")
        
        system_prompt = (
            "AGENT_ID: ANALYST_V2. You are the AGENTIC Analyst. "
            "Your primary goal is to help customers find products. "
            "DISCOVERY: ALWAYS use 'search_products' first for keyword-based requests. "
            "Use 'get_collection' only for broad listing. "
            "ALWAYS check inventory/availability via 'get_piece_detail' before recommending."
        )
        logger.info(f"Analyst: Using System Prompt: {system_prompt}")

        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": f"User prompt: {prompt}"}
        ]

        total_tool_calls = 0
        for step in range(15):
            # Enforce tool choice on the very first step if it's a discovery query
            if forced_tool and any(t["function"]["name"] == forced_tool for t in tools):
                current_tool_choice = "required"
                logger.info(f"Analyst: Forcing first tool call to '{forced_tool}' using 'required'.")
                messages.append({"role": "user", "content": f"Please start by using the '{forced_tool}' tool."})
            else:
                current_tool_choice = "required" if (step == 0 and is_discovery and tools) else "auto"
            
            try:
                response = await self.client.chat.completions.create(
                    model=self.model,
                    messages=messages,
                    tools=tools if tools else None,
                    tool_choice=current_tool_choice,
                    temperature=0.0
                )
            except Exception as e:
                logger.error(f"Analyst API Error: {str(e)}")
                # Retrying with auto
                response = await self.client.chat.completions.create(
                    model=self.model,
                    messages=messages,
                    tools=tools if tools else None,
                    tool_choice="auto",
                    temperature=0.0
                )
            
            message = response.choices[0].message
            
            if step == 0 and is_discovery and not message.tool_calls:
                logger.info("Analyst: Model skipped tool call. Retrying with explicit instruction.")
                messages.append({"role": "assistant", "content": message.content if message.content else "Searching..."})
                messages.append({"role": "user", "content": "You MUST call a tool to get the actual data. Do not hallucinate."})
                continue 

            messages.append(message)

            if not message.tool_calls:
                return {
                    "specialist": "Analyst",
                    "response": message.content,
                    "status": "success",
                    "total_tool_calls": total_tool_calls
                }

            for tool_call in message.tool_calls:
                total_tool_calls += 1
                raw_tool_name = tool_call.function.name
                
                # Flexible Matching: Remove common prefixes (mcp:, federated-agents:)
                tool_name = raw_tool_name
                if ":" in tool_name:
                    tool_name = tool_name.split(":")[-1]
                
                # Hallucination Guard with DEEP DEBUGGING
                if tool_name not in registered_names:
                    logger.error(f"Analyst: CRITICAL NAME MISMATCH! Received '{raw_tool_name}' (normalized to '{tool_name}') but it's not in registered tools: {registered_names}")
                    observation = {"error": f"Unknown tool: {tool_name}. Available tools: {registered_names}"}
                else:
                    try:
                        tool_args = json.loads(tool_call.function.arguments)
                    except Exception as je:
                        logger.error(f"Failed to parse tool arguments: {je}")
                        tool_args = {}
                    
                    logger.info(f"Analyst executing tool: {tool_name}({tool_args})")
                    observation = await self.proxy.call_tool(tool_name, tool_args)
                
                # Observation compression: limit size for small models
                obs_str = json.dumps(observation)
                if len(obs_str) > 2000:
                    logger.info(f"Analyst: Truncating large observation for {tool_name} ({len(obs_str)} chars)")
                    observation = {"notice": "Result truncated for brevity", "data": obs_str[:2000] + "... [TRUNCATED]"}
                
                messages.append({
                    "role": "tool",
                    "tool_call_id": tool_call.id,
                    "name": raw_tool_name, # Keep original name for the history
                    "content": json.dumps(observation)
                })

        return {"error": "Analyst reached maximum tool calling steps."}
