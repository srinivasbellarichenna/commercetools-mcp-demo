import asyncio
import unittest
from unittest.mock import MagicMock, AsyncMock
from models.step import Step
from models.plan import Plan
from engine.execution_engine import ExecutionEngine
from engine.state_manager import StateManager
from engine.step_executor import StepExecutor
from engine.error_handler import ErrorHandler

from engine.planner import Planner

class TestExecutionEngine(unittest.TestCase):
    def setUp(self):
        self.state_manager = StateManager()
        self.executor = MagicMock(spec=StepExecutor)
        self.error_handler = MagicMock(spec=ErrorHandler)
        self.planner = MagicMock(spec=Planner)
        self.engine = ExecutionEngine(self.state_manager, self.executor, self.error_handler, self.planner)

    async def async_test_run(self):
        # 1. Setup Plan (Pydantic models)
        steps = [
            Step(id="1", action="search_products", inputs={"query": "test"}),
            Step(id="2", action="add_to_cart")
        ]
        plan = Plan(goal="test_goal", steps=steps)
        
        # 2. Mock Executor with state updates
        async def mock_execute(step, state):
            if step.action == "search_products":
                # State is now Pydantic, update nested fields
                state.discovery.results.append({"id": "p1"})
                return [{"id": "p1"}]
            elif step.action == "add_to_cart":
                state.cart["cartId"] = "cart_123"
                return {"cart_id": "cart_123"}
            return {}

        self.executor.execute_async = AsyncMock(side_effect=mock_execute)
        
        # 3. Collect Events
        events = []
        try:
            async for event in self.engine.run_async("session_test", plan):
                events.append(event)
        except Exception as e:
            print(f"Engine Loop Exception: {e}")
            import traceback
            traceback.print_exc()
        
        # 4. Assertions
        types = [e["type"] for e in events]
        if "ERROR" in types:
            self.fail(f"Engine emitted ERROR. Events: {events}")
        
        self.assertIn("PLAN_CREATED", types)
        self.assertIn("STEP_STARTED", types)
        self.assertIn("STEP_COMPLETED", types)
        self.assertIn("DONE", types)
        
        self.assertEqual(plan.current_step_index, 2)
        self.assertTrue(plan.is_complete())
        
        state = self.state_manager.get_state("session_test")
        self.assertEqual(state.get("cartId"), "cart_123")

    async def async_test_replan(self):
        # 1. Setup Plan
        steps = [Step(id="1", action="search_products", inputs={"query": "nonexistent"})]
        plan = Plan(goal="test_replan", steps=steps)
        
        # 2. Mock Executor to return empty (triggering REPLAN in evaluate_result)
        self.executor.execute_async = AsyncMock(return_value=[])
        
        # 3. Mock Planner to return a NEW plan on replan
        new_steps = [Step(id="replan_1", action="search_products", inputs={"query": "fallback"})]
        self.planner.create_plan = AsyncMock(return_value=Plan(goal="fixed_plan", steps=new_steps))
        
        # 4. Run Engine
        events = []
        async for event in self.engine.run_async("session_replan", plan):
            events.append(event)
            
        # 5. Assertions
        types = [e["type"] for e in events]
        self.assertIn("REPLANNING", types)
        self.assertIn("PLAN_UPDATED", types)
        # Should have exactly 2 STEP_STARTED (original + replanned)
        started_count = len([e for e in events if e["type"] == "STEP_STARTED"])
        self.assertEqual(started_count, 2)

    def test_run(self):
        asyncio.run(self.async_test_run())
        
    def test_replan(self):
        asyncio.run(self.async_test_replan())

from shared.mcp_contract import map_and_validate_args, MCPContractException

class TestMCPContract(unittest.TestCase):
    def test_search_products_mapping(self):
        args = {"query": "laptop", "limit": 5}
        mapped = map_and_validate_args("search_products", args)
        self.assertEqual(mapped.get("keyword"), "laptop")
        self.assertEqual(mapped.get("limit"), 5)
        self.assertNotIn("query", mapped)

    def test_search_products_missing_keyword(self):
        args = {"limit": 5}
        with self.assertRaises(MCPContractException):
            map_and_validate_args("search_products", args)

    def test_search_products_null_keyword(self):
        args = {"query": None}
        with self.assertRaises(MCPContractException):
            map_and_validate_args("search_products", args)
