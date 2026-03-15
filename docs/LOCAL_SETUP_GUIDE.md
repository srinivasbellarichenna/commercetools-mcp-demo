# 🏺 KESTREL Atelier: Local Onboarding Manuscript

This guide provides the definitive steps for establishing the **KESTREL Atelier** locally on your system. Follow these instructions to replicate the artisanal microservice ecosystem and initiate autonomous agentic orchestration.

## 🛠️ Prerequisite Architecture

Before beginning the ritual, ensure your system is equipped with the following:

- **Docker & Docker Compose**: For microservice orchestration.
- **Node.js (v18+)**: For the Frontend Atelier.
- **Python (3.10+)**: For the Agency Layer (optional for local dev).
- **LM Studio (v0.3.17+)**: To host the AI Curator (local LLMs).

---

## 🏗️ Step 1: Ecosystem Initialization

1.  **Clone the Heritage**:
    ```bash
    git clone <repository-url>
    cd commercetools-mcp-demo
    ```

2.  **Configure Environment**:
    Navigate to the backend foundation and establish your Commercetools credentials:
    ```bash
    cd commercetools-wrapper
    cp .env.example .env # Ensure .env contains valid PROJECT_KEY, CLIENT_ID, and STRIPE_SECRET_KEY.
    ```

3.  **Launch the Orchestra**:
    Deploy the microservices, frontend, and agency layer via Docker:
    ```bash
    docker compose up -d --build
    ```
    *Note: The system requires ~45 seconds for a stable "settling" of the microservice ecosystem.*

---

## 🎨 Step 2: Accessing the Atelier

- **Frontend Atelier**: Open [http://localhost:3001](http://localhost:3001) in your browser.
- **API Gateway**: Accessible at [http://localhost:8085](http://localhost:8085).
- **Heritge Registry (Products)**: [http://localhost:8081](http://localhost:8081).

---

## 🤖 Step 3: Enabling Agency (MCP)

To connect an AI Curator (like Llama 3) to the KESTREL bridge:

1.  **Open LM Studio** and navigate to **Settings** -> **MCP**.
2.  **Register the Bridge**:
    Add the follows to your `mcp.json`:
    ```json
    "mcpServers": {
      "kestrel-mcp": {
        "url": "http://localhost:8087/sse"
      }
    }
    ```
3.  **Start Conversation**: Load a model (e.g., Llama 3.1 8B Instruct) and ask:
    > *"List the artisanal pieces in our heritage collection."*

---

## 📜 Step 4: Monitoring the Agentic Journey

The **MCP Client** is configured to execute a "Definitive Success" journey upon startup. You can monitor its progress through the system logs:

```bash
docker logs -f commercetools-wrapper-mcp-client-1
```

Look for the culminating achievement: 
`SUCCESS! Order ID: <heritage-uuid>`

---

## 🏛️ Project Directory Structure

- `/frontend`: The visual experience.
- `/commercetools-wrapper`: The backend microservice foundation.
- `/agency`: The MCP server and autonomous client.
- `/docs`: Onboarding and integration guides.
- `/config`: External configuration templates.

---
*KESTREL Atelier - Clarity for the Next Archivist*
