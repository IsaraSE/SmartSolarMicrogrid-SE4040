/*
 * File Name: UserForm.jsx
 * Project: Smart Solar Microgrid Trading System
 * Description: UI Form for adding or editing users.
 */

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { FiArrowLeft, FiUser, FiMail, FiPhone, FiLock, FiEye, FiEyeOff, FiSave, FiFileText, FiShield, FiRefreshCw } from 'react-icons/fi';
import userService from '../../services/userService';
import './UserForm.css';

const UserForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditMode = Boolean(id);

  const [loading, setLoading] = useState(isEditMode);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    role: 'BACKOFFICE',
    accountStatus: 'ACTIVE',
    password: '',
    confirmPassword: '',
    address: '' // Mapped from "Notes"
  });

  useEffect(() => {
    if (isEditMode) {
      fetchUser();
    }
  }, [id]);

  const fetchUser = async () => {
    try {
      setLoading(true);
      const response = await userService.getUserById(id);
      if (response && response.data) {
        const user = response.data;
        setFormData({
          fullName: user.fullName || '',
          email: user.email || '',
          phone: user.phone || '',
          role: user.role || 'BACKOFFICE',
          accountStatus: user.accountStatus || 'ACTIVE',
          address: user.address || '',
          password: '',
          confirmPassword: ''
        });
      }
    } catch (err) {
      setError('Failed to fetch user details.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (error) setError(null);
  };

  const handleReset = () => {
    if (isEditMode) {
      fetchUser();
    } else {
      setFormData({
        fullName: '',
        email: '',
        phone: '',
        role: 'BACKOFFICE',
        accountStatus: 'ACTIVE',
        password: '',
        confirmPassword: '',
        address: ''
      });
      setError(null);
    }
  };

  const validateForm = () => {
    if (!formData.fullName || !formData.phone || !formData.role) {
      setError('Please fill in all required fields.');
      return false;
    }
    
    // Create mode requires email and passwords
    if (!isEditMode) {
      if (!formData.email) {
        setError('Email address is required.');
        return false;
      }
      if (!formData.password || formData.password.length < 6) {
        setError('Password must be at least 6 characters long.');
        return false;
      }
      if (formData.password !== formData.confirmPassword) {
        setError('Passwords do not match.');
        return false;
      }
    }
    
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      setSaving(true);
      if (isEditMode) {
        // Update user
        const updatePayload = {
          fullName: formData.fullName,
          phone: formData.phone,
          address: formData.address || 'N/A', // Notes mapped to address
          accountStatus: formData.accountStatus
        };
        await userService.updateUser(id, updatePayload);
      } else {
        // Create user
        const createPayload = {
          fullName: formData.fullName,
          email: formData.email,
          phone: formData.phone,
          password: formData.password,
          role: formData.role,
          address: formData.address || 'N/A' // Notes mapped to address
        };
        await userService.createUser(createPayload);
      }
      navigate('/users');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save user. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="userform-container">Loading...</div>;
  }

  return (
    <div className="userform-container fade-in">
      <div className="userform-header-row">
        <button className="btn-back" onClick={() => navigate('/users')}>
          <FiArrowLeft /> Back to Users
        </button>
      </div>

      <div className="userform-title-area">
        <h1>{isEditMode ? 'Edit User' : 'Create User'}</h1>
        <p>{isEditMode ? 'Update user details and permissions.' : 'Add a new user to the Smart Solar Microgrid Trading System.'}</p>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <form className="userform-content" onSubmit={handleSubmit}>
        
        {/* Basic Information Card */}
        <div className="form-card">
          <div className="form-card-header">
            <div className="form-card-title">
              <FiUser className="form-card-icon" />
              <h3>Basic Information</h3>
            </div>
            <span className="form-card-subtitle">Provide the user's basic details.</span>
          </div>
          
          <div className="form-grid">
            <div className="form-group">
              <label>Full Name <span className="required">*</span></label>
              <div className="input-wrapper">
                <FiUser className="input-icon" />
                <input 
                  type="text" 
                  name="fullName"
                  placeholder="Enter full name" 
                  value={formData.fullName}
                  onChange={handleInputChange}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>Email Address {isEditMode ? '' : <span className="required">*</span>}</label>
              <div className="input-wrapper">
                <FiMail className="input-icon" />
                <input 
                  type="email" 
                  name="email"
                  placeholder="Enter email address" 
                  value={formData.email}
                  onChange={handleInputChange}
                  disabled={isEditMode}
                  required={!isEditMode}
                />
              </div>
              <span className="input-help">This email will be used for system login and notifications.</span>
            </div>

            <div className="form-group">
              <label>Phone Number <span className="required">*</span></label>
              <div className="input-wrapper">
                <FiPhone className="input-icon" />
                <input 
                  type="text" 
                  name="phone"
                  placeholder="Enter phone number (e.g. +94 77 123 4567)" 
                  value={formData.phone}
                  onChange={handleInputChange}
                  required
                />
              </div>
            </div>
          </div>
        </div>

        {/* Role & Access Card */}
        <div className="form-card">
          <div className="form-card-header">
            <div className="form-card-title">
              <FiShield className="form-card-icon" />
              <h3>Role & Access</h3>
            </div>
            <span className="form-card-subtitle">Define the user's role and account status.</span>
          </div>
          
          <div className="form-grid">
            <div className="form-group">
              <label>Role <span className="required">*</span></label>
              <div className="input-wrapper select-wrapper">
                <FiUser className="input-icon" />
                <select name="role" value={formData.role} onChange={handleInputChange} disabled={isEditMode}>
                  <option value="BACKOFFICE">BACKOFFICE</option>
                  <option value="GRID_OPERATOR">GRID_OPERATOR</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <span className="input-help">Select the appropriate role for this user.</span>
            </div>

            <div className="form-group">
              <label>Account Status <span className="required">*</span></label>
              <div className="input-wrapper select-wrapper">
                <span className={`form-status-dot form-status-${formData.accountStatus?.toLowerCase()}`}></span>
                <select 
                  name="accountStatus" 
                  value={formData.accountStatus} 
                  onChange={handleInputChange}
                  className="status-select"
                >
                  <option value="ACTIVE">Active</option>
                  <option value="PENDING">Pending</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </div>
              <span className="input-help">
                {formData.accountStatus === 'ACTIVE' ? 'Active users can log in to the system.' : 'Users not active cannot log in.'}
              </span>
            </div>
          </div>
        </div>

        {/* Security Card (Only for Create) */}
        {!isEditMode && (
          <div className="form-card">
            <div className="form-card-header">
              <div className="form-card-title">
                <FiLock className="form-card-icon" />
                <h3>Security</h3>
              </div>
              <span className="form-card-subtitle">Set a secure password for the user account.</span>
            </div>
            
            <div className="form-grid">
              <div className="form-group">
                <label>Password <span className="required">*</span></label>
                <div className="input-wrapper">
                  <FiLock className="input-icon" />
                  <input 
                    type={showPassword ? "text" : "password"} 
                    name="password"
                    placeholder="Enter password" 
                    value={formData.password}
                    onChange={handleInputChange}
                  />
                  <button type="button" className="btn-toggle-password" onClick={() => setShowPassword(!showPassword)}>
                    {showPassword ? <FiEyeOff /> : <FiEye />}
                  </button>
                </div>
                <span className="input-help">Minimum 6 characters.</span>
              </div>

              <div className="form-group">
                <label>Confirm Password <span className="required">*</span></label>
                <div className="input-wrapper">
                  <FiLock className="input-icon" />
                  <input 
                    type={showConfirmPassword ? "text" : "password"} 
                    name="confirmPassword"
                    placeholder="Confirm password" 
                    value={formData.confirmPassword}
                    onChange={handleInputChange}
                  />
                  <button type="button" className="btn-toggle-password" onClick={() => setShowConfirmPassword(!showConfirmPassword)}>
                    {showConfirmPassword ? <FiEyeOff /> : <FiEye />}
                  </button>
                </div>
                <span className="input-help">Re-enter the password to confirm.</span>
              </div>
            </div>
          </div>
        )}

        {/* Additional Information Card */}
        <div className="form-card">
          <div className="form-card-header">
            <div className="form-card-title">
              <FiFileText className="form-card-icon" />
              <h3>Additional Information</h3>
            </div>
            <span className="form-card-subtitle">Optional notes about this user.</span>
          </div>
          
          <div className="form-group">
            <label>Notes</label>
            <div className="textarea-wrapper">
              <FiFileText className="textarea-icon" />
              <textarea 
                name="address"
                placeholder="Enter any additional notes (optional)..."
                rows="4"
                value={formData.address}
                onChange={handleInputChange}
                maxLength="500"
              ></textarea>
              <div className="char-count">{formData.address.length}/500</div>
            </div>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="form-actions">
          <div className="actions-left">
            <button type="button" className="btn-form-reset" onClick={handleReset}>
              <FiRefreshCw className="reset-icon" /> Reset
            </button>
          </div>
          <div className="actions-right">
            <button type="button" className="btn-cancel" onClick={() => navigate('/users')} disabled={saving}>
              Cancel
            </button>
            <button type="submit" className="btn-save" disabled={saving}>
              <FiSave /> {saving ? 'Saving...' : 'Save User'}
            </button>
          </div>
        </div>
        
      </form>
    </div>
  );
};

export default UserForm;
