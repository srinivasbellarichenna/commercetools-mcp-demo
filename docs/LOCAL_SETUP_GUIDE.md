# 🏺 Composable Agentic Commerce: Local Setup Guide

This guide provides the steps for setting up the **Composable Agentic Commerce** demo locally on your system. Follow these instructions to run the microservice ecosystem and initiate autonomous AI agents.

## 🛠️ Prerequisites

Before beginning the setup, ensure your system is equipped with the following:

- **Docker & Docker Compose**: For microservice orchestration.
- **Node.js (v18+)**: For the Frontend application.
- **Python (3.10+)**: For the Agency Layer (optional for local dev).
- **LM Studio (v0.3.17+)**: To host the AI agent (local LLMs).

---

## 🏗️ Step 1: Initialize the Project

1.  **Clone the Repository**:
    ```bash
    git clone <repository-url>
    cd commercetools-mcp-demo
    ```

2.  **Configure Environment**:
    Navigate to the backend foundation and set up your Commercetools and Stripe credentials:
    ```bash
    cd commercetools-wrapper
    cp .env.example .env # Ensure .env contains valid PROJECT_KEY, CLIENT_ID, and STRIPE_SECRET_KEY.
    ```

3.  **Start the Services**:
    Deploy the microservices, frontend, and agency layer via Docker:
    ```bash
    docker compose up -d --build
    ```
    *Note: The system requires ~45 seconds for the microservices to initialize and settle.*

---

## 🎨 Step 2: Accessing the Application

- **Frontend Application**: Open [http://localhost:3001](http://localhost:3001) in your browser.
- **API Gateway**: Accessible at [http://localhost:8085](http://localhost:8085).
- **Product Service**: [http://localhost:8081](http://localhost:8081).

---

## 🤖 Step 3: Enabling the AI Agent (MCP)

To connect an AI agent (like Llama 3) to the bridge:

1.  **Open LM Studio** and navigate to **Settings** -> **MCP**.
2.  **Register the Server**:
    Add the following to your `mcp.json`:
    ```json
    "agentic-commerce-mcp": {
      "url": "http://localhost:8087/sse"
    }
    ```
3.  **Start Conversation**: Load a model (e.g., Llama 3.1 8B Instruct) and ask:
    > *"List the products in the catalog."*

---

## 📜 Step 4: Monitoring the Agent

The **MCP Client** is configured to execute a test order journey upon startup. You can monitor its progress through the logs:

```bash
docker logs -f commercetools-wrapper-mcp-client-1
```

Look for the success message: 
`SUCCESS! Order ID: <order-uuid>`

---

## 🏛️ Project Structure

- `/frontend`: The storefront application.
- `/commercetools-wrapper`: The backend microservice foundation.
- `/agency`: The MCP server and autonomous client.
- `/docs`: Documentation and integration guides.
- `/config`: External configuration templates.

---
*Composable Agentic Commerce - Commercetools MCP Integration Demo*
