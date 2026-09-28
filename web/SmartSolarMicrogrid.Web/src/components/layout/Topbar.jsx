import React, { useState, useEffect, useRef } from 'react';
import { LuSearch, LuBell, LuUser, LuHardHat, LuCalendar } from 'react-icons/lu';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import dashboardService from '../../services/dashboardService';
import './Topbar.css';

const Topbar = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const dropdownRef = useRef(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [results, setResults] = useState({ users: [], stations: [], reservations: [] });

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (!searchQuery.trim()) {
      setResults({ users: [], stations: [], reservations: [] });
      setIsDropdownOpen(false);
      return;
    }

    const fetchSearchData = async () => {
      setIsSearching(true);
      try {
        const query = searchQuery.toLowerCase();
        
        // Parallel fetching
        const [usersRes, prosumersRes, stationsRes, reservationsRes] = await Promise.all([
          dashboardService.getUsers(),
          dashboardService.getProsumers(),
          dashboardService.getStations(),
          dashboardService.getReservations()
        ]);

        const usersData = Array.isArray(usersRes) ? usersRes : (usersRes?.data || usersRes?.$values || []);
        const prosumersData = Array.isArray(prosumersRes) ? prosumersRes : (prosumersRes?.data || prosumersRes?.$values || []);
        const allUsers = [...usersData, ...prosumersData];
        
        const stationsData = Array.isArray(stationsRes) ? stationsRes : (stationsRes?.data || stationsRes?.$values || []);
        const reservationsData = Array.isArray(reservationsRes) ? reservationsRes : (reservationsRes?.data || reservationsRes?.$values || []);

        // Local filtering
        const filteredUsers = allUsers.filter(u => 
          (u.fullName && u.fullName.toLowerCase().includes(query)) || 
          (u.email && u.email.toLowerCase().includes(query)) ||
          (u.nic && u.nic.toLowerCase().includes(query))
        ).slice(0, 3); // Limit to 3

        const filteredStations = stationsData.filter(s => 
          (s.stationName && s.stationName.toLowerCase().includes(query)) || 
          (s.name && s.name.toLowerCase().includes(query)) || // fallback
          (s.address && s.address.toLowerCase().includes(query)) ||
          (s.location && s.location.toLowerCase().includes(query)) // fallback
        ).slice(0, 3);

        const filteredReservations = reservationsData.filter(r => 
          (r.reservationId && r.reservationId.toLowerCase().includes(query)) || 
          (r.reservationNumber && r.reservationNumber.toLowerCase().includes(query)) || 
          (r._id && r._id.toLowerCase().includes(query)) || // fallback
          (r.status && String(r.status).toLowerCase().includes(query))
        ).slice(0, 3);

        setResults({
          users: filteredUsers,
          stations: filteredStations,
          reservations: filteredReservations
        });
        
        setIsDropdownOpen(true);
      } catch (error) {
        console.error('Search error:', error);
      } finally {
        setIsSearching(false);
      }
    };

    // Debounce search
    const timer = setTimeout(fetchSearchData, 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  const handleNavigate = (path) => {
    navigate(path);
    setIsDropdownOpen(false);
    setSearchQuery('');
  };

  const hasResults = results.users.length > 0 || results.stations.length > 0 || results.reservations.length > 0;

  return (
    <div className="topbar">
      <div className="topbar-search" ref={dropdownRef}>
        <LuSearch className="topbar-search-icon" />
        <input 
          type="text" 
          placeholder="Search users, stations, reservations..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onFocus={() => { if (searchQuery.trim()) setIsDropdownOpen(true); }}
        />
        
        {isDropdownOpen && (
          <div className="search-dropdown">
            {isSearching ? (
              <div className="search-loading">Searching...</div>
            ) : !hasResults ? (
              <div className="search-loading">No results found for "{searchQuery}"</div>
            ) : (
              <div className="search-results">
                {results.users.length > 0 && (
                  <div className="search-category">
                    <h5 className="category-title">Users & Prosumers</h5>
                    {results.users.map(u => (
                      <div key={u.userId || u.nic} className="search-item" onClick={() => handleNavigate('/users')}>
                        <LuUser className="item-icon" />
                        <div className="item-details">
                          <span className="item-name">{u.fullName || u.email}</span>
                          <span className="item-sub">{u.role || 'Prosumer'}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {results.stations.length > 0 && (
                  <div className="search-category">
                    <h5 className="category-title">Solar Stations</h5>
                    {results.stations.map(s => (
                      <div key={s.stationId} className="search-item" onClick={() => handleNavigate('/stations')}>
                        <LuHardHat className="item-icon" />
                        <div className="item-details">
                          <span className="item-name">{s.stationName}</span>
                          <span className="item-sub">{s.address}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {results.reservations.length > 0 && (
                  <div className="search-category">
                    <h5 className="category-title">Reservations</h5>
                    {results.reservations.map(r => (
                      <div key={r.reservationId} className="search-item" onClick={() => handleNavigate('/reservations')}>
                        <LuCalendar className="item-icon" />
                        <div className="item-details">
                          <span className="item-name">Reservation #{r.reservationNumber}</span>
                          <span className={`item-sub status-${r.status?.toLowerCase()}`}>{r.status}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      <div className="topbar-actions">
        <div className="topbar-notification">
          <LuBell />
          <span className="notification-badge">3</span>
        </div>

        <div className="topbar-user">
          <img 
            src={`https://ui-avatars.com/api/?name=${encodeURIComponent(user?.fullName || user?.email || 'User')}&background=e2e8f0&color=1a233a`} 
            alt="User Avatar" 
            className="topbar-avatar" 
          />
          <div className="topbar-user-info">
            <h4 className="topbar-user-name">{user?.fullName || user?.email || 'User'}</h4>
            <span className="topbar-user-role">{user?.role || 'User'}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Topbar;
