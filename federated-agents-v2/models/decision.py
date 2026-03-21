from enum import Enum

class Decision(str, Enum):
    CONTINUE = "CONTINUE"
    RETRY = "RETRY"
    REPLAN = "REPLAN"
    SKIP = "SKIP"
    ABORT = "ABORT"
