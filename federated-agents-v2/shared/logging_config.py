import logging
import json
import sys
import os

class JsonFormatter(logging.Formatter):
    def format(self, record):
        from shared.tracing import get_trace_id, get_span_id
        log_entry = {
            "timestamp": self.formatTime(record, self.datefmt),
            "level": record.levelname,
            "logger": record.name,
            "trace_id": get_trace_id(),
            "span_id": get_span_id(),
            "message": record.getMessage(),
        }
        if record.exc_info:
            log_entry["exception"] = self.formatException(record.exc_info)
        return json.dumps(log_entry)

def setup_logging():
    level = os.getenv("LOG_LEVEL", "INFO").upper()
    handler = logging.StreamHandler(sys.stderr)
    handler.setFormatter(JsonFormatter())
    
    # Root logger
    logging.basicConfig(level=level, handlers=[handler])
    
    # Disable propagation for noisy libraries if needed
    # logging.getLogger("uvicorn").propagate = False
