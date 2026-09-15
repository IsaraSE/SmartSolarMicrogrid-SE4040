import React from 'react';
import Drawer from '../../components/common/Drawer/Drawer';
import { FiMail, FiPhone, FiMapPin, FiCreditCard, FiClock, FiUser } from 'react-icons/fi';
import './ProsumerDetailsDrawer.css';

const ProsumerDetailsDrawer = ({ isOpen, onClose, prosumer, onActivate, onReactivate }) => {
  if (!prosumer) return null;

  // Extract initials
  const initials = prosumer.fullName
    ? prosumer.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()
    : 'U';

  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <Drawer isOpen={isOpen} onClose={onClose}>
      <div className="prosumer-details">
        {/* Header Section */}
        <div className="details-header">
          <div className="avatar-large">{initials}</div>
          <h2 className="details-name">{prosumer.fullName}</h2>
          
          <div className="details-badges">
            <span className={`status-badge status-${prosumer.accountStatus?.toLowerCase()}`}>
              <span className="status-dot"></span>
              {prosumer.accountStatus === 'ACTIVE' ? 'Active' : prosumer.accountStatus === 'PENDING' ? 'Pending' : 'Deactivated'}
            </span>
            <span className="role-badge">{prosumer.role}</span>
          </div>
        </div>

        {/* Contact Info Card */}
        <div className="details-section">
          <h3 className="section-title">Contact Information</h3>
          <div className="info-card">
            <div className="info-row">
              <div className="info-icon"><FiMail /></div>
              <div className="info-content">
                <span className="info-label">Email Address</span>
                <span className="info-value">{prosumer.email}</span>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-row">
              <div className="info-icon"><FiPhone /></div>
              <div className="info-content">
                <span className="info-label">Phone Number</span>
                <span className="info-value">{prosumer.phone}</span>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-row">
              <div className="info-icon"><FiMapPin /></div>
              <div className="info-content">
                <span className="info-label">Address</span>
                <span className="info-value">{prosumer.address || '-'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Account Details Card */}
        <div className="details-section">
          <h3 className="section-title">Account Details</h3>
          <div className="info-card">
            <div className="info-row">
              <div className="info-icon"><FiCreditCard /></div>
              <div className="info-content">
                <span className="info-label">National Identity Card (NIC)</span>
                <span className="info-value">{prosumer.nic || 'Not Provided'}</span>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-row">
              <div className="info-icon"><FiUser /></div>
              <div className="info-content">
                <span className="info-label">User ID</span>
                <span className="info-value mono-text">{prosumer.userId || '-'}</span>
              </div>
            </div>
            <div className="divider"></div>
            <div className="info-row">
              <div className="info-icon"><FiClock /></div>
              <div className="info-content">
                <span className="info-label">Account Created At</span>
                <span className="info-value">{formatDate(prosumer.createdAt)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        {(prosumer.accountStatus === 'PENDING' || prosumer.accountStatus === 'INACTIVE') && (
          <div className="details-section">
            <h3 className="section-title">Quick Actions</h3>
            <div className="actions-card">
              {prosumer.accountStatus === 'PENDING' && (
                <button 
                  className="drawer-action-btn btn-activate"
                  onClick={() => {
                    onActivate(prosumer.nic);
                    onClose();
                  }}
                >
                  Approve & Activate Account
                </button>
              )}
              {prosumer.accountStatus === 'INACTIVE' && (
                <button 
                  className="drawer-action-btn btn-reactivate"
                  onClick={() => {
                    onReactivate(prosumer.nic);
                    onClose();
                  }}
                >
                  Reactivate Account
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </Drawer>
  );
};

export default ProsumerDetailsDrawer;
