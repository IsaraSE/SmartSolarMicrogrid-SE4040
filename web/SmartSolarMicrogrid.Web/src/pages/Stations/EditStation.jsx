import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { 
  FiArrowLeft, 
  FiFileText, 
  FiMapPin, 
  FiBatteryCharging, 
  FiClock,
  FiTrash2,
  FiSave
} from 'react-icons/fi';
import { MapContainer, TileLayer, Marker, useMapEvents, ZoomControl } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { stationService } from '../../services/stationService';
import './AddStation.css';

// Fix for default marker icons
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const defaultIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

// Component to handle map clicks and update marker
const LocationSelector = ({ position, setPosition }) => {
  useMapEvents({
    click(e) {
      setPosition([e.latlng.lat, e.latlng.lng]);
    },
  });
  return position ? <Marker position={position} icon={defaultIcon} /> : null;
};

const EditStation = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Form State
  const [formData, setFormData] = useState({
    stationName: '',
    status: 'ACTIVE',
    description: '',
    address: '',
    latitude: 6.9200, 
    longitude: 79.8600,
    capacity: '',
    batterySlotCount: '',
    operatingStartTime: '06:00',
    operatingEndTime: '22:00'
  });

  useEffect(() => {
    if (id) {
      loadStationDetails();
    }
  }, [id]);

  const loadStationDetails = async () => {
    setLoading(true);
    try {
      const response = await stationService.getStationById(id);
      const station = response.data;
      if (station) {
        setFormData({
          stationName: station.stationName || '',
          status: station.status === 1 || station.status === 'ACTIVE' ? 'ACTIVE' : 'DEACTIVATED',
          description: '',
          address: station.address || '',
          latitude: station.latitude || 6.9200,
          longitude: station.longitude || 79.8600,
          capacity: station.capacity || '',
          batterySlotCount: station.batterySlotCount || '',
          operatingStartTime: station.operatingStartTime || '06:00',
          operatingEndTime: station.operatingEndTime || '22:00'
        });
      }
    } catch (err) {
      setError('Failed to load station details.');
    } finally {
      setLoading(false);
    }
  };

  const [mapPosition, setMapPosition] = useState([formData.latitude, formData.longitude]);

  // Update map when manual input changes
  useEffect(() => {
    if (formData.latitude && formData.longitude && !isNaN(formData.latitude) && !isNaN(formData.longitude)) {
      setMapPosition([parseFloat(formData.latitude), parseFloat(formData.longitude)]);
    }
  }, [formData.latitude, formData.longitude]);

  // Update form when map is clicked
  useEffect(() => {
    if (mapPosition) {
      setFormData(prev => ({
        ...prev,
        latitude: mapPosition[0],
        longitude: mapPosition[1]
      }));
    }
  }, [mapPosition]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const stationDto = {
        stationName: formData.stationName,
        address: formData.address,
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
        capacity: parseFloat(formData.capacity),
        batterySlotCount: parseInt(formData.batterySlotCount),
        operatingStartTime: formData.operatingStartTime,
        operatingEndTime: formData.operatingEndTime,
        status: formData.status === 'ACTIVE' ? 1 : 2
      };

      await stationService.updateStation(id, stationDto);
      alert('Station updated successfully!');
      navigate('/stations');
    } catch (err) {
      setError('Failed to update station. Please check your inputs.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="add-station-container">
      <div className="header-bar">
        <div>
          <h1 className="page-title">Edit Station</h1>
          <p className="page-subtitle">Update details for this solar charging station.</p>
        </div>
        <button className="btn-outline" onClick={() => navigate('/stations')}>
          <FiArrowLeft /> Back to Stations
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="form-layout">
        <div className="form-left">
          <form id="stationForm" onSubmit={handleSubmit}>
            
            {/* Station Information */}
            <div className="form-section">
              <div className="section-header">
                <FiFileText className="section-icon-small" />
                <div>
                  <h3>Station Information</h3>
                  <p>Basic details about the solar station.</p>
                </div>
              </div>
              
              <div className="input-row">
                <div className="input-group">
                  <label>Station Name <span className="req">*</span></label>
                  <input 
                    type="text" 
                    name="stationName"
                    value={formData.stationName}
                    onChange={handleChange}
                    required 
                  />
                </div>
                <div className="input-group">
                  <label>Status <span className="info-icon">i</span></label>
                  <div className="status-select-wrapper">
                    <span className="status-dot green"></span>
                    <select name="status" value={formData.status} onChange={handleChange}>
                      <option value="Active">Active</option>
                      <option value="Deactivated">Deactivated</option>
                    </select>
                  </div>
                </div>
              </div>

              <div className="input-group">
                <label>Description / Notes</label>
                <textarea 
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  rows="3"
                  maxLength="500"
                ></textarea>
                <div className="char-count">{formData.description.length}/500</div>
              </div>
            </div>

            {/* Location */}
            <div className="form-section">
              <div className="section-header">
                <FiMapPin className="section-icon-small" />
                <div>
                  <h3>Location</h3>
                  <p>Set the physical location of the station.</p>
                </div>
              </div>
              
              <div className="input-group full">
                <label>Address <span className="req">*</span></label>
                <input 
                  type="text" 
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  required 
                />
              </div>
              
              <div className="input-row">
                <div className="input-group">
                  <label>Latitude <span className="req">*</span></label>
                  <div className="input-with-icon">
                    <input 
                      type="number" 
                      step="any"
                      name="latitude"
                      value={formData.latitude}
                      onChange={handleChange}
                      required 
                    />
                    <span className="right-icon">🎯</span>
                  </div>
                </div>
                <div className="input-group">
                  <label>Longitude <span className="req">*</span></label>
                  <div className="input-with-icon">
                    <input 
                      type="number" 
                      step="any"
                      name="longitude"
                      value={formData.longitude}
                      onChange={handleChange}
                      required 
                    />
                    <span className="right-icon">🎯</span>
                  </div>
                </div>
              </div>
              <div className="field-info">
                <span className="info-icon blue">i</span> You can click on the map to set the location or enter the coordinates manually.
              </div>
            </div>

            {/* Capacity & Slots */}
            <div className="form-section">
              <div className="section-header">
                <FiBatteryCharging className="section-icon-small" />
                <div>
                  <h3>Capacity & Slots</h3>
                  <p>Configure the station capacity and available battery slots.</p>
                </div>
              </div>
              
              <div className="input-row">
                <div className="input-group">
                  <label>Station Capacity (kW) <span className="req">*</span></label>
                  <div className="input-with-suffix">
                    <input 
                      type="number" 
                      name="capacity"
                      value={formData.capacity}
                      onChange={handleChange}
                      min="0"
                      required 
                    />
                    <span className="suffix">kW</span>
                  </div>
                </div>
                <div className="input-group">
                  <label>Battery Slot Count <span className="req">*</span></label>
                  <div className="input-with-suffix">
                    <input 
                      type="number" 
                      name="batterySlotCount"
                      value={formData.batterySlotCount}
                      onChange={handleChange}
                      min="0"
                      required 
                    />
                    <span className="suffix">slots</span>
                  </div>
                </div>
              </div>
              <div className="field-info">
                <span className="info-icon blue">i</span> Total available battery slots for charging and trading operations.
              </div>
            </div>

            {/* Operating Schedule */}
            <div className="form-section">
              <div className="section-header">
                <FiClock className="section-icon-small" />
                <div>
                  <h3>Operating Schedule</h3>
                  <p>Set the daily operating hours for this station.</p>
                </div>
              </div>
              
              <div className="input-row align-end">
                <div className="input-group">
                  <label>Operating Start Time <span className="req">*</span></label>
                  <input 
                    type="time" 
                    name="operatingStartTime"
                    value={formData.operatingStartTime}
                    onChange={handleChange}
                    required 
                  />
                </div>
                <div className="input-group">
                  <label>Operating End Time <span className="req">*</span></label>
                  <input 
                    type="time" 
                    name="operatingEndTime"
                    value={formData.operatingEndTime}
                    onChange={handleChange}
                    required 
                  />
                </div>
                <div className="duration-widget">
                  <span className="sun-icon">☀️</span>
                  <div>
                    <div className="dur-label">Operating Duration</div>
                    <div className="dur-val">16 hours per day <span className="info-icon">i</span></div>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Footer Form */}
            <div className="form-footer-inline">
              <button type="button" className="btn-danger-outline">
                <FiTrash2 /> Deactivate Station
              </button>
              <span className="footer-note">Deactivate this station to remove it from active operations.</span>
              
              <div className="action-buttons">
                <button type="button" className="btn-secondary" onClick={() => navigate('/stations')}>Cancel</button>
                <button type="submit" form="stationForm" className="btn-primary" disabled={loading}>
                   {loading ? 'Saving...' : <><FiSave /> Save Station</>}
                </button>
              </div>
            </div>
            
          </form>
        </div>

        {/* Right Side: Previews */}
        <div className="form-right">
          {/* Map Preview */}
          <div className="preview-card">
            <div className="card-header-small">
              <FiMapPin className="header-icon"/>
              <div>
                <h4>Location Preview</h4>
                <p>Verify the station location on the map.</p>
              </div>
            </div>
            <div className="map-preview-container">
              <MapContainer 
                center={mapPosition} 
                zoom={11} 
                scrollWheelZoom={true}
                zoomControl={false}
                style={{ height: '100%', width: '100%' }}
              >
                <ZoomControl position="bottomright" />
                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                <LocationSelector position={mapPosition} setPosition={setMapPosition} />
              </MapContainer>
            </div>
            <div className="location-set-bar">
              <div className="loc-info">
                <div className="loc-icon-wrapper">
                   <FiMapPin className="green" />
                </div>
                <div>
                  <div className="loc-title">Location Set</div>
                  <div className="loc-coords">Latitude: {formData.latitude.toFixed(4)}, Longitude: {formData.longitude.toFixed(4)}</div>
                </div>
              </div>
              <button className="btn-small-outline" onClick={() => {
                if ("geolocation" in navigator) {
                  navigator.geolocation.getCurrentPosition(function(position) {
                    setMapPosition([position.coords.latitude, position.coords.longitude]);
                  });
                }
              }}>
                <span className="icon">🎯</span> Update on Map
              </button>
            </div>
          </div>

          {/* Station Summary */}
          <div className="preview-card sticky">
            <div className="card-header-small">
              <FiFileText className="header-icon"/>
              <div>
                <h4>Station Summary</h4>
                <p>Quick overview of the station configuration.</p>
              </div>
            </div>
            
            <div className="summary-list">
              <div className="summary-row">
                <span className="sum-icon">🏠</span>
                <span className="sum-label">Name</span>
                <span className="sum-val">{formData.stationName || '-'}</span>
              </div>
              <div className="summary-row">
                <span className="sum-icon">📍</span>
                <span className="sum-label">Address</span>
                <span className="sum-val">{formData.address || '-'}</span>
              </div>
              <div className="summary-row">
                <span className="sum-icon">🎯</span>
                <span className="sum-label">Coordinates</span>
                <span className="sum-val">{Number(formData.latitude).toFixed(4)}, {Number(formData.longitude).toFixed(4)}</span>
              </div>
              <div className="summary-row">
                <span className="sum-icon">🔋</span>
                <span className="sum-label">Capacity</span>
                <span className="sum-val">{formData.capacity ? `${formData.capacity} kW` : '-'}</span>
              </div>
              <div className="summary-row">
                <span className="sum-icon">🪫</span>
                <span className="sum-label">Battery Slots</span>
                <span className="sum-val">{formData.batterySlotCount ? `${formData.batterySlotCount} slots` : '-'}</span>
              </div>
              <div className="summary-row">
                <span className="sum-icon">⏱️</span>
                <span className="sum-label">Operating Hours</span>
                <span className="sum-val">06:00 AM - 10:00 PM</span>
              </div>
              <div className="summary-row">
                <span className="sum-icon green-dot-icon">🟢</span>
                <span className="sum-label">Status</span>
                <span className={`status-badge-inline ${formData.status.toLowerCase()}`}>{formData.status}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditStation;
