from typing import Any, Dict
import logging

logger = logging.getLogger("step-executor")

class StepExecutor:
    def __init__(self, product_cap=None, cart_cap=None, checkout_cap=None):
        from capabilities.product_capability import ProductCapability
        from capabilities.cart_capability import CartCapability
        from capabilities.checkout_capability import CheckoutCapability
        self.product = product_cap
        self.cart = cart_cap
        self.checkout = checkout_cap

    async def execute_async(self, step: Any, state: Any) -> Any:
        """
        Routes the step action to the appropriate capability.
        """
        action = step.action
        logger.info(f"Executing step {step.id}: {action} (Inputs: {step.inputs})")

        if action == "search_products":
            if not self.product: raise Exception("Product capability not initialized")
            
            # --- GUARD: Intent Recovery ---
            # If the LLM generates a dummy placeholder from the prompt or an empty query, recover it from state
            query = step.inputs.get("query", "")
            if not query or query in {"...", "string", "actual search terms"}:
                fallback_query = state.intent.get("query", "")
                if fallback_query:
                    step.inputs["query"] = fallback_query
                    logger.warning(f"StepExecutor: Recovered missing search query from state intent: '{fallback_query}'")
            
            results = await self.product.search_async(step.inputs)
            
            # --- GUARD: Suspect Empty Result ---
            # If the backend returns empty despite a valid query, we explicitly warn
            if not results and query:
                logger.warning(f"[SUSPICIOUS EMPTY RESULT] Query '{query}' yielded zero results. This could be a pure absence of data, or a severe parameter mapping contract mismatch down the chain.")
            
            # Allow Evaluator to intelligently decide based on result payload
            logger.info(f"StepExecutor: search_products returned {len(results) if isinstance(results, list) else 'non-list'} items")
            if isinstance(results, list):
                # Gap 3: Rank and trim the working memory intelligently to preserve semantic context
                ranked_results = []
                for p in results:
                    if isinstance(p, dict):
                        # Simple rank heuristic: products with prices get higher priority
                        priority = 1 if "price" in p else 0
                        ranked_results.append((priority, p))
                        
                # Sort by priority descending
                ranked_results.sort(key=lambda x: x[0], reverse=True)
                trimmed_results = [r[1] for r in ranked_results[:100]]
                
                state.discovery.results = trimmed_results
                logger.info(f"StepExecutor: state.discovery.results updated to Top {len(state.discovery.results)}")
                # Return trimmed list so execution history focuses on the relevant subset
                return trimmed_results
            return results

        elif action == "select_product":
            products = state.discovery.results
            if not products:
                 raise Exception("No search results found to select from")
            
            # Simple heuristic: pick the first one unless criteria is provided
            selected = products[0]
            state.discovery.shortlisted = [selected]
            return {"selected_product": selected.get("name")}

        elif action == "add_to_cart":
            if not self.cart: raise Exception("Cart capability not initialized")
            # Get from shortlisted or discovery
            product = state.discovery.shortlisted[0] if state.discovery.shortlisted else None
            if not product:
                raise Exception("No product selected to add to cart")
            
            cart = await self.cart.add_async(product)
            
            # Allow Evaluator to intelligently handle invalid carts
            if isinstance(cart, dict) and "id" in cart:
                state.cart["cartId"] = cart.get("id")
                state.cart["cartVersion"] = cart.get("version")
            return cart # Return the structured cart dictionary

        elif action == "checkout":
            if not self.checkout: raise Exception("Checkout capability not initialized")
            cart_id = state.cart.get("cartId")
            if not cart_id:
                raise Exception("No cart ID found to checkout")
            
            result = await self.checkout.process_async(cart_id)
            
            # Allow Evaluator to gracefully handle failed checkout attempts
            if isinstance(result, dict) and "orderNumber" in result:
                state.execution.current_step = "PAYMENT_DONE"
            return result

        elif action == "present_options":
             products = state.discovery.results
             return {"options": [p.get("name") for p in products[:5]]}

        else:
            raise Exception(f"Unknown action: {action}")
