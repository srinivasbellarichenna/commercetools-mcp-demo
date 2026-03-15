import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { API_BASE_URL } from '../config/api';

const Account = () => {
  const { user, logout, refreshUser } = useContext(AuthContext);
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const [editFirstName, setEditFirstName] = useState(user?.firstName || '');
  const [editLastName, setEditLastName] = useState(user?.lastName || '');
  const [editEmail, setEditEmail] = useState(user?.email || '');
  const [editingAddressId, setEditingAddressId] = useState(null);
  const [editAddressData, setEditAddressData] = useState({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user) {
      navigate('/auth');
    } else {
      refreshUser();
      const fetchOrders = async () => {
        try {
          const res = await fetch(`${API_BASE_URL}/orders/customer/${user.id}`);
          if (res.ok) {
            const data = await res.json();
            setOrders(data.results || []);
          }
        } catch(e) {
          console.error("Failed to load orders", e);
        }
      };
      fetchOrders();
    }
  }, [user, navigate]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (editEmail !== user.email) params.append('email', editEmail);
      if (editFirstName !== user.firstName) params.append('firstName', editFirstName);
      if (editLastName !== user.lastName) params.append('lastName', editLastName);

      const res = await fetch(`${API_BASE_URL}/customers/${user.id}/profile?${params.toString()}`, {
        method: 'PATCH'
      });
      if (res.ok) {
        await refreshUser();
        setIsEditing(false);
      }
    } catch (e) {
      console.error("Profile update failed", e);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateAddress = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/customers/${user.id}/addresses/${editingAddressId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(editAddressData)
      });
      if (res.ok) {
        await refreshUser();
        setEditingAddressId(null);
      }
    } catch (e) {
      console.error("Address update failed", e);
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveAddress = async (addressId) => {
    if (!window.confirm("Are you sure you wish to remove this destination from your registry?")) return;
    try {
      const res = await fetch(`${API_BASE_URL}/customers/${user.id}/addresses/${addressId}`, {
        method: 'DELETE'
      });
      if (res.ok) {
        await refreshUser();
      }
    } catch (e) {
      console.error("Address removal failed", e);
    }
  };

  if (!user) return null;

  return (
    <div className="container" style={{ paddingTop: '160px', minHeight: '80vh', paddingBottom: '120px', background: 'var(--bg-primary)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '5rem', borderBottom: '1px solid rgba(44, 62, 45, 0.1)', paddingBottom: '2rem' }}>
        <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }}>
          <p style={{ color: 'var(--accent-secondary)', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.2em', fontWeight: 600, marginBottom: '0.5rem' }}>Customer Portal</p>
          <h1 style={{ fontFamily: 'var(--font-display)', fontSize: '3.5rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>
            Welcome, {user.firstName || user.email}
          </h1>
        </motion.div>
        <button className="btn-outline" onClick={() => { logout(); navigate('/'); }}>Sign Out</button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) 1.5fr', gap: '6rem' }}>
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
            <h3 style={{ fontFamily: 'var(--font-display)', color: 'var(--bg-secondary)', fontSize: '1.8rem', margin: 0 }}>Profile Details</h3>
            <button className="btn-outline" style={{ padding: '0.4rem 1rem', fontSize: '0.8rem' }} onClick={() => setIsEditing(!isEditing)}>
              {isEditing ? 'Cancel Edit' : 'Modify Profile'}
            </button>
          </div>

          <div style={{ backgroundColor: 'var(--bg-surface)', padding: '2.5rem', border: '1px solid rgba(44, 62, 45, 0.05)', borderRadius: 'var(--border-radius)', marginBottom: '4rem', boxShadow: 'var(--shadow-soft)' }}>
            {isEditing ? (
              <form onSubmit={handleUpdateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label style={labelStyle}>Given Name</label>
                    <input type="text" value={editFirstName} onChange={e => setEditFirstName(e.target.value)} style={inputStyle} />
                  </div>
                  <div className="form-group">
                    <label style={labelStyle}>Surname</label>
                    <input type="text" value={editLastName} onChange={e => setEditLastName(e.target.value)} style={inputStyle} />
                  </div>
                </div>
                <div className="form-group">
                  <label style={labelStyle}>Electronic Mail</label>
                  <input type="email" value={editEmail} onChange={e => setEditEmail(e.target.value)} style={inputStyle} />
                </div>
                <button type="submit" className="btn-primary" disabled={loading} style={{ padding: '1rem' }}>
                  {loading ? 'Updating Profile...' : 'Save Changes'}
                </button>
              </form>
            ) : (
              <>
                <p style={{ color: 'var(--accent-primary)', marginBottom: '1.25rem', fontWeight: 500 }}><strong style={{ color: 'var(--bg-secondary)', letterSpacing: '0.05em' }}>CUSTOMER:</strong> {user.firstName} {user.lastName}</p>
                <p style={{ color: 'var(--accent-primary)', marginBottom: '1.25rem', fontWeight: 500 }}><strong style={{ color: 'var(--bg-secondary)', letterSpacing: '0.05em' }}>EMAIL:</strong> {user.email}</p>
                <p style={{ color: 'var(--accent-primary)', fontWeight: 500 }}><strong style={{ color: 'var(--bg-secondary)', letterSpacing: '0.05em' }}>ACCOUNT ID:</strong> {user.id || 'N/A'}</p>
              </>
            )}
          </div>

          <h3 style={{ fontFamily: 'var(--font-display)', color: 'var(--bg-secondary)', marginBottom: '2rem', fontSize: '1.8rem' }}>Saved Destinations</h3>
          {!user.addresses || user.addresses.length === 0 ? (
            <div style={{ backgroundColor: 'var(--bg-surface)', padding: '3rem', border: '1px solid rgba(44, 62, 45, 0.05)', borderRadius: 'var(--border-radius)', color: 'var(--accent-secondary)', fontWeight: 500, fontStyle: 'italic' }}>
              No destinations committed. You may save addresses during checkout.
            </div>
          ) : (
            <div style={{ display: 'grid', gap: '1.5rem' }}>
              {user.addresses.map((addr) => (
                <div key={addr.id} style={{ backgroundColor: 'var(--bg-surface)', padding: '2rem', border: '1px solid rgba(44, 62, 45, 0.05)', borderRadius: 'var(--border-radius)', boxShadow: 'var(--shadow-soft)', position: 'relative' }}>
                  {editingAddressId === addr.id ? (
                    <form onSubmit={handleUpdateAddress} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                      <input type="text" placeholder="Street" value={editAddressData.streetName || ''} onChange={e => setEditAddressData({...editAddressData, streetName: e.target.value})} style={inputStyle} />
                      <input type="text" placeholder="Number" value={editAddressData.streetNumber || ''} onChange={e => setEditAddressData({...editAddressData, streetNumber: e.target.value})} style={inputStyle} />
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                        <input type="text" placeholder="Postal Code" value={editAddressData.postalCode || ''} onChange={e => setEditAddressData({...editAddressData, postalCode: e.target.value})} style={inputStyle} />
                        <input type="text" placeholder="City" value={editAddressData.city || ''} onChange={e => setEditAddressData({...editAddressData, city: e.target.value})} style={inputStyle} />
                      </div>
                      <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                        <button type="submit" className="btn-primary" style={{ padding: '0.6rem 1rem', fontSize: '0.85rem' }}>Save</button>
                        <button type="button" className="btn-outline" style={{ padding: '0.6rem 1rem', fontSize: '0.85rem' }} onClick={() => setEditingAddressId(null)}>Cancel</button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <div style={{ position: 'absolute', top: '1.5rem', right: '1.5rem', display: 'flex', gap: '1rem' }}>
                        <button 
                          onClick={() => { setEditingAddressId(addr.id); setEditAddressData(addr); }}
                          style={{ background: 'none', border: 'none', color: 'var(--bg-secondary)', fontSize: '0.75rem', fontWeight: 600, cursor: 'pointer', letterSpacing: '0.05em', opacity: 0.6 }}
                        >
                          EDIT
                        </button>
                        <button 
                          onClick={() => handleRemoveAddress(addr.id)}
                          style={{ background: 'none', border: 'none', color: '#8b4513', fontSize: '0.75rem', fontWeight: 600, cursor: 'pointer', letterSpacing: '0.05em', opacity: 0.6 }}
                        >
                          REMOVE
                        </button>
                      </div>
                      <p style={{ fontWeight: 600, marginBottom: '0.75rem', color: 'var(--bg-secondary)', fontSize: '1.1rem' }}>{addr.firstName} {addr.lastName}</p>
                      <p style={{ color: 'var(--accent-primary)', fontSize: '0.95rem', lineHeight: 1.6, fontWeight: 500, opacity: 0.8 }}>
                        {addr.streetName} {addr.streetNumber}<br />
                        {addr.postalCode} {addr.city}<br />
                        {addr.country}
                      </p>
                    </>
                  )}
                </div>
              ))}
            </div>
          )}

          <h3 style={{ fontFamily: 'var(--font-display)', color: 'var(--bg-secondary)', marginTop: '4rem', marginBottom: '2rem', fontSize: '1.8rem' }}>Vaulted Payment Methods</h3>
          <div style={{ backgroundColor: 'var(--bg-surface)', padding: '2.5rem', border: '1px solid rgba(44, 62, 45, 0.05)', borderRadius: 'var(--border-radius)', boxShadow: 'var(--shadow-soft)' }}>
            {user.custom?.fields?.savedPaymentMethods ? (() => {
              try {
                const payment = JSON.parse(user.custom.fields.savedPaymentMethods);
                return (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', opacity: 0.8 }}>
                    <div style={{ width: '50px', height: '32px', backgroundColor: 'var(--bg-primary)', borderRadius: '4px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, color: 'var(--bg-secondary)', fontSize: '0.7rem' }}>
                      {payment.brand?.toUpperCase() || 'CARD'}
                    </div>
                    <div>
                      <p style={{ color: 'var(--bg-secondary)', fontWeight: 600, margin: 0 }}>•••• •••• •••• {payment.last4}</p>
                      <p style={{ color: 'var(--accent-secondary)', fontSize: '0.8rem', margin: 0 }}>Securely Vaulted</p>
                    </div>
                  </div>
                );
              } catch (e) {
                return <p style={{ color: 'var(--accent-secondary)', fontStyle: 'italic' }}>Acquisition record refinement in progress...</p>;
              }
            })() : (
              <p style={{ color: 'var(--accent-secondary)', fontStyle: 'italic', margin: 0 }}>
                No payment instruments currently committed to your heritage profile.
              </p>
            )}
            <p style={{ marginTop: '2rem', fontSize: '0.85rem', color: 'var(--accent-secondary)', fontStyle: 'italic' }}>
              Your secure archives are managed via tokenized vaulting. Sensitive data never enters our atelier.
            </p>
          </div>
        </div>

        <div>
          <h3 style={{ fontFamily: 'var(--font-display)', color: 'var(--bg-secondary)', marginBottom: '2rem', fontSize: '1.8rem' }}>Order History</h3>
          {orders.length === 0 ? (
            <div style={{ backgroundColor: 'var(--bg-surface)', padding: '6rem 3rem', border: '1px solid rgba(44, 62, 45, 0.05)', borderRadius: 'var(--border-radius)', textAlign: 'center', color: 'var(--accent-secondary)', boxShadow: 'var(--shadow-soft)' }}>
              <p style={{ fontStyle: 'italic', marginBottom: '2rem', fontSize: '1.1rem' }}>Your personal collection is currently empty.</p>
              <button className="btn-outline" onClick={() => navigate('/shop')}>Discover the Atelier</button>
            </div>
          ) : (
            <div style={{ display: 'grid', gap: '1.5rem' }}>
              {orders.map(o => (
                <div key={o.id} style={{ padding: '2rem', backgroundColor: 'var(--bg-surface)', border: '1px solid rgba(44, 62, 45, 0.05)', borderRadius: 'var(--border-radius)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: 'var(--shadow-soft)' }}>
                  <div>
                    <div style={{ fontFamily: 'var(--font-display)', fontSize: '1.25rem', color: 'var(--bg-secondary)', marginBottom: '0.5rem', fontWeight: 500 }}>ORDER #{o.orderNumber || o.id.substring(0,8)}</div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--accent-secondary)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.1em' }}>
                      STATUS: <span style={{ color: '#2C3E2D' }}>{o.orderState}</span>
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ color: 'var(--bg-secondary)', fontWeight: 600, fontSize: '1.25rem' }}>{o.totalPrice?.currencyCode} {(o.totalPrice?.centAmount / 100).toFixed(2)}</div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--accent-secondary)', fontStyle: 'italic' }}>{o.lineItems?.length} archival pieces</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

const inputStyle = {
  width: '100%',
  padding: '1rem',
  background: 'var(--bg-primary)',
  border: '1px solid rgba(44, 62, 45, 0.1)',
  borderRadius: '8px',
  color: 'var(--bg-secondary)',
  outline: 'none',
  fontFamily: 'var(--font-body)',
  fontSize: '0.95rem'
};

const labelStyle = {
  display: 'block',
  fontSize: '0.75rem',
  textTransform: 'uppercase',
  letterSpacing: '0.1em',
  color: 'var(--accent-secondary)',
  marginBottom: '0.5rem',
  fontWeight: 600
};

export default Account;
