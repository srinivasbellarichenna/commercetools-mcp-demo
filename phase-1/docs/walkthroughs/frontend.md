# Frontend Walkthrough: React Commerce App

The frontend is a modern, responsive web application built with React and Vite. It serves as the primary user interface for customers to browse products, manage their cart, and complete the checkout process.

## Architecture & State Management

The frontend is built on a modular React architecture that prioritizes performance and maintainability.

### Global Context Providers

* **[AuthContext](../../frontend/src/context/AuthContext.jsx)**: Manages the user's authentication lifecycle. It handles login, registration, and maintains the `user` state globally. It also handles synchronization of customer data from Commercetools.
* **[CartContext](../../frontend/src/context/CartContext.jsx)**: Orchestrates all shopping cart operations. It manages the `cart` state, handles item additions/removals, and ensures the cart is correctly associated with the authenticated user. It utilizes `localStorage` for cart persistence across sessions.

### Routing & Navigation

* **[App.jsx](../../frontend/src/App.jsx)**: The central hub that defines the application's structure and routes using `react-router-dom`.
* **[Nav.jsx](../../frontend/src/components/Nav.jsx)**: A global navigation component that provides access to the catalog, cart (with real-time item count), and user account/authentication pages.

## Key UI Components & Pages

### Core Components

* **[Hero.jsx](../../frontend/src/components/Hero.jsx)**: The primary landing section, featuring smooth entrance animations powered by `framer-motion` and an artisanal aesthetic.
* **[ProductGrid.jsx](../../frontend/src/components/ProductGrid.jsx)**: Responsively renders the product catalog, leveraging `ProductCard` for individual items.
* **[ProductCard.jsx](../../frontend/src/components/ProductCard.jsx)**: A reusable card component that displays product projections, pricing, and provides quick navigation to details.

### Sophisticated Workflows

* **[Checkout.jsx](../../frontend/src/pages/Checkout.jsx)**: A complex 4-step orchestrated workflow:
  1. **Shipping**: Capture and validate delivery address, with support for saved addresses from the user profile.
  2. **Billing**: Capture billing details or sync with the shipping address.
  3. **Delivery**: Fetch and select available Commercetools shipping methods.
  4. **Payment**: Final review and redirection to a secure Stripe Checkout session.

## Backend API Integration Mapping

The frontend communicates with the backend microservices through the API Gateway. Below is a mapping of key frontend modules to the specific backend endpoints they consume:

### Core State & Session Management

* **[CartContext](../../frontend/src/context/CartContext.jsx)**:
  * `GET /api/carts/{id}`: Synchronizes the local session with the backend state.
  * `POST /api/carts`: Initializes new shopping sessions (anonymous or user-linked).
  * `POST /api/carts/{id}/items`: Handles adding products to the active cart.
  * `DELETE /api/carts/{id}/items/{itemId}`: Handles line item removal.
  * `POST /api/carts/{id}/customer`: Links an anonymous cart to a logged-in customer.

* **[Auth.jsx](../../frontend/src/pages/Auth.jsx)**:
  * `POST /api/customers/login`: Authenticates existing users.
  * `POST /api/customers/register`: Provisions new customer accounts.

### Product Discovery

* **[ProductGrid.jsx](../../frontend/src/components/ProductGrid.jsx)**:
  * `GET /api/products`: Fetches the paginated product collection with support for sorting and filtering.
* **[ProductDetail.jsx](../../frontend/src/pages/ProductDetail.jsx)**:
  * `GET /api/products/{id}`: Retrieves comprehensive data for a single product projection.

### Checkout & Order Lifecycle

* **[Checkout.jsx](../../frontend/src/pages/Checkout.jsx)**:
  * `GET /api/customers/{id}`: Pre-populates shipping data from the user's stored addresses.
  * `POST /api/carts/{id}/shipping-address`: Persists the delivery destination to the cart.
  * `GET /api/carts/{id}/shipping-methods`: Retrieves valid shipping options based on the address.
  * `POST /api/carts/{id}/shipping-method`: Saves the selected delivery method.
  * `POST /api/carts/{id}/billing-address`: Persists billing info (if different from shipping).
  * `POST /api/payments/checkout`: Requests a secure Stripe Checkout session URL.

* **[CheckoutSuccess.jsx](../../frontend/src/pages/CheckoutSuccess.jsx)**:
  * `POST /api/orders/from-cart`: Converts the finalized and paid cart into a permanent Order.
  * `GET /api/payments/session/{sessionId}`: Verifies the payment status with Stripe.
  * `POST /api/customers/{id}/payments`: Vaults the payment method to the customer's profile if requested.

## Visual Design & Aesthetics

* **Thematic Styling**: Custom CSS variables defined in [index.css](../../frontend/src/index.css) create a consistent "Premium Artisanal" look with deep greens, soft creams, and elegant typography.
* **Micro-Animations**: Extensive use of `framer-motion` for page transitions, hover states, and progressive disclosure in forms to enhance perceived quality.

## Development & Deployment

### Configuration

* **[api.js](../../frontend/src/config/api.js)**: Centralizes service connectivity, pointing to the API Gateway.

### Local Development

To start the frontend application in development mode:

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

3. Start the development server:

   ```bash
   npm run dev
   ```

The application will typically be available at `http://localhost:5173`. It expects the API Gateway and the Agentic MCP Server (SSE) to be running and accessible.
