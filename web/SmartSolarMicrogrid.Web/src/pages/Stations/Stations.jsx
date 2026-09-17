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
  FiHome
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
  const navigate = useNavigate();
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
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
    { name: 'Deactivated', value: deactivatedStations, color: '#e2e8f0' }
  ];

  // Map center (Sri Lanka approximate center)
  const mapCenter = [7.8731, 80.7718];

  const filteredStations = stations.filter(s => 
    s.stationName?.toLowerCase().includes(searchTerm.toLowerCase()) || 
    s.stationId?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="stations-container">
      <div className="stations-header">
        <div>
          <h1 className="page-title">Station Management</h1>
          <p className="page-subtitle">Manage solar microgrid stations across the network. Add, update and monitor station details.</p>
        </div>
        <div className="header-meta">
          <div className="date-widget">
            <FiClock className="widget-icon" />
            <div className="widget-content">
              <span className="widget-title">Tue, 22 Apr 2025</span>
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

      {/* Top Stat Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon-wrapper green-light">
              <FiHome className="stat-icon" />
            </div>
            <p className="stat-label">Total Stations</p>
          </div>
          <div className="stat-card-bottom">
            <div className="stat-info">
              <h2 className="stat-value">{totalStations}</h2>
              <p className="stat-trend positive">↑ 27% <span>vs. last month</span></p>
            </div>
            <Sparkline color="#10b981" />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon-wrapper blue-light">
              <FiPower className="stat-icon" />
            </div>
            <p className="stat-label">Active Stations</p>
          </div>
          <div className="stat-card-bottom">
            <div className="stat-info">
              <h2 className="stat-value">{activeStations}</h2>
              <p className="stat-trend positive">↑ 20% <span>vs. last month</span></p>
            </div>
            <Sparkline color="#3b82f6" />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon-wrapper yellow-light">
              <FiPauseCircle className="stat-icon" />
            </div>
            <p className="stat-label">Deactivated Stations</p>
          </div>
          <div className="stat-card-bottom">
            <div className="stat-info">
              <h2 className="stat-value">{deactivatedStations}</h2>
              <p className="stat-trend negative">↓ 33% <span>vs. last month</span></p>
            </div>
            <Sparkline color="#eab308" />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon-wrapper purple-light">
              <FiBatteryCharging className="stat-icon" />
            </div>
            <p className="stat-label">Total Capacity</p>
          </div>
          <div className="stat-card-bottom">
            <div className="stat-info">
              <h2 className="stat-value">{totalCapacity} MW</h2>
              <p className="stat-trend positive">↑ 18% <span>vs. last month</span></p>
            </div>
            <Sparkline color="#a855f7" />
          </div>
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
                  <div className="legend-label"><span className="legend-dot gray"></span> Deactivated</div>
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
          <button className="btn-primary-blue" onClick={() => navigate('/stations/add')}>
            + Add Station
          </button>
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
            <select className="filter-select">
              <option>All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="DEACTIVATED">Deactivated</option>
            </select>
            <select className="filter-select">
              <option>All Regions</option>
              <option>Colombo</option>
              <option>Kandy</option>
            </select>
            <select className="filter-select sort-select">
              <option>Sort by</option>
              <option>Name A-Z</option>
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
                <th>Battery Slots</th>
                <th>Operating Hours</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="10" style={{textAlign: 'center', padding: '20px'}}>Loading stations...</td></tr>
              ) : filteredStations.length === 0 ? (
                <tr><td colSpan="10" style={{textAlign: 'center', padding: '20px'}}>No stations found.</td></tr>
              ) : (
                filteredStations.map(station => (
                  <tr key={station.stationId}>
                    <td className="font-semibold">{station.stationName}</td>
                    <td className="address-col" title={station.address}>{station.address}</td>
                    <td>{station.capacity} MW</td>
                    <td>{station.batterySlotCount}</td>
                    <td>{station.operatingStartTime} - {station.operatingEndTime}</td>
                    <td>
                      <div className={`status-badge-btn status-${(station.status === 'ACTIVE' || station.status === 0) ? 'active' : 'deactivated'}`} style={{ cursor: 'default' }}>
                        <div className="status-badge-content">
                          <span className="status-dot"></span>
                          <span>{(station.status === 'ACTIVE' || station.status === 0) ? 'Active' : 'Deactivated'}</span>
                        </div>
                      </div>
                    </td>
                    <td className="actions-cell" style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                      <button className="pill-btn btn-view" title="View" onClick={() => setSelectedStation(station)}>
                        <FiEye /> View
                      </button>
                      <button className="pill-btn btn-edit" title="Edit" onClick={() => navigate(`/stations/edit/${station.stationId}`)}>
                        <FiEdit2 /> Edit
                      </button>
                      {(station.status === 'ACTIVE' || station.status === 0) ? (
                        <button className="pill-btn btn-deactivate" title="Deactivate" onClick={() => handleDeactivate(station)}>
                          <FiSlash /> Deactivate
                        </button>
                      ) : (
                        <button className="pill-btn btn-activate" title="Activate" onClick={() => handleActivate(station)}>
                          <FiPlay /> Activate
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        
        <div className="pagination">
          <span>Showing 1 - {Math.min(5, filteredStations.length)} of {filteredStations.length} stations</span>
          <div className="page-numbers">
            <button className="action-btn"><FiChevronLeft/></button>
            <div className="page-num active">1</div>
            <div className="page-num">2</div>
            <div className="page-num">3</div>
            <button className="action-btn"><FiChevronRight/></button>
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
              <div className="detail-group">
                <label>Station Name</label>
                <div className="detail-value">{selectedStation.stationName}</div>
              </div>
              <div className="detail-group">
                <label>Description</label>
                <div className="detail-value">{selectedStation.description || 'None'}</div>
              </div>
              <div className="detail-group">
                <label>Station ID</label>
                <div className="detail-value">{selectedStation.stationId}</div>
              </div>
              <div className="detail-group">
                <label>Address</label>
                <div className="detail-value">{selectedStation.address}</div>
              </div>
              <div className="detail-group">
                <label>Latitude</label>
                <div className="detail-value">{selectedStation.latitude}</div>
              </div>
              <div className="detail-group">
                <label>Longitude</label>
                <div className="detail-value">{selectedStation.longitude}</div>
              </div>
              <div className="detail-group">
                <label>Capacity</label>
                <div className="detail-value">{selectedStation.capacity} MW</div>
              </div>
              <div className="detail-group">
                <label>Battery Slots</label>
                <div className="detail-value">{selectedStation.batterySlotCount}</div>
              </div>
              <div className="detail-group">
                <label>Operating Hours</label>
                <div className="detail-value">{selectedStation.operatingStartTime} - {selectedStation.operatingEndTime}</div>
              </div>
              <div className="detail-group">
                <label>Status</label>
                <div className="detail-value">
                  {selectedStation.status === 'ACTIVE' || selectedStation.status === 0 ? 'Active' : 'Deactivated'}
                </div>
              </div>
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
              <p>Are you sure you want to change the status of <strong>{statusConfirm.station.stationName}</strong> from <strong className={`text-${(statusConfirm.station.status === 'ACTIVE' || statusConfirm.station.status === 0) ? 'active' : 'deactivated'}`}>{(statusConfirm.station.status === 'ACTIVE' || statusConfirm.station.status === 0) ? 'Active' : 'Deactivated'}</strong> to <strong className={`text-${statusConfirm.newStatus === 0 ? 'active' : 'deactivated'}`}>{statusConfirm.newStatus === 0 ? 'Active' : 'Deactivated'}</strong>?</p>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setStatusConfirm(null)}>Cancel</button>
              <button className="btn-modal-confirm" onClick={confirmStatusChange}>Confirm Change</button>
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
