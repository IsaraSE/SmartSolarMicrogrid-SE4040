import { LuSearch, LuBell } from 'react-icons/lu';
import { useAuth } from '../../context/AuthContext';
import './Topbar.css';

const Topbar = () => {
  const { user } = useAuth();

  return (
    <div className="topbar">
      <div className="topbar-search">
        <LuSearch className="topbar-search-icon" />
        <input type="text" placeholder="Search users, stations, reservations..." />
        <div className="topbar-search-shortcut">
          <span>⌘</span>
          <span>K</span>
        </div>
      </div>

      <div className="topbar-actions">
        <div className="topbar-notification">
          <LuBell />
          <span className="notification-badge">3</span>
        </div>

        <div className="topbar-user">
          <img 
            src="https://ui-avatars.com/api/?name=Tharindu+Perera&background=e2e8f0&color=1a233a" 
            alt="User Avatar" 
            className="topbar-avatar" 
          />
          <div className="topbar-user-info">
            <h4 className="topbar-user-name">{user?.fullName || 'Tharindu Perera'}</h4>
            <span className="topbar-user-role">{user?.role || 'Backoffice'}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Topbar;
