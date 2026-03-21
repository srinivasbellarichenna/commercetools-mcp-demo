import contextvars
import uuid
from typing import Optional, Any

# Context variables for trace and span IDs with explicit typing
trace_id_var: contextvars.ContextVar[Optional[str]] = contextvars.ContextVar("trace_id", default=None)
span_id_var: contextvars.ContextVar[Optional[str]] = contextvars.ContextVar("span_id", default=None)

def get_trace_id() -> str:
    tid = trace_id_var.get()
    if tid is None:
        tid = str(uuid.uuid4())
        trace_id_var.set(tid)
    return str(tid)

def get_span_id() -> str:
    sid = span_id_var.get()
    if sid is None:
        sid = str(uuid.uuid4())[:8]
        span_id_var.set(sid)
    return str(sid)

def set_tracing(trace_id: str, span_id: Optional[str] = None):
    trace_id_var.set(trace_id)
    if span_id:
        span_id_var.set(span_id)
    else:
        span_id_var.set(str(uuid.uuid4())[:8])

class TraceSpan:
    def __init__(self, name: str):
        self.name = name
        self.span_id = str(uuid.uuid4())[:8]
        self.token: Optional[contextvars.Token[Optional[str]]] = None

    def __enter__(self):
        self.token = span_id_var.set(self.span_id)
        return self

    def __exit__(self, exc_type: Any, exc_val: Any, exc_tb: Any):
        if self.token:
            span_id_var.reset(self.token)
