import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FiSearch, 
  FiFilter, 
  FiUsers,
  FiClock,
  FiUserCheck,
  FiUserX,
  FiTrendingUp,
  FiTrendingDown,
  FiPlay,
  FiSlash,
  FiEye,
  FiX,
  FiUser,
  FiMail,
  FiPhone,
  FiMapPin,
  FiCreditCard,
  FiActivity,
  FiCalendar
} from 'react-icons/fi';
import { PiSunLight } from 'react-icons/pi';
import { prosumerService } from '../../services/prosumerService';
import './Prosumers.css';

const Prosumers = () => {
  const navigate = useNavigate();
  const [prosumers, setProsumers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState('All'); // 'All', 'Pending', 'Active', 'Deactivated'
  const [selectedProsumer, setSelectedProsumer] = useState(null);
  const [statusConfirm, setStatusConfirm] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(15);

  const getInitials = (name) => {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  const getAvatarColor = (name) => {
    if (!name) return '#94a3b8';
    const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4'];
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
  };

  const today = new Date();
  const formattedToday = today.toLocaleDateString('en-GB', { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' });

  useEffect(() => {
    fetchProsumers();
  }, []);

  const fetchProsumers = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await prosumerService.getAllProsumers();
      if (response.success) {
        setProsumers(response.data);
      } else {
        setError(response.message || 'Failed to fetch prosumers');
      }
    } catch (err) {
      console.error('Error fetching prosumers:', err);
      setError('An error occurred while fetching prosumer data.');
    } finally {
      setLoading(false);
    }
  };

  const handleActivate = async (nic) => {
    try {
      const response = await prosumerService.activateProsumer(nic);
      if (response.success) {
        fetchProsumers();
      }
    } catch (err) {
      console.error('Failed to activate prosumer:', err);
      alert('Failed to activate prosumer');
    }
  };

  const handleReactivate = async (nic) => {
    try {
      const response = await prosumerService.reactivateProsumer(nic);
      if (response.success) {
        fetchProsumers();
      }
    } catch (err) {
      console.error('Failed to reactivate prosumer:', err);
      alert('Failed to reactivate prosumer');
    }
  };

  const handleDeactivate = async (nic) => {
    try {
      const response = await prosumerService.deactivateProsumer(nic);
      if (response.success) {
        fetchProsumers();
      }
    } catch (err) {
      console.error('Failed to deactivate prosumer:', err);
      alert('Failed to deactivate prosumer');
    }
  };

  const confirmStatusChange = async () => {
    if (!statusConfirm) return;
    const { action, nic } = statusConfirm;
    
    if (action === 'ACTIVATE') {
      await handleActivate(nic);
    } else if (action === 'DEACTIVATE') {
      await handleDeactivate(nic);
    } else if (action === 'REACTIVATE') {
      await handleReactivate(nic);
    }
    
    setStatusConfirm(null);
  };

  const totalProsumers = prosumers.length;
  const pendingCount = prosumers.filter(p => p.accountStatus === 'PENDING').length;
  const activeCount = prosumers.filter(p => p.accountStatus === 'ACTIVE').length;
  const deactivatedCount = prosumers.filter(p => p.accountStatus === 'DEACTIVATED').length;

  const filteredProsumers = useMemo(() => {
    const result = prosumers.filter(prosumer => {
      const matchesSearch = 
        (prosumer.nic || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (prosumer.fullName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (prosumer.email || '').toLowerCase().includes(searchTerm.toLowerCase());
      
      const matchesTab = 
        activeTab === 'All' ? true :
        activeTab === 'Pending' ? prosumer.accountStatus === 'PENDING' :
        activeTab === 'Active' ? prosumer.accountStatus === 'ACTIVE' :
        activeTab === 'Deactivated' ? prosumer.accountStatus === 'DEACTIVATED' : true;
        
      return matchesSearch && matchesTab;
    });

    // Sort to ensure DEACTIVATED prosumers always appear at the bottom
    return result.sort((a, b) => {
      if (a.accountStatus === 'DEACTIVATED' && b.accountStatus !== 'DEACTIVATED') return 1;
      if (a.accountStatus !== 'DEACTIVATED' && b.accountStatus === 'DEACTIVATED') return -1;
      return 0;
    });
  }, [prosumers, searchTerm, activeTab]);

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentProsumers = filteredProsumers.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredProsumers.length / itemsPerPage);

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return (
      <div className="date-cell">
        <span className="date-day">{date.toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' })}</span>
        <span className="date-time">{date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</span>
      </div>
    );
  };

  return (
    <div className="prosumers-container fade-in">
      <div className="prosumers-header">
        <div className="prosumers-title">
          <h1>Prosumer Management</h1>
          <p>Manage solar prosumers in the microgrid network. View, activate or deactivate prosumer accounts.</p>
        </div>
        <div className="prosumers-breadcrumbs">
          <span>Users</span>
          <span className="separator">›</span>
          <span className="current">Prosumer Management</span>
        </div>
      </div>



      <div className="prosumers-content-card">
        <div className="prosumers-toolbar">
          <div className="tabs">
            <button className={`tab-btn ${activeTab === 'All' ? 'active' : ''}`} onClick={() => setActiveTab('All')}>
              All Prosumers <span className="tab-count">({totalProsumers})</span>
            </button>
            <button className={`tab-btn ${activeTab === 'Pending' ? 'active' : ''}`} onClick={() => setActiveTab('Pending')}>
              Pending <span className="tab-count">({pendingCount})</span>
            </button>
            <button className={`tab-btn ${activeTab === 'Active' ? 'active' : ''}`} onClick={() => setActiveTab('Active')}>
              Active <span className="tab-count">({activeCount})</span>
            </button>
            <button className={`tab-btn ${activeTab === 'Deactivated' ? 'active' : ''}`} onClick={() => setActiveTab('Deactivated')}>
              Deactivated <span className="tab-count">({deactivatedCount})</span>
            </button>
          </div>

          <div className="toolbar-actions">
            <div className="search-box">
              <FiSearch className="search-icon" />
              <input 
                type="text" 
                placeholder="Search prosumers..." 
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
        </div>
        <div className="table-responsive">
          <table className="prosumers-table users-table">
            <thead>
              <tr>
                <th>NIC</th>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th style={{ textAlign: 'center' }}>Status</th>
                <th className="th-actions" style={{ textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="9" className="state-cell">Loading prosumers...</td></tr>
              ) : error ? (
                <tr><td colSpan="9" className="state-cell error">{error}</td></tr>
              ) : currentProsumers.length === 0 ? (
                <tr><td colSpan="9" className="state-cell">No prosumers found.</td></tr>
              ) : (
                currentProsumers.map(prosumer => (
                  <tr key={prosumer.nic || prosumer.userId}>
                    <td className="cell-nic">{prosumer.nic || '-'}</td>
                    <td>
                      <div className="cell-user">
                        <span className="user-name">
                          {prosumer.fullName ? prosumer.fullName.split(' ').slice(0, 2).join(' ') : 'Unknown'}
                        </span>
                      </div>
                    </td>
                    <td className="cell-email">
                      <div className="email-text" title={prosumer.email}>{prosumer.email}</div>
                    </td>
                    <td className="cell-phone">{prosumer.phone}</td>
                    <td style={{ textAlign: 'center' }}>
                      <span 
                        className={`status-badge-btn static-badge status-${prosumer.accountStatus?.toLowerCase() || 'unknown'}`} 
                        style={{ 
                          display: 'inline-flex', 
                          padding: '6px 12px', 
                          borderRadius: '20px', 
                          fontSize: '0.85rem', 
                          fontWeight: '500', 
                          border: 'none', 
                          cursor: 'default', 
                          backgroundColor: prosumer.accountStatus === 'ACTIVE' ? '#dcfce7' : prosumer.accountStatus === 'PENDING' ? '#fef3c7' : '#fee2e2', 
                          color: prosumer.accountStatus === 'ACTIVE' ? '#166534' : prosumer.accountStatus === 'PENDING' ? '#b45309' : '#991b1b' 
                        }}
                      >
                        {prosumer.accountStatus === 'ACTIVE' ? 'Active' : prosumer.accountStatus === 'PENDING' ? 'Pending' : 'Deactivated'}
                      </span>
                    </td>
                    <td className="cell-actions" style={{ display: 'flex', gap: '8px', alignItems: 'center', justifyContent: 'center' }}>
                      <button className="review-btn" onClick={() => setSelectedProsumer(prosumer)}>
                        Review
                      </button>
                      {activeTab === 'Pending' && (
                        <button className="activate-btn" onClick={() => setStatusConfirm({ action: 'ACTIVATE', actionText: 'Activate', nic: prosumer.nic, name: prosumer.fullName })}>
                          Activate
                        </button>
                      )}
                      {activeTab === 'Active' && (
                        <button className="deactivate-btn" onClick={() => setStatusConfirm({ action: 'DEACTIVATE', actionText: 'Deactivate', nic: prosumer.nic, name: prosumer.fullName })}>
                          Deactivate
                        </button>
                      )}
                      {activeTab === 'Deactivated' && (
                        <button className="reactivate-btn" onClick={() => setStatusConfirm({ action: 'REACTIVATE', actionText: 'Reactivate', nic: prosumer.nic, name: prosumer.fullName })}>
                          Reactivate
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="table-footer">
          <div className="footer-info">
            Showing {filteredProsumers.length === 0 ? 0 : indexOfFirstItem + 1} to {Math.min(indexOfLastItem, filteredProsumers.length)} of {filteredProsumers.length} prosumers
          </div>
          <div className="footer-controls">
            <div className="pagination">
              <button className="page-btn" disabled={currentPage === 1} onClick={() => setCurrentPage(p => Math.max(1, p - 1))}>‹</button>
              {Array.from({length: totalPages}, (_, i) => i + 1).map(page => (
                <button 
                  key={page} 
                  className={`page-btn ${currentPage === page ? 'active' : ''}`}
                  onClick={() => setCurrentPage(page)}
                >{page}</button>
              ))}
              <button className="page-btn" disabled={currentPage === totalPages || totalPages === 0} onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}>›</button>
            </div>
            <div className="rows-per-page">
              <span>Show</span>
              <select value={itemsPerPage} onChange={(e) => {setItemsPerPage(Number(e.target.value)); setCurrentPage(1);}}>
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={15}>15</option>
                <option value={20}>20</option>
              </select>
              <span>per page</span>
            </div>
          </div>
        </div>
      </div>

      {/* View Prosumer Modal (Premium Design) */}
      {selectedProsumer && (
        <div className="premium-modal-overlay">
          <div className="premium-modal-content fade-in">
            <div className="premium-modal-header">
              <div className="premium-modal-icon-container">
                <FiUsers />
              </div>
              <div className="premium-modal-title-group">
                <h2>Prosumer Details</h2>
                <p>View detailed information about this prosumer.</p>
              </div>
              <button className="premium-modal-close-btn" onClick={() => setSelectedProsumer(null)}>
                <FiX />
              </button>
            </div>
            
            <div className="premium-modal-body">
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiUser /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Full Name</span>
                  <span className="premium-info-value">{selectedProsumer.fullName}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiCreditCard /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">NIC Number</span>
                  <span className="premium-info-value">{selectedProsumer.nic}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiMail /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Email Address</span>
                  <span className="premium-info-value">{selectedProsumer.email}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiPhone /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Phone Number</span>
                  <span className="premium-info-value">{selectedProsumer.phone}</span>
                </div>
              </div>
              
              <div className="premium-info-card full-width">
                <div className="premium-info-icon"><FiMapPin /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Address</span>
                  <span className="premium-info-value">{selectedProsumer.address || 'Colombo'}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiActivity /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Status</span>
                  <div className="premium-info-value">
                    <span className={`status-badge-btn static-badge status-${selectedProsumer.accountStatus?.toLowerCase() || 'unknown'}`} style={{ display: 'inline-flex', padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: '600', backgroundColor: (selectedProsumer.accountStatus === 'ACTIVE' || selectedProsumer.accountStatus === 1) ? '#dcfce7' : (selectedProsumer.accountStatus === 'PENDING' || selectedProsumer.accountStatus === 0) ? '#fef3c7' : '#fee2e2', color: (selectedProsumer.accountStatus === 'ACTIVE' || selectedProsumer.accountStatus === 1) ? '#166534' : (selectedProsumer.accountStatus === 'PENDING' || selectedProsumer.accountStatus === 0) ? '#b45309' : '#991b1b' }}>
                      <span className="status-dot" style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'currentColor', marginRight: '6px' }}></span>
                      {(selectedProsumer.accountStatus === 'ACTIVE' || selectedProsumer.accountStatus === 1) ? 'Active' : (selectedProsumer.accountStatus === 'PENDING' || selectedProsumer.accountStatus === 0) ? 'Pending' : 'Deactivated'}
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiCalendar /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Created At</span>
                  <span className="premium-info-value">{selectedProsumer.createdAt ? new Date(selectedProsumer.createdAt).toLocaleString() : 'N/A'}</span>
                </div>
              </div>
            </div>
            
            <div className="premium-modal-footer">
              <button className="btn-premium-close" onClick={() => setSelectedProsumer(null)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* Status Confirmation Modal */}
      {statusConfirm && (
        <div className="user-modal-overlay">
          <div className="user-modal-content status-confirm-modal fade-in">
            <div className="user-modal-header">
              <h2>Confirm {statusConfirm.actionText}</h2>
              <button className="user-modal-close" onClick={() => setStatusConfirm(null)}>&times;</button>
            </div>
            <div className="user-modal-body">
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
                <div className="user-avatar" style={{ width: '48px', height: '48px', fontSize: '1.2rem', backgroundColor: `${getAvatarColor(statusConfirm.name)}20`, color: getAvatarColor(statusConfirm.name), display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '8px' }}>
                  {getInitials(statusConfirm.name)}
                </div>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#0f172a' }}>{statusConfirm.name}</h3>
                  <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>NIC: {statusConfirm.nic}</p>
                </div>
              </div>
              <div style={{ backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                  {statusConfirm.action === 'ACTIVATE' || statusConfirm.action === 'REACTIVATE' ? (
                    <FiUserCheck style={{ color: '#10b981', fontSize: '1.5rem' }} />
                  ) : (
                    <FiUserX style={{ color: '#ef4444', fontSize: '1.5rem' }} />
                  )}
                  <span style={{ fontWeight: '600', color: '#1e293b' }}>
                    {statusConfirm.action === 'ACTIVATE' || statusConfirm.action === 'REACTIVATE' ? 'Activate Account' : 'Deactivate Account'}
                  </span>
                </div>
                <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>
                  Are you sure you want to {statusConfirm.actionText.toLowerCase()} this account?
                  {statusConfirm.action === 'DEACTIVATE' && ' They will lose access to the system.'}
                </p>
              </div>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setStatusConfirm(null)}>Cancel</button>
              <button 
                className="btn-modal-confirm" 
                style={{ backgroundColor: statusConfirm.action === 'DEACTIVATE' ? '#ef4444' : '#10b981' }} 
                onClick={confirmStatusChange}
              >
                {statusConfirm.actionText}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Prosumers;
