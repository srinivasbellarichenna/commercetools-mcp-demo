import asyncio
import logging
import os
import sys
import json
from mcp.server.fastmcp import FastMCP

# Ensure local modules (shared, specialists) are findable when run via Claude Desktop
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from specialists.analyst import Analyst
from specialists.closer import Closer
from shared.mcp_proxy import MCPProxy

# Configure logging - directed to both stderr (for Claude) and the main container stdout (for Docker logs viewer)
handlers = [logging.StreamHandler(sys.stderr)]
if os.path.exists("/proc/1/fd/1"):
    try:
        handlers.append(logging.FileHandler("/proc/1/fd/1"))
    except Exception:
        pass

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=handlers
)
logger = logging.getLogger("orchestrator")

# Initialize FastMCP
mcp = FastMCP("Federated-Agents-Orchestrator")

# Shared state and tools - will be initialized via run_sse.py or manually
proxy = MCPProxy()
analyst = Analyst(proxy)
closer = Closer(proxy)

async def initialize_agency():
    """
    Called at app startup to warm the MCP session.
    """
    await proxy.initialize()
    logger.info("Agency: Persistent tools warmed and ready.")

@mcp.tool()
async def commerce_agent_request_v2(prompt: str, customer_id: str = "") -> str:
    """
    The main entry point for the Agentic Agency. It triages the request 
    and delegates to the specialized Analyst or Closer agents.
    
    Args:
        prompt: The user's request (e.g. "Find me a green chair" or "Check out my cart")
        customer_id: The ID of the current customer (optional)
    """
    safe_customer_id = customer_id if customer_id else None
    logger.info(f"Agency: Received high-level request: {prompt} (Customer: {safe_customer_id})")
    
    # Simple Triage Logic
    prompt_lower = prompt.lower()
    
    if any(k in prompt_lower for k in ["find", "search", "show", "collection", "recommend", "look"]):
        logger.info("Agency Status: Delegating to Analyst Specialist...")
        result = await analyst.handle_discovery(prompt, safe_customer_id)
        return json.dumps(result, indent=2)
        
    elif any(k in prompt_lower for k in ["buy", "checkout", "cart", "order", "purchase", "pay"]):
        logger.info("Agency Status: Delegating to Closer Specialist...")
        result = await closer.handle_transaction(prompt, safe_customer_id)
        return json.dumps(result, indent=2)
        
    return "I am the Agentic Agency Orchestrator. Please specify if you want to discover products or proceed with a purchase."

if __name__ == "__main__":
    # When run directly (e.g. by Claude Desktop via stdio), mcp.run() handles everything.
    # In Docker, run_sse.py imports mcp and runs it via uvicorn.
    mcp.run()
