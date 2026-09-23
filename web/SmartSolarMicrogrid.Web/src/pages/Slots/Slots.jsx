import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  FiClock, FiCalendar, FiMapPin, FiGrid, FiUsers, FiZap,
  FiCheckCircle, FiSlash, FiEdit2, FiMoreHorizontal, FiMoreVertical, FiRefreshCcw,
  FiPlus, FiChevronLeft, FiChevronRight, FiChevronsLeft, FiChevronsRight,
  FiEye, FiTrash2, FiList, FiPlay, FiX, FiUser, FiActivity, FiFileText
} from 'react-icons/fi';

import { stationService } from '../../services/stationService';
import { slotService } from '../../services/slotService';
import dashboardService from '../../services/dashboardService';
import stationHeroBg from '../../assets/images/solar-hero-bg.jpg';
import './Slots.css';
import '../Users/Users.css'; // For modal styles
import { useAuth } from '../../context/AuthContext';

const Slots = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stations, setStations] = useState([]);
  const [selectedStation, setSelectedStation] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);

  const [reservations, setReservations] = useState([]);

  // Tab State
  const [activeTab, setActiveTab] = useState('ALL');

  // Pagination State
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  // Modal and Dropdown State
  const [activeDropdown, setActiveDropdown] = useState(null);
  const [selectedSlot, setSelectedSlot] = useState(null); // For View Modal
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [statusConfirm, setStatusConfirm] = useState(null);
  const [infoMsg, setInfoMsg] = useState(null);

  const dropdownRef = useRef(null);

  const today = new Date();
  const formattedToday = today.toLocaleDateString('en-US', { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' });

  const location = useLocation();

  useEffect(() => {
    fetchInitialData();

    const handleClickOutside = (event) => {
      if (!event.target.closest('.dropdown-container')) {
        setActiveDropdown(null);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [location.state?.stationId]);

  const fetchInitialData = async () => {
    try {
      setLoading(true);
      const response = await stationService.getAllStations();
      const stationList = response.data || [];
      setStations(stationList);

      try {
        const resRes = await dashboardService.getReservations();
        setReservations(resRes.data || resRes || []);
      } catch (err) {
        console.error("Failed to load reservations", err);
      }

      if (stationList.length > 0) {
        let initialStation = { stationId: 'all', stationName: 'All Stations' };

        const savedStationId = localStorage.getItem('lastSelectedStationId');

        if (location.state?.stationId) {
          const matched = stationList.find(s => s.stationId === location.state.stationId);
          if (matched) initialStation = matched;
        } else if (savedStationId) {
          if (savedStationId === 'all') {
            initialStation = { stationId: 'all', stationName: 'All Stations' };
          } else {
            const matched = stationList.find(s => s.stationId === savedStationId);
            if (matched) initialStation = matched;
          }
        }

        setSelectedStation(initialStation);
        localStorage.setItem('lastSelectedStationId', initialStation.stationId);
        await fetchSlotsForStation(initialStation.stationId, stationList);
      }
    } catch (error) {
      console.error("Failed to load initial data:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchSlotsForStation = async (stationId, currentStations = stations) => {
    try {
      setLoading(true);
      if (stationId === 'all') {
        const promises = currentStations.map(st => slotService.getSlotsByStationId(st.stationId));
        const results = await Promise.all(promises);
        const allSlots = results.flatMap(r => r.data || []);
        setSlots(allSlots);
      } else {
        const response = await slotService.getSlotsByStationId(stationId);
        setSlots(response.data || []);
      }
    } catch (error) {
      console.error("Failed to load slots:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleStationChange = (e) => {
    const stationId = e.target.value;
    let station = { stationId: 'all', stationName: 'All Stations' };
    if (stationId !== 'all') {
      station = stations.find(s => s.stationId === stationId);
    }
    setSelectedStation(station);
    localStorage.setItem('lastSelectedStationId', stationId);
    fetchSlotsForStation(stationId);
    setActiveTab('ALL');
  };

  const getStationName = (stationId) => {
    if (!stationId) return 'All Stations';
    const station = stations.find(s => s.stationId === stationId);
    return station ? station.stationName : 'All Stations';
  };

  const formatSlotTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', { month: 'short', day: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', hour12: true });
  };

  const getStatusText = (status) => {
    if (typeof status === 'string') {
      return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
    }
    switch (status) {
      case 0: return 'Available';
      case 1: return 'Reserved';
      case 2: return 'Unavailable';
      case 3: return 'Pending';
      default: return 'Unknown';
    }
  };

  const handleAddClick = () => {
    if (selectedStation) {
      navigate('/slots/add', { state: { stationId: selectedStation.stationId } });
    }
  };

  const getReservedByText = (slot) => {
    const st = slot.effectiveStatus || slot.status;
    if (st !== 1 && st !== 'RESERVED' && st !== 3 && st !== 'PENDING') {
      return '-';
    }
    const reservation = reservations.find(r => r.slotId === slot.slotId && (r.status === 2 || String(r.status).toUpperCase() === 'APPROVED' || r.status === 0 || String(r.status).toUpperCase() === 'PENDING'));
    if (reservation && reservation.prosumerNic) {
      return reservation.prosumerNic;
    }
    return slot.reservedBy || 'Unknown Prosumer (ID)';
  };

  const handleEditSlot = (slot) => {
    navigate(`/slots/edit/${slot.slotId}`, { state: { slot } });
  };

  const toggleDropdown = (id, event) => {
    event.stopPropagation();
    if (activeDropdown === id) {
      setActiveDropdown(null);
    } else {
      setActiveDropdown(id);
    }
  };

  const requestDeleteSlot = (slot) => {
    if (slot.status === 1) { // RESERVED
      setInfoMsg({ type: 'error', title: 'Cannot Delete', message: 'Reserved slots cannot be deleted. The reservation must be cancelled first.' });
      return;
    }
    setDeleteConfirm(slot);
    setActiveDropdown(null);
  };

  const confirmDelete = async () => {
    if (!deleteConfirm) return;
    try {
      await slotService.deleteSlot(deleteConfirm.slotId);
      setInfoMsg({ type: 'success', title: 'Deleted', message: 'Slot deleted successfully.' });
      fetchSlotsForStation(selectedStation.stationId);
    } catch (error) {
      console.error("Failed to delete slot:", error);
      setInfoMsg({ type: 'error', title: 'Error', message: error.response?.data?.message || 'Failed to delete slot.' });
    } finally {
      setDeleteConfirm(null);
    }
  };

  const requestStatusChange = (slot, newStatus) => {
    setStatusConfirm({ slot, newStatus });
  };

  const confirmStatusChange = async () => {
    if (!statusConfirm) return;
    const { slot, newStatus } = statusConfirm;

    try {
      await slotService.updateSlot(slot.slotId, {
        startDateTime: slot.startDateTime,
        endDateTime: slot.endDateTime,
        status: newStatus,
        notes: slot.notes
      });
      setInfoMsg({ type: 'success', title: 'Status Updated', message: 'Slot status updated successfully.' });
      fetchSlotsForStation(selectedStation.stationId);
    } catch (error) {
      console.error("Failed to update status:", error);
      setInfoMsg({ type: 'error', title: 'Error', message: 'Failed to update slot status.' });
    } finally {
      setStatusConfirm(null);
    }
  };

  // Filter slots based on tab
  const filteredSlots = slots.map(slot => {
    // Determine effective status to fix older slots that are stuck as AVAILABLE in DB
    const reservation = reservations.find(r => r.slotId === slot.slotId && (String(r.status).toUpperCase() === 'PENDING' || r.status === 0 || String(r.status).toUpperCase() === 'APPROVED' || r.status === 1));
    let effective = typeof slot.status === 'string' ? slot.status.toUpperCase() : slot.status;
    if (reservation) {
       if (String(reservation.status).toUpperCase() === 'PENDING' || reservation.status === 0) effective = 'PENDING';
       if (String(reservation.status).toUpperCase() === 'APPROVED' || reservation.status === 1) effective = 'RESERVED';
    } else {
       if (effective === 0) effective = 'AVAILABLE';
       if (effective === 1) effective = 'RESERVED';
       if (effective === 2) effective = 'UNAVAILABLE';
       if (effective === 3) effective = 'PENDING';
    }
    return { ...slot, effectiveStatus: effective };
  }).filter(slot => {
    const status = slot.effectiveStatus;
    if (activeTab === 'AVAILABLE') return status === 'AVAILABLE';
    if (activeTab === 'UNAVAILABLE') return status === 'UNAVAILABLE';
    if (activeTab === 'RESERVED') return status === 'RESERVED';
    if (activeTab === 'PENDING') return status === 'PENDING';
    return true; // ALL
  });

  // Reset pagination when tab changes
  useEffect(() => {
    setCurrentPage(1);
  }, [activeTab]);

  // Pagination Calculations
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredSlots.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredSlots.length / itemsPerPage);



  return (
    <div className="slots-container fade-in">
      {/* Header */}
      <div className="slots-header">
        <div className="slots-title">
          <h1>Slot Management</h1>
          <p>Manage time slots for station operations, reservations and maintenance.</p>
        </div>
        <div className="slots-breadcrumbs">
          <span>Stations</span>
          <span className="separator">›</span>
          <span>{selectedStation?.stationName || 'Loading...'}</span>
          <span className="separator">›</span>
          <span className="current">Slot Management</span>
        </div>
      </div>



      {/* Main Content Area */}
      <div className="table-wrapper">
        <div className="table-header">
          <div className="table-tabs">
            <button className={`tab-btn ${activeTab === 'ALL' ? 'active' : ''}`} onClick={() => setActiveTab('ALL')}>
              All Slots
            </button>
            <button className={`tab-btn ${activeTab === 'AVAILABLE' ? 'active' : ''}`} onClick={() => setActiveTab('AVAILABLE')}>
              Available
            </button>
            <button className={`tab-btn ${activeTab === 'UNAVAILABLE' ? 'active' : ''}`} onClick={() => setActiveTab('UNAVAILABLE')}>
              Unavailable
            </button>
            <button className={`tab-btn ${activeTab === 'PENDING' ? 'active' : ''}`} onClick={() => setActiveTab('PENDING')}>
              Pending
            </button>
            <button className={`tab-btn ${activeTab === 'RESERVED' ? 'active' : ''}`} onClick={() => setActiveTab('RESERVED')}>
              Reserved
            </button>
          </div>
          <div className="table-actions-right" style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <FiMapPin style={{ position: 'absolute', left: '16px', color: '#64748b', fontSize: '18px' }} />
              <select
                value={selectedStation?.stationId || 'all'}
                onChange={handleStationChange}
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
                  width: '260px'
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
            {user?.role === 'GRID_OPERATOR' && (
              <button className="btn-add" onClick={handleAddClick} disabled={!selectedStation || selectedStation.stationId === 'all'} style={{ height: '42px', display: 'flex', alignItems: 'center', gap: '8px', padding: '0 16px', whiteSpace: 'nowrap', borderRadius: '8px' }}>
                <FiPlus /> Add Slot
              </button>
            )}
          </div>
        </div>

        <div className="table-scroll-container">
          <table className="slots-table">
            <thead>
              <tr>
                <th>Slot Name</th>
                <th>Start Date & Time</th>
                <th>End Date & Time</th>
                <th style={{ textAlign: 'center' }}>Status</th>
                <th style={{ textAlign: 'center' }}>Reserved By</th>
                <th className="actions-column" style={{ textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="6" style={{ textAlign: 'center', padding: '40px' }}>Loading slots...</td></tr>
              ) : currentItems.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', color: '#64748b', padding: '40px' }}>
                    No slots found for this category.
                  </td>
                </tr>
              ) : (
                currentItems.map(slot => {
                  const st = slot.effectiveStatus;
                  return (
                    <tr key={slot.slotId}>
                      <td className="font-semibold">{slot.slotName || 'Unknown'}</td>
                      <td>{formatSlotTime(slot.startDateTime)}</td>
                      <td>{formatSlotTime(slot.endDateTime)}</td>
                      <td style={{ textAlign: 'center' }}>
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
                            backgroundColor: (st === 'AVAILABLE') ? '#dcfce7' : (st === 'RESERVED') ? '#fef3c7' : (st === 'PENDING') ? '#fef08a' : '#fee2e2',
                            color: (st === 'AVAILABLE') ? '#166534' : (st === 'RESERVED') ? '#b45309' : (st === 'PENDING') ? '#854d0e' : '#991b1b'
                          }}
                        >
                          {getStatusText(st)}
                        </span>
                      </td>
                      <td className="text-secondary" style={{ textAlign: 'center' }}>
                        {getReservedByText(slot)}
                      </td>
                      <td className="actions-cell" style={{ textAlign: 'center', verticalAlign: 'middle', position: 'relative', zIndex: activeDropdown === slot.slotId ? 50 : 1 }}>
                        <div style={{ display: 'inline-flex', gap: '8px', alignItems: 'center', justifyContent: 'center' }}>
                          <button className="review-btn" onClick={() => setSelectedSlot(slot)}>
                            Review
                          </button>

                          {user?.role === 'GRID_OPERATOR' && (
                            <>
                              <div style={{ display: 'flex', gap: '8px' }}>
                                {(st === 'AVAILABLE') && (
                                  <button className="deactivate-btn" onClick={() => requestStatusChange(slot, 2)}>
                                    Deactivate
                                  </button>
                                )}

                                {(st === 'UNAVAILABLE') && (
                                  <button className="activate-btn" onClick={() => requestStatusChange(slot, 0)}>
                                    Activate
                                  </button>
                                )}

                              </div>
                            </>
                          )}
                        </div>
                        {user?.role === 'GRID_OPERATOR' && (
                          <div className="dropdown-container" style={{ position: 'absolute', right: '16px', top: '50%', transform: 'translateY(-50%)' }}>
                                <button
                                  className="pill-btn"
                                  style={{ padding: '6px 8px', backgroundColor: 'transparent', border: 'none', color: '#64748b' }}
                                  onClick={(e) => toggleDropdown(slot.slotId, e)}
                                >
                                  <FiMoreVertical size={18} />
                                </button>
                                {activeDropdown === slot.slotId && (
                                  <div className="dropdown-menu fade-in" style={{ position: 'absolute', right: 0, top: '100%', zIndex: 10, minWidth: '150px', backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
                                    <button className="dropdown-item" style={{ width: '100%', textAlign: 'left', padding: '10px 16px', background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.875rem', color: '#334155' }} onClick={() => { handleEditSlot(slot); setActiveDropdown(null); }}>
                                      <FiEdit2 /> Edit Slot
                                    </button>
                                    <button className="dropdown-item text-danger" style={{ width: '100%', textAlign: 'left', padding: '10px 16px', background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.875rem', color: '#ef4444' }} onClick={() => { requestDeleteSlot(slot); setActiveDropdown(null); }}>
                                      <FiTrash2 /> Delete Slot
                                    </button>
                                  </div>
                                )}
                              </div>
                            )}
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>

        <div className="table-footer">
          <div className="footer-info">
            Showing {filteredSlots.length === 0 ? 0 : indexOfFirstItem + 1} to {Math.min(indexOfLastItem, filteredSlots.length)} of {filteredSlots.length} slots
          </div>
          <div className="footer-controls">
            <div className="pagination">
              <button className="page-btn" disabled={currentPage === 1} onClick={() => setCurrentPage(p => Math.max(1, p - 1))}>‹</button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
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
              <select value={itemsPerPage} onChange={(e) => { setItemsPerPage(Number(e.target.value)); setCurrentPage(1); }}>
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* --- Modals --- */}

      {/* View Slot Modal (Premium Design) */}
      {selectedSlot && (
        <div className="premium-modal-overlay">
          <div className="premium-modal-content fade-in">
            <div className="premium-modal-header">
              <div className="premium-modal-icon-container">
                <FiCalendar />
              </div>
              <div className="premium-modal-title-group">
                <h2>Slot Details</h2>
                <p>View detailed information about this time slot.</p>
              </div>
              <button className="premium-modal-close-btn" onClick={() => setSelectedSlot(null)}>
                <FiX />
              </button>
            </div>

            <div className="premium-modal-body">
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiGrid /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Slot Name</span>
                  <span className="premium-info-value">{selectedSlot.slotName || 'Unknown'}</span>
                </div>
              </div>

              <div className="premium-info-card">
                <div className="premium-info-icon"><FiMapPin /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Station</span>
                  <span className="premium-info-value">{getStationName(selectedSlot.stationId)}</span>
                </div>
              </div>

              <div className="premium-info-card">
                <div className="premium-info-icon"><FiCalendar /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Start Date & Time</span>
                  <span className="premium-info-value">{formatSlotTime(selectedSlot.startDateTime)}</span>
                </div>
              </div>

              <div className="premium-info-card">
                <div className="premium-info-icon"><FiCalendar /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">End Date & Time</span>
                  <span className="premium-info-value">{formatSlotTime(selectedSlot.endDateTime)}</span>
                </div>
              </div>

              <div className="premium-info-card">
                <div className="premium-info-icon"><FiClock /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Duration</span>
                  <span className="premium-info-value">{Math.round((new Date(selectedSlot.endDateTime) - new Date(selectedSlot.startDateTime)) / (1000 * 60 * 60))} hour(s)</span>
                </div>
              </div>

              <div className="premium-info-card">
                <div className="premium-info-icon"><FiZap /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Capacity</span>
                  <span className="premium-info-value">{selectedSlot.capacity || 5.0} kWh</span>
                </div>
              </div>

              <div className="premium-info-card">
                <div className="premium-info-icon"><FiActivity /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Status</span>
                  <div className="premium-info-value">
                    <span className={`status-badge-btn static-badge status-${selectedSlot.effectiveStatus === 'AVAILABLE' ? 'available' : selectedSlot.effectiveStatus === 'RESERVED' ? 'reserved' : selectedSlot.effectiveStatus === 'PENDING' ? 'pending' : 'unavailable'}`} style={{ display: 'inline-flex', padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: '600', backgroundColor: (selectedSlot.effectiveStatus === 'AVAILABLE') ? '#dcfce7' : (selectedSlot.effectiveStatus === 'RESERVED') ? '#fef3c7' : (selectedSlot.effectiveStatus === 'PENDING') ? '#fef08a' : '#fee2e2', color: (selectedSlot.effectiveStatus === 'AVAILABLE') ? '#166534' : (selectedSlot.effectiveStatus === 'RESERVED') ? '#b45309' : (selectedSlot.effectiveStatus === 'PENDING') ? '#854d0e' : '#991b1b' }}>
                      <span className="status-dot" style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'currentColor', marginRight: '6px' }}></span>
                      {getStatusText(selectedSlot.effectiveStatus)}
                    </span>
                  </div>
                </div>
              </div>

              <div className="premium-info-card">
                <div className="premium-info-icon"><FiUser /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Reserved By</span>
                  <span className="premium-info-value">{getReservedByText(selectedSlot)}</span>
                </div>
              </div>

              <div className="premium-info-card">
                <div className="premium-info-icon"><FiFileText /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Notes</span>
                  <span className="premium-info-value">{selectedSlot.notes || 'No notes provided.'}</span>
                </div>
              </div>
            </div>

            <div className="premium-modal-footer">
              <button className="btn-premium-close" onClick={() => setSelectedSlot(null)}>Close</button>
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
              {(() => {
                const oldSt = typeof statusConfirm.slot.status === 'string' ? statusConfirm.slot.status.toUpperCase() : statusConfirm.slot.status;
                const newSt = typeof statusConfirm.newStatus === 'string' ? statusConfirm.newStatus.toUpperCase() : statusConfirm.newStatus;
                return (
                  <>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
                      <div className="detail-icon" style={{ backgroundColor: '#f1f5f9', color: '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', width: '48px', height: '48px', borderRadius: '8px' }}>
                        <FiGrid style={{ fontSize: '1.2rem' }} />
                      </div>
                      <div>
                        <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#0f172a' }}>{statusConfirm.slot.slotName || 'Unknown Slot'}</h3>
                        <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>{formatSlotTime(statusConfirm.slot.startDateTime)}</p>
                      </div>
                    </div>
                    <div style={{ backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                      <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>
                        Are you sure you want to change the status of this slot from <strong>{getStatusText(statusConfirm.slot.status)}</strong> to <strong>{getStatusText(statusConfirm.newStatus)}</strong>?
                      </p>
                    </div>
                  </>
                );
              })()}
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setStatusConfirm(null)}>Cancel</button>
              <button className="btn-modal-confirm" onClick={confirmStatusChange}>Confirm Change</button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirm && (
        <div className="user-modal-overlay">
          <div className="user-modal-content status-confirm-modal fade-in" style={{ borderTop: '4px solid #ef4444' }}>
            <div className="user-modal-header">
              <h2>Delete Slot</h2>
              <button className="user-modal-close" onClick={() => setDeleteConfirm(null)}>&times;</button>
            </div>
            <div className="user-modal-body">
              <p>Are you sure you want to delete this slot? This action cannot be undone.</p>
              <p style={{ marginTop: '10px', fontSize: '0.9em', color: '#64748b' }}>Slot ID: {deleteConfirm.slotId}</p>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setDeleteConfirm(null)}>Cancel</button>
              <button className="btn-modal-confirm" style={{ background: '#ef4444', color: 'white', border: 'none' }} onClick={confirmDelete}>Delete Slot</button>
            </div>
          </div>
        </div>
      )}

      {/* Info / Error Modal */}
      {infoMsg && (
        <div className="user-modal-overlay">
          <div className="user-modal-content fade-in" style={{ maxWidth: '400px' }}>
            <div className="user-modal-header" style={{ borderBottom: 'none', paddingBottom: 0 }}>
              <h2 style={{ color: infoMsg.type === 'error' ? '#ef4444' : '#10b981', display: 'flex', alignItems: 'center', gap: '8px' }}>
                {infoMsg.type === 'error' ? '⚠️' : '✅'} {infoMsg.title}
              </h2>
              <button className="user-modal-close" onClick={() => setInfoMsg(null)}>&times;</button>
            </div>
            <div className="user-modal-body" style={{ padding: '20px', paddingTop: '10px' }}>
              <p style={{ margin: 0, color: '#475569', lineHeight: '1.5' }}>{infoMsg.message}</p>
            </div>
            <div className="user-modal-footer" style={{ borderTop: 'none', justifyContent: 'flex-end', padding: '20px', paddingTop: '0' }}>
              <button className="btn-modal-confirm" style={{ background: '#f8fafc', color: '#0f172a', border: '1px solid #cbd5e1' }} onClick={() => setInfoMsg(null)}>Close</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default Slots;
