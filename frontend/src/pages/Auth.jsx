import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const Auth = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [error, setError] = useState('');
  
  const { login } = useContext(AuthContext);
  const { assignPatronToBag } = useContext(CartContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const endpoint = isLogin ? 'http://localhost:8085/api/customers/login' : 'http://localhost:8085/api/customers/register';
    let url = endpoint + `?email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`;
    if (!isLogin) {
      if (firstName) url += `&firstName=${encodeURIComponent(firstName)}`;
      if (lastName) url += `&lastName=${encodeURIComponent(lastName)}`;
    }

    try {
      const res = await fetch(url, { method: 'POST' });
      if (res.ok) {
        const data = await res.json();
        const user = data.customer;
        login(user, "mock-jwt-token-kestrel");
        
        // Explicitly synchronize cart with newly authenticated patron
        await assignPatronToBag(user.id);
        
        navigate('/account');
      } else {
        setError('Authentication failed. Please verify credentials.');
      }
    } catch (e) {
      setError('System unavailable.');
    }
  };

  return (
    <div style={{ minHeight: '80vh', display: 'flex', alignItems: 'center', justifyContent: 'center', paddingTop: '120px', paddingBottom: '80px', background: 'var(--bg-primary)' }}>
      <motion.div 
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        style={{ width: '100%', maxWidth: '450px', padding: '4rem', backgroundColor: 'var(--bg-surface)', borderRadius: 'var(--border-radius)', border: '1px solid rgba(44, 62, 45, 0.05)', boxShadow: 'var(--shadow-soft)' }}
      >
        <h2 style={{ fontFamily: 'var(--font-display)', textAlign: 'center', marginBottom: '3rem', fontSize: '2.5rem', color: 'var(--bg-secondary)', fontWeight: 500 }}>
          {isLogin ? 'Enter Atelier' : 'Join the Legacy'}
        </h2>
        
        {error && <p style={{ color: '#8b4513', textAlign: 'center', marginBottom: '1.5rem', fontSize: '0.9rem', fontWeight: 600 }}>{error}</p>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {!isLogin && (
            <div style={{ display: 'flex', gap: '1rem' }}>
              <input type="text" placeholder="First Name" required value={firstName} onChange={e => setFirstName(e.target.value)} style={inputStyle} />
              <input type="text" placeholder="Last Name" required value={lastName} onChange={e => setLastName(e.target.value)} style={inputStyle} />
            </div>
          )}
          <input type="email" placeholder="Email Address" required value={email} onChange={e => setEmail(e.target.value)} style={inputStyle} />
          <input type="password" placeholder="Password" required value={password} onChange={e => setPassword(e.target.value)} style={inputStyle} />
          
          <button type="submit" className="btn-primary" style={{ marginTop: '1.5rem', padding: '1.2rem' }}>
            {isLogin ? 'Sign In' : 'Establish Account'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: '2.5rem', color: 'var(--accent-secondary)', fontSize: '0.95rem', cursor: 'pointer', fontWeight: 500 }} onClick={() => setIsLogin(!isLogin)}>
          {isLogin ? "New to the atelier? Register here." : "Already a patron? Sign in."}
        </p>
      </motion.div>
    </div>
  );
};

const inputStyle = {
  width: '100%',
  padding: '1.2rem',
  background: 'var(--bg-primary)',
  border: '1px solid rgba(44, 62, 45, 0.1)',
  borderRadius: '8px',
  color: 'var(--bg-secondary)',
  outline: 'none',
  fontFamily: 'var(--font-body)',
  fontSize: '0.95rem',
  fontWeight: 500,
  transition: 'border-color 0.3s ease'
};

export default Auth;
