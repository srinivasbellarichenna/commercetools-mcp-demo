# Implementation Tasks

- [x] **Phase 1: Implementation**
  - [x] Fetch customer from Commercetools in `CartServiceImpl.java` when setting customer ID
  - [x] Apply `setCustomerId` and `setCustomerEmail` actions to the cart
  - [x] Modify `PaymentController.java` to return Stripe session JSON directly using `.toJson()`
- [x] **Phase 2: Verification**
  - [x] Compile `cart-service` and `payment-service` with Maven
  - [x] Rebuild and restart the container services
  - [x] Place a test order on the storefront and verify it succeeds and links both Customer ID and Email in Commercetools
- [x] **Phase 3: get_orders_by_customer MCP Tool**
  - [x] Implement the pagination parameter additions in the order service Java backend
  - [x] Implement the `get_orders_by_customer` MCP tool in the Python server
  - [x] Rebuild and restart container services
  - [x] Add automated test (`test_get_orders.py`) and verify it runs successfully inside the container

