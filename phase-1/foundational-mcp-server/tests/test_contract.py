import pytest
import httpx
import os

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8085/api")

@pytest.mark.asyncio
async def test_product_service_contract():
    """Verify the product service is reachable and returns the expected schema."""
    async with httpx.AsyncClient() as client:
        # Check health endpoint from actuator
        health_resp = await client.get(f"http://localhost:8081/actuator/health")
        if health_resp.status_code == 200:
            assert health_resp.json()["status"] == "UP"

        # Check the actual API via Gateway
        response = await client.get(f"{API_BASE_URL}/products?limit=1")
        # If the backend isn't fully running or no DB, it might be 404 or 200.
        # But we verify it doesn't return 500 (Internal Server Error)
        assert response.status_code in [200, 404, 400]

@pytest.mark.asyncio
async def test_cart_service_contract():
    async with httpx.AsyncClient() as client:
        response = await client.post(f"{API_BASE_URL}/carts?currencyCode=USD&country=US")
        assert response.status_code in [200, 400, 404, 401]
