import React, { useState, useEffect } from 'react';
import { 
  FiUser, FiMail, FiPhone, FiShield,
  FiLock, FiEye, FiEyeOff, FiLogOut,
  FiMapPin, FiActivity, FiCheckCircle, FiX, FiAlertCircle
} from 'react-icons/fi';
import './Settings.css';
import { useAuth } from '../../context/AuthContext';
import userService from '../../services/userService';
import authService from '../../services/authService';

const Settings = () => {
  const { user, logout, updateUser } = useAuth();

  const [feedbackModal, setFeedbackModal] = useState({ isOpen: false, type: '', message: '' });
  const [logoutModal, setLogoutModal] = useState(false);

  const [profileData, setProfileData] = useState({
    fullName: '',
    email: '',
    phone: '+94 71 234 5678',
    role: '',
    address: '123 Solar Way, Colombo',
    status: 'ACTIVE'
  });
  
  const [initialProfileData, setInitialProfileData] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await authService.getProfile();
        if (response.success && response.data) {
          const userDto = response.data;
          const data = {
            fullName: userDto.fullName || '',
            email: userDto.email || '',
            phone: userDto.phone || '',
            role: userDto.role === 1 || userDto.role === 'GRID_OPERATOR' ? 'GRID_OPERATOR' : 'BackOffice',
            address: userDto.address || '',
            status: userDto.accountStatus === 0 || userDto.accountStatus === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE'
          };
          setProfileData(data);
          setInitialProfileData(data);
        }
      } catch (error) {
        console.error("Failed to fetch profile:", error);
        // Fallback to local storage user
        if (user) {
          const data = {
            fullName: user.fullName || '',
            email: user.email || '',
            phone: '',
            role: user.role || '',
            address: '',
            status: 'ACTIVE'
          };
          setProfileData(data);
          setInitialProfileData(data);
        }
      }
    };

    fetchProfile();
  }, [user]);

  const [securityData, setSecurityData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  // Handlers
  const handleProfileChange = (e) => {
    setProfileData({ ...profileData, [e.target.name]: e.target.value });
  };

  const handleSecurityChange = (e) => {
    setSecurityData({ ...securityData, [e.target.name]: e.target.value });
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSavingProfile(true);

    if (initialProfileData && JSON.stringify(profileData) === JSON.stringify(initialProfileData)) {
      setSavingProfile(false);
      setFeedbackModal({ isOpen: true, type: 'info', message: 'No changes were made to your profile.' });
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(profileData.email)) {
      setSavingProfile(false);
      setFeedbackModal({ isOpen: true, type: 'error', message: 'Please enter a valid email address.' });
      return;
    }

    try {
      const response = await authService.updateProfile({
        fullName: profileData.fullName,
        email: profileData.email,
        phone: profileData.phone,
        address: profileData.address,
      });

      setSavingProfile(false);
      
      if (response.success) {
        // Update local persistence
        if (updateUser) {
          updateUser({
            fullName: profileData.fullName,
            email: profileData.email,
            phone: profileData.phone,
            address: profileData.address,
          });
        }
        setInitialProfileData(profileData);
        setFeedbackModal({ isOpen: true, type: 'success', message: 'Profile updated successfully!' });
      }
    } catch (error) {
      setSavingProfile(false);
      const errorMsg = error.response?.data?.message || 'Failed to update profile. Email or Phone may already be in use.';
      setFeedbackModal({ isOpen: true, type: 'error', message: errorMsg });
    }
  };

  const handleUpdatePassword = async (e) => {
    e.preventDefault();

    if (securityData.newPassword !== securityData.confirmPassword) {
      setFeedbackModal({ isOpen: true, type: 'error', message: 'New passwords do not match.' });
      return;
    }

    const newPwd = securityData.newPassword;
    if (newPwd.length < 8) {
      setFeedbackModal({ isOpen: true, type: 'error', message: 'New password must be at least 8 characters long.' });
      return;
    }

    const hasLetter = /[a-zA-Z]/.test(newPwd);
    const hasNumber = /[0-9]/.test(newPwd);
    if (!hasLetter || !hasNumber) {
      setFeedbackModal({ isOpen: true, type: 'error', message: 'New password must contain both letters and numbers.' });
      return;
    }

    setSavingPassword(true);

    try {
      const response = await authService.changePassword({
        currentPassword: securityData.currentPassword,
        newPassword: securityData.newPassword
      });

      setSavingPassword(false);

      if (response.success) {
        setFeedbackModal({ isOpen: true, type: 'success', message: 'Password updated successfully!' });
        setSecurityData({ currentPassword: '', newPassword: '', confirmPassword: '' });
      }
    } catch (error) {
      setSavingPassword(false);
      const errorMsg = error.response?.data?.message || 'Current password is incorrect or update failed.';
      setFeedbackModal({ isOpen: true, type: 'error', message: errorMsg });
    }
  };

  const handleLogoutClick = () => {
    setLogoutModal(true);
  };

  const confirmLogout = () => {
    setLogoutModal(false);
    logout();
  };

  return (
    <div className="settings-container fade-in">
      
      {/* Header */}
      <div className="settings-header">
        <div className="header-left">
          <h1>Settings</h1>
          <p>Manage your account settings.</p>
        </div>
        <div className="breadcrumbs">
          <span>Settings</span> &gt; 
          <span>Settings Management</span>
        </div>
      </div>

      {/* Grid Layout */}
      <div className="settings-grid">
        
        {/* Profile Settings Card */}
        <div className="settings-card">
          <div className="card-header">
            <div className="card-icon"><FiUser /></div>
            <div className="card-text-content">
              <h2>Profile Settings</h2>
              <p>Update your personal information.</p>
            </div>
          </div>
          <form className="card-body" onSubmit={handleSaveProfile}>
            <div className="form-group">
              <label>Full Name</label>
              <div className="input-with-icon">
                <FiUser className="input-icon" />
                <input 
                  type="text" 
                  name="fullName"
                  value={profileData.fullName} 
                  onChange={handleProfileChange}
                  required
                />
              </div>
            </div>
            
            <div className="form-group">
              <label>Email Address</label>
              <div className="input-with-icon">
                <FiMail className="input-icon" />
                <input 
                  type="email" 
                  name="email"
                  value={profileData.email} 
                  onChange={handleProfileChange}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>Phone Number</label>
              <div className="input-with-icon">
                <FiPhone className="input-icon" />
                <input 
                  type="text" 
                  name="phone"
                  value={profileData.phone} 
                  onChange={handleProfileChange}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>Address</label>
              <div className="input-with-icon">
                <FiMapPin className="input-icon" />
                <input 
                  type="text" 
                  name="address"
                  value={profileData.address} 
                  onChange={handleProfileChange}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>System Role</label>
              <div className="input-with-icon">
                <FiShield className="input-icon" />
                <input 
                  type="text" 
                  name="role"
                  value={profileData.role || ''} 
                  disabled
                  style={{ backgroundColor: '#f8fafc', color: '#64748b' }}
                />
              </div>
            </div>

            <div className="form-group">
              <label>Account Status</label>
              <div className="input-with-icon">
                <FiActivity className="input-icon" />
                <input 
                  type="text" 
                  name="status"
                  value={profileData.status || ''} 
                  disabled
                  style={{ backgroundColor: '#f8fafc', color: '#10b981', fontWeight: '500' }}
                />
              </div>
            </div>

            <button type="submit" className="btn-save-changes" disabled={savingProfile}>
              {savingProfile ? 'Saving...' : 'Save Changes'}
            </button>
          </form>
        </div>

        {/* Security Settings Card */}
        <div className="settings-card">
          <div className="card-header">
            <div className="card-icon security"><FiLock /></div>
            <div className="card-text-content">
              <h2>Security Settings</h2>
              <p>Update your password to keep your account secure.</p>
            </div>
          </div>
          <form className="card-body" onSubmit={handleUpdatePassword}>
            <div className="form-group">
              <label>Current Password</label>
              <div className="input-with-icon">
                <FiLock className="input-icon" />
                <input 
                  type={showCurrentPassword ? "text" : "password"} 
                  name="currentPassword"
                  placeholder="Enter current password"
                  value={securityData.currentPassword}
                  onChange={handleSecurityChange}
                  required
                />
                <button type="button" className="icon-btn right" onClick={() => setShowCurrentPassword(!showCurrentPassword)}>
                  {showCurrentPassword ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label>New Password</label>
              <div className="input-with-icon">
                <FiLock className="input-icon" />
                <input 
                  type={showNewPassword ? "text" : "password"} 
                  name="newPassword"
                  placeholder="Enter new password"
                  value={securityData.newPassword}
                  onChange={handleSecurityChange}
                  required
                />
                <button type="button" className="icon-btn right" onClick={() => setShowNewPassword(!showNewPassword)}>
                  {showNewPassword ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label>Confirm Password</label>
              <div className="input-with-icon">
                <FiLock className="input-icon" />
                <input 
                  type={showConfirmPassword ? "text" : "password"} 
                  name="confirmPassword"
                  placeholder="Confirm new password"
                  value={securityData.confirmPassword}
                  onChange={handleSecurityChange}
                  required
                />
                <button type="button" className="icon-btn right" onClick={() => setShowConfirmPassword(!showConfirmPassword)}>
                  {showConfirmPassword ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn-save-changes" disabled={savingPassword} style={{ marginTop: 'auto' }}>
              {savingPassword ? 'Updating...' : 'Update Password'}
            </button>
          </form>
        </div>

      </div>
      
      <div className="settings-footer">
        <button type="button" className="btn-logout" onClick={handleLogoutClick}>
          <FiLogOut /> Log Out
        </button>
      </div>

      {/* Feedback Modal */}
      {feedbackModal.isOpen && (
        <div className="center-modal-overlay fade-in">
          <div className="center-modal-content fade-in-up">
            <button className="center-modal-close" onClick={() => setFeedbackModal({ isOpen: false, type: '', message: '' })}>
              <FiX />
            </button>
            
            <div className="center-modal-icon-wrapper">
              <div className={`center-modal-icon-bg-1 ${feedbackModal.type}`}></div>
              <div className={`center-modal-icon-bg-2 ${feedbackModal.type}`}></div>
              <div className={`center-modal-icon ${feedbackModal.type}`}>
                {feedbackModal.type === 'error' && <FiAlertCircle />}
                {feedbackModal.type === 'success' && <FiCheckCircle />}
                {feedbackModal.type === 'info' && <FiAlertCircle />}
              </div>
            </div>

            <h2 className="center-modal-title">
              {feedbackModal.type === 'error' && 'Error'}
              {feedbackModal.type === 'success' && 'Success'}
              {feedbackModal.type === 'info' && 'Information'}
            </h2>
            
            <p className="center-modal-desc">{feedbackModal.message}</p>
            
            <button 
              className={`center-modal-btn ${feedbackModal.type}`} 
              onClick={() => setFeedbackModal({ isOpen: false, type: '', message: '' })}
            >
              Okay
            </button>
          </div>
        </div>
      )}

      {/* Logout Confirmation Modal */}
      {logoutModal && (
        <div className="user-modal-overlay">
          <div className="user-modal-content status-confirm-modal fade-in" style={{ maxWidth: '450px' }}>
            <div className="user-modal-header">
              <h2>Confirm Logout</h2>
              <button className="user-modal-close" onClick={() => setLogoutModal(false)}>&times;</button>
            </div>
            <div className="user-modal-body">
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
                <div className="detail-icon" style={{ backgroundColor: '#fef2f2', color: '#ef4444' }}>
                  <FiLogOut />
                </div>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#0f172a' }}>{profileData.fullName}</h3>
                  <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>{profileData.email}</p>
                </div>
              </div>
              <div style={{ backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                  <FiLogOut style={{ color: '#ef4444', fontSize: '1.5rem' }} />
                  <span style={{ fontWeight: '600', color: '#1e293b' }}>
                    Sign Out of System
                  </span>
                </div>
                <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>
                  Are you sure you want to log out of your account? You will need to sign in again to access the dashboard.
                </p>
              </div>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setLogoutModal(false)}>Cancel</button>
              <button 
                className="btn-modal-confirm" 
                style={{ backgroundColor: '#ef4444' }}
                onClick={confirmLogout}
              >
                Yes, Log Out
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default Settings;
