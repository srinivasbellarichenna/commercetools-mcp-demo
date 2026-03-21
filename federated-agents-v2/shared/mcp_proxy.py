import os
import json
import httpx
import logging
import asyncio
from typing import Dict, Any, List, Optional
from mcp import ClientSession
from mcp.client.sse import sse_client
from shared.mcp_contract import map_and_validate_args, MCPContractException

import contextlib

logger = logging.getLogger("mcp-proxy")

class MCPProxy:
    """
    Bridge that connects Federated Agents to the foundational-mcp-server using the MCP protocol over SSE.
    """
    def __init__(self, sse_url: Optional[str] = None):
        self.sse_url = sse_url or os.getenv("MCP_SERVER_SSE_URL", "http://foundational-mcp-server:8000/sse")
        logger.info(f"MCP-Proxy: Request-scoped proxy initialized (Target: {self.sse_url})")

    async def initialize(self):
        """Shim for compatibility with orchestrator startup."""
        pass

    @contextlib.asynccontextmanager
    async def get_session(self):
        """Creates a fresh session for each operation to avoid anyio scope affinity issues."""
        async with sse_client(self.sse_url) as (read, write):
            async with ClientSession(read, write) as session:
                await session.initialize()
                yield session

    async def get_tools(self) -> List[Dict[str, Any]]:
        """Fetch tools wrapped in OpenAI function format."""
        try:
            async with self.get_session() as session:
                result = await session.list_tools()
                tools = [t.model_dump() for t in result.tools]
                
                return [
                    {
                        "type": "function",
                        "function": {
                            "name": t["name"],
                            "description": t.get("description", ""),
                            "parameters": t.get("inputSchema", {"type": "object", "properties": {}})
                        }
                    }
                    for t in tools
                ]
        except Exception as e:
            logger.error(f"MCP-Proxy: Failed to get tools - {str(e)}")
            return []

    async def call_tool(self, tool_name: str, arguments: Dict[str, Any]) -> Dict[str, Any]:
        """Executes a tool call on a fresh session with strict contract checking."""
        logger.info(f"MCP-Proxy: Preparing call to '{tool_name}'...")
        try:
            # 1. Map & Validate Strict Requirements
            try:
                mapped_arguments = map_and_validate_args(tool_name, arguments)
            except MCPContractException as ce:
                logger.error(f"[MCP BOUNDARY REJECTION] {ce}")
                return {"error": str(ce)}
                
            # 2. Input Echo Logging (Crucial Boundary Safety)
            logger.info(f"[MCP CALL ECHO] action: '{tool_name}' | original_args: {arguments} | mapped_args: {mapped_arguments}")
            
            async with self.get_session() as session:
                result = await session.call_tool(tool_name, mapped_arguments)
                logger.info(f"MCP-Proxy: Result for '{tool_name}' received.")
                
                if hasattr(result, 'content'):
                    # MCP results often contain a list of content items (Text, Image, etc.)
                    full_content = ""
                    for item in result.content:
                        if hasattr(item, 'text'):
                            full_content += item.text
                    
                    logger.info(f"MCP-Proxy: Raw content length: {len(full_content)}")
                    logger.debug(f"MCP-Proxy: Raw content snippet: {full_content[:500]}")
                    try:
                        parsed = json.loads(full_content)
                        logger.info(f"MCP-Proxy: Successfully parsed JSON. Type: {type(parsed)}")
                        return parsed
                    except Exception as e:
                        logger.warning(f"MCP-Proxy: Failed to parse JSON: {e}")
                        return {"raw": full_content}
                
                return {"result": str(result)}
        except Exception as e:
            logger.error(f"MCP-Proxy: Call Error: {str(e)}")
            return {"error": f"Tool execution failed: {str(e)}"}
