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
import { FiSearch, FiRefreshCw, FiPlus, FiEdit2, FiMoreHorizontal } from 'react-icons/fi';
import userService from '../../services/userService';
import './Users.css';

const Users = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
              <option value="ADMIN">ADMIN</option>
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
                <th>User ID</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Role</th>
                <th>Account Status</th>
                <th>Created At</th>
                <th className="th-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8" className="loading-cell">Loading users...</td>
                </tr>
              ) : currentUsers.length === 0 ? (
                <tr>
                  <td colSpan="8" className="empty-cell">No users found.</td>
                </tr>
              ) : (
                currentUsers.map((user) => (
                  <tr key={user.userId}>
                    <td className="cell-id">
                      {user.userId ? `USR-${user.userId.substring(18, 24).toUpperCase()}` : 'N/A'}
                    </td>
                    <td>
                      <div className="cell-user">
                        <div 
                          className="user-avatar" 
                          style={{ backgroundColor: `${getAvatarColor(user.fullName)}20`, color: getAvatarColor(user.fullName) }}
                        >
                          {getInitials(user.fullName)}
                        </div>
                        <span className="user-name">{user.fullName}</span>
                      </div>
                    </td>
                    <td className="cell-email">{user.email}</td>
                    <td className="cell-phone">{user.phone}</td>
                    <td>
                      <span className={`role-badge role-${user.role?.toLowerCase() || 'unknown'}`}>
                        {user.role}
                      </span>
                    </td>
                    <td>
                      <span className={`status-badge status-${user.accountStatus?.toLowerCase() || 'unknown'}`}>
                        <span className="status-dot"></span>
                        {user.accountStatus === 'ACTIVE' ? 'Active' : user.accountStatus === 'PENDING' ? 'Pending' : 'Inactive'}
                      </span>
                    </td>
                    <td>{formatDate(user.createdAt)}</td>
                    <td className="cell-actions">
                      <button className="btn-icon" onClick={() => navigate(`/users/edit/${user.userId}`)}><FiEdit2 /></button>
                      <button className="btn-icon"><FiMoreHorizontal /></button>
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
    </div>
  );
};

export default Users;
