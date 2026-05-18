import React from 'react';
import { motion } from 'framer-motion';
import './Hero.css';

const Hero = () => {
  return (
    <section className="hero">
      <div className="container hero-container">
        <motion.div 
          className="hero-content"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
        >
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2 }}
            className="hero-badge"
          >
            ESTABLISHED 1923 • HANDCRAFTED
          </motion.div>
          
          <h1 className="hero-title">
            Celebrating <span className="italic">Artisanal</span> Heritage
          </h1>
          
          <p className="hero-subtitle">
            Conscious luxury, born from a century of craftsmanship. Each piece is meticulously handcrafted in our Cotswolds workshop, designed to last generations.
          </p>
          
          <div className="hero-actions">
            <button className="btn-primary" onClick={() => document.getElementById('collection').scrollIntoView({ behavior: 'smooth' })}>
              Shop the Collection
            </button>
            <button className="btn-outline">
              Explore the Craft
            </button>
          </div>
        </motion.div>

        <div className="hero-visual">
          <div className="hero-blob"></div>
          <div className="hero-texture"></div>
        </div>
      </div>
    </section>
  );
};

export default Hero;
