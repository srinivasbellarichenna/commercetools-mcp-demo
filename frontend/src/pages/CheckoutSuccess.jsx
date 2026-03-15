import React, { useEffect, useContext, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { CheckCircle, Package, ArrowRight, ShieldCheck } from 'lucide-react';
import { CartContext } from '../context/CartContext';

const CheckoutSuccess = () => {
  const [searchParams] = useSearchParams();
  const { refreshCart } = useContext(CartContext);
  const [orderId, setOrderId] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const finalizeOrder = async () => {
      const cartId = searchParams.get('cartId');
      const cartVersion = searchParams.get('cartVersion');
      
      if (cartId && cartVersion) {
        setLoading(true);
        try {
          const res = await fetch(`http://localhost:8085/api/orders/from-cart?cartId=${cartId}&cartVersion=${cartVersion}`, {
            method: 'POST'
          });
          if (res.ok) {
            const data = await res.json();
            setOrderId(data.id);
            // Clear the cart on success
            localStorage.removeItem('cartId');
            refreshCart();
          }
        } catch (e) {
          console.error("Order creation failed", e);
        } finally {
          setLoading(false);
        }
      }
    };

    finalizeOrder();
  }, [searchParams]);

  if (loading) return (
    <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', background: 'var(--bg-primary)', gap: '2rem' }}>
      <div className="animate-spin" style={{ width: '48px', height: '48px', border: '3px solid rgba(44, 62, 45, 0.1)', borderTopColor: 'var(--bg-secondary)', borderRadius: '50%' }}></div>
      <p style={{ fontFamily: 'var(--font-display)', letterSpacing: '0.1em', opacity: 0.6 }}>ARCHIVING YOUR ACQUISITION...</p>
    </div>
  );

  return (
    <div className="container" style={{ paddingTop: '160px', minHeight: '80vh', paddingBottom: '120px', textAlign: 'center' }}>
      <motion.div 
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.8, ease: [0.4, 0, 0.2, 1] }}
        style={{ maxWidth: '600px', margin: '0 auto' }}
      >
        <div style={{ 
          width: '80px', 
          height: '80px', 
          borderRadius: '50%', 
          backgroundColor: 'var(--bg-secondary)', 
          color: 'var(--bg-primary)', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          margin: '0 auto 3rem'
        }}>
          <CheckCircle size={40} />
        </div>

        <h1 style={{ fontFamily: 'var(--font-display)', fontSize: '3.5rem', marginBottom: '1.5rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>
          Acquisition Secured
        </h1>
        
        <p style={{ fontSize: '1.2rem', color: 'var(--text-secondary)', lineHeight: 1.8, marginBottom: '4rem' }}>
          Your artisanal selection is now part of your heritage. Our master craftsmen in the Cotswolds are preparing your pieces for their journey to your collection.
        </p>

        <div style={{ 
          backgroundColor: 'var(--bg-surface)', 
          padding: '2.5rem', 
          borderRadius: '20px', 
          marginBottom: '4rem',
          boxShadow: 'var(--shadow-soft)',
          border: '1px solid rgba(44, 62, 45, 0.05)',
          textAlign: 'left'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
            <Package size={20} style={{ color: 'var(--accent-secondary)' }} />
            <span style={{ fontWeight: 600, color: 'var(--bg-secondary)', letterSpacing: '0.05em' }}>ORDER DETAILS</span>
          </div>
          
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.95rem', opacity: 0.8, marginBottom: '0.75rem' }}>
            <span>Status</span>
            <span style={{ color: 'var(--bg-secondary)', fontWeight: 600 }}>Archived & Committed</span>
          </div>
          
          {orderId && (
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.95rem', opacity: 0.8 }}>
              <span>Manifest ID</span>
              <span style={{ fontFamily: 'monospace', fontWeight: 600 }}>{orderId}</span>
            </div>
          )}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <Link to="/" className="btn-primary" style={{ padding: '1.4rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.75rem' }}>
            Return to the Collection <ArrowRight size={20} />
          </Link>
          <Link to="/account" style={{ color: 'var(--accent-secondary)', fontWeight: 600, textDecoration: 'none', fontSize: '0.95rem' }}>
            View your archival records
          </Link>
        </div>

        <div style={{ mt: '6rem', pt: '3rem', borderTop: '1px solid rgba(44, 62, 45, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', fontSize: '0.8rem', opacity: 0.4, letterSpacing: '0.1em' }}>
          <ShieldCheck size={14} /> KESTREL SECURE ARCHIVE
        </div>
      </motion.div>
    </div>
  );
};

export default CheckoutSuccess;
