# Frontend Application: Architectural Walkthrough

This document outlines the core technical decisions implemented within the React frontend application.

## 1. Global State Management (CartContext)
E-commerce applications require highly synchronized state, especially regarding the user's cart. 
We utilized the React Context API (`CartContext.jsx`) to create a global source of truth. 
- **Resilience**: The context fetches the `cartId` from `localStorage` upon initialization. If a cart is found, it automatically re-hydrates the application state from the backend.
- **Synchronization**: We explicitly implemented an `await refreshCart()` hook that fires after state-mutating actions (like selecting a shipping method). This guarantees that the UI (e.g., the "Total Due" calculation) is always perfectly aligned with the backend's source of truth.

## 2. Dynamic Checkout Flow
The `Checkout.jsx` component implements a robust state machine for the checkout process, tracking progress from Address -> Billing -> Shipping Method -> Final Review.
- **Validation**: It enforces strict validation blocks, preventing users from advancing if critical information (like city or first name) is missing.
- **Real-time Updates**: When a shipping method is selected, the frontend instantly commits this to the backend API and refreshes the cart to display the newly calculated grand total (including delivery fees).

## 3. Production Dockerization
The frontend is optimized for production deployment using a **multi-stage Docker build**.
1. **Build Stage**: It uses a lightweight Node.js image (`node:20-slim`) to install dependencies and run the Vite build process (`npm run build`).
2. **Serve Stage**: It discards the heavy Node modules and copies only the compiled static assets (`/dist`) into an `nginx:stable-alpine` image.
This results in a highly secure, incredibly small container footprint that serves the application at lightning speeds.

## 4. Backend API Integration Mapping

The frontend communicates with the backend microservices exclusively through the API Gateway. Below is a mapping of key frontend workflows to the backend endpoints they consume:

### Core State & Session Management
- **Cart Context (`CartContext.jsx`)**:
  - `GET /api/carts/{id}`: Synchronizes the local session.
  - `POST /api/carts`: Initializes new shopping sessions.
  - `POST /api/carts/{id}/items`: Adds products.
  - `DELETE /api/carts/{id}/items/{itemId}`: Removes line items.
  - `POST /api/carts/{id}/customer`: Links an anonymous cart to a logged-in customer.

- **Authentication (`Auth.jsx`)**:
  - `POST /api/customers/login`: Authenticates existing users.
  - `POST /api/customers/register`: Provisions new accounts.

### Product Discovery
- **Product Grid (`ProductGrid.jsx`)**:
  - `GET /api/products`: Fetches the paginated product collection.
- **Product Detail (`ProductDetail.jsx`)**:
  - `GET /api/products/{id}`: Retrieves comprehensive data for a single product.

### Checkout Lifecycle (`Checkout.jsx`)
- `GET /api/customers/{id}`: Pre-populates shipping data from the user's stored addresses.
- `POST /api/carts/{id}/shipping-address`: Persists the delivery destination.
- `GET /api/carts/{id}/shipping-methods`: Retrieves valid shipping options.
- `POST /api/carts/{id}/shipping-method`: Saves the selected delivery method.
- `POST /api/carts/{id}/billing-address`: Persists billing info.
- `POST /api/payments/checkout`: Requests a secure Stripe Checkout session URL.
- `POST /api/orders/from-cart`: Converts the finalized cart into a permanent Order.
