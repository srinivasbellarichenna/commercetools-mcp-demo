from models.decision import Decision

class Evaluator:
    def evaluate(self, step, result, state, error=None):
        """
        Drives the agentic loop by deciding what to do next.
        """
        # --- ERROR CASES ---
        if error:
            return self._handle_error(step, error, state)

        # --- SUCCESS CASES ---
        action = step.action

        if action == "search_products":
            status = self._validate_search_results(result)
            if status == "EMPTY":
                if state.intent.get("strict_query"):
                    return Decision.ABORT
                if state.replan_count >= 2:
                    return Decision.SKIP
                return Decision.REPLAN
            if status == "INVALID":
                if state.retry_count >= 1:
                    return Decision.REPLAN
                return Decision.RETRY
            return Decision.CONTINUE

        if action == "select_product":
            if not result or result.get("error"):
                return Decision.REPLAN
            return Decision.CONTINUE

        if action == "add_to_cart":
            status = self._validate_cart_result(result)
            if status == "INVALID":
                if state.retry_count >= 2:
                    return Decision.REPLAN
                return Decision.RETRY
            return Decision.CONTINUE

        if action == "checkout":
            status = self._validate_checkout_result(result)
            if status == "INVALID":
                if state.retry_count >= 2:
                    return Decision.ABORT
                return Decision.RETRY
            return Decision.CONTINUE

        return Decision.CONTINUE

    def _validate_search_results(self, results):
        if not results or (isinstance(results, list) and len(results) == 0):
            return "EMPTY"
        if isinstance(results, list):
            for item in results:
                if not isinstance(item, dict):
                    return "INVALID"
                # Deep validation for required schema fields
                if "id" not in item:
                    return "INVALID"
                # If price is completely missing or mathematically invalid, reject it
                if "name" not in item:
                    return "INVALID"
        return "VALID"

    def _validate_cart_result(self, result):
        if not result or not isinstance(result, dict) or "id" not in result:
            return "INVALID"
        return "VALID"

    def _validate_checkout_result(self, result):
        if not result or not isinstance(result, dict) or "orderNumber" not in result:
            return "INVALID"
        return "VALID"

    def _handle_error(self, step, error, state):
        error_str = str(error).lower()

        # Handle transient/versioning issues with RETRY
        if "version conflict" in error_str or "concurrent modification" in error_str:
            return Decision.RETRY
        
        if "timeout" in error_str:
            return Decision.RETRY

        # Handle business logic issues with REPLAN
        if "out of stock" in error_str or "unavailable" in error_str:
            return Decision.REPLAN

        # Critical unknown failures ABORT
        return Decision.ABORT
