FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Environment variables will be provided via docker-compose
ENV PYTHONUNBUFFERED=1

# Run the Federated Agents using the explicit SSE runner
CMD ["python", "run_sse.py"]
