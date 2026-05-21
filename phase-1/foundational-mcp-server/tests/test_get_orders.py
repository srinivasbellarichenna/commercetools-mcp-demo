import pytest
import json
from main import get_orders_by_customer

@pytest.mark.asyncio
async def test_get_orders_by_customer():
    # Call the tool with the test customer ID
    customer_id = "5d4e77af-b72e-42e0-81da-8532c5c3015a"
    result_str = await get_orders_by_customer(customer_id=customer_id, limit=2, offset=0)
    
    # Assert it didn't fail
    assert not result_str.startswith("Error")
    
    # Check details of response
    orders = json.loads(result_str)
    assert isinstance(orders, list)
    
    print(f"\nRetrieved {len(orders)} orders for customer {customer_id}")
    for order in orders:
        print(f"Order ID: {order.get('order_id')}, CreatedAt: {order.get('created_at')}")
        print(f"  Line items:")
        for item in order.get('line_items', []):
            print(f"    - Name: {item.get('product_name')}, SKU: {item.get('sku')}, Qty: {item.get('quantity')}")
            
    # We should have at most 2 orders due to limit=2
    assert len(orders) <= 2
    
    for order in orders:
        assert "order_id" in order
        assert "order_number" in order
        assert "created_at" in order
        assert "status" in order
        assert "total_price" in order
        assert "line_items" in order
        
        # Verify line items shape
        for item in order["line_items"]:
            assert "product_name" in item
            assert "sku" in item
            assert "quantity" in item
