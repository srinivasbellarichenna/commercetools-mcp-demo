import React, { createContext, useState, useEffect, useContext } from 'react';
import { AuthContext } from './AuthContext';
import { API_BASE_URL } from '../config/api';

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState(null);
  const { user } = useContext(AuthContext);

  const refreshCart = async () => {
    try {
      const existingCartId = localStorage.getItem('cartId');
      if (existingCartId && existingCartId !== 'undefined') {
        const res = await fetch(`${API_BASE_URL}/carts/${existingCartId}`);
        if (res.ok) {
          const data = await res.json();
          // Migration: If the existing cart isn't EUR or isn't in the DE territory, we must clear it
          if (data.totalPrice?.currencyCode !== 'EUR' || data.country !== 'DE') {
            console.log("Legacy cart detected, migrating...");
            localStorage.removeItem('cartId');
            await createNewCart();
            return;
          }
          setCart(data);
        } else {
          localStorage.removeItem('cartId');
          await createNewCart();
        }
      } else {
        await createNewCart();
      }
    } catch (e) {
      console.error("Cart refresh failed", e);
    }
  };

  const createNewCart = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/carts?currencyCode=EUR&country=DE`, { method: 'POST' });
      const data = await res.json();
      setCart(data);
      localStorage.setItem('cartId', data.id);
      
      if (user) {
        await assignUserToCart(user.id, data.id);
      }
    } catch (e) {
      console.error("Cart creation failed", e);
    }
  };

  const assignUserToCart = async (customerId, specificCartId = null) => {
    const targetCartId = specificCartId || (cart ? cart.id : localStorage.getItem('cartId'));
    if (!targetCartId || !customerId || targetCartId === 'undefined') return;
    
    if (cart && cart.id === targetCartId && cart.customerId === customerId) {
      return;
    }

    try {
      const res = await fetch(`${API_BASE_URL}/carts/${targetCartId}/customer?customerId=${customerId}`, {
        method: 'POST'
      });
      if (res.ok) {
        const updatedCart = await res.json();
        setCart(updatedCart);
        console.log("Customer identity synchronized with Cart");
        return updatedCart;
      }
    } catch (e) {
      console.error("Failed to synchronize customer identity", e);
    }
  };

  useEffect(() => {
    if (user && cart && !cart.customerId) {
      assignUserToCart(user.id);
    }
  }, [user, cart]);

  const addToCart = async (sku, quantity = 1) => {
    if (!cart) return;
    try {
      const params = new URLSearchParams();
      params.append('sku', sku);
      params.append('quantity', quantity);

      const res = await fetch(`${API_BASE_URL}/carts/${cart.id}/items?${params.toString()}`, {
        method: 'POST'
      });
      if (!res.ok) throw new Error(`Add to cart failed: ${res.status}`);
      const updatedCart = await res.json();
      setCart(updatedCart);
    } catch (e) {
      console.error("Add to cart failed", e);
    }
  };

  const removeFromCart = async (lineItemId) => {
    if (!cart) return;
    try {
      const res = await fetch(`${API_BASE_URL}/carts/${cart.id}/items/${lineItemId}`, {
        method: 'DELETE'
      });
      const updatedCart = await res.json();
      setCart(updatedCart);
    } catch (e) {
      console.error("Remove from cart failed", e);
    }
  };

  useEffect(() => {
    refreshCart();
  }, []);

  return (
    <CartContext.Provider value={{ cart, refreshCart, addToCart, removeFromCart, assignUserToCart }}>
      {children}
    </CartContext.Provider>
  );
};
