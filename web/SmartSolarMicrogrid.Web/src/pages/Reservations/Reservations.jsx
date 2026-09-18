import React, { useState, useEffect } from 'react';
import { FiInfo, FiCalendar, FiEdit2, FiX, FiSearch, FiArrowRight, FiRefreshCcw, FiFilter, FiEye, FiTrash2, FiChevronLeft, FiChevronRight, FiCheck } from 'react-icons/fi';
import { reservationService } from '../../services/reservationService';
import { stationService } from '../../services/stationService';
import './Reservations.css';

const Reservations = () => {
  const [reservations, setReservations] = useState([]);
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState('ALL');
  
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
    if (status === 1 || status === 'APPROVED') {
      return <span className="status-badge confirmed">Confirmed</span>;
    } else if (status === 2 || status === 'CANCELLED') {
      return <span className="status-badge cancelled">Cancelled</span>;
    } else if (status === 0 || status === 'PENDING') {
      return <span className="status-badge pending">Pending</span>;
    } else {
      return <span className="status-badge pending">{status}</span>;
    }
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
    if (activeTab === 'ALL') return true;
    if (activeTab === 'PENDING' && (res.status === 0 || res.status === 'PENDING')) return true;
    if (activeTab === 'APPROVED' && (res.status === 1 || res.status === 'APPROVED')) return true;
    if (activeTab === 'CANCELLED' && (res.status === 2 || res.status === 'CANCELLED')) return true;
    if (activeTab === 'COMPLETED' && (res.status === 3 || res.status === 'COMPLETED')) return true;
    return false;
  });

  return (
    <div className="reservations-container fade-in">
      
      {/* Header */}
      <div className="reservations-header">
        <div className="header-left">
          <h1>Reservation Management</h1>
          <p>View, search and manage all charging reservations across your solar microgrid network.</p>
        </div>
        <div className="breadcrumbs">
          <span>Home</span> &gt; 
          <span>Reservations</span> &gt; 
          <span>Reservation Management</span>
        </div>
      </div>

      {/* Rules Banner */}
      <div className="rules-banner">
        <div className="rules-banner-header">
          <div className="info-icon-circle"><FiInfo /></div>
          Reservation Rules
        </div>
        <div className="rules-grid">
          <div className="rule-item">
            <div className="rule-icon-wrapper booking"><FiCalendar /></div>
            <div className="rule-content">
              <span className="rule-title">Booking Window</span>
              <span className="rule-desc">Reservations can only be made within 7 days from the current date.</span>
            </div>
          </div>
          <div className="rule-item">
            <div className="rule-icon-wrapper update"><FiEdit2 /></div>
            <div className="rule-content">
              <span className="rule-title">Update Notice</span>
              <span className="rule-desc">Updates to a reservation require at least 12 hours notice before the scheduled start time.</span>
            </div>
          </div>
          <div className="rule-item">
            <div className="rule-icon-wrapper cancel"><FiX /></div>
            <div className="rule-content">
              <span className="rule-title">Cancellation Notice</span>
              <span className="rule-desc">Cancellations require at least 12 hours notice before the scheduled start time.</span>
            </div>
          </div>
        </div>
      </div>

      {/* Filters Bar */}
      <div className="reservations-filters-bar">
        <div className="filters-top-row">
          <div className="filter-item search-item">
            <label>Search</label>
            <div className="filter-input">
              <FiSearch style={{color: '#94a3b8'}} />
              <input type="text" placeholder="Search by ID or name..." />
            </div>
          </div>
          
          {/* Status filter removed, replaced by tabs below */}

          <div className="filter-item">
            <label>Date Range</label>
            <div className="filter-input" style={{fontSize: '13px', justifyContent: 'center'}}>
              <FiCalendar style={{color: '#64748b'}} />
              <span>Apr 15, 2025</span>
              <FiArrowRight style={{color: '#94a3b8', margin: '0 4px'}} />
              <span>Apr 30, 2025</span>
            </div>
          </div>

          <div className="filter-item">
            <label>Station</label>
            <div className="filter-select">
              <select defaultValue="all">
                <option value="all">All Stations</option>
                {stations.map(st => (
                  <option key={st.stationId} value={st.stationId}>{st.stationName}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        <div className="filters-bottom-row">
          <div className="actions-group">
            <button className="btn-reset">Reset</button>
            <button className="btn-apply"><FiFilter /> Apply Filters</button>
          </div>
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
          <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <span className="showing-text">Showing 1 - {filteredReservations.length} reservations</span>
            <div className="per-page-control">
              Show 
              <select className="per-page-select">
                <option>10</option>
                <option>25</option>
                <option>50</option>
              </select>
              per page
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
                <th>
                  <div className="th-content">Status</div>
                </th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="8" style={{textAlign: 'center', padding: '24px'}}>Loading reservations...</td></tr>
              ) : filteredReservations.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{textAlign: 'center', padding: '40px', color: '#64748b'}}>
                    No reservations found for the selected filters.
                  </td>
                </tr>
              ) : (
                filteredReservations.map(res => (
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
                    <td>
                      {mapStatusToBadge(res.status)}
                    </td>
                    <td>
                      <div className="table-actions">
                        {(res.status === 0 || res.status === 'PENDING') && (
                          <button className="pill-btn btn-view" style={{ backgroundColor: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0' }} title="Approve Reservation" onClick={() => setConfirmModal({ isOpen: true, type: 'APPROVE', reservationId: res.reservationId })}><FiCheck /> Approve</button>
                        )}
                        <button className="pill-btn btn-view" title="View Details" onClick={() => { setSelectedReservation(res); setShowViewModal(true); }}><FiEye /> View</button>
                        {res.status !== 2 && res.status !== 'CANCELLED' && res.status !== 3 && res.status !== 'COMPLETED' && (
                          <button className="action-btn delete" title="Cancel Reservation" onClick={() => setConfirmModal({ isOpen: true, type: 'CANCEL', reservationId: res.reservationId })}><FiTrash2 /></button>
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
          <span className="showing-text">Showing 1 - 10 of 24 reservations</span>
          <div className="pagination">
            <button className="page-btn"><FiChevronLeft /></button>
            <button className="page-btn active">1</button>
            <button className="page-btn">2</button>
            <button className="page-btn">3</button>
            <button className="page-btn"><FiChevronRight /></button>
          </div>
        </div>
      </div>

      {/* View Modal */}
      {showViewModal && selectedReservation && (
        <div className="modal-overlay">
          <div className="modal-content view-modal">
            <div className="modal-header">
              <h2>Reservation Details</h2>
              <button className="close-btn" onClick={() => setShowViewModal(false)}><FiX /></button>
            </div>
            <div className="modal-body" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="detail-group">
                <label>Reservation ID</label>
                <div className="detail-value">{selectedReservation.reservationNumber || selectedReservation.reservationId}</div>
              </div>
              <div className="detail-group">
                <label>Prosumer NIC</label>
                <div className="detail-value">{selectedReservation.prosumerNic}</div>
              </div>
              <div className="detail-group">
                <label>Station / Slot</label>
                <div className="detail-value">
                  {getStationName(selectedReservation.stationId)}<br/>
                  <span style={{color: '#64748b', fontSize: '13px'}}>{selectedReservation.slotName || 'Unknown Slot'}</span>
                </div>
              </div>
              <div className="detail-group">
                <label>Status</label>
                <div className="detail-value">
                  {mapStatusToBadge(selectedReservation.status)}
                </div>
              </div>
              <div className="detail-group">
                <label>Start Date & Time</label>
                <div className="detail-value">
                  {formatDate(selectedReservation.scheduledStartDateTime)} at {formatTime(selectedReservation.scheduledStartDateTime)}
                </div>
              </div>
              <div className="detail-group">
                <label>End Date & Time</label>
                <div className="detail-value">
                  {formatDate(selectedReservation.scheduledEndDateTime)} at {formatTime(selectedReservation.scheduledEndDateTime)}
                </div>
              </div>
              <div className="detail-group">
                <label>Created At</label>
                <div className="detail-value">
                  {formatDate(selectedReservation.createdAt)} {formatTime(selectedReservation.createdAt)}
                </div>
              </div>
              <div className="detail-group">
                <label>Updated At</label>
                <div className="detail-value">
                  {formatDate(selectedReservation.updatedAt)} {formatTime(selectedReservation.updatedAt)}
                </div>
              </div>
              {selectedReservation.completedAt && (
                <div className="detail-group">
                  <label>Completed At</label>
                  <div className="detail-value">
                    {formatDate(selectedReservation.completedAt)} {formatTime(selectedReservation.completedAt)}
                  </div>
                </div>
              )}
              <div className="detail-group" style={{ gridColumn: '1 / -1' }}>
                <label>QR Reference (Mobile Verification Code)</label>
                <div className="detail-value" style={{ fontFamily: 'monospace', background: '#f8fafc', padding: '8px', borderRadius: '4px', border: '1px dashed #cbd5e1' }}>
                  {selectedReservation.qrReference || selectedReservation.reservationId}
                </div>
              </div>
            </div>
            <div className="modal-actions" style={{ marginTop: '24px', display: 'flex', justifyContent: 'flex-end' }}>
              <button className="btn-secondary" onClick={() => setShowViewModal(false)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* Confirmation Modal */}
      {confirmModal.isOpen && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '400px' }}>
            <div className="modal-header">
              <h2>{confirmModal.type === 'APPROVE' ? 'Approve Reservation' : 'Cancel Reservation'}</h2>
              <button className="close-btn" onClick={() => setConfirmModal({ isOpen: false, type: '', reservationId: null })}><FiX /></button>
            </div>
            <div className="modal-body">
              <p style={{ margin: '0 0 16px 0', color: '#475569', lineHeight: '1.5' }}>
                {confirmModal.type === 'APPROVE' 
                  ? "Are you sure you want to approve this reservation? The Prosumer will be able to proceed with energy exchange." 
                  : "Are you sure you want to cancel this reservation? This action cannot be undone."}
              </p>
            </div>
            <div className="modal-actions" style={{ padding: '0 24px 24px 24px', display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
              <button className="btn-secondary" onClick={() => setConfirmModal({ isOpen: false, type: '', reservationId: null })}>Close</button>
              <button 
                className="btn-primary" 
                style={{ background: confirmModal.type === 'CANCEL' ? '#ef4444' : '#10b981', border: 'none', padding: '8px 16px', borderRadius: '6px', color: 'white', fontWeight: '500', cursor: 'pointer' }}
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
