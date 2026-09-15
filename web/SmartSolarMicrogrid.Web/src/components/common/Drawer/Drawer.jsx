import React, { useEffect } from 'react';
import { FiX } from 'react-icons/fi';
import './Drawer.css';

const Drawer = ({ isOpen, onClose, title, children }) => {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'auto';
    }
    return () => {
      document.body.style.overflow = 'auto';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className={`drawer-overlay ${isOpen ? 'open' : ''}`} onClick={onClose}>
      <div 
        className={`drawer-container ${isOpen ? 'open' : ''}`} 
        onClick={(e) => e.stopPropagation()}
      >
        <div className="drawer-header">
          {title && <h2 className="drawer-title">{title}</h2>}
          <button className="drawer-close" onClick={onClose} aria-label="Close">
            <FiX size={24} />
          </button>
        </div>
        <div className="drawer-content">
          {children}
        </div>
      </div>
    </div>
  );
};

export default Drawer;
