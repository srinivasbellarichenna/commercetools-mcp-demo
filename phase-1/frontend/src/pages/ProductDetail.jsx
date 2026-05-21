import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ShoppingBag, ChevronLeft, ShieldCheck, Truck, Loader2 } from 'lucide-react';
import { CartContext } from '../context/CartContext';
import { API_BASE_URL } from '../config/api';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useContext(CartContext);
  const [adding, setAdding] = useState(false);

  const [selectedVariant, setSelectedVariant] = useState(null);
  const [selectedAttributes, setSelectedAttributes] = useState({});

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/products/${id}`);
        const data = await res.json();
        setProduct(data);
      } catch (e) {
        console.error("Failed to load product", e);
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  useEffect(() => {
    if (product && product.masterVariant) {
      const initialAttrs = {};
      (product.masterVariant.attributes || []).forEach(attr => {
        const val = typeof attr.value === 'object' && attr.value !== null 
          ? attr.value.label || attr.value.key || JSON.stringify(attr.value)
          : attr.value;
        initialAttrs[attr.name] = val;
      });
      setSelectedAttributes(initialAttrs);
      setSelectedVariant(product.masterVariant);
    }
  }, [product]);

  const allVariants = product ? [product.masterVariant, ...(product.variants || [])].filter(Boolean) : [];

  // Extract unique attribute names across all variants
  const attributeNames = Array.from(new Set(
    allVariants.flatMap(v => (v.attributes || []).map(a => a.name))
  ));

  // Get unique values for a given attribute name
  const getUniqueAttributeValues = (name) => {
    const vals = allVariants.map(v => {
      const attr = v.attributes?.find(a => a.name === name);
      if (!attr) return null;
      return typeof attr.value === 'object' && attr.value !== null 
        ? attr.value.label || attr.value.key || JSON.stringify(attr.value)
        : attr.value;
    }).filter(Boolean);
    return Array.from(new Set(vals));
  };

  const handleAttributeChange = (name, value) => {
    const nextAttrs = { ...selectedAttributes, [name]: value };
    setSelectedAttributes(nextAttrs);

    // Find the variant that matches all selected attributes
    const match = allVariants.find(v => {
      return Object.entries(nextAttrs).every(([key, val]) => {
        const attrVal = v.attributes?.find(a => a.name === key)?.value;
        const attrStr = typeof attrVal === 'object' && attrVal !== null
          ? attrVal.label || attrVal.key || JSON.stringify(attrVal)
          : attrVal;
        return String(attrStr) === String(val);
      });
    });

    if (match) {
      setSelectedVariant(match);
    }
  };

  const handleAdd = async () => {
    const sku = selectedVariant?.sku || product.masterVariant?.sku || product.sku;
    if (!sku) {
      console.error("No SKU found for product");
      return;
    }
    setAdding(true);
    await addToCart(sku, 1);
    setAdding(false);
    navigate('/cart');
  };

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: 'var(--bg-primary)' }}>
      <Loader2 className="animate-spin" size={48} style={{ color: 'var(--bg-secondary)', opacity: 0.5 }} />
    </div>
  );

  if (!product) return <div className="container" style={{ paddingTop: '150px' }}>Piece not found.</div>;

  const activeVariant = selectedVariant || product.masterVariant;
  const price = activeVariant?.prices?.[0]?.value || 
                product.masterData?.current?.masterVariant?.prices?.[0]?.value;
  const imageUrl = activeVariant?.images?.[0]?.url || 
                   product.masterData?.current?.masterVariant?.images?.[0]?.url ||
                   'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?q=80&w=800&auto=format&fit=crop';
  const title = product.name?.['en-US'] || product.name?.['en-GB'] || product.name?.en || (typeof product.name === 'object' ? Object.values(product.name)[0] : product.name) || 'Artisanal Piece';
  const description = product.description?.['en-US'] || product.description?.['en-GB'] || product.description?.en || (typeof product.description === 'object' ? Object.values(product.description)[0] : product.description) || 'A timeless addition to your collection, handcrafted with heritage techniques.';

  return (
    <div className="container" style={{ paddingTop: '160px', paddingBottom: '100px' }}>
      <button onClick={() => navigate(-1)} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '3rem', color: 'var(--accent-secondary)', fontWeight: 600, background: 'none', border: 'none', cursor: 'pointer' }}>
        <ChevronLeft size={20} /> Back to Collection
      </button>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6rem', alignItems: 'start' }}>
        <motion.div 
          initial={{ opacity: 0, x: -30 }} 
          animate={{ opacity: 1, x: 0 }}
          style={{ position: 'sticky', top: '140px' }}
        >
          <div style={{ 
            borderRadius: '20px', 
            overflow: 'hidden', 
            boxShadow: 'var(--shadow-soft)',
            border: '1px solid rgba(44, 62, 45, 0.05)',
            backgroundColor: 'white'
          }}>
            <img src={imageUrl} alt={title} style={{ width: '100%', height: 'auto', display: 'block' }} />
          </div>
        </motion.div>

        <motion.div initial={{ opacity: 0, x: 30 }} animate={{ opacity: 1, x: 0 }}>
          <div style={{ marginBottom: '3rem' }}>
            <span style={{ color: 'var(--accent-secondary)', fontSize: '0.85rem', fontWeight: 700, letterSpacing: '0.2rem', textTransform: 'uppercase', marginBottom: '1rem', display: 'block' }}>
              Composable Agentic Commerce
            </span>
            <h1 style={{ fontSize: '3.5rem', marginBottom: '1.5rem', color: 'var(--bg-secondary)', lineHeight: 1.1 }}>{title}</h1>
            <div style={{ fontSize: '1.8rem', color: 'var(--accent-primary)', fontWeight: 600, fontFamily: 'var(--font-body)' }}>
              {price ? `${(price.centAmount / 100).toFixed(2)} ${price.currencyCode}` : 'Inquiry Required'}
            </div>
          </div>

          <div style={{ marginBottom: '3.5rem', color: 'var(--text-secondary)', fontSize: '1.1rem', lineHeight: 1.8 }}>
            <p>{description}</p>
          </div>

          {/* Dynamic Variant Selector */}
          {attributeNames.length > 0 && (
            <div style={{ marginBottom: '3.5rem', display: 'flex', flexDirection: 'column', gap: '2rem' }}>
              {attributeNames.map(name => {
                const values = getUniqueAttributeValues(name);
                if (values.length <= 1) return null; // No need to select if only one option exists
                const label = name.replace(/([A-Z])/g, ' $1').toUpperCase();
                return (
                  <div key={name}>
                    <span style={{ fontSize: '0.8rem', fontWeight: 700, letterSpacing: '0.15em', color: 'var(--bg-secondary)', display: 'block', marginBottom: '1rem' }}>
                      SELECT {label}
                    </span>
                    <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                      {values.map(val => {
                        const isSelected = String(selectedAttributes[name]) === String(val);
                        return (
                          <button
                            key={val}
                            onClick={() => handleAttributeChange(name, val)}
                            style={{
                              padding: '0.75rem 1.5rem',
                              border: isSelected ? '2px solid var(--bg-secondary)' : '1px solid rgba(44, 62, 45, 0.15)',
                              backgroundColor: isSelected ? 'var(--bg-secondary)' : 'transparent',
                              color: isSelected ? 'var(--bg-primary)' : 'var(--text-secondary)',
                              borderRadius: '30px',
                              fontFamily: 'var(--font-body)',
                              fontWeight: isSelected ? 600 : 500,
                              cursor: 'pointer',
                              transition: 'all 0.25s ease',
                              fontSize: '0.9rem'
                            }}
                            onMouseEnter={(e) => {
                              if (!isSelected) {
                                e.currentTarget.style.borderColor = 'var(--bg-secondary)';
                                e.currentTarget.style.color = 'var(--bg-secondary)';
                              }
                            }}
                            onMouseLeave={(e) => {
                              if (!isSelected) {
                                e.currentTarget.style.borderColor = 'rgba(44, 62, 45, 0.15)';
                                e.currentTarget.style.color = 'var(--text-secondary)';
                              }
                            }}
                          >
                            {val}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          <div style={{ display: 'flex', gap: '1rem', marginBottom: '4rem' }}>
            <button 
              className="btn-primary" 
              style={{ flex: 1, padding: '1.4rem', fontSize: '1rem' }} 
              onClick={handleAdd}
              disabled={adding}
            >
              {adding ? 'Adding to Cart...' : 'Add to Cart'}
            </button>
            <button className="btn-outline" style={{ padding: '0 1.5rem' }}><ShoppingBag size={24} /></button>
          </div>

          <div style={{ borderTop: '1px solid rgba(44, 62, 45, 0.1)', paddingTop: '3rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
              <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: 'var(--bg-secondary)', color: 'var(--bg-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <ShieldCheck size={20} />
              </div>
              <div style={{ fontSize: '0.85rem' }}>
                <div style={{ fontWeight: 700, color: 'var(--bg-secondary)' }}>Authenticity Guaranteed</div>
                <div style={{ opacity: 0.6 }}>Includes heritage certificate</div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
              <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: 'rgba(44, 62, 45, 0.05)', color: 'var(--bg-secondary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Truck size={20} />
              </div>
              <div style={{ fontSize: '0.85rem' }}>
                <div style={{ fontWeight: 700, color: 'var(--bg-secondary)' }}>World-wide Courier</div>
                <div style={{ opacity: 0.6 }}>Insured artisanal delivery</div>
              </div>
            </div>
          </div>
          
          <div style={{ marginTop: '3rem', fontSize: '0.8rem', textAlign: 'center', opacity: 0.5, letterSpacing: '0.05em' }}>
            <ShieldCheck size={14} style={{ verticalAlign: 'middle', marginRight: '4px' }} />
            ENCRYPTED & SECURE PAYMENT PROCESSION
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default ProductDetail;
