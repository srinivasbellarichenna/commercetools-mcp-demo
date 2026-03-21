# Innovative Prompts for Federated Agents

This guide provides a set of innovative prompts designed to test the full potential of your **Federated Agents** (the **Analyst** and the **Closer**). These prompts are designed to trigger complex reasoning, multi-specialist coordination, and tool-assisted automation.

## 🌟 Level 1: Advanced Discovery (The Analyst)

These prompts challenge the **Analyst** to go beyond simple keyword matching and act as a personalized shopping assistant.

1. **The "Interior Designer" Approach**
   - *Prompt:* "I'm looking for a premium living room set that matches a Scandinavian aesthetic. What can you recommend from our latest collection that feels minimalist and warm?"
   - *Why it's interesting:* Forces the Analyst to interpret "Scandinavian aesthetic" and "minimalist" against the product descriptions.

2. **The "Value Hunter" Comparison**
   - *Prompt:* "Find me three different wooden chairs in the collection. Compare their features and prices, and tell me which one offers the best value for a home office."
   - *Why it's interesting:* Requires the agent to fetch multiple products, perform a comparative analysis, and provide a recommendation.

3. **The "Gifting" Scenario**
   - *Prompt:* "I need a sophisticated housewarming gift under $200. Suggest three options from the home decor section and explain why they make good gifts."
   - *Why it's interesting:* Adds a price constraint and requires "reasoning" on why a product is suitable for a specific occasion.

---

## 🚀 Level 2: Complex Transactions (The Closer)

These prompts test the **Closer's** ability to manage the state of a transaction and guide the user through the checkout flow.

4. **The "Informed Checkout"**
   - *Prompt:* "I've added the 'Nordic Lounge Chair' to my cart. Can you check if my shipping address for my email 'john.doe@example.com' is already set? If so, I'm ready to buy."
   - *Why it's interesting:* Requires fetching a customer by email, checking the cart state, and preparing for payment.

5. **The "Pre-flight Check"**
   - *Prompt:* "Show me everything currently in my cart and give me a breakdown of the total cost. If everything looks correct, generate a secure payment link for me."
   - *Why it's interesting:* Tests the agent's ability to retrieve cart data and recommend the `create_stripe_checkout` tool.

---

## 🔥 Level 3: Cross-Agent Orchestration

These "Ultimate" prompts require the Orchestrator to potentially hand off between both specialists or handle conditional logic.

6. **The "Bundle & Close"**
   - *Prompt:* "Find a matching side table for the sofa I just looked at. If you find one under $150, add it to my cart and let's proceed straight to the checkout summary."
   - *Why it's interesting:* This is the "Holy Grail" of the agency. It involves Discovery (Analyst) -> Action (Add to Cart) -> Transaction (Closer) in a single flow.

7. **The "Corporate Procurement"**
   - *Prompt:* "We are furnishing a new startup office. I need 10 ergonomic chairs and 5 height-adjustable desks. Give me a quote for the entire set, then explain the steps to finalize this as a corporate purchase."
   - *Why it's interesting:* Challenges the agent with high quantities and a more formal business-to-business (B2B) tone.

---

## 🛠️ Pro Tips for Claude Desktop
- **Context is King:** If you've already interacted with a product, the agents might remember it in the "conversation context," but for the *Federated Agents* tool, be explicit about what you are looking for.
- **Customer Identity:** Always mention your email if you want the agent to find your specific "Cart" or "Account".
- **Tool Awareness:** If the agent seems stuck, ask: "What tools do you have available to help me with this?"
