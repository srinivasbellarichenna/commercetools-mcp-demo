import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import { API_BASE_URL } from '../config/api';

const Checkout = () => {
  const { cart, refreshCart, assignUserToCart } = useContext(CartContext);
  const { user, refreshUser } = useContext(AuthContext);
  const [step, setStep] = useState(1);
  const [shippingMethods, setShippingMethods] = useState([]);
  const [loading, setLoading] = useState(false);
  const [customerProfile, setCustomerProfile] = useState(null);
  const [saveToProfile, setSaveToProfile] = useState(false);
  const [savePaymentMethod, setSavePaymentMethod] = useState(false);
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    shippingAddress: {
      firstName: '',
      lastName: '',
      streetName: '',
      streetNumber: '',
      city: '',
      postalCode: '',
      country: 'DE',
    },
    billingAddress: {
      firstName: '',
      lastName: '',
      streetName: '',
      streetNumber: '',
      city: '',
      postalCode: '',
      country: 'DE',
    },
    sameAsShipping: true,
    shippingMethodId: '',
  });

  useEffect(() => {
    if (!cart) refreshCart();
    
    if (user) {
      fetch(`${API_BASE_URL}/customers/${user.id}`)
        .then(res => res.json())
        .then(data => setCustomerProfile(data))
        .catch(err => console.error("Failed to fetch profile", err));
    }
  }, [user]);

  const handleInputChange = (e, section) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [section]: {
        ...prev[section],
        [name]: value
      }
    }));
  };

  const handleSelectSavedAddress = (addr) => {
    setFormData(prev => ({
      ...prev,
      shippingAddress: {
        firstName: addr.firstName,
        lastName: addr.lastName,
        streetName: addr.streetName,
        streetNumber: addr.streetNumber,
        city: addr.city,
        postalCode: addr.postalCode,
        country: addr.country,
      }
    }));
  };

  const nextStep = async () => {
    if (step === 1) {
      setLoading(true);
      try {
        await fetch(`${API_BASE_URL}/carts/${cart.id}/shipping-address`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(formData.shippingAddress),
        });
        
        if (saveToProfile && user) {
          await fetch(`${API_BASE_URL}/customers/${user.id}/addresses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData.shippingAddress),
          });
          refreshUser();
        }

        const res = await fetch(`${API_BASE_URL}/carts/${cart.id}/shipping-methods`);
        const methods = await res.json();
        setShippingMethods(methods);
        setStep(2);
      } catch (e) {
        console.error("Failed to set shipping address", e);
      } finally {
        setLoading(false);
      }
    } else if (step === 2) {
      setLoading(true);
      try {
        const billingAddr = formData.sameAsShipping ? formData.shippingAddress : formData.billingAddress;
        await fetch(`${API_BASE_URL}/carts/${cart.id}/billing-address`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(billingAddr),
        });
        setStep(3);
      } catch (e) {
        console.error("Failed to set billing address", e);
      } finally {
        setLoading(false);
      }
    } else if (step === 3) {
      if (!formData.shippingMethodId) return;
      setLoading(true);
      try {
        await fetch(`${API_BASE_URL}/carts/${cart.id}/shipping-method?shippingMethodId=${formData.shippingMethodId}`, {
          method: 'POST',
        });
        setStep(4);
      } catch (e) {
        console.error("Failed to set shipping method", e);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleStripeCheckout = async () => {
    setLoading(true);
    try {
      if (user && cart && !cart.customerId) {
        await assignUserToCart(user.id);
      }

      const successUrl = `${window.location.origin}/checkout/success`;
      const cancelUrl = `${window.location.origin}/cart`;
      const res = await fetch(`${API_BASE_URL}/payments/checkout?cartId=${cart.id}&successUrl=${encodeURIComponent(successUrl)}&cancelUrl=${encodeURIComponent(cancelUrl)}`, { 
        method: 'POST' 
      });
      const data = await res.json();
      if (data.url) {
        if (savePaymentMethod) {
          localStorage.setItem('savePaymentPreference', 'true');
        } else {
          localStorage.removeItem('savePaymentPreference');
        }
        window.location.href = data.url;
      }
    } catch (e) {
      console.error("Payment initiation failed", e);
    } finally {
      setLoading(false);
    }
  };

  if (!cart) return <div className="container" style={{paddingTop: '150px', background: 'var(--bg-primary)'}}>Loading...</div>;

  return (
    <div className="container" style={{ paddingTop: '160px', minHeight: '80vh', paddingBottom: '120px' }}>
      <div style={{ maxWidth: '900px', margin: '0 auto' }}>
        
        {/* Progress Header */}
        <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
          <span style={{ backgroundColor: 'var(--bg-secondary)', color: 'var(--bg-primary)', padding: '0.75rem 1.5rem', borderRadius: '40px', fontWeight: 600, letterSpacing: '0.1em', fontSize: '0.85rem' }}>
            SECURE CHECKOUT
          </span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5rem', position: 'relative', padding: '0 2rem' }}>
          {[1, 2, 3, 4].map((s) => (
            <div key={s} style={{ 
              zIndex: 2,
              width: '45px', 
              height: '45px', 
              borderRadius: '50%', 
              backgroundColor: s <= step ? 'var(--bg-secondary)' : 'var(--bg-surface)',
              color: s <= step ? 'var(--bg-primary)' : 'var(--accent-secondary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontFamily: 'var(--font-display)',
              fontWeight: 600,
              boxShadow: s <= step ? '0 10px 20px rgba(44, 62, 45, 0.1)' : 'none',
              transition: 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)'
            }}>
              {s}
            </div>
          ))}
          <div style={{ 
            position: 'absolute', 
            top: '22.5px', 
            left: '2rem', 
            right: '2rem', 
            height: '1px', 
            backgroundColor: 'rgba(44, 62, 45, 0.1)', 
            zIndex: 1 
          }}>
            <motion.div 
              style={{ height: '100%', backgroundColor: 'var(--bg-secondary)' }}
              animate={{ width: `${((step - 1) / 3) * 100}%` }}
              transition={{ duration: 0.8, ease: [0.4, 0, 0.2, 1] }}
            />
          </div>
        </div>

        <AnimatePresence mode="wait">
          {step === 1 && (
            <motion.div 
              key="step1"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
            >
              <h2 style={{ fontFamily: 'var(--font-display)', fontSize: '2.5rem', marginBottom: '2.5rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>Where shall we send your order?</h2>
              
              {customerProfile?.addresses?.length > 0 && (
                <div style={{ marginBottom: '4rem' }}>
                  <h4 style={{ fontSize: '0.85rem', color: 'var(--accent-primary)', marginBottom: '1.5rem', textTransform: 'uppercase', letterSpacing: '0.1em', fontWeight: 600 }}>From your Address Book</h4>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1.5rem' }}>
                    {customerProfile.addresses.map((addr, idx) => (
                      <div 
                        key={idx}
                        onClick={() => handleSelectSavedAddress(addr)}
                        style={{ 
                          padding: '1.5rem', 
                          border: `2px solid ${formData.shippingAddress.streetName === addr.streetName && formData.shippingAddress.city === addr.city ? 'var(--bg-secondary)' : 'transparent'}`,
                          backgroundColor: 'var(--bg-surface)',
                          borderRadius: 'var(--border-radius)',
                          cursor: 'pointer',
                          fontSize: '0.9rem',
                          boxShadow: 'var(--shadow-soft)',
                          transition: 'all 0.3s ease'
                        }}
                      >
                        <p style={{ fontWeight: 600, color: 'var(--bg-secondary)', marginBottom: '0.5rem' }}>{addr.firstName} {addr.lastName}</p>
                        <p style={{ color: 'var(--accent-primary)', opacity: 0.8 }}>{addr.streetName} {addr.streetNumber}</p>
                        <p style={{ color: 'var(--accent-primary)', opacity: 0.8 }}>{addr.city}, {addr.postalCode}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <h4 style={{ fontSize: '0.85rem', color: 'var(--accent-primary)', marginBottom: '1.5rem', textTransform: 'uppercase', letterSpacing: '0.1em', fontWeight: 600 }}>
                {customerProfile?.addresses?.length > 0 ? 'Or enter a new destination' : 'Destination Address'}
              </h4>
              <div className="form-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                <input type="text" placeholder="First Name" name="firstName" value={formData.shippingAddress.firstName} onChange={(e) => handleInputChange(e, 'shippingAddress')} style={inputStyle} />
                <input type="text" placeholder="Last Name" name="lastName" value={formData.shippingAddress.lastName} onChange={(e) => handleInputChange(e, 'shippingAddress')} style={inputStyle} />
                <input type="text" placeholder="Street" name="streetName" value={formData.shippingAddress.streetName} onChange={(e) => handleInputChange(e, 'shippingAddress')} style={inputStyle} />
                <input type="text" placeholder="House No." name="streetNumber" value={formData.shippingAddress.streetNumber} onChange={(e) => handleInputChange(e, 'shippingAddress')} style={inputStyle} />
                <input type="text" placeholder="City" name="city" value={formData.shippingAddress.city} onChange={(e) => handleInputChange(e, 'shippingAddress')} style={inputStyle} />
                <input type="text" placeholder="Postal Code" name="postalCode" value={formData.shippingAddress.postalCode} onChange={(e) => handleInputChange(e, 'shippingAddress')} style={inputStyle} />
              </div>

              {user && (
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginTop: '2rem', cursor: 'pointer', fontSize: '0.95rem', fontWeight: 500, color: 'var(--bg-secondary)' }}>
                  <input type="checkbox" checked={saveToProfile} onChange={() => setSaveToProfile(!saveToProfile)} style={{ width: '18px', height: '18px', accentColor: 'var(--bg-secondary)' }} />
                  <span>Commit this address to my permanent record</span>
                </label>
              )}

              <button className="btn-primary" style={{ marginTop: '3rem', width: '100%', padding: '1.4rem' }} onClick={nextStep} disabled={loading || !formData.shippingAddress.firstName || !formData.shippingAddress.city}>
                {loading ? 'Processing...' : 'Proceed to Billing'}
              </button>
            </motion.div>
          )}

          {step === 2 && (
            <motion.div 
              key="step2"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
            >
              <h2 style={{ fontFamily: 'var(--font-display)', fontSize: '2.5rem', marginBottom: '2.5rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>Billing Information</h2>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '3rem', cursor: 'pointer', fontSize: '1rem', fontWeight: 500, color: 'var(--bg-secondary)' }}>
                <input type="checkbox" checked={formData.sameAsShipping} onChange={() => setFormData(prev => ({ ...prev, sameAsShipping: !prev.sameAsShipping }))} style={{ width: '18px', height: '18px', accentColor: 'var(--bg-secondary)' }} />
                <span>Matches Destination Address</span>
              </label>
              
              {!formData.sameAsShipping && (
                <div className="form-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                  <input type="text" placeholder="First Name" name="firstName" value={formData.billingAddress.firstName} onChange={(e) => handleInputChange(e, 'billingAddress')} style={inputStyle} />
                  <input type="text" placeholder="Last Name" name="lastName" value={formData.billingAddress.lastName} onChange={(e) => handleInputChange(e, 'billingAddress')} style={inputStyle} />
                  <input type="text" placeholder="Street" name="streetName" value={formData.billingAddress.streetName} onChange={(e) => handleInputChange(e, 'billingAddress')} style={inputStyle} />
                  <input type="text" placeholder="House No." name="streetNumber" value={formData.billingAddress.streetNumber} onChange={(e) => handleInputChange(e, 'billingAddress')} style={inputStyle} />
                  <input type="text" placeholder="City" name="city" value={formData.billingAddress.city} onChange={(e) => handleInputChange(e, 'billingAddress')} style={inputStyle} />
                  <input type="text" placeholder="Postal Code" name="postalCode" value={formData.billingAddress.postalCode} onChange={(e) => handleInputChange(e, 'billingAddress')} style={inputStyle} />
                </div>
              )}
              
              <div style={{ display: 'flex', gap: '1.5rem', marginTop: '4rem' }}>
                <button className="btn-outline" style={{ flex: 1, padding: '1.2rem' }} onClick={() => setStep(1)}>Back</button>
                <button className="btn-primary" style={{ flex: 2, padding: '1.2rem' }} onClick={nextStep} disabled={loading}>
                  {loading ? 'Processing...' : 'Select Delivery Method'}
                </button>
              </div>
            </motion.div>
          )}

          {step === 3 && (
            <motion.div 
              key="step3"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
            >
              <h2 style={{ fontFamily: 'var(--font-display)', fontSize: '2.5rem', marginBottom: '2.5rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>Select Delivery Method</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                {shippingMethods.map(method => (
                  <div 
                    key={method.id}
                    onClick={() => setFormData(prev => ({ ...prev, shippingMethodId: method.id }))}
                    style={{ 
                      padding: '2rem', 
                      border: `2px solid ${formData.shippingMethodId === method.id ? 'var(--bg-secondary)' : 'transparent'}`,
                      backgroundColor: 'var(--bg-surface)',
                      borderRadius: 'var(--border-radius)',
                      cursor: 'pointer',
                      display: 'flex', 
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      boxShadow: 'var(--shadow-soft)',
                      transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
                    }}
                  >
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '1.1rem', color: 'var(--bg-secondary)', marginBottom: '0.25rem' }}>{method.name.en || method.name}</div>
                      <div style={{ fontSize: '0.9rem', color: 'var(--accent-primary)', opacity: 0.7 }}>{method.description?.en || 'Standard artisanal delivery'}</div>
                    </div>
                    <div style={{ fontWeight: 600, fontSize: '1.1rem', color: 'var(--bg-secondary)' }}>
                      {method.zoneRates?.[0]?.shippingRates?.[0]?.price?.centAmount / 100} {method.zoneRates?.[0]?.shippingRates?.[0]?.price?.currencyCode}
                    </div>
                  </div>
                ))}
              </div>
              <div style={{ display: 'flex', gap: '1.5rem', marginTop: '4rem' }}>
                <button className="btn-outline" style={{ flex: 1, padding: '1.2rem' }} onClick={() => setStep(2)}>Back</button>
                <button className="btn-primary" style={{ flex: 2, padding: '1.2rem' }} onClick={nextStep} disabled={!formData.shippingMethodId || loading}>
                  {loading ? 'Processing...' : 'Final Review'}
                </button>
              </div>
            </motion.div>
          )}

          {step === 4 && (
            <motion.div 
              key="step4"
              initial={{ opacity: 0, scale: 0.98 }}
              animate={{ opacity: 1, scale: 1 }}
            >
              <h2 style={{ fontFamily: 'var(--font-display)', fontSize: '2.5rem', marginBottom: '2.5rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>Final Confirmation</h2>
              <div style={{ backgroundColor: 'var(--bg-surface)', padding: '3rem', borderRadius: 'var(--border-radius)', marginBottom: '3rem', boxShadow: 'var(--shadow-soft)', border: '1px solid rgba(44, 62, 45, 0.05)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.25rem', color: 'var(--accent-primary)', fontWeight: 500, opacity: 0.8 }}>
                  <span>Subtotal</span>
                  <span>{cart.totalPrice.centAmount / 100} {cart.totalPrice.currencyCode}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.25rem', color: 'var(--accent-primary)', fontWeight: 500, opacity: 0.8 }}>
                  <span>Delivery</span>
                  <span>{cart.shippingInfo?.price?.centAmount / 100 || 0} {cart.totalPrice.currencyCode}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 600, fontSize: '1.6rem', paddingTop: '1.5rem', borderTop: '1px solid rgba(44, 62, 45, 0.1)', color: 'var(--bg-secondary)', marginTop: '1rem' }}>
                  <span>Total Due</span>
                </div>
              </div>

              {user && (
                <div style={{ marginBottom: '3rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer', fontSize: '0.95rem', fontWeight: 500, color: 'var(--bg-secondary)' }}>
                    <input 
                      type="checkbox" 
                      checked={savePaymentMethod} 
                      onChange={() => setSavePaymentMethod(!savePaymentMethod)} 
                      style={{ width: '18px', height: '18px', accentColor: 'var(--bg-secondary)' }} 
                    />
                    <span>Securely save this payment method for future orders</span>
                  </label>
                  <p style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: 'var(--accent-secondary)', fontStyle: 'italic', marginLeft: '2.2rem' }}>
                    Your card details are tokenized and stored securely. We never store sensitive data on our servers.
                  </p>
                </div>
              )}

              <div style={{ display: 'flex', gap: '1.5rem' }}>
                <button className="btn-outline" style={{ flex: 1, padding: '1.2rem' }} onClick={() => setStep(3)}>Back</button>
                <button className="btn-primary" style={{ flex: 2, padding: '1.4rem' }} onClick={handleStripeCheckout} disabled={loading}>
                  {loading ? 'Connecting to Vault...' : 'Initiate Secure Payment'}
                </button>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};

const inputStyle = {
  padding: '1.2rem 1.5rem',
  background: 'var(--bg-surface)',
  border: '1px solid rgba(44, 62, 45, 0.1)',
  borderRadius: '12px',
  color: 'var(--bg-secondary)',
  fontFamily: 'var(--font-body)',
  fontSize: '1rem',
  fontWeight: 500,
  outline: 'none',
  transition: 'border-color 0.3s ease'
};

export default Checkout;
