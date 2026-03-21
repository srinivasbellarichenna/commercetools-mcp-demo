To ensure Claude uses your **Specialized Agents** (the Agency) instead of just calling low-level API tools directly, you should use prompts that trigger **reasoning, curation, or managed workflows**.

Here are the best ways to "force" the use of the agents:

### 1. The "Specialist" Prompt (Recommended)
Ask Claude to consult the specific experts you built. This forces it to look for a tool that represents an "Agency" or "Specialist."

*   **Prompt**: *"I want to consult the **Agentic Agency Specialists**. Can you ask the **Analyst** to find me some premium furniture that would fit a minimalist office?"*
*   **Prompt**: *"I'm ready to buy. Can you have the **Closer** specialist manage my checkout process for cart ID `123`?"*

### 2. High-Level Intent Prompting
Avoid asking for specific technical actions (like "add to cart"). Instead, ask for a **goal**.

*   **Prompt**: *"I'm looking for a gift recommendation for a luxury home. Can the Agency suggest something and tell me why it's a good fit?"*
    *   *Claude will see that [get_collection](cci:1://file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/agency/mcp-server/main.py:26:0-66:67) just gives a list, but [commerce_agent_request](cci:1://file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/federated-agents/orchestrator.py:21:0-47:126) (the Orchestrator) is described as handling "triaging" and "reasoning." It will pick the Orchestrator.*

### 3. The "Preferred Gateway" Strategy
If you want Claude to **only** use the Agents, the cleanest way is to **disable the low-level MCP server in your config**.

Since your `federated-agents` (Orchestrator) already has its own [MCPProxy](cci:2://file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/federated-agents/shared/mcp_proxy.py:12:0-58:34) that talks directly to the backend APIs, **Claude doesn't actually need the low-level MCP server connected.**

Try this:
1.  **Remove** (or comment out) the `agency-mcp-server` from your `claude_desktop_config.json`.
2.  **Keep** only the `agentic-agency`.
3.  Now, the **only** tool Claude sees is [commerce_agent_request](cci:1://file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/federated-agents/orchestrator.py:21:0-47:126). It *has* to use your agents to get anything done!

### 4. Provide a Customs System Prompt to Claude
You can tell Claude how you want it to behave. At the start of your chat with Claude, give it this instruction:

> *"You are an AI Host for a Federated Commerce Agency. For any product discovery, search, or checkout requests, you MUST delegate to the [commerce_agent_request](cci:1://file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/federated-agents/orchestrator.py:21:0-47:126) tool rather than attempting to figure it out yourself. This tool connects you to specialized Analyst and Closer agents who have deeper domain knowledge."*

### Why this works:
Claude picks tools based on their **Descriptions**.
*   [get_collection](cci:1://file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/agency/mcp-server/main.py:26:0-66:67) description: *"List the products available in the storefront."*
*   [commerce_agent_request](cci:1://file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/federated-agents/orchestrator.py:21:0-47:126) description: *"The main entry point for the Agentic Agency. It **triages** the request and **delegates** to the **specialized Analyst or Closer agents**."*

By using words like **"Specialist," "Agent," "Orchestrate,"** or **"Triage"** in your prompt, you hit the keywords that make Claude choose the higher-level Agency tool.
