import React, { useState, useEffect } from 'react';
import { FiInfo, FiCalendar, FiEdit2, FiX, FiSearch, FiArrowRight, FiRefreshCcw, FiFilter, FiEye, FiTrash2, FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import { BiSortAlt2 } from 'react-icons/bi';
import { reservationService } from '../../services/reservationService';
import { stationService } from '../../services/stationService';
import './Reservations.css';

const Reservations = () => {
  const [reservations, setReservations] = useState([]);
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);

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
          
          <div className="filter-item">
            <label>Status</label>
            <div className="filter-select">
              <select defaultValue="All Statuses">
                <option value="All Statuses">All Statuses</option>
                <option value="Confirmed">Confirmed</option>
                <option value="Pending">Pending</option>
                <option value="Cancelled">Cancelled</option>
              </select>
            </div>
          </div>

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
        <div className="table-header-controls">
          <span className="showing-text">Showing 1 - 10 of 24 reservations</span>
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

        <div className="table-responsive-wrapper">
          <table className="res-table">
            <thead>
              <tr>
                <th><input type="checkbox" /></th>
                <th>
                  <div className="th-content">Reservation ID <BiSortAlt2 className="sort-icon" /></div>
                </th>
                <th>
                  <div className="th-content">Prosumer NIC <BiSortAlt2 className="sort-icon" /></div>
                </th>
                <th>
                  <div className="th-content">Station <BiSortAlt2 className="sort-icon" /></div>
                </th>
                <th>
                  <div className="th-content">Scheduled Start <BiSortAlt2 className="sort-icon" /></div>
                </th>
                <th>
                  <div className="th-content">Scheduled End <BiSortAlt2 className="sort-icon" /></div>
                </th>
                <th>
                  <div className="th-content">Status <BiSortAlt2 className="sort-icon" /></div>
                </th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="8" style={{textAlign: 'center', padding: '24px'}}>Loading reservations...</td></tr>
              ) : reservations.length === 0 ? (
                <>
                  {/* Visual UI mockup matching reference design if empty API */}
                  <tr>
                    <td><input type="checkbox" /></td>
                    <td className="res-id">RES-20250422-001</td>
                    <td className="res-nic">199012345678</td>
                    <td>Kandy Green Station</td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 22, 2025</span>
                        <span className="time-text">11:30 AM</span>
                      </div>
                    </td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 22, 2025</span>
                        <span className="time-text">01:30 PM</span>
                      </div>
                    </td>
                    <td><span className="status-badge confirmed">Confirmed</span></td>
                    <td>
                      <div className="table-actions">
                        <button className="action-btn view"><FiEye /></button>
                        <button className="action-btn edit"><FiEdit2 /></button>
                        <button className="action-btn delete"><FiTrash2 /></button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td><input type="checkbox" /></td>
                    <td className="res-id">RES-20250422-002</td>
                    <td className="res-nic">198765432109</td>
                    <td>Galle Coastal Hub</td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 22, 2025</span>
                        <span className="time-text">02:00 PM</span>
                      </div>
                    </td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 22, 2025</span>
                        <span className="time-text">04:00 PM</span>
                      </div>
                    </td>
                    <td><span className="status-badge confirmed">Confirmed</span></td>
                    <td>
                      <div className="table-actions">
                        <button className="action-btn view"><FiEye /></button>
                        <button className="action-btn edit"><FiEdit2 /></button>
                        <button className="action-btn delete"><FiTrash2 /></button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td><input type="checkbox" /></td>
                    <td className="res-id">RES-20250422-003</td>
                    <td className="res-nic">199283746512</td>
                    <td>Colombo Central Node</td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 22, 2025</span>
                        <span className="time-text">04:30 PM</span>
                      </div>
                    </td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 22, 2025</span>
                        <span className="time-text">06:00 PM</span>
                      </div>
                    </td>
                    <td><span className="status-badge pending">Pending</span></td>
                    <td>
                      <div className="table-actions">
                        <button className="action-btn view"><FiEye /></button>
                        <button className="action-btn edit"><FiEdit2 /></button>
                        <button className="action-btn delete"><FiTrash2 /></button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td><input type="checkbox" /></td>
                    <td className="res-id">RES-20250421-004</td>
                    <td className="res-nic">198912345678</td>
                    <td>Ratnapura Solar Hub</td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 23, 2025</span>
                        <span className="time-text">09:00 AM</span>
                      </div>
                    </td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 23, 2025</span>
                        <span className="time-text">11:00 AM</span>
                      </div>
                    </td>
                    <td><span className="status-badge confirmed">Confirmed</span></td>
                    <td>
                      <div className="table-actions">
                        <button className="action-btn view"><FiEye /></button>
                        <button className="action-btn edit"><FiEdit2 /></button>
                        <button className="action-btn delete"><FiTrash2 /></button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td><input type="checkbox" /></td>
                    <td className="res-id">RES-20250420-006</td>
                    <td className="res-nic">198723456789</td>
                    <td>Kandy Green Station</td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 24, 2025</span>
                        <span className="time-text">10:00 AM</span>
                      </div>
                    </td>
                    <td>
                      <div className="date-time-cell">
                        <span>Apr 24, 2025</span>
                        <span className="time-text">12:00 PM</span>
                      </div>
                    </td>
                    <td><span className="status-badge cancelled">Cancelled</span></td>
                    <td>
                      <div className="table-actions">
                        <button className="action-btn view"><FiEye /></button>
                        <button className="action-btn edit"><FiEdit2 /></button>
                        <button className="action-btn delete"><FiTrash2 /></button>
                      </div>
                    </td>
                  </tr>
                </>
              ) : (
                reservations.map(res => (
                  <tr key={res.reservationId}>
                    <td><input type="checkbox" /></td>
                    <td className="res-id">{res.reservationId?.substring(0,8).toUpperCase() || 'RES-####'}</td>
                    <td className="res-nic">{res.prosumerNic || 'N/A'}</td>
                    <td>{getStationName(res.stationId)}</td>
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
                    <td>{mapStatusToBadge(res.status)}</td>
                    <td>
                      <div className="table-actions">
                        <button className="action-btn view"><FiEye /></button>
                        <button className="action-btn edit"><FiEdit2 /></button>
                        <button className="action-btn delete"><FiTrash2 /></button>
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
    </div>
  );
};

export default Reservations;
