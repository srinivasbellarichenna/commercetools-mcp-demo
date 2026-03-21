from models.plan import Plan
from models.step import Step

class Replanner:
    def replan(self, state, failed_step):
        """
        Generates a new plan based on the failure context and current state.
        """
        action = failed_step.action

        # Scenario: Search failure -> Try relaxing constraints
        if action == "search_products":
            # Re-read intent or last query if available
            last_query = state.discovery.last_query or "products"
            
            return Plan(
                goal="recovery_search",
                steps=[
                    Step(
                        id="recover_search_1",
                        action="search_products",
                        inputs={"query": last_query, "relaxed": True}
                    )
                ]
            )

        # Scenario: Selection/Add-to-cart failure -> Try picking an alternative
        if action == "add_to_cart":
            # If a specific product failed, maybe try searching for a similar one
            return Plan(
                goal="alternative_selection",
                steps=[
                    Step(id="alt_1", action="search_products", inputs={"query": "similar products"}),
                    Step(id="alt_2", action="select_product"),
                    Step(id="alt_3", action="add_to_cart")
                ]
            )

        # Default fallback: If we don't have a heuristic, return None (triggering failure)
        return None
