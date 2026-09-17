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
  FiX
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
        <div className="header-left">
          <h1>Prosumer Management</h1>
          <p>Manage solar prosumers in the microgrid network. View, activate or deactivate prosumer accounts.</p>
        </div>
        <div className="header-right">
          <div className="date-widget">
            <FiClock className="widget-icon" />
            <div className="widget-content">
              <span className="widget-title">{formattedToday}</span>
              <span className="widget-subtitle">Good to see you today.</span>
            </div>
          </div>
          <div className="weather-widget">
            <PiSunLight className="widget-icon text-yellow" />
            <div className="widget-content">
              <span className="widget-title">A cleaner</span>
              <span className="widget-subtitle">tomorrow is possible.</span>
            </div>
          </div>
        </div>
      </div>

      <div className="summary-cards">
        <div className="summary-card total-card">
          <div className="card-top">
            <div className="card-icon-wrapper green-bg"><FiUsers className="card-icon green-text" /></div>
            <span className="card-title">Total Prosumers</span>
          </div>
          <div className="card-bottom">
            <div className="card-value">{totalProsumers}</div>
            <div className="card-trend green-text">
              <FiTrendingUp /> 12% 
              <span className="trend-text">vs. last month</span>
            </div>
            <div className="sparkline green-line"></div>
          </div>
        </div>

        <div className="summary-card pending-card">
          <div className="card-top">
            <div className="card-icon-wrapper orange-bg"><FiClock className="card-icon orange-text" /></div>
            <span className="card-title">Pending Activation</span>
          </div>
          <div className="card-bottom">
            <div className="card-value">{pendingCount}</div>
            <div className="card-trend red-text">
              <FiTrendingDown /> 25%
              <span className="trend-text">vs. last month</span>
            </div>
            <div className="sparkline yellow-line"></div>
          </div>
        </div>

        <div className="summary-card active-card">
          <div className="card-top">
            <div className="card-icon-wrapper blue-bg"><FiUserCheck className="card-icon blue-text" /></div>
            <span className="card-title">Active Prosumers</span>
          </div>
          <div className="card-bottom">
            <div className="card-value">{activeCount}</div>
            <div className="card-trend green-text">
              <FiTrendingUp /> 18%
              <span className="trend-text">vs. last month</span>
            </div>
            <div className="sparkline blue-line"></div>
          </div>
        </div>

        <div className="summary-card deactivated-card">
          <div className="card-top">
            <div className="card-icon-wrapper red-bg"><FiUserX className="card-icon red-text" /></div>
            <span className="card-title">Deactivated</span>
          </div>
          <div className="card-bottom">
            <div className="card-value">{deactivatedCount}</div>
            <div className="card-trend red-text">
              <FiTrendingDown /> 6%
              <span className="trend-text">vs. last month</span>
            </div>
            <div className="sparkline red-line"></div>
          </div>
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
            <button className="btn-filter">
              <FiFilter /> Filters
            </button>
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
                <th>Account Status</th>
                <th className="th-actions">Actions</th>
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
                        <div 
                          className="user-avatar" 
                          style={{ backgroundColor: `${getAvatarColor(prosumer.fullName)}20`, color: getAvatarColor(prosumer.fullName) }}
                        >
                          {getInitials(prosumer.fullName)}
                        </div>
                        <span className="user-name">
                          {prosumer.fullName ? prosumer.fullName.split(' ').slice(0, 2).join(' ') : 'Unknown'}
                        </span>
                      </div>
                    </td>
                    <td className="cell-email">
                      <div className="email-text" title={prosumer.email}>{prosumer.email}</div>
                    </td>
                    <td className="cell-phone">{prosumer.phone}</td>
                    <td>
                      <span className={`status-badge-btn static-badge status-${prosumer.accountStatus?.toLowerCase() || 'unknown'}`}>
                        <div className="status-badge-content">
                          <span className="status-dot"></span>
                          <span>{prosumer.accountStatus === 'ACTIVE' ? 'Active' : prosumer.accountStatus === 'PENDING' ? 'Pending' : 'Deactivated'}</span>
                        </div>
                      </span>
                    </td>
                    <td className="cell-actions">
                      <button className="btn-action btn-view-text" onClick={() => setSelectedProsumer(prosumer)}>
                        <FiEye style={{ marginRight: '4px' }} /> View
                      </button>
                      {activeTab === 'Pending' && (
                        <button className="btn-action btn-activate" onClick={() => setStatusConfirm({ action: 'ACTIVATE', actionText: 'Activate', nic: prosumer.nic, name: prosumer.fullName })}>
                          <FiPlay style={{ marginRight: '4px' }} /> Activate
                        </button>
                      )}
                      {activeTab === 'Active' && (
                        <button className="btn-action btn-deactivate" onClick={() => setStatusConfirm({ action: 'DEACTIVATE', actionText: 'Deactivate', nic: prosumer.nic, name: prosumer.fullName })}>
                          <FiSlash style={{ marginRight: '4px' }} /> Deactivate
                        </button>
                      )}
                      {activeTab === 'Deactivated' && (
                        <button className="btn-action btn-reactivate" onClick={() => setStatusConfirm({ action: 'REACTIVATE', actionText: 'Reactivate', nic: prosumer.nic, name: prosumer.fullName })}>
                          <FiPlay style={{ marginRight: '4px' }} /> Reactivate
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

      {/* View Prosumer Modal */}
      {selectedProsumer && (
        <div className="user-modal-overlay">
          <div className="user-modal-content fade-in">
            <div className="user-modal-header">
              <h2>Prosumer Details</h2>
              <button className="user-modal-close" onClick={() => setSelectedProsumer(null)}>&times;</button>
            </div>
            <div className="user-modal-body">
              <div className="detail-group">
                <label>Full Name</label>
                <div className="detail-value">{selectedProsumer.fullName}</div>
              </div>
              <div className="detail-group">
                <label>Email Address</label>
                <div className="detail-value">{selectedProsumer.email}</div>
              </div>
              <div className="detail-group">
                <label>Phone Number</label>
                <div className="detail-value">{selectedProsumer.phone}</div>
              </div>
              <div className="detail-group">
                <label>NIC Number</label>
                <div className="detail-value">{selectedProsumer.nic}</div>
              </div>
              <div className="detail-group">
                <label>Account Status</label>
                <div className="detail-value">{selectedProsumer.accountStatus === 'ACTIVE' ? 'Active' : selectedProsumer.accountStatus === 'PENDING' ? 'Pending' : 'Deactivated'}</div>
              </div>
              <div className="detail-group">
                <label>Address</label>
                <div className="detail-value">{selectedProsumer.address || 'None'}</div>
              </div>
              <div className="detail-group">
                <label>Created At</label>
                <div className="detail-value">
                  {selectedProsumer.createdAt ? new Date(selectedProsumer.createdAt).toLocaleString() : 'N/A'}
                </div>
              </div>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-close" onClick={() => setSelectedProsumer(null)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* Status Confirmation Modal */}
      {statusConfirm && (
        <div className="user-modal-overlay">
          <div className="user-modal-content status-confirm-modal fade-in">
            <div className="user-modal-header">
              <h2>Confirm Status Change</h2>
              <button className="user-modal-close" onClick={() => setStatusConfirm(null)}>&times;</button>
            </div>
            <div className="user-modal-body">
              <p>
                Are you sure you want to <strong>{statusConfirm.actionText.toLowerCase()}</strong> the prosumer account for <strong>{statusConfirm.name}</strong>?
              </p>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setStatusConfirm(null)}>Cancel</button>
              <button className="btn-modal-confirm" onClick={confirmStatusChange}>{statusConfirm.actionText}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Prosumers;
