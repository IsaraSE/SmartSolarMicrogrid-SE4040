import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FiSearch, 
  FiFilter, 
  FiMoreVertical,
  FiUsers,
  FiClock,
  FiUserCheck,
  FiUserX,
  FiTrendingUp,
  FiTrendingDown
} from 'react-icons/fi';
import { PiSunLight } from 'react-icons/pi';
import { prosumerService } from '../../services/prosumerService';
import ProsumerDetailsDrawer from './ProsumerDetailsDrawer';
import './Prosumers.css';

const Prosumers = () => {
  const navigate = useNavigate();
  const [prosumers, setProsumers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState('All'); // 'All', 'Pending', 'Active', 'Deactivated'
  const [selectedProsumer, setSelectedProsumer] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

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

  const totalProsumers = prosumers.length;
  const pendingCount = prosumers.filter(p => p.accountStatus === 'PENDING').length;
  const activeCount = prosumers.filter(p => p.accountStatus === 'ACTIVE').length;
  const deactivatedCount = prosumers.filter(p => p.accountStatus === 'INACTIVE').length;

  const filteredProsumers = useMemo(() => {
    return prosumers.filter(prosumer => {
      const matchesSearch = 
        (prosumer.nic || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (prosumer.fullName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (prosumer.email || '').toLowerCase().includes(searchTerm.toLowerCase());
      
      const matchesTab = 
        activeTab === 'All' ? true :
        activeTab === 'Pending' ? prosumer.accountStatus === 'PENDING' :
        activeTab === 'Active' ? prosumer.accountStatus === 'ACTIVE' :
        activeTab === 'Deactivated' ? prosumer.accountStatus === 'INACTIVE' : true;
        
      return matchesSearch && matchesTab;
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
          <table className="prosumers-table">
            <thead>
              <tr>
                <th>NIC</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Address</th>
                <th>Account Status</th>
                <th>Created At</th>
                <th>Last Reservation</th>
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
                    <td className="cell-name">{prosumer.fullName}</td>
                    <td className="cell-email">
                      <div className="email-text" title={prosumer.email}>{prosumer.email}</div>
                    </td>
                    <td className="cell-phone">{prosumer.phone}</td>
                    <td className="cell-address">
                      <div className="address-text" title={prosumer.address}>{prosumer.address || '-'}</div>
                    </td>
                    <td>
                      <span className={`status-badge status-${prosumer.accountStatus?.toLowerCase() || 'unknown'}`}>
                        <span className="status-dot"></span>
                        {prosumer.accountStatus === 'ACTIVE' ? 'Active' : prosumer.accountStatus === 'PENDING' ? 'Pending' : 'Deactivated'}
                      </span>
                    </td>
                    <td>{formatDate(prosumer.createdAt)}</td>
                    <td>{'-'}</td>
                    <td className="cell-actions">
                      {prosumer.accountStatus === 'PENDING' ? (
                        <button className="action-btn btn-activate" onClick={() => handleActivate(prosumer.nic)}>Activate</button>
                      ) : prosumer.accountStatus === 'INACTIVE' ? (
                        <button className="action-btn btn-reactivate" onClick={() => handleReactivate(prosumer.nic)}>Reactivate</button>
                      ) : (
                        <button className="action-btn btn-view" onClick={() => setSelectedProsumer(prosumer)}>View</button>
                      )}
                      <button className="btn-icon"><FiMoreVertical /></button>
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
                <option value={20}>20</option>
              </select>
              <span>per page</span>
            </div>
          </div>
        </div>
      </div>

      <ProsumerDetailsDrawer 
        isOpen={!!selectedProsumer} 
        onClose={() => setSelectedProsumer(null)} 
        prosumer={selectedProsumer}
        onActivate={handleActivate}
        onReactivate={handleReactivate}
      />
    </div>
  );
};

export default Prosumers;
