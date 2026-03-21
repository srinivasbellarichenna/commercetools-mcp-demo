from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional

class DiscoveryState(BaseModel):
    results: List[Dict[str, Any]] = Field(default_factory=list)
    shortlisted: List[Dict[str, Any]] = Field(default_factory=list)
    last_query: Optional[str] = None

import time

class ExecutionHistory(BaseModel):
    step_id: str
    action: str
    status: str
    timestamp: float = Field(default_factory=time.time)
    result_count: int = 0
    summary: Optional[Dict[str, Any]] = None
    result: Optional[Any] = None

class ExecutionState(BaseModel):
    current_step: str = ""
    history: List[ExecutionHistory] = Field(default_factory=list)

class State(BaseModel):
    session_id: str
    trace_id: Optional[str] = None
    retry_count: int = 0
    replan_count: int = 0
    intent: Dict[str, Any] = Field(default_factory=dict)
    discovery: DiscoveryState = Field(default_factory=DiscoveryState)
    cart: Dict[str, Any] = Field(default_factory=dict)
    execution: ExecutionState = Field(default_factory=ExecutionState)

    def to_dict(self) -> dict:
        return self.model_dump()

    def get(self, key: str, default: Any = None) -> Any:
        # Legacy support for .get() on state
        if key == "cartId":
            return self.cart.get("cartId", default)
        if key == "searchResults":
            return self.discovery.results
        if key == "selectedProduct":
            return self.discovery.shortlisted[0] if self.discovery.shortlisted else default
        
        data = self.model_dump()
        return data.get(key, default)

    def update(self, key: str, value: Any):
        # Legacy support for .update() on state
        if key == "searchResults":
            self.discovery.results = value
        elif key == "selectedProduct":
            self.discovery.shortlisted = [value] if value else []
        elif key == "cartId":
            self.cart["cartId"] = value
        else:
            # Fallback to intent for unknown top-level keys
            self.intent[key] = value
