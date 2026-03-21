from mcp.server.fastmcp import FastMCP
from orchestrator_v2 import engine

# Define the FastMCP server for Federated Agents V2
mcp = FastMCP("Federated Agents V2")

@mcp.tool()
async def commerce_agent_v2(prompt: str, session_id: str = "default-user"):
    """
    Run the autonomous Federated Agents V2 engine for a shopping request.
    This uses planning, execution, and evaluation cycles.
    """
    import logging
    logger = logging.getLogger("mcp-orchestrator")
    logger.info(f"Starting V2 engine for session: {session_id}")
    
    from engine.planner import Planner
    planner = Planner()
    plan = await planner.create_plan(prompt)
    
    final_state = None
    async for event in engine.run_async(session_id, plan):
        if event["type"] == "DONE":
            final_state = event["data"]
    
    return {
        "status": "success",
        "session_id": session_id,
        "final_state": final_state
    }
