import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingBag, User, Search, Menu } from 'lucide-react';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';

const Nav = () => {
  const { cart } = useContext(CartContext);
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();

  const cartCount = cart?.lineItems?.reduce((acc, item) => acc + item.quantity, 0) || 0;

  return (
    <nav style={{ 
      position: 'fixed', 
      top: 0, 
      left: 0, 
      right: 0, 
      zIndex: 1000, 
      height: '90px', 
      display: 'flex', 
      alignItems: 'center',
      transition: 'var(--transition)'
    }}>
      <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
        <div style={{ display: 'flex', gap: '3rem', alignItems: 'center' }}>
          <Link to="/" style={{ 
            fontFamily: 'var(--font-display)', 
            fontSize: '1.8rem', 
            fontWeight: '600', 
            color: 'var(--bg-secondary)',
            letterSpacing: '0.15em'
          }}>
            KESTREL
          </Link>
          
          <div style={{ display: 'flex', gap: '2rem' }}>
            <Link to="/" className="nav-link">Collection</Link>
            <Link to="/story" className="nav-link">Our Story</Link>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
          <button style={{ color: 'var(--bg-secondary)', opacity: 0.8 }}><Search size={20} /></button>
          
          <Link to="/cart" style={{ position: 'relative', color: 'var(--bg-secondary)', opacity: 0.8 }}>
            <ShoppingBag size={20} />
            {cartCount > 0 && (
              <span style={{ 
                position: 'absolute', 
                top: '-8px', 
                right: '-8px', 
                backgroundColor: 'var(--bg-secondary)', 
                color: 'var(--bg-primary)', 
                fontSize: '10px', 
                width: '18px', 
                height: '18px', 
                borderRadius: '50%', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                fontWeight: 'bold'
              }}>
                {cartCount}
              </span>
            )}
          </Link>

          <Link to={user ? "/account" : "/auth"} style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: '0.5rem', 
            backgroundColor: 'rgba(44, 62, 45, 0.05)', 
            padding: '0.6rem 1.2rem', 
            borderRadius: '30px',
            color: 'var(--bg-secondary)',
            fontSize: '0.85rem',
            fontWeight: '600'
          }}>
            <User size={18} />
            {user ? 'Atelier' : 'Sign In'}
          </Link>
          
          <button style={{ display: 'none', color: 'var(--bg-secondary)' }} className="mobile-menu"><Menu size={24} /></button>
        </div>
      </div>
    </nav>
  );
};

export default Nav;
