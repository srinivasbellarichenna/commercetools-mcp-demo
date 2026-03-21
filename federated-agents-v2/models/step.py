from pydantic import BaseModel, Field
from typing import Optional, Dict, Any

class Step(BaseModel):
    id: str
    action: str
    inputs: Dict[str, Any] = Field(default_factory=dict)
    condition: Optional[str] = None
    status: str = "PENDING"  # PENDING, RUNNING, DONE, FAILED, SKIPPED
