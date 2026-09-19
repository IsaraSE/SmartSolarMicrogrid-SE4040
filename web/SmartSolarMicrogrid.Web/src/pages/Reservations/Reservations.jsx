import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { FiInfo, FiCalendar, FiEdit2, FiX, FiSearch, FiArrowRight, FiRefreshCcw, FiFilter, FiEye, FiTrash2, FiChevronLeft, FiChevronRight, FiCheck, FiHash, FiUser, FiMapPin, FiActivity, FiClock, FiCheckCircle, FiXCircle, FiGrid, FiCopy } from 'react-icons/fi';
import { reservationService } from '../../services/reservationService';
import { stationService } from '../../services/stationService';
import { useAuth } from '../../context/AuthContext';
import './Reservations.css';

const Reservations = () => {
  const { user } = useAuth();
  const [reservations, setReservations] = useState([]);
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStation, setFilterStation] = useState('all');
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const initialTab = queryParams.get('tab') || 'ALL';
  const [activeTab, setActiveTab] = useState(initialTab);
  
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  const [selectedReservation, setSelectedReservation] = useState(null);
  const [showViewModal, setShowViewModal] = useState(false);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, type: '', reservationId: null });
  const [messageModal, setMessageModal] = useState({ isOpen: false, title: '', message: '', type: 'success' });

  useEffect(() => {
    fetchReservations();
  }, []);

  const fetchReservations = async () => {
    try {
      setLoading(true);
      const [resResponse, statResponse] = await Promise.all([
        reservationService.getReservations(),
        stationService.getAllStations()
      ]);
      setReservations(resResponse.data || []);
      setStations(statResponse.data || []);
    } catch (error) {
      console.error("Failed to load data:", error);
    } finally {
      setLoading(false);
    }
  };

  const executeApprove = async () => {
    try {
      setLoading(true);
      setConfirmModal({ ...confirmModal, isOpen: false });
      await reservationService.updateReservationStatus(confirmModal.reservationId, { status: 1 }); // 1 is APPROVED
      await fetchReservations();
      setMessageModal({
        isOpen: true,
        title: 'Success',
        message: 'Reservation approved successfully!',
        type: 'success'
      });
    } catch (error) {
      if (error.response?.status === 401) return;
      console.error("Failed to approve reservation:", error);
      setMessageModal({
        isOpen: true,
        title: 'Error',
        message: error.response?.data?.message || "Failed to approve reservation.",
        type: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const executeCancel = async () => {
    try {
      setLoading(true);
      setConfirmModal({ ...confirmModal, isOpen: false });
      await reservationService.cancelReservation(confirmModal.reservationId);
      await fetchReservations();
      setMessageModal({
        isOpen: true,
        title: 'Success',
        message: 'Reservation cancelled successfully!',
        type: 'success'
      });
    } catch (error) {
      if (error.response?.status === 401) return;
      console.error("Failed to cancel reservation:", error);
      setMessageModal({
        isOpen: true,
        title: 'Error',
        message: error.response?.data?.message || "Failed to cancel reservation.",
        type: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const getStationName = (stationId) => {
    const station = stations.find(s => s.stationId === stationId);
    return station ? station.stationName : (stationId?.substring(0,8) + '...');
  };

  const mapStatusToBadge = (status) => {
    // API returns 0: PENDING, 1: APPROVED, 2: CANCELLED, 3: COMPLETED
    let st = status;
    if (typeof status === 'string') {
      st = status.toUpperCase();
    }

    let bgColor = '#f1f5f9';
    let textColor = '#64748b';
    let text = st;

    if (st === 1 || st === 'APPROVED') {
      bgColor = '#dcfce7'; // green
      textColor = '#166534';
      text = 'Confirmed';
    } else if (st === 2 || st === 'CANCELLED') {
      bgColor = '#fee2e2'; // red
      textColor = '#991b1b';
      text = 'Cancelled';
    } else if (st === 0 || st === 'PENDING') {
      bgColor = '#fef3c7'; // yellow
      textColor = '#b45309';
      text = 'Pending';
    } else if (st === 3 || st === 'COMPLETED') {
      bgColor = '#dbeafe'; // blue
      textColor = '#1e40af';
      text = 'Completed';
    }

    return (
      <span 
        style={{ 
          display: 'inline-flex', 
          width: '130px',
          justifyContent: 'center',
          padding: '6px 16px', 
          borderRadius: '20px', 
          fontSize: '0.85rem', 
          fontWeight: '500', 
          border: 'none', 
          cursor: 'default', 
          backgroundColor: bgColor, 
          color: textColor 
        }}
      >
        {text}
      </span>
    );
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' });
  };

  const formatTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
  };

  const filteredReservations = reservations.filter(res => {
    // 1. Tab filter
    let matchTab = false;
    if (activeTab === 'ALL') matchTab = true;
    else if (activeTab === 'PENDING' && (res.status === 0 || res.status === 'PENDING')) matchTab = true;
    else if (activeTab === 'APPROVED' && (res.status === 1 || res.status === 'APPROVED')) matchTab = true;
    else if (activeTab === 'CANCELLED' && (res.status === 2 || res.status === 'CANCELLED')) matchTab = true;
    else if (activeTab === 'COMPLETED' && (res.status === 3 || res.status === 'COMPLETED')) matchTab = true;
    
    if (!matchTab) return false;

    // 2. Station filter
    if (filterStation !== 'all' && res.stationId !== filterStation) return false;

    // 3. Search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const resId = (res.reservationNumber || res.reservationId || '').toLowerCase();
      const prosumer = (res.prosumerNic || '').toLowerCase();
      const stationName = (getStationName(res.stationId) || '').toLowerCase();
      const slotName = (res.slotName || '').toLowerCase();
      
      if (!resId.includes(q) && !prosumer.includes(q) && !stationName.includes(q) && !slotName.includes(q)) {
        return false;
      }
    }
    return true;
  });

  // Reset pagination when filters change
  useEffect(() => {
    setCurrentPage(1);
  }, [activeTab, filterStation, searchQuery]);

  const totalPages = Math.ceil(filteredReservations.length / itemsPerPage);

  // Pagination Calculations
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredReservations.slice(indexOfFirstItem, indexOfLastItem);

  return (
    <div className="reservations-container fade-in">
      
      {/* Header */}
      <div className="reservations-header">
        <div className="reservations-title">
          <h1>Reservation Management</h1>
          <p>View, search and manage all charging reservations across your solar microgrid network.</p>
        </div>
        <div className="reservations-breadcrumbs">
          <span>Reservations</span>
          <span className="separator">›</span>
          <span className="current">Reservation Management</span>
        </div>
      </div>




      {/* Table Card */}
      <div className="reservations-table-card">
        <div className="table-header-controls" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div className="table-tabs" style={{ display: 'flex', gap: '8px' }}>
            <button className={`tab-btn ${activeTab === 'ALL' ? 'active' : ''}`} onClick={() => setActiveTab('ALL')}>All</button>
            <button className={`tab-btn ${activeTab === 'PENDING' ? 'active' : ''}`} onClick={() => setActiveTab('PENDING')}>Pending</button>
            <button className={`tab-btn ${activeTab === 'APPROVED' ? 'active' : ''}`} onClick={() => setActiveTab('APPROVED')}>Approved</button>
            <button className={`tab-btn ${activeTab === 'CANCELLED' ? 'active' : ''}`} onClick={() => setActiveTab('CANCELLED')}>Cancelled</button>
            <button className={`tab-btn ${activeTab === 'COMPLETED' ? 'active' : ''}`} onClick={() => setActiveTab('COMPLETED')}>Completed</button>
          </div>
          
          <div className="table-actions-right" style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <div className="table-search" style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <FiSearch style={{ position: 'absolute', left: '16px', color: '#94a3b8', fontSize: '18px' }} />
              <input 
                type="text" 
                placeholder="Search reservations..." 
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={{ 
                  padding: '12px 40px 12px 46px', 
                  borderRadius: '10px', 
                  border: '1px solid #e2e8f0', 
                  outline: 'none', 
                  width: '260px', 
                  fontSize: '15px',
                  boxSizing: 'border-box'
                }}
              />
            </div>
            
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <FiMapPin style={{ position: 'absolute', left: '16px', color: '#64748b', fontSize: '18px' }} />
              <select 
                value={filterStation}
                onChange={(e) => setFilterStation(e.target.value)}
                style={{ 
                  padding: '12px 40px 12px 46px', 
                  borderRadius: '10px', 
                  border: '1px solid #e2e8f0', 
                  outline: 'none', 
                  backgroundColor: 'white', 
                  appearance: 'none', 
                  cursor: 'pointer', 
                  fontSize: '15px',
                  fontWeight: '600',
                  color: '#0f172a',
                  width: '260px',
                  boxSizing: 'border-box'
                }}
              >
                <option value="all">All Stations</option>
                {stations.map(station => (
                  <option key={station.stationId} value={station.stationId}>{station.stationName}</option>
                ))}
              </select>
              <svg 
                width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" 
                style={{ position: 'absolute', right: '16px', color: '#0f172a', pointerEvents: 'none' }}
              >
                <polyline points="7 15 12 20 17 15"></polyline>
                <polyline points="7 9 12 4 17 9"></polyline>
              </svg>
            </div>
          </div>
        </div>

        <div className="table-responsive-wrapper">
          <table className="res-table">
            <thead>
              <tr>
                <th>
                  <div className="th-content">Reservation ID</div>
                </th>
                <th>
                  <div className="th-content">Prosumer</div>
                </th>
                <th>
                  <div className="th-content">Station / Slot</div>
                </th>
                <th>
                  <div className="th-content">Start Date & Time</div>
                </th>
                <th>
                  <div className="th-content">End Date & Time</div>
                </th>
                <th style={{textAlign: 'center'}}>
                  <div className="th-content" style={{justifyContent: 'center'}}>Status</div>
                </th>
                <th style={{textAlign: 'center'}}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="7" style={{textAlign: 'center', padding: '40px'}}>Loading reservations...</td></tr>
              ) : currentItems.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{textAlign: 'center', padding: '40px', color: '#64748b'}}>
                    No reservations found matching the filters.
                  </td>
                </tr>
              ) : (
                currentItems.map(res => (
                  <tr key={res.reservationId}>
                    
                    <td className="res-id">{res.reservationNumber || res.reservationId?.substring(0,8).toUpperCase() || 'RES-####'}</td>
                    <td className="res-nic">{res.prosumerNic || 'N/A'}</td>
                    <td>
                      <div className="date-time-cell">
                        <span>{getStationName(res.stationId)}</span>
                        <span className="time-text">{res.slotName || 'Unknown Slot'}</span>
                      </div>
                    </td>
                    <td>
                      <div className="date-time-cell">
                        <span>{formatDate(res.scheduledStartDateTime)}</span>
                        <span className="time-text">{formatTime(res.scheduledStartDateTime)}</span>
                      </div>
                    </td>
                    <td>
                      <div className="date-time-cell">
                        <span>{formatDate(res.scheduledEndDateTime)}</span>
                        <span className="time-text">{formatTime(res.scheduledEndDateTime)}</span>
                      </div>
                    </td>
                    <td style={{textAlign: 'center'}}>
                      {mapStatusToBadge(res.status)}
                    </td>
                    <td>
                      <div className="table-actions" style={{ display: 'flex', gap: '8px', alignItems: 'center', justifyContent: 'center' }}>
                        <button className="review-btn" onClick={() => { setSelectedReservation(res); setShowViewModal(true); }}>Review</button>
                        {(res.status === 0 || res.status === 'PENDING') && user?.role === 'GRID_OPERATOR' && (
                          <button className="activate-btn" onClick={() => setConfirmModal({ isOpen: true, type: 'APPROVE', reservationId: res.reservationId })}>Approve</button>
                        )}
                        {res.status !== 2 && res.status !== 'CANCELLED' && res.status !== 3 && res.status !== 'COMPLETED' && user?.role === 'GRID_OPERATOR' && (
                          <button className="deactivate-btn" onClick={() => setConfirmModal({ isOpen: true, type: 'CANCEL', reservationId: res.reservationId })}>Cancel</button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="table-footer">
          <div className="footer-info">
            Showing {filteredReservations.length === 0 ? 0 : indexOfFirstItem + 1} to {Math.min(indexOfLastItem, filteredReservations.length)} of {filteredReservations.length} reservations
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
                <option value={50}>50</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* View Reservation Modal (Premium Design) */}
      {showViewModal && selectedReservation && (
        <div className="premium-modal-overlay">
          <div className="premium-modal-content fade-in">
            <div className="premium-modal-header">
              <div className="premium-modal-icon-container">
                <FiCalendar />
              </div>
              <div className="premium-modal-title-group">
                <h2>Reservation Details</h2>
                <p>View complete reservation information and status</p>
              </div>
              <button className="premium-modal-close-btn" onClick={() => setShowViewModal(false)}>
                <FiX />
              </button>
            </div>
            
            <div className="premium-modal-body">
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiHash /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Reservation ID</span>
                  <span className="premium-info-value">{selectedReservation.reservationNumber || selectedReservation.reservationId}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiUser /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Prosumer NIC</span>
                  <span className="premium-info-value">{selectedReservation.prosumerNic}</span>
                </div>
              </div>
              
              <div className="premium-info-card full-width">
                <div className="premium-info-icon"><FiMapPin /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Station / Slot</span>
                  <span className="premium-info-value">{getStationName(selectedReservation.stationId)} - {selectedReservation.slotName || 'Unknown Slot'}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiCalendar /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Start Date & Time</span>
                  <span className="premium-info-value">{formatDate(selectedReservation.scheduledStartDateTime)} at {formatTime(selectedReservation.scheduledStartDateTime)}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiCalendar /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">End Date & Time</span>
                  <span className="premium-info-value">{formatDate(selectedReservation.scheduledEndDateTime)} at {formatTime(selectedReservation.scheduledEndDateTime)}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiActivity /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Status</span>
                  <div className="premium-info-value">
                    {mapStatusToBadge(selectedReservation.status)}
                  </div>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiClock /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Created At</span>
                  <span className="premium-info-value">{formatDate(selectedReservation.createdAt)} {formatTime(selectedReservation.createdAt)}</span>
                </div>
              </div>
              
              <div className="premium-info-card full-width">
                <div className="premium-info-icon"><FiGrid /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">QR Reference</span>
                  <span className="premium-info-value">
                    {selectedReservation.qrReference || selectedReservation.reservationId}
                    <button style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', display: 'flex', alignItems: 'center' }} onClick={() => navigator.clipboard.writeText(selectedReservation.qrReference || selectedReservation.reservationId)} title="Copy QR Reference">
                      <FiCopy />
                    </button>
                  </span>
                </div>
              </div>
            </div>
            
            <div className="premium-modal-footer has-info">
              <div className="premium-footer-info">
                {selectedReservation.status === 2 || selectedReservation.status === 'CANCELLED' ? (
                  <>
                    <FiInfo style={{ fontSize: '16px' }} />
                    <span>This reservation was cancelled and is no longer active.</span>
                  </>
                ) : selectedReservation.status === 0 || selectedReservation.status === 'PENDING' ? (
                  <>
                    <FiInfo style={{ fontSize: '16px' }} />
                    <span>This reservation is pending approval.</span>
                  </>
                ) : (
                  <>
                    <FiInfo style={{ fontSize: '16px', opacity: 0 }} />
                    <span></span>
                  </>
                )}
              </div>
              <button className="btn-premium-close" onClick={() => setShowViewModal(false)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* Confirmation Modal */}
      {confirmModal.isOpen && (
        <div className="user-modal-overlay">
          <div className="user-modal-content status-confirm-modal fade-in">
            <div className="user-modal-header">
              <h2>{confirmModal.type === 'APPROVE' ? 'Approve Reservation' : 'Cancel Reservation'}</h2>
              <button className="user-modal-close" onClick={() => setConfirmModal({ isOpen: false, type: '', reservationId: null })}>&times;</button>
            </div>
            <div className="user-modal-body">
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
                <div className="detail-icon" style={{ backgroundColor: '#f1f5f9', color: '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', width: '48px', height: '48px', borderRadius: '8px' }}>
                  <FiCalendar style={{ fontSize: '1.2rem' }} />
                </div>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#0f172a' }}>Reservation Action</h3>
                  <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>ID: {confirmModal.reservationId}</p>
                </div>
              </div>
              <div style={{ backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>
                  {confirmModal.type === 'APPROVE' 
                    ? "Are you sure you want to approve this reservation? The Prosumer will be able to proceed with energy exchange." 
                    : "Are you sure you want to cancel this reservation? This action cannot be undone."}
                </p>
              </div>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setConfirmModal({ isOpen: false, type: '', reservationId: null })}>Cancel</button>
              <button 
                className="btn-modal-confirm" 
                style={{ backgroundColor: confirmModal.type === 'CANCEL' ? '#ef4444' : '#10b981' }}
                onClick={confirmModal.type === 'APPROVE' ? executeApprove : executeCancel}
              >
                {confirmModal.type === 'APPROVE' ? 'Approve' : 'Cancel Reservation'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Message Modal */}
      {messageModal.isOpen && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '400px' }}>
            <div className="modal-header">
              <h2>{messageModal.title}</h2>
              <button className="close-btn" onClick={() => setMessageModal({ isOpen: false, title: '', message: '', type: 'success' })}><FiX /></button>
            </div>
            <div className="modal-body">
              <p style={{ margin: '0 0 16px 0', color: '#475569', lineHeight: '1.5' }}>
                {messageModal.message}
              </p>
            </div>
            <div className="modal-actions" style={{ padding: '0 24px 24px 24px', display: 'flex', justifyContent: 'flex-end' }}>
              <button 
                className="btn-primary" 
                style={{ background: messageModal.type === 'error' ? '#ef4444' : '#10b981', border: 'none', padding: '8px 16px', borderRadius: '6px', color: 'white', fontWeight: '500', cursor: 'pointer' }}
                onClick={() => setMessageModal({ isOpen: false, title: '', message: '', type: 'success' })}
              >
                Okay
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default Reservations;
