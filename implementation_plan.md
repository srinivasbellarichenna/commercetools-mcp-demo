# Map Customer details and Fix Order Creation

Resolve order creation issues in Commercetools by setting the customer's email on the cart and fixing a serialization error in the Stripe checkout session endpoint.

## User Review Required

> [!IMPORTANT]
> - `cart-service`: We will query the customer details from Commercetools using the customer ID to retrieve their email address, and apply both `setCustomerId` and `setCustomerEmail` update actions to the cart.
> - `payment-service`: We will modify `/api/payments/session/{sessionId}` to return `session.toJson()` (the raw JSON string from Stripe SDK) instead of returning the Java SDK `Session` object. This avoids a 500 error caused by Jackson trying to serialize internal Stripe SDK classes like `StripeResponse`.

## Open Questions

None. The issues and resolutions are fully diagnosed.

---

## Proposed Changes

### Cart Service (`cart-service`)

#### [MODIFY] [CartServiceImpl.java](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/backend/cart-service/src/main/java/com/example/cart/service/CartServiceImpl.java)
- In `setCustomerId`, fetch the Customer from Commercetools using their ID.
- Extract the customer's email.
- Update the cart by setting both customer ID and customer email.
- Gracefully fall back to only setting the ID if customer lookup fails.

### Payment Service (`payment-service`)

#### [MODIFY] [PaymentController.java](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/backend/payment-service/src/main/java/com/example/payment/controller/PaymentController.java)
- Change `@GetMapping("/session/{sessionId}")` return type to `Mono<ResponseEntity<String>>`.
- Add `produces = "application/json"` to the `@GetMapping` annotation.
- Map the retrieved session to `ResponseEntity.ok(session.toJson())`.

---

## Verification Plan

### Automated Tests
- Build and compile both `cart-service` and `payment-service`:
  ```bash
  mvn clean package -DskipTests -f phase-1/backend/cart-service/pom.xml
  mvn clean package -DskipTests -f phase-1/backend/payment-service/pom.xml
  ```

### Manual Verification
- Verify that `GET /api/payments/session/{sessionId}` returns the correct JSON string without 500 serialization error.
- Verify checkout and order placement flows.
- Verify that placing an order correctly maps customer email and ID using `get_order_details.py`.
