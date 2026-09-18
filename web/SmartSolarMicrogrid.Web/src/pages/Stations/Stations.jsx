import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { PiSunLight } from 'react-icons/pi';
import { 
  FiSearch, 
  FiMapPin, 
  FiPower, 
  FiPauseCircle,
  FiBattery, 
  FiBatteryCharging,
  FiClock,
  FiEdit2, 
  FiEye,
  FiSlash,
  FiPlay,
  FiChevronLeft,
  FiChevronRight,
  FiBarChart2,
  FiHome,
  FiFileText,
  FiHash,
  FiCheckCircle,
  FiXCircle,
  FiActivity,
  FiPlus
} from 'react-icons/fi';
import { 
  PieChart, 
  Pie, 
  Cell, 
  ResponsiveContainer
} from 'recharts';
import { MapContainer, TileLayer, Marker, Popup, ZoomControl } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { useAuth } from '../../context/AuthContext';
import { stationService } from '../../services/stationService';
import './Stations.css';

// Fix for default marker icons in react-leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Custom icons for map markers
const createCustomIcon = (color) => {
  return new L.Icon({
    iconUrl: `https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-${color}.png`,
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });
};

const activeIcon = createCustomIcon('green');
const inactiveIcon = createCustomIcon('red');

// Sparkline Mock SVG component
const Sparkline = ({ color }) => (
  <svg className="sparkline" viewBox="0 0 100 30" preserveAspectRatio="none">
    <path d="M0,25 C20,20 30,10 50,15 C70,20 80,5 100,2" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" />
  </svg>
);

