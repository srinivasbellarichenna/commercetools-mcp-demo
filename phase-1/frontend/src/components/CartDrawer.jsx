import React, { useContext } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Trash2, ArrowRight, ShoppingBag } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import './CartDrawer.css';

const CartDrawer = ({ isOpen, onClose }) => {
  const { cart, removeFromCart } = useContext(CartContext);
  const navigate = useNavigate();

  const isEmpty = !cart || !cart.lineItems || cart.lineItems.length === 0;

  const handleCheckout = () => {
    onClose();
    navigate('/checkout');
  };

  const handleViewBag = () => {
    onClose();
    navigate('/cart');
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="drawer-backdrop"
          />
          
          {/* Drawer */}
          <motion.div
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="drawer-content"
          >
            <div className="drawer-header">
              <h2 className="drawer-title">Current Bag</h2>
              <button onClick={onClose} className="close-btn">
                <X size={24} />
              </button>
            </div>

            <div className="drawer-body">
              {isEmpty ? (
                <div className="empty-drawer">
                  <ShoppingBag size={48} className="empty-icon" />
                  <p>Your bag is currently empty.</p>
                  <button onClick={onClose} className="btn-outline" style={{ marginTop: '1.5rem' }}>
                    Continue Shopping
                  </button>
                </div>
              ) : (
                <div className="cart-items">
                  {cart.lineItems.map((item) => (
                    <div key={item.id} className="cart-item">
                      <div className="item-image">
                        <img 
                          src={item.variant?.images?.[0]?.url || 'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?q=80&w=200'} 
                          alt={item.name?.en} 
                        />
                      </div>
                      <div className="item-details">
                        <div className="item-header">
                          <h4 className="item-name">{item.name?.en || Object.values(item.name)[0]}</h4>
                          <span className="item-price">
                            {(item.totalPrice?.centAmount / 100).toFixed(2)} {item.totalPrice?.currencyCode}
                          </span>
                        </div>
                        <p className="item-qty">Quantity: {item.quantity}</p>
                        <button 
                          onClick={() => removeFromCart(item.id)}
                          className="remove-btn"
                        >
                          <Trash2 size={14} /> Remove
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {!isEmpty && (
              <div className="drawer-footer">
                <div className="summary-row">
                  <span>Subtotal</span>
                  <span className="summary-total">
                    {(cart.totalPrice?.centAmount / 100).toFixed(2)} {cart.totalPrice?.currencyCode}
                  </span>
                </div>
                <p className="summary-disclaimer">Shipping and taxes calculated at checkout.</p>
                <div className="footer-actions">
                  <button onClick={handleCheckout} className="btn-primary full-width">
                    Secure Checkout <ArrowRight size={18} />
                  </button>
                  <button onClick={handleViewBag} className="btn-text">
                    View Full Bag
                  </button>
                </div>
              </div>
            )}
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

export default CartDrawer;
