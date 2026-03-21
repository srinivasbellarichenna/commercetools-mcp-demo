from orchestrator import mcp, proxy
import uvicorn
from starlette.responses import JSONResponse
import logging

logger = logging.getLogger("run-sse")

async def list_tools_endpoint(request):
    """
    Health endpoint to verify tool discovery status.
    Returns the warm tool registry in the OpenAI-compatible format.
    """
    tools = await proxy.get_tools()
    return JSONResponse({
        "status": "synchronized" if tools else "pending",
        "count": len(tools),
        "tools": [t["function"]["name"] for t in tools]
    })

async def debug_analyst_endpoint(request):
    """
    Definitive test: Call the analyst in-process and check logs.
    """
    from orchestrator import analyst
    prompt = request.query_params.get("prompt", "Find me a green chair")
    customer_id = request.query_params.get("customer_id", None)
    result = await analyst.handle_discovery(prompt, customer_id)
    return JSONResponse(result)

async def debug_closer_endpoint(request):
    """
    Definitive test: Call the closer in-process and check logs.
    """
    from orchestrator import closer
    prompt = request.query_params.get("prompt", "Checkout my cart_12345 for john@example.com")
    customer_id = request.query_params.get("customer_id", None)
    result = await closer.handle_transaction(prompt, customer_id)
    return JSONResponse(result)

if __name__ == "__main__":
    # Get the SSE app instance from the FastMCP method
    app = mcp.sse_app()
    
    # Register the startup hook to warm the MCP session
    from orchestrator import initialize_agency
    app.add_event_handler("startup", initialize_agency)
    
    # Add the health endpoint
    app.add_route("/tools/list", list_tools_endpoint, methods=["GET"])
    app.add_route("/debug/analyst", debug_analyst_endpoint, methods=["GET"])
    app.add_route("/debug/closer", debug_closer_endpoint, methods=["GET"])
    
    logger.info("Starting Federated Agents SSE Server on port 8000...")
    uvicorn.run(app, host="0.0.0.0", port=8000)
