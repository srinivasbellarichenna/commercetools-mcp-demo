import logging

logger = logging.getLogger("mcp-contract")

# Map standard orchestration fields -> foundational MCP fields
PARAM_MAPPING = {
    "search_products": {
        "query": "keyword",
        "brand": "brand",
        "priceMax": "limit", # Wait... foundational MCP limit isn't price_max. It doesn't have priceMax!
        "limit": "limit",
        "offset": "offset"
    },
    "get_piece_detail": {
        "piece_id": "piece_id"
    },
    "add_to_cart": {
        "cart_id": "cart_id",
        "sku": "sku",
        "quantity": "quantity"
    },
    "place_order": {
        "cart_id": "cart_id",
        "version": "version"
    }
}

# Strict Type Validations & Required Fields
MCP_SCHEMAS = {
    "search_products": {"required": ["keyword"]},
    "get_piece_detail": {"required": ["piece_id"]},
    "add_to_cart": {"required": ["cart_id", "sku"]},
    "place_order": {"required": ["cart_id", "version"]}
}

class MCPContractException(Exception):
    pass

def map_and_validate_args(action: str, inputs: dict) -> dict:
    """
    1. Removes hard nulls
    2. Maps semantic Agent keys to strict Backend MCP keys
    3. Assertions
    """
    mapping = PARAM_MAPPING.get(action, {})
    
    # 1. Map Inputs safely
    mapped_args = {}
    for k, v in inputs.items():
        if v is not None and v != "":
            target_key = mapping.get(k, k)
            mapped_args[target_key] = v
            
    # 2. Strict Schema Guarding
    schema = MCP_SCHEMAS.get(action)
    if schema:
        missing = [req for req in schema["required"] if req not in mapped_args]
        if missing:
            logger.error(f"Contract Failure: Action '{action}' missing required args: {missing}. Given: {mapped_args}")
            raise MCPContractException(f"Missing required parameter(s): {', '.join(missing)} for action '{action}'")
    
    return mapped_args
