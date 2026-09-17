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
import { FiSearch, FiRefreshCw, FiPlus, FiEdit2, FiChevronDown } from 'react-icons/fi';
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

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('All Roles');
  const [statusFilter, setStatusFilter] = useState('All Status');

  // Pagination (Mocked for UI)
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  useEffect(() => {
    fetchUsers();
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
              <option value="PENDING">PENDING</option>
              <option value="INACTIVE">INACTIVE</option>
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
                <th>Account Status</th>
                <th className="th-actions">Actions</th>
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
                        <div 
                          className="user-avatar" 
                          style={{ backgroundColor: `${getAvatarColor(user.fullName)}20`, color: getAvatarColor(user.fullName) }}
                        >
                          {getInitials(user.fullName)}
                        </div>
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
                    <td>
                      <button 
                        className={`status-badge-btn status-${user.accountStatus?.toLowerCase() || 'unknown'}`}
                        onClick={() => handleStatusClick(user)}
                      >
                        <div className="status-badge-content">
                          <span className="status-dot"></span>
                          <span>{user.accountStatus === 'ACTIVE' ? 'Active' : 'Deactivated'}</span>
                        </div>
                        <FiChevronDown className="status-chevron" />
                      </button>
                    </td>
                    <td className="cell-actions">
                      <button className="btn-action btn-view-text" onClick={() => setViewUser(user)}>View</button>
                      <button className="btn-action btn-edit-text" onClick={() => navigate(`/users/edit/${user.userId}`)}>Edit</button>
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

      {/* View User Modal */}
      {viewUser && (
        <div className="user-modal-overlay">
          <div className="user-modal-content fade-in">
            <div className="user-modal-header">
              <h2>User Details</h2>
              <button className="user-modal-close" onClick={() => setViewUser(null)}>&times;</button>
            </div>
            <div className="user-modal-body">
              <div className="detail-group">
                <label>Full Name</label>
                <div className="detail-value">{viewUser.fullName}</div>
              </div>
              <div className="detail-group">
                <label>Email Address</label>
                <div className="detail-value">{viewUser.email}</div>
              </div>
              <div className="detail-group">
                <label>Phone Number</label>
                <div className="detail-value">{viewUser.phone}</div>
              </div>
              <div className="detail-group">
                <label>Role</label>
                <div className="detail-value">{viewUser.role}</div>
              </div>
              <div className="detail-group">
                <label>Account Status</label>
                <div className="detail-value">{viewUser.accountStatus === 'ACTIVE' ? 'Active' : 'Deactivated'}</div>
              </div>
              <div className="detail-group">
                <label>Additional Information</label>
                <div className="detail-value">{viewUser.address || 'None'}</div>
              </div>
              <div className="detail-group">
                <label>Created At</label>
                <div className="detail-value">
                  {viewUser.createdAt ? new Date(viewUser.createdAt).toLocaleString() : 'N/A'}
                </div>
              </div>
              <div className="detail-group">
                <label>User ID</label>
                <div className="detail-value text-muted">{viewUser.userId}</div>
              </div>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-close" onClick={() => setViewUser(null)}>Close</button>
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
              <p>Are you sure you want to change the status of <strong>{statusConfirm.user.fullName}</strong> from <strong className={`text-${statusConfirm.user.accountStatus.toLowerCase()}`}>{statusConfirm.user.accountStatus === 'ACTIVE' ? 'Active' : 'Deactivated'}</strong> to <strong className={`text-${statusConfirm.newStatus.toLowerCase()}`}>{statusConfirm.newStatus === 'ACTIVE' ? 'Active' : 'Deactivated'}</strong>?</p>
            </div>
            <div className="user-modal-footer">
              <button className="btn-modal-cancel" onClick={() => setStatusConfirm(null)}>Cancel</button>
              <button className="btn-modal-confirm" onClick={confirmStatusChange}>Confirm Change</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default Users;
