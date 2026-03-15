import httpx
import json
import os
import logging
import sys
import asyncio
from typing import Any
from mcp.server.fastmcp import FastMCP
from mcp.server.transport_security import TransportSecuritySettings

# Configure logging for transparency
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stdout
)
logger = logging.getLogger("agentic-mcp")

# Initialize the AGENTIC FastMCP server
mcp = FastMCP(
    "agentic-mcp-server",
    transport_security=TransportSecuritySettings(enable_dns_rebinding_protection=False)
)

API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8085/api")

@mcp.tool()
async def get_collection(limit: int = 10, offset: int = 0) -> str:
    """
    List the products available in the storefront.
    
    Args:
        limit: Maximum number of items to retrieve.
        offset: Number of items to skip.
    """
    logger.info(f"Tool Call: get_collection(limit={limit}, offset={offset})")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
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
                    logger.info(f"Product {product.get('id')} -> Captured SKU: {product['suggested_sku']}")
            
            result_str = json.dumps(data, indent=2)
        return result_str
    except Exception as e:
        logger.error(f"Error in get_collection: {str(e)}")
        return f"Error fetching collection from registry: {str(e)}"

@mcp.tool()
async def get_piece_detail(piece_id: str) -> str:
    """
    View the detailed specifications of a specific product.
    """
    logger.info(f"Tool Call: get_piece_detail(piece_id='{piece_id}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            url = f"{API_BASE_URL}/products/{piece_id}"
            logger.info(f"Backend Request: GET {url}")
            
            response = await client.get(url)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            return json.dumps(result, indent=2)
    except Exception as e:
        logger.error(f"Error in get_piece_detail: {str(e)}")
        return f"Error fetching product details for {piece_id}: {str(e)}"

@mcp.tool()
async def initialize_cart(currency: str = "EUR", country: str = "DE") -> str:
    """
    Create a new shopping cart for the current session.
    """
    logger.info(f"Tool Call: initialize_bag(currency='{currency}', country='{country}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            params = {"currencyCode": currency, "country": country}
            url = f"{API_BASE_URL}/carts"
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            logger.info(f"Cart Initialized: {result.get('id')}")
            result_str = json.dumps(result, indent=2)
        return result_str
    except Exception as e:
        logger.error(f"Error in initialize_cart: {str(e)}")
        return f"Error initiating shopping cart: {str(e)}"

@mcp.tool()
async def get_shipping_methods(cart_id: str) -> str:
    """
    Get available shipping methods for a cart.
    """
    logger.info(f"Tool Call: get_shipping_methods(cart_id='{cart_id}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            url = f"{API_BASE_URL}/carts/{cart_id}/shipping-methods"
            logger.info(f"Backend Request: GET {url}")
            
            response = await client.get(url)
            response.raise_for_status()
            return json.dumps(response.json(), indent=2)
    except Exception as e:
        logger.error(f"Error in get_shipping_methods: {str(e)}")
        return f"Error fetching shipping methods: {str(e)}"

@mcp.tool()
async def set_shipping_method(cart_id: str, shipping_method_id: str) -> str:
    """
    Set the shipping method for a cart.
    """
    logger.info(f"Tool Call: set_shipping_method(cart_id='{cart_id}', method_id='{shipping_method_id}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            url = f"{API_BASE_URL}/carts/{cart_id}/shipping-method"
            params = {"shippingMethodId": shipping_method_id}
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            response.raise_for_status()
            return json.dumps(response.json(), indent=2)
    except Exception as e:
        logger.error(f"Error in set_shipping_method: {str(e)}")
        return f"Error setting shipping method: {str(e)}"

@mcp.tool()
async def create_payment(cart_id: str, amount: int, currency: str = "EUR", payment_method: str = "credit_card") -> str:
    """
    Create a Payment object in Commercetools.
    """
    logger.info(f"Tool Call: create_payment(cart_id='{cart_id}', amount={amount})")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            params = {
                "cartId": cart_id,
                "amount": str(amount),
                "currency": currency,
                "paymentMethod": payment_method
            }
            url = f"{API_BASE_URL}/payments"
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            response.raise_for_status()
            return json.dumps(response.json(), indent=2)
    except Exception as e:
        logger.error(f"Error in create_payment: {str(e)}")
        return f"Error creating payment: {str(e)}"

@mcp.tool()
async def add_payment_to_cart(cart_id: str, payment_id: str) -> str:
    """
    Associate a Payment object with a Cart.
    """
    logger.info(f"Tool Call: add_payment_to_cart(cart_id='{cart_id}', payment_id='{payment_id}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            params = {"paymentId": payment_id}
            url = f"{API_BASE_URL}/carts/{cart_id}/payments"
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            response.raise_for_status()
            return json.dumps(response.json(), indent=2)
    except Exception as e:
        logger.error(f"Error in add_payment_to_cart: {str(e)}")
        return f"Error adding payment to cart: {str(e)}"

@mcp.tool()
async def create_stripe_checkout(cart_id: str) -> str:
    """
    Generate a Stripe Checkout URL for the given cart.
    CRITICAL: This tool MUST be called and the resulting URL must be presented to the customer 
    for payment BEFORE calling 'place_order'.
    """
    logger.info(f"Tool Call: create_stripe_checkout(cart_id='{cart_id}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            url = f"{API_BASE_URL}/payments/checkout"
            params = {"cartId": cart_id}
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            response.raise_for_status()
            return json.dumps(response.json(), indent=2)
    except Exception as e:
        logger.error(f"Error in create_stripe_checkout: {str(e)}")
        return f"Error creating Stripe checkout: {str(e)}"

@mcp.tool()
async def get_customer_by_email(email: str) -> str:
    """
    Look up a customer profile by their email address.
    """
    logger.info(f"Tool Call: get_customer_by_email(email='{email}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            url = f"{API_BASE_URL}/customers/search"
            params = {"email": email}
            logger.info(f"Backend Request: GET {url} params={params}")
            
            response = await client.get(url, params=params)
            response.raise_for_status()
            result_str = json.dumps(response.json(), indent=2)
        return result_str
    except Exception as e:
        logger.error(f"Error in get_customer_by_email: {str(e)}")
        return f"Error fetching customer profile: {str(e)}"

@mcp.tool()
async def add_to_cart(cart_id: str, sku: str, quantity: int = 1) -> str:
    """
    Add a product to the cart using its SKU.
    """
    logger.info(f"Tool Call: commit_to_bag(cart_id='{cart_id}', sku='{sku}', quantity={quantity})")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            # The backend API requires 'sku' as a request parameter
            params = {"sku": sku, "quantity": quantity}
            url = f"{API_BASE_URL}/carts/{cart_id}/items"
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            logger.info(f"Backend Response: Status={response.status_code}")
            response.raise_for_status()
            result = response.json()
            logger.info(f"Item Added. New Version: {result.get('version')}")
            result_str = json.dumps(result, indent=2)
        return result_str
    except Exception as e:
        logger.error(f"Error in add_to_cart: {str(e)}")
        return f"Error adding product to cart: {str(e)}"

@mcp.tool()
async def set_shipping_address(cart_id: str, first_name: str, last_name: str, street: str, city: str, postal_code: str, country: str = "DE") -> str:
    """
    Set the shipping address for the cart.
    """
    logger.info(f"Tool Call: set_shipping_address(cart_id='{cart_id}', street='{street}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
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
            logger.info(f"Shipping Address Set for cart {cart_id}")
            result_str = json.dumps(result, indent=2)
        return result_str
    except Exception as e:
        logger.error(f"Error in set_shipping_address: {str(e)}")
        return f"Error setting shipping address: {str(e)}"

@mcp.tool()
async def get_cart(cart_id: str) -> str:
    """
    Retrieve the current state of a cart.
    """
    logger.info(f"Tool Call: get_cart(cart_id='{cart_id}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            url = f"{API_BASE_URL}/carts/{cart_id}"
            logger.info(f"Backend Request: GET {url}")
            
            response = await client.get(url)
            response.raise_for_status()
            result_str = json.dumps(response.json(), indent=2)
        return result_str
    except Exception as e:
        logger.error(f"Error in get_cart: {str(e)}")
        return f"Error fetching cart: {str(e)}"

@mcp.tool()
async def place_order(cart_id: str, version: int) -> str:
    """
    Finalize the checkout and place a formal order.
    IMPORTANT: This tool should only be called AFTER the customer has confirmed payment 
    via the Stripe URL generated by 'create_stripe_checkout'.
    """
    logger.info(f"Tool Call: place_order(cart_id='{cart_id}', version={version})")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            # SAFETY CHECK: Verify that the cart has a payment attached
            cart_url = f"{API_BASE_URL}/carts/{cart_id}"
            logger.info(f"Pre-Order Safety Check: GET {cart_url}")
            cart_response = await client.get(cart_url)
            
            if cart_response.status_code == 200:
                cart_data = cart_response.json()
                payment_info = cart_data.get("paymentInfo")
                if not payment_info or not payment_info.get("payments"):
                    logger.warning(f"Order blocking: No payment attached to cart {cart_id}")
                    return "ERROR: No payment method is attached to this cart. Please use 'create_stripe_checkout' to initiate payment before placing the order."
            
            params = {"cartId": cart_id, "cartVersion": version}
            url = f"{API_BASE_URL}/orders/from-cart"
            logger.info(f"Backend Request: POST {url} params={params}")
            
            response = await client.post(url, params=params)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            if response.status_code == 200:
                result = response.json()
                logger.info(f"ORDER PLACED SUCCESSFULLY: {result.get('id')}")
                return json.dumps(result, indent=2)
            
            # If placement fails (e.g. cart already ordered), try to find the order
            logger.warning(f"Order placement failed with {response.status_code}. Checking if order already exists...")
            search_url = f"{API_BASE_URL}/orders/cart/{cart_id}"
            search_res = await client.get(search_url)
            
            if search_res.status_code == 200:
                search_data = search_res.json()
                if search_data.get("results") and len(search_data["results"]) > 0:
                    order = search_data["results"][0]
                    logger.info(f"Existing order found: {order.get('id')}. Returning existing order.")
                    return json.dumps(order, indent=2)
            
            response.raise_for_status()
            return f"Error placing order: {response.text}"
    except Exception as e:
        logger.error(f"Error in place_order: {str(e)}")
        # Fallback search if we hit an exception
        try:
            async with httpx.AsyncClient(timeout=10.0) as client:
                search_url = f"{API_BASE_URL}/orders/cart/{cart_id}"
                search_res = await client.get(search_url)
                if search_res.status_code == 200:
                    search_data = search_res.json()
                    if search_data.get("results") and len(search_data["results"]) > 0:
                        return json.dumps(search_data["results"][0], indent=2)
        except:
            pass
        return f"Error finalizing order: {str(e)}"

@mcp.tool()
async def get_customer_profile(customer_id: str) -> str:
    """
    Retrieve a customer's profile and order history.
    """
    logger.info(f"Tool Call: get_customer_profile(customer_id='{customer_id}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            url = f"{API_BASE_URL}/customers/{customer_id}"
            logger.info(f"Backend Request: GET {url}")
            
            response = await client.get(url)
            logger.info(f"Backend Response: Status={response.status_code}")
            
            response.raise_for_status()
            result = response.json()
            return json.dumps(result, indent=2)
    except Exception as e:
        logger.error(f"Error in get_customer_profile: {str(e)}")
        return f"Error retrieving customer record for {customer_id}: {str(e)}"

@mcp.tool()
async def update_customer_profile(customer_id: str, first_name: str = "", last_name: str = "", email: str = "") -> str:
    """
    Update a customer's profile details.
    """
    logger.info(f"Tool Call: update_customer_profile(customer_id='{customer_id}', first_name='{first_name}', last_name='{last_name}', email='{email}')")
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
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
        logger.error(f"Error in update_customer_profile: {str(e)}")
        return f"Error updating customer profile for {customer_id}: {str(e)}"

if __name__ == "__main__":
    mcp.run()
