import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { FiCalendar, FiClock, FiChevronLeft, FiPlus } from 'react-icons/fi';
import { stationService } from '../../services/stationService';
import { slotService } from '../../services/slotService';
import '../Stations/Stations.css'; // Reusing station form styles

const AddSlot = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [stations, setStations] = useState([]);
  const [existingSlots, setExistingSlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    stationId: '',
    slotName: '',
    date: '',
    startTime: '',
    endTime: '',
    status: 0, // AVAILABLE
    notes: ''
  });

  useEffect(() => {
    fetchStations();
  }, []);

  useEffect(() => {
    if (formData.stationId) {
      fetchExistingSlots(formData.stationId);
    } else {
      setExistingSlots([]);
    }
  }, [formData.stationId]);

  const fetchExistingSlots = async (stationId) => {
    try {
      const res = await slotService.getSlotsByStationId(stationId);
      setExistingSlots(res.data || []);
    } catch (err) {
      console.error("Failed to fetch existing slots:", err);
    }
  };

  const fetchStations = async () => {
    try {
      const response = await stationService.getAllStations();
      const stationList = response.data || [];
      setStations(stationList);
      
      // If a station was passed in via state from Slots page, pre-select it
      if (location.state?.stationId) {
        setFormData(prev => ({ ...prev, stationId: location.state.stationId }));
      } else if (stationList.length > 0) {
        setFormData(prev => ({ ...prev, stationId: stationList[0].stationId }));
      }
    } catch (error) {
      console.error("Failed to load stations:", error);
      setError('Failed to load stations. Please refresh.');
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'notes' && value.length > 500) return; // Enforce max length
    
    // Auto reset slotName if station changes
    if (name === 'stationId') {
      setFormData({ ...formData, [name]: value, slotName: '' });
      return;
    }
    
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.stationId || !formData.slotName || !formData.date || !formData.startTime || !formData.endTime) {
      setError('Please fill out all required fields.');
      return;
    }

    setLoading(true);
    try {
      // Combine date and time into ISO strings
      const startDateTime = new Date(`${formData.date}T${formData.startTime}`).toISOString();
      const endDateTime = new Date(`${formData.date}T${formData.endTime}`).toISOString();

      if (new Date(startDateTime) >= new Date(endDateTime)) {
        setError('End time must be after start time.');
        setLoading(false);
        return;
      }

      const createDto = {
        stationId: formData.stationId,
        slotName: formData.slotName,
        startDateTime: startDateTime,
        endDateTime: endDateTime,
        notes: formData.notes
      };

      await slotService.createSlot(createDto);
      navigate('/slots', { state: { stationId: formData.stationId } });
    } catch (err) {
      console.error('Failed to create slot:', err);
      setError(err.response?.data?.message || 'Failed to create slot. Please check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="add-station-container fade-in">
      <div className="add-station-header">
        <div className="breadcrumbs">
          <span onClick={() => navigate('/stations')} style={{cursor: 'pointer'}}>Stations</span> &gt; 
          <span onClick={() => navigate('/slots')} style={{cursor: 'pointer'}}> Slot Management</span> &gt; 
          <span className="current">Create Slot</span>
        </div>
        <h1>Create New Slot</h1>
        <p>Create a new time slot for station operations and reservations.</p>
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
                <p>Fill in the details to create a new time slot.</p>
              </div>
            </div>
            
            <div className="input-row">
              <div className="input-group">
                <label>Station <span className="req">*</span></label>
                <select 
                  name="stationId" 
                  value={formData.stationId} 
                  onChange={handleChange}
                  required
                  className="filter-select"
                  style={{ width: '100%', padding: '12px' }}
                >
                  <option value="" disabled>Select a Station</option>
                  {stations.map(station => (
                    <option key={station.stationId} value={station.stationId}>
                      {station.stationName}
                    </option>
                  ))}
                </select>
                <span className="input-hint">Select the station for this slot.</span>
              </div>
              
              <div className="input-group">
                <label>Slot Name <span className="req">*</span></label>
                <select 
                  name="slotName" 
                  value={formData.slotName} 
                  onChange={handleChange}
                  required
                  className="filter-select"
                  style={{ width: '100%', padding: '12px' }}
                  disabled={!formData.stationId}
                >
                  <option value="" disabled>Select Slot</option>
                  {(() => {
                    const selected = stations.find(s => s.stationId === formData.stationId);
                    if (!selected) return null;
                    
                    // Generate Acronym
                    const acronym = selected.stationName.split(' ').map(w => w.charAt(0).toUpperCase()).join('');
                    const capacity = selected.batterySlotCount || 1;
                    const createdNames = existingSlots.map(s => s.slotName);
                    
                    const options = [];
                    for(let i=1; i<=capacity; i++) {
                      const suffix = i.toString().padStart(3, '0');
                      const name = `${acronym} ${suffix}`;
                      const isCreated = createdNames.includes(name);
                      
                      options.push(
                        <option 
                          key={name} 
                          value={name} 
                          disabled={isCreated}
                          style={isCreated ? { color: '#94a3b8' } : {}}
                        >
                          {name} {isCreated ? '(Already Created)' : ''}
                        </option>
                      );
                    }
                    return options;
                  })()}
                </select>
                <span className="input-hint">Select the physical slot name.</span>
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
              <span className="input-hint">Select the date for this time slot.</span>
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
                <span className="input-hint">Select the slot start time.</span>
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
                <span className="input-hint">Select the slot end time.</span>
              </div>
            </div>

            <div className="input-group full">
              <label>Status</label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
                <span className="slot-status-badge available" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', margin: 0 }}>
                   AVAILABLE
                </span>
                <span style={{ color: '#64748b', fontSize: '0.85rem', marginLeft: '10px' }}>
                  (New slots are automatically set to Available)
                </span>
              </div>
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
              <FiPlus /> {loading ? 'Creating...' : 'Create Slot'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddSlot;
