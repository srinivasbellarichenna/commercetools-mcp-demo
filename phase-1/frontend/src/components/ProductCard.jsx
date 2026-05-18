import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import './ProductCard.css';

const ProductCard = ({ product }) => {
  const price = product.masterVariant?.prices?.[0]?.value || 
                product.masterData?.current?.masterVariant?.prices?.[0]?.value;
  
  const formattedPrice = price 
    ? `${(price.centAmount / 100).toFixed(2)} ${price.currencyCode}`
    : 'Contact for Pricing';

  const imageUrl = product.masterVariant?.images?.[0]?.url || 
                   product.masterData?.current?.masterVariant?.images?.[0]?.url ||
                   'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?q=80&w=800&auto=format&fit=crop';

  const title = product.name?.['en-US'] || product.name?.['en-GB'] || product.name?.en || (typeof product.name === 'object' ? Object.values(product.name)[0] : product.name) || 'Product';
  const description = product.description?.['en-US'] || product.description?.['en-GB'] || product.description?.en || (typeof product.description === 'object' ? Object.values(product.description)[0] : product.description) || 'A scalable and modular component for your agentic commerce engine.';

  return (
    <motion.div 
      layout
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.9 }}
      whileHover={{ y: -10 }}
      className="product-card"
    >
      <Link to={`/product/${product.id}`}>
        <div className="product-image-container">
          <img src={imageUrl} alt={title} className="product-image" />
          <div className="product-overlay">
            <span className="view-details">View Details</span>
          </div>
        </div>
        
        <div className="product-info">
          <div className="product-category">Enterprise Standard</div>
          <h3 className="product-title">{title}</h3>
          <div className="product-price">{formattedPrice}</div>
        </div>
      </Link>
    </motion.div>
  );
};

export default ProductCard;
