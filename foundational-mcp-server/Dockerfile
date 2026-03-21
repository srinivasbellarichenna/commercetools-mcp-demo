FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Environment defaults for containerized operation
ENV API_BASE_URL=http://api-gateway:8080/api

# Run the KESTREL MCP Server using the explicit SSE runner
# This ensures its prestigious presence in the Docker orchestra on port 8087
ENTRYPOINT ["python", "run_sse.py"]
