from models.state import State
from models.plan import Plan
from models.step import Step
from typing import Dict

class StateManager:
    def __init__(self):
        self.sessions: Dict[str, State] = {}

    def get_state(self, session_id: str) -> State:
        if session_id not in self.sessions:
            self.sessions[session_id] = State(session_id=session_id)
        return self.sessions[session_id]

    def clear_session(self, session_id: str):
        if session_id in self.sessions:
            del self.sessions[session_id]
