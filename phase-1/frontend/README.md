# Foundation Layer: Frontend Application

This directory contains the customer-facing storefront for the Composable Agentic Commerce project. It provides a modern, responsive interface for users to browse the catalog, manage their cart, and securely checkout.

## 🎨 Technologies Used

- **Framework**: React 18
- **Build Tool**: Vite
- **Styling**: Vanilla CSS (custom design system)
- **Routing**: React Router DOM
- **Deployment**: Nginx (via Docker)

## 🏗️ Architecture & Features

The frontend is designed to consume the Unified API Gateway (`http://localhost:8085`) provided by the Java backend. 
Key features include:
- **Product Discovery**: Browse the catalog, view high-quality images, and read specifications.
- **Cart Context**: Global state management for shopping carts, instantly reflecting changes made either manually or by an AI agent.
- **Secure Checkout**: Multi-step checkout flow featuring address validation, shipping method selection, and seamless handoff to Stripe for PCI-compliant payment tokenization.

## 🚀 Building and Running

### Docker (Recommended)
The frontend is automatically built and served via Nginx alongside the rest of the ecosystem:
```bash
# From the backend directory
docker compose up -d --build
```
The application will be available at `http://localhost:3001`.

### Local Development
To run the Vite development server with Hot Module Replacement (HMR):
```bash
npm install
npm run dev
```
