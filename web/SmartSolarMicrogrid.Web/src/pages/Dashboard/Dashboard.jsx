import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import dashboardService from '../../services/dashboardService';
import { 
  LuCalendar, 
  LuSun, 
  LuUser,
  LuUsers, 
  LuHardHat, 
  LuBatteryCharging,
  LuChartBar,
  LuServer,
  LuArrowUp,
  LuArrowDown,
  LuMail,
  LuPhone,
  LuCreditCard,
  LuShieldCheck,
  LuMapPin,
  LuCircleCheck,
  LuShieldAlert,
  LuInfo,
  LuZap,
  LuClock
} from 'react-icons/lu';
import { FiMoreVertical, FiX } from 'react-icons/fi';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import './Dashboard.css';

const STATUS_COLORS = {
  ACTIVE: '#10b981', // green
  PENDING: '#facc15', // yellow
  DEACTIVATED: '#ef4444' // red
};

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const userName = user?.fullName?.split(' ')[0] || user?.email?.split('@')[0] || 'User';

  const [loading, setLoading] = useState(true);
  const [currentDate, setCurrentDate] = useState('');
  
  // KPIs
  const [totalWebUsers, setTotalWebUsers] = useState(0);
  const [pendingProsumers, setPendingProsumers] = useState(0);
  const [activeStations, setActiveStations] = useState(0);
  const [pendingReservationsCount, setPendingReservationsCount] = useState(0);
  
  // Charts & Tables Data
  const [systemOverviewData, setSystemOverviewData] = useState([]);
  const [prosumerStatusData, setProsumerStatusData] = useState([]);
  const [prosumerStats, setProsumerStats] = useState({ total: 0, active: 0, activePct: 0, pending: 0, pendingPct: 0, deactivated: 0, deactivatedPct: 0 });
  const [pendingProsumersList, setPendingProsumersList] = useState([]);
  
  // Modals
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, prosumer: null });
  const [messageModal, setMessageModal] = useState({ isOpen: false, title: '', message: '', type: 'success' });
  const [reviewModal, setReviewModal] = useState({ isOpen: false, prosumer: null });

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(5);

  // Raw data for dynamic recalculation
  const [rawUsers, setRawUsers] = useState([]);
  const [rawProsumers, setRawProsumers] = useState([]);
  const [rawReservations, setRawReservations] = useState([]);
  const [chartPeriod, setChartPeriod] = useState(7);

  const getInitials = (name) => {
    if (!name) return 'U';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  const generateChartData = (users, prosumers, reservations, days) => {
    const lastNDays = [];
    for (let i = days - 1; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      lastNDays.push({
        date: d,
        name: d.toLocaleDateString('en-US', { month: 'short', day: '2-digit' }),
        'User Registrations': 0,
        'Reservations': 0
      });
    }

    [...users, ...prosumers].forEach(u => {
      if (!u.createdAt && !u.registeredAt) return;
      const rDate = new Date(u.createdAt || u.registeredAt);
      const dayMatch = lastNDays.find(d => d.date.getDate() === rDate.getDate() && d.date.getMonth() === rDate.getMonth() && d.date.getFullYear() === rDate.getFullYear());
      if (dayMatch) {
        dayMatch['User Registrations']++;
      }
    });

    reservations.forEach(r => {
      if (!r.createdAt) return;
      const rDate = new Date(r.createdAt);
      const dayMatch = lastNDays.find(d => d.date.getDate() === rDate.getDate() && d.date.getMonth() === rDate.getMonth() && d.date.getFullYear() === rDate.getFullYear());
      if (dayMatch) {
        dayMatch['Reservations']++;
      }
    });

    return lastNDays;
  };

  useEffect(() => {
    // Set dynamic date
    const dateOptions = { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' };
    setCurrentDate(new Date().toLocaleDateString('en-GB', dateOptions));

    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const [usersRes, prosumersRes, stationsRes, reservationsRes] = await Promise.all([
          dashboardService.getUsers(),
          dashboardService.getProsumers(),
          dashboardService.getStations(),
          dashboardService.getReservations()
        ]);

        const allUsers = usersRes.data || usersRes || [];
        const allProsumers = prosumersRes.data || prosumersRes || [];
        const allStations = stationsRes.data || stationsRes || [];
        const allReservations = reservationsRes.data || reservationsRes || [];

        setRawUsers(allUsers);
        setRawProsumers(allProsumers);
        setRawReservations(allReservations);

        // 1. KPIs
        setTotalWebUsers(allUsers.length + allProsumers.length);
        
        const pendingPros = allProsumers.filter(p => p.accountStatus === 'PENDING' || p.accountStatus === 0);
        setPendingProsumers(pendingPros.length);
        
        setActiveStations(allStations.filter(s => s.status === 'ACTIVE' || s.status === 0).length);
        
        setPendingReservationsCount(allReservations.filter(r => r.status === 'PENDING' || r.status === 0).length);

        // 2. System Overview (Bar Chart)
        setSystemOverviewData(generateChartData(allUsers, allProsumers, allReservations, 7));

        // 3. Prosumer Account Status (Donut Chart)
        const totalPros = allProsumers.length || 1; // avoid div by 0
        const activePros = allProsumers.filter(p => p.accountStatus === 'ACTIVE' || p.accountStatus === 1).length;
        const deactPros = allProsumers.filter(p => p.accountStatus === 'DEACTIVATED' || p.accountStatus === 2).length;
        const pendPros = allProsumers.filter(p => p.accountStatus === 'PENDING' || p.accountStatus === 0).length;

        setProsumerStatusData([
          { name: 'Active', value: activePros, color: STATUS_COLORS.ACTIVE },
          { name: 'Pending', value: pendPros, color: STATUS_COLORS.PENDING },
          { name: 'Deactivated', value: deactPros, color: STATUS_COLORS.DEACTIVATED },
        ]);

        setProsumerStats({
          total: allProsumers.length,
          active: activePros,
          activePct: Math.round((activePros / totalPros) * 100),
          pending: pendPros,
          pendingPct: Math.round((pendPros / totalPros) * 100),
          deactivated: deactPros,
          deactivatedPct: Math.round((deactPros / totalPros) * 100)
        });

        // 4. Pending Prosumer Activations (Table)
        const pendingProsList = pendingPros
          .sort((a, b) => new Date(b.createdAt || b.registeredAt || 0) - new Date(a.createdAt || a.registeredAt || 0))
          .map(p => {
            const rDate = new Date(p.createdAt || p.registeredAt || new Date());
            return {
              nic: p.nic,
              name: p.firstName ? `${p.firstName} ${p.lastName}` : p.fullName || 'Unknown',
              email: p.email,
              phone: p.phoneNumber || p.phone || 'N/A',
              address: p.address || 'N/A',
              createdAtStr: rDate.toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' }) + ', ' + rDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
              dateStr: rDate.toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' }) + ' ' + rDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
              status: 'Pending',
              rawStatus: p.accountStatus || 'PENDING'
            };
          });
          
        setPendingProsumersList(pendingProsList);

      } catch (error) {
        console.error("Failed to load dashboard data", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="custom-tooltip" style={{ backgroundColor: '#fff', padding: '10px', border: '1px solid #e2e8f0', borderRadius: '6px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }}>
          <p style={{ margin: '0 0 5px', fontWeight: '600', color: '#1e293b' }}>{label}</p>
          {payload.map((entry, index) => (
            <p key={index} style={{ color: entry.color, margin: 0, fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ display: 'inline-block', width: '8px', height: '8px', borderRadius: '2px', backgroundColor: entry.color }}></span>
              {entry.name}: {entry.value}
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  const handlePeriodChange = (e) => {
    const days = parseInt(e.target.value, 10);
    setChartPeriod(days);
    setSystemOverviewData(generateChartData(rawUsers, rawProsumers, rawReservations, days));
  };

  const executeActivate = () => {
    setConfirmModal({ isOpen: false, prosumer: null });
    setMessageModal({
      isOpen: true,
      title: 'Activation Successful',
      message: 'The prosumer account has been successfully verified and activated. They can now access the Smart Energy Platform.',
      type: 'success'
    });
  };

  return (
    <div className="dashboard-container">
      {/* Header */}
      <div className="dashboard-header-container">
        <div className="dashboard-welcome">
          <h1>Welcome back, {userName}!</h1>
          <p>Manage users, prosumers and the HelioGrid platform.</p>
        </div>
        <div className="dashboard-date-weather">
          <div className="dashboard-date">
            <LuCalendar style={{ color: '#64748b' }} />
            <div className="date-text">
              <h4>{currentDate}</h4>
              <p>Good to see you today.</p>
            </div>
          </div>
          <div className="dashboard-weather">
            <LuSun style={{ color: '#facc15' }} />
            <p>A cleaner<br />tomorrow is possible.</p>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="stat-cards-grid new-kpis">
        {/* Total Web Users (Blue) */}
        <div className="stat-card" style={{ background: '#f8fafc', borderColor: '#e2e8f0' }}>
          <div className="stat-card-header">
            <div className="stat-icon" style={{ background: '#eff6ff', color: '#3b82f6' }}><LuUser /></div>
            <h3>Total Web Users</h3>
          </div>
          <div className="stat-value-row">
            <span className="stat-value">{loading ? '...' : totalWebUsers}</span>
            <div className="stat-trend" style={{flexDirection: 'column', alignItems: 'flex-start', gap: '2px'}}>
              <span className="trend-val positive"><LuArrowUp /> 12% <span style={{color: '#94a3b8', fontSize: '0.7rem', fontWeight: '500'}}>vs. last week</span></span>
              <span className="trend-desc" style={{fontSize: '0.75rem'}}>Registered platform users</span>
            </div>
          </div>
        </div>

        {/* Pending Prosumers (Yellow) */}
        <div className="stat-card" style={{ background: '#fefce8', borderColor: '#fef08a' }}>
          <div className="stat-card-header">
            <div className="stat-icon" style={{ background: '#fef9c3', color: '#ca8a04' }}><LuUsers /></div>
            <h3>Pending Prosumers</h3>
          </div>
          <div className="stat-value-row">
            <span className="stat-value">{loading ? '...' : pendingProsumers}</span>
            <div className="stat-trend" style={{flexDirection: 'column', alignItems: 'flex-start', gap: '2px'}}>
              <span className="trend-val negative" style={{color: '#ef4444'}}><LuArrowUp /> 33% <span style={{color: '#94a3b8', fontSize: '0.7rem', fontWeight: '500'}}>vs. last week</span></span>
              <span className="trend-desc" style={{fontSize: '0.75rem'}}>Awaiting verification</span>
            </div>
          </div>
        </div>

        {/* Active Stations (Green) */}
        <div className="stat-card" style={{ background: '#f0fdf4', borderColor: '#dcfce7' }}>
          <div className="stat-card-header">
            <div className="stat-icon" style={{ background: '#dcfce7', color: '#10b981' }}><LuBatteryCharging /></div>
            <h3>Active Stations</h3>
          </div>
          <div className="stat-value-row">
            <span className="stat-value">{loading ? '...' : activeStations}</span>
            <div className="stat-trend" style={{flexDirection: 'column', alignItems: 'flex-start', gap: '2px'}}>
              <span className="trend-val positive"><LuArrowUp /> 0% <span style={{color: '#94a3b8', fontSize: '0.7rem', fontWeight: '500'}}>vs. last week</span></span>
              <span className="trend-desc" style={{fontSize: '0.75rem'}}>Out of {loading ? '...' : activeStations} total stations</span>
            </div>
          </div>
        </div>

        {/* Pending Reservations (Purple) */}
        <div className="stat-card" style={{ background: '#faf5ff', borderColor: '#f3e8ff' }}>
          <div className="stat-card-header">
            <div className="stat-icon" style={{ background: '#f3e8ff', color: '#a855f7' }}><LuCalendar /></div>
            <h3>Pending Reservations</h3>
          </div>
          <div className="stat-value-row">
            <span className="stat-value">{loading ? '...' : pendingReservationsCount}</span>
            <div className="stat-trend" style={{flexDirection: 'column', alignItems: 'flex-start', gap: '2px'}}>
              <span className="trend-val negative" style={{color: '#ef4444'}}><LuArrowUp /> 60% <span style={{color: '#94a3b8', fontSize: '0.7rem', fontWeight: '500'}}>vs. last week</span></span>
              <span className="trend-desc" style={{fontSize: '0.75rem'}}>Awaiting confirmation</span>
            </div>
          </div>
        </div>
      </div>

      {/* Charts */}
      <div className="charts-grid mockup-charts-layout">
        {/* System Overview (Bar Chart) */}
        <div className="card overview-card">
          <div className="card-header">
            <div className="card-title">
              <LuChartBar />
              <div style={{display: 'flex', flexDirection: 'column'}}>
                <h3 style={{margin: 0, fontSize: '1rem'}}>System Overview</h3>
                <span style={{fontSize: '0.75rem', color: '#64748b', fontWeight: '500'}}>User registrations and reservations (last {chartPeriod} days)</span>
              </div>
            </div>
            <select className="card-select" value={chartPeriod} onChange={handlePeriodChange}>
              <option value={7}>Last 7 days</option>
              <option value={14}>Last 14 days</option>
              <option value={30}>Last 30 days</option>
            </select>
          </div>
          <div style={{ width: '100%', height: 260, marginTop: '20px' }}>
            {loading ? (
              <div style={{height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8'}}>Loading chart data...</div>
            ) : (
              <ResponsiveContainer>
                <BarChart data={systemOverviewData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#64748b' }} dy={10} />
                  <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#64748b' }} />
                  <RechartsTooltip content={<CustomTooltip />} cursor={{fill: '#f8fafc'}} />
                  <Legend iconType="square" iconSize={10} wrapperStyle={{ fontSize: '12px', color: '#64748b', paddingTop: '20px' }} />
                  <Bar dataKey="User Registrations" fill="#3b82f6" barSize={16} radius={[2, 2, 0, 0]} />
                  <Bar dataKey="Reservations" fill="#10b981" barSize={16} radius={[2, 2, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        {/* Prosumer Account Status (Donut Chart) */}
        <div className="card status-card">
          <div className="card-header" style={{borderBottom: 'none'}}>
            <div className="card-title">
              <LuUsers />
              <div style={{display: 'flex', flexDirection: 'column'}}>
                <h3 style={{margin: 0, fontSize: '1rem'}}>Prosumer Account Status</h3>
                <span style={{fontSize: '0.75rem', color: '#64748b', fontWeight: '500'}}>Distribution of prosumer accounts</span>
              </div>
            </div>
          </div>
          
          <div className="donut-chart-container">
            <div className="donut-visual">
              {!loading && (
                <ResponsiveContainer width="100%" height={180}>
                  <PieChart>
                    <Pie
                      data={prosumerStatusData}
                      innerRadius={65}
                      outerRadius={85}
                      paddingAngle={2}
                      dataKey="value"
                      stroke="none"
                    >
                      {prosumerStatusData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <RechartsTooltip content={<CustomTooltip />} />
                  </PieChart>
                </ResponsiveContainer>
              )}
              <div className="donut-center">
                <span className="donut-number">{loading ? '-' : prosumerStats.total}</span>
                <span className="donut-label">Total<br/>Prosumers</span>
              </div>
            </div>
            
            <div className="donut-legend">
              <div className="legend-row">
                <span className="legend-marker" style={{backgroundColor: STATUS_COLORS.ACTIVE}}></span>
                <span className="legend-name">Active</span>
                <span className="legend-val">{loading ? '-' : prosumerStats.active}</span>
                <span className="legend-pct">{loading ? '-' : prosumerStats.activePct}%</span>
              </div>
              <div className="legend-row">
                <span className="legend-marker" style={{backgroundColor: STATUS_COLORS.PENDING}}></span>
                <span className="legend-name">Pending</span>
                <span className="legend-val">{loading ? '-' : prosumerStats.pending}</span>
                <span className="legend-pct">{loading ? '-' : prosumerStats.pendingPct}%</span>
              </div>
              <div className="legend-row">
                <span className="legend-marker" style={{backgroundColor: STATUS_COLORS.DEACTIVATED}}></span>
                <span className="legend-name">Deactivated</span>
                <span className="legend-val">{loading ? '-' : prosumerStats.deactivated}</span>
                <span className="legend-pct">{loading ? '-' : prosumerStats.deactivatedPct}%</span>
              </div>
            </div>
          </div>
          
          <div className="donut-footer">
            <div className="trend-box">
              <LuChartBar style={{ color: '#10b981' }} />
            </div>
            <div className="trend-text">
              <span className="trend-up">+14%</span>
              <span className="trend-desc">Total prosumers vs. last month</span>
            </div>
          </div>
        </div>
      </div>

      {/* Tables Area */}
      <div className="tables-grid">
        <div className="card full-width">
          <div className="card-header">
            <div className="card-title">
              <LuUsers />
              <div style={{display: 'flex', flexDirection: 'column'}}>
                <h3 style={{margin: 0, fontSize: '1rem'}}>Pending Prosumer Activations</h3>
                <span style={{fontSize: '0.75rem', color: '#64748b', fontWeight: '500'}}>New prosumer registrations awaiting verification</span>
              </div>
            </div>
            <a href="/prosumers" className="view-all">View All</a>
          </div>
          <table className="dashboard-table">
            <thead>
              <tr>
                <th>NIC</th>
                <th>Prosumer Name</th>
                <th>Email</th>
                <th>Registered At</th>
                <th style={{textAlign: 'center'}}>Status</th>
                <th style={{textAlign: 'center'}}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', color: '#64748b', padding: '20px' }}>Loading activities...</td>
                </tr>
              ) : pendingProsumersList.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', color: '#64748b', padding: '20px' }}>No pending activations.</td>
                </tr>
              ) : (
                pendingProsumersList.slice((currentPage - 1) * itemsPerPage, (currentPage - 1) * itemsPerPage + itemsPerPage).map((pro, index) => (
                  <tr key={index}>
                    <td style={{fontFamily: 'monospace', color: '#475569'}}>{pro.nic}</td>
                    <td style={{fontWeight: '500', color: '#1e293b'}}>{pro.name}</td>
                    <td>{pro.email}</td>
                    <td>{pro.dateStr}</td>
                    <td style={{textAlign: 'center'}}>
                      <span className="badge pending-badge">Pending</span>
                    </td>
                    <td>
                      <div style={{display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px'}}>
                        <button className="review-btn" onClick={() => setReviewModal({ isOpen: true, prosumer: pro })}>Review</button>
                        <button className="activate-btn" onClick={() => setConfirmModal({ isOpen: true, prosumer: pro })}>Activate</button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {/* Pagination Controls */}
          {pendingProsumersList.length > 0 && (
            <div className="pagination-container">
              <div className="pagination-info">
                Showing {Math.min((currentPage - 1) * itemsPerPage + 1, pendingProsumersList.length)}-{Math.min((currentPage - 1) * itemsPerPage + itemsPerPage, pendingProsumersList.length)} of {pendingProsumersList.length} pending requests
              </div>
              <div className="pagination-controls">
                <div className="items-per-page">
                  <select 
                    value={itemsPerPage} 
                    onChange={(e) => { 
                      setItemsPerPage(Number(e.target.value)); 
                      setCurrentPage(1); 
                    }}
                  >
                    <option value={5}>5 per page</option>
                    <option value={10}>10 per page</option>
                    <option value={20}>20 per page</option>
                  </select>
                </div>
                <div className="page-buttons">
                  <button 
                    className="page-btn nav-btn" 
                    disabled={currentPage === 1} 
                    onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                  >
                    &lt;
                  </button>
                  
                  {Array.from({ length: Math.ceil(pendingProsumersList.length / itemsPerPage) }, (_, i) => i + 1).map(page => (
                    <button 
                      key={page} 
                      className={`page-btn ${currentPage === page ? 'active' : ''}`}
                      onClick={() => setCurrentPage(page)}
                    >
                      {page}
                    </button>
                  ))}
                  
                  <button 
                    className="page-btn nav-btn" 
                    disabled={currentPage === Math.ceil(pendingProsumersList.length / itemsPerPage)} 
                    onClick={() => setCurrentPage(prev => Math.min(prev + 1, Math.ceil(pendingProsumersList.length / itemsPerPage)))}
                  >
                    &gt;
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
      
      {/* Tiny footer disclaimer */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #e2e8f0' }}>
        <div>
          <p style={{ margin: 0, fontFamily: 'Inter', fontSize: '0.75rem', fontWeight: '600', color: '#1a233a' }}>HelioGrid — Smart Energy Platform</p>
          <p style={{ margin: 0, fontFamily: 'Inter', fontSize: '0.7rem', color: '#64748b' }}>Building a smarter, cleaner, and more resilient energy future.</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontFamily: 'Inter', fontSize: '0.75rem', color: '#64748b', fontWeight: '500' }}>
          <LuSun style={{ color: '#10b981' }} /> Renewable People. Real Progress.
        </div>
      </div>

      {/* Review Modal (Premium Design) */}
      {reviewModal.isOpen && reviewModal.prosumer && (
        <div className="premium-modal-overlay">
          <div className="premium-modal-content fade-in">
            <div className="premium-modal-header">
              <div className="premium-modal-icon-container">
                <LuUsers />
              </div>
              <div className="premium-modal-title-group">
                <h2>Prosumer Details</h2>
                <p>View detailed information about this prosumer account.</p>
              </div>
              <button className="premium-modal-close-btn" onClick={() => setReviewModal({ isOpen: false, prosumer: null })}>
                <FiX />
              </button>
            </div>
            
            <div className="premium-modal-body">
              <div className="premium-info-card">
                <div className="premium-info-icon"><LuUser /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Full Name</span>
                  <span className="premium-info-value">{reviewModal.prosumer.name}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><LuCreditCard /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">NIC Number</span>
                  <span className="premium-info-value">{reviewModal.prosumer.nic}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><LuMail /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Email Address</span>
                  <span className="premium-info-value">{reviewModal.prosumer.email}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><LuPhone /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Phone Number</span>
                  <span className="premium-info-value">{reviewModal.prosumer.phone}</span>
                </div>
              </div>
              
              <div className="premium-info-card full-width">
                <div className="premium-info-icon"><LuMapPin /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Address</span>
                  <span className="premium-info-value">{reviewModal.prosumer.address}</span>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><LuShieldCheck /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Status</span>
                  <div className="premium-info-value">
                    <span className="status-badge-btn static-badge status-pending" style={{ display: 'inline-flex', padding: '4px 10px', borderRadius: '20px', fontSize: '0.8rem', fontWeight: '600', backgroundColor: '#fef3c7', color: '#b45309' }}>
                      <span className="status-dot" style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'currentColor', marginRight: '6px' }}></span>
                      {reviewModal.prosumer.status}
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="premium-info-card">
                <div className="premium-info-icon"><LuCalendar /></div>
                <div className="premium-info-content">
                  <span className="premium-info-label">Created At</span>
                  <span className="premium-info-value">{reviewModal.prosumer.createdAtStr}</span>
                </div>
              </div>
            </div>
            
            <div className="premium-modal-footer">
              <button className="btn-premium-close" onClick={() => setReviewModal({ isOpen: false, prosumer: null })}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* Confirmation Modal */}
      {confirmModal.isOpen && confirmModal.prosumer && (
        <div className="user-modal-overlay">
          <div className="user-modal-content status-confirm-modal" style={{ maxWidth: '540px', borderRadius: '16px', overflow: 'hidden', padding: '32px', position: 'relative' }}>
            <button className="user-modal-close" onClick={() => setConfirmModal({ isOpen: false, prosumer: null })} style={{ position: 'absolute', top: '24px', right: '24px' }}><FiX /></button>
            
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '16px', marginBottom: '24px' }}>
              <div style={{ width: '48px', height: '48px', borderRadius: '50%', backgroundColor: '#dcfce7', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#10b981', fontSize: '1.5rem', flexShrink: 0 }}>
                <LuCircleCheck />
              </div>
              <div>
                <h2 style={{ margin: '0 0 4px', fontSize: '1.5rem', color: '#0f172a' }}>Activate Prosumer</h2>
                <p style={{ margin: 0, color: '#64748b', fontSize: '0.95rem' }}>Confirm account activation for this prosumer.</p>
              </div>
            </div>

            <div style={{ backgroundColor: '#f8fafc', border: '1px solid #f1f5f9', borderRadius: '12px', padding: '24px', marginBottom: '24px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
                <div style={{ width: '56px', height: '56px', borderRadius: '50%', backgroundColor: '#e2e8f0', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#10b981', fontSize: '1.25rem', fontWeight: 'bold' }}>
                  {getInitials(confirmModal.prosumer.name)}
                </div>
                <div>
                  <h3 style={{ margin: '0 0 4px', fontSize: '1.15rem', color: '#0f172a', fontWeight: '600' }}>{confirmModal.prosumer.name}</h3>
                  <p style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>{confirmModal.prosumer.email}</p>
                </div>
              </div>
              
              <div style={{ height: '1px', backgroundColor: '#e2e8f0', margin: '0 -24px 20px' }}></div>

              <div style={{ display: 'flex', alignItems: 'center' }}>
                <div style={{ flex: 1, display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                  <LuCreditCard style={{ color: '#64748b', fontSize: '1.2rem', marginTop: '2px' }} />
                  <div>
                    <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: '600' }}>NIC</div>
                    <div style={{ fontSize: '0.95rem', color: '#0f172a', fontWeight: '500', marginTop: '2px' }}>{confirmModal.prosumer.nic}</div>
                  </div>
                </div>
                
                <div style={{ width: '1px', backgroundColor: '#e2e8f0', height: '40px', margin: '0 24px' }}></div>
                
                <div style={{ flex: 1, display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                  <LuClock style={{ color: '#64748b', fontSize: '1.2rem', marginTop: '2px' }} />
                  <div>
                    <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: '600' }}>Current Status</div>
                    <div style={{ marginTop: '4px' }}>
                      <span style={{ backgroundColor: '#fef9c3', color: '#ca8a04', padding: '4px 12px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: '600' }}>
                        {confirmModal.prosumer.status}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '16px', marginBottom: '20px' }}>
              <div style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: '#dcfce7', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#10b981', fontSize: '1.1rem', flexShrink: 0 }}>
                <LuShieldCheck />
              </div>
              <div>
                <h4 style={{ margin: '0 0 4px', fontSize: '1rem', color: '#0f172a', fontWeight: '600' }}>Are you sure you want to activate this account?</h4>
                <p style={{ margin: 0, color: '#64748b', fontSize: '0.9rem', lineHeight: '1.5' }}>
                  Once activated, the prosumer will be granted access to the energy trading platform.
                </p>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', backgroundColor: '#f0fdf4', padding: '12px 16px', borderRadius: '8px', marginBottom: '32px' }}>
              <LuInfo style={{ color: '#10b981', fontSize: '1.1rem' }} />
              <p style={{ margin: 0, color: '#334155', fontSize: '0.9rem' }}>
                This action changes the account status from <strong>Pending</strong> to <strong>Active</strong>.
              </p>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
              <button className="btn-modal-cancel" style={{ padding: '10px 20px', fontSize: '0.95rem' }} onClick={() => setConfirmModal({ isOpen: false, prosumer: null })}>Cancel</button>
              <button className="btn-modal-confirm" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 20px', fontSize: '0.95rem', backgroundColor: '#10b981' }} onClick={executeActivate}>
                <LuZap /> Activate Account
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Message Modal */}
      {messageModal.isOpen && (
        <div className="user-modal-overlay">
          <div className="user-modal-content" style={{ maxWidth: '400px' }}>
            <div className="user-modal-header">
              <h2>{messageModal.title}</h2>
              <button className="user-modal-close" onClick={() => setMessageModal({ isOpen: false, title: '', message: '', type: 'success' })}><FiX /></button>
            </div>
            <div className="user-modal-body">
              <p style={{ margin: 0, color: '#475569', lineHeight: '1.5' }}>
                {messageModal.message}
              </p>
            </div>
            <div className="user-modal-footer">
              <button 
                className="btn-modal-confirm" 
                onClick={() => setMessageModal({ isOpen: false, title: '', message: '', type: 'success' })}
              >
                Okay
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default Dashboard;
