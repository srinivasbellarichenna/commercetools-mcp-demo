import os
import uvicorn
import logging
import json
import asyncio
from starlette.responses import JSONResponse, StreamingResponse
from orchestrator_v2 import engine, planner, initialize_v2, proxy

# Use the existing mcp instance if we want to keep the SSE-MCP bridge
# or just use plain Starlette/FastAPI if the Agent was the main thing.
# The user's run_sse.py used mcp.sse_app()
from orchestrator import mcp

from starlette.middleware import Middleware
from starlette.middleware.base import BaseHTTPMiddleware

class SimpleAuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        if request.url.path in ["/stream", "/tools/list"]:
            api_key = request.headers.get("X-API-KEY")
            if api_key != os.getenv("AGENT_API_KEY", "prod-secret-123"):
                 return JSONResponse({"error": "Unauthorized"}, status_code=401)
        return await call_next(request)

logger = logging.getLogger("run-sse-v2")

async def sse_stream(prompt: str, session_id: str):
    """
    Generator for SSE events from the Execution Engine.
    """
    try:
        # 1. Establish initial intent in Working Memory
        state = engine.state_manager.get_state(session_id)
        state.intent["query"] = prompt

        # 2. Create Plan
        plan = await planner.create_plan(prompt)
        
        # 2. Run Engine
        async for event in engine.run_async(session_id, plan):
            # Format as SSE event
            data = json.dumps(event)
            yield f"data: {data}\n\n"
            
    except Exception as e:
        logger.error(json.dumps({"event": "SSE_ERROR", "error": str(e), "session_id": session_id}))
        yield f"data: {json.dumps({'type': 'ERROR', 'data': {'error': str(e)}})}\n\n"

async def stream_endpoint(request):
    """
    New Federated Agents V2 Stream Endpoint.
    """
    prompt = request.query_params.get("prompt")
    session_id = request.query_params.get("session_id", "default-user")
    
    if not prompt:
        return JSONResponse({"error": "Missing prompt"}, status_code=400)
    
    # Observability: Log request
    logger.info(json.dumps({"event": "REQUEST_RECEIVED", "prompt": prompt, "session_id": session_id}))
    
    return StreamingResponse(sse_stream(prompt, session_id), media_type="text/event-stream")

async def list_tools_endpoint(request):
    tools = await proxy.get_tools()
    return JSONResponse({
        "status": "synchronized" if tools else "pending",
        "count": len(tools),
        "tools": [t["function"]["name"] for t in tools]
    })

if __name__ == "__main__":
    # Add security middleware
    app = mcp.sse_app()
    app.add_middleware(SimpleAuthMiddleware)
    
    # Register V2 routes
    app.add_route("/stream", stream_endpoint, methods=["GET"])
    app.add_route("/tools/list", list_tools_endpoint, methods=["GET"])
    
    # Use V2 initialization
    app.add_event_handler("startup", initialize_v2)
    
    # SILENCE: Ensure logs go to stderr or are suppressed for protocol safety
    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="error")
