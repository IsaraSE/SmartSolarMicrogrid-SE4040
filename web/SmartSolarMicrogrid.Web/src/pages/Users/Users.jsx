/*
 * File Name: Users.jsx
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: User Management page for system users.
 * Date: 2026-09-15
 */

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiSearch, FiRefreshCw, FiPlus, FiEdit2, FiChevronDown, FiUser, FiMail, FiPhone, FiShield, FiActivity, FiMapPin, FiFileText, FiCalendar, FiUserCheck, FiUserX, FiHash, FiMoreVertical, FiX } from 'react-icons/fi';
import userService from '../../services/userService';
import './Users.css';

const Users = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modals state
  const [viewUser, setViewUser] = useState(null);
  const [statusConfirm, setStatusConfirm] = useState(null); // { user, newStatus }
  const [actionMenuOpen, setActionMenuOpen] = useState(null);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('All Roles');
  const [statusFilter, setStatusFilter] = useState('All Status');

  // Pagination (Mocked for UI)
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  useEffect(() => {
    fetchUsers();
    
    const handleClickOutside = () => setActionMenuOpen(null);
    window.addEventListener('click', handleClickOutside);
    return () => window.removeEventListener('click', handleClickOutside);
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await userService.getAllUsers();
      if (response && response.data) {
        setUsers(response.data);
      }
    } catch (err) {
      setError('Failed to fetch users. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setSearchTerm('');
    setRoleFilter('All Roles');
    setStatusFilter('All Status');
    setCurrentPage(1);
  };

  const handleStatusClick = (user) => {
    const newStatus = user.accountStatus === 'ACTIVE' ? 'DEACTIVATED' : 'ACTIVE';
    setStatusConfirm({ user, newStatus });
  };

  const confirmStatusChange = async () => {
    if (!statusConfirm) return;
    try {
      const { user, newStatus } = statusConfirm;
      await userService.updateUser(user.userId, {
        ...user,
        accountStatus: newStatus
      });
      setStatusConfirm(null);
      fetchUsers(); // Refresh the list
    } catch (err) {
      alert("Failed to update status");
    }
  };

  // Helper to get initials
  const getInitials = (name) => {
    if (!name) return 'U';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  // Helper for generating deterministic colors for avatars based on name
  const getAvatarColor = (name) => {
    const colors = ['#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#ec4899', '#0ea5e9'];
    if (!name) return colors[0];
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
  };

  // Filter logic
  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (user.userId && user.userId.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesRole = roleFilter === 'All Roles' || user.role === roleFilter;
    const matchesStatus = statusFilter === 'All Status' || user.accountStatus === statusFilter;

    return matchesSearch && matchesRole && matchesStatus;
  });

  // Pagination logic
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentUsers = filteredUsers.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredUsers.length / itemsPerPage) || 1;

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return (
      <div className="date-cell">
        <span className="date-day">{date.toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' })}</span>
        <span className="date-time">{date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</span>
      </div>
    );
  };

  return (
    <div className="users-container fade-in">
      {/* Header */}
      <div className="users-header">
        <div className="users-title">
          <h1>User Management</h1>
          <p>Manage system users, roles and access permissions.</p>
        </div>
        <div className="users-breadcrumbs">
          <span>Users</span>
          <span className="separator">›</span>
          <span className="current">User Management</span>
        </div>
      </div>

      {/* Toolbar */}
      <div className="users-toolbar">
        <div className="toolbar-left">
          <div className="search-box">
            <FiSearch className="search-icon" />
            <input
              type="text"
              placeholder="Search by name, email, or user ID..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          
          <div className="filter-dropdown">
            <select value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
              <option value="All Roles">All Roles</option>
              <option value="BACKOFFICE">BACKOFFICE</option>
              <option value="GRID_OPERATOR">GRID_OPERATOR</option>
            </select>
          </div>

          <div className="filter-dropdown">
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="All Status">All Status</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="DEACTIVATED">DEACTIVATED</option>
            </select>
          </div>

          <button className="btn-reset" onClick={handleReset}>
            <FiRefreshCw /> Reset
          </button>
        </div>

        <div className="toolbar-right">
          <button className="btn-add" onClick={() => navigate('/users/new')}>
            <FiPlus /> Add User
          </button>
        </div>
      </div>

      {/* Error State */}
      {error && <div className="error-banner">{error}</div>}

      {/* Data Table */}
      <div className="table-card">
        <div className="table-responsive">
          <table className="users-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Role</th>
                <th style={{ textAlign: 'center' }}>Status</th>
                <th className="th-actions" style={{ textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="6" className="loading-cell">Loading users...</td>
                </tr>
              ) : currentUsers.length === 0 ? (
                <tr>
                  <td colSpan="6" className="empty-cell">No users found.</td>
                </tr>
              ) : (
                currentUsers.map((user) => (
                  <tr key={user.userId}>
                    <td>
                      <div className="cell-user">
                        <span className="user-name">
                          {user.fullName ? user.fullName.split(' ').slice(0, 2).join(' ') : 'Unknown'}
                        </span>
                      </div>
                    </td>
                    <td>
                      <div className="cell-email" title={user.email}>{user.email}</div>
                    </td>
                    <td className="cell-phone">{user.phone}</td>
                    <td>
                      <span className={`role-badge role-${user.role?.toLowerCase() || 'unknown'}`}>
                        {user.role}
                      </span>
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <span className={`status-badge-btn static-badge status-${user.accountStatus?.toLowerCase() || 'unknown'}`} style={{ display: 'inline-flex', padding: '6px 12px', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '500', border: 'none', cursor: 'default', backgroundColor: user.accountStatus === 'ACTIVE' ? '#dcfce7' : '#fee2e2', color: user.accountStatus === 'ACTIVE' ? '#166534' : '#991b1b' }}>
                        {user.accountStatus === 'ACTIVE' ? 'Active' : 'Deactivated'}
                      </span>
                    </td>
                    <td className="cell-actions" style={{ display: 'flex', gap: '8px', alignItems: 'center', justifyContent: 'center', position: 'relative', whiteSpace: 'nowrap', minWidth: 'max-content' }}>
                      <button className="review-btn" onClick={() => setViewUser(user)}>Review</button>
                      
                      {user.accountStatus === 'ACTIVE' ? (
                        <button className="deactivate-btn" onClick={() => handleStatusClick(user)}>Deactivate</button>
                      ) : (
                        <button className="activate-btn" onClick={() => handleStatusClick(user)}>Activate</button>
                      )}
                      
                      <div className="dropdown-container" style={{ marginLeft: 'auto' }}>
                        <button 
                          className="review-btn" 
                          style={{ padding: '6px 10px', display: 'flex', alignItems: 'center', justifyContent: 'center' }} 
                          onClick={(e) => {
                            e.stopPropagation();
                            setActionMenuOpen(actionMenuOpen === user.userId ? null : user.userId);
                          }}
                        >
                          <FiMoreVertical />
                        </button>
                        {actionMenuOpen === user.userId && (
                          <div 
                            className="action-dropdown-menu" 
                            style={{ position: 'absolute', right: '0', top: '100%', zIndex: 10, background: 'white', borderRadius: '8px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)', border: '1px solid #e2e8f0', minWidth: '100px', padding: '4px 0', marginTop: '4px' }}
                          >
                            <button 
                              style={{ display: 'block', width: '100%', padding: '6px 14px', background: 'transparent', border: 'none', color: '#475569', fontSize: '0.8rem', cursor: 'pointer', textAlign: 'center', fontWeight: '600' }}
                              onClick={() => {
                                setActionMenuOpen(null);
                                navigate(`/users/edit/${user.userId}`);
                              }}
                              onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f8fafc'}
                              onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                            >
                              Edit
                            </button>
                          </div>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Footer Pagination */}
        <div className="table-footer">
          <div className="footer-info">
            Showing {filteredUsers.length === 0 ? 0 : indexOfFirstItem + 1} to {Math.min(indexOfLastItem, filteredUsers.length)} of {filteredUsers.length} users
          </div>
          <div className="footer-controls">
            <div className="pagination">
              <button 
                className="page-btn" 
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
              >
                ‹
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                <button 
                  key={page} 
                  className={`page-btn ${currentPage === page ? 'active' : ''}`}
                  onClick={() => setCurrentPage(page)}
                >
                  {page}
                </button>
              ))}
              <button 
                className="page-btn" 
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
              >
                ›
              </button>
            </div>
            <div className="rows-per-page">
              <span>Show</span>
              <select value={itemsPerPage} onChange={(e) => setItemsPerPage(Number(e.target.value))}>
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={25}>25</option>
              </select>
              <span>per page</span>
            </div>
          </div>
        </div>
      </div>

      {/* View User Modal (Premium Design) */}
      {viewUser && (
        <div className="premium-modal-overlay">
          <div className="premium-modal-content fade-in">
            <div className="premium-modal-header">
              <div className="premium-modal-icon-container">
                <FiUser />
              </div>
              <div className="premium-modal-title-group">
                <h2>User Details</h2>
                <p>View detailed information about this user.</p>
              </div>
              <button className="premium-modal-close-btn" onClick={() => setViewUser(null)}>
                <FiX />
              </button>
            </div>
            
            <div className="premium-modal-body">
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiUser /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Full Name</span>
                  <span className="premium-info-value">{viewUser.fullName}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiMail /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Email Address</span>
                  <span className="premium-info-value">{viewUser.email}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiPhone /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Phone Number</span>
                  <span className="premium-info-value">{viewUser.phone}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiShield /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Role</span>
                  <span className="premium-info-value">{viewUser.role}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiActivity /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Status</span>
                  <div className="premium-info-value">
                    <span className={`status-badge-btn static-badge status-${viewUser.accountStatus?.toLowerCase() || 'unknown'}`} style={{ display: 'inline-flex', padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: '600', backgroundColor: viewUser.accountStatus === 'ACTIVE' ? '#dcfce7' : '#fee2e2', color: viewUser.accountStatus === 'ACTIVE' ? '#166534' : '#991b1b' }}>
                      <span className="status-dot" style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'currentColor', marginRight: '6px' }}></span>
                      {viewUser.accountStatus === 'ACTIVE' ? 'Active' : 'Deactivated'}
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><FiHash /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">User ID</span>
                  <span className="premium-info-value text-muted">{viewUser.userId}</span>
                </div>
              </div>
              
              <div className="premium-info-card full-width">
                <div className="premium-info-icon"><FiMapPin /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Address</span>
                  <span className="premium-info-value">{viewUser.address || 'System'}</span>
                </div>
              </div>
              
              <div className="premium-info-card full-width">
                <div className="premium-info-icon"><FiFileText /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Additional Information</span>
                  <span className="premium-info-value">{viewUser.additionalInfo || 'None'}</span>
                </div>
              </div>
              
              <div className="premium-info-card full-width">
                <div className="premium-info-icon"><FiCalendar /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Created At</span>
                  <span className="premium-info-value">{viewUser.createdAt ? new Date(viewUser.createdAt).toLocaleString() : 'N/A'}</span>
                </div>
              </div>
            </div>
            
            <div className="premium-modal-footer">
              <button className="btn-premium-close" onClick={() => setViewUser(null)}>Close</button>
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
                <div className="user-avatar" style={{ width: '48px', height: '48px', fontSize: '1.2rem', backgroundColor: `${getAvatarColor(statusConfirm.user.fullName)}20`, color: getAvatarColor(statusConfirm.user.fullName), display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '8px' }}>
                  {getInitials(statusConfirm.user.fullName)}
                </div>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#0f172a' }}>{statusConfirm.user.fullName}</h3>
                  <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>Role: {statusConfirm.user.role}</p>
                </div>
              </div>
              <div style={{ backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                  {statusConfirm.newStatus === 'ACTIVE' ? (
                    <FiUserCheck style={{ color: '#10b981', fontSize: '1.5rem' }} />
                  ) : (
                    <FiUserX style={{ color: '#ef4444', fontSize: '1.5rem' }} />
                  )}
                  <span style={{ fontWeight: '600', color: '#1e293b' }}>
                    {statusConfirm.newStatus === 'ACTIVE' ? 'Activate Account' : 'Deactivate Account'}
                  </span>
                </div>
                <p style={{ margin: 0, fontSize: '0.85rem', color: '#64748b' }}>
                  Are you sure you want to change the status of this user to <strong>{statusConfirm.newStatus === 'ACTIVE' ? 'Active' : 'Deactivated'}</strong>?
                  {statusConfirm.newStatus === 'DEACTIVATED' && ' They will lose access to the system.'}
                </p>
              </div>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setStatusConfirm(null)}>Cancel</button>
              <button 
                className="btn-modal-confirm" 
                style={{ backgroundColor: statusConfirm.newStatus === 'DEACTIVATED' ? '#ef4444' : '#10b981' }}
                onClick={confirmStatusChange}
              >
                Confirm Change
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default Users;