const Stations = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(5);
  const [statusFilter, setStatusFilter] = useState('All Statuses');
  const [sortBy, setSortBy] = useState('Sort by');
  const [selectedStation, setSelectedStation] = useState(null);
  const [statusConfirm, setStatusConfirm] = useState(null);
  const [infoMsg, setInfoMsg] = useState(null);

  useEffect(() => {
    fetchStations();
  }, []);

  const fetchStations = async () => {
    try {
      setLoading(true);
      const response = await stationService.getAllStations();
      setStations(response.data || []);
    } catch (error) {
      console.error("Failed to load stations:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeactivate = (station) => {
    setStatusConfirm({
      station,
      newStatus: 1, // DEACTIVATED
    });
  };

  const handleActivate = (station) => {
    setStatusConfirm({
      station,
      newStatus: 0, // ACTIVE
    });
  };

  const confirmStatusChange = async () => {
    if (!statusConfirm) return;
    try {
      if (statusConfirm.newStatus === 1) {
        await stationService.deactivateStation(statusConfirm.station.stationId);
      } else {
        const updateData = {
          stationName: statusConfirm.station.stationName,
          address: statusConfirm.station.address,
          latitude: statusConfirm.station.latitude,
          longitude: statusConfirm.station.longitude,
          capacity: statusConfirm.station.capacity,
          batterySlotCount: statusConfirm.station.batterySlotCount,
          operatingStartTime: statusConfirm.station.operatingStartTime,
          operatingEndTime: statusConfirm.station.operatingEndTime,
          status: 0 // ACTIVE
        };
        await stationService.updateStation(statusConfirm.station.stationId, updateData);
      }
      fetchStations();
      setStatusConfirm(null);
    } catch (error) {
      const msg = error.response?.data?.message || 'Failed to update station status.';
      setStatusConfirm(null);
      setInfoMsg({ title: 'Error', message: msg, type: 'error' });
    }
  };

  // Calculate statistics
  const totalStations = stations.length;
  const activeStations = stations.filter(s => s.status === 'ACTIVE' || s.status === 0).length;
  const deactivatedStations = stations.filter(s => s.status === 'DEACTIVATED' || s.status === 1).length;
  const totalCapacity = stations.reduce((sum, s) => sum + (s.capacity || 0), 0);

  // Chart data
  const chartData = [
    { name: 'Active', value: activeStations, color: '#10b981' },
    { name: 'Deactivated', value: deactivatedStations, color: '#ef4444' }
  ];

  // Map center (Sri Lanka approximate center)
  const mapCenter = [7.8731, 80.7718];

  const filteredStations = stations.filter(s => {
    const searchMatch = 
      s.stationName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.address?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.stationId?.toString().includes(searchTerm);
      
    let statusMatch = true;
    if (statusFilter === 'ACTIVE') {
      statusMatch = s.status === 'ACTIVE' || s.status === 0;
    } else if (statusFilter === 'DEACTIVATED') {
      statusMatch = s.status === 'DEACTIVATED' || s.status === 1;
    }
    
    return searchMatch && statusMatch;
  }).sort((a, b) => {
    if (sortBy === 'Name A-Z') {
      return (a.stationName || '').localeCompare(b.stationName || '');
    }
    return 0; // Default sort
  });

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentStations = filteredStations.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredStations.length / itemsPerPage);

  return (
    <div className="stations-container">
      <div className="stations-header">
        <div>
          <h1 className="page-title">Station Management</h1>
          <p className="page-subtitle">Manage solar microgrid stations across the network. Add, update and monitor station details.</p>
        </div>
        <div className="stations-breadcrumbs">
          <span>Stations</span>
          <span className="separator">›</span>
          <span className="current">Station Management</span>
        </div>
      </div>


      {/* Map and Chart Section */}
      <div className="visuals-grid">
        {/* Leaflet Map */}
        <div className="map-card">
          <div className="card-header">
            <h3><FiMapPin /> Station Locations</h3>
          </div>
          <div className="map-container">
            <MapContainer 
              center={mapCenter} 
              zoom={7} 
              scrollWheelZoom={false} 
              zoomControl={false}
              style={{ height: '100%', width: '100%', borderRadius: '12px' }}
            >
              <ZoomControl position="bottomright" />
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {stations.map(station => (
                <Marker 
                  key={station.stationId} 
                  position={[station.latitude, station.longitude]}
                  icon={station.status === 'ACTIVE' || station.status === 0 ? activeIcon : inactiveIcon}
                >
                  <Popup>
                    <strong>{station.stationName}</strong>
                  </Popup>
                </Marker>
              ))}
              
              {/* Floating Map Legend */}
              <div className="map-legend">
                <div className="legend-item"><span className="legend-dot green"></span> Active Station</div>
                <div className="legend-item"><span className="legend-dot red"></span> Deactivated Station</div>
              </div>
            </MapContainer>
          </div>
        </div>

        {/* Donut Chart */}
        <div className="chart-card">
          <div className="card-header">
            <h3><FiBarChart2 /> Station Distribution</h3>
            <button className="text-btn">View Full Map</button>
          </div>
          <div className="chart-body">
            <div className="chart-side-by-side">
              <div className="chart-container">
                <ResponsiveContainer width="100%" height={140}>
                  <PieChart>
                    <Pie
                      data={chartData}
                      cx="50%"
                      cy="50%"
                      innerRadius={45}
                      outerRadius={65}
                      paddingAngle={0}
                      stroke="none"
                      dataKey="value"
                    >
                      {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
                <div className="chart-center-text">
                  <span className="big-num">{totalStations}</span>
                  <span className="small-text">Total Stations</span>
                </div>
              </div>
              
              <div className="chart-legend">
                <div className="legend-row">
                  <div className="legend-label"><span className="legend-dot green"></span> Active</div>
                  <div className="legend-stats">
                    <span className="legend-value">{activeStations}</span> 
                    <span className="legend-pct">{totalStations ? Math.round((activeStations/totalStations)*100) : 0}%</span>
                  </div>
                </div>
                <div className="legend-row">
                  <div className="legend-label"><span className="legend-dot red"></span> Deactivated</div>
                  <div className="legend-stats">
                    <span className="legend-value">{deactivatedStations}</span> 
                    <span className="legend-pct">{totalStations ? Math.round((deactivatedStations/totalStations)*100) : 0}%</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="chart-footer-card">
               <div className="footer-icon-leaf">🌿</div>
               <div className="footer-info">
                 <h4>{totalCapacity} MW</h4>
                 <p>Total installed capacity</p>
               </div>
               <div className="footer-trend positive">
                 ↑ 18% <br/><span>vs. last month</span>
               </div>
               <Sparkline color="#10b981" />
            </div>
          </div>
        </div>
      </div>

      {/* Data Table Section */}
      <div className="table-card">
        <div className="table-header-row">
          <div className="card-header" style={{marginBottom: 0}}>
            <h3><FiHome /> All Stations</h3>
          </div>
          {user?.role === 'BACKOFFICE' && (
            <button className="btn-add" onClick={() => navigate('/stations/add')}>
              <FiPlus /> Add Station
            </button>
          )}
        </div>
        
        <div className="table-filters">
          <div className="search-box">
            <FiSearch className="search-icon" />
            <input 
              type="text" 
              placeholder="Search stations by ID, name or address..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          
          <div className="filter-group">
            <select className="filter-select" value={statusFilter} onChange={(e) => {setStatusFilter(e.target.value); setCurrentPage(1);}}>
              <option value="All Statuses">All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="DEACTIVATED">Deactivated</option>
            </select>

            <select className="filter-select sort-select" value={sortBy} onChange={(e) => {setSortBy(e.target.value); setCurrentPage(1);}}>
              <option value="Sort by">Sort by</option>
              <option value="Name A-Z">Name A-Z</option>
            </select>
          </div>
        </div>

        <div className="table-responsive">
          <table className="stations-table">
            <thead>
              <tr>
                <th>Station Name</th>
                <th>Address</th>
                <th>Capacity</th>
                <th style={{textAlign: 'center'}}>Battery Slots</th>
                <th>Operating Hours</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="10" style={{textAlign: 'center', padding: '20px'}}>Loading stations...</td></tr>
              ) : currentStations.length === 0 ? (
                <tr><td colSpan="10" style={{textAlign: 'center', padding: '20px'}}>No stations found.</td></tr>
              ) : (
                currentStations.map(station => (
                  <tr key={station.stationId}>
                    <td className="font-semibold">{station.stationName}</td>
                    <td className="address-col" title={station.address}>{station.address}</td>
                    <td>{station.capacity} MW</td>
                    <td style={{textAlign: 'center'}}>{station.batterySlotCount}</td>
                    <td>{station.operatingStartTime} - {station.operatingEndTime}</td>
                    <td>
                      <span 
                        className={`status-badge-btn static-badge status-${(station.status === 'ACTIVE' || station.status === 0) ? 'active' : 'deactivated'}`} 
                        style={{ 
                          display: 'inline-flex', 
                          padding: '6px 12px', 
                          borderRadius: '20px', 
                          fontSize: '0.85rem', 
                          fontWeight: '500', 
                          border: 'none', 
                          cursor: 'default', 
                          backgroundColor: (station.status === 'ACTIVE' || station.status === 0) ? '#dcfce7' : '#fee2e2', 
                          color: (station.status === 'ACTIVE' || station.status === 0) ? '#166534' : '#991b1b' 
                        }}
                      >
                        {(station.status === 'ACTIVE' || station.status === 0) ? 'Active' : 'Deactivated'}
                      </span>
                    </td>
                    <td className="actions-cell" style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                      <button className="review-btn" onClick={() => setSelectedStation(station)}>Review</button>
                      {user?.role === 'BACKOFFICE' && (
                        <>
                          <button className="edit-btn" onClick={() => navigate(`/stations/edit/${station.stationId}`)}>Edit</button>
                          {(station.status === 'ACTIVE' || station.status === 0) ? (
                            <button className="deactivate-btn" onClick={() => handleDeactivate(station)}>Deactivate</button>
                          ) : (
                            <button className="activate-btn" onClick={() => handleActivate(station)}>Activate</button>
                          )}
                        </>
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
            Showing {filteredStations.length === 0 ? 0 : indexOfFirstItem + 1} to {Math.min(indexOfLastItem, filteredStations.length)} of {filteredStations.length} stations
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

      {/* View Station Modal */}
      {selectedStation && (
        <div className="user-modal-overlay">
          <div className="user-modal-content fade-in">
            <div className="user-modal-header">
              <h2>Station Details</h2>
              <button className="user-modal-close" onClick={() => setSelectedStation(null)}>&times;</button>
            </div>
            <div className="user-modal-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                <div className="detail-card">
                  <div className="detail-icon"><FiHome /></div>
                  <div className="detail-info">
                    <span className="label">Station Name</span>
                    <span className="value">{selectedStation.stationName}</span>
                  </div>
                </div>
                <div className="detail-card">
                  <div className="detail-icon"><FiHash /></div>
                  <div className="detail-info">
                    <span className="label">Station ID</span>
                    <span className="value" style={{ fontSize: '0.8rem' }}>{selectedStation.stationId}</span>
                  </div>
                </div>
                <div className="detail-card" style={{ gridColumn: 'span 2' }}>
                  <div className="detail-icon"><FiMapPin /></div>
                  <div className="detail-info">
                    <span className="label">Address</span>
                    <span className="value">{selectedStation.address}</span>
                  </div>
                </div>
                <div className="detail-card">
                  <div className="detail-icon"><FiPower /></div>
                  <div className="detail-info">
                    <span className="label">Capacity</span>
                    <span className="value">{selectedStation.capacity} MW</span>
                  </div>
                </div>
                <div className="detail-card">
                  <div className="detail-icon"><FiBattery /></div>
                  <div className="detail-info">
                    <span className="label">Battery Slots</span>
                    <span className="value">{selectedStation.batterySlotCount}</span>
                  </div>
                </div>
                <div className="detail-card" style={{ gridColumn: 'span 2' }}>
                  <div className="detail-icon"><FiClock /></div>
                  <div className="detail-info">
                    <span className="label">Operating Hours</span>
                    <span className="value">{selectedStation.operatingStartTime} - {selectedStation.operatingEndTime}</span>
                  </div>
                </div>
                <div className="detail-card">
                  <div className="detail-icon"><FiActivity /></div>
                  <div className="detail-info">
                    <span className="label">Status</span>
                    <span className="value">{selectedStation.status === 'ACTIVE' || selectedStation.status === 0 ? 'Active' : 'Deactivated'}</span>
                  </div>
                </div>
                <div className="detail-card" style={{ gridColumn: 'span 2' }}>
                  <div className="detail-icon"><FiFileText /></div>
                  <div className="detail-info">
                    <span className="label">Description</span>
                    <span className="value">{selectedStation.description || 'None'}</span>
                  </div>
                </div>
              </div>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-close" onClick={() => setSelectedStation(null)}>Close</button>
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
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
                <div className="detail-icon" style={{ backgroundColor: '#f1f5f9', color: '#64748b' }}>
                  <FiHome />
                </div>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#0f172a' }}>{statusConfirm.station.stationName}</h3>
                  <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>Capacity: {statusConfirm.station.capacity} MW</p>
                </div>
              </div>
              <div style={{ backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                  {statusConfirm.newStatus === 0 ? (
                    <FiCheckCircle style={{ color: '#10b981', fontSize: '1.5rem' }} />
                  ) : (
                    <FiXCircle style={{ color: '#ef4444', fontSize: '1.5rem' }} />
                  )}
                  <span style={{ fontWeight: '600', color: '#1e293b' }}>
                    {statusConfirm.newStatus === 0 ? 'Activate Station' : 'Deactivate Station'}
                  </span>
                </div>
                <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>
                  Are you sure you want to change the status of this station to <strong>{statusConfirm.newStatus === 0 ? 'Active' : 'Deactivated'}</strong>?
                </p>
              </div>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setStatusConfirm(null)}>Cancel</button>
              <button 
                className="btn-modal-confirm" 
                style={{ backgroundColor: statusConfirm.newStatus === 1 ? '#ef4444' : '#10b981' }}
                onClick={confirmStatusChange}
              >
                Confirm Change
              </button>
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

export default Stations;
