import asyncio
import os
import sys
import logging
import json

# Add current dir to path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from shared.mcp_proxy import MCPProxy
from engine.state_manager import StateManager
from engine.step_executor import StepExecutor
from engine.error_handler import ErrorHandler
from engine.execution_engine import ExecutionEngine
from orchestrator_v2 import engine, planner, initialize_v2
from capabilities.product_capability import ProductCapability
from capabilities.cart_capability import CartCapability
from capabilities.checkout_capability import CheckoutCapability

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("federated-agents-v2")

async def run_v2_demo(prompt: str, session_id: str = "demo-session"):
    # 1. Initialize logic
    proxy = MCPProxy()
    # No explicit initialize needed for v2 shim if using request-scoped sessions
    
    product_cap = ProductCapability(proxy)
    cart_cap = CartCapability(proxy)
    checkout_cap = CheckoutCapability(proxy)
    
    executor = StepExecutor(product_cap, cart_cap, checkout_cap)
    state_manager = StateManager()
    error_handler = ErrorHandler()
    engine = ExecutionEngine(state_manager, executor, error_handler)
    planner = Planner()
    
    # 2. Create Plan
    plan = await planner.create_plan(prompt)
    
    # 3. Run Engine (Async Stream)
    print(f"\n--- GOAL: {plan.goal} ---\n")
    async for event in engine.run_async(session_id, plan):
        print(f"EVENT: {event['type']} | {event.get('step', '')} | {json.dumps(event.get('data', {}))}")

if __name__ == "__main__":
    prompt = "Find a red chair and add it to my cart"
    if len(sys.argv) > 1:
        prompt = " ".join(sys.argv[1:])
    
    asyncio.run(run_v2_demo(prompt))
