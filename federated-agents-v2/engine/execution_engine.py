import asyncio
import json
import logging
import uuid
from typing import AsyncGenerator, Any, Optional, Dict
from engine.state_manager import StateManager
from engine.step_executor import StepExecutor
from engine.error_handler import ErrorHandler
from engine.planner import Planner
from engine.evaluator import Evaluator
from engine.replanner import Replanner
from models.decision import Decision
from models.state import ExecutionHistory
from shared.tracing import TraceSpan, get_trace_id, get_span_id, set_tracing

logger = logging.getLogger("execution-engine")

class ExecutionEngine:
    def __init__(self, state_manager: StateManager, executor: StepExecutor, error_handler: ErrorHandler, planner: Planner):
        self.state_manager = state_manager
        self.executor = executor
        self.error_handler = error_handler
        self.planner = planner
        self.evaluator = Evaluator()
        self.replanner = Replanner()
        self.max_retries = 3

    async def run_async(self, session_id: str, plan: Any) -> AsyncGenerator[dict, None]:
        """
        Executes the plan with an autonomous evaluation-replanning loop.
        """
        state = self.state_manager.get_state(session_id)
        
        # Initialize tracing for this session run
        trace_id = state.trace_id or str(uuid.uuid4())
        state.trace_id = trace_id
        set_tracing(trace_id)

        try:
            yield self._event("PLAN_CREATED", data={"goal": plan.goal, "steps_count": len(plan.steps)})

            while not plan.is_complete():
                with TraceSpan(f"step_{plan.current_step_index}"):
                    step = plan.next_step()
                
                # JSON Logging for production observability
                logger.info(json.dumps({
                    "action": "STEP_START",
                    "step_id": step.id,
                    "action_name": step.action,
                    "inputs": step.inputs,
                    "trace_id": get_trace_id(),
                    "span_id": get_span_id()
                }))

                yield self._event("STEP_STARTED", step=step.action, data={"id": step.id, "inputs": step.inputs})

                # --- EXECUTION ---
                try:
                    # FIX: Method name in StepExecutor is execute_async
                    result = await self.executor.execute_async(step, state)
                    
                    result_count = len(result) if isinstance(result, list) else 1
                    if isinstance(result, dict) and "results_count" in result:
                        result_count = result.get("results_count", 1)
                    
                    # Payload Guard
                    guarded_result = result
                    if isinstance(result, list) and len(result) > 100:
                        guarded_result = result[:100]

                    # Summarization
                    def summarize(res):
                        if isinstance(res, list):
                            return {"count": len(res), "sample": res[:2]}
                        if isinstance(res, dict):
                            return {"type": "dict", "keys": list(res.keys())}
                        return {"type": type(res).__name__, "value": str(res)[:100]}
                        
                    # Track history in state for observability
                    state.execution.history.append(ExecutionHistory(
                        step_id=step.id,
                        action=step.action,
                        status="COMPLETED",
                        result_count=result_count,
                        summary=summarize(result),
                        result=guarded_result
                    ))
                except Exception as e:
                    logger.error(f"Execution failed for step {step.id}: {e}")
                    result = {"error": str(e)}
                    state.execution.history.append(ExecutionHistory(
                        step_id=step.id,
                        action=step.action,
                        status="FAILED",
                        result_count=0,
                        result={"error": str(e)}
                    ))

                # --- EVALUATION ---
                decision = self.evaluator.evaluate(step, result, state)
                
                yield self._event("STEP_RESULT", step=step.action, data={
                    "id": step.id,
                    "result": result,
                    "decision": decision
                })

                # --- DECISION HANDLING ---
                if decision == Decision.CONTINUE:
                    step.status = "DONE"
                    plan.mark_done()
                    state.retry_count = 0
                    yield self._event("STEP_COMPLETED", step=step.action, data={"id": step.id})

                elif decision == Decision.RETRY:
                    state.retry_count += 1
                    if state.retry_count > self.max_retries:
                        yield self._event("ERROR", step=step.action, data={"reason": "Max retries exceeded"})
                        break
                    
                    yield self._event("RETRYING", step=step.action, data={"id": step.id, "attempt": state.retry_count})
                    continue # Loop again on same step

                elif decision == Decision.REPLAN:
                    state.replan_count += 1
                    yield self._event("REPLANNING", data={"reason": f"Step {step.action} failed evaluation. Triggering recovery."})
                    
                    # Try heuristic replanner first
                    new_plan = self.replanner.replan(step, result, state)
                    if not new_plan and self.planner:
                        # Fallback to LLM planner if heuristic fails
                        new_plan = await self.planner.create_plan(f"Recover from failure in {step.action}. Current state: {state.intent}", state.model_dump())
                    
                    if new_plan:
                        plan = new_plan
                        yield self._event("PLAN_UPDATED", data={"goal": plan.goal, "steps_count": len(plan.steps)})
                    else:
                        yield self._event("ERROR", data={"reason": "Replanning failed"})
                        break

                elif decision == Decision.ABORT:
                    yield self._event("ABORTED", data={"reason": "Evaluator requested abort"})
                    break
        except Exception as e:
            logger.exception(f"Engine Fatal Error: {e}")
            yield self._event("ERROR", data={"reason": f"Fatal engine error: {str(e)}"})
        finally:
            # ALWAYS emit DONE to prevent protocol hanging
            yield self._event("DONE", data=state.model_dump())

    def _event(self, event_type: str, step: Optional[str] = None, data: Optional[Dict[str, Any]] = None) -> dict:
        event = {
            "type": event_type,
            "step": step,
            "trace_id": get_trace_id(),
            "span_id": get_span_id(),
            "data": data or {}
        }
        logger.info(f"EVENT: {json.dumps(event)}")
        return event

    def _evaluate_condition(self, condition: str, state: Any) -> bool:
        if condition == "cart_not_empty":
            return state.cart.get("cartId") is not None
        return True
