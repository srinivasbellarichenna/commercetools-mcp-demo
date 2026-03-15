import asyncio
import os
import json
import logging
import sys
import httpx
from openai import OpenAI
from mcp import ClientSession, StdioServerParameters
from mcp.client.sse import sse_client
from dotenv import load_dotenv

load_dotenv()

# Configure logging for artisanal transparency
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stdout
)
logger = logging.getLogger("mcp-client")

# Environment variables
LM_STUDIO_URL = os.getenv("LM_STUDIO_URL", "http://host.docker.internal:1234/v1")
MODEL_NAME = os.getenv("MODEL_NAME", "meta-llama-3-8b-instruct")
MCP_SERVER_SSE_URL = os.getenv("MCP_SERVER_SSE_URL", "http://mcp-server:8087/sse")
DEFINITIVE_SUCCESS = os.getenv("DEFINITIVE_SUCCESS", "false").lower() == "true"

client = OpenAI(base_url=LM_STUDIO_URL, api_key="lm-studio")

async def run_artisanal_orchestration():
    """
    Orchestrate the user journey using the MCP server and LLM.
    """
    # Mandatory settling delay for microservices
    logger.info("Awaiting microservice ecosystem stabilization (45s)...")
    await asyncio.sleep(45)
    
    logger.info("Starting Artisanal MCP Client Orchestration...")
    logger.info(f"Connecting to LLM at: {LM_STUDIO_URL}")
    logger.info(f"Connecting to MCP at: {MCP_SERVER_SSE_URL}")

    try:
        async with sse_client(MCP_SERVER_SSE_URL) as (read_stream, write_stream):
            async with ClientSession(read_stream, write_stream) as session:
                await session.initialize()
                logger.info("Connected to KESTREL MCP Server.")

                if DEFINITIVE_SUCCESS:
                    logger.info("--- ENTERING DEFINITIVE SUCCESS MODE ---")
                    # Step 1: Discover real product
                    resp = await session.call_tool("get_collection", {"limit": 1})
                    product_data = json.loads(resp.content[0].text)["results"][0]
                    product_id = product_data["id"]
                    sku = product_data.get("suggested_sku", "default-sku")
                    logger.info(f"Discovered Piece: {product_id} (SKU: {sku})")
                    
                    if sku == "default-sku" or sku == "missing-sku":
                        logger.error("DANGER: Hallucinated or missing SKU detected. Acquisition halted.")
                        return

                    # Step 2: Initialize Bag
                    resp = await session.call_tool("initialize_bag", {"currency": "EUR", "country": "DE"})
                    bag_data = json.loads(resp.content[0].text)
                    bag_id = bag_data["id"]
                    version = bag_data["version"]
                    logger.info(f"Initialized Bag: {bag_id} (Version: {version})")
                    
                    # Step 3: Commit to Bag
                    resp = await session.call_tool("commit_to_bag", {"cart_id": bag_id, "sku": sku, "quantity": 1})
                    bag_data = json.loads(resp.content[0].text)
                    version = bag_data["version"]
                    logger.info(f"Committed Piece. New Version: {version}")
                    
                    # Step 4: Set Destination
                    resp = await session.call_tool("set_shipping_address", {
                        "cart_id": bag_id, 
                        "first_name": "Artisanal",
                        "last_name": "Curator",
                        "street": "Heritage Lane 1",
                        "city": "Berlin",
                        "postal_code": "10117",
                        "country": "DE"
                    })
                    bag_data = json.loads(resp.content[0].text)
                    version = bag_data["version"]
                    logger.info(f"Set Destination. New Version: {version}")
                    
                    # Step 5: Place Order
                    resp = await session.call_tool("place_order", {"cart_id": bag_id, "version": version})
                    order_data = json.loads(resp.content[0].text)
                    order_id = order_data["id"]
                    logger.info(f"SUCCESS! Order ID: {order_id}")
                    
                    # Graceful pause to ensure logs are captured
                    await asyncio.sleep(2)
                    return

                # Normal LLM-driven flow
                system_prompt = """You are the curator of the KESTREL Artisanal Ledger.
Your mission is to guide the user through a data-driven acquisition journey.
CRITICAL RULES:
1. NEVER hallucinate IDs (product IDs, cart IDs, order IDs).
2. ALWAYS use real IDs discovered through tool calls.
3. If an ID has a dynamic part (like a UUID), parse it EXACTLY from the tool output.
4. Product ids look like UUIDs (e.g., '7894c287-6440-4883-8545-9571cb3d5514').
5. To add to bag, use the `suggested_sku` field from `get_collection` results.
6. The user journey is: get_collection -> initialize_bag -> commit_to_bag -> set_shipping_address -> place_order.
7. Once you have an Order ID, provide it to the user as final proof of the heritage acquisition."""

                messages = [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": "I want to place an artisanal order for a piece from the collection."}
                ]

                for cycle in range(10):
                    logger.info(f"Orchestration Cycle {cycle + 1}/10...")
                    
                    # Call LLM
                    logger.info("LLM is reasoning...")
                    response = client.chat.completions.create(
                        model=MODEL_NAME,
                        messages=messages,
                        tools=[{
                            "type": "function",
                            "function": {
                                "name": name,
                                "description": tool.description,
                                "parameters": tool.input_schema
                            }
                        } for name, tool in session.list_tools().tools],
                        timeout=120.0
                    )

                    message = response.choices[0].message
                    messages.append(message)

                    if not message.tool_calls:
                        logger.info(f"Curator Response: {message.content}")
                        break

                    for tool_call in message.tool_calls:
                        tool_name = tool_call.function.name
                        tool_args = json.loads(tool_call.function.arguments)
                        
                        logger.info(f"Executing Curatorial Tool: {tool_name} with {tool_args}")
                        
                        # Execute individual tool call
                        result = await session.call_tool(tool_name, tool_args)
                        result_text = result.content[0].text
                        
                        messages.append({
                            "role": "tool",
                            "tool_call_id": tool_call.id,
                            "name": tool_name,
                            "content": result_text
                        })
                        
                        logger.info(f"Tool Result: {result_text[:200]}...")

    except Exception as e:
        logger.error(f"Failed to establish SSE connection: {str(e)}")
    
    logger.info("Artisanal Orchestration Cycle Finished.")

if __name__ == "__main__":
    asyncio.run(run_artisanal_orchestration())
