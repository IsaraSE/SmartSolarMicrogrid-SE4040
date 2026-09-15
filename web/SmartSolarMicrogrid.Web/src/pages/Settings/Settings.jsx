import React, { useState } from 'react';
import { 
  FiUser, FiMail, FiPhone, FiShield, 
  FiLock, FiEye, FiEyeOff, FiInfo,
  FiBell, FiCalendar, FiRadio, FiUsers,
  FiLogOut, FiAlertCircle
} from 'react-icons/fi';
import './Settings.css';
import { useAuth } from '../../context/AuthContext';

const Settings = () => {
  const { logout } = useAuth();

  // Form State
  const [profileData, setProfileData] = useState({
    fullName: 'Tharindu Perera',
    email: 'tharindu@smartsolar.com',
    phone: '+94 71 234 5678',
    role: 'BackOffice'
  });

  const [securityData, setSecurityData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [notifications, setNotifications] = useState({
    reservationAlerts: true,
    stationAlerts: true,
    userApprovalAlerts: true,
    emailNotifications: true
  });

  // Handlers
  const handleProfileChange = (e) => {
    setProfileData({ ...profileData, [e.target.name]: e.target.value });
  };

  const handleSecurityChange = (e) => {
    setSecurityData({ ...securityData, [e.target.name]: e.target.value });
  };

  const toggleNotification = (key) => {
    setNotifications(prev => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <div className="settings-container fade-in">
      
      {/* Header */}
      <div className="settings-header">
        <div className="header-left">
          <h1>Settings</h1>
          <p>Manage your account, security and notification preferences.</p>
        </div>
        <div className="breadcrumbs">
          <span>Home</span> &gt; 
          <span>Settings</span>
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
              <p>View and update your personal information.</p>
            </div>
          </div>
          <div className="card-body">
            <div className="form-group">
              <label>Full Name</label>
              <div className="input-with-icon">
                <FiUser className="input-icon" />
                <input 
                  type="text" 
                  name="fullName"
                  value={profileData.fullName} 
                  onChange={handleProfileChange}
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
                />
              </div>
            </div>

            <div className="form-group">
              <label>Role</label>
              <div className="input-with-icon">
                <FiShield className="input-icon" />
                <select name="role" value={profileData.role} onChange={handleProfileChange}>
                  <option value="BackOffice">BackOffice</option>
                  <option value="Admin">Admin</option>
                  <option value="Prosumer">Prosumer</option>
                </select>
              </div>
            </div>

            <button className="btn-save-changes">Save Changes</button>
          </div>
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
          <div className="card-body">
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
                />
                <button type="button" className="icon-btn right" onClick={() => setShowNewPassword(!showNewPassword)}>
                  {showNewPassword ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label>Confirm New Password</label>
              <div className="input-with-icon">
                <FiLock className="input-icon" />
                <input 
                  type={showConfirmPassword ? "text" : "password"} 
                  name="confirmPassword"
                  placeholder="Confirm new password"
                  value={securityData.confirmPassword}
                  onChange={handleSecurityChange}
                />
                <button type="button" className="icon-btn right" onClick={() => setShowConfirmPassword(!showConfirmPassword)}>
                  {showConfirmPassword ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
            </div>

            <div className="alert-banner info">
              <div className="alert-icon"><FiInfo /></div>
              <p>Your password must be at least 8 characters long and include a combination of letters, numbers and symbols.</p>
            </div>

            <button className="btn-save-changes">Change Password</button>
          </div>
        </div>

        {/* Notification Preferences Card */}
        <div className="settings-card">
          <div className="card-header">
            <div className="card-icon notification"><FiBell /></div>
            <div className="card-text-content">
              <h2>Notification Preferences</h2>
              <p>Choose what notifications you want to receive.</p>
            </div>
          </div>
          <div className="card-body notification-list">
            
            <div className="notification-item">
              <div className="notif-icon"><FiCalendar /></div>
              <div className="notif-content">
                <span className="notif-title">Reservation Alerts</span>
                <span className="notif-desc">Get notified about new and updated reservations.</span>
              </div>
              <div 
                className={`toggle-switch ${notifications.reservationAlerts ? 'active' : ''}`}
                onClick={() => toggleNotification('reservationAlerts')}
              >
                <div className="toggle-knob"></div>
              </div>
            </div>

            <div className="notification-item">
              <div className="notif-icon"><FiRadio /></div>
              <div className="notif-content">
                <span className="notif-title">Station Alerts</span>
                <span className="notif-desc">Get notified about station status changes.</span>
              </div>
              <div 
                className={`toggle-switch ${notifications.stationAlerts ? 'active' : ''}`}
                onClick={() => toggleNotification('stationAlerts')}
              >
                <div className="toggle-knob"></div>
              </div>
            </div>

            <div className="notification-item">
              <div className="notif-icon"><FiUsers /></div>
              <div className="notif-content">
                <span className="notif-title">User Approval Alerts</span>
                <span className="notif-desc">Get notified about new user registrations.</span>
              </div>
              <div 
                className={`toggle-switch ${notifications.userApprovalAlerts ? 'active' : ''}`}
                onClick={() => toggleNotification('userApprovalAlerts')}
              >
                <div className="toggle-knob"></div>
              </div>
            </div>

            <div className="notification-item">
              <div className="notif-icon"><FiMail /></div>
              <div className="notif-content">
                <span className="notif-title">Email Notifications</span>
                <span className="notif-desc">Receive notifications via email.</span>
              </div>
              <div 
                className={`toggle-switch ${notifications.emailNotifications ? 'active' : ''}`}
                onClick={() => toggleNotification('emailNotifications')}
              >
                <div className="toggle-knob"></div>
              </div>
            </div>

          </div>
        </div>

        {/* Account Card */}
        <div className="settings-card account-card">
          <div className="card-header">
            <div className="card-icon account"><FiUser /></div>
            <div className="card-text-content">
              <h2>Account</h2>
              <p>Manage your account session.</p>
            </div>
          </div>
          <div className="card-body">
            <div className="alert-banner danger">
              <div className="alert-icon"><FiAlertCircle /></div>
              <div className="alert-text-block">
                <strong>You will be signed out from the system.</strong>
                <p>Make sure to save any unsaved changes before logging out.</p>
              </div>
            </div>
            
            <button className="btn-logout" onClick={logout}>
              <FiLogOut style={{ transform: 'rotate(180deg)' }} /> Logout
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};

export default Settings;
