from shared.mcp_proxy import MCPProxy
import logging

logger = logging.getLogger("base-capability")

class BaseCapability:
    def __init__(self, proxy: MCPProxy):
        self.proxy = proxy

    async def _call_tool(self, tool_name: str, arguments: dict):
        return await self.proxy.call_tool(tool_name, arguments)
