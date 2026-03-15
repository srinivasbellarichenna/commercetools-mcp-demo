# Commercetools TypeScript SDK — Headless Commerce Reference

## Overview
Procedural guide for building headless storefronts using the official Commercetools TypeScript SDK. Covers cart, checkout, and customer account operations via the "Me" endpoints.

## Architecture
- **Frontic Client**: Product data, categories, search, projections (read)
- **Commercetools SDK**: Cart, checkout/orders, customer account, payments (write/session)

## SDK Setup
### Dependencies
```bash
npm install @commercetools/platform-sdk @commercetools/ts-client
```

### Client Builder
Use a singleton pattern to reuse the client and cache OAuth tokens.

```typescript
import { ClientBuilder } from '@commercetools/ts-client'
import { createApiBuilderFromCtpClient } from '@commercetools/platform-sdk'

let _apiRoot = null

export function getApiRoot() {
  if (_apiRoot) return _apiRoot
  
  const authOptions = {
    host: AUTH_HOST,
    projectKey: PROJECT_KEY,
    credentials: { clientId: CLIENT_ID, clientSecret: CLIENT_SECRET },
    scopes: SCOPES.split(' '),
    httpClient: fetch,
  }

  const httpOptions = { host: API_HOST, httpClient: fetch }

  const client = new ClientBuilder()
    .withClientCredentialsFlow(authOptions)
    .withHttpMiddleware(httpOptions)
    .build()

  _apiRoot = createApiBuilderFromCtpClient(client)
    .withProjectKey({ projectKey: PROJECT_KEY })
  return _apiRoot
}
```

## Core Workflows
### Anonymous Session → Cart → Login → Checkout
1. Build client with anonymous/client-credentials flow.
2. Create cart: `apiRoot.carts().post({ body: cartDraft })`
3. Add items: `apiRoot.carts().withId({ ID }).post({ body: update })`
4. Login: `apiRoot.me().login().post({ body: credentials })` (cart transfers automatically).
5. Set addresses/shipping/payment.
6. Create order: `apiRoot.me().orders().post({ body: orderDraft })`

## Common Patterns & Pitfalls
- **Singleton API Root**: Reuse the client instance to avoid excessive token requests.
- **Execute()**: Always call `.execute()` on the request builder.
- **Retry on 409**: Handle version conflicts by re-fetching and retrying.
- **Batch Actions**: Send multiple actions in one update request for atomicity.
- **Me Endpoints**: Use `/me` endpoints for storefront-scoped operations.
- **Money Type**: Use `centAmount` (integer) for all prices.
- **Anonymous ID**: Use a stable `anonymousId` in cookies instead of `cartId`.

## Security
- **Cookie Signing**: HMAC-sign customer session cookies.
- **Predicate Injection**: Validate all variables used in `where` clauses.
- **Input Validation**: Validate all user data at the API boundary.
- **Rate Limiting**: Protect login/register endpoints.
- **Error Sanitization**: Never expose raw SDK errors to the client.
