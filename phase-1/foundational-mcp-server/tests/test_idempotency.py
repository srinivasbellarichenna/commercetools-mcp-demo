import pytest
import httpx
import os
import json
import base64
from main import place_order

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8085/api")
PROJECT_KEY = os.getenv("PROJECT_KEY") or os.getenv("CTP_PROJECT_KEY")
AUTH_URL = os.getenv("AUTH_URL") or os.getenv("CTP_AUTH_URL", "https://auth.eu-central-1.aws.commercetools.com")
API_URL = os.getenv("API_URL") or os.getenv("CTP_API_URL", "https://api.eu-central-1.aws.commercetools.com")
CLIENT_ID = os.getenv("CLIENT_ID") or os.getenv("CTP_CLIENT_ID")
CLIENT_SECRET = os.getenv("CLIENT_SECRET") or os.getenv("CTP_CLIENT_SECRET")

async def get_token(client: httpx.AsyncClient):
    if not PROJECT_KEY or not CLIENT_ID or not CLIENT_SECRET:
        raise ValueError("Missing Commercetools credentials in environment variables.")
    url = f"{AUTH_URL}/oauth/token"
    auth_str = f"{CLIENT_ID}:{CLIENT_SECRET}"
    auth_b64 = base64.b64encode(auth_str.encode("utf-8")).decode("utf-8")
    
    response = await client.post(
        url,
        data={"grant_type": "client_credentials", "scope": f"manage_project:{PROJECT_KEY}"},
        headers={
            "Authorization": f"Basic {auth_b64}",
            "Content-Type": "application/x-www-form-urlencoded"
        }
    )
    response.raise_for_status()
    return response.json()["access_token"]

async def get_existing_cart_id_from_orders(client: httpx.AsyncClient):
    if not PROJECT_KEY or not CLIENT_ID or not CLIENT_SECRET:
        return None, None
    try:
        token = await get_token(client)
    except Exception:
        return None, None
    url = f"{API_URL}/{PROJECT_KEY}/orders?limit=1"
    response = await client.get(
        url,
        headers={"Authorization": f"Bearer {token}"}
    )
    response.raise_for_status()
    orders = response.json()
    results = orders.get("results", [])
    if not results:
        return None, None
    first_order = results[0]
    return first_order["cart"]["id"], first_order["version"]

@pytest.mark.asyncio
async def test_duplicate_order_endpoint_idempotency():
    """Verify that calling POST /api/orders/from-cart with an already ordered cart returns 200 OK and the order."""
    async with httpx.AsyncClient() as client:
        cart_id, version = await get_existing_cart_id_from_orders(client)
        if not cart_id:
            pytest.skip("No existing orders found in Commercetools to test idempotency.")
            
        # Request order creation for the already ordered cart (without sessionId since it's already created)
        url = f"{API_BASE_URL}/orders/from-cart"
        params = {"cartId": cart_id, "cartVersion": version}
        
        response = await client.post(url, params=params)
        assert response.status_code == 200
        
        order_data = response.json()
        assert order_data["cart"]["id"] == cart_id

@pytest.mark.asyncio
async def test_place_order_tool_idempotency():
    """Verify that place_order tool in MCP server returns the existing order for an already ordered cart."""
    async with httpx.AsyncClient() as client:
        cart_id, version = await get_existing_cart_id_from_orders(client)
        if not cart_id:
            pytest.skip("No existing orders found in Commercetools to test idempotency.")
        
    # Call place_order tool from main.py
    result_str = await place_order(cart_id=cart_id, version=version)
    assert not result_str.startswith("ERROR:")
    
    order_data = json.loads(result_str)
    assert order_data["cart"]["id"] == cart_id
