import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Trash2, ArrowRight, ShoppingBag, Loader2 } from 'lucide-react';
import { CartContext } from '../context/CartContext';

const Cart = () => {
  const { cart, removeFromCart } = useContext(CartContext);
  const navigate = useNavigate();

  if (!cart) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: 'var(--bg-primary)' }}>
      <Loader2 className="animate-spin" size={48} style={{ color: 'var(--bg-secondary)', opacity: 0.5 }} />
    </div>
  );

  const isEmpty = !cart.lineItems || cart.lineItems.length === 0;

  return (
    <div className="container" style={{ paddingTop: '160px', minHeight: '80vh', paddingBottom: '100px' }}>
      <div style={{ marginBottom: '4rem' }}>
        <p style={{ color: 'var(--accent-secondary)', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.2rem', fontWeight: 600, marginBottom: '0.5rem' }}>Acquisition Path</p>
        <h1 style={{ fontSize: '3.5rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>Your Bag</h1>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: isEmpty ? '1fr' : '1.5fr 1fr', gap: '5rem', alignItems: 'start' }}>
        <div style={{ borderTop: '1px solid rgba(44, 62, 45, 0.1)' }}>
          <AnimatePresence>
            {!isEmpty ? cart.lineItems.map((item) => (
              <motion.div 
                key={item.id}
                layout
                initial={{ opacity: 1 }}
                exit={{ opacity: 0, x: -50 }}
                style={{ 
                  display: 'flex', 
                  gap: '2rem', 
                  padding: '2.5rem 0', 
                  borderBottom: '1px solid rgba(44, 62, 45, 0.1)',
                  alignItems: 'center'
                }}
              >
                <div style={{ width: '120px', height: '150px', borderRadius: '12px', overflow: 'hidden', border: '1px solid rgba(44, 62, 45, 0.05)', backgroundColor: 'white' }}>
                  <img src={item.variant?.images?.[0]?.url || 'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?q=80&w=200'} alt={item.name?.['en-US'] || item.name?.['en-GB'] || item.name?.en || (typeof item.name === 'object' ? Object.values(item.name)[0] : item.name)} style={{ width: '100%', height: '100%', objectFit: 'cover', filter: 'sepia(0.1)' }} />
                </div>
                
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                    <div>
                      <h4 style={{ fontSize: '1.4rem', color: 'var(--bg-secondary)', marginBottom: '0.5rem' }}>{item.name?.['en-US'] || item.name?.['en-GB'] || item.name?.en || (typeof item.name === 'object' ? Object.values(item.name)[0] : item.name)}</h4>
                      <p style={{ color: 'var(--accent-secondary)', fontSize: '0.9rem' }}>Qty: {item.quantity}</p>
                    </div>
                    <div style={{ fontWeight: 600, fontSize: '1.1rem', color: 'var(--bg-secondary)' }}>
                      {(item.totalPrice?.centAmount / 100).toFixed(2)} {item.totalPrice?.currencyCode}
                    </div>
                  </div>
                  <button 
                    onClick={() => removeFromCart(item.id)}
                    style={{ color: 'var(--accent-secondary)', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 600 }}
                  >
                    <Trash2 size={16} /> Remove from Bag
                  </button>
                </div>
              </motion.div>
            )) : (
              <div style={{ textAlign: 'center', padding: '10rem 0' }}>
                <ShoppingBag size={64} style={{ color: 'var(--bg-secondary)', opacity: 0.1, marginBottom: '2rem' }} />
                <h3 style={{ fontSize: '1.5rem', color: 'var(--bg-secondary)', marginBottom: '1rem' }}>Your bag is waiting to be filled</h3>
                <Link to="/" className="btn-outline">Explore the Collection</Link>
              </div>
            )}
          </AnimatePresence>
        </div>

        {!isEmpty && (
          <motion.div 
            initial={{ opacity: 0, y: 30 }} 
            animate={{ opacity: 1, y: 0 }}
            style={{ 
              backgroundColor: 'var(--bg-surface)', 
              padding: '3rem', 
              borderRadius: '20px', 
              boxShadow: 'var(--shadow-soft)',
              border: '1px solid rgba(44, 62, 45, 0.05)',
              position: 'sticky',
              top: '140px'
            }}
          >
            <h3 style={{ fontSize: '1.6rem', color: 'var(--bg-secondary)', marginBottom: '2rem', borderBottom: '1px solid rgba(44, 62, 45, 0.1)', paddingBottom: '1rem' }}>Summary</h3>
            
            {cart.discountCodes?.length > 0 && (
              <div style={{ backgroundColor: 'rgba(44, 62, 45, 0.05)', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '0.9rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>
                Patron benefits applied to this acquisition.
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.2rem', color: 'var(--text-secondary)' }}>
              <span>Subtotal</span>
              <span>{(cart.totalPrice?.centAmount / 100).toFixed(2)} {cart.totalPrice?.currencyCode}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '2rem', color: 'var(--text-secondary)' }}>
              <span>Delivery</span>
              <span style={{ fontSize: '0.85rem', fontStyle: 'italic' }}>Calculated at next step</span>
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.6rem', fontWeight: 600, color: 'var(--bg-secondary)', marginBottom: '3rem', borderTop: '1px solid rgba(44, 62, 45, 0.1)', paddingTop: '1.5rem' }}>
              <span>Total</span>
              <span>{(cart.totalPrice?.centAmount / 100).toFixed(2)} {cart.totalPrice?.currencyCode}</span>
            </div>

            <button 
              className="btn-primary" 
              style={{ width: '100%', padding: '1.4rem', fontSize: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.75rem' }}
              onClick={() => navigate('/checkout')}
            >
              Secure Checkout <ArrowRight size={20} />
            </button>
          </motion.div>
        )}
      </div>
    </div>
  );
};

export default Cart;
