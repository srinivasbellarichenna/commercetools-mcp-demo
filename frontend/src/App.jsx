import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Nav from './components/Nav';
import Home from './pages/Home';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import Auth from './pages/Auth';
import Account from './pages/Account';
import CheckoutSuccess from './pages/CheckoutSuccess';

function App() {
  return (
    <Router>
      <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
        <Nav />
        <main style={{ flex: 1 }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/product/:id" element={<ProductDetail />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/checkout" element={<Checkout />} />
            <Route path="/checkout/success" element={<CheckoutSuccess />} />
            <Route path="/auth" element={<Auth />} />
            <Route path="/account" element={<Account />} />
          </Routes>
        </main>
        
        <footer style={{ 
          padding: '4rem 2rem', 
          backgroundColor: 'var(--bg-secondary)', 
          color: 'var(--bg-primary)',
          marginTop: 'auto'
        }}>
          <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '1.2rem', letterSpacing: '0.15em', fontWeight: 600 }}>
              AGENTIC COMMERCE
            </div>
            <div style={{ fontSize: '0.8rem', opacity: 0.7, letterSpacing: '0.05em' }}>
              &copy; {new Date().getFullYear()} COMPOSABLE AGENTIC COMMERCE.
            </div>
          </div>
        </footer>
      </div>
    </Router>
  );
}

export default App;
