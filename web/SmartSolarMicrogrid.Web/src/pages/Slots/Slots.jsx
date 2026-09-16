import React, { useState, useEffect } from 'react';
import { FiClock, FiCalendar, FiMapPin, FiGrid, FiUsers, FiZap, FiCheckCircle, FiSlash, FiEdit2, FiMoreHorizontal, FiRefreshCcw, FiPlus, FiChevronLeft, FiChevronRight, FiChevronsLeft, FiChevronsRight } from 'react-icons/fi';
import { PiSunLight } from 'react-icons/pi';
import { stationService } from '../../services/stationService';
import { slotService } from '../../services/slotService';
import stationHeroBg from '../../assets/images/solar-hero-bg.jpg';
import './Slots.css';

const Slots = () => {
  const [stations, setStations] = useState([]);
  const [selectedStation, setSelectedStation] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('add');
  const [editingSlot, setEditingSlot] = useState(null);
  const [formData, setFormData] = useState({
    startDateTime: '',
    endDateTime: '',
    status: 0
  });
  const [modalLoading, setModalLoading] = useState(false);

  const today = new Date();
  const formattedToday = today.toLocaleDateString('en-US', { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' });

  useEffect(() => {
    fetchInitialData();
  }, []);

  const fetchInitialData = async () => {
    try {
      setLoading(true);
      const response = await stationService.getAllStations();
      const stationList = response.data || [];
      setStations(stationList);
      
      if (stationList.length > 0) {
        setSelectedStation(stationList[0]);
        await fetchSlotsForStation(stationList[0].stationId);
      }
    } catch (error) {
      console.error("Failed to load initial data:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchSlotsForStation = async (stationId) => {
    try {
      const response = await slotService.getSlotsByStationId(stationId);
      setSlots(response.data || []);
    } catch (error) {
      console.error("Failed to load slots:", error);
    }
  };

  const handleStationChange = (e) => {
    const stationId = e.target.value;
    const station = stations.find(s => s.stationId === stationId);
    setSelectedStation(station);
    if (stationId) {
      fetchSlotsForStation(stationId);
    }
  };

  const formatSlotTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', { month: 'short', day: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', hour12: true });
  };

  const handleAddClick = () => {
    setModalMode('add');
    setFormData({ startDateTime: '', endDateTime: '', status: 0 });
    setShowModal(true);
  };

  const handleEditSlot = (slot) => {
    setModalMode('edit');
    setEditingSlot(slot);
    
    // Format for datetime-local input (YYYY-MM-DDThh:mm)
    const formatForInput = (dateStr) => {
      if (!dateStr) return '';
      const d = new Date(dateStr);
      return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0,16);
    };

    setFormData({
      startDateTime: formatForInput(slot.startDateTime),
      endDateTime: formatForInput(slot.endDateTime),
      status: slot.status
    });
    setShowModal(true);
  };

  const handleDeleteSlot = async (slotId) => {
    if (window.confirm("Are you sure you want to delete this slot?")) {
      try {
        await slotService.deleteSlot(slotId);
        fetchSlotsForStation(selectedStation.stationId);
      } catch (error) {
        console.error("Failed to delete slot:", error);
        alert("Failed to delete slot");
      }
    }
  };

  const handleModalSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.startDateTime || !formData.endDateTime) {
      alert("Please select both a start and end date/time.");
      return;
    }

    setModalLoading(true);
    try {
      if (modalMode === 'add') {
        await slotService.createSlot({
          stationId: selectedStation.stationId,
          startDateTime: new Date(formData.startDateTime).toISOString(),
          endDateTime: new Date(formData.endDateTime).toISOString()
        });
      } else {
        await slotService.updateSlot(editingSlot.slotId, {
          startDateTime: new Date(formData.startDateTime).toISOString(),
          endDateTime: new Date(formData.endDateTime).toISOString(),
          status: parseInt(formData.status)
        });
      }
      setShowModal(false);
      fetchSlotsForStation(selectedStation.stationId);
    } catch (error) {
      console.error(`Failed to ${modalMode} slot:`, error);
      alert(`Failed to ${modalMode} slot`);
    } finally {
      setModalLoading(false);
    }
  };

  // SVG Sparkline component for stats
  const Sparkline = ({ color }) => (
    <svg className="slots-stat-chart" viewBox="0 0 80 40" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M5 30C15 30 20 15 30 20C40 25 45 10 55 15C65 20 70 5 75 5" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );

  return (
    <div className="slots-container fade-in">
      {/* Header */}
      <div className="slots-header">
        <div className="header-left">
          <div className="breadcrumbs">
            <span>Stations</span> &gt; 
            <span>{selectedStation?.stationName || 'Loading...'}</span> &gt; 
            <span>Slot Management</span>
          </div>
          <h1>Slot Management</h1>
          <p>Manage time slots for station operations, reservations and maintenance.</p>
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

      {/* Station Info Card */}
      <div className="station-info-card">
        <img src={stationHeroBg} alt="Station" className="station-hero-image" />
        <div className="station-details">
          <div className="station-title-row">
            <h2>{selectedStation?.stationName || 'Select a Station'}</h2>
            <span className="status-badge operational">Operational</span>
          </div>
          <div className="station-location">
            <FiMapPin />
            {selectedStation?.address || 'Location unavailable'}
          </div>
          <div className="station-stats-row">
            <div className="info-stat">
              <FiGrid className="info-stat-icon capacity" />
              <div className="info-stat-content">
                <span className="info-stat-value">{selectedStation?.capacity || 0} MW</span>
                <span className="info-stat-label">Installed Capacity</span>
              </div>
            </div>
            <div className="info-stat">
              <FiUsers className="info-stat-icon prosumers" />
              <div className="info-stat-content">
                <span className="info-stat-value">48</span>
                <span className="info-stat-label">Registered Prosumers</span>
              </div>
            </div>
            <div className="info-stat">
              <PiSunLight className="info-stat-icon energy" />
              <div className="info-stat-content">
                <span className="info-stat-value">2,340 MWh</span>
                <span className="info-stat-label">Total Energy Traded</span>
              </div>
            </div>
            <div className="info-stat">
              <FiZap className="info-stat-icon hybrid" />
              <div className="info-stat-content">
                <span className="info-stat-value">Grid + Storage</span>
                <span className="info-stat-label">Hybrid Supply</span>
              </div>
            </div>
          </div>
        </div>
        <button className="view-details-btn">
          View Station Details &rarr;
        </button>
      </div>

      {/* Stats Grid */}
      <div className="slots-stats-grid">
        <div className="slots-stat-card">
          <div className="slots-stat-left">
            <div className="slots-stat-icon-wrapper total">
              <FiCalendar />
            </div>
            <div className="slots-stat-info">
              <span className="slots-stat-title">Total Slots</span>
              <span className="slots-stat-value">48</span>
              <span className="slots-stat-sub">for selected period</span>
            </div>
          </div>
        </div>
        <div className="slots-stat-card">
          <div className="slots-stat-left">
            <div className="slots-stat-icon-wrapper available">
              <FiCheckCircle />
            </div>
            <div className="slots-stat-info">
              <span className="slots-stat-title">Available Slots</span>
              <span className="slots-stat-value">26</span>
              <span className="slots-stat-sub">54% of total</span>
            </div>
          </div>
          <Sparkline color="#3b82f6" />
        </div>
        <div className="slots-stat-card">
          <div className="slots-stat-left">
            <div className="slots-stat-icon-wrapper reserved">
              <FiClock />
            </div>
            <div className="slots-stat-info">
              <span className="slots-stat-title">Reserved Slots</span>
              <span className="slots-stat-value">16</span>
              <span className="slots-stat-sub">33% of total</span>
            </div>
          </div>
          <Sparkline color="#eab308" />
        </div>
        <div className="slots-stat-card">
          <div className="slots-stat-left">
            <div className="slots-stat-icon-wrapper upcoming">
              <FiSlash />
            </div>
            <div className="slots-stat-info">
              <span className="slots-stat-title">Upcoming Slots</span>
              <span className="slots-stat-value">6</span>
              <span className="slots-stat-sub">Next 24 hours</span>
            </div>
          </div>
          <Sparkline color="#ef4444" />
        </div>
      </div>

      {/* Filters Bar */}
      <div className="slots-filters-bar">
        <div className="filters-group">
          <div className="filter-item">
            <label>Station</label>
            <select className="filter-select" value={selectedStation?.stationId || ''} onChange={handleStationChange}>
              {stations.map(station => (
                <option key={station.stationId} value={station.stationId}>{station.stationName}</option>
              ))}
            </select>
          </div>
          <div className="filter-item">
            <label>Date Range</label>
            <div className="filter-input">
              <FiCalendar style={{color: '#64748b'}} />
              <span>Apr 21, 2025 - Apr 27, 2025</span>
            </div>
          </div>
          <div className="filter-item">
            <label>Status</label>
            <select className="filter-select">
              <option>All Statuses</option>
              <option>Available</option>
              <option>Reserved</option>
              <option>Unavailable</option>
            </select>
          </div>
        </div>
        <div className="actions-group">
          <button className="btn-reset">
            <FiRefreshCcw /> Reset
          </button>
          <button className="btn-add-slot" onClick={handleAddClick} disabled={!selectedStation}>
            <FiPlus /> Add Slot
          </button>
        </div>
      </div>

      {/* Table Card */}
      <div className="slots-table-card">
        <div className="table-header-row">
          <div className="table-title">
            <FiCalendar className="table-title-icon" />
            Time Slots (Apr 21 - Apr 27, 2025)
          </div>
          <div className="table-nav">
            <button className="nav-btn"><FiChevronLeft /></button>
            <span className="nav-label">This Week</span>
            <button className="nav-btn"><FiChevronRight /></button>
          </div>
        </div>
        
        <table className="slots-table">
          <thead>
            <tr>
              <th>Slot ID</th>
              <th>Start Date & Time</th>
              <th>End Date & Time</th>
              <th>Status</th>
              <th>Reserved By</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="6" style={{textAlign: 'center'}}>Loading slots...</td></tr>
            ) : slots.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{textAlign: 'center', color: '#64748b', padding: '20px'}}>
                    No slots available. Click "Add Slot" to create one.
                  </td>
                </tr>
              ) : (
                slots.map(slot => (
                  <tr key={slot.slotId}>
                    <td className="slot-id">{slot.slotId ? slot.slotId.substring(0,8).toUpperCase() : 'N/A'}</td>
                    <td>{formatSlotTime(slot.startDateTime)}</td>
                    <td>{formatSlotTime(slot.endDateTime)}</td>
                    <td>
                      <span className={`slot-status-badge ${slot.status === 0 ? 'available' : slot.status === 1 ? 'reserved' : 'unavailable'}`}>
                        {slot.status === 0 ? 'Available' : slot.status === 1 ? 'Reserved' : 'Unavailable'}
                      </span>
                    </td>
                    <td className={`reserved-by ${slot.reservedBy ? 'active' : ''}`}>
                      {slot.reservedBy || '-'}
                    </td>
                    <td className="table-actions">
                      <FiEdit2 className="action-icon" onClick={() => handleEditSlot(slot)} title="Edit Slot" />
                      <FiSlash className="action-icon" style={{color: '#ef4444', marginLeft: '12px'}} onClick={() => handleDeleteSlot(slot.slotId)} title="Delete Slot" />
                    </td>
                  </tr>
                ))
              )}
          </tbody>
        </table>

        <div className="table-footer">
          <span className="showing-text">Showing 1 - {slots.length} of {slots.length} slots</span>
          <div className="pagination">
            <button className="page-btn"><FiChevronsLeft /></button>
            <button className="page-btn active">1</button>
            <button className="page-btn"><FiChevronsRight /></button>
          </div>
        </div>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="slots-modal-overlay">
          <div className="slots-modal fade-in">
            <div className="slots-modal-header">
              <h3>{modalMode === 'add' ? 'Add New Slot' : 'Edit Slot'}</h3>
              <button className="slots-modal-close" onClick={() => setShowModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleModalSubmit}>
              <div className="slots-modal-body">
                <div className="slots-form-group">
                  <label>Start Date & Time</label>
                  <input 
                    type="datetime-local" 
                    required 
                    value={formData.startDateTime}
                    onChange={(e) => setFormData({...formData, startDateTime: e.target.value})}
                  />
                </div>
                <div className="slots-form-group">
                  <label>End Date & Time</label>
                  <input 
                    type="datetime-local" 
                    required 
                    value={formData.endDateTime}
                    onChange={(e) => setFormData({...formData, endDateTime: e.target.value})}
                  />
                </div>
                {modalMode === 'edit' && (
                  <div className="slots-form-group">
                    <label>Status</label>
                    <select 
                      value={formData.status}
                      onChange={(e) => setFormData({...formData, status: parseInt(e.target.value)})}
                    >
                      <option value={0}>Available</option>
                      <option value={1}>Reserved</option>
                      <option value={2}>Unavailable</option>
                    </select>
                  </div>
                )}
              </div>
              <div className="slots-modal-footer">
                <button type="button" className="slots-btn-cancel" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="slots-btn-save" disabled={modalLoading}>
                  {modalLoading ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Slots;
