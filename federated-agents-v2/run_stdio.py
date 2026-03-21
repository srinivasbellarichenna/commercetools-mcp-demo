import sys
import os
import logging
from orchestrator import mcp
from shared.logging_config import setup_logging

# Silence all stdout/stderr for protocol safety
setup_logging()

if __name__ == "__main__":
    # Ensure dependencies are warmed up
    from orchestrator_v2 import initialize_v2
    import asyncio
    
    # Run in stdio mode (default for mcp.run)
    # FastMCP handles the stdio loop
    mcp.run()
