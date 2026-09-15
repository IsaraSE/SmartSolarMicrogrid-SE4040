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
        await fetchSlotsForStation(stationList[0].id);
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
    const station = stations.find(s => s.id === stationId);
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
            <span>{selectedStation?.name || 'Loading...'}</span> &gt; 
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
            <h2>{selectedStation?.name || 'Select a Station'}</h2>
            <span className="status-badge operational">Operational</span>
          </div>
          <div className="station-location">
            <FiMapPin />
            {selectedStation?.location || 'Location unavailable'}
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
            <select className="filter-select" value={selectedStation?.id || ''} onChange={handleStationChange}>
              {stations.map(station => (
                <option key={station.id} value={station.id}>{station.name}</option>
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
          <button className="btn-add-slot">
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
              <>
                {/* Mock Data for visual fidelity if API returns empty */}
                <tr>
                  <td className="slot-id">SLOT-001</td>
                  <td>Apr 22, 2025 08:00 AM</td>
                  <td>Apr 22, 2025 10:00 AM</td>
                  <td><span className="slot-status-badge available">Available</span></td>
                  <td className="reserved-by">-</td>
                  <td className="table-actions">
                    <FiCalendar className="action-icon" />
                    <FiEdit2 className="action-icon" />
                    <FiMoreHorizontal className="action-icon" />
                  </td>
                </tr>
                <tr>
                  <td className="slot-id">SLOT-002</td>
                  <td>Apr 22, 2025 10:00 AM</td>
                  <td>Apr 22, 2025 12:00 PM</td>
                  <td><span className="slot-status-badge reserved">Reserved</span></td>
                  <td className="reserved-by active">SunPower (Pvt) Ltd</td>
                  <td className="table-actions">
                    <FiCalendar className="action-icon" />
                    <FiEdit2 className="action-icon" />
                    <FiMoreHorizontal className="action-icon" />
                  </td>
                </tr>
                <tr>
                  <td className="slot-id">SLOT-003</td>
                  <td>Apr 22, 2025 12:00 PM</td>
                  <td>Apr 22, 2025 02:00 PM</td>
                  <td><span className="slot-status-badge available">Available</span></td>
                  <td className="reserved-by">-</td>
                  <td className="table-actions">
                    <FiCalendar className="action-icon" />
                    <FiEdit2 className="action-icon" />
                    <FiMoreHorizontal className="action-icon" />
                  </td>
                </tr>
                <tr>
                  <td className="slot-id">SLOT-004</td>
                  <td>Apr 22, 2025 02:00 PM</td>
                  <td>Apr 22, 2025 04:00 PM</td>
                  <td><span className="slot-status-badge unavailable">Unavailable</span></td>
                  <td className="reserved-by active">Maintenance</td>
                  <td className="table-actions">
                    <FiCalendar className="action-icon" />
                    <FiEdit2 className="action-icon" />
                    <FiMoreHorizontal className="action-icon" />
                  </td>
                </tr>
                <tr>
                  <td className="slot-id">SLOT-005</td>
                  <td>Apr 22, 2025 04:00 PM</td>
                  <td>Apr 22, 2025 06:00 PM</td>
                  <td><span className="slot-status-badge reserved">Reserved</span></td>
                  <td className="reserved-by active">Nimal Perera</td>
                  <td className="table-actions">
                    <FiCalendar className="action-icon" />
                    <FiEdit2 className="action-icon" />
                    <FiMoreHorizontal className="action-icon" />
                  </td>
                </tr>
              </>
            ) : (
              slots.map(slot => (
                <tr key={slot.id}>
                  <td className="slot-id">{slot.id.substring(0,8).toUpperCase()}</td>
                  <td>{formatSlotTime(slot.startTime)}</td>
                  <td>{formatSlotTime(slot.endTime)}</td>
                  <td>
                    <span className={`slot-status-badge ${slot.status === 0 ? 'available' : slot.status === 1 ? 'reserved' : 'unavailable'}`}>
                      {slot.status === 0 ? 'Available' : slot.status === 1 ? 'Reserved' : 'Unavailable'}
                    </span>
                  </td>
                  <td className={`reserved-by ${slot.reservedBy ? 'active' : ''}`}>
                    {slot.reservedBy || '-'}
                  </td>
                  <td className="table-actions">
                    <FiCalendar className="action-icon" />
                    <FiEdit2 className="action-icon" />
                    <FiMoreHorizontal className="action-icon" />
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        <div className="table-footer">
          <span className="showing-text">Showing 1 - 8 of 48 slots</span>
          <div className="pagination">
            <button className="page-btn"><FiChevronsLeft /></button>
            <button className="page-btn active">1</button>
            <button className="page-btn">2</button>
            <button className="page-btn">3</button>
            <button className="page-btn">4</button>
            <button className="page-btn">5</button>
            <button className="page-btn"><FiChevronsRight /></button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Slots;
