import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { FiCalendar, FiClock, FiChevronLeft, FiSave } from 'react-icons/fi';
import { stationService } from '../../services/stationService';
import { slotService } from '../../services/slotService';
import '../Stations/Stations.css'; // Reusing station form styles

const EditSlot = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [initialLoad, setInitialLoad] = useState(true);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    stationId: '',
    slotName: '',
    date: '',
    startTime: '',
    endTime: '',
    status: 0,
    notes: ''
  });

  useEffect(() => {
    fetchInitialData();
  }, [id]);

  const fetchInitialData = async () => {
    try {
      setInitialLoad(true);
      const stationRes = await stationService.getAllStations();
      const stationList = stationRes.data || [];
      setStations(stationList);
      
      const slotData = location.state?.slot;

      if (!slotData) {
        setError('Slot details not found. Please return to slots.');
        setInitialLoad(false);
        return;
      }

      // Parse date and time from ISO string
      const startDate = new Date(slotData.startDateTime);
      const endDate = new Date(slotData.endDateTime);
      
      const dateStr = startDate.toISOString().split('T')[0];
      const startTimeStr = startDate.toISOString().split('T')[1].substring(0, 5);
      const endTimeStr = endDate.toISOString().split('T')[1].substring(0, 5);

      setFormData({
        stationId: slotData.stationId,
        slotName: slotData.slotName,
        date: dateStr,
        startTime: startTimeStr,
        endTime: endTimeStr,
        status: slotData.status,
        notes: slotData.notes || ''
      });
    } catch (error) {
      console.error("Failed to load data:", error);
      setError('Failed to load slot details. Please return to slots.');
    } finally {
      setInitialLoad(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'notes' && value.length > 500) return;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.stationId || !formData.date || !formData.startTime || !formData.endTime) {
      setError('Please fill out all required fields.');
      return;
    }

    setLoading(true);
    try {
      const startDateTime = new Date(`${formData.date}T${formData.startTime}`).toISOString();
      const endDateTime = new Date(`${formData.date}T${formData.endTime}`).toISOString();

      if (new Date(startDateTime) >= new Date(endDateTime)) {
        setError('End time must be after start time.');
        setLoading(false);
        return;
      }

      const updateDto = {
        startDateTime: startDateTime,
        endDateTime: endDateTime,
        status: isNaN(parseInt(formData.status)) ? formData.status : parseInt(formData.status),
        notes: formData.notes
      };

      await slotService.updateSlot(id, updateDto);
      navigate('/slots');
    } catch (err) {
      console.error('Failed to update slot:', err);
      setError(err.response?.data?.message || 'Failed to update slot. Please check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  if (initialLoad) {
    return <div className="loading-container">Loading slot details...</div>;
  }

  return (
    <div className="add-station-container fade-in">
      <div className="add-station-header">
        <div className="breadcrumbs">
          <span onClick={() => navigate('/stations')} style={{cursor: 'pointer'}}>Stations</span> &gt; 
          <span onClick={() => navigate('/slots')} style={{cursor: 'pointer'}}> Slot Management</span> &gt; 
          <span className="current">Edit Slot</span>
        </div>
        <h1>Edit Slot</h1>
        <p>Modify time slot details and availability.</p>
      </div>

      <div className="add-station-content">
        <form onSubmit={handleSubmit} className="station-form" style={{ maxWidth: '800px' }}>
          {error && <div className="error-banner">{error}</div>}

          {/* Slot Information */}
          <div className="form-section">
            <div className="section-header">
              <FiCalendar className="section-icon-small" />
              <div>
                <h3>Slot Information</h3>
                <p>Update details for this time slot.</p>
              </div>
            </div>
            
            <div className="input-row">
              <div className="input-group">
                <label>Station</label>
                <select 
                  name="stationId" 
                  value={formData.stationId} 
                  disabled
                  className="filter-select"
                  style={{ width: '100%', padding: '12px', backgroundColor: '#f1f5f9', cursor: 'not-allowed' }}
                >
                  <option value="" disabled>Select a Station</option>
                  {stations.map(station => (
                    <option key={station.stationId} value={station.stationId}>
                      {station.stationName}
                    </option>
                  ))}
                </select>
                <span className="input-hint">Station cannot be changed.</span>
              </div>
              <div className="input-group">
                <label>Slot Name</label>
                <input 
                  type="text" 
                  value={formData.slotName || 'Unknown'} 
                  disabled
                  style={{ width: '100%', padding: '12px', backgroundColor: '#f1f5f9', cursor: 'not-allowed', border: '1px solid #e2e8f0', borderRadius: '8px' }}
                />
                <span className="input-hint">Slot name cannot be changed.</span>
              </div>
            </div>
            
            <div className="input-group full">
              <label>Date <span className="req">*</span></label>
              <div className="input-with-icon">
                <input 
                  type="date" 
                  name="date"
                  value={formData.date}
                  onChange={handleChange}
                  required 
                />
                <span className="right-icon"><FiCalendar /></span>
              </div>
            </div>
            
            <div className="input-row">
              <div className="input-group">
                <label>Start Time <span className="req">*</span></label>
                <div className="input-with-icon">
                  <input 
                    type="time" 
                    name="startTime"
                    value={formData.startTime}
                    onChange={handleChange}
                    required 
                  />
                  <span className="right-icon"><FiClock /></span>
                </div>
              </div>
              
              <div className="input-group">
                <label>End Time <span className="req">*</span></label>
                <div className="input-with-icon">
                  <input 
                    type="time" 
                    name="endTime"
                    value={formData.endTime}
                    onChange={handleChange}
                    required 
                  />
                  <span className="right-icon"><FiClock /></span>
                </div>
              </div>
            </div>

            <div className="input-group full">
              <label>Status <span className="req">*</span></label>
              <select 
                name="status"
                value={typeof formData.status === 'string' ? formData.status.toUpperCase() : (formData.status === 0 ? 'AVAILABLE' : formData.status === 1 ? 'RESERVED' : 'UNAVAILABLE')}
                onChange={handleChange}
                className="filter-select"
                style={{ width: '100%', padding: '12px' }}
              >
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="RESERVED" disabled>RESERVED</option>
                <option value="UNAVAILABLE">UNAVAILABLE</option>
              </select>
              <span className="input-hint">Change the availability status. Reserved slots cannot be modified here.</span>
            </div>

            <div className="input-group full">
              <label>Optional Notes</label>
              <textarea 
                name="notes"
                value={formData.notes}
                onChange={handleChange}
                rows="3"
                placeholder="Add any additional information (optional)..."
                maxLength="500"
              ></textarea>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: '#94a3b8', marginTop: '6px' }}>
                <span>Notes can include maintenance info, expected feed, or other operational details.</span>
                <span>{formData.notes.length}/500</span>
              </div>
            </div>
          </div>

          <div className="form-actions" style={{ justifyContent: 'flex-start', gap: '15px' }}>
            <button type="button" className="btn-cancel" onClick={() => navigate('/slots')}>
              <FiChevronLeft /> Back to Slots
            </button>
            <button type="submit" className="btn-save" disabled={loading}>
              <FiSave /> {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditSlot;
