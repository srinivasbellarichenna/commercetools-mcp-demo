import logging
from typing import Any

logger = logging.getLogger("error-handler")

class ErrorHandler:
    def handle(self, error: Exception, step: Any, state: Any) -> str:
        """
        Determines the recovery action for a given error.
        Returns: "RETRY", "FALLBACK", or "FAIL"
        """
        error_msg = str(error).lower()
        logger.error(f"Error executing step {step.id} ({step.action}): {error}")

        # Example: Cart version conflict (common in Commercetools)
        if "version" in error_msg and "conflict" in error_msg:
            logger.info("Decision: RETRY (Version Conflict)")
            return "RETRY"

        # Example: Product not found
        if "not found" in error_msg:
            logger.info("Decision: FAIL (Resource Not Found)")
            return "FAIL"

        return "FAIL"
