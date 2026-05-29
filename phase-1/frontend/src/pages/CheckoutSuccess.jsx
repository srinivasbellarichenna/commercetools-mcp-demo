import React, { useEffect, useContext, useState, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { CheckCircle, Package, ArrowRight, ShieldCheck } from 'lucide-react';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import { API_BASE_URL } from '../config/api';

const CheckoutSuccess = () => {
  const [searchParams] = useSearchParams();
  const { refreshCart } = useContext(CartContext);
  const { user, refreshUser } = useContext(AuthContext);
  const [orderId, setOrderId] = useState(null);
  const [loading, setLoading] = useState(false);
  const hasFinalized = useRef(false);
  const hasSavedPayment = useRef(false);

  useEffect(() => {
    const finalizeOrder = async () => {
      const cartId = searchParams.get('cartId');
      const cartVersion = searchParams.get('cartVersion');
      const sessionId = searchParams.get('sessionId');
      
      if (cartId && !hasFinalized.current) {
        hasFinalized.current = true;
        setLoading(true);
        console.log("🚀 Finalizing order check for Cart:", cartId);
        
        try {
          // Check if order already exists for this cart (idempotency check)
          const checkRes = await fetch(`${API_BASE_URL}/orders/cart/${cartId}`);
          if (checkRes.ok) {
            const checkData = await checkRes.json();
            if (checkData.results && checkData.results.length > 0) {
              setOrderId(checkData.results[0].id);
              console.log("✨ Order already existed for Cart, retrieved ID:", checkData.results[0].id);
              localStorage.removeItem('cartId');
              refreshCart();
              setLoading(false);
              return;
            }
          }

          if (!cartVersion || !sessionId) {
            console.error("❌ Cannot finalize: cartVersion or sessionId missing.");
            setLoading(false);
            return;
          }

          console.log("🛒 Creating order from cart with version", cartVersion, "using session", sessionId);
          const res = await fetch(`${API_BASE_URL}/orders/from-cart?cartId=${cartId}&cartVersion=${cartVersion}&sessionId=${sessionId}`, {
            method: 'POST'
          });
          
          if (res.ok) {
            const data = await res.json();
            setOrderId(data.id);
            console.log("✅ Order created successfully:", data.id);
            
            // Clear the cart on success
            localStorage.removeItem('cartId');
            refreshCart();
          } else {
            console.error("❌ Order conversion failed:", res.status);
          }
        } catch (e) {
          console.error("❌ Order creation exception:", e);
        } finally {
          setLoading(false);
        }
      }
    };

    finalizeOrder();
  }, [searchParams]);

  useEffect(() => {
    const savePayment = async () => {
      const sessionId = searchParams.get('sessionId');
      const savePref = localStorage.getItem('savePaymentPreference');
      
      if (orderId && user && sessionId && savePref === 'true' && !hasSavedPayment.current) {
        hasSavedPayment.current = true;
        console.log("💳 Attempting to vault payment method for User:", user.id);
        
        try {
          const sessionRes = await fetch(`${API_BASE_URL}/payments/session/${sessionId}`);
          const sessionData = await sessionRes.json();
          
          const last4 = sessionData.payment_intent_data?.payment_method_options?.card?.last4 || '4242';
          const brand = sessionData.payment_intent_data?.payment_method_options?.card?.brand || 'visa';
          
          const saveUrl = `${API_BASE_URL}/customers/${user.id}/payment-methods`;
          const saveRes = await fetch(saveUrl, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              paymentToken: sessionId,
              last4,
              brand
            })
          });
          
          if (saveRes.ok) {
            console.log("✨ Payment method successfully vaulted.");
            await refreshUser();
            localStorage.removeItem('savePaymentPreference');
          } else {
            console.error("❌ Failed to vault payment method. Status:", saveRes.status);
          }
        } catch (e) {
          console.error("❌ Exception during payment vaulting:", e);
        }
      }
    };

    savePayment();
  }, [orderId, user, searchParams]);

  if (loading) return (
    <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', background: 'var(--bg-primary)', gap: '2rem' }}>
      <div className="animate-spin" style={{ width: '48px', height: '48px', border: '3px solid rgba(44, 62, 45, 0.1)', borderTopColor: 'var(--bg-secondary)', borderRadius: '50%' }}></div>
      <p style={{ fontFamily: 'var(--font-display)', letterSpacing: '0.1em', opacity: 0.6 }}>FINALIZING YOUR ORDER...</p>
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
          Order Confirmed
        </h1>
        
        <p style={{ fontSize: '1.2rem', color: 'var(--text-secondary)', lineHeight: 1.8, marginBottom: '4rem' }}>
          Your order has been successfully placed. We've sent a confirmation email to your registered address. You can track your order status in your account dashboard.
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
            <span style={{ color: 'var(--bg-secondary)', fontWeight: 600 }}>Confirmed & Processing</span>
          </div>
          
          {orderId && (
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.95rem', opacity: 0.8 }}>
              <span>Order ID</span>
              <span style={{ fontFamily: 'monospace', fontWeight: 600 }}>{orderId}</span>
            </div>
          )}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <Link to="/" className="btn-primary" style={{ padding: '1.4rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.75rem' }}>
            Return to Store <ArrowRight size={20} />
          </Link>
          <Link to="/account" style={{ color: 'var(--accent-secondary)', fontWeight: 600, textDecoration: 'none', fontSize: '0.95rem' }}>
            View your order history
          </Link>
        </div>

        <div style={{ marginTop: '6rem', paddingTop: '3rem', borderTop: '1px solid rgba(44, 62, 45, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', fontSize: '0.8rem', opacity: 0.4, letterSpacing: '0.1em' }}>
          <ShieldCheck size={14} /> SECURE AGENTIC PLATFORM
        </div>
      </motion.div>
    </div>
  );
};

export default CheckoutSuccess;
