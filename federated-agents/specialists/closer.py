import logging
import os
import json
from typing import Optional
from openai import AsyncOpenAI
from shared.mcp_proxy import MCPProxy

logger = logging.getLogger("closer")

class Closer:
    """
    Transaction Specialist: Handles secure checkout, address setting, and order placement.
    """
    def __init__(self, proxy: MCPProxy):
        self.proxy = proxy
        self.client = AsyncOpenAI(
            base_url=os.getenv("LM_STUDIO_URL", "http://host.docker.internal:1234/v1"),
            api_key="lm-studio"
        )
        self.model = os.getenv("MODEL_NAME", "qwen3-4b-instruct-2507-mlx")

    async def handle_transaction(self, prompt: str, customer_id: Optional[str] = None):
        """
        Native OpenAI tool-calling reasoning loop for transactions with dynamic tool registry.
        """
        logger.info(f"Closer: Processing transaction request: {prompt}")
        
        # 1. Fetch dynamic tool definitions (they will be warm from the persistent proxy)
        tools = await self.proxy.get_tools()
        if not tools:
             logger.warning("Closer: Tools not warm, attempting immediate initialization...")
             await self.proxy.initialize()
             tools = await self.proxy.get_tools()

        # DEEP DEBUG: Log exactly what's in our registry
        registered_names = [t["function"]["name"] for t in tools]
        logger.info(f"Closer: Registered tools in this turn: {registered_names}")

        messages = [
            {"role": "system", "content": "AGENT_ID: CLOSER_V2. You are the AGENTIC Closer. You handle checkout. NEVER hallucinate. You MUST call tools to verify cart state, set addresses, or place orders. Do not invent Cart IDs."},
            {"role": "user", "content": f"Context: Customer={customer_id}. User says: {prompt}"}
        ]

        total_tool_calls = 0
        for step in range(15):
            # Determine if we should force a specific tool call on turn 0
            forced_tool = None
            is_action_request = any(k in prompt.lower() for k in ["buy", "checkout", "add", "cart", "order", "status", "pay", "stripe"])
            
            if step == 0 and is_action_request and tools:
                if "cart_" in prompt.lower() or "id:" in prompt.lower():
                    forced_tool = "get_cart"
                elif "initialize" in prompt.lower() or "create cart" in prompt.lower():
                    forced_tool = "initialize_cart"
                elif "sku" in prompt.lower():
                    forced_tool = "add_to_cart"

            # Construct the specialized tool choice
            if forced_tool and any(t["function"]["name"] == forced_tool for t in tools):
                current_tool_choice = "required"
                logger.info(f"Closer: Forcing first tool call to '{forced_tool}' using 'required'.")
                messages.append({"role": "user", "content": f"Please start by using the '{forced_tool}' tool."})
            else:
                current_tool_choice = "required" if (step == 0 and is_action_request and tools) else "auto"
            
            try:
                response = await self.client.chat.completions.create(
                    model=self.model,
                    messages=messages,
                    tools=tools if tools else None,
                    tool_choice=current_tool_choice,
                    temperature=0.0
                )
            except Exception as e:
                logger.error(f"Closer API Error: {str(e)}")
                response = await self.client.chat.completions.create(
                    model=self.model,
                    messages=messages,
                    tools=tools if tools else None,
                    tool_choice="auto",
                    temperature=0.0
                )
            
            message = response.choices[0].message
            
            # TRACE: Log the complete LLM response
            logger.info(f"Closer Step {step} LLM RAW: content='{message.content}', tools={message.tool_calls}")

            if step == 0 and is_action_request and not message.tool_calls:
                logger.info("Closer: Model skipped tool call. Retrying with explicit instruction.")
                messages.append({"role": "assistant", "content": message.content if message.content else "Processing checkout..."})
                messages.append({"role": "user", "content": "You MUST call a tool to perform this action. Use the provided tools."})
                continue 

            messages.append(message)

            if not message.tool_calls:
                return {
                    "specialist": "Closer",
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
                
                # Hallucination Guard
                if tool_name not in registered_names:
                    logger.error(f"Closer: CRITICAL NAME MISMATCH! Received '{raw_tool_name}' (normalized to '{tool_name}') but it's not in registered tools: {registered_names}")
                    observation = {"error": f"Unknown tool: {tool_name}. Available tools: {registered_names}"}
                else:
                    try:
                        tool_args = json.loads(tool_call.function.arguments)
                    except Exception as je:
                        logger.error(f"Failed to parse tool arguments: {je}")
                        tool_args = {}
                    
                    logger.info(f"Closer executing tool: {tool_name}({tool_args})")
                    observation = await self.proxy.call_tool(tool_name, tool_args)
                
                # Observation compression: limit size for small models
                obs_str = json.dumps(observation)
                if len(obs_str) > 2000:
                    logger.info(f"Closer: Truncating large observation for {tool_name} ({len(obs_str)} chars)")
                    observation = {"notice": "Result truncated for brevity", "data": obs_str[:2000] + "... [TRUNCATED]"}
                
                messages.append({
                    "role": "tool",
                    "tool_call_id": tool_call.id,
                    "name": raw_tool_name, # Keep original name for the history
                    "content": json.dumps(observation)
                })

        return {"error": "Closer reached maximum tool calling steps."}
