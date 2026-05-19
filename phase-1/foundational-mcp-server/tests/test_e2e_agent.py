import pytest
import httpx
import os

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8085/api")

@pytest.mark.asyncio
async def test_e2e_agent_workflow():
    """
    Simulates the E2E workflow an Agent would perform:
    1. Search for a product
    2. Create a Cart
    3. Add product to Cart
    """
    async with httpx.AsyncClient() as client:
        # Step 1: Search Products
        product_resp = await client.get(f"{API_BASE_URL}/products?limit=1")
        if product_resp.status_code != 200:
            pytest.skip("Backend not fully available or no products indexed. Skipping E2E.")
            
        products_data = product_resp.json()
        if not products_data or "results" not in products_data or len(products_data["results"]) == 0:
            pytest.skip("No products found in DB. Skipping E2E.")
            
        product = products_data["results"][0]
        sku = product["masterVariant"]["sku"]
        
        # Step 2: Create Cart
        cart_resp = await client.post(f"{API_BASE_URL}/carts?currencyCode=EUR&country=DE")
        assert cart_resp.status_code == 200
        cart_id = cart_resp.json()["id"]
        
        # Step 3: Add to Cart
        add_item_resp = await client.post(f"{API_BASE_URL}/carts/{cart_id}/items?sku={sku}&quantity=1")
        assert add_item_resp.status_code == 200
        cart_data = add_item_resp.json()
        assert len(cart_data["lineItems"]) > 0
        assert cart_data["lineItems"][0]["variant"]["sku"] == sku
