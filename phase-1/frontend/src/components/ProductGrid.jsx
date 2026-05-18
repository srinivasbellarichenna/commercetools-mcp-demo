import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, SlidersHorizontal, Loader2 } from 'lucide-react';
import { API_BASE_URL } from '../config/api';
import ProductCard from './ProductCard';

const ProductGrid = () => {
  const [products, setProducts] = useState([]);
  const [total, setTotal] = useState(0);
  const [offset, setOffset] = useState(0);
  const [limit] = useState(12);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState('price asc');

  const fetchProducts = async (newOffset = 0, isAppend = false) => {
    try {
      if (!isAppend) setLoading(true);
      else setLoadingMore(true);

      const url = `${API_BASE_URL}/products?limit=${limit}&offset=${newOffset}&sort=${sort}${search ? `&text=${search}` : ''}`;
      const res = await fetch(url);
      const data = await res.json();
      
      if (isAppend) {
        setProducts(prev => [...prev, ...data.results]);
      } else {
        setProducts(data.results);
      }
      setTotal(data.total);
      setOffset(newOffset);
    } catch (e) {
      console.error("Failed to fetch products", e);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  useEffect(() => {
    fetchProducts(0, false);
  }, [search, sort]);

  const handleLoadMore = () => {
    const nextOffset = offset + limit;
    if (nextOffset < total) {
      fetchProducts(nextOffset, true);
    }
  };

  const hasMore = products.length < total;

  return (
    <section id="collection" style={{ padding: '8rem 0', background: 'var(--bg-primary)' }}>
      <div className="container">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '4rem' }}>
          <div>
            <h2 style={{ fontSize: '3rem', color: 'var(--bg-secondary)', marginBottom: '1rem' }}>The Collection</h2>
            <p style={{ color: 'var(--accent-secondary)', fontSize: '1rem', fontWeight: 500 }}>
              Discover {total} products curated for the modern enterprise.
            </p>
          </div>
          
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
            <div style={{ position: 'relative' }}>
              <Search size={18} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', opacity: 0.4 }} />
              <input 
                type="text" 
                placeholder="Search collection..." 
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{ paddingLeft: '3rem', border: '1px solid rgba(44, 62, 45, 0.1)', background: 'var(--bg-surface)', color: 'var(--bg-secondary)' }}
              />
            </div>
            
            <div style={{ position: 'relative' }}>
              <SlidersHorizontal size={18} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', opacity: 0.4 }} />
              <select 
                value={sort}
                onChange={(e) => setSort(e.target.value)}
                style={{ paddingLeft: '3rem', appearance: 'none', background: 'var(--bg-surface)', border: '1px solid rgba(44, 62, 45, 0.1)', cursor: 'pointer' }}
              >
                <option value="price asc">Price: Low to High</option>
                <option value="price desc">Price: High to Low</option>
                <option value="name.en asc">Name: A to Z</option>
              </select>
            </div>
          </div>
        </div>

        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '10rem 0' }}>
            <Loader2 className="animate-spin" size={40} style={{ color: 'var(--bg-secondary)', opacity: 0.5 }} />
          </div>
        ) : (
          <motion.div 
            layout
            className="product-grid" 
            style={{ 
              display: 'grid', 
              gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', 
              gap: '2.5rem' 
            }}
          >
            <AnimatePresence mode="popLayout">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </AnimatePresence>
          </motion.div>
        )}

        {hasMore && !loading && (
          <div style={{ marginTop: '5rem', textAlign: 'center' }}>
            <button 
              className="btn-outline" 
              onClick={handleLoadMore} 
              disabled={loadingMore}
              style={{ minWidth: '200px', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.75rem' }}
            >
              {loadingMore ? (
                <>
                  <Loader2 className="animate-spin" size={18} />
                  Loading items...
                </>
              ) : (
                'View More'
              )}
            </button>
          </div>
        )}
        
        {!hasMore && products.length > 0 && !loading && (
          <div style={{ marginTop: '5rem', textAlign: 'center', color: 'var(--accent-secondary)', fontStyle: 'italic', opacity: 0.6 }}>
            You have explored the entire current collection.
          </div>
        )}
      </div>
    </section>
  );
};

export default ProductGrid;
