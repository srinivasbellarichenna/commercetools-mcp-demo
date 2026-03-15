import httpx
import json
import os
import logging
import sys
import asyncio
from typing import Any
from mcp.server.fastmcp import FastMCP
from mcp.server.transport_security import TransportSecuritySettings

# Configure logging for artisanal transparency
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stdout
)
logger = logging.getLogger("kestrel-mcp")

# Initialize the KESTREL FastMCP server
mcp = FastMCP(
    "kestrel-mcp-server",
    transport_security=TransportSecuritySettings(enable_dns_rebinding_protection=False)
)

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8085/api")

@mcp.tool()
async def get_collection(limit: int = 10, offset: int = 0) -> str:
    """
    List the artisanal collection of pieces (products).
    
    Args:
        limit: Maximum number of artifacts to retrieve.
        offset: Number of artifacts to skip.
    """
    logger.info(f"Tool Call: get_collection(limit={limit}, offset={offset})")
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            params = {"limit": limit, "offset": offset}
            url = f"{API_BASE_URL}/products"
            logger.info(f"Backend Request: GET {url} params={params}")
            
            response = await client.get(url, params=params)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            data = response.json()
            # Enrich results with a default variantId and real SKU for easier consumption
            if "results" in data:
                for product in data["results"]:
                    product["suggested_variant_id"] = 1
                    # Robust SKU extraction strategy
                    sku = None
                    if "masterVariant" in product:
                        sku = product["masterVariant"].get("sku")
                    elif "masterData" in product:
                        # Some versions of the API return masterData
                        sku = product.get("masterData", {}).get("current", {}).get("masterVariant", {}).get("sku")
                    
                    product["suggested_sku"] = sku or "missing-sku"
                    logger.info(f"Piece {product.get('id')} -> Captured SKU: {product['suggested_sku']}")
                    
            return json.dumps(data, indent=2)
        except Exception as e:
            logger.error(f"Error in get_collection: {str(e)}")
            return f"Error fetching collection from registry: {str(e)}"

@mcp.tool()
async def get_piece_detail(piece_id: str) -> str:
    """
    View the detailed heritage and specifications of a specific piece.
    """
    logger.info(f"Tool Call: get_piece_detail(piece_id='{piece_id}')")
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            url = f"{API_BASE_URL}/products/{piece_id}"
            logger.info(f"Backend Request: GET {url}")
            
            response = await client.get(url)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            return json.dumps(result, indent=2)
        except Exception as e:
            logger.error(f"Error in get_piece_detail: {str(e)}")
            return f"Error fetching piece details for {piece_id}: {str(e)}"

@mcp.tool()
async def initialize_bag(currency: str = "EUR", country: str = "DE") -> str:
    """
    Create a new acquisition bag (cart) for the current session.
    """
    logger.info(f"Tool Call: initialize_bag(currency='{currency}', country='{country}')")
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            params = {"currencyCode": currency, "country": country}
            url = f"{API_BASE_URL}/carts"
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            logger.info(f"Bag Initialized: {result.get('id')}")
            return json.dumps(result, indent=2)
        except Exception as e:
            logger.error(f"Error in initialize_bag: {str(e)}")
            return f"Error initiating acquisition bag: {str(e)}"

@mcp.tool()
async def commit_to_bag(cart_id: str, sku: str, quantity: int = 1) -> str:
    """
    Add an artisanal piece to the acquisition bag using its SKU.
    """
    logger.info(f"Tool Call: commit_to_bag(cart_id='{cart_id}', sku='{sku}', quantity={quantity})")
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            # The backend API requires 'sku' as a request parameter
            params = {"sku": sku, "quantity": quantity}
            url = f"{API_BASE_URL}/carts/{cart_id}/items"
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            logger.info(f"Backend Response: Status={response.status_code}")
            response.raise_for_status()
            result = response.json()
            logger.info(f"Asset Committed. New Version: {result.get('version')}")
            return json.dumps(result, indent=2)
        except Exception as e:
            logger.error(f"Error in commit_to_bag: {str(e)}")
            return f"Error committing piece to bag: {str(e)}"

@mcp.tool()
async def set_shipping_address(cart_id: str, first_name: str, last_name: str, street: str, city: str, postal_code: str, country: str = "DE") -> str:
    """
    Formalize the destination for the acquisition bag.
    """
    logger.info(f"Tool Call: set_shipping_address(cart_id='{cart_id}', street='{street}')")
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            payload = {
                "firstName": first_name,
                "lastName": last_name,
                "streetName": street,
                "city": city,
                "postalCode": postal_code,
                "country": country
            }
            url = f"{API_BASE_URL}/carts/{cart_id}/shipping-address"
            logger.info(f"Backend Request: POST {url} payload={payload}")
            
            response = await client.post(url, json=payload)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            logger.info(f"Destination Formalized for bag {cart_id}")
            return json.dumps(result, indent=2)
        except Exception as e:
            logger.error(f"Error in set_shipping_address: {str(e)}")
            return f"Error setting shipping address: {str(e)}"

@mcp.tool()
async def place_order(cart_id: str, version: int) -> str:
    """
    Finalize the acquisition and place a formal order.
    """
    logger.info(f"Tool Call: place_order(cart_id='{cart_id}', version={version})")
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            params = {"cartId": cart_id, "cartVersion": version}
            url = f"{API_BASE_URL}/orders/from-cart"
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            logger.info(f"ORDER PLACED SUCCESSFULLY: {result.get('id')}")
            return json.dumps(result, indent=2)
        except Exception as e:
            logger.error(f"Error in place_order: {str(e)}")
            return f"Error placing order: {str(e)}"

@mcp.tool()
async def get_patron_registry(customer_id: str) -> str:
    """
    Retrieve a patron's records and acquisition history from the registry.
    """
    logger.info(f"Tool Call: get_patron_registry(customer_id='{customer_id}')")
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            url = f"{API_BASE_URL}/customers/{customer_id}"
            logger.info(f"Backend Request: GET {url}")
            
            response = await client.get(url)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            return json.dumps(result, indent=2)
        except Exception as e:
            logger.error(f"Error in get_patron_registry: {str(e)}")
            return f"Error retrieving patron record for {customer_id}: {str(e)}"

@mcp.tool()
async def refine_registry_record(customer_id: str, first_name: str = "", last_name: str = "", email: str = "") -> str:
    """
    Update a patron's profile details in the formal registry.
    """
    logger.info(f"Tool Call: refine_registry_record(customer_id='{customer_id}', first_name='{first_name}', last_name='{last_name}', email='{email}')")
    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            params = {}
            if first_name: params["firstName"] = first_name
            if last_name: params["lastName"] = last_name
            if email: params["email"] = email
            
            url = f"{API_BASE_URL}/customers/{customer_id}/profile"
            logger.info(f"Backend Request: PATCH {url} params={params}")
            
            response = await client.patch(url, params=params)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            return json.dumps(result, indent=2)
        except Exception as e:
            logger.error(f"Error in refine_registry_record: {str(e)}")
            return f"Error refining registry record for {customer_id}: {str(e)}"

if __name__ == "__main__":
    mcp.run()
