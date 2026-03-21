import logging
from shared.logging_config import setup_logging

setup_logging()
logger = logging.getLogger("orchestrator-v2")
from shared.mcp_proxy import MCPProxy
from engine.state_manager import StateManager
from engine.step_executor import StepExecutor
from engine.error_handler import ErrorHandler
from engine.execution_engine import ExecutionEngine
from engine.planner import Planner
from capabilities.product_capability import ProductCapability
from capabilities.cart_capability import CartCapability
from capabilities.checkout_capability import CheckoutCapability

logger = logging.getLogger("orchestrator-v2")

# Singletons
proxy = MCPProxy()
state_manager = StateManager()
error_handler = ErrorHandler()
planner = Planner()

# Capabilities
product_cap = ProductCapability(proxy)
cart_cap = CartCapability(proxy)
checkout_cap = CheckoutCapability(proxy)

# Step Executor
executor = StepExecutor(product_cap, cart_cap, checkout_cap)

# Execution Engine
engine = ExecutionEngine(state_manager, executor, error_handler, planner)

async def initialize_v2():
    """
    Initializes the MCP proxy (if needed) and any other V2 resources.
    """
    logger.info("Initializing Federated Agents V2...")
    # MCPProxy doesn't need explicit init if it lazy-loads, 
    # but we might want to warm it up.
    try:
        await proxy.get_tools()
        logger.info("MCP Proxy warmed up.")
    except Exception as e:
        logger.error(f"Failed to warm up MCP Proxy: {e}")
