import { NavLink } from 'react-router-dom';
import { 
  LuLayoutDashboard, 
  LuUsers, 
  LuHouse, 
  LuBatteryCharging, 
  LuCalendar, 
  LuCalendarCheck, 
  LuSettings 
} from 'react-icons/lu';
import heliogridLogo from '../../assets/images/heliogrid-logo-transparent.png';
import solarHeroBg from '../../assets/images/solar-hero-bg.jpg';
import './Sidebar.css';

const Sidebar = () => {
  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: <LuLayoutDashboard /> },
    { path: '/users', label: 'Users', icon: <LuUsers /> },
    { path: '/prosumers', label: 'Prosumers', icon: <LuHouse /> },
    { path: '/stations', label: 'Stations', icon: <LuBatteryCharging /> },
    { path: '/slots', label: 'Slots', icon: <LuCalendar /> },
    { path: '/reservations', label: 'Reservations', icon: <LuCalendarCheck /> },
    { path: '/settings', label: 'Settings', icon: <LuSettings /> },
  ];

  return (
    <div className="sidebar">
      <img src={solarHeroBg} alt="Background" className="sidebar-bg" />
      
      <div className="sidebar-content">
        <div className="sidebar-header">
          <img src={heliogridLogo} alt="HelioGrid Logo" className="sidebar-logo" />
          <div className="sidebar-brand">
            <h2>HelioGrid</h2>
            <p>Smart Energy Platform</p>
          </div>
        </div>

        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
            >
              {item.icon}
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <h3>Clean Energy<br/>Stronger Communities</h3>
          <p></p>
          <div className="sidebar-footer-brand">
            <img src={heliogridLogo} alt="Logo" />
            <span>Powering a<br/>Sustainable Tomorrow</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
