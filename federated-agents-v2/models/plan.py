from pydantic import BaseModel
from typing import List
from .step import Step

class Plan(BaseModel):
    goal: str
    steps: List[Step]
    current_step_index: int = 0

    def next_step(self) -> Step:
        if self.is_complete():
            raise Exception("Plan already complete")
        return self.steps[self.current_step_index]

    def mark_done(self):
        self.current_step_index += 1

    def is_complete(self) -> bool:
        return self.current_step_index >= len(plan.steps) if hasattr(self, 'steps') else True

    # Fix for self-reference in is_complete
    def is_complete(self) -> bool:
        return self.current_step_index >= len(self.steps)
