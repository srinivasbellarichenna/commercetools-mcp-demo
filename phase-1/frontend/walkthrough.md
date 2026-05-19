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
